package llc.ufwa.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtilities {
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private static DateFormat quickDf = new SimpleDateFormat("MM-dd-yyyy");
    private static DateFormat shortFormat = new SimpleDateFormat("MM/dd HH:mm");
    private static DateFormat df_brief = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static String formatBriefDateTime( Date date ) {
        return df_brief.format( date );
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
}
