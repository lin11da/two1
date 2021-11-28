package com.two.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReturnThreshold {
    private String illumination;
    private String temperature;
    private String humidity;
    private String co2;
    private String conductivity;
    private String watertemp;
}
