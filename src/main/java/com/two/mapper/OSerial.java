package com.two.mapper;

import com.two.pojo.AllDatalog;
import com.two.pojo.Handle;
import com.two.pojo.Sensorid;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository  //代表持久层
@Mapper
public interface OSerial {
    Sensorid selectsenserid(String serialid);

    Integer selecthumialarm(String humisensorid);

    Integer selectwatertempalarm(String co2senserid);

    Integer selecttempalarm(String tempsensorid);

    Integer selectilluminancealarm(String illuminancesensorid);

    Integer selectconductivityalarm(String conductivitysensorid);

    Integer selectco2alarm(String co2senserid);

    /**
     * 删除设备后的操作
     *
     * @param serialid
     * @return
     */
    Integer deletesenserid(String serialid);

    /**
     * 报警信息最新时间 temp
     *
     * @param serialid
     * @return
     */
    AllDatalog alarmmaxtimetoco2time(String serialid);

    /**
     * 最新的报警数据 temp
     *
     * @param createtime
     * @return
     */
    AllDatalog alarmmaxtimetoco2(String createtime);

    /**
     * =======================================================================
     * <p>
     * 报警信息最新时间 co2
     *
     * @param serialid
     * @return
     */
    AllDatalog alarmmaxtimetotemptime(String serialid);


    /**
     * 最新的报警数据 co2
     *
     * @param createtime
     * @return
     */
    AllDatalog alarmmaxtimetotemp(String createtime);


    /**
     * =======================================================================
     * 最新的报警数据 humi
     *
     * @param createtime
     * @return
     */
    AllDatalog alarmmaxtimetohumi(String createtime);

    /**
     * 报警信息最新时间 humi
     *
     * @param serialid
     * @return
     */
    AllDatalog alarmmaxtimetohumitime(String serialid);


    /**
     * =======================================================================
     * 最新的报警数据 conductivity
     *
     * @param createtime
     * @return
     */
    AllDatalog alarmmaxtimetoconductivity(String createtime);

    /**
     * 报警信息最新时间 conductivity
     *
     * @param serialid
     * @return
     */
    AllDatalog alarmmaxtimetoconductivitytime(String serialid);


    /**
     * =======================================================================
     * 最新的报警数据 watertemp
     *
     * @param createtime
     * @return
     */
    AllDatalog alarmmaxtimetowatertemp(String createtime);

    /**
     * 报警信息最新时间 watertemp
     *
     * @param serialid
     * @return
     */
    AllDatalog alarmmaxtimetowatertemptime(String serialid);


    /**
     * =======================================================================
     * 最新的报警数据  illuminance
     *
     * @param createtime
     * @return
     */
    AllDatalog alarmmaxtimetoilluminance(String createtime);

    /**
     * 报警信息最新时间  illuminance
     *
     * @param serialid
     * @return
     */
    AllDatalog alarmmaxtimetoilluminancetime(String serialid);


    /**
     * ======+++++++++++++++++++++++++++++++++++++++++++++++++++++++++
     * 删除mysql
     */
    Integer delectco2alarm(String co2senserid);

    Integer delecthumidityalarm(String humisensorid);

    Integer delecttwoconductivityalarm(String conductivitysensorid);

    Integer delecttemperatureaalarm(String tempsensorid);

    Integer delecttwowatertempalarm(String watertempsensorid);

    Integer delectilluminationalarm(String illuminancesensorid);


    /**
     * 用户删除报警信息
     * ============================================================================
     */
    Integer delectuserco2senser(String co2senserid, String updatetime);

    Integer delectuserhumiditysenser(String humisensorid, String updatetime);

    Integer delectuserconductivitysenser(String conductivitysensorid, String updatetime);

    Integer delectusertemperatureasenser(String tempsensorid, String updatetime);

    Integer delectuserwatertempsenser(String watertempsensorid, String updatetime);

    Integer delectuserilluminationsenser(String illuminancesensorid, String updatetime);


    /**
     * ==================================================================================
     * 获取  data = 1 的最小时间数据-
     */

    AllDatalog alarmmaxtimetoco2timemin(String co2senserid, String starttime, String endtime);

    AllDatalog alarmmaxtimetotemptimemin(String tempsensorid, String starttime, String endtime);

    AllDatalog alarmmaxtimetohumitimemin(String humisensorid, String starttime, String endtime);

    AllDatalog alarmmaxtimetoconductivitytimemin(String conductivitysensorid, String starttime, String endtime);

    AllDatalog alarmmaxtimetowatertemptimemin(String watertempsensorid, String starttime, String endtime);

    AllDatalog alarmmaxtimetoilluminancetimemin(String illuminancesensorid, String starttime, String endtime);


    /**
     * =======================================================================================
     * 查找数据  准备复制
     */
    List<AllDatalog> selecttimebeteennowtominco2(String starttime, String endtime, String co2senserid);

    List<AllDatalog> selecttimebeteennowtominhumi(String starttime, String endtime, String humisensorid);

    List<AllDatalog> selecttimebeteennowtomintemp(String starttime, String endtime, String tempsensorid);

    List<AllDatalog> selecttimebeteennowtomintwoconductivity(String starttime, String endtime, String conductivitysensorid);

    List<AllDatalog> selecttimebeteennowtominwatertemp(String starttime, String endtime, String watertempsensorid);

    List<AllDatalog> selecttimebeteennowtominillumination(String starttime, String endtime, String illuminancesensorid);


    /**
     * =====================================================================================================
     * 插入处理的数据
     */

    Handle selecthandleonlyid(String senserid, String starttime);

    Integer inserthandle(String alarmonlyid,
                         String senserid,
                         String handlename,
                         int handleresult,
                         int total,
                         double data,
                         String starttime,
                         String endtime,
                         String updatetime,
                         String createtime);

    Integer updatehandle(int handleresult, String updatetime, String starttime);

    /**
     * =======================================================================================================================
     * 更改处理状态
     */

    Integer updatehandleco2(String onlyid, String updatetime, int haveread);

    Integer updatehandlehumi(String onlyid, String updatetime, int haveread);

    Integer updatehandlewtomintemp(String onlyid, String updatetime, int haveread);

    Integer updatehandleconductivity(String onlyid, String updatetime, int haveread);

    Integer updatehandlewatertemp(String onlyid, String updatetime, int haveread);

    Integer updatehandleillumination(String onlyid, String updatetime, int haveread);


    /**
     * 获取报警次数  user  根据时间段
     * ================================================================================================================================
     */
    Integer selectco2alarmtatol(String co2sensorid, String starttime, String endtime);

    Integer selecthumialarmtatol(String humisensorid, String starttime, String endtime);

    Integer selectwatertempalarmtatol(String watertempsensorid, String starttime, String endtime);

    Integer selecttempalarmtatol(String tempsensorid, String starttime, String endtime);

    Integer selectilluminancealarmtatol(String illuminancesensorid, String starttime, String endtime);

    Integer selectconductivityalarmtatol(String conductivitysensorid, String starttime, String endtime);

    /**
     * ================================================================================================================================
     */
    Sensorid selectserialidbysensoridco2(String sensor);

    Sensorid selectserialidbysensoridhumi(String sensor);

    Sensorid selectserialidbysensoridwatertemp(String sensor);

    Sensorid selectserialidbysensoridtemp(String sensor);

    Sensorid selectserialidbysensoridilluminance(String sensor);

    Sensorid selectserialidbysensoridconductivity(String sensor);


}
