package com.two.mapper;

import com.two.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Mapper
@Repository
public interface OProject {
    /**
     * 根据projectid查询是否存在信息
     *
     * @param projectid
     * @return
     */
    ProjectData getProjectInfo(String projectid);

    /**
     * 创建项目
     *
     * @param projectid
     * @param userid
     * @param projectname
     * @param projectleader
     * @param headofsafety
     * @param addressproject
     * @param updatetime
     * @param createtime
     * @return
     */
    Integer projectregistered(String projectid, String userid, String projectname, String projectleader, String headofsafety, String addressproject, String longitude, String latitude, String updatetime, String createtime);

    /**
     * 根据userid查询用户项目
     *
     * @param userid
     * @return
     */
    List<ProjectData> selectallproject(String userid);

    /**
     * 查看用户是否存在此设备
     *
     * @param userid
     * @param projectid
     * @return
     */
    ProjectData selectprojecton(String userid, String projectid);

    /**
     * 用户更新项目信息
     *
     * @param projectname
     * @param projectleader
     * @param headofsafety
     * @param addressproject
     * @param updatetime
     * @param projectid
     * @return
     */
    Integer updateprojectdata(String projectname, String projectleader, String headofsafety, String addressproject, String updatetime, String projectid, String longitude, String latitude);

    /**
     * 查看有多少个projectid绑定设备序列号，有多少个就是多少个设备在项目里
     *
     * @param projectid projectid
     * @return
     */
    Integer selectequipmenttatol(String projectid);

    /**
     * 设备插入到项目
     *
     * @param onlyid
     * @param projectid
     * @param serialid
     * @param updatetime
     * @param createtime
     * @return
     */
    Integer insertequipmenttoproject(String onlyid, String projectid, String serialid, String updatetime, String createtime);

    /**
     * 查询项目设备信息
     *
     * @param projectid
     * @return
     */
    List<ProjectData> selecteequipmentdata(String projectid);

    /**
     * 查询这个项目里是否有这个设备
     *
     * @param projectid
     * @param serialid
     * @return
     */
    Boolean selectprojectid(String projectid, String serialid);

    Boolean selectprojectinuserid(String userid, String projectid);

    /**
     * 查询设备数量
     *
     * @param serialid
     * @return
     */
    Integer selectetatol(String serialid);

    /**
     * 插入全部数据
     *
     * @param onlyid
     * @param userid
     * @param serialid
     * @param updatetime
     * @param createtime
     * @param haveread
     * @param temp
     * @param co2
     * @param humi
     * @param conductivity
     * @param watertemp
     * @param illuminance
     * @return
     */
    Integer insertalldatalog(String onlyid, String userid, String projectid, String serialid, double temp, double co2, double humi, double conductivity, double watertemp, double illuminance, int haveread, int status, String updatetime, String createtime);

    /**
     * 查询全部数据数量
     *
     * @param serialid
     * @return
     */
    Integer selectdatatatol(String serialid);


    Integer selectuserdatatatol(String userid);

    /**
     * 选择设备查询信息
     *
     * @param serialid
     * @param startIndex
     * @param pageSize
     * @return
     */
    List<AllDatalog> chooseselectalldatalog(String serialid, int startIndex, int pageSize);

    /**
     * 查询全部设备信息
     *
     * @param startIndex
     * @param pageSize
     * @return
     */
    List<AllDatalog> selectalldatalog(String userid,int startIndex, int pageSize);

    /**
     * 查询项目id
     *
     * @param serialid
     * @return
     */
    ProjectData selectprojectidtodata(String serialid);

    /**
     * 用projectid查找projectdata表
     *
     * @param projectid
     * @return
     */
    ProjectData selectprojectbyprojectid(String projectid);


    DoubleData selectonedaydataaverage(Map<String, String> map);

    List<ProjectData> selectdatalog(String starttime, String endtime);

    List<ProjectData> selectdataloguser(String userid, String starttime, String endtime);

    List<Sensorid> selectsenseridonbyuserid(String userid);

    List<Handle> selectuserhandle(String senserid);

    Integer updatesrialidstatus(String status, String updatetime, String serialid);

    Integer updateprojectstatus(String status, String updatetime, String projectid);

    /**
     * 删除项目
     *
     * @param updatetime
     * @param projectid
     * @return
     */
    Integer delectproject(String updatetime, String projectid);

    /**
     * 用户设备实时在线情况
     *
     * @param updatetime
     * @param serialid
     * @return
     */
    Integer serialonline(String updatetime, String serialid, String status);

    /**
     * 查询用户下的项目个数
     *
     * @param userid
     * @return
     */
    Integer projecttotal(String userid);

    /**
     * 删除阈值
     *
     * @param serialid
     * @param userid
     * @return
     */
    Integer delectthreshold(String serialid, String userid);
}
