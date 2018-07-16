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
        Used for formatting timestamp
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

    public static String getTimeFormatted(long timeMillis) {
        long seconds = timeMillis/1000;
        long minutes = seconds/60;
        long hours = minutes/60;

        StringBuilder b = new StringBuilder();
        if (hours > 9) {
            b.append(hours);
        }
        else if (hours > 0) {
            b.append("0" + hours);
        }

        if (minutes > 0) {
            if (b.length() > 0) {
                //hours exists
                b.append("H ");
            }

            if (minutes > 9) {
                b.append(minutes);
            }
            else {
                //0 < mins < 10
                b.append("0"+minutes);
            }
        }

        if (seconds > 0) {
            if (b.length() > 0) {
                //hours or mins exists
                b.append("M ");
            }

            if (seconds > 9) {
                b.append(seconds);
            }
            else {
                b.append("0"+seconds);
            }
            b.append('S');
        }

        return b.toString();
    }
}
