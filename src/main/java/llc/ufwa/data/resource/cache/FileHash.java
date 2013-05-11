package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.ByteArrayLongConverter;
import llc.ufwa.data.resource.Converter;

import org.apache.log4j.Logger;

/**
 * this is a file backed hashing mechanism.
 * 
 * @author Sean Wagner
 *
 */
public class FileHash {
    
    private static final Logger logger = Logger.getLogger(FileHash.class);

    private final int hashSize;
    private final int maxKeySize;
    private final File file;
    private final Converter<byte [], Long> converter = new ByteArrayLongConverter();
    private final int SLICE_SIZE;
    
    public FileHash(
        final int maxKeySize,
        final int hashSize,
        final File file
     
    ) {
        
        if(file.isDirectory()) {
            throw new RuntimeException("hash file location must not be a directory");
        }
        
        this.file = file;
        
        this.hashSize = hashSize;
        this.maxKeySize = maxKeySize;
        
        this.SLICE_SIZE = maxKeySize + 24;
        
    }
    
    public void put(final String key, final FileSegment segment) {
        
        if(key.length() > maxKeySize) {
            throw new IllegalArgumentException("key too long");
        }
        
        if(key.equals("")) {
            throw new IllegalArgumentException("key must not be empty");
        }
        
        final int keyHash = key.hashCode();
        
        final int limitedHash = keyHash % hashSize;
        
        logger.debug("limitedHash " + limitedHash);
        
        int hashedIndex = limitedHash * (SLICE_SIZE);
        
        final int start = hashedIndex;
        int keysBefore = 0;
        
        while(true) {
        
            try {
                
                final RandomAccessFile random = new RandomAccessFile(file, "rws");
               
                final byte [] currentKeyIn = new byte[maxKeySize];
                random.seek(hashedIndex);
                
                //read in key at this hash location.
                final int read = random.read(currentKeyIn);
                
                final boolean write;
                
                if(read <= 0) { //nothing there, write
                
                    write = true;    
                    
                }
                else {
                    
                    int term = 0;
                    
                    for(; term < currentKeyIn.length; term++) {
                        
                        if(currentKeyIn[term] == '\0') {
                            break;
                        }
                    }

                    logger.debug("term: " + term);
                    
                    final String currentKey = new String(currentKeyIn, 0, term);    
                    
                    logger.debug("currentKey: '" + currentKey + "'");
                    // if same key, or empty key, write
                    if(currentKey.equals(key) || currentKey.equals("")) {
                        write = true;
                    }
                    else {//skip to next hash location
                        write = false;
                    }
                    
                }
                
                if(write) {
                
                    final byte[] bytesIndex = converter.restore(segment.getIndex());
                    final byte[] bytesLength = converter.restore(segment.getLength());
                    final byte[] bytesKeysBefore = converter.restore((long)keysBefore);
                    
                    final byte[] keyBytes = key.getBytes();
                    
                    final byte[] fullKey = Arrays.copyOf(keyBytes, maxKeySize);
                           
                    random.seek(hashedIndex);
                    
                    random.write(fullKey);
                    random.write(bytesIndex);
                    random.write(bytesLength);
                    random.write(bytesKeysBefore);
        
                    random.close();
                    break;
                    
                }
                else {
                    
                    logger.debug("hash Collision");
                    
                    hashedIndex += SLICE_SIZE;
                    keysBefore++;
                    
                    logger.debug("hashIndex " + hashedIndex);
                    
                    if(hashedIndex >= hashSize * (SLICE_SIZE)) {
                        
                        logger.debug("index: " + hashedIndex + " exceeds size: " + (hashSize * (SLICE_SIZE)));
                        
                        hashedIndex = 0;
                    }
                    else if(hashedIndex == start) {
                        
                        logger.error("hash is full");
                        
                        throw new RuntimeException("hash is full");
                    }
                    
                }
                
            } 
            catch (FileNotFoundException e) {
                logger.error("file not found in fileHash putHash", e);
            } 
            catch (IOException e) {
                logger.error("io exception in fileHash putHash", e);
            } 
            catch (ResourceException e) {
                logger.error("failed to convert in putHash", e);
            }
        }
        
    }
    
    public FileSegment get(final String key) {
        
        FileSegment returnVal;
        
        if(key.length() > maxKeySize) {
            throw new IllegalArgumentException("key too long");
        }
        
        if(key.equals("")) {
            throw new IllegalArgumentException("key must not be empty");
        }
        
        final int keyHash = key.hashCode();
        
        final int limitedHash = keyHash % hashSize;
        
        logger.debug("limitedHash " + limitedHash);
        
        int hashedIndex = limitedHash * (SLICE_SIZE);
        
        final int start = hashedIndex;
        
        while(true) {
        
            try {
                
                final RandomAccessFile random = new RandomAccessFile(file, "rws");
               
                final byte [] currentKeyIn = new byte[maxKeySize];
                random.seek(hashedIndex);
                
                final int read = random.read(currentKeyIn);
                
                final boolean write;
                
                if(read <= 0) {
                    
                    //nothing at the location, which means the hash doesn't exist.
                    returnVal = null;
                    break;
                    
                }
                else {
                    
                    int term = 0;
                    
                    for(; term < currentKeyIn.length; term++) {
                        
                        if(currentKeyIn[term] == '\0') {
                            break;
                        }
                        
                    }

                    logger.debug("term: " + term);
                    
                    final String currentKey = new String(currentKeyIn, 0, term);    
                    
                    logger.debug("currentKey: '" + currentKey + "'");
                    
                    if(currentKey.equals(key)) {
                        write = true;
                    }
                    else {
                        write = false;
                    }
                    
                }
                
                if(write) {
                
                    final byte[] bytesIndex = new byte[8];
                    final byte[] bytesLength = new byte[8];
                    final byte[] keysBefore = new byte[8];
                    final byte [] keyRead = new byte[this.maxKeySize];
                    
                    int term = 0;
                    
                    for(; term < keyRead.length; term++) {
                        
                        if(keyRead[term] == '\0') {
                            break;
                        }
                        
                    }
                    
                    final String keyReadString = new String(keyRead, 0, term);    
                           
                    random.seek(hashedIndex);
                    
                    random.read(keyRead);
                    random.read(bytesIndex);
                    random.read(bytesLength);
                    random.read(keyRead);
                   
                    random.close();
                    
                    returnVal = new FileSegment(converter.convert(bytesLength), converter.convert(bytesIndex));
                    
                    break;
                    
                }
                else {
                    
                    logger.debug("hash Collision");
                    
                    hashedIndex += SLICE_SIZE;
                    
                    logger.debug("hashIndex " + hashedIndex);
                    
                    if(hashedIndex >= hashSize * SLICE_SIZE) {
                        
                        logger.debug("index: " + hashedIndex + " exceeds size: " + (hashSize * SLICE_SIZE));
                        
                        hashedIndex = 0;
                    }
                    else if(hashedIndex == start) {
                        
                        logger.error("hash is full");
                        
                        throw new RuntimeException("hash is full");
                    }
                    
                }
                
            } 
            catch (FileNotFoundException e) {
                logger.error("file not found in fileHash putHash", e);
            } 
            catch (IOException e) {
                logger.error("io exception in fileHash putHash", e);
            } 
            catch (ResourceException e) {
                logger.error("failed to convert in putHash", e);
            }
        }
        
        return returnVal;        
        
    }
}
