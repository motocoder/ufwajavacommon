package llc.ufwa.util;
import java.io.File;
import java.io.IOException;


public class DeleteSVN {

    public static void main(String ... args) {
        
        final File file = new File(".");
        
        traverse(file);
        
         
    }
    
    private static void traverse(File file) {
        
        if(file.getName().toLowerCase().equals(".svn")) {
            
            System.out.println(file.getAbsolutePath());
            file.delete();
            return;
            
        }
        
        if(file.list() == null || file.list().length == 0) {
            return;
        }
        
        for(final String child : file.list()) {
            
            try {
                
                traverse(new File(file.getCanonicalPath() + File.separator + child));
                
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
    }
    
}
