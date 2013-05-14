package llc.ufwa.data.resource.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import llc.ufwa.data.DefaultEntry;
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
public class FileHash<Key, Value> {
    
    private static final Logger logger = Logger.getLogger(FileHash.class);

    private final static int BUCKET_SIZE = 4;
    
    private final int hashSize;
    private final File file;
    private final Converter<byte [], Integer> converter = new ByteArrayIntegerConverter();
    private final HashDataManager<Key, Value> blobManager;
    
    
    public FileHash(
        final File file,
        final HashDataManager<Key, Value> blobManager,
        final int hashSize
    ) {
        
        this.hashSize = hashSize;
        
        if(file.isDirectory()) {
            throw new RuntimeException("hash file location must not be a directory");
        }
        
        this.blobManager = blobManager;        
        this.file = file;
        
        //if the file doesn't exist, initialize an empty hash of the desired size
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
    
    /**
     * 
     * @param key
     * @param blob
     */
    public void put(
      final Key key,
      final Value blob
    ) {
        
        final int limitedHash = key.hashCode() % hashSize; //limit the hash size to our hash
      
        int hashedIndex = limitedHash * (BUCKET_SIZE); //multiply by bucket size so we know index.
     
        try {
          
            final RandomAccessFile random = new RandomAccessFile(file, "rws");
            
            try {
              
               final byte [] currentKeyIn = new byte[4];
               random.seek(hashedIndex);
               
               //read in key at this hash location.
               final int read = random.read(currentKeyIn); //read in the current value
               
               if(read <= 0) { //file was too short, empty values weren't filled in
                   throw new RuntimeException("hash was not initialized properly");
               }
               
               final int blobIndex = converter.convert(currentKeyIn);
             
               final Set<Entry<Key, Value>> toWrite = new HashSet<Entry<Key, Value>>();
               
               if(blobIndex >= 0) {
                   
                   //if there is already something hashed here, retrieve the hash bucket and add to it
                   final Set<Entry<Key, Value>> blobs = blobManager.getBlobsAt(blobIndex);
                   
                   toWrite.addAll(blobs); 
                   
               }
               
               //if this key was already in the bucket remove it.
               Entry<Key, Value> remove = null;
               
               for(final Map.Entry<Key, Value> entry : toWrite) {
                   
                   if(entry.getKey().equals(key)) {
                       remove = entry;
                   }
                   
               }
               
               toWrite.remove(remove);               
                              
               //then add it to the bucket again
               toWrite.add(
                   new DefaultEntry<Key, Value>(key, blob)
               );
               
               //save the new values, if a new index is allocated, store it in the hash
               final int blobIndexAfterSet = blobManager.setBlobs(blobIndex, toWrite);
               
               if(blobIndexAfterSet != blobIndex) {

                   final byte[] bytesIndex = converter.restore(blobIndexAfterSet);
                   
                   random.seek(hashedIndex);
                   random.write(bytesIndex);
                   
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
    
    public Value get(
      final Key key
    ) {
      
        final int limitedHash = key.hashCode() % hashSize; //limit the hash to our hash size
    
        int hashedIndex = limitedHash * (BUCKET_SIZE); //determine byte index
        
        try {
        
            final RandomAccessFile random = new RandomAccessFile(file, "rws");
          
            try {
                
               
                final byte [] currentKeyIn = new byte[4];
                random.seek(hashedIndex);
             
                //read in key at this hash location.
                final int read = random.read(currentKeyIn);
             
                if(read <= 0) { //file should have been initialized to hash size
                    throw new RuntimeException("hash was not initialized properly");
                }
             
                //convert the values read into an index
                int blobIndex = converter.convert(currentKeyIn);
             
                Value returnVal = null;
               
                if(blobIndex >= 0) {
                 
                    //if there is values on this hash
                    final Set<Entry<Key, Value>> blobs = blobManager.getBlobsAt(blobIndex);
                 
                    for(Entry<Key, Value> blob : blobs) {
                       
                        if(blob.getKey().equals(key)) {
                           
                            //return the value mapped to this key
                            returnVal = blob.getValue();
                            break;
                           
                        }
                       
                    }
                 
                } 
                //else the returnval will be null
             
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

    public void remove(Key key) {
        
        logger.debug("removing key " + key);
        
        final int limitedHash = key.hashCode() % hashSize; //limit the hash size
        
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
             
               //convert to an index
               int blobIndex = converter.convert(currentKeyIn);
               
               //if there is a value on this hash, retrieve its value
               if(blobIndex >= 0) {
                 
                   Entry<Key, Value> removing = null;
                   
                   logger.debug("blobIndex2 " + blobIndex);
                   
                   final Set<Entry<Key, Value>> blobs = blobManager.getBlobsAt(blobIndex);
                 
                   for(Entry<Key, Value> blob : blobs) {
                       
                       logger.debug("blob at index " + blob.getKey());
                       
                       if(blob.getKey().equals(key)) {
                           
                           //once we find a key that matches removed the value
                           removing = blob;
                           break;
                           
                       }
                       
                   }
                   
                   if(removing != null) {
                       
                       //save the blobs after removing the value mapped to our key
                       logger.debug("removing not null");
                       
                       blobs.remove(removing);
                       blobManager.setBlobs(blobIndex, blobs);
                       
                       if(blobs.size() == 0) {
                           
                           //if blobs is empty, remove the hash as well.
                           
                           logger.debug("blobs size 0");
                           
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
    
}
