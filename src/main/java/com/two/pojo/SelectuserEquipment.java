package com.two.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SelectuserEquipment {
    private String serialid;
    private String equipmentname;
    private String typesof;
    private String status;
    private String data;
    private String updatetime;
    private String createtime;
}
