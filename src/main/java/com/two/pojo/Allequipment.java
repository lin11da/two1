package com.two.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class Allequipment {
    private String userid;
    private String equipmentid;
    private String secretid;
    private String serialid;
    private String typesof;
    private String equipmentname;

    private String starttime;

    private String endtime;

    private String updatetime;
    private String createtime;
    private int startIndex;
    private int pageSize;
    private int pages;
    private String onlyid;
    private String data;
    private String projectname;
    private String projectid;
    private String oldprojectid;
    private String disable;

}
