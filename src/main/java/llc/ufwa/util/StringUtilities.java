package llc.ufwa.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import llc.ufwa.data.beans.ParsedProperty;


public final class StringUtilities {
    
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private static DateFormat quickDf = new SimpleDateFormat("MM-dd-yyyy");
    private static DateFormat shortFormat = new SimpleDateFormat("MM/dd HH:mm");
    
    
    public static String join(Collection<?> values, String joinString) {
        StringBuilder build = new StringBuilder();        
        
        for(Object value : values ) {
            build.append(value.toString()).append(joinString);
        }
        
        return values.size() == 0 ? "" : build.substring(0, build.lastIndexOf(joinString) );
    }
    
    public static String formatDateTime( Date date ) {
        return df.format( date );
    }
    public static String formatQuickDateTime( Date date ) {
        return quickDf.format( date );
    }
    public static String formatShortDateTime( Date date ) {
        return shortFormat.format( date );
    }
    
    
    public static String join(Object [] values, String joinString) {
        StringBuilder build = new StringBuilder();        
        for(Object value : values ) {
            build.append(String.valueOf(value)).append(joinString);
        }
        
        return build.substring(0, build.lastIndexOf(joinString) );
    }

	public static String formatDuration(long millis) {
		
		long sec = millis / 1000;
		long min = sec /60;
		long hour = min / 60;
		
		return hour + "hrs " + (min % 60) + "mins " + (sec %60) + "secs";
		
	}
	
    public static Set<String> getAllSubstrings(final String original) {

        final Set<String> returnVals = new HashSet<String>();

        final int length = original.length();

        for (int c = 0; c < length; c++) {

            for (int i = 1; i <= length - c; i++) {
                returnVals.add(original.substring(c, c + i));
            }

        }

        return returnVals;

    }
	
	public static boolean isEmpty(String check) {

        final boolean returnVal;

        if (check == null) {
            returnVal = true;
        }
        else if (check.trim().length() == 0) {
            returnVal = true;
        }
        else {
            returnVal = false;
        }

        return returnVal;

    }


	public static String formatDayDuration(long millis) {
		long sec = millis / 1000;
		long min = sec /60;
		long hour = min / 60;
		long days = hour/ 24;
		
		return days + "days " + (hour % 24) + "hrs " + (min % 60) + "mins " + (sec %60) + "secs";
		
	}
	
	public static String formatSQLSafeString(String filteringStr) {
		String[] toxicCharacters = {"\'", ";", "\\"};
		String[] safeCharacters = {"\"", ".", "\\\\"};
		for (int i=0; i < toxicCharacters.length; i++) {
			filteringStr = filteringStr.replace(toxicCharacters[i], safeCharacters[i]);
		}
		return filteringStr.trim();
	}
	
	public static String formatMoney(long cents) {
	    StringBuilder money = new StringBuilder(String.format("%.2f",((float)cents) /100f));
	    
	    final int decimal = money.indexOf(".");

	    for(int skipped = 0, i = decimal; i > 0; i--, skipped++) {
	        if(skipped == 3) {
	            skipped = 0;
	            money.insert(i, ',');
	        }
	    }
	    
	    money.insert(0, "$");
	    
	    return money.toString();
	}
	
	/**
	 * 
	 * @param toParse
	 * @return
	 * @throws FormatException
	 */
	public static List<ParsedProperty> parseProperties(String toParse) throws FormatException {
		
		final List<ParsedProperty> returnVal =
			new ArrayList<ParsedProperty>();
		
		int index = toParse.indexOf("${");
		
		while(true) {

			if(index < 0) {
				break;
			}
			
			int stopIndex = toParse.indexOf("}", index);
			
			if(stopIndex < 0) {
				throw new FormatException("Invalid Format " + toParse);
			}
			
			final String propName = toParse.substring(index + 2, stopIndex);
			
			returnVal.add(new ParsedProperty(index, propName));
			
			index += 2;			
			index = toParse.indexOf("${", index);
		}
		
		return returnVal;
	}
	
	public static String generateSpaces(int size) {
		StringBuilder returnVal = new StringBuilder();
		
		for(int i = 0; i < size; i++) {
			returnVal.append(" ");
		}
		
		return returnVal.toString();
	}

    public static String limitString(
        final String body,
        final String string,
        final int size
    ) {
        
        if(body.length() < size) {
            return body;
        }
        else {
            return body.substring(0, size - string.length()) + string;
        }
        
    }
	
	/** from GWTStringUtils TODO remove?
	 * 
	 * public static String join(Collection<?> values, String joinString) {
        return join(values.toArray(), joinString);      
    }
    
    public static String join(Object [] values, String joinString) {
        
        final String returnVal;
        
        if(values.length > 0) {
            StringBuilder build = new StringBuilder();        
            for(Object value : values ) {
                build.append(value.toString()).append(joinString);
            }
            
            returnVal = build.substring(0, build.lastIndexOf(joinString) );
        }
        else {
            returnVal = "";
        }
        
        return returnVal;
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    public static String getHexString(byte[] b) {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
              Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public static String formatDuration(long millis) {
        
        long sec = millis / 1000;
        long min = sec /60;
        long hour = min / 60;
        
        return hour + "hrs " + (min % 60) + "mins " + (sec %60) + "secs";
        
    }
    
//  public static String formatBriefDayDuration(long duration) {
//      StringBuilder timeStr = new StringBuilder();
//      if (duration >= 0) {        
//          duration /= 1000;
//          int days = (int) (duration / 86400);
//          duration %= 86400;
//          int hrs = (int) (duration / 3600);
//          duration %= 3600;
//          int mins = (int) (duration / 60);
//          duration %= 60;
//          int secs = (int) duration;
//          timeStr.append(String.format("%03d:", days));
//          timeStr.append(String.format("%02d:", hrs));
//          timeStr.append(String.format("%02d:", mins));
//          timeStr.append(String.format("%02d", secs));
//      } else {
//          timeStr.append(" ");
//      }
//      return timeStr.toString();
//    }
//
//  public static String formatDayDuration(long duration) {
//      
//      StringBuilder timeStr = new StringBuilder();
//      
//      if (duration < 0) {
//          timeStr.append("-");
//      }
//      
//      duration = Math.abs(duration);  
//                        
//        duration /= 1000;
//        int days = (int) (duration / 86400);
//        duration %= 86400;
//        int hrs = (int) (duration / 3600);
//        duration %= 3600;
//        int mins = (int) (duration / 60);
//        duration %= 60;
//        int secs = (int) duration;
//        timeStr.append(String.format("%03d", days) + " Days ");
//        timeStr.append(String.format("%02d", hrs) + " Hrs ");
//        timeStr.append(String.format("%02d", mins) + " Mins ");
//        timeStr.append(String.format("%02d", secs) + " Secs");
//        
//        return timeStr.toString();
//    }
//  
//  
//    public static String formatHourDuration(long duration) {
//        StringBuilder timeStr = new StringBuilder();
//        if (duration >= 0) {
//          long hours = duration / 3600000;
//          long mins = duration /60000 % 60;
//          long secs = duration / 1000 % 60;
//          
//          timeStr.append(String.format("%03d", hours) + " Hrs ");
//          timeStr.append(String.format("%02d", mins) + " Mins ");
//          timeStr.append(String.format("%02d", secs) + " Secs");
//        }
//        
//        return  timeStr.toString();
//    }
//    
//    public static String formatBriefHourDuration(long duration) {
//        StringBuilder timeStr = new StringBuilder();
//        if (duration >= 0) {
//          long hours = duration / 3600000;
//          long mins = duration /60000 % 60;
//          long secs = duration / 1000 % 60;
//          
//          timeStr.append(String.format("%03d H: ", hours));
//          timeStr.append(String.format("%02d M: ", mins));
//          timeStr.append(String.format("%02d S", secs));
//        }
//        
//        return  timeStr.toString();
//    }
    
    
    public static String formatSQLSafeString(String filteringStr) {
        String[] toxicCharacters = {"\'", ";", "\\"};
        String[] safeCharacters = {"\"", ".", "\\\\"};
        for (int i=0; i < toxicCharacters.length; i++) {
            filteringStr = filteringStr.replace(toxicCharacters[i], safeCharacters[i]);
        }
        return filteringStr.trim();
    }
//  
//  public static String formatMoney(long cents) {
//      StringBuilder money = new StringBuilder(String.format("%.2f",((float)cents) /100f));
//      
//      final int decimal = money.indexOf(".");
//
//      for(int skipped = 0, i = decimal; i > 0; i--, skipped++) {
//          if(skipped == 3) {
//              skipped = 0;
//              money.insert(i, ',');
//          }
//      }
//      
//      money.insert(0, "$");
//      
//      return money.toString();
//  }
    
    public static List<ParsedProperty> parseProperties(String toParse) throws FormatException {
        
        final List<ParsedProperty> returnVal =
            new ArrayList<ParsedProperty>();
        
        int index = toParse.indexOf("${");
        
        while(true) {

            if(index < 0) {
                break;
            }
            
            int stopIndex = toParse.indexOf("}", index);
            
            if(stopIndex < 0) {
                throw new FormatException("Invalid Format " + toParse);
            }
            
            final String propName = toParse.substring(index + 2, stopIndex);
            
            returnVal.add(new ParsedProperty(index, propName));
            
            index += 2;         
            index = toParse.indexOf("${", index);
        }
        
        return returnVal;
    }
    
    public static String generateSpaces(int size) {
        StringBuilder returnVal = new StringBuilder();
        
        for(int i = 0; i < size; i++) {
            returnVal.append(" ");
        }
        
        return returnVal.toString();
    }
    
    
    // this method is used to check special chars in sql string
    public static String escape(String field) {
      String rtn = field.replace("'", "''");
      return rtn;
    }
	 */
}
