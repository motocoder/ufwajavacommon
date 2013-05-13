package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import llc.ufwa.data.exception.HashBlobException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.ByteArrayIntegerConverter;
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

    private final static int BUCKET_SIZE = 4;
    
    private final int hashSize;
    private final File file;
    private final Converter<byte [], Integer> converter = new ByteArrayIntegerConverter();
    private final HashDataBlobManager blobManager;
    
    
    public FileHash(
        final File file,
        final HashDataBlobManager blobManager,
        final int hashSize
    ) {
        
        this.hashSize = hashSize;
        
        if(file.isDirectory()) {
            throw new RuntimeException("hash file location must not be a directory");
        }
        
        this.blobManager = blobManager;        
        this.file = file;
        
        if(!file.exists()) {
            
            try {
                
                final OutputStream output = new FileOutputStream(file);
                
                try {
                    
                    final byte [] bytes = new byte [1024];
                    
                    Arrays.fill(bytes, (byte)-1);
                    
                    final int max = hashSize * BUCKET_SIZE;
                    
                    for(int i = 0; i < max; i += 1024) {
                        output.write(bytes);
                    }
                    
                    output.flush();
                    
                }
                finally {
                    output.close();
                }
                
            } 
            catch (FileNotFoundException e) {
                
                logger.error("Failed to establish file hash", e);
                throw new RuntimeException("failed to establish file hash", e);
                
            }
            catch (IOException e) {
                
                logger.error("Failed to establish file hash 2", e);
                throw new RuntimeException("failed to establish file hash 2", e);
                
            }
            
        }
    }
    
    public void put(
      final String key,
      final HashDataBlob blob
    ) {
        
        final int limitedHash = key.hashCode() % hashSize;
      
        int hashedIndex = limitedHash * (BUCKET_SIZE); 
     
        try {
          
            final RandomAccessFile random = new RandomAccessFile(file, "rws");
            
            try {
              
               final byte [] currentKeyIn = new byte[4];
               random.seek(hashedIndex);
               
               //read in key at this hash location.
               final int read = random.read(currentKeyIn);
               
               if(read <= 0) {
                   throw new RuntimeException("hash was not initialized properly");
               }
               
               int blobIndex = converter.convert(currentKeyIn);
             
               final Set<HashDataBlob> toWrite = new HashSet<HashDataBlob>();
               
               if(blobIndex >= 0) {
                   
                   final Set<HashDataBlob> blobs = blobManager.getBlobsAt(blobIndex);
                   
                   toWrite.addAll(blobs);
                   
               }
               else {
                   
                   blobIndex = blobManager.newBucket();
                   
                   final byte[] bytesIndex = converter.restore(blobIndex);
                 
                   random.seek(hashedIndex);
                   random.write(bytesIndex); 
                   
               }
               
               toWrite.remove(blob);               
               toWrite.add(blob);
               
               blobManager.setBlobs(blobIndex, toWrite);
               
            }
            finally {
                random.close();
            }
            
        }
        catch (FileNotFoundException e) {
          
            logger.error("file not found in fileHash putHash", e);
            throw new RuntimeException("failed hash blob", e);
        } 
        catch (IOException e) {
          
            logger.error("io exception in fileHash putHash", e);
            throw new RuntimeException("failed hash blob", e);
          
        } 
        catch (ResourceException e) {
           
            logger.error("failed to convert in putHash", e);
            throw new RuntimeException("failed hash blob", e);
          
        } 
        catch (HashBlobException e) {
          
            logger.error("failed hash blob", e);
            throw new RuntimeException("failed hash blob", e);
          
        }
            
    }
    
    public HashDataBlob get(
      final String key
    ) {
      
        final int limitedHash = key.hashCode() % hashSize;
    
        int hashedIndex = limitedHash * (BUCKET_SIZE); 
        
        try {
        
            final RandomAccessFile random = new RandomAccessFile(file, "rws");
          
            try {
            
               final byte [] currentKeyIn = new byte[4];
               random.seek(hashedIndex);
             
               //read in key at this hash location.
               final int read = random.read(currentKeyIn);
             
               if(read <= 0) {
                   throw new RuntimeException("hash was not initialized properly");
               }
             
               int blobIndex = converter.convert(currentKeyIn);
             
               HashDataBlob returnVal = null;
               
               if(blobIndex >= 0) {
                 
                   final Set<HashDataBlob> blobs = blobManager.getBlobsAt(blobIndex);
                 
                   for(HashDataBlob blob : blobs) {
                       
                       if(blob.getKey().equals(key)) {
                           
                           returnVal = blob;
                           break;
                           
                       }
                       
                   }
                 
               }
             
               return returnVal;
             
            }
            finally {
                random.close();
            }
          
        }
        catch (FileNotFoundException e) {
        
            logger.error("file not found in fileHash putHash", e);
            throw new RuntimeException("failed hash blob", e);
        } 
        catch (IOException e) {
        
            logger.error("io exception in fileHash putHash", e);
            throw new RuntimeException("failed hash blob", e);
        
        } 
        catch (ResourceException e) {
         
            logger.error("failed to convert in putHash", e);
            throw new RuntimeException("failed hash blob", e);
        
        } 
        catch (HashBlobException e) {
        
            logger.error("failed hash blob", e);
            throw new RuntimeException("failed hash blob", e);
        
        }
          
    }

    public void remove(String key) {
        
        final int limitedHash = key.hashCode() % hashSize;
        
        int hashedIndex = limitedHash * (BUCKET_SIZE); 
        
        try {
        
            final RandomAccessFile random = new RandomAccessFile(file, "rws");
          
            try {
            
               final byte [] currentKeyIn = new byte[4];
               random.seek(hashedIndex);
             
               //read in key at this hash location.
               final int read = random.read(currentKeyIn);
             
               if(read <= 0) {
                   throw new RuntimeException("hash was not initialized properly");
               }
             
               int blobIndex = converter.convert(currentKeyIn);
             
               
               
               if(blobIndex >= 0) {
                 
                   HashDataBlob removing = null;
                   
                   final Set<HashDataBlob> blobs = blobManager.getBlobsAt(blobIndex);
                 
                   for(HashDataBlob blob : blobs) {
                       
                       if(blob.getKey().equals(key)) {
                           
                           removing = blob;
                           break;
                           
                       }
                       
                   }
                   
                   if(removing != null) {
                       
                       blobs.remove(removing);
                       blobManager.setBlobs(blobIndex, blobs);
                       
                       if(blobs.size() == 0) {
                           
                           final byte[] bytesIndex = converter.restore(-1);
                         
                           random.seek(hashedIndex);
                           random.write(bytesIndex); 
                           
                       }
                       
                   }
                 
               }
             
            }
            finally {
                random.close();
            }
          
        }
        catch (FileNotFoundException e) {
        
            logger.error("file not found in fileHash putHash", e);
            throw new RuntimeException("failed hash blob", e);
        } 
        catch (IOException e) {
        
            logger.error("io exception in fileHash putHash", e);
            throw new RuntimeException("failed hash blob", e);
        
        } 
        catch (ResourceException e) {
         
            logger.error("failed to convert in putHash", e);
            throw new RuntimeException("failed hash blob", e);
        
        } 
        catch (HashBlobException e) {
        
            logger.error("failed hash blob", e);
            throw new RuntimeException("failed hash blob", e);
        
        }
        
    }
            
//    
//    public void put(
//        final String key,
//        final int length,
//        final InputStream data
//    ) {
//        
//        final int limitedHash = key.hashCode() % hashSize;
//        
//        int hashedIndex = limitedHash * (BUCKET_SIZE);
//        
//        final int start = hashedIndex;
//        
//        while(true) {
//        
//            try {
//                
//                final RandomAccessFile random = new RandomAccessFile(file, "rws");
//                
//                try {
//                   
//                    final byte [] currentKeyIn = new byte[4];
//                    random.seek(hashedIndex);
//                    
//                    //read in key at this hash location.
//                    final int read = random.read(currentKeyIn);
//                    
//                    final boolean write;
//                    
//                    if(read <= 0) { //nothing there, write
//                    
//                        write = true;    
//                        
//                    }
//                    else {
//                        
//                        final int blobIndex = converter.convert(currentKeyIn);
//                        
//                        if(blobIndex < 0) {
//                            write = true;
//                        }
//                        else {
//                            
//                            final HashDataBlob blobAtCurrent = blobManager.getBlobAt(blobIndex);
//                            
//                            // if same key write
//                            if(blobAtCurrent.getKey().equals(key)) {
//                                write = true;
//                            }
//                            else if(blobAtCurrent.getKey().hashCode() == key.hashCode()) {
//                                
//                                //skip to next hash location
//                                
//                                blobManager.update(
//                                    new HashDataBlob(
//                                        blobAtCurrent.getIndex(),
//                                        blobAtCurrent.getKey(), 
//                                        blobAtCurrent.getHashesAfter() + 1,
//                                        blobAtCurrent.getDataLength()
//                                    )
//                                );
//                                
//                                write = false;
                                
//                            }
//                            else {
//                                write = false;
//                            }
//                            
//                        }
//                        
//                    }
//                    
//                    if(write) {
//                        
//                        final int index = blobManager.allocate(length);
//                        
//                        final HashDataBlob blob = new HashDataBlob(index, key, 0, length);
//                        
//                        blobManager.write(blob, data);
//                    
//                        final byte[] bytesIndex = converter.restore(index);
//                        
//                        if(hashedIndex == 308) {
//                            logger.debug("put at 308 " + index);
//                        }
//                        
//                        random.seek(hashedIndex);
//                        random.write(bytesIndex);        
//                        break;
//                        
//                    }
//                    else {
//                        
//                        hashedIndex += BUCKET_SIZE;
//                        
//                        if(hashedIndex >= hashSize * (BUCKET_SIZE)) {
//                            hashedIndex = 0;
//                        }
//                        else if(hashedIndex == start) {
//                            throw new RuntimeException("hash is full");
//                        }
//                        
//                    }
//                    
//                }
//                finally {
//                    random.close();
//                }
//                
//            } 
//            catch (FileNotFoundException e) {
//                logger.error("file not found in fileHash putHash", e);
//            } 
//            catch (IOException e) {
//                logger.error("io exception in fileHash putHash", e);
//            } 
//            catch (ResourceException e) {
//                logger.error("failed to convert in putHash", e);
//            } 
//            catch (HashBlobException e) {
//                logger.error("failed hash blob", e);
//            }
//        }
//        
//    }
//    
//    public HashDataBlob get(final String key) {
//        
//        final int limitedHash = key.hashCode() % hashSize;
//        
//        int hashedIndex = limitedHash * (BUCKET_SIZE);
//        
//        final int start = hashedIndex;
//        HashDataBlob returnVal = null;
//        
//        boolean first = true;
//        boolean firstDifferent 
//        
//        
//        while(true) {
//            
//            try {
//                
//                final RandomAccessFile random = new RandomAccessFile(file, "rws");
//                
//                try {
//                    
//                    final byte [] currentKeyIn = new byte[4];
//                    random.seek(hashedIndex);
//                    
//                    //read in key at this hash location.
//                    final int read = random.read(currentKeyIn);
//                    
//                    final boolean write;
//                    
//                    if(read <= 0) { //nothing there, write
//                    
//                        write = true;    
//                        
//                    }
//                    else {
//                        
//                        final int blobIndex = converter.convert(currentKeyIn);
//                        
//                        if(blobIndex < 0) {
//                            break;                            
//                        }
//                        else {
//                            
//                            final HashDataBlob blobAtCurrent = blobManager.getBlobAt(blobIndex);
//                            
//                            // if same key write
//                            if(blobAtCurrent.getKey().equals(key)) {
//                                write = true;
//                            }
//                            else if(blobAtCurrent.getKey().hashCode() == key.hashCode()) {
//                                
//                                if(blobAtCurrent.getHashesAfter() == 0) {
//                                    break;
//                                    
//                                }
//                                else {//skip to next hash location
//                                    
//                                    logger.debug("after " + blobAtCurrent.getHashesAfter());
//                                    write = false;
//                                }
//                            }
//                            else {
//                                write = false;
//                            }
//                                
//                        }
//                        
//                    }
//                    
//                    if(write) {
//                    
//                        final byte[] bytesIndex = new byte [4];
//                        
//                        random.seek(hashedIndex);
//                        random.read(bytesIndex);
//                        
//                        final int index = converter.convert(bytesIndex);
//                        
//                        returnVal = blobManager.getBlobAt(index);
//                        break;
//                        
//                    }
//                    else {
//                        hashedIndex += BUCKET_SIZE;
//                        
//                        if(hashedIndex >= hashSize * (BUCKET_SIZE)) {
//                            hashedIndex = 0;
//                        }
//                        else if(hashedIndex == start) {
//                            
//                            throw new RuntimeException("hash is full");
//                        }
//                        
//                    }
//                }
//                finally {
//                    random.close();
//                }
//                
//            } 
//            catch (FileNotFoundException e) {
//                logger.error("file not found in fileHash putHash", e);
//            } 
//            catch (IOException e) {
//                logger.error("io exception in fileHash putHash", e);
//            } 
//            catch (ResourceException e) {
//                logger.error("failed to convert in putHash", e);
//            } 
//            catch (HashBlobException e) {
//                logger.error("failed hash blob", e);
//            }
//        }   
//        
//        return returnVal;
//        
//    }
//    
//    public void remove(final String key) {
//        
//        final int limitedHash = key.hashCode() % hashSize;
//        
//        int hashedIndex = limitedHash * (BUCKET_SIZE);
//        
//        final int start = hashedIndex;
//        
//        while(true) {
//            
//            try {
//                
//                final RandomAccessFile random = new RandomAccessFile(file, "rws");
//                
//                try {
//                    
//                    final byte [] currentKeyIn = new byte[4];
//                    random.seek(hashedIndex);
//                    
//                    //read in key at this hash location.
//                    final int read = random.read(currentKeyIn);
//                    
//                    final boolean write;
//                    
//                    if(read <= 0) { //nothing there, write
//                        write = true;                          
//                    }
//                    else {
//                        
//                        final int blobIndex = converter.convert(currentKeyIn);
//                        
//                        if(blobIndex < 0) {
//                            write = false;                       
//                        }
//                        else {
//                            
//                            final HashDataBlob blobAtCurrent = blobManager.getBlobAt(blobIndex);
//                            
//                            // if same key write
//                            if(blobAtCurrent.getKey().equals(key)) {
//                                write = true;
//                            } 
//                            else if(blobAtCurrent.getKey().hashCode() == key.hashCode()) {
//                                
//                                if(blobAtCurrent.getHashesAfter() == 0) {
//                                    logger.debug("couldnt find " + key);
//                                    break;                                
//                                }
//                                else {//skip to next hash location
//                                    
//                                    blobManager.update(
//                                        new HashDataBlob(
//                                            blobAtCurrent.getIndex(),
//                                            blobAtCurrent.getKey(),
//                                            blobAtCurrent.getHashesAfter() - 1,
//                                            blobAtCurrent.getDataLength()
//                                        )
//                                    );
//                                    
//                                    write = false;
//                                    
//                                }
//                                
//                            }
//                            else {
//                                write = false;
//                            }
//                            
//                        }
//                        
//                    }
//                    
//                    if(write) {
//                    
//                        final int index = -1;
//                        
//                        final byte[] bytesIndex = converter.restore(index);
//                        
////                        if(hashedIndex == 308) {
//                            logger.debug("remove at " + hashedIndex + " " + index);
////                        }
//                        
//                        random.seek(hashedIndex);
//                        random.write(bytesIndex);   
//                        break;
//                        
//                    }
//                    else {
//                        
//                        hashedIndex += BUCKET_SIZE;
//                        
//                        if(hashedIndex >= hashSize * (BUCKET_SIZE)) {
//                            hashedIndex = 0;
//                        }
//                        else if(hashedIndex == start) {
//                            throw new RuntimeException("hash is full");
//                        }
//                        
//                    }
//                }
//                finally {
//                    random.close();
//                }
//                
//            } 
//            catch (FileNotFoundException e) {
//                logger.error("file not found in fileHash putHash", e);
//            } 
//            catch (IOException e) {
//                logger.error("io exception in fileHash putHash", e);
//            } 
//            catch (ResourceException e) {
//                logger.error("failed to convert in putHash", e);
//            } 
//            catch (HashBlobException e) {
//                logger.error("failed hash blob", e);
//            }
//        }   
//        
//    }
}
