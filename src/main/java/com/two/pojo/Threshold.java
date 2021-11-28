package com.two.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Threshold {
    private String userid;
    private String serialid;
    private String illumination;
    private String temperature;
    private String humidity;
    private String co2;
    private String watertemp;
    private String conductivity;
    private String updatetime;
    private String createtime;
}
