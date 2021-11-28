package com.two.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.two.mapper.OProject;
import com.two.mapper.OUserm;
import com.two.pojo.*;
import com.two.untils.TimeData;
import com.two.untils.Userreturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Userservice {

    @Autowired
    OProject oProject;
    @Autowired
    OUserm oUserm;


    public List<JSONObject> equipmentlog(List<ProjectData> selectallproject, int startIndex, int pageSize) {
        //把信息装进list返回回去
        List<JSONObject> list = new ArrayList<>();
        for (ProjectData data : selectallproject) {
            Map<String, Object> map = new HashMap<>();
            String projectid = data.getProjectid();
            String projectname = data.getProjectname();
            List<ProjectData> selecteequipmentdata = oProject.selecteequipmentdata(projectid);
            for (ProjectData selecteequipmentdatum : selecteequipmentdata) {
                String serialid = selecteequipmentdatum.getSerialid();
                //用设备序列号进行查询信息  做了分页
                List<ProjectData> selectequipmentdatalog = oUserm.selectequipmentdatalog(serialid, startIndex, pageSize);
                for (ProjectData projectData : selectequipmentdatalog) {
                    Map<String, Object> datamap = new HashMap<>();
                    String onlyid = projectData.getOnlyid();
                    String createtime = projectData.getCreatetime();

                    //查询设备温湿度等。。。。。。
                    Rdata selectuserequipmentalarmlog = oUserm.selectuserequipmentalarmlog(onlyid);

                    //查询设备名称
                    AddAllequipment selectexist = oUserm.selectexist(serialid);
                    //设备拿到的各种信息
                    String equipmentname = selectexist.getEquipmentname();
                    String userid = selectexist.getUserid();
                    String co2 = selectuserequipmentalarmlog.getCo2();
                    String humi = selectuserequipmentalarmlog.getHumi();
                    String ph = selectuserequipmentalarmlog.getConductivity();
                    String watertemp = selectuserequipmentalarmlog.getWatertemp();
                    String illuminance = selectuserequipmentalarmlog.getIlluminance();
                    String temp = selectuserequipmentalarmlog.getTemp();

                    datamap.put("projectname", projectname);
                    datamap.put("userid", userid);
                    datamap.put("serialid", serialid);
                    datamap.put("onlyid", onlyid);
                    datamap.put("co2", co2);
                    datamap.put("equipmentname", equipmentname);
                    datamap.put("temp", temp);
                    datamap.put("humi", humi);
                    datamap.put("conductivity", ph);
                    datamap.put("watertemp", watertemp);
                    datamap.put("illuminance", illuminance);
                    datamap.put("time", createtime);

                    map.put("datalog", datamap);
                    //map转json
                    JSONObject jsonObject = new JSONObject(datamap);
                    list.add(jsonObject);

                }

            }

        }
        System.out.println(list);
        return list;
    }


    /**
     * alldatalog   String projectid,String serialid,int startIndex,int pageSize
     *
     * @param projectid
     * @param serialid
     * @param startIndex
     * @param pageSize
     * @return
     */
    public JSONObject choosealldataloglimit(String projectid, String serialid, int startIndex, int pageSize) {

        try {
            //查询项目名称
            ProjectData projectInfo = oProject.getProjectInfo(projectid);

            //查询设备名称
            AddAllequipment selectexist = oUserm.selectexist(serialid);
            Map<String, Object> prammap = new HashMap<>();
            //查询设备数据数量
            Integer selectdatatatol = oProject.selectdatatatol(serialid);
            prammap.put("total", selectdatatatol);
            List<Map<String, Object>> list = new ArrayList<>();
            List<AllDatalog> selectalldatalog = oProject.chooseselectalldatalog(serialid, startIndex, pageSize);
            for (AllDatalog allDatalog : selectalldatalog) {
                Map<String, Object> map = new HashMap<>();
                double conductivity = allDatalog.getConductivity();
                double illuminance = allDatalog.getIlluminance();
                double co2 = allDatalog.getCo2();
                String onlyid = allDatalog.getOnlyid();
                double temp = allDatalog.getTemp();
                double humi = allDatalog.getHumi();
                double watertemp = allDatalog.getWatertemp();
                String createtime = allDatalog.getCreatetime();
                String equipmentname = selectexist.getEquipmentname();
                String projectname = projectInfo.getProjectname();
                map.put("serialid", serialid);
                map.put("conductivity", conductivity);
                map.put("illuminance", illuminance);
                map.put("co2", co2);
                map.put("onlyid", onlyid);
                map.put("temp", temp);
                map.put("humi", humi);
                map.put("watertemp", watertemp);
                map.put("time", createtime);
                map.put("equipmentname", equipmentname);
                map.put("projectname", projectname);
                list.add(map);

            }
            prammap.put("log", list);
            JSONObject jsonObject = new JSONObject(prammap);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * alldataloglimit
     *
     * @param selectalldatalog
     * @return
     */
    public JSONObject alldataloglimit(List<AllDatalog> selectalldatalog,String userid) {
        try {
            Integer selectuserdatatatol = oProject.selectuserdatatatol(userid);
            List<Map<String, Object>> list = new ArrayList<>();
            Map<String, Object> prammap = new HashMap<>();
            prammap.put("total", selectuserdatatatol);

            for (AllDatalog allDatalog : selectalldatalog) {
                Map<String, Object> map = new HashMap<>();
                double conductivity = allDatalog.getConductivity();
                double illuminance = allDatalog.getIlluminance();
                double co2 = allDatalog.getCo2();
                String onlyid = allDatalog.getOnlyid();
                double temp = allDatalog.getTemp();
                double humi = allDatalog.getHumi();
                double watertemp = allDatalog.getWatertemp();
                String createtime = allDatalog.getCreatetime();
                String projectid = allDatalog.getProjectid();
                String serialid = allDatalog.getSerialid();

                //查询项目名称
                ProjectData projectInfo = oProject.getProjectInfo(projectid);
                String projectname = projectInfo.getProjectname();
                //查询设备名称
                AddAllequipment selectexist = oUserm.selectexist(serialid);
                String equipmentname = selectexist.getEquipmentname();

                map.put("serialid", serialid);
                map.put("conductivity", conductivity);
                map.put("illuminance", illuminance);
                map.put("co2", co2);
                map.put("onlyid", onlyid);
                map.put("temp", temp);
                map.put("humi", humi);
                map.put("userid",allDatalog.getUserid());
                map.put("watertemp", watertemp);
                map.put("time", createtime);
                map.put("equipmentname", equipmentname);
                map.put("projectname", projectname);
                list.add(map);
            }
            prammap.put("datalog", list);

            JSONObject jsonObject = new JSONObject(prammap);
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

//    public JSONObject alldataloglimit(List<AllDatalog> selectalldatalog,String userid) {
//        try {
//            Integer selectuserdatatatol = oProject.selectuserdatatatol(userid);
//            List<Map<String, Object>> list = new ArrayList<>();
//            Map<String, Object> prammap = new HashMap<>();
//            prammap.put("total", selectuserdatatatol);
//
//            for (AllDatalog allDatalog : selectalldatalog) {
//                Map<String, Object> map = new HashMap<>();
//                double conductivity = allDatalog.getConductivity();
//                double illuminance = allDatalog.getIlluminance();
//                double co2 = allDatalog.getCo2();
//                String onlyid = allDatalog.getOnlyid();
//                double temp = allDatalog.getTemp();
//                double humi = allDatalog.getHumi();
//                double watertemp = allDatalog.getWatertemp();
//                String createtime = allDatalog.getCreatetime();
//                String projectid = allDatalog.getProjectid();
//                String serialid = allDatalog.getSerialid();
//                //查询项目名称
//                ProjectData projectInfo = oProject.getProjectInfo(projectid);
//                String projectname = projectInfo.getProjectname();
//                //查询设备名称
//                AddAllequipment selectexist = oUserm.selectexist(serialid);
//                String equipmentname = selectexist.getEquipmentname();
//
//                map.put("serialid", serialid);
//                map.put("conductivity", conductivity);
//                map.put("illuminance", illuminance);
//                map.put("co2", co2);
//                map.put("onlyid", onlyid);
//                map.put("temp", temp);
//                map.put("humi", humi);
//
//                map.put("watertemp", watertemp);
//                map.put("createtime", createtime);
//                map.put("equipmentname", equipmentname);
//                map.put("projectname", projectname);
//                list.add(map);
//            }
//            prammap.put("log", list);
//
//            JSONObject jsonObject = new JSONObject(prammap);
//            return jsonObject;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    /**
     * selectaverage
     *
     * @param time
     * @param serialid
     * @return
     */
    public JSONObject selectaverage(String[] time, String serialid) {
        DecimalFormat df = new DecimalFormat("#0.00");

        Map<String, Object> parmmap = new HashMap<>();

        List<String> listco2 = new ArrayList<>();
        List<String> listhumi = new ArrayList<>();
        List<String> listconductivity = new ArrayList<>();
        List<String> listilluminance = new ArrayList<>();
        List<String> listwatertemp = new ArrayList<>();
        List<String> listtemp = new ArrayList<>();

        for (String ohtime : time) {
            String starttime = TimeData.StringToTimestamp(ohtime);
            long longstarttime = Long.valueOf(starttime);
            long endtime = longstarttime - 86400000;
            String stringendtime = String.valueOf(endtime);
            Map<String, String> map = new HashMap<>();
            map.put("serialid", serialid);
            map.put("starttime", starttime);
            map.put("endtime", stringendtime);

            DoubleData selectonedaydataaverage = oProject.selectonedaydataaverage(map);
            try {
                if (selectonedaydataaverage == null) {
                    String sco2 = "0.00";
                    String shumi = "0.00";
                    String sconductivity = "0.00";
                    String silluminance = "0.00";
                    String swatertemp = "0.00";
                    String stemp = "0.00";

                    listco2.add(sco2);
                    listhumi.add(shumi);
                    listconductivity.add(sconductivity);
                    listilluminance.add(silluminance);
                    listtemp.add(stemp);
                    listwatertemp.add(swatertemp);
                }
                double co2 = selectonedaydataaverage.getCo2();
                double humi = selectonedaydataaverage.getHumi();
                double conductivity = selectonedaydataaverage.getConductivity();
                double illuminance = selectonedaydataaverage.getIlluminance();
                double watertemp = selectonedaydataaverage.getWatertemp();
                double temp = selectonedaydataaverage.getTemp();

                //保留两位小数
                String sco2 = df.format(co2);
                String shumi = df.format(humi);
                String sconductivity = df.format(conductivity);
                String silluminance = df.format(illuminance);
                String swatertemp = df.format(watertemp);
                String stemp = df.format(temp);


                listco2.add(sco2);
                listhumi.add(shumi);
                listconductivity.add(sconductivity);
                listilluminance.add(silluminance);
                listtemp.add(stemp);
                listwatertemp.add(swatertemp);

                System.out.println("oneday:  " + sco2 + "==" + shumi + "==" + sconductivity + "==" + silluminance + "==" + swatertemp + "==" + stemp);
            } catch (Exception e) {
                System.out.println("oneday  报错" );
            }
        }
        parmmap.put("co2", listco2);
        parmmap.put("humi", listhumi);
        parmmap.put("conductivity", listconductivity);
        parmmap.put("illuminance", listilluminance);
        parmmap.put("watertemp", listwatertemp);
        parmmap.put("temp", listtemp);
        JSONObject jsonObject = new JSONObject(parmmap);
        return jsonObject;
    }

}
