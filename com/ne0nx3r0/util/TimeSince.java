package com.ne0nx3r0.util;

import java.util.Date;

public class TimeSince {
    public static String getTimeSinceString(Date then) {
        return TimeSince.getTimeSinceString(then, new Date());
    }
    
    // source:
    // http://www.mkyong.com/java/java-time-elapsed-in-days-hours-minutes-seconds/
    public static String getTimeSinceString(Date then, Date now) {
        //milliseconds
        long different = now.getTime() - then.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        if(elapsedDays > 0) {
            return elapsedDays+" days";
        }

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        if(elapsedHours > 0) {
            return elapsedHours+" hours";
        }

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        if(elapsedMinutes > 0) {
            return elapsedMinutes+" minutes";
        }

        long elapsedSeconds = different / secondsInMilli;

        if(elapsedSeconds > 0) {
            return elapsedSeconds+" seconds";
        }
        
        return different+"ms";
    }
}
