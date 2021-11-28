package com.two.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class Rdata {
    private String temp;
    private String co2;
    private String humi;
    private String conductivity;
    private String watertemp;
    private String illuminance;
    private String time;
}
