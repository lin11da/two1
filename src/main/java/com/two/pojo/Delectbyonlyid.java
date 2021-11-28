package com.two.pojo;

import lombok.Data;
import org.apache.tomcat.util.descriptor.web.SecurityRoleRef;

@Data
public class Delectbyonlyid {
    private String[] onlyid;
    private String[] senserid;
    private String[] alarmonlyid;
    private String time;
    private String handleman;
    private int haveread;
    private String starttime;
    private String endtime;
}
