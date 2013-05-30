package llc.ufwa.gen;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llc.ufwa.util.StringUtilities;

public abstract class GenDataReader {
    
    private static final Logger logger = LoggerFactory.getLogger(GenDataReader.class);
    
    public InputStream getInputStream() throws IOException {
        
        return new GZIPInputStream(new InputStream() {
            
            private DataClass currentDataClass = null;
            private String currentData = null;
            private int currentIndex = 0;
            private int currentDataClassStart = 0;
            
            @Override
            public int read() throws IOException {
                
                if(currentData == null || currentDataClass.length() <= (currentIndex + currentDataClassStart)) {
                    
                    final List<DataClass> datas = getDataClasses();
                     
                    int start = 0;
                    
                    this.currentData = null;
                    
                    for(final DataClass data : datas) {
                        
                        final int length = data.length();
                        
                        if(currentIndex >= start && currentIndex < (start + length)) {
                            
                            this.currentDataClass = data;
                            this.currentData = data.getData();
                            this.currentDataClassStart = start;
                            break;
                            
                        }
                        else {
                            start += length;
                        }
                        
                    }
                    
                }
                
                if(currentData == null) {
                    return -1;
                }
                
                final int index = (currentIndex - currentDataClassStart) * 2;
                
                final byte[] returnBytes = StringUtilities.hexStringToByteArray(currentData.substring(index, index + 2));
                
                if(returnBytes.length != 1) {
                    throw new RuntimeException("couldn't convert returnBytes hex properly");
                }
                
                currentIndex++;
                
                return returnBytes[0] & 0xff;
                
            }

            @Override
            public void close() throws IOException {
                
                super.close();
                
                this.currentDataClass = null;
                this.currentData = null;
                this.currentIndex = 0;
                this.currentDataClassStart = 0;
                
            }
            
        }
        );
        
    }
    
    public abstract List<DataClass> getDataClasses();

}
