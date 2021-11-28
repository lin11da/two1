package com.two.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.two.mapper.OConfiguration;
import com.two.pojo.Configuration;
import com.two.pojo.RootRedis;
import com.two.pojo.User;
import com.two.untils.JWTUtils;
import com.two.untils.Redisutils;
import com.two.untils.Userreturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;

@CrossOrigin
@RestController
public class SystemConfiguration {
    @Autowired
    Redisutils redisutils;

    @Autowired
    OConfiguration oConfiguration;


    String unknowreason = "操作失败";
    String unknowtoken = "Token传入错误";
    String errornotenull = "参数不能为空";
    String errornoknow = "未知错误";


    @PostMapping("/root/systemconfiguration")
    public Userreturn systemconfiguration(HttpServletRequest request, @RequestBody Configuration configuration) {

        //当前时间戳
        long updatatime = System.currentTimeMillis();
        long createtime = System.currentTimeMillis();
        try {
            String contact = configuration.getContact();
            String announcementoftheinformation = configuration.getAnnouncementoftheinformation();
            String contactaddress = configuration.getContactaddress();
            String sitetitle = configuration.getSitetitle();
            String websitedescription = configuration.getWebsitedescription();
            String websitekeywords = configuration.getWebsitekeywords();
            String websitelogo = configuration.getWebsitelogo();
            String websiteregistrationnumber = configuration.getWebsiteregistrationnumber();

            if ("".equals(configuration) || "".equals(announcementoftheinformation) || "".equals(contactaddress) || "".equals(sitetitle)
                    || "".equals(websitedescription) || "".equals(websitekeywords) || "".equals(websitelogo) || "".equals(websiteregistrationnumber)
                    || configuration == null || announcementoftheinformation == null || configuration == null || sitetitle == null || websitedescription == null
                    || websitekeywords == null || websitelogo == null || websiteregistrationnumber == null) {
                return new Userreturn<>(errornotenull);
            }

            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("2".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    Configuration infoconfiguration = oConfiguration.getInfoconfiguration();
                    if (infoconfiguration == null) {
                        Integer insertconfiguration = oConfiguration.insertconfiguration(sitetitle, websitedescription, websitekeywords, websitelogo, contact, contactaddress, websiteregistrationnumber, announcementoftheinformation, String.valueOf(updatatime), String.valueOf(createtime));
                        if (insertconfiguration == 1) {
                            return new Userreturn<>();
                        }
                    } else {
                        int id = infoconfiguration.getId();
                        Integer updateconfiguration = oConfiguration.updateconfiguration(id, sitetitle, websitedescription, websitekeywords,
                                websitelogo, contact, contactaddress, websiteregistrationnumber, announcementoftheinformation, String.valueOf(updatatime));
                        if (updateconfiguration == 1) {
                            return new Userreturn<>();
                        } else {
                            return new Userreturn<>(unknowreason);
                        }
                    }
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(errornoknow);
        }
        return new Userreturn<>(errornoknow);
    }


    @GetMapping("/selectconfiguration")
    public Userreturn selectconfiguration() {
        Configuration infoconfiguration = oConfiguration.getInfoconfiguration();
        infoconfiguration.setId(0);
        return new Userreturn<>(infoconfiguration);
    }
}
