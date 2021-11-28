package com.two.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.two.mapper.OProject;
import com.two.mapper.ORootm;
import com.two.mapper.OUserm;
import com.two.pojo.*;
import com.two.service.Projectservice;
import com.two.untils.JWTUtils;
import com.two.untils.Redisutils;
import com.two.untils.Userreturn;
import com.two.untils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@CrossOrigin
@RestController
public class Project {
    @Autowired
    OProject oProject;

    @Autowired
    ORootm oRootm;

    @Autowired
    Redisutils redisutils;

    @Autowired
    Projectservice projectservice;

    @Autowired
    OUserm oUserm;
    /**
     * 用户权限
     */
    String userrole = "0";

    String noknowerror = "未知错误";
    String unknowtoken = "Token传入错误";
    String errornotedatamis = "数据异常";

    /**
     * token失效
     */
    String errornotetoken = "账号登录信息已过期，请重新登录";

    /**
     * 用户创建项目
     *
     * @param request
     * @param projectData
     * @return
     */
    @PostMapping("/user/createproject")
    public Userreturn newproject(HttpServletRequest request, @RequestBody ProjectData projectData) {
        String addressproject = projectData.getAddressproject();
        String projectleader = projectData.getProjectleader();
        String projectname = projectData.getProjectname();
        String headofsafety = projectData.getHeadofsafety();
        //经度
        String lng = projectData.getLongitude();
        //纬度
        String lat = projectData.getLatitude();


        //创建当前时间
        String createtime = Utils.stringtime();
        //16位随机数
        String randomStr = Utils.RandomStr(16);

        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            String usernumber = tokenInfo.get("usernumber");
            //用电话号码查询userid
            User existuser = oUserm.existuser(usernumber);
            String userid = existuser.getUserid();

            //查看是否有权限
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {

                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    //查询随机生成的projectid是否存在
                    ProjectData projectInfo = oProject.getProjectInfo(randomStr);
                    if (projectInfo != null) {
                        randomStr = Utils.RandomStr(16);
                    }
                    //如果不存在则创建项目
                    Integer projectregistered = oProject.projectregistered(randomStr, userid, projectname, projectleader, headofsafety, addressproject, lng, lat, createtime, createtime);
                    if (projectregistered == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>("请检查信息");
                    }
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(noknowerror);
        }
    }

    /**
     * 用户的项目信息
     *
     * @param request
     * @return
     */
    @GetMapping("/user/selectproject")
    public Userreturn selectallproject(HttpServletRequest request) {

        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            String usernumber = tokenInfo.get("usernumber");
            //用电话号码查询userid
            User existuser = oUserm.existuser(usernumber);
            String userid = existuser.getUserid();

            //查看是否有权限
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {


                    List<ProjectData> selectallproject = oProject.selectallproject(userid);

                    List<JSONObject> allproject = projectservice.allproject(selectallproject);

                    return new Userreturn<>(allproject);
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(noknowerror);
        }
    }

    /**
     * 用户选择项目
     *
     * @param request
     * @return
     */
    @GetMapping("/user/chooseproject")
    public Object chooseprojectdata(HttpServletRequest request) {
        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            String usernumber = tokenInfo.get("usernumber");
            //用电话号码查询userid
            User existuser = oUserm.existuser(usernumber);
            String userid = existuser.getUserid();

            //查看是否有权限
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    List<ProjectData> selectallproject = oProject.selectallproject(userid);
                    List<Map<String, Object>> chooseprojectdata = projectservice.chooseprojectdata(selectallproject);
                    return new Userreturn<>(chooseprojectdata);
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(noknowerror);
        }
    }

    /**
     * 选择设备查看信息
     *
     * @param request
     * @param addAllequipment
     * @return
     */
    @PostMapping("user/chooseserialid")
    public Userreturn selectprojectandserialid(HttpServletRequest request, @RequestBody AddAllequipment addAllequipment) {
        String projectid = addAllequipment.getProjectid();
        String serialid = addAllequipment.getSerialid();
        int pages = addAllequipment.getPages();
        try {
            int startIndex = 8 * (pages - 1);
            int pageSize = 8;

            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            String usernumber = tokenInfo.get("usernumber");
            //用电话号码查询userid
            User existuser = oUserm.existuser(usernumber);
            String userid = existuser.getUserid();
            Map<String, Object> map = new HashMap<>();
            //查看是否有权限
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {

                    Boolean selectprojectinuserid = oProject.selectprojectinuserid(userid, projectid);
                    Boolean selectprojectid = oProject.selectprojectid(projectid, serialid);
                    if (!selectprojectinuserid.equals(selectprojectid)) {
                        return new Userreturn<>("数据错误");
                    }
                    Integer selectetatol = oProject.selectetatol(serialid);
                    List<ProjectData> selectequipmentdatalog = oUserm.selectequipmentdatalog(serialid, startIndex, pageSize);
                    List<JSONObject> projectandserialidservice = projectservice.projectandserialidservice(selectequipmentdatalog, serialid, projectid);
                    map.put("total", selectetatol);
                    map.put("list", projectandserialidservice);
                    return new Userreturn<>(map);
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(noknowerror);
        }

    }


    @PostMapping("user/delectldata")
    public Userreturn deletedatalogs(HttpServletRequest request, @RequestBody Delectbyonlyid delectbyonlyid) {
        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            String[] onlyid = delectbyonlyid.getOnlyid();
            //查看是否有权限
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    int i = 0;
                    for (String delectid : onlyid) {
                        Integer delectdatalogs = oUserm.delectdataalllogs(delectid);
                        if (delectdatalogs == 1) {
                            i++;
                            System.out.println(delectid + "  删除成功");
                            if (onlyid.length == i) {
                                System.out.println("全部删除成功");
                                return new Userreturn<>();
                            }
                        } else {
                            return new Userreturn<>(errornotedatamis);
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
            return new Userreturn<>(noknowerror);
        }
        return new Userreturn<>(errornotedatamis);
    }

    /**
     * delect设备记录  2！！！！！！！！！！！！！！！！！！！！！！！！！！
     *
     * @param request
     * @param delectbyonlyid
     * @return
     */

    @PostMapping("/user/delectlalldata")
    public Userreturn delectdataloglist(HttpServletRequest request, @RequestBody Delectbyonlyid delectbyonlyid) {

        try {
            String[] onlyid = delectbyonlyid.getOnlyid();
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    int i = 0;
                    for (String delectid : onlyid) {
                        Integer delectdatalogs = oUserm.delectdataalllogs(delectid);
                        if (delectdatalogs == 1) {
                            i++;
                            System.out.println(delectid + "  删除成功");
                            if (onlyid.length == i) {
                                System.out.println("全部删除成功");
                                return new Userreturn<>();
                            }
                        } else {
                            return new Userreturn<>(errornotedatamis);
                        }
                    }
                    return new Userreturn<>(noknowerror);
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(noknowerror);
        }
    }

}
