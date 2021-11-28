package com.two.pojo;

import lombok.Data;

@Data
public class Querydata {
    private String deviceid;
    private String paramsname;
    private String paramsvalues;
    private String accepttime;

    public String getDeviceid() {
        return deviceid;
    }

    public String getParamsname() {
        return paramsname;
    }

    public String getParamsvalues() {
        return paramsvalues;
    }

    public String getAccepttime() {
        return accepttime;
    }
}
