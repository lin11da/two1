package com.two.service;

import com.alibaba.fastjson.JSONObject;
import com.two.controller.OUser;
import com.two.mapper.OProject;
import com.two.mapper.ORootm;
import com.two.mapper.OSerial;
import com.two.mapper.OUserm;
import com.two.pojo.*;
import com.two.untils.Userreturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Projectservice {
    @Autowired
    private OProject oProject;

    @Autowired
    OUserm oUserm;

    @Autowired
    ORootm oRootm;

    @Autowired
    OSerial oSerial;


    /**
     * selectallproject
     *
     * @param selectallproject
     * @return
     */
    public List<JSONObject> allproject(List<ProjectData> selectallproject) {
        List<JSONObject> list = new ArrayList<>();
        for (ProjectData data : selectallproject) {
            Map<String, Object> projectmap = new HashMap<>();
            Map<String, String> map = new HashMap<>();
            String projectid = data.getProjectid();
            String projectname = data.getProjectname();
            String projectleader = data.getProjectleader();
            String headofsafety = data.getHeadofsafety();
            String addressproject = data.getAddressproject();
            String createtime = data.getCreatetime();
            String status = data.getStatus();
            //经度
            String lng = data.getLongitude();
            //纬度
            String lat = data.getLatitude();

            map.put("projectname", projectname);
            map.put("projectleader", projectleader);
            map.put("headofsafety", headofsafety);
            map.put("addressproject", addressproject);
            map.put("createtime", createtime);
            //经温度
            map.put("lng", lng);
            map.put("lat", lat);
            //设备状态
            map.put("startstate", status);
            map.put("projectid", projectid);
            //查看有多少个projectid绑定设备序列号，有多少个就是多少个设备在项目里
            Integer selectequipmenttatol = oProject.selectequipmenttatol(projectid);
            projectmap.put("equipmenttatol", selectequipmenttatol);
            projectmap.put("project", map);

            JSONObject json = new JSONObject(projectmap);
            list.add(json);
        }
        return list;

    }


    public List<JSONObject> selectprojectuser(List<ProjectData> selectallproject) {
        try {
            List<JSONObject> list = new ArrayList<>();
            for (ProjectData data : selectallproject) {
                Map<String, Object> map = new HashMap<>();
                String projectname = data.getProjectname();
                String projectid = data.getProjectid();
                map.put("projectname", projectname);
                map.put("projectid", projectid);
                JSONObject json = new JSONObject(map);
                list.add(json);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * selectprojectdata
     *
     * @param selectallproject
     * @return
     */
    public Map<String, Object> selectprojectdata(List<ProjectData> selectallproject, int startIndex, int pageSize) {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Integer> map = new HashMap<>();
        Map<String, Object> endmap = new HashMap<>();
        int i = 0;
        for (ProjectData data : selectallproject) {
            String projectname = data.getProjectname();
            String projectid = data.getProjectid();


            //查询项目设备信息
            List<ProjectData> selecteequipmentdata = oProject.selecteequipmentdata(projectid);
            for (ProjectData projectData : selecteequipmentdata) {

                Map<String, Object> project2 = new HashMap<>();
                //项目名称和项目id
                project2.put("projectname", projectname);
                project2.put("projectid", projectid);
                String serialid = projectData.getSerialid();
                //查询项目设备信息
                List<AddAllequipment> selectequipmentdata = oUserm.selectequipmentdata(serialid, startIndex, pageSize);
                for (AddAllequipment selectequipmentdatum : selectequipmentdata) {
                    i++;

                    Map<String, Object> projectmap = new HashMap<>();
                    String equipmentname = selectequipmentdatum.getEquipmentname();
                    String cgsn = selectequipmentdatum.getCgsn();
                    String ccid = selectequipmentdatum.getCcid();
                    int typesof = selectequipmentdatum.getTypesof();
                    String status = selectequipmentdatum.getStatus();
                    String createtime = selectequipmentdatum.getCreatetime();

                    projectmap.put("serialid", serialid);
                    projectmap.put("equipmentname", equipmentname);
                    projectmap.put("cgsn", cgsn);
                    projectmap.put("imei", ccid);
                    projectmap.put("typesof", String.valueOf(typesof));
                    projectmap.put("status", status);
                    projectmap.put("createtime", createtime);
                    endmap.put("total", i);

                    project2.put("project", projectmap);

                    list.add(project2);
                    System.out.println(list);
                }
            }
        }

        endmap.put("list", list);
        return endmap;
    }


    public List<Map<String, Object>> chooseprojectdata(List<ProjectData> selectallproject) {

        List<Map<String, Object>> returndata = new ArrayList<>();
        for (ProjectData data : selectallproject) {
            Map<String, Object> endmap = new HashMap<>();
            List<Map<String, String>> list = new ArrayList<>();
            String projectid = data.getProjectid();
            String projectname = data.getProjectname();
            endmap.put("projectid", projectid);
            endmap.put("projectname", projectname);
            //查询项目设备信息
            List<ProjectData> selecteequipmentdata = oProject.selecteequipmentdata(projectid);
            for (ProjectData selecteequipmentdatum : selecteequipmentdata) {
                Map<String, Object> pramap = new HashMap<>();

                Map<String, String> map = new HashMap<>();
                String serialid = selecteequipmentdatum.getSerialid();
                AddAllequipment selectexist = oUserm.selectexist(serialid);
                String equipmentname = selectexist.getEquipmentname();
                //设备序列号和设备名称
                map.put("projectid", serialid);
                map.put("projectname", equipmentname);
                list.add(map);
                pramap.put("project", list);
            }
            endmap.put("endproject", list);
            returndata.add(endmap);

        }

        return returndata;
    }


    /**
     * selectprojectandserialid
     */
    public List<JSONObject> projectandserialidservice(List<ProjectData> selectequipmentdatalog, String serialid, String projectid) {
        try {
            List<JSONObject> list = new ArrayList<>();

            for (ProjectData data : selectequipmentdatalog) {
                Map<String, Object> map = new HashMap<>();
                String onlyid = data.getOnlyid();
                //查询设备xinxi
                Rdata selectuserequipmentalarmlog = oUserm.selectuserequipmentalarmlog(onlyid);
                String createtime = data.getCreatetime();
                //查询设备名称等
                AddAllequipment addAllequipment = oRootm.selectusereruipmentstatus(serialid);

                //查询项目名称
                ProjectData projectData = oProject.getProjectInfo(projectid);

                String projectname = projectData.getProjectname();

                String equipmentname = addAllequipment.getEquipmentname();

                String co2 = selectuserequipmentalarmlog.getCo2();
                String humi = selectuserequipmentalarmlog.getHumi();
                String conductivity = selectuserequipmentalarmlog.getConductivity();
                String watertemp = selectuserequipmentalarmlog.getWatertemp();
                String illuminance = selectuserequipmentalarmlog.getIlluminance();
                String temp = selectuserequipmentalarmlog.getTemp();
                map.put("time", createtime);
                map.put("serialid", serialid);
                map.put("co2", co2);
                map.put("humi", humi);
                map.put("conductivity", conductivity);
                map.put("watertemp", watertemp);
                map.put("illuminance", illuminance);
                map.put("temp", temp);
                map.put("projectname", projectname);
                map.put("equipmentname", equipmentname);
                JSONObject jsonObject = new JSONObject(map);
                list.add(jsonObject);

            }
            System.out.println(list);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("请选择正确参数");
            return null;
        }
    }

    /**
     * selectdatalog
     *
     * @param selectdatalog
     * @return
     */
    public List<Map> selectdatalog(List<ProjectData> selectdatalog) {
        List<Map> list = new ArrayList<>();
        for (ProjectData projectData : selectdatalog) {
            String serialid = projectData.getSerialid();


            //用serialid查询项目id
            ProjectData selectprojectidtodata = oProject.selectprojectidtodata(serialid);
            String projectid = selectprojectidtodata.getProjectid();

            ProjectData projectInfo = oProject.getProjectInfo(projectid);
            //项目名称
            String projectname = projectInfo.getProjectname();

            //设备名称
            AddAllequipment selectusereruipmentstatus = oRootm.selectusereruipmentstatus(serialid);
            String equipmentname = selectusereruipmentstatus.getEquipmentname();

            //拿出senserid
            Sensorid selectsenserid = oSerial.selectsenserid(serialid);

            String co2sensorid = selectsenserid.getCo2sensorid();
            String conductivitysensorid = selectsenserid.getConductivitysensorid();
            String illuminancesensorid = selectsenserid.getIlluminancesensorid();
            String tempsensorid = selectsenserid.getTempsensorid();
            String watertempsensorid = selectsenserid.getWatertempsensorid();
            String humisensorid = selectsenserid.getHumisensorid();

            try {
                //查看报警次数
                Integer co2alarm = oSerial.selectco2alarm(co2sensorid);
                if (co2alarm > 0) {

                    Map<String, Object> co2map = new HashMap<>();

                    //获取最新的时间
                    AllDatalog co2maxtime = oSerial.alarmmaxtimetoco2time(serialid);
                    String createtime = co2maxtime.getCreatetime();
                    //获取最大的值
                    AllDatalog co2data = oSerial.alarmmaxtimetoco2(createtime);
                    double co2 = co2data.getCo2();

                    co2map.put("equipmentname", equipmentname);
                    co2map.put("projectname", projectname);
                    co2map.put("serialid", serialid);
                    co2map.put("total", co2alarm);
                    co2map.put("alarmdata", "co2浓度过高");
                    co2map.put("information", co2);
                    co2map.put("time", createtime);
                    co2map.put("sensorid", co2sensorid);
                    co2map.put("haveread", 1);

                    list.add(co2map);
                }
            } catch (Exception e) {
                System.out.println("co2数据为null");
            }

            try {
                Integer conductivityalarm = oSerial.selectconductivityalarm(conductivitysensorid);
                if (conductivityalarm > 0) {
                    Map<String, Object> paramapconductivity = new HashMap<>();
                    Map<String, Object> conductivitymap = new HashMap<>();

                    //获取最新的时间 conductivityalarm
                    AllDatalog alarmmaxtimetoconductivitytime = oSerial.alarmmaxtimetoconductivitytime(serialid);
                    String createtime = alarmmaxtimetoconductivitytime.getCreatetime();
                    //获取最大的值 conductivityalarm
                    AllDatalog alarmmaxtimetoconductivity = oSerial.alarmmaxtimetoconductivity(createtime);
                    double conductivity = alarmmaxtimetoconductivity.getConductivity();


                    conductivitymap.put("equipmentname", equipmentname);
                    conductivitymap.put("projectname", projectname);
                    conductivitymap.put("serialid", serialid);
                    conductivitymap.put("total", conductivityalarm);
                    conductivitymap.put("alarmdata", "导电率过高");
                    conductivitymap.put("information", conductivity);
                    conductivitymap.put("time", createtime);
                    conductivitymap.put("sensorid", conductivitysensorid);
                    conductivitymap.put("haveread", 1);

                    list.add(conductivitymap);
                }
            } catch (Exception e) {
                System.out.println("导电率数据为null");
            }


            try {
                Integer tempalarm = oSerial.selecttempalarm(tempsensorid);
                if (tempalarm > 0) {
                    Map<String, Object> paramaptemp = new HashMap<>();
                    Map<String, Object> tempmap = new HashMap<>();

                    //获取最新的时间 tempalarm
                    AllDatalog tempmaxtime = oSerial.alarmmaxtimetotemptime(serialid);
                    String createtime = tempmaxtime.getCreatetime();
                    //获取最大的值 tempalarm
                    AllDatalog tempdata = oSerial.alarmmaxtimetotemp(createtime);
                    double temp = tempdata.getTemp();

                    tempmap.put("equipmentname", equipmentname);
                    tempmap.put("projectname", projectname);
                    tempmap.put("serialid", serialid);
                    tempmap.put("total", tempalarm);
                    tempmap.put("alarmdata", "温度过高");
                    tempmap.put("information", temp);
                    tempmap.put("time", createtime);
                    tempmap.put("sensorid", tempsensorid);
                    tempmap.put("haveread", 1);

                    list.add(tempmap);
                }
            } catch (Exception e) {
                System.out.println("温度数据为null");
            }


            try {
                Integer illuminancealarm = oSerial.selectilluminancealarm(illuminancesensorid);
                if (illuminancealarm > 0) {
                    Map<String, Object> paramapilluminance = new HashMap<>();
                    Map<String, Object> illuminancemap = new HashMap<>();
                    //获取最新的时间 illuminancealarm
                    AllDatalog alarmmaxtimetoilluminancetime = oSerial.alarmmaxtimetoilluminancetime(serialid);
                    String createtime = alarmmaxtimetoilluminancetime.getCreatetime();
                    //获取最大的值 illuminancealarm
                    AllDatalog alarmmaxtimetoilluminance = oSerial.alarmmaxtimetoilluminance(createtime);
                    double illuminance = alarmmaxtimetoilluminance.getIlluminance();

                    illuminancemap.put("equipmentname", equipmentname);
                    illuminancemap.put("projectname", projectname);
                    illuminancemap.put("serialid", serialid);
                    illuminancemap.put("total", illuminancealarm);
                    illuminancemap.put("alarmdata", "光照强度过高");
                    illuminancemap.put("information", illuminance);
                    illuminancemap.put("time", createtime);
                    illuminancemap.put("sensorid", illuminancesensorid);
                    illuminancemap.put("haveread", 1);
                    list.add(illuminancemap);
                }
            } catch (Exception e) {
                System.out.println("光照数据为null");
            }


            try {


                Integer humialarm = oSerial.selecthumialarm(humisensorid);
                if (humialarm > 0) {
                    Map<String, Object> paramaphumi = new HashMap<>();
                    Map<String, Object> humimap = new HashMap<>();
                    //获取最新的时间 humialarm
                    AllDatalog alarmmaxtimetohumitime = oSerial.alarmmaxtimetohumitime(serialid);
                    String createtime = alarmmaxtimetohumitime.getCreatetime();
                    //获取最大的值 humialarm
                    AllDatalog alarmmaxtimetohumi = oSerial.alarmmaxtimetohumi(createtime);
                    double humi = alarmmaxtimetohumi.getHumi();


                    humimap.put("equipmentname", equipmentname);
                    humimap.put("projectname", projectname);
                    humimap.put("serialid", serialid);
                    humimap.put("total", humialarm);
                    humimap.put("alarmdata", "湿度过高");
                    humimap.put("information", humi);
                    humimap.put("time", createtime);
                    humimap.put("sensorid", humisensorid);
                    humimap.put("haveread", 1);
                    list.add(humimap);
                }
            } catch (Exception e) {
                System.out.println("湿度数据为null");
            }


            try {
                Integer watertempalarm = oSerial.selectwatertempalarm(watertempsensorid);
                if (watertempalarm > 0) {
                    Map<String, Object> paramapwatertemp = new HashMap<>();
                    Map<String, Object> watertempmap = new HashMap<>();
                    //获取最新的时间 watertempalarm
                    AllDatalog alarmmaxtimetowatertemptime = oSerial.alarmmaxtimetowatertemptime(serialid);
                    String createtime = alarmmaxtimetowatertemptime.getCreatetime();
                    //获取最大的值 watertempalarm
                    AllDatalog alarmmaxtimetowatertemp = oSerial.alarmmaxtimetowatertemp(createtime);
                    double watertemp = alarmmaxtimetowatertemp.getWatertemp();


                    watertempmap.put("equipmentname", equipmentname);
                    watertempmap.put("projectname", projectname);
                    watertempmap.put("serialid", serialid);
                    watertempmap.put("total", watertempalarm);
                    watertempmap.put("alarmdata", "水温过高");
                    watertempmap.put("information", watertemp);
                    watertempmap.put("time", createtime);
                    watertempmap.put("sensorid", watertempsensorid);
                    watertempmap.put("haveread", 1);
                    list.add(watertempmap);
                }
            } catch (Exception e) {
                System.out.println("null");
            }
        }
        return list;
    }


}
