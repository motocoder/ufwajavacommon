package llc.ufwa.data.resource.cache;

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
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import llc.ufwa.connection.stream.WrappingInputStream;
import llc.ufwa.data.DefaultEntry;
import llc.ufwa.data.exception.HashBlobException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.ByteArrayIntegerConverter;
import llc.ufwa.data.resource.Converter;
import llc.ufwa.data.resource.provider.DefaultResourceProvider;
import llc.ufwa.data.resource.provider.ResourceProvider;
import llc.ufwa.util.DataUtils;
import llc.ufwa.util.StreamUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHashDataManager<Key> implements HashDataManager<Key, InputStream> {

    private static final Logger logger = LoggerFactory.getLogger(FileHashDataManager.class);
    
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
    
    public FileHashDataManager(
        final File root,
        final File tempFileDirectory
    ) { 
        
        if(root.isDirectory()) {
            throw new RuntimeException("file location must not be a directory " + root);
        }
        
        if(!tempFileDirectory.exists()) {
            tempFileDirectory.mkdirs();
        }
        
        if(!tempFileDirectory.isDirectory()) {
            throw new RuntimeException("file location must be a directory " + tempFileDirectory);
        }
        
        this.root = root;
        this.tempFileDirectory = tempFileDirectory;
        
    }
    
    /**
     * Go to index, read data, return
     */
    @Override
    public Set<Entry<Key, InputStream>> getBlobsAt(int blobIndex) throws HashBlobException {
        
        try {
            
            final Set<Entry<Key, File>> tempFiles = new HashSet<Entry<Key, File>>();
            
            final RandomAccessFile random = new RandomAccessFile(root, "rws");
          
            try {
              
                random.seek(blobIndex);
                
                final byte [] intIn = new byte[4];
                
                final int segLength;
                
                {
                  
                    random.read(intIn);
                
                    segLength = converter.convert(intIn);
                  
                }
                
                logger.debug("reading segLength " + segLength);
                
                int dataRead = 0;
                
                while(dataRead + 4 < segLength) {

                    final File tempFile = new File(tempFileDirectory, this.idProvider.provide());
                     
                    final int fill;
                    
                    random.read(intIn); //read the key length
                    
                    dataRead += 4;
                    
                    fill = converter.convert(intIn);
                    
                    logger.debug("fill " + fill);
                    
                    
                    
                    logger.debug("reading keyLength " + fill);
                    
                    logger.debug("currentPointer: " + random.getFilePointer());
                    
                    if(dataRead == 4 && fill < 0) {
                        return null;
                    }
                    
                    if(dataRead > 4 && fill < 0) { //segment was short recycled one.
                        
                        logger.debug("recycling");
                        break;
                        
                    }

                    final Key key;
                    
                    {
                      
                        //read the key data
                        final byte [] buffer = new byte[1024];
                        final ByteArrayOutputStream out = new ByteArrayOutputStream();
                        
                        try {
                        
                            for(int i = 0; i < fill; ) {
                                
                                final int amountToRead;
                                
                                if(i + buffer.length > fill) {
                                    amountToRead = fill - i;    
                                }
                                else {
                                    amountToRead = buffer.length;
                                }
                                
                                logger.debug("amount to read " + amountToRead);
                                
                                final int read = random.read(buffer, 0, amountToRead);
                                
                                logger.debug("read " + read);
                                
                                dataRead += read;
                                
                                if(read != amountToRead) {
                                    throw new HashBlobException("cannot read entire segment");
                                }
                                
                                out.write(buffer, 0, read);
                                out.flush();
                                
                                if(read < buffer.length) {
                                    break;
                                }
                                                            
                            }
                            
                            key = DataUtils.deserialize(out.toByteArray()); 
                            
                        }
                        catch (ClassNotFoundException e) {
                            throw new HashBlobException("Could no deserialize key", e);
                        }
                        finally {
                            out.close();
                        }
                        
                    }
                    
                    logger.debug("reading value for key: " + key);
                    
                    final int dataFill;
                    
                    logger.debug("currentPointer: " + random.getFilePointer());
                    random.read(intIn);
                    
                    dataRead += 4;
                    
                    dataFill = converter.convert(intIn);
                    
                    logger.debug("reading data fill " + dataFill);
                    logger.debug("reading dataRead before data: " + dataRead);
                    
                    {
                        final byte [] buffer = new byte[1024];
                        final FileOutputStream out = new FileOutputStream(tempFile);
                        
                        try {
                        
                            for(int i = 0; i < dataFill; ) {
                                
                                final int amountToRead;
                                
                                if(i + buffer.length > dataFill) {
                                    amountToRead = dataFill - i;    
                                }
                                else {
                                    amountToRead = buffer.length;
                                }
                                
                                logger.debug("amount to read " + amountToRead);
                                
                                final int read = random.read(buffer, 0, amountToRead);
                                
                                dataRead += read;
                                
                                if(read != amountToRead) {
                                    throw new HashBlobException("cannot read entire segment");
                                }
                                
                                out.write(buffer, 0, read);
                                out.flush();
                                
                                if(read < buffer.length) {
                                    break;
                                }
                                                            
                            }
                            
                        }
                        finally {
                            out.close();
                        }
                    }
                    
                    DefaultEntry<Key, File> entry = new DefaultEntry<Key, File>(key, tempFile);
                    
                    tempFiles.add(entry);
                   
                }
                
            } 
            finally {
                random.close();
            }
            
            final Set<Entry<Key, InputStream>> returnVals = new HashSet<Entry<Key, InputStream>>();
            
            for(final Entry<Key, File> temp : tempFiles) {
                
                temp.getValue().deleteOnExit();
                
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
          
            throw new HashBlobException("failed to allocate new bucket", e);
          
        }
        catch (ResourceException e) {
            
            logger.error("Failed to allocate new bucket3", e);
             
            throw new HashBlobException("failed to allocate new bucket3", e);
            
        }
        catch (IOException e) {
          
            logger.error("Failed to allocate new bucket2", e);
          
            throw new HashBlobException("failed to allocate new bucket2", e);
          
        }
        
    }
    
    @Override
    public int setBlobs(int blobIndex, Set<Entry<Key, InputStream>> blobs) throws HashBlobException {
        
        //first copy in all the original data into seperate files to preserve it.
        final Set<Entry<Key, File>> tempFiles = new HashSet<Entry<Key, File>>();
        final Map<Key, byte []> keyDatas = new HashMap<Key, byte []>();
        
        int totalSize = 0;
        
        for(final Entry<Key, InputStream> entry : blobs) {
            
            final File tempFile;
            
            try {
                tempFile = new File(this.tempFileDirectory, idProvider.provide());
            } 
            catch (ResourceException e1) {
                throw new HashBlobException("failed to get temp file name");
            }
            
            tempFiles.add(new DefaultEntry<Key, File>(entry.getKey(), tempFile));
            
            try {
                
                final OutputStream output = new FileOutputStream(tempFile);
                
                try {
                    StreamUtil.copyTo(entry.getValue(), output);
                }
                finally {
                    
                    output.close();
                    
                    tempFile.deleteOnExit();
                    
                }
                
                logger.debug(entry.getKey() + " file size after temp " + tempFile.length());
                
                final byte [] keyData = DataUtils.serialize(entry.getKey());
                
                totalSize += tempFile.length();
                totalSize += keyData.length;
                totalSize += 8; //fill int and key length
                
                keyDatas.put(entry.getKey(), keyData);
                
            }
            catch (IOException e) {
                
                logger.error("Failed to allocate temp file");
                throw new HashBlobException("Failed to allocate temp file");
                
            }
            
        }
        
        final int writtingIndex;
        
        //if the data was at an existing index we need to check if the segment is big enough for all the new data.
        if(blobIndex >= 0) {
            
            logger.debug("writting to existing");
            
            try {
              
                final RandomAccessFile random = new RandomAccessFile(root, "rws");
              
                try {
                  
                    final long length = random.length();
                  
                    if((blobIndex + 4) >= length) {
                        throw new HashBlobException("invalid index");
                    }
                  
                    final byte [] currentKeyIn = new byte[4];
                    random.seek(blobIndex);
                
                    final int segLength;
                  
                    {
                      
                        random.read(currentKeyIn);
                    
                        segLength = converter.convert(currentKeyIn);
                      
                    }
                  
                    if(segLength < totalSize) {
                        
                        logger.debug("erasing");
                        
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
                        writtingIndex = findFreeSegment(totalSize);
                        
                    }
                    else { //current segment is fine.
                        writtingIndex = blobIndex;
                    }
               
                } 
                finally {
                    random.close();
                }
              
            } 
            catch (FileNotFoundException e) {
              
                logger.error("Failed to allocate new bucket", e);
              
                throw new HashBlobException("failed to allocate new bucket", e);
              
            }
            catch (ResourceException e) {
                
                logger.error("Failed to allocate new bucket3", e);
                 
                throw new HashBlobException("failed to allocate new bucket3", e);
                
            }
            catch (IOException e) {
              
                logger.error("Failed to allocate new bucket2", e);
              
                throw new HashBlobException("failed to allocate new bucket2", e);
              
            }
        }
        else {
            
            //-1 blob index means we need to just fetch a new segment, data didn't exist before.
            writtingIndex = findFreeSegment(totalSize);
            
        }
        
        logger.debug("writting this shit");
        
        //write this shit in
        try {
            
            final RandomAccessFile random = new RandomAccessFile(root, "rws");
          
            try {
              
                final long length = random.length();
              
                if((writtingIndex + 4) >= length) {
                    throw new HashBlobException("invalid index");
                }
              
                random.seek(writtingIndex + 4); //skip the length
                
                int totalWritten = 0;
                
                for(final Entry<Key, File> tempFile : tempFiles) {
                    
                    final byte[] keyData = keyDatas.get(tempFile.getKey());
                    final byte[] keyLengthBytes = converter.restore(keyData.length);
                    
                    logger.debug("writting keyLength " + keyData.length);
                   
                    totalWritten += keyData.length + 8;
                            
                    random.write(keyLengthBytes);
                    random.write(keyData);
                    
                    final int fileLength = (int)tempFile.getValue().length();
                    
                    final byte[] sizeBytes = converter.restore(fileLength);
                    
                    logger.debug("writting value size " + fileLength + " for key " + tempFile.getKey());
                    
                    random.write(sizeBytes);
                    
                    final FileInputStream input = new FileInputStream(tempFile.getValue());
                    
                    try {
                        
                        final byte [] buffer = new byte[1024];
                        int totalRead = 0;
                        
                        while(true) {
                            
                            final int read = input.read(buffer);
                            
                            if(read > 0) {
                                
                                totalRead += read;
                                
                                if(totalRead > fileLength) {
                                    throw new RuntimeException("File system is lieing");
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
                
                if(totalSize >= totalWritten + 4) {
                    
                    logger.debug("terminating");
                    
                    final byte[] terminatorBytes = converter.restore(-1);
                    random.write(terminatorBytes);
                    
                }
                
            } 
            finally {
                random.close();
            }
          
        } 
        catch (FileNotFoundException e) {
          
            logger.error("Failed to allocate new bucket", e);
          
            throw new HashBlobException("failed to allocate new bucket", e);
          
        }
        catch (ResourceException e) {
            
            logger.error("Failed to allocate new bucket3", e);
             
            throw new HashBlobException("failed to allocate new bucket3", e);
            
        }
        catch (IOException e) {
          
            logger.error("Failed to allocate new bucket2", e);
          
            throw new HashBlobException("failed to allocate new bucket2", e);
          
        }
        
        return writtingIndex;
        
        
    }
    
    private int findFreeSegment(final int totalSize) throws HashBlobException {
        
        logger.debug("finding free seg");
        
        final NavigableMap<Integer, Set<Integer>> tail = this.freeSegments.tailMap(totalSize, true);
        
        int returnVal = -1;
        
        //first look into the existing free segments
        if(tail.size() == 0) {
            
            logger.debug("pulling from existing pool");
            
            int blobIndex = 0;
            
            while(true) {
                
                try {
                    
                    final RandomAccessFile random = new RandomAccessFile(root, "rws");
                  
                    try {
                        
                        final long length = random.length();
                      
                        if((blobIndex + 4) >= length) {
                            
                            logger.debug("reached end of file " + blobIndex + " " + length);
                            
                            //end of file reached found nothing;
                            blobIndex = -1;
                            break;
                            
                        }
                      
                        final byte [] currentKeyIn = new byte[4];
                        random.seek(blobIndex);
                    
                        logger.debug("position " + random.getFilePointer());
                        
                        final int segLength;
                        final int fill;
                        
                        {
                          
                            random.read(currentKeyIn);
                        
                            segLength = converter.convert(currentKeyIn);
                          
                        }
    
                        {
                            
                            random.read(currentKeyIn);
                        
                            fill = converter.convert(currentKeyIn);
                          
                        }
                        
                        logger.debug("finding length " + segLength);
                        logger.debug("fill " + fill);
                        logger.debug("finding needs " + totalSize);
                        
                        if(fill == -1) { //encountered free segment, add it to known free.
                            
                            if(segLength >= totalSize) {
                                
                                logger.debug("found existing");
                                returnVal = blobIndex;
                                break;
                                
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
                        
                        blobIndex += 8 + segLength;
                        
                    } 
                    finally {
                        random.close();
                    }
                  
                } 
                catch (FileNotFoundException e) {
                  
                    logger.error("Failed to allocate new bucket", e);
                  
                    throw new HashBlobException("failed to allocate new bucket", e);
                  
                }
                catch (ResourceException e) {
                    
                    logger.error("Failed to allocate new bucket3", e);
                     
                    throw new HashBlobException("failed to allocate new bucket3", e);
                    
                }
                catch (IOException e) {
                  
                    logger.error("Failed to allocate new bucket2", e);
                  
                    throw new HashBlobException("failed to allocate new bucket2", e);
                  
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
                        
                        logger.debug("created new seg length " + totalSize);
                        
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
                  
                    throw new HashBlobException("failed to allocate new bucket", e);
                  
                }
                catch (ResourceException e) {
                    
                    logger.error("Failed to allocate new bucket3", e);
                     
                    throw new HashBlobException("failed to allocate new bucket3", e);
                    
                }
                catch (IOException e) {
                  
                    logger.error("Failed to allocate new bucket2", e);
                  
                    throw new HashBlobException("failed to allocate new bucket2", e);
                  
                }
                
            }
            else {
                logger.debug("using existing segment at " + returnVal);
            }
            
        }
        else {
            
            logger.debug("using existing free seg");
            
            final Entry<Integer, Set<Integer>> first = tail.firstEntry();
            
            final Set<Integer> list = first.getValue();
            
            if(list.size() == 1) {
                this.freeSegments.remove(first.getKey());
            }
            
            returnVal = list.iterator().next();
            
        }
        
        return returnVal;
        
    }

}
