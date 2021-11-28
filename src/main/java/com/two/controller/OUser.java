package com.two.controller;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.two.mapper.*;
import com.two.pojo.*;
import com.two.service.Projectservice;
import com.two.service.UserO;
import com.two.service.Userservice;
import com.two.untils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@CrossOrigin
@RestController
public class OUser {

    @Autowired
    OUserm oUserm;

    @Autowired
    Redisutils redisutils;

    @Autowired
    OEquipment oEquipment;

    @Autowired
    UserO userO;

    @Autowired
    Userservice userservice;

    @Autowired
    OProject oProject;

    @Autowired
    Projectservice projectservice;

    @Autowired
    ORootm oRootm;

    @Autowired
    OSerial oSerial;


    /**
     * 用户注册
     *
     * @param user json
     * @return msg
     */
    @PostMapping("/user/adduser")
    public Userreturn userregistered(@RequestBody User user) {
        //当前时间戳
        long updatatime = System.currentTimeMillis();
        long createtime = System.currentTimeMillis();
        String stringupdatatime = String.valueOf(updatatime);
        String stringcreatetime = String.valueOf(createtime);
        //权限为0，用户权限
        String authority = "0";
        //表示用户数据为注册
        String data = "1";

        try {
            String username = user.getUsername();
            String usernumber = user.getUsernumber();
            String password = user.getPassword();
            boolean usernamelong = Utils.datalong(username, 8);
            boolean usernumberlong = Utils.datalong(usernumber, 11);
            boolean passwordlong = Utils.datalong(password, 16);
            if (!passwordlong || !usernamelong || !usernumberlong) {
                return new Userreturn<>("格式数据不符");
            }
            String userid = Utils.RandomStr(16);
            //判断电话号码是否是数字
            Boolean pdsz = Utils.pdsz(usernumber);
            if (pdsz == false || usernumber.length() != 11) {
                return new Userreturn<>(errornotenumberdll);
            }

            if (username == null || username == "") {
                username = Utils.RandomStr(8);
            }

            User userInfo = oUserm.existuser(usernumber);

            if (userInfo != null) {
                return new Userreturn<>(errornotexistuser);
            }
            Integer userregistered = oUserm.Userregistered(userid, username, usernumber, password, data, authority, stringupdatatime, stringcreatetime);
            if (userregistered == 1) {
                return new Userreturn<>();
            } else {
                return new Userreturn<>(noknowerror);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(dataerror);
        }
    }

    /**
     * 未完成
     * 用户登录
     *
     * @param user json
     * @return msg
     */
    @PostMapping("/user/login")
    public Userreturn login(HttpServletRequest request, @RequestBody User user) {

        //当前时间戳
        long logintime = System.currentTimeMillis();
        long updatatime = System.currentTimeMillis();
        long createtime = System.currentTimeMillis();

        String usernumber = user.getUsernumber();
        String password = user.getPassword();
        Map<String, Object> map = new IdentityHashMap<>();
        UserRedis userRedis = new UserRedis();
        User existuser = oUserm.loginuser(usernumber, password);
        if (existuser == null) {
            return new Userreturn<>(erroraccountpassword);
        } else if ("0".equals(existuser.getData())) {
            return new Userreturn<>(errordata);
        }
        String userid = existuser.getUserid();
        String role = existuser.getAuthority();
        String username = existuser.getUsername();

        //执行登录方法，如果没有异常就可以了
        try {
            //获取ip
            String ip = Ipuutil.getIpAddr(request);

            //查找该用户设备数量
            Integer selectuseridequipment = oUserm.selectuseridequipment(userid);
            //添加登录日志
            Integer insertloginlog = oUserm.insertloginlog(userid, ip, String.valueOf(logintime), String.valueOf(updatatime), String.valueOf(createtime));
            if (insertloginlog == 1) {
                System.out.println(usernumber + " 已登录===登录日志正常记录");
            } else {
                System.out.println("登录日志记录异常");
            }

            //创建JWT令牌
            String usertoken = JWTUtils.getToken(usernumber, password, role);

            String rediskey = "root_" + usernumber;
            //将数据库数据存入redis
            userRedis.setUserRedis(usertoken);
            //永久储存
            boolean set = redisutils.set(rediskey, userRedis, 1);
            System.out.println(usernumber + " 数据存入redis：  " + set);

            map.put("token", usertoken);
            map.put("quantity", String.valueOf(selectuseridequipment));
            map.put("userid", userid);
            map.put("username", username);

            return new Userreturn<>(map);

        } catch (Exception e) {   //用户名不存在
            e.printStackTrace();
            System.out.println("user: 用户名错误");
            return new Userreturn<>(erroraccountpassword);
        }

    }

    @PostMapping("/user/startup")
    public Userreturn startupproject(HttpServletRequest request, @RequestBody ProjectData projectData) {

        String status = projectData.getStatus();
        String projectid = projectData.getProjectid();
        String stringtime = Utils.stringtime();

        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            String usernumber = tokenInfo.get("usernumber");
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }


            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    List<ProjectData> selecteequipmentdata = oProject.selecteequipmentdata(projectid);
                    int i = 0;
                    for (ProjectData selecteequipmentdatum : selecteequipmentdata) {

                        String serialid = selecteequipmentdatum.getSerialid();
                        Integer updatesrialidstatus = oProject.updatesrialidstatus(status, stringtime, serialid);
                        Integer updateprojectstatus = oProject.updateprojectstatus(status, stringtime, projectid);
                        if (updatesrialidstatus == 1) {
                            if (updateprojectstatus == 1) {
                                i++;
                                if (selecteequipmentdata.size() == i) {
                                    return new Userreturn<>();
                                }
                            }

                        } else {
                            return new Userreturn<>(noknowerror);
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
        return new Userreturn<>(noknowerror);
    }


    /**
     * 用户查看项目名称
     *
     * @param request
     * @return
     */
    @GetMapping("/user/selectprojectname")
    public Userreturn selectproject(HttpServletRequest request) {
        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            String usernumber = tokenInfo.get("usernumber");
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //用电话号码查询userid
            User existuser = oUserm.existuser(usernumber);
            String userid = existuser.getUserid();

            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    List<ProjectData> selectallproject = oProject.selectallproject(userid);
                    List<JSONObject> selectprojectuser = projectservice.selectprojectuser(selectallproject);

                    return new Userreturn<>(selectprojectuser);
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
     * 用户添加设备到项目
     *
     * @param request          HttpServletRequest request
     * @param projectequipment Projectequipment   serialid  equipmentname  typesof
     * @return Userreturn
     */
    @PostMapping("/user/insertequipment")
    public Userreturn insertequipment(HttpServletRequest request, @RequestBody Projectequipment projectequipment) {

        //当前时间戳
        long updatatime = System.currentTimeMillis();
        long createtime = System.currentTimeMillis();

        //序列号
        String serialid = projectequipment.getSerialid();

        //项目设备onlyid
        String randomStr = Utils.RandomStr(16);

        String co2senserid = Utils.RandomStr(16) + "-" + Utils.RandomStr(8);
        String humisenserid = Utils.RandomStr(16) + "-" + Utils.RandomStr(8);
        String tempsenserid = Utils.RandomStr(16) + "-" + Utils.RandomStr(8);
        String illuminacesenserid = Utils.RandomStr(16) + "-" + Utils.RandomStr(8);
        String watertempsenserid = Utils.RandomStr(16) + "-" + Utils.RandomStr(8);
        String conductivitysenserid = Utils.RandomStr(16) + "-" + Utils.RandomStr(8);

        //设备名称
        String equipmentname = projectequipment.getEquipmentname();

        if (equipmentname.length() > 12) {
            return new Userreturn<>("设备名称长度最高为12位字符");
        }

        //设备类型
        int typesof = projectequipment.getTypesof();

        String projectid = projectequipment.getProjectid();

        if (projectid == null || setnull.equals(projectid)) {
            return new Userreturn<>("数据传输错误");
        }

        String data = "1";
        String status = "0";

        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            String usernumber = tokenInfo.get("usernumber");
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {

                    //从token里拿出号码，去查询userid
                    User existuser = oUserm.existuser(usernumber);
                    String userid = existuser.getUserid();

                    //查询allequipment是否存在序列号
                    Allequipment equipmentserialidexist = oEquipment.equipmentserialidexist(serialid);

//                    //查询userequipment是否序列号被绑定
//                    AddAllequipment selectuserquipment = oUserm.selectuserquipment(userid, serialid);

                    //查询userequipment中是否已绑定该设备
                    AddAllequipment selectexist = oUserm.selectexist(serialid);

                    if (equipmentserialidexist == null) {
                        return new Userreturn<>(unknowserialid);
                    }
                    if (selectexist != null) {
                        return new Userreturn<>(errorserialidother);
                    }
//                    else if (selectuserquipment!=null){
//
//                        return new Userreturn<>(errorserialid);
//                    }
                    //查询typesof表中是否有这个设备类型
                    Typesofname equipmenttypesof = oEquipment.equipmenttypesof(typesof);
                    if (equipmenttypesof == null) {
                        return new Userreturn<>(errortypesof);
                    }

                    //用户添加设备
                    Integer useraddequipment = oUserm.useraddequipment(userid, serialid, equipmentname, typesof, data, String.valueOf(updatatime), String.valueOf(createtime));

                    //把设备添加到项目
                    Integer insertequipmenttoproject = oProject.insertequipmenttoproject(randomStr, projectid, serialid, String.valueOf(updatatime), String.valueOf(createtime));

                    Integer serialidsenser = oUserm.serialidsenser(userid, serialid, co2senserid, tempsenserid, humisenserid, conductivitysenserid,
                            watertempsenserid, illuminacesenserid, String.valueOf(updatatime), String.valueOf(createtime));


                    if (useraddequipment == 1 && insertequipmenttoproject == 1 && serialidsenser == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn(noknowerror);
                    }

                } else {
                    return new Userreturn<>(0.0);
                }

            } else {
                return new Userreturn<>(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(noknowerror);
        }

    }

    /**
     * 用户更新项目信息
     *
     * @param request
     * @param projectData
     */
    @PostMapping("user/upjectdata")
    public Userreturn upjectdata(HttpServletRequest request, @RequestBody ProjectData projectData) {
        String addressproject = projectData.getAddressproject();
        String headofsafety = projectData.getHeadofsafety();
        String projectleader = projectData.getProjectleader();
        String projectname = projectData.getProjectname();
        String projectid = projectData.getProjectid();
        String stringtime = Utils.stringtime();
        //经度
        String lng = projectData.getLongitude();
        //纬度
        String lat = projectData.getLatitude();

        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            String usernumber = tokenInfo.get("usernumber");
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //用电话号码查询userid
            User existuser = oUserm.existuser(usernumber);
            String userid = existuser.getUserid();

            ProjectData selectprojecton = oProject.selectprojecton(userid, projectid);
            if (selectprojecton == null) {
                return new Userreturn<>("你还没有该设备");
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    Integer updateprojectdata = oProject.updateprojectdata(projectname, projectleader, headofsafety, addressproject, stringtime, projectid, lng, lat);
                    if (updateprojectdata == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>("修改失败");
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
     * 用户删除项目
     *
     * @param request
     * @param projectData
     * @return
     */
    @PostMapping("user/deproject")
    public Userreturn delectproject(HttpServletRequest request, @RequestBody ProjectData projectData) {
        String projectid = projectData.getProjectid();
        String stringtime = Utils.stringtime();

        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            String usernumber = tokenInfo.get("usernumber");
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //用电话号码查询userid
            User existuser = oUserm.existuser(usernumber);
            String userid = existuser.getUserid();

            ProjectData selectprojecton = oProject.selectprojecton(userid, projectid);
            if (selectprojecton == null) {
                return new Userreturn<>("你还没有该设备");
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    ProjectData selectprojectbyprojectid = oProject.selectprojectbyprojectid(projectid);
                    if (selectprojectbyprojectid == null) {
                        Integer delectproject = oProject.delectproject(stringtime, projectid);
                        if (delectproject == 1) {
                            return new Userreturn<>();
                        } else {
                            return new Userreturn<>("删除失败");
                        }
                    } else {
                        return new Userreturn<>("项目下还有设备未移除");
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


    @PostMapping("user/deequipment")
    public Userreturn delectuserequipment(HttpServletRequest request, @RequestBody ProjectData projectData) {
        String projectid = projectData.getProjectid();
        String serialid = projectData.getSerialid();


        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            String usernumber = tokenInfo.get("usernumber");
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //用电话号码查询userid
            User existuser = oUserm.existuser(usernumber);
            String userid = existuser.getUserid();

            ProjectData selectprojecton = oProject.selectprojecton(userid, projectid);
            if (selectprojecton == null) {
                return new Userreturn<>("你还没有该设备");
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    Integer delectuserequipment = oUserm.delectuserequipment(projectid, serialid);
                    Integer delectequipmenttouserequipment = oUserm.delectequipmenttouserequipment(userid, serialid);
                    Integer delectdatalogsall = oUserm.delectdatalogsall(serialid);
                    Integer deletesenserid = oSerial.deletesenserid(serialid);
                    Integer delectthreshold = oProject.delectthreshold(serialid, userid);
                    if (delectuserequipment == 1 && delectequipmenttouserequipment == 1 && delectdatalogsall == 1 && deletesenserid > 0 || delectthreshold == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>("删除失败,数据异常");
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
     * 用户查看设备列表
     *
     * @param request
     * @return
     */
    @PostMapping("/user/selectprojectdata")
    public Userreturn selectprojectdata(HttpServletRequest request, @RequestBody AddAllequipment addAllequipment) {
        try {
            int pages = addAllequipment.getPages();
            int startIndex = 8 * (pages - 1);
            int pageSize = 8 + (8 * (pages - 1));

            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                //从token里拿出号码，去查询userid
                String usernumber = tokenInfo.get("usernumber");
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    User existuser = oUserm.existuser(usernumber);
                    String userid = existuser.getUserid();

                    List<ProjectData> selectallproject = oProject.selectallproject(userid);
                    Map<String, Object> selectprojectdata = projectservice.selectprojectdata(selectallproject, startIndex, pageSize);

                    return new Userreturn(selectprojectdata);
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
     * 用户删除设备
     *
     * @param delectbyonlyid
     * @param request
     * @return
     */
    @PostMapping("user/delectserialid")
    public Userreturn deletesrialid(@RequestBody Delectbyonlyid delectbyonlyid, HttpServletRequest request) {
        String[] alarmonlyid = delectbyonlyid.getAlarmonlyid();
        int i = 0;

        try {

            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                //从token里拿出号码，去查询userid
                String usernumber = tokenInfo.get("usernumber");


                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    for (String s : alarmonlyid) {
                        Integer delectprocessed = oUserm.delectprocessed(s);
                        if (delectprocessed == 1) {
                            System.out.println(s + "  删除成功");
                        } else {
                            return new Userreturn<>("数据异常");
                        }
                        i++;
                        if (alarmonlyid.length == i) {
                            return new Userreturn<>();
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

        return new Userreturn<>("数据异常");
    }


    /**
     * 用户修改密码
     *
     * @param request HttpServletRequest request
     * @param user    root
     * @return Userreturn
     */
    @PostMapping("/user/updatauserpassword")
    public Userreturn updatauserpassowrd(HttpServletRequest request, @RequestBody User user) {
        String password = user.getPassword();
        String oldpassword = user.getOldpassword();
        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {

                if (password.length() > 16) {
                    return new Userreturn<>("新密码长度超过16位");
                }
                String usernumber = tokenInfo.get("usernumber");
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    User existuser = oUserm.loginuser(usernumber, oldpassword);
                    if (existuser == null) {
                        return new Userreturn("旧密码不正确");
                    }

                    Integer updatarootpassword = oUserm.updatauserpassword(usernumber, password);
                    if (updatarootpassword == 1) {
                        System.out.println(usernumber + "修改密码成功");
                        return new Userreturn<>();
                    }
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new Userreturn<>(unknowreason);
        }
        return new Userreturn<>(unknowreason);
    }


    /**
     * 查询用户下的设备和设备名称
     *
     * @param request
     * @return
     */
    @GetMapping("/user/alluserqeuipment")
    public Userreturn alluserqeuipment(HttpServletRequest request) {
        try {

            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();

                if (requesttoken.equals(roottoken)) {

                    User existuser = oUserm.existuser(usernumber);
                    String userid = existuser.getUserid();
                    List<AddAllequipment> addAllequipments = oUserm.alluserqeuipment(userid);
                    Map<String, Map<String, String>> linkedHashMap = new LinkedHashMap<>();
                    for (int i = 0; i < addAllequipments.size(); i++) {
                        Map<String, String> map = new LinkedHashMap<>();


                        AddAllequipment addAllequipment = addAllequipments.get(i);
                        map.put("serialid" + i, addAllequipment.getSerialid());
                        map.put("equipmentname" + i, addAllequipment.getEquipmentname());
                        linkedHashMap.put("equipment", map);

                        System.out.println(linkedHashMap.get("equipment"));
                        if (addAllequipments.size() == i + 1) {
                            return new Userreturn<>(linkedHashMap);
                        }
                    }
                    return new Userreturn<>();
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(errornoerror);
        }
    }

    /**
     * 修改用户设备名称
     *
     * @param request         HttpServletRequest request
     * @param addAllequipment AddAllequipment
     * @return Userreturn
     */
    @PostMapping("/user/updateequipmentname")
    public Userreturn updateequipmentname(HttpServletRequest request, @RequestBody AddAllequipment addAllequipment) {
        //当前时间戳
        long updatatime = System.currentTimeMillis();
        String userid = addAllequipment.getUserid();
        String equipmentname = addAllequipment.getEquipmentname();
        if (equipmentname.length() > 12) {
            return new Userreturn<>("设备名称长度最高为12位字符");
        }
        String serialid = addAllequipment.getSerialid();
        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");

                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    AddAllequipment selectuserquipment = oUserm.selectuserquipment(userid, serialid);
                    if (selectuserquipment == null) {
                        return new Userreturn<>(errornotenull);
                    }
                    Integer updatauserequipmentusername = oUserm.updatauserequipmentusername(userid, serialid, equipmentname, String.valueOf(updatatime));
                    if (updatauserequipmentusername == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>(errornoerror);
                    }

                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new Userreturn<>(errornoerror);
        }

    }

    /**
     * 查看用户是否绑定阈值
     *
     * @param request   HttpServletRequest request
     * @param threshold serialid
     * @return Userreturn
     */
    @PostMapping("/user/selectuserEquipmentThreshold")
    public Userreturn selectuserEquipmentThreshold(HttpServletRequest request, @RequestBody Threshold threshold) {
        Map<String, String> map = new LinkedHashMap<>();
        String serialid = threshold.getSerialid();
        try {

            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                User existuser = oUserm.existuser(usernumber);
                String userid = existuser.getUserid();

                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    //查看序列号和userid是否绑定了，绑定了说明有了阈值
                    Threshold existuserthreshold = oUserm.existuserthreshold(userid, serialid);
                    if (existuserthreshold != null) {
                        String humidity = existuserthreshold.getHumidity();
                        String illumination = existuserthreshold.getIllumination();
                        String temperature = existuserthreshold.getTemperature();
                        String conductivity = existuserthreshold.getConductivity();
                        String watertemp = existuserthreshold.getWatertemp();
                        String co2 = existuserthreshold.getCo2();

                        map.put("humi", humidity);
                        map.put("illumination", illumination);
                        map.put("temp", temperature);
                        map.put("co2", co2);
                        map.put("conductivity", conductivity);
                        map.put("watertemp", watertemp);
                        return new Userreturn<>(map);
                    } else {
                        return new Userreturn<>("未设定阈值");
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
     * 用户设定阈值,如果已经设定了则是修改
     *
     * @param request   HttpServletRequest request
     * @param threshold serialid illumination humidity temperature
     * @return
     */
    @PostMapping("/user/insertuserEquipmentThreshold")
    public Userreturn insertuserEquipmentThreshold(HttpServletRequest request, @RequestBody Threshold threshold) {

        //当前时间戳
        long updatatime = System.currentTimeMillis();
        long createtime = System.currentTimeMillis();

        String serialid = threshold.getSerialid();
        String illumination = threshold.getIllumination();
        String humidity = threshold.getHumidity();
        String temperature = threshold.getTemperature();
        String co2 = threshold.getCo2();
        String watertemp = threshold.getWatertemp();
        String conductivity = threshold.getConductivity();
        //判断是不是两位小数的double
        boolean pattern = Utils.pattern(humidity, temperature, illumination, co2, watertemp, conductivity);
        if (!pattern) {
            return new Userreturn<>(errornotedata);
        }

        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                User existuser = oUserm.existuser(usernumber);
                String userid = existuser.getUserid();

                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    //查看序列号和userid是否绑定了，绑定了说明有了阈值
                    Threshold existuserthreshold = oUserm.existuserthreshold(userid, serialid);
                    if (existuserthreshold == null) {
                        Integer integer = oUserm.insertuserEquipment(userid, serialid, illumination, temperature, humidity, co2, watertemp, conductivity, String.valueOf(updatatime), String.valueOf(createtime));
                        if (integer == 1) {
                            return new Userreturn<>();
                        } else {
                            return new Userreturn<>(noknowerror);
                        }
                    } else {
                        Integer integer = oUserm.updateuserEquipmentthreshold(serialid, illumination, temperature, humidity, co2, watertemp, conductivity, String.valueOf(updatatime));
                        if (integer == 1) {
                            return new Userreturn<>();
                        } else {
                            return new Userreturn<>(noknowerror);
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
    }

    /**
     * 选择项目设备查看数据  all
     */
    @PostMapping("/user/choosedatalog")
    public Userreturn choosealldataloglimit(@RequestBody AddAllequipment addAllequipment, HttpServletRequest request) {
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
                    JSONObject alldatalog = userservice.choosealldataloglimit(projectid, serialid, startIndex, pageSize);
                    System.out.println(alldatalog);
                    if (alldatalog != null) {
                        return new Userreturn<>(alldatalog);
                    } else {
                        return new Userreturn<>(noknowerror);
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
     * 查询全部设备数据
     *  alldatalog
     * @param addAllequipment
     * @param request
     * @return
     */
    @PostMapping("/user/selectequipmentdatalog")
    public Userreturn alldataloglimit(@RequestBody AddAllequipment addAllequipment, HttpServletRequest request) {

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
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    List<AllDatalog> selectalldatalog = oProject.selectalldatalog(userid,startIndex, pageSize);

                    JSONObject alldataloglimit = userservice.alldataloglimit(selectalldatalog,userid);

                    if (alldataloglimit != null) {
                        return new Userreturn<>(alldataloglimit);
                    } else {
                        return new Userreturn<>();
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
     * 实时数据
     *
     * @param addAllequipment
     * @param request
     * @return
     */
    @PostMapping("/user/dataltime")
    public Userreturn gettimedatal(@RequestBody AddAllequipment addAllequipment, HttpServletRequest request) {
        String serialid = addAllequipment.getSerialid();
        String projectid = addAllequipment.getProjectid();

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
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    Boolean selectprojectid = oProject.selectprojectid(projectid, serialid);
                    if (!selectprojectid) {
                        return new Userreturn("数据错误");
                    }
                    Map<String, Object> map = new HashMap<>();
                    Object obj = redisutils.get("key_" + serialid);
                    ObjectMapper rdataMapper = new ObjectMapper();
                    Rdata rdata = rdataMapper.convertValue(obj, Rdata.class);

                    if (rdata == null) {
                        return new Userreturn<>("暂时没有数据");
                    }

                    String co2 = rdata.getCo2();
                    String humi = rdata.getHumi();
                    String conductivity = rdata.getConductivity();
                    String illuminance = rdata.getIlluminance();
                    String watertemp = rdata.getWatertemp();
                    String temp = rdata.getTemp();
                    String time = rdata.getTime();

                    map.put("co2", co2);
                    map.put("humi", humi);
                    map.put("conductivity", conductivity);
                    map.put("illuminance", illuminance);
                    map.put("water", watertemp);
                    map.put("temp", temp);
                    map.put("time", time);

                    JSONObject jsonObject = new JSONObject(map);
                    return new Userreturn<>(jsonObject);
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
     * 查看用户在线设备个数
     *
     * @param request
     * @return
     */
    @GetMapping("user/equipmentonline")
    public Userreturn equipmentonlinecout(HttpServletRequest request) {
        try {
            Map<String, Integer> map = new HashMap<>();
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
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    //设备总数
                    Integer equipmenttotal = oUserm.selectuseridequipment(userid);
                    System.out.println(usernumber + "设备总数：  " + equipmenttotal);
                    //设备在线数量
                    Integer equipmentonline = oUserm.countequipmentonline(userid);
                    System.out.println(usernumber + "设备在线数量：  " + equipmentonline);
                    int offline = equipmenttotal - equipmentonline;
                    System.out.println(usernumber + "设备离线数量：  " + offline);

                    map.put("online", equipmentonline);
                    map.put("offline", offline);
                    return new Userreturn<>(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(noknowerror);
        }
        return new Userreturn<>(noknowerror);

    }


    //用户修改设备类型
    @PostMapping("/user/updateserialidtypesof")
    public Userreturn updateSerialIdTypes(HttpServletRequest request, @RequestBody Allequipment allequipment) {
        String equipmentname = allequipment.getEquipmentname();
        String typesof = allequipment.getTypesof();
        String serialid = allequipment.getSerialid();
        try {
            if (equipmentname.length() > 12) {
                return new Userreturn<>("设备名称长度最高为12位字符");
            }
            Integer integer = Integer.valueOf(typesof);
            //查询typesof表中是否有这个设备类型
            Typesofname equipmenttypesof = oEquipment.equipmenttypesof(integer);
            if (equipmenttypesof == null) {
                return new Userreturn<>(errortypesof);
            }
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
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    Integer updateprodata = oUserm.updateprodata(equipmentname, typesof, serialid);
                    if (updateprodata == 1) {
                        return new Userreturn<>();
                    }
                    return new Userreturn<>("数据异常");
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
     * 查询用户历史记录
     *
     * @param request
     * @param threshold
     * @return
     */
    @PostMapping("/user/selectdata")
    public Userreturn selectdata(HttpServletRequest request, @RequestBody Threshold threshold) {
        String serialid = threshold.getSerialid();

        LinkedHashMap<Object, Object> linkedHashMap = new LinkedHashMap<>();
        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                User existuser = oUserm.existuser(usernumber);
                String userid = existuser.getUserid();

                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    List<Illumination> userillumination = oUserm.userillumination(userid, serialid);
                    if (userillumination == null) {
                        return new Userreturn<>("暂无数据");
                    }
                    linkedHashMap.put("illumination", userillumination);
                    System.out.println("已查询用户" + usernumber + " 的记录");
                    return new Userreturn<>(linkedHashMap);
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
     * 最近七天中每一天的数据平均数
     *
     * @param rTime
     * @return
     */
    @PostMapping("user/onedaydata")
    public Userreturn selectaverage(@RequestBody RTime rTime, HttpServletRequest request) {
        String projectid = rTime.getProjectid();
        String serialid = rTime.getSerialid();
        String[] time = rTime.getTime();

        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //判断有没有这个设备在项目里
            Boolean selectprojectid = oProject.selectprojectid(projectid, serialid);
            if (!selectprojectid) {
                return new Userreturn<>("数据错误");
            }

            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");

                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    JSONObject selectaverage = userservice.selectaverage(time, serialid);
                    return new Userreturn<>(selectaverage);
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
     * 查看报警信息
     *
     * @param request
     * @return
     */
    @PostMapping("/user/alarmuser")
    public Userreturn alarmdatauser(HttpServletRequest request, @RequestBody AddAllequipment addAllequipment) {

        String starttime = addAllequipment.getStarttime();
        String endtime = addAllequipment.getEndtime();
        try {
            Date date = new Date();
            //如果输入的时间为null  就自己生产时间Data
            if ("".equals(starttime) || starttime == null || "".equals(endtime) || endtime == null) {
                starttime = "2020-08-01 12:12:00:000";
                endtime = TimeData.DateToString(date);
            }

            String stringstarttime = TimeData.StringToTimestamp(starttime);
            String stringendtime = TimeData.StringToTimestamp(endtime);

            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                User existuser = oUserm.existuser(usernumber);
                String userid = existuser.getUserid();

                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    //查询datalog表  去重  ，拿到serialid
                    List<ProjectData> selectdatalog = oProject.selectdataloguser(userid, stringstarttime, stringendtime);
                    List<Map> list = projectservice.selectdatalog(selectdatalog);
                    return new Userreturn<>(list);
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
     * 用户是否已处理
     *
     * @param delectbyonlyid
     * @param request
     * @return
     */
    @PostMapping("user/handleman")
    public Userreturn handleman(@RequestBody Delectbyonlyid delectbyonlyid, HttpServletRequest request) {
        String[] senserid = delectbyonlyid.getSenserid();
//        传的是最大的参数
        String time = delectbyonlyid.getTime();
        String handleman = delectbyonlyid.getHandleman();
        int haveread = delectbyonlyid.getHaveread();
        String stringtime = Utils.stringtime();
        String randomStr = Utils.RandomStr(18);

        String starttime = delectbyonlyid.getStarttime();
        String endtime = delectbyonlyid.getEndtime();


        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }

            Date date = new Date();
            //如果输入的时间为null  就自己生产时间Data
            if ("".equals(starttime) || starttime == null || "".equals(endtime) || endtime == null) {
                starttime = "2020-08-01 12:12:00:000";
                endtime = TimeData.DateToString(date);
            }
//            data转时间搓
            String stringstarttime = TimeData.StringToTimestamp(starttime);
            String stringendtime = TimeData.StringToTimestamp(endtime);

            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");

                User existuser = oUserm.existuser(usernumber);
                String userid = existuser.getUserid();


                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {

                    for (String s : senserid) {
                        int i = 0;


                        //拿到data为1的最小时间搓
                        AllDatalog allDatalog = oSerial.alarmmaxtimetoco2timemin(s, stringstarttime, stringendtime);
                        if (allDatalog != null) {
                            String mintime = allDatalog.getCreatetime();
                            Integer integer1 = oSerial.selectco2alarmtatol(s, mintime, time);
                            //拿到时间段里  co2senserid里的数据
                            List<AllDatalog> allDatalogs = oSerial.selecttimebeteennowtominco2(mintime, time, s);

                            //获取最大的值
                            AllDatalog co2data = oSerial.alarmmaxtimetoco2(time);
                            double co2 = co2data.getCo2();

                            for (AllDatalog datalog : allDatalogs) {

                                String onlyid = datalog.getOnlyid();
                                //查找onlyid是否已经存在
                                Handle selecthandleonlyid = oSerial.selecthandleonlyid(s, mintime);
                                if (selecthandleonlyid == null) {
                                    i++;
                                    oSerial.updatehandleco2(onlyid, stringtime, haveread);
                                    //把修改记录存入记录表
                                    oSerial.inserthandle(randomStr, s, handleman, haveread, integer1, co2, mintime, time, stringtime, stringtime);
                                }
                                //修改记录
                                Integer integer = oSerial.updatehandleco2(onlyid, stringtime, haveread);
                                if (integer == 1) {
                                    i++;
                                    if (i == allDatalogs.size()) {
                                        return new Userreturn<>();
                                    }

                                }

                            }
                        }

                        AllDatalog alarmmaxtimetohumitimemin = oSerial.alarmmaxtimetohumitimemin(s, stringstarttime, stringendtime);
                        if (alarmmaxtimetohumitimemin != null) {

                            String mintime = alarmmaxtimetohumitimemin.getCreatetime();
                            System.out.println(mintime + "==========");

                            Integer integer1 = oSerial.selecthumialarmtatol(s, mintime, time);
                            List<AllDatalog> selecttimebeteennowtominhumi = oSerial.selecttimebeteennowtominhumi(mintime, time, s);

                            //获取最大的值 humialarm
                            AllDatalog alarmmaxtimetohumi = oSerial.alarmmaxtimetohumi(time);
                            double humi = alarmmaxtimetohumi.getHumi();


                            for (AllDatalog datalog : selecttimebeteennowtominhumi) {
                                String onlyid = datalog.getOnlyid();
                                //查找starttime是否已经存在
                                Handle selecthandleonlyid = oSerial.selecthandleonlyid(s, mintime);
                                if (selecthandleonlyid == null) {
                                    i++;
                                    oSerial.updatehandlehumi(onlyid, stringtime, haveread);
                                    //把修改记录存入记录表
                                    oSerial.inserthandle(randomStr, s, handleman, haveread, integer1, humi, mintime, time, stringtime, stringtime);

                                } else {

                                    Integer updatehandlehumi = oSerial.updatehandlehumi(onlyid, stringtime, haveread);
                                    if (updatehandlehumi == 1) {
                                        i++;
                                        if (i == selecttimebeteennowtominhumi.size()) {
                                            return new Userreturn<>();
                                        }

                                    }
                                }
                            }
                        }

                        AllDatalog alarmmaxtimetotemptimemin = oSerial.alarmmaxtimetotemptimemin(s, stringstarttime, stringendtime);
                        if (alarmmaxtimetotemptimemin != null) {

                            String mintime = alarmmaxtimetotemptimemin.getCreatetime();
                            Integer integer1 = oSerial.selecttempalarmtatol(s, mintime, time);

                            //获取最大的值 tempalarm
                            AllDatalog tempdata = oSerial.alarmmaxtimetotemp(time);
                            double temp = tempdata.getTemp();
                            List<AllDatalog> selecttimebeteennowtomintemp = oSerial.selecttimebeteennowtomintemp(mintime, time, s);

                            for (AllDatalog datalog : selecttimebeteennowtomintemp) {
                                String onlyid = datalog.getOnlyid();
                                //查找starttime是否已经存在
                                Handle selecthandleonlyid = oSerial.selecthandleonlyid(s, mintime);
                                if (selecthandleonlyid == null) {
                                    i++;
                                    oSerial.updatehandlewtomintemp(onlyid, stringtime, haveread);
                                    //把修改记录存入记录表
                                    oSerial.inserthandle(randomStr, s, handleman, haveread, integer1, temp, mintime, time, stringtime, stringtime);


                                } else {

                                    //修改记录
                                    Integer updatehandlewtomintemp = oSerial.updatehandlewtomintemp(onlyid, stringtime, haveread);

                                    if (updatehandlewtomintemp == 1) {
                                        i++;
                                        if (i == selecttimebeteennowtomintemp.size()) {
                                            return new Userreturn<>();
                                        }
                                    }
                                }


                            }
                        }

                        AllDatalog alarmmaxtimetoconductivitytimemin = oSerial.alarmmaxtimetoconductivitytimemin(s, stringstarttime, stringendtime);
                        if (alarmmaxtimetoconductivitytimemin != null) {

                            String mintime = alarmmaxtimetoconductivitytimemin.getCreatetime();
                            Integer integer1 = oSerial.selectconductivityalarmtatol(s, mintime, time);
                            List<AllDatalog> selecttimebeteennowtomintwoconductivity = oSerial.selecttimebeteennowtomintwoconductivity(mintime, time, s);
                            //获取最大的值 conductivityalarm
                            AllDatalog alarmmaxtimetoconductivity = oSerial.alarmmaxtimetoconductivity(time);
                            double conductivity = alarmmaxtimetoconductivity.getConductivity();

                            for (AllDatalog datalog : selecttimebeteennowtomintwoconductivity) {
                                String onlyid = datalog.getOnlyid();
                                //查找starttime是否已经存在
                                Handle selecthandleonlyid = oSerial.selecthandleonlyid(s, mintime);
                                if (selecthandleonlyid == null) {
                                    i++;
                                    oSerial.updatehandleconductivity(onlyid, stringtime, haveread);
                                    //把修改记录存入记录表
                                    oSerial.inserthandle(randomStr, s, handleman, haveread, integer1, conductivity, mintime, time, stringtime, stringtime);


                                } else {

                                    //修改记录
                                    Integer updatehandleconductivity = oSerial.updatehandleconductivity(onlyid, stringtime, haveread);

                                    if (updatehandleconductivity == 1) {
                                        i++;
                                        if (i == selecttimebeteennowtomintwoconductivity.size()) {
                                            return new Userreturn<>();
                                        }
                                    }
                                }

                            }
                        }

                        AllDatalog alarmmaxtimetowatertemptimemin = oSerial.alarmmaxtimetowatertemptimemin(s, stringstarttime, stringendtime);
                        if (alarmmaxtimetowatertemptimemin != null) {

                            String mintime = alarmmaxtimetowatertemptimemin.getCreatetime();
                            Integer integer1 = oSerial.selectwatertempalarmtatol(s, mintime, time);
                            //获取最大的值 watertempalarm
                            AllDatalog alarmmaxtimetowatertemp = oSerial.alarmmaxtimetowatertemp(time);
                            double watertemp = alarmmaxtimetowatertemp.getWatertemp();

                            List<AllDatalog> selecttimebeteennowtominwatertemp = oSerial.selecttimebeteennowtominwatertemp(mintime, time, s);


                            for (AllDatalog datalog : selecttimebeteennowtominwatertemp) {
                                String onlyid = datalog.getOnlyid();
                                Handle selecthandleonlyid = oSerial.selecthandleonlyid(s, mintime);
                                //查找starttime是否已经存在
                                if (selecthandleonlyid == null) {
                                    i++;
                                    oSerial.updatehandlewatertemp(onlyid, stringtime, haveread);
                                    //把修改记录存入记录表
                                    oSerial.inserthandle(randomStr, s, handleman, haveread, integer1, watertemp, mintime, time, stringtime, stringtime);

                                } else {

                                    //修改记录
                                    Integer updatehandlewatertemp = oSerial.updatehandlewatertemp(onlyid, stringtime, haveread);

                                    if (updatehandlewatertemp == 1) {
                                        i++;
                                        if (i == selecttimebeteennowtominwatertemp.size()) {
                                            return new Userreturn<>();
                                        }
                                    }
                                }

                            }
                        }

                        AllDatalog alarmmaxtimetoilluminancetimemin = oSerial.alarmmaxtimetoilluminancetimemin(s, stringstarttime, stringendtime);
                        if (alarmmaxtimetoilluminancetimemin != null) {

                            String mintime = alarmmaxtimetoilluminancetimemin.getCreatetime();
                            Integer integer1 = oSerial.selectilluminancealarmtatol(s, mintime, time);
                            List<AllDatalog> selecttimebeteennowtominillumination = oSerial.selecttimebeteennowtominillumination(mintime, time, s);

                            //获取最大的值 illuminancealarm
                            AllDatalog alarmmaxtimetoilluminance = oSerial.alarmmaxtimetoilluminance(time);
                            double illuminance = alarmmaxtimetoilluminance.getIlluminance();


                            for (AllDatalog datalog : selecttimebeteennowtominillumination) {
                                String onlyid = datalog.getOnlyid();
                                //查找starttime是否已经存在
                                Handle selecthandleonlyid = oSerial.selecthandleonlyid(s, mintime);
                                if (selecthandleonlyid == null) {
                                    i++;
                                    oSerial.updatehandleillumination(onlyid, stringtime, haveread);
                                    //把修改记录存入记录表
                                    oSerial.inserthandle(randomStr, s, handleman, haveread, integer1, illuminance, mintime, time, stringtime, stringtime);

                                } else {

                                    //修改记录   xtsj.illumination
                                    Integer updatehandleillumination = oSerial.updatehandleillumination(onlyid, stringtime, haveread);

                                    if (updatehandleillumination == 1) {
                                        i++;
                                        if (i == selecttimebeteennowtominillumination.size()) {
                                            return new Userreturn<>();
                                        }
                                    }
                                }

                            }
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
        return new Userreturn("数据异常");
    }

    /**
     * 用户已处理
     *
     * @param request
     * @return
     */
    @GetMapping("/user/outofstock")
    public Userreturn ii(HttpServletRequest request) {

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

            List<Sensorid> selectsenseridonbyuserid = oProject.selectsenseridonbyuserid(userid);
            List<String> list = new ArrayList<>();

            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    for (Sensorid sensoridbyuserid : selectsenseridonbyuserid) {
                        String co2sensorid = sensoridbyuserid.getCo2sensorid();
                        String humisensorid = sensoridbyuserid.getHumisensorid();
                        String conductivitysensorid = sensoridbyuserid.getConductivitysensorid();
                        String illuminancesensorid = sensoridbyuserid.getIlluminancesensorid();
                        String watertempsensorid = sensoridbyuserid.getWatertempsensorid();
                        String tempsensorid = sensoridbyuserid.getTempsensorid();
                        list.add(conductivitysensorid);
                        list.add(co2sensorid);
                        list.add(humisensorid);
                        list.add(illuminancesensorid);
                        list.add(watertempsensorid);
                        list.add(tempsensorid);
                    }
                    List<Map<String, Object>> mapList = new ArrayList<>();

                    for (String s : list) {

                        try {


                            List<Handle> selectuserhandle = oProject.selectuserhandle(s);
                            for (Handle handle : selectuserhandle) {
                                Map<String, Object> parmMap = new HashMap<>();
                                Map<String, String> map = new HashMap<>();
                                double data = handle.getData();
                                int total = handle.getTotal();
                                String handlename = handle.getHandlename();
                                int handleresult = handle.getHandleresult();
                                String endtime = handle.getEndtime();
                                String alarmonlyid = handle.getAlarmonlyid();
                                parmMap.put("alarmonlyid", alarmonlyid);
                                parmMap.put("information", data);
                                parmMap.put("total", total);
                                parmMap.put("handlename", handlename);

                                parmMap.put("haveread", handleresult);
                                parmMap.put("time", endtime);

                                Sensorid selectserialidbysensorid = oSerial.selectserialidbysensoridco2(s);
                                if (selectserialidbysensorid != null) {
                                    String serialid = selectserialidbysensorid.getSerialid();

                                    map.put("serialid", serialid);
                                    parmMap.put("alarmdata", "co2浓度过高");
                                    parmMap.put("sensorid", s);
                                }

                                Sensorid selectserialidbysensoridhumi = oSerial.selectserialidbysensoridhumi(s);
                                if (selectserialidbysensoridhumi != null) {
                                    String serialid = selectserialidbysensoridhumi.getSerialid();
                                    map.put("serialid", serialid);
                                    parmMap.put("alarmdata", "湿度过高");
                                    parmMap.put("sensorid", s);
                                }

                                Sensorid selectserialidbysensoridwatertemp = oSerial.selectserialidbysensoridwatertemp(s);
                                if (selectserialidbysensoridwatertemp != null) {
                                    String serialid = selectserialidbysensoridwatertemp.getSerialid();
                                    map.put("serialid", serialid);
                                    parmMap.put("alarmdata", "水温过高");
                                    parmMap.put("sensorid", s);
                                }

                                Sensorid selectserialidbysensoridtemp = oSerial.selectserialidbysensoridtemp(s);
                                if (selectserialidbysensoridtemp != null) {
                                    String serialid = selectserialidbysensoridtemp.getSerialid();
                                    map.put("serialid", serialid);
                                    parmMap.put("alarmdata", "温度过高");
                                    parmMap.put("sensorid", s);
                                }

                                Sensorid selectserialidbysensoridilluminance = oSerial.selectserialidbysensoridilluminance(s);
                                if (selectserialidbysensoridilluminance != null) {
                                    String serialid = selectserialidbysensoridilluminance.getSerialid();
                                    map.put("serialid", serialid);
                                    parmMap.put("alarmdata", "光照过高");
                                    parmMap.put("sensorid", s);
                                }

                                Sensorid selectserialidbysensoridconductivity = oSerial.selectserialidbysensoridconductivity(s);
                                if (selectserialidbysensoridconductivity != null) {
                                    String serialid = selectserialidbysensoridconductivity.getSerialid();
                                    map.put("serialid", serialid);
                                    parmMap.put("alarmdata", "导电率过高");
                                    parmMap.put("sensorid", s);
                                }
                                String serialid = map.get("serialid");

                                //设备名称
                                AddAllequipment selectusereruipmentstatus = oRootm.selectusereruipmentstatus(serialid);
                                String equipmentname = selectusereruipmentstatus.getEquipmentname();

                                ProjectData selectprojectidtodata = oProject.selectprojectidtodata(serialid);
                                String projectid = selectprojectidtodata.getProjectid();

                                ProjectData projectInfo = oProject.getProjectInfo(projectid);
                                String projectname = projectInfo.getProjectname();

                                parmMap.put("equipmentname", equipmentname);
                                parmMap.put("projectname", projectname);
                                parmMap.put("serialid", serialid);
                                mapList.add(parmMap);
                            }


                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    System.out.println(mapList);
                    return new Userreturn<>(mapList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(noknowerror);
        }
        return new Userreturn<>("数据异常");
    }

    /**
     * 用户删除报警信息
     *
     * @param delectbyonlyid
     * @param request
     * @return
     */
    @PostMapping("/user/delectbysenseridtouser")
    public Userreturn deleteusersenserid(@RequestBody Delectbyonlyid delectbyonlyid, HttpServletRequest request) {
        try {
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            String stringtime = Utils.stringtime();
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    String[] senserid = delectbyonlyid.getSenserid();
                    int i = 0;
                    for (String allsenserid : senserid) {
                        Integer delectuserco2senser = oSerial.delectuserco2senser(allsenserid, stringtime);
                        if (delectuserco2senser > 0) {
                            i++;
                        }
                        Integer delectuserhumiditysenser = oSerial.delectuserhumiditysenser(allsenserid, stringtime);
                        if (delectuserhumiditysenser > 0) {
                            i++;
                        }
                        Integer delectuserconductivitysenser = oSerial.delectuserconductivitysenser(allsenserid, stringtime);
                        if (delectuserconductivitysenser > 0) {
                            i++;
                        }
                        Integer delectusertemperatureasenser = oSerial.delectusertemperatureasenser(allsenserid, stringtime);
                        if (delectusertemperatureasenser > 0) {
                            i++;
                        }
                        Integer delectuserwatertempsenser = oSerial.delectuserwatertempsenser(allsenserid, stringtime);
                        if (delectuserwatertempsenser > 0) {
                            i++;
                        }
                        Integer delectuserilluminationsenser = oSerial.delectuserilluminationsenser(allsenserid, stringtime);
                        if (delectuserilluminationsenser > 0) {
                            i++;
                        }

                        if (i == senserid.length) {
                            System.out.println("已删除完成");
                            return new Userreturn<>();
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
            return new Userreturn(noknowerror);
        }
        return new Userreturn<>("数据异常");
    }

    /**
     * 用户信息
     *
     * @param request
     * @return
     */
    @GetMapping("user/userdata")
    public Userreturn userdata(HttpServletRequest request) {
        try {
            Map<String, Object> map = new HashMap<>();
            List<String> list = new LinkedList<>();
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }

            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");

                //用电话号码查询userid
                User existuser = oUserm.existuser(usernumber);
                String userid = existuser.getUserid();

                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    int i = 0;
                    User selectuserdata = oUserm.selectuserdata(userid);
                    String username = selectuserdata.getUsername();
                    //查询项目信息
                    map.put("username", username);
                    map.put("number", usernumber);
                    List<ProjectData> selectallproject = oProject.selectallproject(userid);
                    for (ProjectData projectData : selectallproject) {
                        String projectname = projectData.getProjectname();

                        String projectid = projectData.getProjectid();
                        //查询项目设备数量
                        Integer selectequipmenttatol = oProject.selectequipmenttatol(projectid);
                        i = selectequipmenttatol + i;
                        map.put("equipmenttotal", String.valueOf(i));
                        list.add(projectname);
                    }
                    /**
                     * 查询项目个数
                     */
                    Integer projecttotal = oProject.projecttotal(userid);
                    map.put("projecttotal", String.valueOf(projecttotal));
                    map.put("projectname", list);
                    return new Userreturn<>(map);
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn(noknowerror);
        }
    }

    /**
     * 查看用户处理和未处理总条数
     *
     * @param request
     * @return
     */
    @GetMapping("/user/equipmentdatatotal")
    public Userreturn alarmtotal(HttpServletRequest request) {
        try {
            Map<String, Integer> map = new HashMap<>();
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
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
//                    未处理报警次数
                    RIntdata alarmtotal = oUserm.alarmuntreatedtotal(userid);
                    int co2alarm = alarmtotal.getCo2();
                    int humialarm = alarmtotal.getHumi();
                    int conductivityalarm = alarmtotal.getConductivity();
                    int illuminancealarm = alarmtotal.getIlluminance();
                    int tempalarm = alarmtotal.getTemp();
                    int watertempalarm = alarmtotal.getWatertemp();
                    int alarm = co2alarm + humialarm + conductivityalarm + illuminancealarm + tempalarm + watertempalarm;

                    RIntdata alarmhavingtotal = oUserm.alarmhavingtotal(userid);
                    int co2having = alarmhavingtotal.getCo2();
                    int humihaving = alarmhavingtotal.getHumi();
                    int conductivityhaving = alarmhavingtotal.getConductivity();
                    int illuminancehaving = alarmhavingtotal.getIlluminance();
                    int temphaving = alarmhavingtotal.getTemp();
                    int watertemphaving = alarmhavingtotal.getWatertemp();

                    int having = co2having + humihaving + conductivityhaving + illuminancehaving + temphaving + watertemphaving;

                    map.put("alarmtotal", alarm);
                    map.put("havingtotal", having);
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


    /**
     * 六个报警数据 分类次数
     *
     * @param request
     * @return
     */
    @GetMapping("/user/equipmentdataseparate")
    public Userreturn alarmtotaltoseparate(HttpServletRequest request) {
        try {
            Map<String, Integer> map = new HashMap<>();
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
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    RIntdata alarmtotal = oUserm.alarmuntreatedtotal(userid);
                    int co2alarm = alarmtotal.getCo2();
                    int humialarm = alarmtotal.getHumi();
                    int conductivityalarm = alarmtotal.getConductivity();
                    int illuminancealarm = alarmtotal.getIlluminance();
                    int tempalarm = alarmtotal.getTemp();
                    int watertempalarm = alarmtotal.getWatertemp();
                    int alarm = co2alarm + humialarm + conductivityalarm + illuminancealarm + tempalarm + watertempalarm;


                    map.put("co2", co2alarm);
                    map.put("humi", humialarm);
                    map.put("conductivity", conductivityalarm);
                    map.put("illuminance", illuminancealarm);
                    map.put("temp", tempalarm);
                    map.put("watertemp", watertempalarm);
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


    /**
     * 用户传感器个数
     *
     * @param request
     * @return
     */
    @GetMapping("/user/categorytotal")
    public Userreturn categorytotal(HttpServletRequest request) {
        try {
            Map<String, Integer> map = new HashMap<>();
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
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    //获取设备总个数
                    Integer selectuseridequipment = oUserm.selectuseridequipment(userid);
                    //获取设备传感器总个数
                    Integer selectuseridsensor = oUserm.selectuseridsensor(userid);

                    //控制器个数
                    int controller = selectuseridequipment - selectuseridsensor;
                    map.put("sensor", selectuseridsensor);
                    map.put("controller", controller);

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


    @GetMapping("/user/monthalarmtotal")
    public Userreturn monthalarm(HttpServletRequest request) {
        long starttime = System.currentTimeMillis();
        try {

            Map<String, Long> stringmap = new HashMap<>();
            List<Integer> alarmlist = new LinkedList<>();
            List<Integer> havinglist = new LinkedList<>();
            Map<String, List<Integer>> map = new HashMap<>();
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
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {

                    String dateToString = TimeData.getDateToString(starttime);
                    String substring = dateToString.substring(0, 4);
                    String newyear = substring + "-12-31 00:00:00:000";
                    System.out.println(substring);
                    String newyeartime = TimeData.StringToTimestamp(newyear);

                    Long aLong = Long.valueOf(newyeartime);
                    String s = "2678400000";
                    Long bLong = Long.valueOf(s);
                    stringmap.put("month12", aLong);
                    //从最后开始算起
                    for (int i = 11; i >= 0; i--) {
                        aLong = aLong - bLong;
                        stringmap.put("month" + i, aLong);
                    }

                    for (int i = 12; i > 0; i--) {
                        int j = (i - 1);
                        Long aLong1 = stringmap.get("month" + i);
                        Long aLong2 = stringmap.get("month" + j);
                        //每个月报警次数
                        RIntdata alarmtotal = oUserm.alarmtotaltomonthbydatalog(userid, String.valueOf(aLong2), String.valueOf(aLong1));
                        int co2alarm = alarmtotal.getCo2();
                        int humialarm = alarmtotal.getHumi();
                        int conductivityalarm = alarmtotal.getConductivity();
                        int illuminancealarm = alarmtotal.getIlluminance();
                        int tempalarm = alarmtotal.getTemp();
                        int watertempalarm = alarmtotal.getWatertemp();
                        int alarm = co2alarm + humialarm + conductivityalarm + illuminancealarm + tempalarm + watertempalarm;
                        alarmlist.add(alarm);

                        //每个月处理次数
                        RIntdata alarmhavingtotal = oUserm.alarmhavingtotaltoMonth(userid, String.valueOf(aLong2), String.valueOf(aLong1));
                        int co2having = alarmhavingtotal.getCo2();
                        int humihaving = alarmhavingtotal.getHumi();
                        int conductivityhaving = alarmhavingtotal.getConductivity();
                        int illuminancehaving = alarmhavingtotal.getIlluminance();
                        int temphaving = alarmhavingtotal.getTemp();
                        int watertemphaving = alarmhavingtotal.getWatertemp();

                        int having = co2having + humihaving + conductivityhaving + illuminancehaving + temphaving + watertemphaving;
                        havinglist.add(having);
                        map.put("alarm", alarmlist);
                        map.put("having", havinglist);


                    }
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

    /**
     * 主页表格  报警总次数
     *
     * @param request
     * @return
     */
    @GetMapping("/user/formdata")
    public Userreturn returnform(HttpServletRequest request) {
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
            String role = tokenInfo.get("role");
            if (userrole.equals(role)) {
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    //用userid查询项目个数
                    List<ProjectData> selectallproject = oProject.selectallproject(userid);
                    List<Map<String, Object>> list = new LinkedList<>();
                    for (ProjectData projectData : selectallproject) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        //项目名称
                        String projectname = projectData.getProjectname();
                        map.put("projectname", projectname);
                        String projectid = projectData.getProjectid();
                        //项目设备数量
                        Integer selectequipmenttatol = oProject.selectequipmenttatol(projectid);
                        map.put("equipmenttotal", selectequipmenttatol);
                        //查询项目里的设备
                        List<ProjectData> selecteequipmentdata = oProject.selecteequipmentdata(projectid);
                        for (ProjectData selecteequipmentdatum : selecteequipmentdata) {
                            String serialid = selecteequipmentdatum.getSerialid();
                            RIntdata selectalarmtotal = oRootm.selectalarmtotal(serialid);
                            int co2 = selectalarmtotal.getCo2();
                            int conductivity = selectalarmtotal.getConductivity();
                            int humi = selectalarmtotal.getHumi();
                            int illuminance = selectalarmtotal.getIlluminance();
                            int temp = selectalarmtotal.getTemp();
                            int watertemp = selectalarmtotal.getWatertemp();
                            int alarm = co2 + conductivity + humi + illuminance + temp + watertemp;
                            int alarmtotal = alarm + alarm;
                            map.put("alarmtotal", alarmtotal);
                        }
                        list.add(map);
                    }
                    return new Userreturn<>(list);
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
     * 用户修改信息
     *
     * @param request
     * @param user
     * @return
     */
    @PostMapping("/user/updateuser")
    public Userreturn updateuserdata(HttpServletRequest request, @RequestBody User user) {
        String username = user.getUsername();
        String newusernumber = user.getUsernumber();
        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");

            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("0".equals(role)) {
                //从token里拿出号码，去查询userid
                String usernumber = tokenInfo.get("usernumber");
                //用电话号码查询userid
                User existuser = oUserm.existuser(usernumber);
                String userid = existuser.getUserid();
                //判断有没有这个账号
                if (!usernumber.equals(newusernumber)) {
                    User userInfo = oUserm.existuser(newusernumber);
                    if (userInfo != null) {
                        return new Userreturn<>(errornotexistuser);
                    }
                }

                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + tokenInfo.get("usernumber"));
                ObjectMapper objectMapper = new ObjectMapper();
                UserRedis userRedis = objectMapper.convertValue(redistoken, UserRedis.class);
                String roottoken = userRedis.getUserRedis();
                if (requesttoken.equals(roottoken)) {
                    Integer updatauserdata = oUserm.updatauserdata(newusernumber, username, userid);
                    if (updatauserdata == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>(noknowerror);
                    }

                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(unknowreason);
        }
    }


    String userrole = "0";
    String noknowerror = "未知错误";
    String unknowreason = "操作失败";
    String erroraccountpassword = "账号或密码不正确";
    String errordata = "用户已注销";
    String dataerror = "数据错误";
    String errornotexistuser = "用户已存在!";
    String unknowserialid = "没有该序列号";
    String errorserialidother = "该设备已被注册";
    String errorserialid = "该设备已被注册";
    String unknowtoken = "Token传入错误";
    String errortypesof = "没有该设备类型";
    String errornotenumberdll = "输入的号码不正确";
    String errornotenull = "该卡号不存在";
    String errornoerror = "未知错误";
    String errornotedata = "数据错误";
    //token失效
    String errornotetoken = "账号登录信息已过期，请重新登录";
    String setnull = "";

}
