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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import llc.ufwa.util.StreamUtil;
import llc.ufwa.util.WebUtil.WebResponse;

public class WebUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(WebUtil.class);
    
    public static String doGet(final URL url, final Map<String, String> headers) throws IOException {
        return new String(doGetBytes(url, headers));        
    }
    
    public static byte [] doGetBytes(final URL url, final Map<String, String> headers) throws IOException {
        
        try {
            
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("GET");
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
                throw new IOException("<WebUtil><1>" + "Failed to get response");
            }
            
            return out.toByteArray();
                      
        } 
        catch (MalformedURLException e) {
            
            logger.error("<WebUtil><2>" + "ERROR:", e);
            throw new IOException("<WebUtil><3>" + "ERROR:");
            
        }
        catch (ProtocolException e) {
            
            logger.error("<WebUtil><4>" + "ERROR:", e);
            throw new IOException("<WebUtil><5>" + "Error:");
            
        } 
    }
    
    public static byte [] doDeleteBytes(final URL url, final Map<String, String> headers) throws IOException {
        
        try {
            
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("DELETE");
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
                throw new IOException("<WebUtil><1>" + "Failed to get response");
            }
            
            return out.toByteArray();
                      
        } 
        catch (MalformedURLException e) {
            
            logger.error("<WebUtil><2>" + "ERROR:", e);
            throw new IOException("<WebUtil><3>" + "ERROR:");
            
        }
        catch (ProtocolException e) {
            
            logger.error("<WebUtil><4>" + "ERROR:", e);
            throw new IOException("<WebUtil><5>" + "Error:");
            
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
                throw new IOException("<WebUtil><6>" + "Failed to get response");
            }
            
            return new String(out.toByteArray());
                      
        } 
        catch (MalformedURLException e) {
            
            logger.error("<WebUtil><7>" + "ERROR:", e);
            throw new IOException("<WebUtil><8>" + "ERROR:");
            
        }
        catch (ProtocolException e) {
            
            logger.error("<WebUtil><9>" + "ERROR:", e);
            throw new IOException("<WebUtil><10>" + "Error:");
            
        } 
        
    }
    
    public static String doPut(
            final URL url,
            final Map<String, String> headers
        ) throws IOException {
            
            try {
                
                final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                
                connection.setRequestMethod("PUT");
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
                    throw new IOException("<WebUtil><11>" + "Failed to get response");
                }
                
                return new String(out.toByteArray());
                          
            } 
            catch (MalformedURLException e) {
                
                logger.error("<WebUtil><12>" + "ERROR:", e);
                throw new IOException("<WebUtil><13>" + "ERROR:");
                
            }
            catch (ProtocolException e) {
                
                logger.error("<WebUtil><14>" + "ERROR:", e);
                throw new IOException("<WebUtil><15>" + "Error:");
                
            } 
            
        }

    public static InputStream doGetInputStream(URL url, Map<String, String> headers) throws IOException {

        try {
            
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            
            for(final Map.Entry<String, String> entry : headers.entrySet()) {
                connection.addRequestProperty(entry.getKey(), entry.getValue());
            }          
            connection.connect();
            
            return connection.getInputStream();
                      
        } 
        catch (MalformedURLException e) {
            
            logger.error("<WebUtil><16>" + "ERROR:", e);
            throw new IOException("<WebUtil><17>" + "ERROR:");
            
        }
        catch (ProtocolException e) {
            
            logger.error("<WebUtil><18>" + "ERROR:", e);
            throw new IOException("<WebUtil><19>" + "Error:");
            
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
                throw new IOException("<WebUtil><20>" + "Failed to get response");
            }
            
            return new String(out.toByteArray());
                      
        } 
        catch (MalformedURLException e) {
            
            logger.error("<WebUtil><21>" + "ERROR:", e);
            throw new IOException("<WebUtil><22>" + "ERROR:");
            
        }
        catch (ProtocolException e) {
            
            logger.error("<WebUtil><23>" + "ERROR:", e);
            throw new IOException("<WebUtil><24>" + "Error:");
            
        } 
    
    }
    
    public static class WebResponse {
        
        private final String response;
        private final Map<String, List<String>> headers = new HashMap<String, List<String>>(); 
        private final boolean wasError;
        
        public WebResponse(
            final String response, 
            final Map<String, List<String>> headers
        ) {
            this.response = response;
            this.headers.putAll(headers);
            this.wasError = false;
        }
        
        public WebResponse(
            final String response, 
            final Map<String, List<String>> headers,
            final boolean wasError
        ) {
            
            this.response = response;
            this.headers.putAll(headers);
            this.wasError = wasError;
            
        }
        
        public String getResponse() {
            return response;
        }
        
        public boolean wasError() {
            return wasError;
        }

        public List<String> getHeader(String headerName) {
            return headers.get(headerName);
        }
        
    }

    public static WebResponse doPostJSON(
        final URL url, 
        final Map<String, String> headers,
        final String body
    ) throws IOException {
        
        try {
            
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Accept", "application/json");
            if(!headers.containsKey("Content-Type")) {
                connection.addRequestProperty("Content-Type", "application/json");
            }

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
            
            if(connection.getResponseCode() != 200 && connection.getResponseCode() != 201) {
                
                final InputStream in = connection.getErrorStream();
                
                final ByteArrayOutputStream out = new ByteArrayOutputStream();            
                
                if(in != null) {
                    
                    try {
                        StreamUtil.copyTo(in, out);
                    }
                    finally {
                        in.close();
                    }
                    
                }
                
                return new WebResponse(new String(out.toByteArray()), connection.getHeaderFields(), true);
                
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
                throw new IOException("<WebUtil><26>" + "Failed to get response");
            }
            
            return new WebResponse(new String(out.toByteArray()), connection.getHeaderFields());
                      
        } 
        catch (MalformedURLException e) {
            
            logger.error("<WebUtil><27>" + "ERROR:", e);
            throw new IOException("<WebUtil><28>" + "ERROR:");
            
        }
        catch (ProtocolException e) {
            
            logger.error("<WebUtil><29>" + "ERROR:", e);
            throw new IOException("<WebUtil><30>" + "Error:");
            
        } 
    }

    public static WebResponse doPutJSON(
        final URL url,
        final Map<String, String> headers, 
        final String body
    ) throws IOException {
        
        try {
            
            final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            
            connection.setRequestMethod("PUT");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("Content-Type", "application/json");

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
            
            if(connection.getResponseCode() != 200) {
                throw new IOException("<WebUtil><31>" + "server returned code " + connection.getResponseCode());
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
                throw new IOException("<WebUtil><32>" + "Failed to get response");
            }
            
            return new WebResponse(new String(out.toByteArray()), connection.getHeaderFields());
                      
        } 
        catch (MalformedURLException e) {
            
            logger.error("<WebUtil><33>" + "ERROR:", e);
            throw new IOException("<WebUtil><34>" + "ERROR:");
            
        }
        catch (ProtocolException e) {
            
            logger.error("<WebUtil><35>" + "ERROR:", e);
            throw new IOException("<WebUtil><36>" + "Error:");
            
        } 
    }

    public static String doDelete(URL url, Map<String, String> headers) throws IOException {
        return new String(doDeleteBytes(url, headers));      
    }

}
