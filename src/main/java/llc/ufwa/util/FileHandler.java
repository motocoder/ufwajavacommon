	package llc.ufwa.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
/**
 * this class is deprecated and really old.
 * 
 * @author littleboy
 *
 */
public class FileHandler {
    private static Logger logger = LoggerFactory.getLogger(FileHandler.class);    
  
    public static InputStream getURLStream( String url ) {          
        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setUseCaches( false );
            return conn.getInputStream();
        } 
        catch ( Exception ex ) {
            logger.error("Could not get input stream for url " + url, ex);
            return null;
        }
    }
    
    public static List<String> getURLFilesList(URL url, String extension) {
    	
    	List<String> returnList = new ArrayList<String>();
        
        String finalString = null;
        try {
            InputStream in = (InputStream)url.openConnection().getContent();
            BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
            String inString =inReader.readLine();
            StringBuffer inBuffer = new StringBuffer();
            while(inString != null) {
                inBuffer.append(inString);
                inString = inReader.readLine();
            }
            finalString = inBuffer.toString();
        }
        catch(IOException ex) {
            logger.error("Error checking for path to test mode file ", ex);
        }
        
        if(finalString == null) {
            return returnList;
        }
        
        int stringLength = finalString.length();
        for(int i = finalString.indexOf(extension, 0); i < stringLength; i = finalString.indexOf(extension, i+1)) {
            int z;
            
            if(i < 0 ) break;
            
            for(z = i - 1; z > 0; z--) {
                if(finalString.charAt(z) == '"' || finalString.charAt(z) == '>') {
                    if(finalString.charAt(z) == '>') {
                        break;
                    }
                    String moduleName = finalString.substring(z+1, i);
                    
                    if(returnList.contains(moduleName)) break;
                    
                    returnList.add(moduleName); 
                    break;
                }
                 
            }
        }

        return returnList;
        
    }
    public static void copyFromStream(InputStream inFile, File outFile) {
        try {
            InputStreamReader in = new InputStreamReader(inFile);
            outFile.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(outFile);
            int c;
    
            while ((c = in.read()) != -1)
              out.write(c);
            
            out.close();  
            in.close();
            
        }
        catch(Exception ex) {
            logger.error("Could not copy fileStream from web to local", ex);
        }
    }
    public static BufferedReader getFileReader(String fileName) {
        try {
            return
                new BufferedReader(new FileReader(fileName));
            
        }
        catch(Exception ex) {
            logger.error("Could not open file " + fileName, ex);
            return null;
        }
    }
//    public static ImageIcon getImage( String name, ClassLoader cl ) {
//        java.net.URL imgURL = cl.getResource( name );
//
//        if ( imgURL != null ) {
//          return new ImageIcon( imgURL );
//        } else {
//          logger.error("Could not load image " + name);
//          return null;
//        }
//    }
    public static boolean downloadFile( String webFile, String lclFile ) {

        try {
          URLConnection conn = new URL(webFile).openConnection();
          conn.setUseCaches( false );
          InputStream is = conn.getInputStream();

          File f = new File( lclFile );
          f.getParentFile().mkdirs();
          FileOutputStream fos = new FileOutputStream( f );

          int oneChar = 0;
          while( (oneChar=is.read()) != -1 )  fos.write( oneChar );
          fos.close();

        } catch ( Exception ex ) {
          logger.error("Exception copying file ", ex);
          return false;
        }
        return true;
      }
    
    public static Collection<String> listFiles(URL url, String extension) {

        Set<String> gatheredFiles = new HashSet<String>();
        
        String finalString = null;
        try {
            InputStream in = (InputStream)url.openConnection().getContent();
            BufferedReader inReader = new BufferedReader(new InputStreamReader(in));
            String inString =inReader.readLine();
            StringBuffer inBuffer = new StringBuffer();
            while(inString != null) {
                inBuffer.append(inString);
                inString = inReader.readLine();
            }
            finalString = inBuffer.toString();
        }
        catch(IOException ex) {
            logger.error("Error checking for path to test mode file ", ex);
        }
        
        if(finalString == null) {
            return gatheredFiles;
        }
        
        int stringLength = finalString.length();
        for(int i = finalString.indexOf("." + extension, 0); i < stringLength; i = finalString.indexOf("." + extension, i+1)) {
            int z;
            
            if(i < 0 ) break;
            
            for(z = i - 1; z > 0; z--) {
                if(finalString.charAt(z) == '"' || finalString.charAt(z) == '>' ) {
                    if( finalString.charAt(z) == '>' ) {
                        break;
                    }
                    String moduleName = finalString.substring(z+1, i);
                    
                    gatheredFiles.add(moduleName + "." + extension);                    
                    break;
                }
                 
            }
        }
        
        return gatheredFiles;
        
    }
    
    /**
     * creates image icon from png image in the com/cefd/th/images directory
     * @param name name with extension ie "image.png"
     * @return ImageIcon of the image.
     */
//    public static ImageIcon createImageIcon2( String name ) {
//        
//        java.net.URL imgURL = FileHandler.class.getResource( "com/cefd/utilities/images/"+name );
//    
//        if ( imgURL != null ) {
//          return new ImageIcon( imgURL );
//        } else {
//          logger.error("Could not find image icon " + name);
//          return null;
//        }
//      }

      public static BufferedReader getURLReader( String url ) {    
          BufferedReader bf = null;
          try {
            URLConnection conn = new URL(url).openConnection();
            conn.setUseCaches( false );
            bf = new BufferedReader( new InputStreamReader(conn.getInputStream()) );
          } catch ( Exception ex ) {
      	    ex.printStackTrace();
            
          }
          return bf;
        }
      
      public static BufferedReader getURLReader( final URL url ) {    
          BufferedReader bf = null;
          try {
            URLConnection conn = url.openConnection();
            conn.setUseCaches( false );
            bf = new BufferedReader( new InputStreamReader(conn.getInputStream()) );
          } catch ( Exception ex ) {
      	    ex.printStackTrace();
            
          }
          return bf;
        }
      
    public static String readString(URL url) throws IOException {
    	
    	URLConnection conn = url.openConnection();
    	conn.setUseCaches( false );
    	
    	BufferedInputStream in = new BufferedInputStream(conn.getInputStream());
    	final StringBuilder builder = new StringBuilder();
    	
    	final byte [] buffer = new byte[4048];
    	
    	while(true) {
    		final int read = in.read(buffer);
    		
    		if(read > 0) {
    			builder.append(new String(buffer, 0, read));
    		}
    		else {
    			break;
    		}
    	}
    	
        return builder.toString();
    	
    }
    
    public static String extractFileAsString(String fname) {
        StringBuffer sb = new StringBuffer("");

        try {
          ClassLoader cl = FileHandler.class.getClassLoader();
          URL url = cl.getResource( fname );
          BufferedReader br = new BufferedReader( new InputStreamReader(url.openStream()) );

          String line = "";
          while (line != null) {
            sb.append(line).append("\n");
            line = br.readLine();
          }
          br.close();
          return sb.toString();

        } catch (Exception ex) {
          logger.error("error", ex);
          return null;
        }
      }
   
  ///////////////////////////////////////////////////////////////////////////////////////////
  /* info of xml def file */
  ///////////////////////////////////////////////////////////////////////////////////////////

//    public static ImageIcon createImageIcon( String path ) {
//      ClassLoader cl = FileHandler.class.getClassLoader();
//      java.net.URL imgURL = cl.getResource( path );
//
//      if ( imgURL != null ) {
//        return new ImageIcon( imgURL );
//      } else {
//        logger.error( "Couldn't find file: " + path );
//        return null;
//      }
//    }
//
//    public static java.awt.Image getImage( String name ) {
//      ClassLoader cl = FileHandler.class.getClassLoader();
//      java.net.URL imgURL = cl.getResource( "com/cefd/th/images/"+name );
//
//      if ( imgURL != null ) {
//        return new ImageIcon( imgURL ).getImage();
//      } else {
//        logger.error( "Couldn't find file: com/cefd/th/images/"+name );
//        return null;
//      }
//    }  

}