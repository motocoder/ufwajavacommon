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
import llc.ufwa.data.exception.BadDataException;
import llc.ufwa.data.exception.CorruptDataException;
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

/**
 * Hash manager backed by file system.
 * 
 * Format:
 *  [SKDLP{N*[KDLP]}]
 * 
 * Where:
 * S == 4 bytes, segment length, -1 value if it has been discarded due to corruption
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
 * TODO Consolidate reads/writes.
 * 
 * * Some of this class was derived from: https://code.google.com/p/jdbm2/ 
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
 * @param <Key>
 */
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
        this.keySerializer = keySerializer;
        
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
              
                random.seek(blobIndex); //go to index
                
                final byte [] intIn = new byte[4];
                
                //read the segment length
                final int segLength;
                
                {
                  
                    random.read(intIn);
                
                    segLength = converter.convert(intIn);
                  
                }
                
                if(segLength < -1 || segLength == 0 || segLength > random.length()) { 
                    
                    //This segment was not initialized properly, it is bad we need to never attempt to use it, delete
                    throw new BadDataException();
                    
                }
                
                int dataRead = 0;
                
                //while we approach the end
                while(dataRead + 4 < segLength) {

                    final File tempFile = new File(tempFileDirectory, this.idProvider.provide());
                     
                    final int fill;
                    
                    random.read(intIn); //read the key length
                    
                    dataRead += 4;
                    
                    fill = converter.convert(intIn);
                    
                    if(dataRead == 4 && fill < 0) { //return null if there is nothing here, we do this for empty segments
                        return null;
                    }
                    
                    if(dataRead > 4 && fill < 0) { //segment was short recycled one. we are done
                        break;
                        
                    }
                    
                    if(fill > segLength) { //corrupt data
                        throw new CorruptDataException();
                    }

                    //extract a key from the data
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
                                
                                final int read = random.read(buffer, 0, amountToRead);
                                
                                dataRead += read;
                                
                                if(read != amountToRead) {
                                    throw new CorruptDataException();
                                }
                                
                                out.write(buffer, 0, read);
                                out.flush();
                                
                                if(read < buffer.length) {
                                    break;
                                }
                                                            
                            }
                            
                            key = keySerializer.restore(out.toByteArray()); 
                            
                        }
                        finally {
                            out.close();
                        }
                        
                    }
                    
                    //extract the data for this key                    
                    final int dataFill;
                    
                    random.read(intIn);
                    
                    dataRead += 4;
                    
                    if(dataRead > segLength) {
                        throw new CorruptDataException();
                    }
                    
                    dataFill = converter.convert(intIn);
                    
                    if(dataRead + dataFill > segLength) {
                        throw new CorruptDataException();
                    }
                   
                    {
                        final byte [] buffer = new byte[1024];
                        final FileOutputStream out = new FileOutputStream(tempFile); //store the data in a temporary file
                        
                        try {
                        
                            for(int i = 0; i < dataFill; ) {
                                
                                final int amountToRead;
                                
                                if(i + buffer.length > dataFill) {
                                    amountToRead = dataFill - i;    
                                }
                                else {
                                    amountToRead = buffer.length;
                                }
                                
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
                    
                    //add this to prepared values
                    DefaultEntry<Key, File> entry = new DefaultEntry<Key, File>(key, tempFile);
                    
                    tempFiles.add(entry);
                   
                }
                
            } 
            catch (CorruptDataException e) {
                
                eraseSegment(blobIndex);    
                return null;
                
            } 
            catch (BadDataException e) {
                
                deleteSegment(blobIndex);    
                return null;
                
            }
            finally {
                random.close();
            }
            
            
            //return inputstreams to the new data
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
    
    /**
     * This makes the segment invisible.
     * 
     * This should only be used in the event of critical failure.
     * 
     * @param blobIndex
     * @throws HashBlobException
     */
    private void deleteSegment(int blobIndex) throws HashBlobException {
        
        try {
            
            final RandomAccessFile random = new RandomAccessFile(root, "rws");
          
            try {
              
                random.seek(blobIndex); //go to index
                
                //erase the current segment and add it to free segs
                final byte[] fillToWrite = converter.restore(-1);
                random.write(fillToWrite);
                
                //TODO check value just written.
                
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

    /**
     * Clears out the data at this segment freeing it up.
     * 
     * @param blobIndex
     * @throws HashBlobException
     */
    private void eraseSegment(int blobIndex) throws HashBlobException {
        
        try {
                        
            final RandomAccessFile random = new RandomAccessFile(root, "rws");
          
            try {
              
                random.seek(blobIndex); //go to index
                
                final byte [] intIn = new byte[4];
                
                //read the segment length
                final int segLength;
                
                {
                  
                    random.read(intIn);
                
                    segLength = converter.convert(intIn);
                  
                }
                
                
                //erase the current segment and add it to free segs
                final byte[] fillToWrite = converter.restore(-1);
                random.write(fillToWrite);
                //TODO check value just written.
                
                Set<Integer> segs = this.freeSegments.get(segLength);
                
                if(segs == null) {
                    
                    segs = new HashSet<Integer>();
                    this.freeSegments.put(segLength, segs);
                    
                }
                
                segs.add(blobIndex);
                
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
                
                final byte [] keyData = keySerializer.convert(entry.getKey()); 
                
                totalSize += tempFile.length();
                totalSize += keyData.length;
                totalSize += 8; //fill int and key length
                
                keyDatas.put(entry.getKey(), keyData);
                
            }
            catch (IOException e) {
                
                logger.error("Failed to allocate temp file");
                throw new HashBlobException("Failed to allocate temp file");
                
                
            } catch (ResourceException e) {
                
                logger.error("Failed to serialize when allocating temp file");
                throw new HashBlobException("Failed to serialize when allocating temp file");
                
            }
            
        }
        
        final int writtingIndex;
        
        //if the data was at an existing index we need to check if the segment is big enough for all the new data.
        if(blobIndex >= 0) {
            
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
                  
                    //read in this segments length
                    {
                      
                        random.read(currentKeyIn); 
                    
                        segLength = converter.convert(currentKeyIn);
                      
                    }
                  
                    //if the segment is too small for the new data
                    if(segLength < totalSize) {
                        
                        //erase the current segment and add it to free segs
                        final byte[] fill = converter.restore(-1);
                        random.write(fill);
                        
                        //TODO check value just written.
                        
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
        
        //write this shit in
        try {
            
            final RandomAccessFile random = new RandomAccessFile(root, "rws");
          
            try {
              
                final long length = random.length();
              
                if((writtingIndex + 4) >= length) {
                    throw new HashBlobException("invalid index");
                }
              
                random.seek(writtingIndex + 4 + 4); //skip the length
                
                int totalWritten = 0;
                
                byte [] firstKeyData = null;
                
                //write out the keys and their data
                for(final Entry<Key, File> tempFile : tempFiles) {
                    
                    final byte[] keyData = keyDatas.get(tempFile.getKey());
                    
                    if(firstKeyData == null) {
                        firstKeyData = keyData;
                    }
                    else {
                        
                        final byte[] keyLengthBytes = converter.restore(keyData.length);
                        
                        random.write(keyLengthBytes);
                        //TODO check value just written.
                        
                    }
                    
                    totalWritten += keyData.length + 8;
                            
                    
                    random.write(keyData);
                    
                    final int fileLength = (int)tempFile.getValue().length();
                    
                    final byte[] sizeBytes = converter.restore(fileLength);
                    
                    random.write(sizeBytes);
                    
                    //TODO check value just written.
                    
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
                
                //if our segment is bigger than our data by 4 or more bytes, write a terminating -1                
                if(totalSize >= totalWritten + 4) {
                    
                    final byte[] terminatorBytes = converter.restore(-1);
                    random.write(terminatorBytes);
                    
                    //TODO check value just written.
                    
                }
                                
                //We are setting the first keylength last because that is what is used to determine if this segment is used. 
                //This will hopefully make it as crash proof as possible.
                if(firstKeyData != null) {

                    random.seek(writtingIndex + 4); //skip the length
                    
                    final byte[] keyLengthBytes = converter.restore(firstKeyData.length);
                    
                    random.write(keyLengthBytes);
                    
                    //TODO check value just written.
                    
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
        
        final NavigableMap<Integer, Set<Integer>> tail = this.freeSegments.tailMap(totalSize, true);
        
        int returnVal = -1;
        
        //first look into the existing free segments
        if(tail.size() == 0) {
            
            int blobIndex = 0;
            
            while(true) {
                
                try {
                    
                    final RandomAccessFile random = new RandomAccessFile(root, "rws");
                  
                    try {
                        
                        final long length = random.length();
                      
                        System.out.println((blobIndex + 4) + "    " + length);
                        
                        if((blobIndex + 4) >= length) {
                            
                            //end of file reached found nothing;
                            blobIndex = -1;
                            break;
                            
                        }
                        
                        final byte [] currentKeyIn = new byte[4];
                        
                        System.out.println(blobIndex);
                        
                    	random.seek(blobIndex);
                        
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
                        
                        if(fill == -1) { //encountered free segment, add it to known free.
                            
                        	if(segLength >= totalSize) {
                                
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
                        
                        blobIndex += 8 + Math.abs(segLength); //skip to next segment
                        
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
                        
                        random.seek(length);
                        
                        random.write(lengthData);
                        
                        //TODO check value just written.
                        
                        final byte [] fillData = converter.restore(-1);
                    
                        random.write(fillData);
                        //TODO check value just written.
                        
                        random.seek(totalSize + length + 4); //seek to last 4 bytes of seg.
                        random.write(fillData);
                        
                        //TODO check value just written.
                        
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
            
        }
        else {
            
            //use existing free segment that we already know about.
            
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
