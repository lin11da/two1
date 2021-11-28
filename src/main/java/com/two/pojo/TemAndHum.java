package com.two.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemAndHum {
    private String userid;
    private String serialid;

    private double temperature;
    private double humidity;
    private double illumination;
    private String co2;
    private String watertemp;
    private String conductivity;

    private String updatetime;
    private String createtime;
}
