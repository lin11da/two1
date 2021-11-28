package com.two.mapper;

import com.two.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface OEquipment {
    /**
     * allequipment   serialid查询信息(序列号)
     *
     * @param serialid 序列号
     * @return Allequipment
     */
    Allequipment equipmentserialidexist(String serialid);

    /**
     * @param typesof 设备类型
     * @return Typesofname
     */
    Typesofname equipmenttypesof(int typesof);

    /**
     * 遍历数据库，一次性请求全部已经注册的设备
     *
     * @return List<Allequipment>
     */
    List<Allequipment> indexequipment();

    /**
     * 查询全部用户已注册设备  disable = 1
     *
     * @return
     */
    List<Allequipment> selectalluserequipment();

    Allequipment selectuserequipmentid(String serialid);

    /**
     * 查询被注册后的全部设备
     *
     * @param paramMap map
     * @return User
     */
    User selecttoserialid(Map<String, String> paramMap);

    /**
     * 查询每个设备的设置的阈值
     *
     * @param serialid serialid
     * @return Selectthreshold
     */
    Threshold selectthreshold(String serialid);

    /**
     * 更新ccid
     *
     * @param serialid
     * @param cgsn
     * @param ccid
     * @param updatetime
     * @return
     */
    Integer updataccid(String serialid, String cgsn, String ccid, String updatetime);

    /**
     * 插入湿度
     *
     * @param userid     userid
     * @param serialid   serialid
     * @param typesof    typesof
     * @param humidity   humidity
     * @param updatetime updatetime
     * @param createtime createtime
     *                   //     * @return Integer
     *                   //
     */
    Integer insertuserequipmenthumi(String onlyid, String userid, String serialid, int typesof, double humidity, String humiditysenserid, String updatetime, String createtime);

    /**
     * 插入温度
     *
     * @param userid      userid
     * @param serialid    serialid
     * @param typesof     typesof
     * @param temperature temperature
     * @param updatetime  updatetime
     * @param createtime  createtime
     * @return Integer
     */
    Integer insertuserequipmenttemp(String onlyid, String userid, String serialid, int typesof, double temperature, String temperaturesenserid, String updatetime, String createtime);

    /**
     * 插入co2
     *
     * @param userid
     * @param serialid
     * @param typesof
     * @param co2
     * @param updatetime
     * @param createtime
     * @return
     */
    Integer insertuserequipmentco2(String onlyid, String userid, String serialid, int typesof, double co2, String co2senserid, String updatetime, String createtime);

    /**
     * 插入光照
     *
     * @param userid
     * @param serialid
     * @param typesof
     * @param illumination
     * @param updatetime
     * @param createtime
     * @return
     */
    Integer insertuserequipmentillumination(String onlyid, String userid, String serialid, int typesof, double illumination, String illuminationsenserid, String updatetime, String createtime);

    /**
     * 插入ph
     *
     * @param userid
     * @param serialid
     * @param typesof
     * @param conductivity
     * @param updatetime
     * @param createtime
     * @return
     */
    Integer insertuserequipmentph(String onlyid, String userid, String serialid, int typesof, double conductivity, String conductivitysenserid, String updatetime, String createtime);

    /**
     * 插入水温
     *
     * @param userid
     * @param serialid
     * @param typesof
     * @param watertemp
     * @param updatetime
     * @param createtime
     * @return
     */
    Integer insertuserequipmentwatertemp(String onlyid, String userid, String serialid, int typesof, double watertemp, String watertempsenserid, String updatetime, String createtime);

    /**
     * 插入设备信息记录
     *
     * @param onlyid
     * @param userid
     * @param serialid
     * @param updatetime
     * @param createtime
     * @return
     */
    Integer insertuserdatalog(String onlyid, String userid, String serialid, String updatetime, String createtime);

    Sensorid selectsenser(String serialid);
}
