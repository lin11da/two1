package com.two.mapper;

import com.two.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

@Repository  //代表持久层
@Mapper
public interface ORootm {

    /**
     * 根据usernumber查看数据库某个number的数据
     *
     * @param usernumber usernumber
     * @return User
     */
    Root existroot(String usernumber);

    /**
     * 添加超级管理员
     *
     * @param userid
     * @param username
     * @param usernumber
     * @param password
     * @param accoutid
     * @param authority
     * @param updatetime
     * @param createtime
     * @return
     */
    Integer addsuperroot(String userid, String username, String usernumber, String password, String accoutid, String authority, String updatetime, String createtime);


    /**
     * 管理员修改自己的号码还有名称
     *
     * @param usernumber
     * @param username
     * @return
     */
    Integer updatarootdata(String usernumber, String username, String userid);

    /**
     * 查看管理员信息 userid
     *
     * @param userid
     * @return
     */
    Root selectroot(String userid);

    /**
     * 删除超级管理员
     */
    Integer deletesuperroot(String usernumber);

    /**
     * 根据userid查看数据库某个number的数据
     *
     * @param userid userid
     * @return User
     */
    Root getRootInfo(String userid);

    /**
     * 管理员登录
     *
     * @param usernumber usernumber
     * @param password   password
     * @return Root
     */
    Root loginroot(String usernumber, String password);

    /**
     * serialid查询信息(序列号)
     *
     * @param serialid 序列号
     * @return AddAllequipment
     */
    AddAllequipment equipmentserialid(String serialid);

    /**
     * serialid查询信息 (equipmentid)
     *
     * @param equipmentid equipmentid
     * @return AddAllequipment
     */
    AddAllequipment equipmentidselect(String equipmentid);

    /**
     * 管理员添加设备
     *
     * @param equipmentid 设备id
     * @param secretid    秘钥
     * @param serialid    序列号
     * @param typesof     设备类型
     * @param updatetime  updatatime
     * @param createtime  createtime
     * @return AddAllequipment
     */
    Integer addequipment(String onlyid, String equipmentid, String secretid, String serialid, int typesof, String data, String updatetime, String createtime);

    /**
     * 最高权限管理员可以指定某一用户成为二级管理员
     *
     * @param userid     userid
     * @param username   username
     * @param usernumber usernumber
     * @param password   password
     * @param authority  authority
     * @param updatetime updatetime
     * @param createtime createtime
     * @return Integer
     */
    Integer updateusertoroot(String userid, String username, String usernumber, String password, String authority, String updatetime, String createtime);

    /**
     * 管理员修改密码
     *
     * @param usernumber usernumber
     * @param password   password
     * @return Integer
     */
    Integer updatarootpassword(String usernumber, String password);

    /**
     * 管理员修改用户名
     *
     * @param usernumber usernumber
     * @param username   username
     * @return Integer
     */
    Integer updatarootnumber(String usernumber, String username);

    /**
     * 查询所有用户
     *
     * @return
     */
    List<AllUser> selectAlluser(Map<String, Integer> paramMap);

    /**
     * @param userid
     * @return
     */
    List<SelectuserEquipment> selectUserEquipment(String userid);


    /**
     * 管理员修改用户的设备名 称
     *
     * @param userid        userid
     * @param serialid      序列号
     * @param equipmentname equipmentname
     * @param updatetime    updatetime
     * @return Integer
     */
    Integer rootupdateuserequipment(String userid, String serialid, String equipmentname, int typesof, String updatetime);

    /**
     * 查看某设备下的阈值
     *
     * @param serialid 序列号
     * @return Threshold
     */
    ReturnThreshold rootselectuserthreshold(String serialid);

    /**
     * 管理员修改用户阈值
     *
     * @param userid       userid
     * @param serialid     序列号
     * @param illumination 光照强度
     * @param temperature  温度
     * @param humidity     湿度
     * @param updatetime   updatetime
     * @return
     */
    Integer rootupdatauserthreshiold(String userid, String serialid, String illumination, String temperature, String humidity, String updatetime);

    /**
     * 管理员修改用户全部信息
     *
     * @param userid
     * @param username
     * @param usernumber
     * @param password
     * @param updatetime
     * @return
     */
    Integer rootupdatauserinformation(String userid, String username, String usernumber, String password, String updatetime);


    /**
     * 查看这个设备信息是否被删除
     *
     * @param userid   userid
     * @param serialid 序列号
     * @return Threshold
     */

    SelectuserEquipment serialidanduseridselect(String userid, String serialid);

    /**
     * 查询有设备号的全部设备
     *
     * @return
     */
    List<SelectAllequipment> Allequipemnt(Map<String, Integer> paramMap);

    /**
     * 管理员删除设备
     *
     * @param onlyid
     * @return
     */
    Integer delectequipemnt(String onlyid);

    /**
     * 查询数据总数
     *
     * @return
     */
    Integer selectallequipment();

    /**
     * 管理员更新设备数据
     *
     * @param onlyid
     * @param equipmentid
     * @param secretid
     * @param serialid
     * @param typesof
     * @param updatetime
     * @return
     */
    Integer updateequipment(String onlyid, String equipmentid, String secretid, String serialid, String typesof, String updatetime);


    List<Root> selectAllroot(Map<String, Integer> paramMap);

    /**
     * 查询管理员总数
     *
     * @return
     */
    Integer selectAllrootpags();

    /**
     * 删除管理元
     *
     * @param userid
     * @return
     */
    Integer delectroot(String userid);

    /**
     * 查询用户总数  data=1
     *
     * @return
     */
    Integer selectusertotal();

    /**
     * 查询每个用户下有多少个设备
     *
     * @param userid userid
     * @return
     */
    Integer selectuserhaveequipment(String userid);

    /**
     * 查询用户日志
     *
     * @param userid userid
     * @return List<Userloginlog>
     */
    List<Userloginlog> userloginlog(String userid, int startIndex, int pageSize);

    /**
     * 查询该用户的登录次数
     *
     * @param userid userid
     * @return Integer
     */
    Integer userloginlogtotal(String userid);

    /**
     * 管理员删除用户
     *
     * @param userid
     * @return
     */
    Integer deleteuser(String userid);


    /**
     * 管理员删除用户的设备 物理删除
     *
     * @param userid
     * @param serialid
     * @return
     */
    Integer delectuserequipemnt(String userid, String serialid);

    /**
     * 查询用户 usernumber,username,password 信息
     *
     * @param userid
     * @return
     */
    User selectuserdatauserid(String userid);

    /**
     * 查询报警次数
     */
    ReturnThresholdtotal selectpolicefrequency(String serialid, String starttime, String endtime);


    /**
     * 查询某个时间的最大的时间戳，也就是最新的报警信息
     *
     * @param serialid
     * @param starttime
     * @param endtime
     * @return
     */
    Alarmtime selectpolicealarmtime(String serialid, String starttime, String endtime);

    /**
     * 查看有多少个已注册设备
     *
     * @param starttime
     * @param endtime
     * @return
     */
    Integer userequipmenttotal(String starttime, String endtime);

    /**
     * 查询报警的最新数据
     *
     * @param serialid
     * @param illuminationcreatetime
     * @param temperaturecreatetime
     * @param humiditycreatetime
     * @param co2createtime
     * @param watertempcreatetime
     * @param conductivitycreatetime
     * @return
     */
    ReturnThresholdtotal selectalarmdata(String serialid, String illuminationcreatetime, String temperaturecreatetime,
                                         String humiditycreatetime, String co2createtime, String watertempcreatetime, String conductivitycreatetime);

    /**
     * 查询设备状态
     *
     * @param serialid
     * @return
     */
    AddAllequipment selectusereruipmentstatus(String serialid);

    ProjectData selectdatalog(String serialid);

    /**
     * 删除projectdata的设备
     */
    Integer delectuserequipemnttoproject(String serialid);

    Integer disableequipment(String updatetime, String serialid, int disable);

    RIntdata selectalarmtotal(String serialid);

}
