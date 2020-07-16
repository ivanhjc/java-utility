package net.ivanhjc.utility.data;

import org.apache.commons.lang3.RandomUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Date and time utilities
 *
 * @author Ivan Huang on 2018/1/3
 */
public class DateUtils {

    public static final SimpleDateFormat DATE_FORMAT_ONLY_TIME = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat DATE_FORMAT_01 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat DATE_FORMAT_02 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEE", Locale.ENGLISH);
    public static final SimpleDateFormat DATE_FORMAT_03 = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * Returns the current time in the form "hh:mm:ss"
     *
     * @return the time string to return
     */
    public static String nowTime() {
        return LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    /**
     * Obtains only the time part from a date
     *
     * @param date the given date
     * @return the time string to return
     */
    public static String getTime(Date date) {
        return DATE_FORMAT_ONLY_TIME.format(date);
    }

    /**
     * Obtains only the date part from a date
     *
     * @param date
     * @return
     * @see org.apache.commons.lang3.time.DateUtils#truncate(Date, int)
     */
    public static Date getDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Returns the day of week of a given date with the day of week of Monday being 1 and Sunday being 7
     *
     * @param date the given date
     * @return an integer representing the day of week
     */
    public static int dayOfWeekMon1(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int day = cal.get(Calendar.DAY_OF_WEEK);
        return day == 1 ? 7 : day - 1;
    }

    // TODO: 3/29/2019 Find the day of year given the day of month and day of week. For example,
    //  knowing February 11th is a Friday find which year this day is in.
    //  https://www.easycalculation.com/date-day/same-calendar-years.php

    /**
     * Returns the day of month of a given date
     *
     * @param date the given date
     * @return an integer value representing the day of month
     */
    public static int dayOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Checks if a time or a period of time falls into a given periods of time. The time strings are always formatted like "HH:mm:ss" or "HH:mm",
     * with the hour, minute and second numbers being double-digit (0-padded if they are not two-digit integers). A period of time is represented
     * by using a hyphen between two time strings. Returns true if the time or period is between one of the given periods of time. To indicate
     * a period of time that spans two days, such as 21:00 to next day's 06:00, you should use two periods of time, 21:00-23:59:59 and 00:00-06:00,
     * instead of 21:00-06:00, which will return false for any time or period.
     *
     * <p>Examples:
     * <ul>
     * <li>("05:00", "01:00-03:00", "04:00-18:00") -> true</li>
     * <li>("05:00:30", "01:00-03:00", "04:00-18:00") -> true</li>
     * <li>("05:00-13:00", "01:00-03:00", "04:00-18:00") -> true</li>
     * <li>("02:00-05:00", "01:00-03:00", "04:00-18:00") -> false</li>
     * <li>("03:00-04:00", "21:00-06:00") -> false</li>
     * <li>("03:00-04:00", "21:00-23:59:59", "00:00-06:00") -> true</li>
     * </ul>
     * </p>
     *
     * @param time    the time or period of time to check, not null or empty
     * @param periods the periods of time to check if time is in, not null or empty
     */
    public static boolean inPeriods(String time, String... periods) {
        String[] myTimes = time.split("-");
        if (myTimes.length > 2)
            return false;

        LocalTime myTime1 = LocalTime.parse(myTimes[0]);
        LocalTime myTime2 = myTimes.length == 2 ? LocalTime.parse(myTimes[1]) : null;
        for (String period : periods) {
            String[] times = period.split("-");
            LocalTime time1 = LocalTime.parse(times[0]);
            LocalTime time2 = LocalTime.parse(times[1]);
            if (inPeriods(myTime1, time1, time2)) {
                if (myTime2 == null)
                    return true;
                if (inPeriods(myTime2, time1, time2))
                    return true;
            }
        }
        return false;
    }

    public static boolean inPeriods(LocalTime myTime, LocalTime time1, LocalTime time2) {
        return (myTime.isAfter(time1) || myTime.equals(time1)) && (myTime.isBefore(time2) || myTime.equals(time2));
    }

    /**
     * A convenient method for {@link #inPeriods(String, String...)}
     *
     * @param time    the time or period of time to check
     * @param periods the periods of time to check if time is in. Multiple periods are separated by commas.
     */
    public static boolean inPeriods(String time, String periods) {
        return inPeriods(time, periods.split(","));
    }

    /**
     * A convenient method for {@link #inPeriods(String, String...)}
     *
     * @param date    the date to check
     * @param periods the periods of time to check if time is in. Multiple periods are separated by commas.
     */
    public static boolean inPeriods(Date date, String periods) {
        return inPeriods(getTime(date), periods);
    }

    public static boolean inPeriods(Date date, DayType dayType, String days, String periods) {
        switch (dayType) {
            case WEEK:

            case MONTH:
        }
        return true;
    }

    /**
     * Checks if a given date is in one of the given days of week
     *
     * @param date       the date to check
     * @param daysOfWeek formatted as "1,2,3" where 1 is Monday and 7 is Sunday
     */
    public static boolean inDaysOfWeek(Date date, String daysOfWeek) {
        int[] days = ListUtils.parseInt(daysOfWeek);
        int day = dayOfWeekMon1(date);
        for (int d : days) {
            if (d == day)
                return true;
        }
        return false;
    }

    /**
     * Checks if a given date is in one of the given days of month
     *
     * @param date        the date to check
     * @param daysOfMonth the first day of month is 1
     */
    public static boolean inDaysOfMonth(Date date, String daysOfMonth) {
        int[] days = ListUtils.parseInt(daysOfMonth);
        int day = dayOfMonth(date);
        for (int d : days) {
            if (d == day)
                return true;
        }
        return false;
    }

    public static Date getRandomDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, RandomUtils.nextInt(2000, 2020));
        cal.set(Calendar.DAY_OF_YEAR, RandomUtils.nextInt(1, cal.getActualMaximum(Calendar.DAY_OF_YEAR)));
        return cal.getTime();
    }

    /**
     * Returns the number of seconds between two times
     *
     * @param time1 value in range 00:00:00-23:59:59 {@link java.time.temporal.ChronoField#HOUR_OF_DAY}
     * @param time2 value in range 00:00:00-23:59:59
     * @see TemporalUnit#between(Temporal, Temporal)
     * @see Temporal#until(Temporal, TemporalUnit)
     */
    public static long getIntervalInSeconds(String time1, String time2) {
        return ChronoUnit.SECONDS.between(LocalTime.parse(time1), LocalTime.parse(time2));
    }

    public static long getSecondsFromNowToMidnight() {
        return LocalTime.now().until(LocalTime.parse("23:59:59"), ChronoUnit.SECONDS);
    }

    /**
     * Converts a given time to a vernacular time string
     *
     * @param date the time to convert
     * @return the name
     */
    public static String getPointOfTimeName(Date date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime point = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        String name;
        long n, absN;
        if ((absN = Math.abs(n = now.until(point, ChronoUnit.SECONDS))) < 60) {
            name = absN + "秒";
        } else if ((absN = Math.abs(n = now.until(point, ChronoUnit.MINUTES))) < 60) {
            name = absN + "分钟";
        } else if ((absN = Math.abs(n = now.until(point, ChronoUnit.HOURS))) < 24) {
            name = absN + "小时";
        } else if ((absN = Math.abs(n = now.until(point, ChronoUnit.DAYS))) < 365) {
            name = absN + "天";
        } else if ((absN = Math.abs(n = now.until(point, ChronoUnit.YEARS))) < 100) {
            name = absN + "年";
        } else {
            absN = Math.abs(n = now.until(point, ChronoUnit.CENTURIES));
            name = absN + "个世纪";
        }
        return name.concat((n <= 0 ? "前" : "后"));
    }

    /**
     * Checks if a given date is over given length of time (in hours, minutes, etc.) from now
     *
     * @param startPoint the date to check
     * @param timeLength the length of time to check
     * @param unit       the unit to check, for example, to check if "2018-08-21 19:00:00" is over 3.5 hours from now, provide unit as {@link TimeUnit#HOUR}
     * @return true if the date is over the given length of time
     */
    public static boolean isOverFromNow(Date startPoint, double timeLength, TimeUnit unit) {
        return new Date().getTime() - startPoint.getTime() > timeLength * unit.MULTIPLIER;
    }

    /**
     * Returns the number of seconds, minutes, etc. between two dates
     *
     * @param date1 the later date
     * @param date2 the earlier date
     * @param timeUnit  the unit of the number
     */
    public static BigDecimal getIntervalBetween(Date date1, Date date2, TimeUnit timeUnit) {
        return BigDecimal.valueOf((double) (date1.getTime() - date2.getTime()) / timeUnit.MULTIPLIER);
    }

    /**
     * Adds to a date returning a new object.The original {@code Date} is unchanged.
     *
     * @param startPoint the date to add to
     * @param timeLength the amount of time to add to the date, may be negative
     * @param unit       the unit of the amount
     * @see org.apache.commons.lang3.time.DateUtils#addYears(Date, int)
     */
    public static Date add(final Date startPoint, double timeLength, TimeUnit unit) {
        return new Date(startPoint.getTime() + Math.round(timeLength * unit.MULTIPLIER));
    }

    public enum TimeUnit {
        MILLISECOND(1),
        SECOND(1000),
        MINUTE(60000),
        HOUR(3600000),
        DAY(86400000),
        WEEK(604800000);

        public final int MULTIPLIER;

        TimeUnit(int multiplier) {
            this.MULTIPLIER = multiplier;
        }
    }

    public enum DayType {
        WEEK, MONTH
    }
}
