package llc.ufwa.gen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import llc.ufwa.util.StringUtilities;

import org.apache.log4j.lf5.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassDataGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(ClassDataGenerator.class);
    
    public static void generateClassFilesFor(
        final File dataIn,
        final File outputFolder,
        final String name,
        final String packageName
    ) throws IOException {
        
        //We could do this all in one stream but it would be harder to read so I am doing it in logical chunks.
        //and performance isn't that noticeable for this.
        //create the tempFile to store the compressed data
        final File tempFile = new File(outputFolder, name + ".tmp");
        
        if(!outputFolder.exists()) {
            outputFolder.mkdirs();
        }
        
        if(!outputFolder.isDirectory()) {
            throw new IllegalArgumentException("output folder must be a directory");    
        }
        
        if(tempFile.exists()) {
            tempFile.delete();
        }
        
        tempFile.createNewFile();
        
        tempFile.deleteOnExit();
        
        if(tempFile.length() != 0) {
            throw new RuntimeException("couldn't delete old temp");
        }
        
        {
        
            final InputStream in = new FileInputStream(dataIn);
            
            try {
                
                final OutputStream out = new GZIPOutputStream(new FileOutputStream(tempFile));
                
                try {
                    
                    StreamUtils.copy(in, out);
                    
                    out.flush();
                    
                }
                finally {
                    out.close();
                }
                
            }
            finally {
                in.close();
            }
            
        }
        
        int chunk = 0;
        
        {
         
            final FileInputStream in = new FileInputStream(tempFile);
            
            try {
        
                final byte [] buffer = new byte[2048];
                
                final Set<OutputStream> chunkJavaFiles = new HashSet<OutputStream>();
                OutputStream chunkOut = null;
                
                try {
                
                    int currentChunkSize = 0;
                    
                    for(int i = 0; true;) {
                        
                        if(chunkOut != null && i < chunk * 63000) {
                            
                            final int read = in.read(buffer);
                            
                            if(read > 0) {
                                
                                logger.debug("first byte " + (buffer[0] + 128));
                                logger.debug("second byte " + (buffer[1] + 128));
                                
                                currentChunkSize += read;
             
                                final byte [] toWrite = Arrays.copyOf(buffer, read);
                                
                                chunkOut.write(StringUtilities.toHex(toWrite).getBytes(Charset.forName("UTF-8")));
                                chunkOut.flush();
                                
                                i += read;
                                
                            }
                            else {
                                
                                chunkOut.write("\";}".getBytes(Charset.forName("UTF-8")));
                                chunkOut.write(("public int length(){return " + currentChunkSize + ";}}").getBytes(Charset.forName("UTF-8")));
                                chunkOut.flush();
                                break;
                                
                            }
             
                            
                        }
                        else {
                            
                            if(chunkOut != null) {
                            
                                chunkOut.write("\";}".getBytes(Charset.forName("UTF-8")));
                                chunkOut.write(("public int length(){return " + currentChunkSize + ";}}").getBytes(Charset.forName("UTF-8")));
                                chunkOut.flush();
                                
                            }
                            
                            currentChunkSize = 0;
                            chunk++;
                            
                            chunkOut = new FileOutputStream(new File(outputFolder, name + "Chunk" + chunk));
                            chunkJavaFiles.add(chunkOut);
                            
                            chunkOut.write(("package " + packageName +";import llc.ufwa.gen.DataClass;").getBytes(Charset.forName("UTF-8")));
                            chunkOut.write(("public class " + name + "Chunk" + chunk + " implements DataClass{").getBytes(Charset.forName("UTF-8")));
                            chunkOut.write(("public String getData(){return \"").getBytes(Charset.forName("UTF-8")));
                            chunkOut.flush();
                            
                        }
                        
                    }
                    
                }
                finally {
                    
                    boolean throwAgain = false;
                    
                    for(final OutputStream out : chunkJavaFiles) {
                        
                        try {
                            out.close();
                        }
                        catch(IOException e) {
                            
                            throwAgain = true;
                            logger.error("ERROR CLOSING FILE", e);
                            
                        }
                        
                    }
                    
                    if(throwAgain) {
                        throw new IOException("ERROR CLOSING CHUNK FILE");
                    }
                    
                }
                
            }
            finally {
                in.close();
            }
            
        }
        
        {
            
            //generate GenDataReader
            
            final File dataReaderFile = new File(outputFolder, name + "Reader");
            final FileOutputStream readerOut = new FileOutputStream(dataReaderFile);
            
            try {
                
                readerOut.write(("package " + packageName +";import llc.ufwa.gen.GenDataReader;import java.util.List;import java.util.ArrayList;import llc.ufwa.gen.DataClass;").getBytes(Charset.forName("UTF-8")));
                readerOut.write(("public class " + name + "Reader" + " extends GenDataReader {").getBytes(Charset.forName("UTF-8")));
                readerOut.write(("public List<DataClass> getDataClasses(){ List<DataClass> l = new ArrayList<DataClass>();").getBytes(Charset.forName("UTF-8")));
                
                for(int i = 1; i <= chunk; i++) {
                    readerOut.write(("l.add(new " + name + "Chunk" + i + "());" ).getBytes(Charset.forName("UTF-8")));
                }
                
                readerOut.write(("return l;}}" ).getBytes(Charset.forName("UTF-8")));
                readerOut.flush();
                
                
            }
            finally {
                readerOut.close();
            }
            
        }
        
        
        
    }
    
    

}
