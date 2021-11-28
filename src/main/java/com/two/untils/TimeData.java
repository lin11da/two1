package com.two.untils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 10位时间戳
 */
public class TimeData {
    /**
     * String(yyyy-MM-dd HH:mm:ss) 转 Date
     *
     * @param time String
     * @return Date
     * @throws ParseException
     */
    // String date = "2010/05/04 12:34:23";
    public static Date StringToDate(String time) throws ParseException {

        Date date = new Date();
        // 注意format的格式要与日期String的格式相匹配
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = dateFormat.parse(time);
            System.out.println(date.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return date;
    }

    /**
     * Date转为String(yyyy-MM-dd HH:mm:ss:SSS)
     *
     * @param time
     * @return
     */
    public static String DateToString(Date time) {
        Date date = new Date();
        DateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        try {
            String dateStr = sf.format(date);
            return dateStr;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("转时间戳失败");
        }
        return null;
    }


    /**
     * String(yyyy-MM-dd HH:mm:ss:SSS)转13位时间戳
     *
     * @param time String
     * @return Integer
     */

    public static String StringToTimestamp(String time) {

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

        Date date = new Date();
        try {
            date = sf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("转时间戳失败");
        }
        System.out.println(date.getTime());
        return String.valueOf(date.getTime());
    }


    /**
     * 10位时间戳转Date
     *
     * @param time Integer
     * @return Date
     */
    public static Date TimestampToDate(Integer time) {
        long temp = (long) time * 1000;
        Timestamp ts = new Timestamp(temp);
        Date date = new Date();
        try {
            date = ts;
            //System.out.println(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * Date类型转换为10位时间戳
     *
     * @param time Date
     * @return Integer
     */
    public static Integer DateToTimestamp(Date time) {
        Timestamp ts = new Timestamp(time.getTime());

        return (int) ((ts.getTime()) / 1000);
    }

    /**
     * 10位int型的时间戳转换为String(yyyy-MM-dd HH:mm:ss)
     *
     * @param time Integer
     * @return String
     */
    public static String timestampToString(Integer time) {
        //int转long时，先进行转型再进行计算，否则会是计算结束后在转型
        long temp = (long) time * 1000;
        Timestamp ts = new Timestamp(temp);
        String tsStr = "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            //方法一
            tsStr = dateFormat.format(ts);
            System.out.println(tsStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tsStr;
    }

    /**
     * 时间戳转换成字符串
     */
    public static String getDateToString(long time) {
        Date d = new Date(time);
        DateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sf.format(d);
    }
}
