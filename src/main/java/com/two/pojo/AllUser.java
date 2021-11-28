package com.two.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.SimpleTypeRegistry;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AllUser {
    private String userid;
    private String username;
    private String usernumber;
    private String authority;
    private String data;
    private String createtime;
    private String quantity;
}
