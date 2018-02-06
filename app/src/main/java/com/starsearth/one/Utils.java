package com.starsearth.one;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by faimac on 2/6/18.
 */

public class Utils {

    /*
        Returns date in local time zone
     */
    public static String formatDateTime(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(timestamp));
        int offsetFromUTC = getOffsetFromUTC(cal);
        cal.add(Calendar.MILLISECOND, offsetFromUTC);
        Date date = cal.getTime(); //For debugging
        String monthString = String.format(Locale.US,"%tB",cal);
        monthString = formatStringFirstLetterCapital(monthString);
        String finalString = cal.get(Calendar.DATE) + " " + monthString + " " + cal.get(Calendar.YEAR);
        return finalString;
    }

    public static String formatStringFirstLetterCapital(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /*
        This function returns the offset from GMT for the current timezone
        Returns: offset in millis
     */
    public static int getOffsetFromUTC(Calendar cal) {
        Date date = cal.getTime();
        TimeZone tz = cal.getTimeZone();
        //Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
        long msFromEpochGmt = date.getTime();
        //gives you the current offset in ms from GMT at the current date
        int offsetFromUTC = tz.getOffset(msFromEpochGmt);
        return offsetFromUTC;
    }
}
