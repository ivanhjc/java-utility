package net.ivanhjc.utility.data;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Ivan Huang on 2018/1/3
 */
public class DateUtilsTest {

    private static final SimpleDateFormat f0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat f1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEE", Locale.ENGLISH);
    private static final String testDate1 = "2018-3-26 17:50:02";

    @Test
    public void parseDate() throws ParseException {
        Date date = f0.parse(testDate1);
        System.out.println(date);
    }

    @Test
    public void nowTime() {
        System.out.println(DateUtils.nowTime());
    }

    @Test
    public void getTime() throws ParseException {
        System.out.println(DateUtils.getTime(f0.parse(testDate1)));
    }

    @Test
    public void getDate() throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = format.parse("2018-06-27");
        Date date2 = DateUtils.getDate(new Date());
        Date date3 = org.apache.commons.lang3.time.DateUtils.truncate(new Date(), Calendar.DATE);
        System.out.println(date1.equals(date2));
        System.out.println(date1.equals(date3));
        System.out.println(org.apache.commons.lang3.time.DateUtils.truncatedEquals(date1, new Date(), Calendar.DATE));
    }

    @Test
    public void dayOfWeekMon1() throws ParseException {
        System.out.println(DateUtils.dayOfWeekMon1(f0.parse(testDate1)));
    }

    @Test
    public void dayOfMonth() throws ParseException {
        System.out.println(DateUtils.dayOfMonth(f0.parse(testDate1)));
    }

    @Test
    public void inPeriods() {
        System.out.println(DateUtils.inPeriods("23:00", "22:00-03:00"));
        /*System.out.println(DateUtils.inPeriods("05:00:30", "01:00-03:00", "04:00-18:00"));
        System.out.println(DateUtils.inPeriods("05:00-13:00", "01:00-03:00", "04:00-18:00"));
        System.out.println(DateUtils.inPeriods("02:00-05:00", "01:00-03:00", "04:00-18:00"));
        System.out.println(DateUtils.inPeriods("03:00-04:00", "21:00-06:00"));
        System.out.println(DateUtils.inPeriods("03:00-04:00", "21:00-23:59:59", "00:00-06:00"));
        System.out.println(DateUtils.inPeriods("03:00-04:00", "21:00-23:59:59,00:00-06:00"));
        System.out.println(DateUtils.inPeriods(new Date(), "21:00-23:59:59,06:00-18:00"));*/
    }

    @Test
    public void inDaysOfWeek() {
        Date[] dates = new Date[7];
        for (int i = 0; i < dates.length; i++) {
            dates[i] = DateUtils.getRandomDate();
        }
        String formatter = "%-30s %-50s %-10s %s%n";
        String pointer = "->";
        System.out.printf(formatter, "Date", "Days", "", "inDaysOfWeek");
        String[] days = {"0,1,2,3", "-1,0,1,2,5", "8,9,10"};
        for (Date date1 : dates) {
            for (String day : days) {
                System.out.printf("%-30s %-50s %-10s %s%n", f1.format(date1), day, pointer, DateUtils.inDaysOfWeek(date1, day));
            }
        }
    }

    @Test
    public void inDaysOfMonth() {
        Date[] dates = new Date[5];
        for (int i = 0; i < dates.length; i++) {
            dates[i] = DateUtils.getRandomDate();
        }
        String formatter = "%-30s %-50s %-10s %s%n";
        String pointer = "->";
        System.out.printf(formatter, "Date", "Days", "", "inDaysOfWeek");
        String[] days = {"0,1,2,3,28", "-1,0,1,2,5,13", "8,9,10,17,19,25"};
        for (Date date1 : dates) {
            for (String day : days) {
                System.out.printf("%-30s %-50s %-10s %s%n", f0.format(date1), day, pointer, DateUtils.inDaysOfMonth(date1, day));
            }
        }
    }

    @Test
    public void calTest() {
        int birthDay = 2;
        int birthMon = 12;
        int birthYear = 1993;
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(birthYear, birthMon - 1, birthDay);
        System.out.println(cal.getTimeZone());
        System.out.println(cal.getTime());
    }

    @Test
    public void getIntervalInSeconds() {
//        System.out.println(DateUtils.getIntervalInSeconds(LocalTime.now().toString(), "19:00:00"));
//        System.out.println(LocalTime.now().until(LocalTime.parse("23:59:59"), ChronoUnit.SECONDS));
//        System.out.println(LocalTime.now().until(LocalTime.parse("00:00:00"), ChronoUnit.SECONDS));
        System.out.println(LocalDateTime.parse("2018-11-08T06:06:59").until(LocalDateTime.parse("2018-11-08T15:52:17"), ChronoUnit.MINUTES));
    }

    @Test
    public void g() {
        System.out.println(DateUtils.getSecondsFromNowToMidnight());
    }

    @Test
    public void getIntervalName() throws ParseException {
        System.out.println(DateUtils.getPointOfTimeName(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2018-11-02 19:40:00")));
    }

    @Test
    public void dateFormatTest() {
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").format(new Date()));
    }

    @Test
    public void isOverFromNow() throws ParseException {
        System.out.println(DateUtils.isOverFromNow(f0.parse("2018-08-21 19:00:00"), 8, DateUtils.TimeUnit.MINUTE));
    }

    @Test
    public void add() {
        System.out.println(DateUtils.add(new Date(), -500, DateUtils.TimeUnit.HOUR));
    }
}
