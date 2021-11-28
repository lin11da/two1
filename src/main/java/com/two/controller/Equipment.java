package com.two.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.two.mapper.OEquipment;
import com.two.mapper.OProject;
import com.two.pojo.*;
import com.two.untils.Httputil;
import com.two.untils.Redisutils;
import com.two.untils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
public class Equipment {

    @Autowired
    private OEquipment oEquipment;

    @Autowired
    Redisutils redisutils;

    @Autowired
    OProject oProject;


    @PostMapping("/index")
    public void index(@ModelAttribute AutoDataVO autoDataVO) {

        //当前时间戳
        long updatetime = System.currentTimeMillis();
        long createtime = System.currentTimeMillis();

        if (autoDataVO.getProductkey() == null) {
            System.out.println("AutoDataVO  为 null");
        }


        System.out.println(autoDataVO.toString());

        try {
            String productkey = autoDataVO.getProductkey();


            //如果是Map<String，Obj>类型的话，直接对比返回也是错误的，也必须需要转类型数据
            Map<String, String> index = Utils.index(autoDataVO);

            if (index.get("CGSN") != null) {
                String cgsn = index.get("CGSN");
                String ccid = index.get("CCID");
                Integer updataccid = oEquipment.updataccid(productkey, cgsn, ccid, String.valueOf(updatetime));
                if (updataccid == 1) {
                    System.out.println("CCID + CGSN 修改成功");
                }
                System.out.println(cgsn + "  " + ccid);
                return;
            }
            String maptoJSONString = JSON.toJSONString(index);
            Rdata rdata = JSON.parseObject(maptoJSONString, Rdata.class);

            //存入redis
            boolean set = redisutils.set("key_" + productkey, rdata, 1);
            if (set) {
                System.out.println("实时数据放入 " + productkey + " redis");
            }

            String randomStr = Utils.RandomStr(18);
            //String转double  如果是0.75这样的话 保留两位小数  如果数据是  4.00 就是4.0
            Double temp = Double.valueOf(index.get("temp"));
            Double humi = Double.valueOf(index.get("humi"));
            Double illuminance = Double.valueOf(index.get("illuminance"));
            Double co2 = Double.valueOf(index.get("co2"));
            Double watertemp = Double.valueOf(index.get("watertemp"));
            Double conductivity = Double.valueOf(index.get("conductivity"));

            //根据序列号 productkey 查询数据库  拿到设备阈值  做比较，如果超过了就存入数据库
            Threshold threshold = oEquipment.selectthreshold(productkey);

            String userid = threshold.getUserid();

            //从数据库拿出设备阈值
            String co21 = threshold.getCo2();
            String humi1 = threshold.getHumidity();
            String illuminance1 = threshold.getIllumination();
            String conductivity1 = threshold.getConductivity();
            String temp1 = threshold.getTemperature();
            String watertemp1 = threshold.getWatertemp();

            //把数据库里的数据转化为double类型
            double mysqdlco2 = Double.parseDouble(co21);
            double mysqldhumi = Double.parseDouble(humi1);
            double mysqldilluminance = Double.parseDouble(illuminance1);
            double mysqldph = Double.parseDouble(conductivity1);
            double mysqldtemp = Double.parseDouble(temp1);
            double mysqldwatertemp = Double.parseDouble(watertemp1);

            ProjectData selectprojectid = oProject.selectprojectidtodata(productkey);
            String projectid = selectprojectid.getProjectid();

            //查询senserid
            Sensorid selectsenser = oEquipment.selectsenser(productkey);
            String co2sensorid = selectsenser.getCo2sensorid();
            String conductivitysensorid = selectsenser.getConductivitysensorid();
            String humisensorid = selectsenser.getHumisensorid();
            String tempsensorid = selectsenser.getTempsensorid();
            String watertempsensorid = selectsenser.getWatertempsensorid();
            String illuminancesensorid = selectsenser.getIlluminancesensorid();


            Integer insertalldatalog = oProject.insertalldatalog(randomStr, userid, projectid, productkey,
                    temp, co2, humi, conductivity, watertemp, illuminance, 1, 1, String.valueOf(updatetime), String.valueOf(createtime));
            if (insertalldatalog == 1) {
                System.out.println("记录插入成功");
            }

            //超出数据库的阈值后，把超过的插入到数据库
            if (mysqdlco2 <= co2) {
                Integer integer = oEquipment.insertuserequipmentco2(randomStr, userid, productkey, 1, co2, co2sensorid, String.valueOf(updatetime), String.valueOf(createtime));
                if (integer == 1) {
                    System.out.println(userid + "  co2  插入成功");
                }
            }
            if (mysqldhumi <= humi) {
                Integer insertuserequipmenthumi = oEquipment.insertuserequipmenthumi(randomStr, userid, productkey, 1, humi, humisensorid, String.valueOf(updatetime), String.valueOf(createtime));
                if (insertuserequipmenthumi == 1) {
                    System.out.println(userid + "  湿度  插入成功");
                }
            }
            if (mysqldilluminance <= illuminance) {
                Integer insertuserequipmentillumination = oEquipment.insertuserequipmentillumination(randomStr, userid, productkey, 1, illuminance, illuminancesensorid, String.valueOf(updatetime), String.valueOf(createtime));
                if (insertuserequipmentillumination == 1) {
                    System.out.println(userid + "  光照  插入成功");
                }
            }
            if (mysqldph <= conductivity) {
                Integer insertuserequipmentph = oEquipment.insertuserequipmentph(randomStr, userid, productkey, 1, conductivity, conductivitysensorid, String.valueOf(updatetime), String.valueOf(createtime));
                if (insertuserequipmentph == 1) {
                    System.out.println(userid + "  ph  插入成功");
                }
            }
            if (mysqldtemp <= temp) {
                Integer insertuserequipmenttemp = oEquipment.insertuserequipmenttemp(randomStr, userid, productkey, 1, temp, tempsensorid, String.valueOf(updatetime), String.valueOf(createtime));
                if (insertuserequipmenttemp == 1) {
                    System.out.println(userid + "  温度  插入成功");
                }
            }
            if (mysqldwatertemp <= watertemp) {
                Integer insertuserequipmentwatertemp = oEquipment.insertuserequipmentwatertemp(randomStr, userid, productkey, 1, watertemp, watertempsensorid, String.valueOf(updatetime), String.valueOf(createtime));
                if (insertuserequipmentwatertemp == 1) {
                    System.out.println(userid + "  水温  插入成功");
                }
            }
            Integer insertuserdatalog = oEquipment.insertuserdatalog(randomStr, userid, productkey, String.valueOf(updatetime), String.valueOf(createtime));
            if (insertuserdatalog == 1) {
                System.out.println("信息log插入成功");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

    }


}
