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
 * 
 * 
 * this is a file backed hashing mechanism. 
 * 
 * Some of this class was derived from: https://code.google.com/p/jdbm2/ 
 * 
 * This is the license:
 * 
 * <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html><head>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
  <link rel="stylesheet" href="LICENSE-2.0_fichiers/style.css" type="text/css">
  <meta name="author" content="The Apache Software Foundation">
  <meta name="email" content="apache.AT.apache.DOT.org">
  <title>Apache License, Version 2.0 - The Apache Software Foundation</title>
</head>
<body>        
<p align="center">
Apache License<br>
Version 2.0, January 2004<br>
<a href="http://www.apache.org/licenses/">http://www.apache.org/licenses/</a>
</p>
<p>
TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION
</p>
<p><b><a name="definitions">1. Definitions</a></b>.</p>
<p>
      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.
</p>
<p>
      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.
</p>
<p>
      "Legal Entity" shall mean the union of the acting entity and all
      other entities that control, are controlled by, or are under common
      control with that entity. For the purposes of this definition,
      "control" means (i) the power, direct or indirect, to cause the
      direction or management of such entity, whether by contract or
      otherwise, or (ii) ownership of fifty percent (50%) or more of the
      outstanding shares, or (iii) beneficial ownership of such entity.
</p>
<p>
      "You" (or "Your") shall mean an individual or Legal Entity
      exercising permissions granted by this License.
</p>
<p>
      "Source" form shall mean the preferred form for making modifications,
      including but not limited to software source code, documentation
      source, and configuration files.
</p>
<p>
      "Object" form shall mean any form resulting from mechanical
      transformation or translation of a Source form, including but
      not limited to compiled object code, generated documentation,
      and conversions to other media types.
</p>
<p>
      "Work" shall mean the work of authorship, whether in Source or
      Object form, made available under the License, as indicated by a
      copyright notice that is included in or attached to the work
      (an example is provided in the Appendix below).
</p>
<p>
      "Derivative Works" shall mean any work, whether in Source or Object
      form, that is based on (or derived from) the Work and for which the
      editorial revisions, annotations, elaborations, or other modifications
      represent, as a whole, an original work of authorship. For the purposes
      of this License, Derivative Works shall not include works that remain
      separable from, or merely link (or bind by name) to the interfaces of,
      the Work and Derivative Works thereof.
</p>
<p>
      "Contribution" shall mean any work of authorship, including
      the original version of the Work and any modifications or additions
      to that Work or Derivative Works thereof, that is intentionally
      submitted to Licensor for inclusion in the Work by the copyright owner
      or by an individual or Legal Entity authorized to submit on behalf of
      the copyright owner. For the purposes of this definition, "submitted"
      means any form of electronic, verbal, or written communication sent
      to the Licensor or its representatives, including but not limited to
      communication on electronic mailing lists, source code control systems,
      and issue tracking systems that are managed by, or on behalf of, the
      Licensor for the purpose of discussing and improving the Work, but
      excluding communication that is conspicuously marked or otherwise
      designated in writing by the copyright owner as "Not a Contribution."
</p>
<p>
      "Contributor" shall mean Licensor and any individual or Legal Entity
      on behalf of whom a Contribution has been received by Licensor and
      subsequently incorporated within the Work.
</p>
<p><b><a name="copyright">2. Grant of Copyright License</a></b>.
Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.
</p>
<p><b><a name="patent">3. Grant of Patent License</a></b>.
Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      (except as stated in this section) patent license to make, have made,
      use, offer to sell, sell, import, and otherwise transfer the Work,
      where such license applies only to those patent claims licensable
      by such Contributor that are necessarily infringed by their
      Contribution(s) alone or by combination of their Contribution(s)
      with the Work to which such Contribution(s) was submitted. If You
      institute patent litigation against any entity (including a
      cross-claim or counterclaim in a lawsuit) alleging that the Work
      or a Contribution incorporated within the Work constitutes direct
      or contributory patent infringement, then any patent licenses
      granted to You under this License for that Work shall terminate
      as of the date such litigation is filed.
</p>
<p><b><a name="redistribution">4. Redistribution</a></b>.
You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the following conditions:
</p><ol type="a">
<li>You must give any other recipients of the Work or
          Derivative Works a copy of this License; and
<br> <br></li>

<li>You must cause any modified files to carry prominent notices
          stating that You changed the files; and
<br> <br></li>

<li>You must retain, in the Source form of any Derivative Works
          that You distribute, all copyright, patent, trademark, and
          attribution notices from the Source form of the Work,
          excluding those notices that do not pertain to any part of
          the Derivative Works; and
<br> <br></li>

<li>If the Work includes a "NOTICE" text file as part of its
          distribution, then any Derivative Works that You distribute must
          include a readable copy of the attribution notices contained
          within such NOTICE file, excluding those notices that do not
          pertain to any part of the Derivative Works, in at least one
          of the following places: within a NOTICE text file distributed
          as part of the Derivative Works; within the Source form or
          documentation, if provided along with the Derivative Works; or,
          within a display generated by the Derivative Works, if and
          wherever such third-party notices normally appear. The contents
          of the NOTICE file are for informational purposes only and
          do not modify the License. You may add Your own attribution
          notices within Derivative Works that You distribute, alongside
          or as an addendum to the NOTICE text from the Work, provided
          that such additional attribution notices cannot be construed
          as modifying the License.</li>
</ol>
      You may add Your own copyright statement to Your modifications and
      may provide additional or different license terms and conditions
      for use, reproduction, or distribution of Your modifications, or
      for any such Derivative Works as a whole, provided Your use,
      reproduction, and distribution of the Work otherwise complies with
      the conditions stated in this License.

<p><b><a name="contributions">5. Submission of Contributions</a></b>.
Unless You explicitly state otherwise,
      any Contribution intentionally submitted for inclusion in the Work
      by You to the Licensor shall be under the terms and conditions of
      this License, without any additional terms or conditions.
      Notwithstanding the above, nothing herein shall supersede or modify
      the terms of any separate license agreement you may have executed
      with Licensor regarding such Contributions.
</p>
<p><b><a name="trademarks">6. Trademarks</a></b>.
This License does not grant permission to use the trade
      names, trademarks, service marks, or product names of the Licensor,
      except as required for reasonable and customary use in describing the
      origin of the Work and reproducing the content of the NOTICE file.
</p>
<p><b><a name="no-warranty">7. Disclaimer of Warranty</a></b>.
Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE. You are solely responsible for determining the
      appropriateness of using or redistributing the Work and assume any
      risks associated with Your exercise of permissions under this License.
</p>
<p><b><a name="no-liability">8. Limitation of Liability</a></b>.
In no event and under no legal theory,
      whether in tort (including negligence), contract, or otherwise,
      unless required by applicable law (such as deliberate and grossly
      negligent acts) or agreed to in writing, shall any Contributor be
      liable to You for damages, including any direct, indirect, special,
      incidental, or consequential damages of any character arising as a
      result of this License or out of the use or inability to use the
      Work (including but not limited to damages for loss of goodwill,
      work stoppage, computer failure or malfunction, or any and all
      other commercial damages or losses), even if such Contributor
      has been advised of the possibility of such damages.
</p>
<p><b><a name="additional">9. Accepting Warranty or Additional Liability</a></b>.
While redistributing
      the Work or Derivative Works thereof, You may choose to offer,
      and charge a fee for, acceptance of support, warranty, indemnity,
      or other liability obligations and/or rights consistent with this
      License. However, in accepting such obligations, You may act only
      on Your own behalf and on Your sole responsibility, not on behalf
      of any other Contributor, and only if You agree to indemnify,
      defend, and hold each Contributor harmless for any liability
      incurred by, or claims asserted against, such Contributor by reason
      of your accepting any such warranty or additional liability.
</p>
<p>
END OF TERMS AND CONDITIONS
</p>
</body></html>
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
                   
                   if(blobs != null) {
                       toWrite.addAll(blobs);
                   }
                   
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
                    
                    if(blobs == null) { //data corrupt lets remove our reference.
                        
                        final byte[] bytesIndex = converter.restore(-1);
                        
                        random.seek(hashedIndex);
                        random.write(bytesIndex); 
                        
                    }
                    else {
                 
                        for(Entry<Key, Value> blob : blobs) {
                           
                            if(blob.getKey().equals(key)) {
                               
                                //return the value mapped to this key
                                returnVal = blob.getValue();
                                break;
                               
                            }
                           
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
                   
                   if(blobs == null) { //data corrupt lets remove our reference.
                       
                       final byte[] bytesIndex = converter.restore(-1);
                       
                       random.seek(hashedIndex);
                       random.write(bytesIndex); 
                       
                   }
                   else {
                 
                       for(Entry<Key, Value> blob : blobs) {
                           
                           logger.debug("blob at index " + blob.getKey());
                           
                           if(blob.getKey().equals(key)) {
                               
                               //once we find a key that matches removed the value
                               removing = blob;
                               break;
                               
                           }
                           
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
