package com.two.untils;

import com.alibaba.fastjson.JSONObject;
import com.two.pojo.AutoDataVO;
import com.two.pojo.Rdata;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

public class Utils {
    /**
     * 获取随机数
     *
     * @param length
     * @return
     */
    public static String RandomStr(int length) {
        String str = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(72);
            stringBuffer.append(str.charAt(number));
        }
        return stringBuffer.toString();
    }


    public static String timeStr(long s) {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt * 1000);
        res = simpleDateFormat.format(date);
        return res;
    }

    /**
     * 判断是不是数字
     *
     * @param number 手机号码
     * @return 是则返回true
     */
    public static Boolean pdsz(String number) {
        boolean isNum = number.matches("[0-9]+");
        return isNum;
    }

    /**
     * 当前时间戳
     *
     * @return 当前时间戳
     */
    public static String time() {
        //返回当前时间戳
        return String.valueOf(System.currentTimeMillis());
    }


    /**
     * 判断输入的是否是整数或者一位小数 或者两位小数点
     * String humidity,String temperature,String illumination
     *
     * @return
     */
    public static boolean pattern(String humidity, String temperature, String illumination, String co2, String watertemp, String conductivity) {
        boolean matches0 = Pattern.matches("^[+]?([0-9]+(.[0-9]{1,2})?)$", humidity);
        boolean matches1 = Pattern.matches("^[+]?([0-9]+(.[0-9]{1,2})?)$", temperature);
        boolean matches2 = Pattern.matches("^[+]?([0-9]+(.[0-9]{1,2})?)$", illumination);
        boolean matches3 = Pattern.matches("^[+]?([0-9]+(.[0-9]{1,2})?)$", co2);
        boolean matches4 = Pattern.matches("^[+]?([0-9]+(.[0-9]{1,2})?)$", watertemp);
        boolean matches5 = Pattern.matches("^[+]?([0-9]+(.[0-9]{1,2})?)$", conductivity);
        if (matches0 && matches1 && matches2 && matches3 && matches4 && matches5) {
            return true;
        } else {
            return false;
        }
    }


    //计算长度
    public static boolean datalong(String data, int datalong) {
        if (data.length() > datalong) {
            return false;
        } else {
            return true;
        }
    }

    //long 类型 10位时间戳转String类型
    public static String stringtime() {
        //当前时间戳
        long time = System.currentTimeMillis();

        return String.valueOf(time);
    }


    /**
     * 从实体类里拿出数据
     * AutoDataVO(productkey=n19wsEZnTMt, deviceid=g8NPHo, data={"paramstype0":{"Temp":"36","Humi":"111"}})
     *
     * @param autoDataVO
     * @return
     */
    public static Map<String, String> index(AutoDataVO autoDataVO) {

        Map<String, String> map = new HashMap<>();
        Map<String, String> doubleMap = new HashMap<>();
        String data = autoDataVO.getData();
        String serialid = autoDataVO.getProductkey();
        String deviceid = autoDataVO.getDeviceid();
        //将String类型 转化为json类型
        JSONObject json = JSONObject.parseObject(autoDataVO.getData());
        //从json类型里拿出数据
        String jsonString = json.getString("paramtype0");

        String stringtime = Utils.stringtime();
        if (jsonString.indexOf("CGSN") != -1) {
            String newjsonString = AllUtils.replaceA(jsonString);
            AutoDataVO jsontoautodata = AllUtils.CGSNjsontoList(newjsonString);
            String ccid = jsontoautodata.getCCID();
            String cgsn = jsontoautodata.getCGSN();
            map.put("CGSN", cgsn);
            map.put("CCID", ccid);
            return map;
        }


        Rdata rdata = AllUtils.jsontoList(jsonString);

        String c02 = rdata.getCo2();
        String humi = rdata.getHumi();
        String illuminance = rdata.getIlluminance();
        String conductivity = rdata.getConductivity();
        String watertemp = rdata.getWatertemp();
        String temp = rdata.getTemp();



        map.put("co2", c02);
        map.put("humi", humi);
        map.put("illuminance", illuminance);
        map.put("conductivity", conductivity);
        map.put("temp", temp);
        map.put("watertemp", watertemp);
        map.put("time", stringtime);

        System.out.println(c02 + " " + humi + " " + illuminance + " " + conductivity + " " + watertemp + " " + temp);

        return map;


    }


}
