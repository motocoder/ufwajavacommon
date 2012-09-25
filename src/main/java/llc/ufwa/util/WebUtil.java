package llc.ufwa.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llc.ufwa.util.StreamUtil;

public class WebUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(WebUtil.class);
    
    public static String doGet(final URL url, final Map<String, String> headers) throws IOException {
        return new String(doGetBytes(url, headers));        
    }
    
    public static byte [] doGetBytes(final URL url, final Map<String, String> headers) throws IOException {
        
        try {
            
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(10000);
            
            for(final Map.Entry<String, String> entry : headers.entrySet()) {
                connection.addRequestProperty(entry.getKey(), entry.getValue());
            }          
            connection.connect();
            
            final ByteArrayOutputStream out = new ByteArrayOutputStream();            
            final InputStream in = connection.getInputStream();
            
            if(in != null) {
                
                try {
                    StreamUtil.copyTo(in, out);
                }
                finally {
                    in.close();
                }
                
            }
            else {
                throw new IOException("Failed to get response");
            }
            
            return out.toByteArray();
                      
        } 
        catch (MalformedURLException e) {
            
            logger.error("ERROR:", e);
            throw new IOException("ERROR:");
            
        }
        catch (ProtocolException e) {
            
            logger.error("ERROR:", e);
            throw new IOException("Error:");
            
        } 
    }

    public static String doPutXML(
        final URL url,
        final Map<String, String> headers,
        final String body
    ) throws IOException {
        
        try {
            
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/xml");

            connection.setReadTimeout(10000);
            
            for(final Map.Entry<String, String> entry : headers.entrySet()) {
                connection.addRequestProperty(entry.getKey(), entry.getValue());
            }   
            
            connection.connect();
            
            {
                
                final OutputStream writing = connection.getOutputStream();
                
                try {
                    
                    final InputStream reading = new ByteArrayInputStream(body.getBytes("UTF-8"));
                    
                    StreamUtil.copyTo(reading, writing);
                    
                }
                finally {
                    writing.close();
                }
                
            }
            
            final ByteArrayOutputStream out = new ByteArrayOutputStream();            
            final InputStream in = connection.getInputStream();
            
            if(in != null) {
                
                try {
                    StreamUtil.copyTo(in, out);
                }
                finally {
                    in.close();
                }
                
            }
            else {
                throw new IOException("Failed to get response");
            }
            
            return new String(out.toByteArray());
                      
        } 
        catch (MalformedURLException e) {
            
            logger.error("ERROR:", e);
            throw new IOException("ERROR:");
            
        }
        catch (ProtocolException e) {
            
            logger.error("ERROR:", e);
            throw new IOException("Error:");
            
        } 
        
    }
    
    public static String doPut(
            final URL url,
            final Map<String, String> headers
        ) throws IOException {
            
            try {
                
                final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                
                connection.setRequestMethod("PUT");
                connection.setDoOutput(false);

                connection.setReadTimeout(10000);
                
                for(final Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.addRequestProperty(entry.getKey(), entry.getValue());
                }   
                
                connection.connect();
                
                final ByteArrayOutputStream out = new ByteArrayOutputStream();            
                final InputStream in = connection.getInputStream();
                
                if(in != null) {
                    
                    try {
                        StreamUtil.copyTo(in, out);
                    }
                    finally {
                        in.close();
                    }
                    
                }
                else {
                    throw new IOException("Failed to get response");
                }
                
                return new String(out.toByteArray());
                          
            } 
            catch (MalformedURLException e) {
                
                logger.error("ERROR:", e);
                throw new IOException("ERROR:");
                
            }
            catch (ProtocolException e) {
                
                logger.error("ERROR:", e);
                throw new IOException("Error:");
                
            } 
            
        }

    public static InputStream doGetInputStream(URL url, Map<String, String> headers) throws IOException {

        try {
            
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setReadTimeout(10000);
            
            for(final Map.Entry<String, String> entry : headers.entrySet()) {
                connection.addRequestProperty(entry.getKey(), entry.getValue());
            }          
            connection.connect();
            
            return connection.getInputStream();
                      
        } 
        catch (MalformedURLException e) {
            
            logger.error("ERROR:", e);
            throw new IOException("ERROR:");
            
        }
        catch (ProtocolException e) {
            
            logger.error("ERROR:", e);
            throw new IOException("Error:");
            
        } 
    }

    public static String doGetXML(
        final URL url,
        final Map<String, String> headers,
        final String body
    ) throws IOException {
        
        try {
            
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/xml");

            connection.setReadTimeout(10000);
            
            for(final Map.Entry<String, String> entry : headers.entrySet()) {
                connection.addRequestProperty(entry.getKey(), entry.getValue());
            }   
            
            connection.connect();
            
            {
                
                final OutputStream writing = connection.getOutputStream();
                
                try {
                    
                    final InputStream reading = new ByteArrayInputStream(body.getBytes("UTF-8"));
                    
                    StreamUtil.copyTo(reading, writing);
                    
                }
                finally {
                    writing.close();
                }
                
            }
            
            final ByteArrayOutputStream out = new ByteArrayOutputStream();            
            final InputStream in = connection.getInputStream();
            
            if(in != null) {
                
                try {
                    StreamUtil.copyTo(in, out);
                }
                finally {
                    in.close();
                }
                
            }
            else {
                throw new IOException("Failed to get response");
            }
            
            return new String(out.toByteArray());
                      
        } 
        catch (MalformedURLException e) {
            
            logger.error("ERROR:", e);
            throw new IOException("ERROR:");
            
        }
        catch (ProtocolException e) {
            
            logger.error("ERROR:", e);
            throw new IOException("Error:");
            
        } 
    
    }

}
