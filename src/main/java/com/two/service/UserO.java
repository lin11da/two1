package com.two.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.two.mapper.OEquipment;
import com.two.mapper.OProject;
import com.two.mapper.OUserm;
import com.two.pojo.*;
import com.two.untils.Httputil;
import com.two.untils.Redisutils;
import com.two.untils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserO {

    @Autowired
    OUserm oUserm;

    @Autowired
    Redisutils redisutils;

    @Autowired
    OEquipment oEquipment;

    @Autowired
    OProject oProject;


    public Map<String, String> Httpequipmentdata(String usernumber) {

        //当前时间戳
        long updatatime = System.currentTimeMillis();
        long createtime = System.currentTimeMillis();
        Map<String, String> retrunmap = new IdentityHashMap<>();
        Map<String, String> objMap = new HashMap<>();
        Map<String, String> map = new HashMap<>();
        Map<String, String> queryDevice = new HashMap<>();

        try {
            //用number 查询出userid
            User existuser = oUserm.existuser(usernumber);
            String userid = existuser.getUserid();
            //用userid拿出serialid
            List<Allequipment> selecuserid = oUserm.selecuserid(userid);
            ObjectMapper mapper = new ObjectMapper();
            for (int x = 0; x < selecuserid.size(); x++) {

                Allequipment allequipment = selecuserid.get(x);
                String data1 = allequipment.getData();
                if (!"0".equals(data1)) {
                    String mysqlserialid = allequipment.getSerialid();
                    Allequipment equipmentserialidexist = oEquipment.equipmentserialidexist(mysqlserialid);
                    String equipmentid = equipmentserialidexist.getEquipmentid();
                    String typesof = equipmentserialidexist.getTypesof();
                    System.out.println(allequipment);


                    map.put("accountId", "4991f93cf8e2df8eb497975ad5988eae");
                    map.put("ProductKey", mysqlserialid);
                    map.put("DeviceID", equipmentid);
                    String sendxwwwform = Httputil.sendxwwwform("https://mqtt.nnhpiot.com/open/api/getTokenV1", map);

                    JSONObject json = JSONObject.parseObject(sendxwwwform);
                    //从返回数据中拿出data（json）
                    System.out.println(json.getString("data"));
                    String httpdata = json.getString("data");
                    if ("{\"errCode\":10001,\"errMsg\":\"产品信息不存在\"}".equals(httpdata)) {
                        retrunmap.put("error", "产品信息不存在");

                        System.out.println("++++++++++++++++");
                        return retrunmap;
                    }

                    queryDevice.put("token", httpdata);
                    queryDevice.put("DeviceID", equipmentid);
                    String data = Httputil.sendxwwwform("https://mqtt.nnhpiot.com/open/api/queryDeviceData", queryDevice);


                    QueryDeviceData queryDeviceData = mapper.readValue(data, QueryDeviceData.class);
                    List<Querydata> querydata = queryDeviceData.getData();
                    for (int i = 0; i < querydata.size(); i++) {
                        System.out.println(querydata.get(i));
                        Querydata querydataobj = querydata.get(i);
                        //假如angle为湿度
                        if ("angle".equals(querydataobj.getParamsname())) {
                            String angle = querydataobj.getParamsvalues();
                            Integer inserthumidity = oUserm.inserthumidity(userid, mysqlserialid, typesof, angle, String.valueOf(updatatime), String.valueOf(createtime));
                            if (inserthumidity == 1) {
                                retrunmap.put("angle", angle);
                                System.out.println(usernumber + " 的 " + mysqlserialid + "  的湿度添加成功 " + angle);
                            }
                        }
                        //假如humi为温度
                        if ("humi".equals(querydataobj.getParamsname())) {
                            String humi = querydataobj.getParamsvalues();
                            Integer inserttemperature = oUserm.inserttemperature(userid, mysqlserialid, typesof, humi, String.valueOf(updatatime), String.valueOf(createtime));
                            if (inserttemperature == 1) {
                                retrunmap.put("humi", humi);
                                System.out.println(usernumber + " 的 " + mysqlserialid + "  的温度添加成功 " + humi);
                            }
                        }
                        // //假如ming为光照
                        if ("ming".equals(querydataobj.getParamsname())) {
                            String ming = querydataobj.getParamsvalues();
                            Integer insertillumination = oUserm.insertillumination(userid, mysqlserialid, typesof, ming, String.valueOf(updatatime), String.valueOf(createtime));
                            if (insertillumination == 1) {
                                retrunmap.put("ming", ming);
                                System.out.println(usernumber + " 的 " + mysqlserialid + "  的光照添加成功 " + ming);
                            }
                        }
                    }
                    System.out.println(retrunmap + "=========================");
                    //转为json字符串；
                    String retrunmapjson = mapper.writeValueAsString(retrunmap);
                    objMap.put(mysqlserialid, retrunmapjson);
                    if (selecuserid.size() == x + 1) {
                        return objMap;
                    }
                }
                return objMap;
            }


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("error");
        }
        return null;
    }


    @Scheduled(cron = "0 0/8 * * * ? ")  //每8分钟执行一次
    @Async
    public void index1() {
        List<Allequipment> list = oEquipment.indexequipment();
        if (list == null) {
            return;
        } else {
            for (Allequipment allequipment : list) {
                Map<String, String> map = new HashMap<>();

                map.put("accountId", "6185ce4d86f1628ce2dc7540d5ef3406");
                map.put("productkey", allequipment.getSerialid());
                map.put("productsecret", allequipment.getSecretid());
                map.put("url", "http://y354c35221.eicp.vip/index");

                String sendxwwwform = Httputil.sendxwwwform("https://mqtt.nnhpiot.com/index/open/addAutoDataTransmission", map);
                System.out.println(sendxwwwform);
            }
        }
    }


    //查询设备是否在线
    @Scheduled(cron = "0/10 * * * * ? ")  //每10s钟执行一次
    @Async
    public void index2() {
        List<Allequipment> selectalluserequipment = oEquipment.selectalluserequipment();
        if (selectalluserequipment.size() == 0) {
            return;
        } else {
            for (Allequipment allequipment : selectalluserequipment) {
                String stringtime = Utils.stringtime();
                String serialid = allequipment.getSerialid();
                System.out.println(serialid);
                String equipmentid = null;
                try {
                    Allequipment selectuserequipmentid = oEquipment.selectuserequipmentid(serialid);
                    equipmentid = selectuserequipmentid.getEquipmentid();
                } catch (Exception e) {
                    System.out.println("设备列表不存在该设备");
                }

                Map<String, String> map = new HashMap<>();
                map.put("deviceid", equipmentid);
                String sendxwwwform = Httputil.sendxwwwform("https://mqtt.nnhpiot.com/open/api/getDeviceIdStatus", map);
                JSONObject json = JSONObject.parseObject(sendxwwwform);

                String jsonString = json.getString("data");
                oProject.serialonline(stringtime, serialid, jsonString);
                System.out.println(jsonString);

            }
            System.out.println("查询设备在线状态成功");
        }

    }

    @GetMapping("/ooo")
    public void index3() {
        List<Allequipment> list = oEquipment.indexequipment();
        if (list == null) {
            return;
        } else {
            for (Allequipment allequipment : list) {
                Map<String, String> map = new HashMap<>();
                map.put("accountId", "6185ce4d86f1628ce2dc7540d5ef3406");
                map.put("productkey", allequipment.getSerialid());
                map.put("productsecret", allequipment.getSecretid());
                map.put("url", "http://y354c35221.eicp.vip/index");

                String sendxwwwform = Httputil.sendxwwwform("https://mqtt.nnhpiot.com/index/open/addAutoDataTransmission", map);
                System.out.println(sendxwwwform);
            }
        }
    }


}
