package llc.ufwa.data.resource.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import llc.ufwa.connection.stream.WrappingInputStream;
import llc.ufwa.data.DefaultEntry;
import llc.ufwa.data.exception.BadDataException;
import llc.ufwa.data.exception.CorruptedDataException;
import llc.ufwa.data.exception.HashBlobException;
import llc.ufwa.data.exception.InvalidIndexException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.ByteArrayIntegerConverter;
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.provider.DefaultResourceProvider;
import llc.ufwa.data.resource.provider.ResourceProvider;
import llc.ufwa.util.StreamUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hash manager backed by file system.
 * 
 * Format:
 *  [SKDLP{N*[KDLP]}]
 * 
 * Where:
 * S == 4 bytes, segment length
 * K == 4 bytes, key length, first key is -1 if the entire segment is empty
 * D == K bytes, key data, serialized
 * L == 4 bytes, data length
 * P == L bytes, data payload
 * {N*[KDLP]} == repeat of key/value data for however many key/vals are in this segment.
 * 
 * Segments are allocated when none that fit can be found.
 * Segments are reused. 
 * Segments are searched for beginning of file to end, and a cache is remembered for which are free in memory.
 * 
 * If a segment is determined to be corrupt it will be erased. 
 * If a segments length value is corrupt, the entire segment is deleted and ignored forever. (This should rarely happen)
 * 
 * 
 * @author Sean Wagner
 *
 * @param <Key>
 */
public class FileHashDataManager<Key> implements HashDataManager<Key, InputStream> {

    private static final Logger logger = LoggerFactory.getLogger(FileHashDataManager.class); 
    
    private static final int BUFFER_SIZE = 2048;
    
    private final File root;
    private final File tempFileDirectory;
    private final Converter<byte [], Integer> converter = new ByteArrayIntegerConverter();
    private final TreeMap<Integer, Set<Integer>> freeSegments = new TreeMap<Integer, Set<Integer>>();

    private final ResourceProvider<String> idProvider = 
        new DefaultResourceProvider<String>() {

            private int id;
            private final int time = (int) (System.currentTimeMillis() % 1000);
            
            @Override
            public synchronized String provide() throws ResourceException {
                return String.valueOf(time) + id++;
            }
            
        };

    private final Converter<Key, byte[]> keySerializer;
    
    /**
     * 
     * @param root
     * @param tempFileDirectory
     */
    public FileHashDataManager(
        final File root,
        final File tempFileDirectory,
        final Converter<Key, byte []> keySerializer
    ) { 
        if(!new File(root.getParent()).exists()) {
            new File(root.getParent()).mkdirs();            
        }
        
        if(root.isDirectory()) {
            throw new RuntimeException("file location must not be a directory " + root);
        }
        
        tempFileDirectory.delete();
        
        if(!tempFileDirectory.exists()) {
            tempFileDirectory.mkdirs();
        }
                
        if(!tempFileDirectory.isDirectory()) {
            throw new RuntimeException("file location must be a directory " + tempFileDirectory);
        }
        
        this.root = root;
        this.tempFileDirectory = tempFileDirectory;
        this.keySerializer = keySerializer;
        
    }
    
    /**
     * Go to index, read data, return
     * @throws  
     */
    @Override
    public Set<Entry<Key, InputStream>> getBlobsAt(int blobIndex) throws HashBlobException {
    	
        try {
            
            final Set<Entry<Key, File>> tempFiles = new HashSet<Entry<Key, File>>();
            
            final RandomAccessFile random = new RandomAccessFile(root, "rws");
            
            try {
                
                random.seek(blobIndex); //go to index
                
                final byte [] intIn = new byte[4];
                
                //read the segment length
                final int segLength;
                final int readSegCount;
                
                {
                
                    readSegCount = random.read(intIn);
                    segLength = converter.convert(intIn);
                
                }
                
                if(segLength < -1 || segLength == 0 || segLength > random.length()) { 
                    
                    logger.error("Invalid seg length " + segLength + " " + (segLength > random.length()) + " for index " + blobIndex);
                    
                    //This segment was not initialized properly, it is bad we need to never attempt to use it, delete
                    throw new BadDataException();
                }
                
                // check if segLength > 1MB, if so, use getBlobs
                if (segLength < 1000000) {
                	random.close();
                	return getBlobs(blobIndex);
                }
                
                if (readSegCount != intIn.length) {
                	throw new CorruptedDataException("bytes read is different from intended");
                }
                
                int dataRead = 0;
                
                //while we approach the end
                while(dataRead + 4 < segLength) {
                                        
                    final File tempFile = new File(tempFileDirectory, this.idProvider.provide());
                     
                    final int fill;
                    
                    final int readCount = random.read(intIn); //read the key length
                           
                    if (readCount != 4) {
                    	throw new CorruptedDataException("bytes read is wrong length");
                    }
                    
                    dataRead += 4;
                    
                    fill = converter.convert(intIn);
                    
                    if(dataRead == 4 && fill < 0) { //return null if there is nothing here, we do this for empty segments
                        return null;
                    }
                    
                    if(dataRead > 4 && fill < 0) { //segment was short recycled one. we are done
                       break;                        
                    }
                    
                    if(fill > segLength) { //corrupt data
                        throw new CorruptedDataException("<1> data is corrupt");
                    }

                    final Key key;
                    
                    {
                        final int currentIndex = segLength - dataRead;
                        
                        //extract a key from the data
                        key = extractKey(random, currentIndex, currentIndex + fill);
                        
                        dataRead += fill;
                        
                    }
                    
                    //extract the data for this key
                    final int dataFill;
                    final int readDataLength;
                    
                    readDataLength = random.read(intIn);
                    
                    if (readDataLength != intIn.length) {
                    	throw new CorruptedDataException("bytes read is wrong length");
                    }
                    
                    dataRead += 4;
                    
                    if(dataRead > segLength) {
                        throw new CorruptedDataException("<3> data is corrupt");
                    }
                    
                    dataFill = converter.convert(intIn);
                    
                    if(dataRead + dataFill > segLength) {
                        throw new CorruptedDataException("<4> data is corrupt");
                    }
                    
                    {
                        
                        final FileOutputStream out = new FileOutputStream(tempFile); //store the data in a temporary file
                        
                        try {
                            
                            final int currentIndex = segLength - dataRead;
                            
                            extractValue(random, currentIndex, currentIndex + dataFill, out);
                            
                            dataRead += dataFill;
                        
                        }
                        finally {
                            
                            out.close();
                            
                        }
                    }

                    //add this to prepared values
                    DefaultEntry<Key, File> entry = new DefaultEntry<Key, File>(key, tempFile);
                    
                    tempFiles.add(entry);
                   
                }
                
            } 
            catch (BadDataException e) {
                
                logger.error("bad data", e);
                throw new CorruptedDataException("Data has been corrupted");                
                
            }
            finally {
                random.close();
            }
            
            //return inputstreams to the new data
            final Set<Entry<Key, InputStream>> returnVals = new HashSet<Entry<Key, InputStream>>();
            
            java.util.Iterator<Entry<Key, File>> itr = tempFiles.iterator();
            
            while (itr.hasNext()) {
                            	
            	final Entry<Key, File> temp = itr.next();
                
                returnVals.add(
                    new DefaultEntry<Key, InputStream>(
                        temp.getKey(),
                        new WrappingInputStream(
                            new FileInputStream(temp.getValue())) {
    
                                @Override
                                public void close() throws IOException {
                                    
                                    super.close();
                                    temp.getValue().delete();
                                    
                                }
                            }
                        )
                    );
                
            }
            
            return returnVals;
            
        } 
        catch (FileNotFoundException e) {
          
            logger.error("Failed to allocate new bucket", e);
            throw new CorruptedDataException("failed to allocate new bucket");
          
        }
        catch (ResourceException e) {
            
            logger.error("Failed to allocate new bucket3", e);
            throw new CorruptedDataException("failed to allocate new bucket3");
            
        }
        catch (IOException e) {
          
            logger.error("Failed to allocate new bucket2", e);
            throw new CorruptedDataException("failed to allocate new bucket2");
          
        }
        
    }
    
    private Key extractKey(
        final RandomAccessFile random, 
        int start, 
        int end
    ) throws CorruptedDataException, IOException, ResourceException {
        
        final int delta = end - start;
        
        //extract a key from the data
        {
          
            //read the key data
            final byte [] buffer = new byte[BUFFER_SIZE];
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            
            try {
            
                for(int i = 0; i < delta; ) {
                                        
                    final int amountToRead;
                    
                    if(i + buffer.length >= delta) {
                        amountToRead = delta - i;    
                    }
                    else {
                        amountToRead = buffer.length;
                    }
                    
                    if(amountToRead == 0) {
                        throw new RuntimeException("ERROR");                        
                    }
                    
                    final int read = random.read(buffer, 0, amountToRead);
                    
                    if(read == 0) {
                        throw new RuntimeException("ERROR");                        
                    }
                    
                    if(read == -1) {
                        
                        break;
                        
                    }
                    
                    out.write(buffer, 0, read);
                    
                    i += read;
                                                
                }
                
                out.flush();
                
                
                return keySerializer.restore(out.toByteArray()); 
                
            }
            finally {
                out.close();
            }
            
        }
        
    }
    
    private void extractValue(
        final RandomAccessFile random, 
        int start, 
        int end,
        final OutputStream out
    ) throws CorruptedDataException, IOException, ResourceException {
        
        final int delta = end - start;
        
        //extract a key from the data
        {
          
            //read the key data
            final byte [] buffer = new byte[BUFFER_SIZE];
            
            try {
            
                for(int i = 0; i < delta; ) {
                                        
                    final int amountToRead;
                    
                    if(i + buffer.length >= delta) {
                        amountToRead = delta - i;    
                    }
                    else {
                        amountToRead = buffer.length;
                    }
                    
                    if(amountToRead == 0) {
                        break;                       
                    }
                    
                    final int read = random.read(buffer, 0, amountToRead);
                    
                    if(read == 0) {
                        throw new RuntimeException("ERROR");                        
                    }
                    
                    if(read == -1) {
                        
                        logger.debug("nothing left to read");
                        break;
                        
                    }
                    
                    out.write(buffer, 0, read);
                    
                    i += read;
                                                
                }
                
                out.flush();
                
                
            }
            finally {
                out.close();
            }
            
        }
        
    }

    /**
     * Go to index, read data, return blobs
     * @throws  
     */
    public Set<Entry<Key, InputStream>> getBlobs(int blobIndex) throws HashBlobException {
    	
        try {
            
            final Set<Entry<Key, InputStream>> returnBlobStreams = new HashSet<Entry<Key, InputStream>>();
            
            final RandomAccessFile random = new RandomAccessFile(root, "rws");
            
            try {
                
                random.seek(blobIndex); //go to index
                
                final byte [] intIn = new byte[4];
                
                //read the segment length
                final int segLength;
                final int readSegCount;
                
                {
                
                    readSegCount = random.read(intIn);
                    segLength = converter.convert(intIn);
                
                }
                
                if(segLength < -1 || segLength == 0 || segLength > random.length()) { 
                    //This segment was not initialized properly, it is bad we need to never attempt to use it, delete
                    throw new BadDataException();
                }
                
                if (readSegCount != intIn.length) {
                	throw new CorruptedDataException("bytes read is different from intended");
                }
                
                int dataRead = 0;
                
                //while we approach the end
                while(dataRead + 4 < segLength) {
                                        
                    final int fill;
                    
                    final int readCount = random.read(intIn); //read the key length
                           
                    if (readCount != 4) {
                    	throw new CorruptedDataException("bytes read is wrong length");
                    }
                    
                    dataRead += 4;
                    
                    fill = converter.convert(intIn);
                                        
                    if(dataRead == 4 && fill < 0) { //return null if there is nothing here, we do this for empty segments
                        return null;
                    }
                    
                    if(dataRead > 4 && fill < 0) { //segment was short recycled one. we are done
                       break;                        
                    }
                    
                    if(fill > segLength) { //corrupt data
                        throw new CorruptedDataException("<1> data is corrupt");
                    }

                    //extract a key from the data
                    final Key key;
                    
                    {
                        
                        final int currentIndex = segLength - dataRead;
                        
                        //extract a key from the data
                        key = extractKey(random, currentIndex, currentIndex + fill);
                        
                        dataRead += fill;
                        
                    }
                    
                    //extract the data for this key                    
                    final int dataFill;
                    final int readDataLength;
                    
                    readDataLength = random.read(intIn);
                    
                    if (readDataLength != intIn.length) {
                    	throw new CorruptedDataException("bytes read is wrong length");
                    }
                    
                    dataRead += 4;
                    
                    if(dataRead > segLength) {
                        throw new CorruptedDataException("<3> data is corrupt");
                    }
                    
                    dataFill = converter.convert(intIn);
                    
                    if(dataRead + dataFill > segLength) {
                        throw new CorruptedDataException("<4> data is corrupt");
                    }
                    
                    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                   
                    {
                    	
                        final int currentIndex = segLength - dataRead;
                        
                        extractValue(random, currentIndex, currentIndex + dataFill, outputStream);
	                        
                        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

                        //add this to prepared values
                        DefaultEntry<Key, InputStream> entry = new DefaultEntry<Key, InputStream>(
                                key, inputStream);
                   
                        returnBlobStreams.add(entry);
                        
                        dataRead += dataFill;
	                    
                	}
                    
                }
                
            }
                
            catch (BadDataException e) {
                
                logger.error("bad data", e);
                throw new CorruptedDataException("Data has been corrupted");                
                
            }
            finally {
                random.close();
            }
            
            //return blobs of new data
            return returnBlobStreams;
            
        }
        catch (FileNotFoundException e) {
          
            logger.error("Failed to allocate new bucket", e);
            throw new CorruptedDataException("failed to allocate new bucket");
          
        }
        catch (ResourceException e) {
            
            logger.error("Failed to allocate new bucket3", e);
            throw new CorruptedDataException("failed to allocate new bucket3");
            
        }
        catch (IOException e) {
          
            logger.error("Failed to allocate new bucket2", e);
            throw new CorruptedDataException("failed to allocate new bucket2");
          
        }
        
    }
    
    /**
     * Clears out the data at this segment freeing it up.
     * 
     * @param blobIndex
     * @throws HashBlobException
     */
    public void eraseBlobs(int blobIndex) throws HashBlobException {
                
        try {
                        
            final RandomAccessFile random = new RandomAccessFile(root, "rws");
          
            try {
              
                random.seek(blobIndex); //go to index
                
                final byte [] intIn = new byte[4];
                
                //read the segment length
                int segLength;
                final int readSegCount;
                
                {
                  
                	readSegCount = random.read(intIn);
                    segLength = converter.convert(intIn);
                  
                }
                
                if (readSegCount != intIn.length) {
                	throw new CorruptedDataException("cannot erase, bytes read is incorrect");
                }
                
                //erase the current segment and add it to free segs
                final byte[] fillToWrite = converter.restore(-1);
                random.write(fillToWrite);
                
                //Check subsequent segments after this one, and merge them if they are empty
                while (true) {
                    
                    final int nextBlobIndex = blobIndex + 8 + segLength;
                    
                    if((nextBlobIndex + 8) < random.length()) {
                        
                        //read the segment length
                        final int nextSegLength;
                        final int nextReadSegCount;
                        
                        final int nextSegFill;
                        final int nextReadSegFill;
                        
                        random.seek(nextBlobIndex);
                        
                        {
                          
                            nextReadSegCount = random.read(intIn);
                            nextSegLength = converter.convert(intIn);
                                                        
                            if (nextReadSegCount != intIn.length) {
                                throw new CorruptedDataException("cannot erase, bytes read is incorrect");
                            }
                            
                            nextReadSegFill = random.read(intIn);
                            nextSegFill = converter.convert(intIn);
                            
                            if (nextReadSegFill != intIn.length) {
                                throw new CorruptedDataException("cannot erase, bytes read is incorrect");
                            }
                            
                        }
                        
                        //next segment is empty, merge it into this one
                        if(nextSegFill < 0) {
                            segLength = merge(nextBlobIndex, nextSegLength, blobIndex, segLength, random);                            
                        }
                        else {
                            break;
                        }
                        
                    }
                    else {
                        break;
                    }
                    
                }
                
                //if this is the last blob and it is now empty, delete it.
                if(blobIndex + 8 + segLength >= random.length()) {
                    random.setLength(blobIndex);                    
                }
                else {
                
                    Set<Integer> segs = this.freeSegments.get(segLength);
                    
                    if(segs == null) {
                        
                        segs = new HashSet<Integer>();
                        this.freeSegments.put(segLength, segs);
                        
                    }
                    
                    segs.add(blobIndex);
                    
                }
                
            } 
            finally {
                random.close();
            }
            
        } 
        catch (FileNotFoundException e) {
          
            logger.error("Failed to allocate new bucket", e);
            throw new CorruptedDataException("failed to allocate new bucket");
          
        }
        catch (ResourceException e) {
            
            logger.error("Failed to allocate new bucket3", e);
            throw new CorruptedDataException("failed to allocate new bucket3");
            
        }
        catch (IOException e) {
          
            logger.error("Failed to allocate new bucket2", e);
            throw new CorruptedDataException("failed to allocate new bucket2");
          
        }
        
    }

    @Override
    public int setBlobs(int blobIndex, Set<Entry<Key, InputStream>> blobs) throws HashBlobException {
                
        if(blobs.size() == 0 || blobs == null) {
            throw new HashBlobException("You must pass in data for set. Use erase instead");
        }
        
        //first copy in all the original data into seperate files to preserve it.
        final Set<Entry<Key, File>> tempFiles = new HashSet<Entry<Key, File>>();
        final Map<Key, byte []> keyDatas = new HashMap<Key, byte []>();
        
        int totalSize = 0;
        
        java.util.Iterator<Entry<Key, InputStream>> itr = blobs.iterator();
        
        while (itr.hasNext()) {
        	            
        	final Entry<Key, InputStream> entry = itr.next();
            
            final File tempFile;
            
            try {
                tempFile = new File(this.tempFileDirectory, idProvider.provide());
            }
            catch (ResourceException e1) {
                throw new CorruptedDataException("failed to get temp file name");
            }

            tempFiles.add(new DefaultEntry<Key, File>(entry.getKey(), tempFile));
            
            try {
                
                final OutputStream output = new FileOutputStream(tempFile);
                
                try {
                    StreamUtil.copyTo(entry.getValue(), output);
                }
                finally {
                    output.close();
                }
                
                if(!tempFile.exists()) { 
                	throw new CorruptedDataException("Failed to allocate temp file");
                }
                
                final byte [] keyData = keySerializer.convert(entry.getKey()); 
                
                totalSize += tempFile.length();
                totalSize += keyData.length;
                totalSize += 8; //fill int and key length
                
                keyDatas.put(entry.getKey(), keyData);
                
            }
            catch (IOException e) {
                
                logger.error("Failed to allocate temp file");
                throw new CorruptedDataException("Failed to allocate temp file");
                
            } catch (ResourceException e) {
                
                logger.error("Failed to serialize when allocating temp file");
                throw new CorruptedDataException("Failed to serialize when allocating temp file");
                
            }
            
        }
                
        final int writingIndex;
        
        //if the data was at an existing index we need to check if the segment is big enough for all the new data.
        if(blobIndex >= 0) {
        	            
            try {
              
                final RandomAccessFile random = new RandomAccessFile(root, "rws");
              
                try {
                  
                    final long length = random.length();
                  
                    if((blobIndex + 4) >= length) {
                        throw new InvalidIndexException("invalid index " + blobIndex + " " + length);
                    }
                  
                    final byte [] currentKeyIn = new byte[4];
                    random.seek(blobIndex);
                
                    final int segLength;
                    final int readSegCount;
                  
                    //read in this segments length
                    {
                      
                    	readSegCount = random.read(currentKeyIn);
                        segLength = converter.convert(currentKeyIn);
                      
                    }
                    
                    if (readSegCount != currentKeyIn.length) {
                    	throw new CorruptedDataException("<1> set failed, bytes read is wrong");
                    }
                  
                    //if the segment is too small for the new data
                    if(segLength < totalSize) {
                        
                        //erase the current segment and add it to free segs
                        final byte[] fill = converter.restore(-1);
                        random.write(fill);
                                                
                        Set<Integer> segs = this.freeSegments.get(segLength);
                        
                        if(segs == null) {
                            
                            segs = new HashSet<Integer>();
                            this.freeSegments.put(segLength, segs);
                            
                        }
                        
                        segs.add(blobIndex);
                        
                        //segment isn't big enough, lets find a new free segment.
                        writingIndex = findFreeSegment(totalSize);
                        
                    }
                    else { //current segment is fine.
                        writingIndex = blobIndex;
                    }
               
                } 
                finally {
                    random.close();
                }
              
            } 
            catch (FileNotFoundException e) {
              
                logger.error("Failed to allocate new bucket", e);
                throw new CorruptedDataException("failed to allocate new bucket");
              
            }
            catch (ResourceException e) {
                
                logger.error("Failed to allocate new bucket3", e);
                throw new CorruptedDataException("failed to allocate new bucket3");
                
            }
            catch (IOException e) {
              
                logger.error("Failed to allocate new bucket2", e);
                throw new CorruptedDataException("failed to allocate new bucket2");
              
            }
        }
        else {
            
            //-1 blob index means we need to just fetch a new segment, data didn't exist before.
            writingIndex = findFreeSegment(totalSize);
            
        }
                
        //write this shit in
        try {
            
            final RandomAccessFile random = new RandomAccessFile(root, "rws");
          
            try {
              
                final long length = random.length();
              
                if((writingIndex + 4) >= length) {
                    throw new InvalidIndexException("invalid index " + (writingIndex + 4) + " " + length + root.length());
                }
              
                final byte [] currentKeyIn = new byte[4];
                random.seek(writingIndex);
                
                final int segLength;
                final int readSegCount;
                
                {
                    
                	readSegCount = random.read(currentKeyIn);
                    segLength = converter.convert(currentKeyIn);
                                      
                }
                
                if (readSegCount != currentKeyIn.length) {
                	throw new CorruptedDataException("<2> set failed, bytes read is wrong");
                }
                
                random.seek(writingIndex + 8);
                
                 //skip the length
                
                int totalWritten = 0;
                
                byte [] firstKeyData = null;
                                
                final Iterator<Entry<Key, File>> itr2 = tempFiles.iterator();
              
                //write out the keys and their data
                while (itr2.hasNext()) {
                	                    
                	final Entry<Key, File> tempFile = itr2.next();
                    
                    final byte[] keyData = keyDatas.get(tempFile.getKey());
                    
                    if(firstKeyData == null) {
                        firstKeyData = keyData;
                    }
                    else {
                        
                        final byte[] keyLengthBytes = converter.restore(keyData.length);
                        random.write(keyLengthBytes);
                        
                    }
                    
                    totalWritten += keyData.length + 8;
           
                    random.write(keyData);
                    
                    final int fileLength = (int)tempFile.getValue().length();
                    
                    final byte[] sizeBytes = converter.restore(fileLength);
                    
                    random.write(sizeBytes);
                    
                    if(!tempFile.getValue().exists()) {
                    	throw new CorruptedDataException("file doesn't exist " + tempFile.getValue().getAbsolutePath());
                    }
                    
                    final FileInputStream input = new FileInputStream(tempFile.getValue());
                    
                    try {
                        
                        final byte [] buffer = new byte[BUFFER_SIZE];
                        int totalRead = 0;
                        
                        while(true) {
                                                        
                            final int read = input.read(buffer);
                                                        
                            if(read > 0) {
                                
                                totalRead += read;
                                
                                if(totalRead > fileLength) {
                                    throw new CorruptedDataException("File system is lying");
                                }
                                
                                totalWritten += read;
                                
                                random.write(buffer, 0, read);
                                
                            }
                            else {
                                break;
                            }
                            
                        }
                        
                    }
                    finally {
                        
                        input.close();
                        tempFile.getValue().delete();
                        
                    }
                    
                }
                                
                //if our segment is bigger than our data by 4 or more bytes, write a terminating -1                
                if(segLength >= totalWritten + 4) {
                    
                    final byte[] terminatorBytes = converter.restore(-1);
                    random.write(terminatorBytes);
                    
                }
                                
                //We are setting the first keylength last because that is what is used to determine if this segment is used. 
                //This will hopefully make it as crash proof as possible.
                if(firstKeyData != null) {

                    random.seek(writingIndex + 4); //skip the length
                    
                    final byte[] keyLengthBytes = converter.restore(firstKeyData.length);
                    
                    random.write(keyLengthBytes);
                    
                }
                
            } 
            finally {
                random.close();
            }
          
        } 
        catch (FileNotFoundException e) {
          
            logger.error("Failed to allocate new bucket", e);
            throw new CorruptedDataException("failed to allocate new bucket");
          
        }
        catch (ResourceException e) {
            
            logger.error("Failed to allocate new bucket3", e);
            throw new CorruptedDataException("failed to allocate new bucket3");
            
        }
        catch (IOException e) {
          
            logger.error("Failed to allocate new bucket2", e);
            throw new CorruptedDataException("failed to allocate new bucket2");
          
        }
        
        return writingIndex;
        
    }
    
    private int findFreeSegment(final int totalSize) throws HashBlobException {
                
        final SortedMap<Integer, Set<Integer>> tail = this.freeSegments.tailMap(totalSize, true);
        
        int returnVal = -1;
        
        //first look into the existing free segments
        if(tail.size() == 0) {
            
            //Start at the beginning of the file
            int blobIndex = 0;
            //this will only contain the index of the previous segment if it was empty but not big enough.
            int previousFreeSegIndex = -1;
            
            while(true) {
                                                
                try {
                    
                    final RandomAccessFile random = new RandomAccessFile(root, "rws");
                                      
                    try {
                        
                        final long length = random.length();
                      
                        if((blobIndex + 4) >= length) {
                                                        
                            //end of file reached found nothing;
                            blobIndex = -1;
                            break;
                            
                        }
                        
                        final byte [] currentKeyIn = new byte[4];
                                                
                    	random.seek(blobIndex);
                        
                        int segLength;
                        final int fill;
                        
                        //read in the segment lenght
                        {
                          
                        	random.read(currentKeyIn);
                            
                            segLength = converter.convert(currentKeyIn);
                            
                        }
    
                        //if the segment length is invalid value or points to something bigger that the file, throw corrupted exception
                        if(segLength < 0 || segLength + blobIndex > random.length()) {
                            throw new CorruptedDataException("seg is corrupt " + blobIndex + " seg " + segLength + " file length " + random.length());    
                        }
                        
                        //read in the length of data in this segment.
                        {
                            
                            random.read(currentKeyIn);
                        
                            fill = converter.convert(currentKeyIn);
                            
                        }
                        
                        if(fill == -1) { //encountered free segment, add it to known free.
                            
                        	if(segLength >= totalSize) {
                        	    
                                returnVal = blobIndex;
                                break;
                                
                            }
                            else {
                                
                                if(previousFreeSegIndex > 0) {
                                                                  
                                    //if previous seg was free, merge these two segments.
                                    final byte [] previousCurrentKeyIn = new byte[4];
                                    
                                    random.seek(previousFreeSegIndex);
                                    
                                    final int previousSegLength;
                                    
                                    //read in the segment lenght
                                    {
                                      
                                        random.read(previousCurrentKeyIn);
                                        
                                        previousSegLength = converter.convert(previousCurrentKeyIn);
                                        
                                    }
                                    
                                    //if the segment length is invalid value or points to something bigger that the file, throw corrupted exception
                                    if(previousSegLength < 0 || previousSegLength + previousFreeSegIndex > random.length()) {
                                        throw new CorruptedDataException("previous seg is corrupt " + previousFreeSegIndex + " seg " + previousSegLength + " file length " + random.length());    
                                    }
                                    
                                    final int newSegLength = merge(blobIndex, segLength, previousFreeSegIndex, previousSegLength, random);
                                    
                                    blobIndex = previousFreeSegIndex;
                                    previousFreeSegIndex = -1;
                                    segLength = newSegLength;
                                    
                                }
                                else {
                                    previousFreeSegIndex = blobIndex;//assign previous free seg index.
                                }
                                
                                Set<Integer> segs = this.freeSegments.get(segLength);
                                
                                if(segs == null) {
                                
                                    segs = new HashSet<Integer>();
                                    this.freeSegments.put(segLength, segs);
                                    
                                }
                                
                                segs.add(blobIndex);
                                
                            }
                            
                        }
                        else {
                            
                            //segment was filled so the previous one will be -1 so it wont be merged
                            previousFreeSegIndex = -1;
                            
                        }
                        
                        blobIndex += 8 + segLength; //skip to next segment
                        
                    } 
                    finally {
                        random.close();
                    }
                  
                } 
                catch (FileNotFoundException e) {
                  
                    logger.error("Failed to allocate new bucket", e);
                  
                    throw new CorruptedDataException("failed to allocate new bucket");
                  
                }
                catch (ResourceException e) {
                    
                    logger.error("Failed to allocate new bucket3", e);
                     
                    throw new CorruptedDataException("failed to allocate new bucket3");
                    
                }
                catch (IOException e) {
                  
                    logger.error("Failed to allocate new bucket2", e);
                  
                    throw new CorruptedDataException("failed to allocate new bucket2");
                  
                }
                
            }
            
            if(returnVal < 0) {
                
                //create new segment
            	
                try {
                    
                    final RandomAccessFile random = new RandomAccessFile(root, "rws");
                  
                    try {
                      
                        final long length = random.length();
                      
                        returnVal = (int)length;
                        
                        final byte [] lengthData = converter.restore(totalSize);
                        
                        random.seek(length);
                        
                        random.write(lengthData);
                        
                        final byte [] fillData = converter.restore(-1);
                    
                        random.write(fillData);
                        
                        random.seek(totalSize + length + 4); //seek to last 4 bytes of seg.
                        random.write(fillData);
                                             
                    } 
                    finally {
                        random.close();
                    }
                  
                } 
                catch (FileNotFoundException e) {
                  
                    logger.error("Failed to allocate new bucket", e);
                  
                    throw new CorruptedDataException("failed to allocate new bucket");
                  
                }
                catch (ResourceException e) {
                    
                    logger.error("Failed to allocate new bucket3", e);
                     
                    throw new CorruptedDataException("failed to allocate new bucket3");
                    
                }
                catch (IOException e) {
                  
                    logger.error("Failed to allocate new bucket2", e);
                  
                    throw new CorruptedDataException("failed to allocate new bucket2");
                  
                }
                
            }
            
            
        }
        else {
           
            
            //use existing free segment that we already know about.
            
            Set<Integer> list = tail.get(tail.firstKey());
            
            final Iterator<Integer> iterator = list.iterator();
            
            returnVal = iterator.next();
            iterator.remove();
            
            if(list.size() == 0) {
                this.freeSegments.remove(tail.firstKey());
            }
                    
        }
        
        splitIfPossible(totalSize, returnVal);
       
        return returnVal;
        
    }
    
    /**
     * 
     * @param blobIndex
     * @param segLength
     * @param previousFreeSegIndex
     * @param random
     * @return
     * @throws IOException
     * @throws ResourceException
     * @throws CorruptedDataException
     */
    private int merge(
        final int blobIndex, 
        final int segLength,
        final int previousFreeSegIndex, 
        final int previousSegLength,
        final RandomAccessFile random
    ) throws IOException, ResourceException, CorruptedDataException {
        
        {
            
            final Set<Integer> oldFreeSegs = freeSegments.get(segLength);
            
            if(oldFreeSegs != null) {
                
                oldFreeSegs.remove(blobIndex);
                
                if(oldFreeSegs.size() == 0) {
                    freeSegments.remove(segLength);
                }
                
            }
            
        }
        
        {
            
            final Set<Integer> oldFreeSegs = freeSegments.get(previousSegLength);
            
            if(oldFreeSegs != null) {
                
                oldFreeSegs.remove(previousFreeSegIndex);
                
                if(oldFreeSegs.size() == 0) {
                    freeSegments.remove(previousSegLength);
                }
                
            }
            
            
        }
        
        final byte [] lengthData = converter.restore(segLength + previousSegLength + 8);
        
        random.seek(previousFreeSegIndex);
        
        random.write(lengthData); //merge the 2 free segments
        
        random.seek(previousFreeSegIndex + 4);
        
        random.write(converter.restore(-1)); //merge the 2 free segments
        
        return segLength + previousSegLength + 8;
        
    }
    
    private void splitIfPossible(final int totalSize, final int blobIndex) throws CorruptedDataException {
        
        if(blobIndex >= 0) {
            
            try {
                
                final RandomAccessFile random = new RandomAccessFile(root, "rws");
                
                try {
                    
                    final byte [] currentKeyIn = new byte[4];
                    
                    random.seek(blobIndex);
                    
                    int segLength;
                    
                    //read in the segment lenght
                    {
                      
                        random.read(currentKeyIn);
                        
                        segLength = converter.convert(currentKeyIn);
                        
                    }
                    
                    if((segLength - totalSize) > 8) {
                                                                        
                        final byte [] lengthData = converter.restore(totalSize);
                        
                        random.seek(blobIndex);
                        
                        random.write(lengthData);
                        
                        random.seek(blobIndex + 4);
                        
                        random.write(converter.restore(-1));
                        
                        final byte [] secondLengthData = converter.restore(segLength - totalSize - 8);
                        
                        random.seek(blobIndex + totalSize + 8);
                        
                        random.write(secondLengthData);
                        
                        random.seek(blobIndex + totalSize + 12);
                        
                        random.write(converter.restore(-1)); //merge the 2 free segments
                    
                    }
                    
                }
                
                finally {
                    random.close();
                }
                
            }
            catch (FileNotFoundException e) {
                
                logger.error("Failed to allocate new bucket", e);
              
                throw new CorruptedDataException("failed to allocate new bucket");
              
            }
            catch (ResourceException e) {
                
                logger.error("Failed to allocate new bucket3", e);
                 
                throw new CorruptedDataException("failed to allocate new bucket3");
                
            }
            catch (IOException e) {
              
                logger.error("Failed to allocate new bucket2", e);
              
                throw new CorruptedDataException("failed to allocate new bucket2");
              
            }
        
        }
        
    }

    @Override
    public void clear() {       
        
    	root.delete();        
        freeSegments.clear();
        
    }

}
