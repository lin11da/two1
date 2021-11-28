package com.two.pojo;

import lombok.Data;

@Data
public class ReturnThresholdtotal {
    private String userid;
    private String username;
    private String equipmentname;
    private String serialid;

    //报警参数
    private double illumination;
    private double temperature;
    private double humidity;
    private double co2;
    private double watertemp;
    private double conductivity;

    //报警次数
    private String illuminationtimes;
    private String temperaturetimes;
    private String humiditytimes;
    private String co2times;
    private String watertemptimes;
    private String conductivitytimes;

    private String illuminationcreatetime;
    private String temperaturecreatetime;
    private String humiditycreatetime;
    private String co2createtime;
    private String watertempcreatetime;
    private String conductivitycreatetime;


}
