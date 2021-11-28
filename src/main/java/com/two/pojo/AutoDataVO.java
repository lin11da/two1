package com.two.pojo;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = false)
public class AutoDataVO {
    private String productkey;
    private String deviceid;
    private String data;
    private String CGSN;
    private String CCID;
}
