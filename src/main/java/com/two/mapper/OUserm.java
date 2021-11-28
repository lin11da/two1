package com.two.mapper;

import com.two.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository  //代表持久层
@Mapper
public interface OUserm {
    /**
     * 用户注册
     *
     * @param userid     userid
     * @param username   username
     * @param usernumber usernumber
     * @param password   password
     * @param updatetime updatetime
     * @param createtime createtime
     * @return
     */
    Integer Userregistered(String userid, String username, String usernumber, String password, String data, String authority, String updatetime, String createtime);

    /**
     * 根据userid查看数据库某个number的数据
     *
     * @param userid userid
     * @return User
     */
    User getUserInfo(String userid);

    /**
     * 根据usernumber查看数据库某个number的数据
     *
     * @param usernumber usernumber
     * @return User
     */
    User existuser(String usernumber);


    /**
     * 用户登录
     *
     * @param usernumber usernumber
     * @param password   password
     * @return User
     */
    User loginuser(String usernumber, String password);

    /**
     * 用户注销
     *
     * @param usernumber usernumber
     * @param data       data
     * @return Integer
     */
    Integer logout(String usernumber, String data);

    /**
     * 用户添加设备
     *
     * @param userid        userid
     * @param serialid      序列号
     * @param equipmentname 设备name
     * @param typesof       设备类型编号
     * @param data          是否被删除1为数可见
     * @param updatetime    updatetime
     * @param createtime    createtime
     * @return Integer
     */
    Integer useraddequipment(String userid, String serialid, String equipmentname, int typesof, String data, String updatetime, String createtime);

    /**
     * 查询该用户是否已注册设备
     *
     * @param userid   userid
     * @param serialid 序列号
     * @return AddAllequipment
     */
    AddAllequipment selectuserquipment(String userid, String serialid);

    /**
     * 查询该设备是否已经被注册
     *
     * @param serialid 序列号
     * @return AddAllequipment
     */
    AddAllequipment selectexist(String serialid);

    /**
     * 查找该用户设备数量
     *
     * @param userid userid
     * @return Integer
     */
    Integer selectuseridequipment(String userid);

    /**
     * 查看有多少个设备在线
     *
     * @param userid
     * @return
     */
    Integer countequipmentonline(String userid);

    /**
     * 未处理报警次数
     *
     * @param userid
     * @return
     */
    RIntdata alarmuntreatedtotal(String userid);

    /**
     * 已处理次数
     *
     * @param userid
     * @return
     */
    RIntdata alarmhavingtotal(String userid);

    /**
     * 用户修改密码
     *
     * @param usernumber usernumber
     * @param password   password
     * @return Integer
     */

    Integer updatauserpassword(String usernumber, String password);

    /**
     * 用户修改name
     *
     * @param usernumber usernumber
     * @param username   username
     * @return Integer
     */
    Integer updatauserdata(String usernumber, String username, String userid);

    /**
     * 插入温度数据
     *
     * @param userid      userid
     * @param serialid    设备序列号
     * @param typesof     设备类型
     * @param temperature 温度
     * @param updatetime  updatetime
     * @param createtime  createtime
     * @return
     */
    Integer inserttemperature(String userid, String serialid, String typesof, String temperature, String updatetime, String createtime);

    /**
     * 插入湿度数据
     *
     * @param userid     userid
     * @param serialid   设备序列号
     * @param typesof    设备类型
     * @param humidity   温度
     * @param updatetime updatetime
     * @param createtime createtime
     * @return
     */
    Integer inserthumidity(String userid, String serialid, String typesof, String humidity, String updatetime, String createtime);

    /**
     * 插入光照数据
     *
     * @param userid       userid
     * @param serialid     设备序列号
     * @param typesof      设备类型
     * @param illumination 温度
     * @param updatetime   updatetime
     * @param createtime   createtime
     * @return
     */
    Integer insertillumination(String userid, String serialid, String typesof, String illumination, String updatetime, String createtime);

    /**
     * 查询serialid （序列号）
     *
     * @param userid userid
     * @return Allequipment
     */
    List<Allequipment> selecuserid(String userid);

    /**
     * 查看用户下的设备
     *
     * @param userid
     * @return
     */
    List<AddAllequipment> alluserqeuipment(String userid);

    /**
     * 用户修改设备名称
     *
     * @param userid        userid
     * @param equipmentname equipmentname
     * @return Integer
     */
    Integer updatauserequipmentusername(String userid, String serialid, String equipmentname, String updatetime);

    /**
     * 用户设定阈值
     *
     * @param userid       userid
     * @param serialid     序列号
     * @param illumination 光照
     * @param temperature  温度
     * @param humidity     湿度
     * @param updatetime   updatetime
     * @param createtime   createtime
     * @return Integer
     */
    Integer insertuserEquipment(String userid, String serialid, String illumination, String temperature, String humidity, String co2,
                                String watertemp, String conductivity, String updatetime, String createtime);

    /**
     * 查找用户是否存在了设定设备的阈值
     *
     * @param userid
     * @param serialid
     * @return
     */
    Threshold existuserthreshold(String userid, String serialid);

    /**
     * 更新用户的设备阈值
     *
     * @param serialid     序列号
     * @param illumination 光照强度
     * @param temperature  温度
     * @param humidity     湿度
     * @param updatetime   更改时间
     * @return
     */
    Integer updateuserEquipmentthreshold(String serialid, String illumination, String temperature, String humidity, String co2, String watertemp, String conductivity, String updatetime);

    /**
     * 查询设备 历史记录
     *
     * @param userid   userid
     * @param serialid 序列号
     * @return List<Illumination>
     */
    List<Illumination> userillumination(String userid, String serialid);

    /**
     * 插入设备信息
     *
     * @param userid
     * @param ip
     * @param logintime
     * @param updatetime
     * @param createtime
     * @return
     */
    Integer insertloginlog(String userid, String ip, String logintime, String updatetime, String createtime);

    /**
     * 查询项目设备信息
     *
     * @param serialid
     * @return
     */
    List<AddAllequipment> selectequipmentdata(String serialid, int startIndex, int pageSize);

    /**
     * 用serialid查询 onlyid 和创建时间
     *
     * @param serialid
     * @return
     */
    List<ProjectData> selectequipmentdatalog(String serialid, int startIndex, int pageSize);

    /**
     * onlyid查询报警信息信息
     *
     * @param onlyid
     * @return
     */
    Rdata selectuserequipmentalarmlog(String onlyid);

    /**
     * 查询有多少条温湿度等。。。。。。数据
     *
     * @param userid
     * @return
     */
    Integer selectuseridequipmentlogtatol(String userid);

    /**
     * 删除设备记录
     *
     * @param onlyid
     * @return
     */
    Integer delectdatalogs(String onlyid);

    /**
     * 删除设备记录 2！！！！！！！！！！！！
     *
     * @param onlyid
     * @return
     */
    Integer delectdataalllogs(String onlyid);

    /**
     * 插入传感器id
     *
     * @return
     */
    Integer serialidsenser(String userid, String serialid, String co2sensorid, String tempsensorid, String humisensorid,
                           String conductivitysensorid, String watertempsensorid, String illuminancesensorid, String updatetime, String createtime);


    /**
     * 更新设备名称和设备类型
     *
     * @param equipmentname
     * @param typesof
     * @param serialid
     * @return
     */
    Integer updateprodata(String equipmentname, String typesof, String serialid);

    /**
     * 更新projectid
     *
     * @param projectid
     * @param oldprojectid
     * @return
     */
    Integer updateprojectid(String projectid, String oldprojectid);


    /**
     * 删除已处理数据  delect = 0
     *
     * @param alarmonlyid
     * @return
     */
    Integer delectprocessed(String alarmonlyid);

    /**
     * 删除设备
     *
     * @param projectid
     * @param serialid
     * @return
     */
    Integer delectuserequipment(String projectid, String serialid);

    Integer delectequipmenttouserequipment(String userid, String serialid);

    Integer delectdatalogsall(String serialid);

    /**
     * 查询用户信息
     *
     * @param userid
     * @return
     */
    User selectuserdata(String userid);

    /**
     * 获取用户传感器个数
     *
     * @param userid
     * @return
     */
    Integer selectuseridsensor(String userid);

    /**
     * 每个月未处理报警次数
     *
     * @param userid
     * @param starttime
     * @param endtime
     * @return
     */
    RIntdata alarmtotaltomonthbydatalog(String userid, String starttime, String endtime);

    /**
     * 查询每个月已处理次数
     *
     * @param userid
     * @param starttime
     * @param endtime
     * @return
     */
    RIntdata alarmhavingtotaltoMonth(String userid, String starttime, String endtime);

    /**
     * 管理员删除用户后删除设备
     */
    Integer delectequipment(String userid);
}
