package com.two.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.two.mapper.*;
import com.two.pojo.*;
import com.two.untils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@CrossOrigin
@RestController
public class ORoot {
    @Autowired
    OUserm oUserm;

    @Autowired
    ORootm oRootm;

    @Autowired
    OEquipment oEquipment;

    @Autowired
    Redisutils redisutils;

    @Autowired
    OSerial oSerial;

    @Autowired
    OProject oProject;


    String success = "操作成功";
    String unknowreason = "操作失败";
    String erroraccountpassword = "账号或密码不正确";
    String errordata = "用户已注销";
    String dataerror = "未知错误";
    String errornotexistuser = "用户不存在!";
    String unknowtoken = "Token传入错误";
    String errornotenumber = "该电话号码已被注册";
    String errorequipmentno = "用户未注册设备";
    String errornotenumberdll = "输入的号码不正确";
    String errornotenull = "参数不能为空";
    //token失效
    String errornotetoken = "账号登录信息已过期，请重新登录";
    String errornoteverificationcode = "验证码错误";
    String errornoknow = "未知错误";
    String errornotenoequipmenttypesof = "没有该类型设备";
    String errornotdatanull = "数据不能为空";
    String errornotedata = "数据错误";

    /**
     * 未完成
     * 管理员登录
     *
     * @param root json
     * @return msg
     */
    @PostMapping("/root/login")
    public Userreturn loginuser(@RequestBody Root root) {
        String usernumber = root.getUsernumber();
        String password = root.getPassword();
        Map<String, String> map = new LinkedHashMap<>();
        RootRedis rootRedis = new RootRedis();
        Root existuser = oRootm.loginroot(usernumber, password);
        if (existuser == null) {
            return new Userreturn<>(erroraccountpassword);
        }


        //执行登录方法，如果没有异常就可以了
        try {
            //查看用户是否已经注销
            Root existroot = oRootm.existroot(usernumber);
            String role = existroot.getAuthority();
            String usertoken = JWTUtils.getToken(usernumber, password, role);
            String username = existroot.getUsername();
            String authority = existroot.getAuthority();

            String rediskey = "root_" + usernumber;
            //将数据库数据存入redis
            rootRedis.setRoottoken(usertoken);
            rootRedis.setAccountid(existuser.getAccoutid());
            //永久储存
            boolean set = redisutils.set(rediskey, rootRedis);

            System.out.println("root" + usernumber + " 数据存入redis：  " + set);
            map.put("role", authority);
            map.put("username", username);
            map.put("token", usertoken);
            System.out.println(username + "已登录");
            return new Userreturn<>(map);
        } catch (Exception e) {   //用户名不存在
            e.printStackTrace();
            System.out.println("root: 用户名错误");
            return new Userreturn<>(erroraccountpassword);
        }
    }

    /**
     * 管理员添加设备
     *
     * @param addAllequipment addAllequipment
     * @param request         HttpServletRequest  request
     * @return Userreturn
     */
    @PostMapping("root/addequipment")
    public Userreturn addequipment(@RequestBody AddAllequipment addAllequipment, HttpServletRequest request) {

        //当前时间戳
        long updatatime = System.currentTimeMillis();
        long createtime = System.currentTimeMillis();

        String secretid = addAllequipment.getSecretid();


        String serialid = addAllequipment.getSerialid();

        //设备id  平台获取
        String equipmentid = addAllequipment.getEquipmentid();

        //前端自己装换为数字
        int typesof = addAllequipment.getTypesof();

        //判断数据是否为null
        if (secretid == null || "".equals(secretid) || serialid == null || "".equals(serialid) || equipmentid == null || "".equals(equipmentid)) {
            return new Userreturn<>(errornotenull);
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
            if ("1".equals(role) || "2".equals(role)) {
                String onlyid = Utils.RandomStr(16);
                String usernumber = tokenInfo.get("usernumber");
                //把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (roottoken.equals(requesttoken)) {
                    //判断有没有该类型设备
                    Typesofname equipmenttypesof = oEquipment.equipmenttypesof(typesof);
                    if (equipmenttypesof == null) {
                        return new Userreturn<>(errornotenoequipmenttypesof);
                    }

                    AddAllequipment equipmentidselect = oRootm.equipmentidselect(equipmentid);
                    AddAllequipment equipmentserialid = oRootm.equipmentserialid(serialid);
                    if (equipmentidselect != null) {
                        return new Userreturn<>("设备号重复，请勿重复添加");
                    } else if (equipmentserialid != null) {
                        return new Userreturn<>("序列号重复，请勿重复添加");
                    }
                    Integer addequipment = oRootm.addequipment(onlyid, equipmentid, secretid, serialid, typesof, "1", String.valueOf(updatatime), String.valueOf(createtime));
                    if (addequipment == 1) {
                        return new Userreturn<>();
                    }
                    System.out.println("root 添加设备");
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(unknowreason);      //返回操作失败

        }
        return null;
    }


    /**
     * 根据电话号码添加管理员
     *
     * @param request
     * @param user
     * @return
     */
    @PostMapping("/root/addspecify")
    public Userreturn addspecify(HttpServletRequest request, @RequestBody User user) {
        //当前时间戳
        long updatatime = System.currentTimeMillis();
        long createtime = System.currentTimeMillis();
        try {
            String userUsernumber = user.getUsernumber();
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }

            Root rootInfo = oRootm.existroot(userUsernumber);
            if (rootInfo != null) {
                return new Userreturn<>(errornotexistuser);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                //把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    String username = user.getUsername();
                    String rootnumber = user.getUsernumber();
                    Root existroot = oRootm.existroot(rootnumber);
                    if (existroot != null) {
                        return new Userreturn<>("用户已存在");
                    }
                    String password = user.getPassword();
                    String userid = Utils.RandomStr(16);
                    String authority = "1";
                    String rootdata = "1";
                    Integer updateusertoroot = oRootm.updateusertoroot(userid, username, rootnumber, password, authority, String.valueOf(updatatime), String.valueOf(createtime));
                    if (updateusertoroot == 1) {
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
            return new Userreturn<>(unknowreason);
        }
        return new Userreturn<>(unknowreason);
    }

    /**
     * 添加超级管理员
     *
     * @param root
     * @return
     */
    @PostMapping("/super/addsupersuperrootyyds")
    public Userreturn addsuperroot(Root root) {
        //当前时间戳
        long updatatime = System.currentTimeMillis();
        long createtime = System.currentTimeMillis();
        String rootusernumber = root.getUsernumber();
        String authority = root.getAuthority();
        String password = root.getPassword();
        String username = root.getUsername();
        String randomStr = Utils.RandomStr(18);
        try {
            Root existroot = oRootm.existroot(rootusernumber);
            if (existroot != null) {
                return new Userreturn<>("用户已存在");
            }
            Integer addsuperroot = oRootm.addsuperroot(randomStr, username, rootusernumber, password, "", authority, String.valueOf(updatatime), String.valueOf(createtime));
            if (addsuperroot == 1) {
                return new Userreturn();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(unknowreason);
        }
        return new Userreturn<>(unknowreason);
    }


    /**
     * 删除超级管理员或者其他管理员
     *
     * @param root
     * @return
     */
    @PostMapping("/super/deletesupersuperrppt")
    public Userreturn deletesuper(Root root) {
        String rootusernumber = root.getUsernumber();

        try {
            Root existroot = oRootm.existroot(rootusernumber);
            if (existroot != null) {
                return new Userreturn<>("用户已存在");
            }
            Integer deletesuperroot = oRootm.deletesuperroot(rootusernumber);
            if (deletesuperroot == 1) {
                return new Userreturn<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(unknowreason);
        }
        return new Userreturn<>(unknowreason);
    }


    /**
     * 管理员查看自己的信息
     *
     * @param request
     * @return
     */
    @PostMapping("/root/selectroot")
    public Userreturn selectrootdata(HttpServletRequest request) {

        Map<String, String> map = new HashMap<>();
        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");

            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {
                //从token里拿出号码，去查询userid
                String usernumber = tokenInfo.get("usernumber");
                Root existroot = oRootm.existroot(usernumber);
                String userid = existroot.getUserid();

                //把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {

                    Root selectroot = oRootm.selectroot(userid);
                    if (selectroot == null) {
                        return new Userreturn<>(errornoknow);
                    }
                    String accoutid = selectroot.getAccoutid();
                    String authority = selectroot.getAuthority();
                    String username = selectroot.getUsername();
                    map.put("acid", accoutid);
                    map.put("authority", authority);
                    map.put("usernumber", usernumber);
                    map.put("userid", userid);
                    map.put("username", username);

                    return new Userreturn<>(map);

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

    /**
     * 管理员修改自己的信息
     *
     * @param request
     * @param root
     * @return
     */
    @PostMapping("/root/updateroot")
    public Userreturn updaterootdata(HttpServletRequest request, @RequestBody Root root) {
        String username = root.getUsername();
        String newusernumber = root.getUsernumber();
        Map<String, String> map = new HashMap<>();
        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");

            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {
                //从token里拿出号码，去查询userid
                String usernumber = tokenInfo.get("usernumber");
                Root existroot = oRootm.existroot(usernumber);
                String userid = existroot.getUserid();

                //把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    Integer updatarootdata = oRootm.updatarootdata(newusernumber, username, userid);
                    if (updatarootdata == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>(errornoknow);
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


    /**
     * 管理员修改自己的密码
     *
     * @param request HttpServletRequest request
     * @param root    root
     * @return Userreturn
     */
    @PostMapping("/root/updatarootpassword")
    public Userreturn updatapassowrd(HttpServletRequest request, @RequestBody Root root) {
        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                String password = root.getPassword();

                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    //判断密码是否为空
                    if (password == null || "".equals(password)) {
                        return new Userreturn<>(errornotdatanull);
                        //最少输入六位密码
                    } else if (password.length() >= 6) {
                        if (password.length() > 16) {
                            return new Userreturn<>("密码长度超过16位");
                        }
                    } else {
                        return new Userreturn<>("密码长度太短");
                    }
                    Integer updatarootpassword = oRootm.updatarootpassword(usernumber, password);
                    if (updatarootpassword == 1) {
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
     * 管理员修改自己的name
     *
     * @param request HttpServletRequest request
     * @param root    root
     * @return Userreturn
     */
    @PostMapping("/root/updatarootname")
    public Userreturn updatarootnumber(HttpServletRequest request, @RequestBody Root root) {
        try {
            //从请求参数拿出token并解析出number
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                String username = root.getUsername();
                if ("".equals(username) || username == null) {
                    return new Userreturn<>(errornotdatanull);
                }
                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    Integer updatarootpassword = oRootm.updatarootnumber(usernumber, username);
                    if (updatarootpassword == 1) {
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
     * 查询全部用户
     *
     * @param request HttpServletRequest request
     * @return Userreturn
     */
    @PostMapping("/root/selectAlluser")
    public Userreturn selectAlluser(HttpServletRequest request, @RequestBody Allequipment allequipment) {
        String requesttoken = request.getHeader("token");
        Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
        int pages = allequipment.getPages();
        Map<String, Integer> paramMap = new HashMap<>();
        if (tokenInfo == null) {
            return new Userreturn<>(unknowtoken);
        }
        try {
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                LinkedHashMap<String, Object> hashMap = new LinkedHashMap<>();

                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {

                    paramMap.put("startIndex", 6 * (pages - 1));
                    paramMap.put("pageSize", 6 + (6 * (pages - 1)));
                    List<AllUser> allUsers = oRootm.selectAlluser(paramMap);

                    for (AllUser allUser : allUsers) {
                        String userid = allUser.getUserid();
                        Integer selectuserhaveequipment = oRootm.selectuserhaveequipment(userid);

                        allUser.setQuantity(String.valueOf(selectuserhaveequipment));
                    }
                    //查询全部用户数量 data=1
                    Integer total = oRootm.selectusertotal();
                    if (allUsers == null) {
                        return new Userreturn<>("暂无用户");
                    }
                    hashMap.put("total", total);
                    hashMap.put("users", allUsers);
                    return new Userreturn<>(hashMap);
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new Userreturn<>(dataerror);
        }
    }

    /**
     * 查看用户设备详细
     *
     * @param request HttpServletRequest request
     * @param allUser usernumber
     * @return Userreturn
     */
    @PostMapping("/root/selectUser")
    public Userreturn selectUser(HttpServletRequest request, @RequestBody AllUser allUser) {
        String userid = allUser.getUserid();
        Map<String, Object> hashMap = new LinkedHashMap<>();

        try {
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    //查询用户信息
                    User userInfo = oUserm.getUserInfo(userid);
                    if (userInfo == null) {
                        return new Userreturn<>(errornotexistuser);
                    }
                    List<SelectuserEquipment> selectuserEquipments = oRootm.selectUserEquipment(userid);
                    if (selectuserEquipments == null) {
                        return new Userreturn<>(errorequipmentno);
                    }
                    hashMap.put("userequipment", selectuserEquipments);
                    return new Userreturn<>(hashMap);

                } else {
                    return new Userreturn<>(0.0);
                }

            } else {
                return new Userreturn<>(0);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new Userreturn<>(dataerror);
        }
    }

    /**
     * 管理员修改用户设备信息
     *
     * @param request         HttpServletRequest request
     * @param addAllequipment serialid equipmentname
     * @return
     */
    @PostMapping("/root/updateequipmentdata")
    public Userreturn rootupdateuserequipment(HttpServletRequest request, @RequestBody AddAllequipment addAllequipment) {

        String serialid = addAllequipment.getSerialid();
        String equipmentname = addAllequipment.getEquipmentname();
        int typesof = addAllequipment.getTypesof();
        String userid = addAllequipment.getUserid();


        //当前时间戳
        long updatatime = System.currentTimeMillis();

        try {
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {

                //判断字符长度
                if (equipmentname.length() > 12) {
                    return new Userreturn<>("设备名称长度最高为12位字符");
                }
                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    //查询用户信息
                    User userInfo = oUserm.getUserInfo(userid);
                    if (userInfo == null) {
                        return new Userreturn<>(errornotexistuser);
                    }

                    Integer rootupdateuserequipment = oRootm.rootupdateuserequipment(userid, serialid, equipmentname, typesof, String.valueOf(updatatime));
                    if (rootupdateuserequipment == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>(dataerror);
                    }
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(dataerror);
        }
    }


    /**
     * 查看某设备下的阈值
     *
     * @param request
     * @param threshold
     * @return
     */
    @PostMapping("/root/selectuserthreshold")
    public Userreturn rootselectuserthreshold(HttpServletRequest request, @RequestBody Threshold threshold) {
        String serialid = threshold.getSerialid();
        String userid = threshold.getUserid();
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    ReturnThreshold rootselectuserthreshold = oRootm.rootselectuserthreshold(serialid);
                    if (rootselectuserthreshold == null) {
                        System.out.println("管理员查看用户 " + serialid + " 设定的阈值");
                        return new Userreturn<>("用户未设定阈值");
                    }
                    map.put("userthreshold", rootselectuserthreshold);
                    return new Userreturn<>(map);
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(dataerror);
        }

    }


    /**
     * 管理员修改用户阈值
     *
     * @param request   HttpServletRequest request
     * @param temAndHum userid serialid humidity temperature illumination
     * @return Userreturn
     */
    @PostMapping("/root/updatauserthreshold")
    public Userreturn rootupdatauserthreshiold(HttpServletRequest request, @RequestBody TemAndHum temAndHum) {
        String userid = temAndHum.getUserid();
        String serialid = temAndHum.getSerialid();
        String conductivity = temAndHum.getConductivity();
        String co2 = temAndHum.getCo2();
        String watertemp = temAndHum.getWatertemp();
        String humidity = String.valueOf(temAndHum.getHumidity());
        String temperature = String.valueOf(temAndHum.getTemperature());
        String illumination = String.valueOf(temAndHum.getIllumination());

        boolean pattern = Utils.pattern(humidity, temperature, illumination, co2, watertemp, conductivity);
        if (!pattern) {
            return new Userreturn<>(errornotedata);
        }


        //当前时间戳
        long updatatime = System.currentTimeMillis();
        try {
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    Integer rootupdatauserthreshiold = oRootm.rootupdatauserthreshiold(userid, serialid, illumination, temperature, humidity, String.valueOf(updatatime));
                    if (rootupdatauserthreshiold == 1) {
                        System.out.println("管理员已修改用户阈值");
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
            return new Userreturn<>(dataerror);
        }
        return new Userreturn<>(dataerror);
    }

    /**
     * 管理员修改用户全部信息
     *
     * @param request
     * @param user
     * @return
     */
    @PostMapping("/root/updateuserinformation")
    public Userreturn rootupdateuserinformation(HttpServletRequest request, @RequestBody User user) {

        //当前时间戳
        long updatetime = System.currentTimeMillis();

        String userid = user.getUserid();
        String password = user.getPassword();
        String username = user.getUsername();
        String userusernumber = user.getUsernumber();
        try {
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }


            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                if (!usernumber.equals(userusernumber)) {
                    User userInfo = oUserm.existuser(userusernumber);
                    if (userInfo != null) {
                        return new Userreturn<>(errornotexistuser);
                    }
                }
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    Integer rootupdatauserpassword = oRootm.rootupdatauserinformation(userid, username, userusernumber, password, String.valueOf(updatetime));
                    if (rootupdatauserpassword == 1) {
                        System.out.println("管理员已修改用户信息");
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
            return new Userreturn<>(dataerror);
        }
        return new Userreturn<>(dataerror);
    }

    /**
     * 管理员查询全部设备
     *
     * @param request
     * @return
     */
    @PostMapping("root/selectallequipment")
    public Userreturn Allequipment(HttpServletRequest request, @RequestBody Allequipment allequipment) {
        try {
            Map<String, Object> map = new HashMap<>();
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            Map<String, Integer> paramMap = new HashMap<>();
            int pages = allequipment.getPages();
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    paramMap.put("startIndex", 8 * (pages - 1));
                    paramMap.put("pageSize", 8 + (8 * (pages - 1)));
                    List<SelectAllequipment> allequipemnt = oRootm.Allequipemnt(paramMap);
                    if (allequipemnt == null) {
                        return new Userreturn<>("管理员还没有注册设备");
                    }
                    Integer selectallequipment = oRootm.selectallequipment();
                    map.put("total", selectallequipment);
                    map.put("equipment", allequipemnt);
                    return new Userreturn<>(map);
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Userreturn<>(dataerror);
    }

    /**
     * 管理员从全部设备里删除设备
     *
     * @param request
     * @return
     */
    @PostMapping("/root/dequipment")
    public Userreturn delectequipment(HttpServletRequest request, @RequestBody Allequipment allequipment) {
        try {
            Map<String, List<SelectAllequipment>> map = new HashMap<>();
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            String onlyid = allequipment.getOnlyid();
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    Integer delectequipemnt = oRootm.delectequipemnt(onlyid);
                    if (delectequipemnt != 0) {
                        System.out.println("删除了 " + delectequipemnt + " 条数据");
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>("没有可删除的数据");
                    }
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(errornoknow);
        }
    }

    /**
     * 管理员更新设备信息
     *
     * @param request
     * @param allequipment
     * @return
     */
    @PostMapping("/root/updateallequipment")
    public Userreturn updateequipment(HttpServletRequest request, @RequestBody Allequipment allequipment) {
        try {

            //当前时间戳
            long updatatime = System.currentTimeMillis();
            Map<String, List<SelectAllequipment>> map = new HashMap<>();
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            String serialid = allequipment.getSerialid();
            String equipmentid = allequipment.getEquipmentid();
            String secretid = allequipment.getSecretid();
            String typesof = allequipment.getTypesof();
            String onlyid = allequipment.getOnlyid();

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    //修改设备信息
                    Integer updateequipment = oRootm.updateequipment(onlyid, equipmentid, secretid, serialid, typesof, String.valueOf(updatatime));
                    if (updateequipment == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>(errornoknow);
                    }
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(errornoknow);
        }
    }

    /**
     * 查看所有管理员
     *
     * @param request
     * @param allequipment
     * @return
     */
    @PostMapping("/root/selectAllroot")
    public Userreturn selectAllroot(HttpServletRequest request, @RequestBody Allequipment allequipment) {
        try {
            int pages = allequipment.getPages();
            Map<String, Integer> paramMap = new HashMap<>();
            Map<String, Object> map = new HashMap<>();
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    paramMap.put("startIndex", 6 * (pages - 1));
                    paramMap.put("pageSize", 6 + (6 * (pages - 1)));
                    List<Root> roots = oRootm.selectAllroot(paramMap);

                    Integer integer = oRootm.selectAllrootpags();
                    map.put("root", roots);
                    map.put("total", integer);
                    return new Userreturn<>(map);

                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(errornoknow);
        }

    }

    /**
     * superroot删除管理员
     *
     * @param request
     * @param allequipment
     * @return
     */
    @PostMapping("/root/delectRoot")
    public Userreturn delectroot(HttpServletRequest request, @RequestBody Allequipment allequipment) {
        try {
            String userid = allequipment.getUserid();

            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("2".equals(role)) {
                Root rootInfo = oRootm.getRootInfo(userid);
                if (rootInfo == null) {
                    return new Userreturn<>("用户已不存在");
                }
                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    Integer delectroot = oRootm.delectroot(userid);
                    if (delectroot == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>("数据异常,请刷新数据");
                    }
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(errornoknow);
        }

    }


    /**
     * 管理员删除用户  逻辑删除  data = 0
     *
     * @param request
     * @param allequipment
     * @return
     */
    @PostMapping("/root/deleteUser")
    public Userreturn deleteuser(HttpServletRequest request, @RequestBody Allequipment allequipment) {
        try {
            String userid = allequipment.getUserid();

            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {
                User userInfo = oUserm.getUserInfo(userid);
                if (userInfo == null) {
                    return new Userreturn<>("用户已不存在");
                }
                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    Integer deleteuser = oRootm.deleteuser(userid);
                    Integer delectequipment = oUserm.delectequipment(userid);
                    if (deleteuser == 1&&delectequipment>0) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>("数据异常,请刷新数据");
                    }
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(errornoknow);
        }

    }

    /**
     * 管理员查看用户登录日志
     *
     * @param allequipment
     * @param request
     * @return
     */
    @PostMapping("/root/selectlog")
    public Userreturn selectuserlog(@RequestBody Allequipment allequipment, HttpServletRequest request) {
        try {
            String userid = allequipment.getUserid();
            int pages = allequipment.getPages();
            int startIndex = 0;
            int pageSize = 0;

            startIndex = 6 * (pages - 1);
            pageSize = 6 + (6 * (pages - 1));

            Map<String, Object> map = new HashMap<>();

            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);
            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("2".equals(role) || "1".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    Integer userloginlogtotal = oRootm.userloginlogtotal(userid);
                    List<Userloginlog> userloginlog = oRootm.userloginlog(userid, startIndex, pageSize);
                    if (userloginlog == null) {
                        return new Userreturn<>("该用户还没有登录日志");
                    }
                    map.put("total", userloginlogtotal);
                    map.put("log", userloginlog);
                    return new Userreturn<>(map);
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(errornoknow);
        }
    }

    /**
     * 管理员删除用户的设备 物理删除
     *
     * @param request
     * @param allequipment
     * @return
     */
    @PostMapping("/root/deleteUserquipment")
    public Userreturn deleteuserequipment(HttpServletRequest request, @RequestBody Allequipment allequipment) {
        try {
            String userid = allequipment.getUserid();
            String serialid = allequipment.getSerialid();
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {
                SelectuserEquipment userInfo = oRootm.serialidanduseridselect(userid, serialid);
                if (userInfo == null) {
                    return new Userreturn<>("用户该设备已不存在");
                }
                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    Integer delectthreshold = oProject.delectthreshold(serialid, userid);
                    Integer delectuserequipemnt = oRootm.delectuserequipemnt(userid, serialid);
                    Integer delectuserequipemnttoproject = oRootm.delectuserequipemnttoproject(serialid);
                    Integer deletesenserid = oSerial.deletesenserid(serialid);
                    if (delectuserequipemnt == 1 && delectuserequipemnttoproject == 1 && deletesenserid > 0 && delectthreshold == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>("数据异常,请刷新数据");
                    }
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(errornoknow);
        }

    }


    /**
     * 管理员查看用户信息
     *
     * @param request
     * @param allequipment
     * @return
     */
    @PostMapping("/root/selectuserdata")
    public Userreturn selectuserdata(HttpServletRequest request, @RequestBody Allequipment allequipment) {
        try {
            String userid = allequipment.getUserid();

            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    User selectuserdata = oRootm.selectuserdatauserid(userid);
                    if (selectuserdata == null) {
                        return new Userreturn<>("userid传输错误");
                    }
                    return new Userreturn<>(selectuserdata);
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(errornoknow);
        }

    }


    /**
     * 管理员查询报警信息
     *
     * @return
     */
    @PostMapping("/root/selectalarm")
    public Userreturn selectalarm(HttpServletRequest request, @RequestBody AddAllequipment addAllequipment) {

        String starttime = addAllequipment.getStarttime();
        String endtime = addAllequipment.getEndtime();

        Date date = new Date();
        //如果输入的时间为null  就自己生产时间Data
        if ("".equals(starttime) || starttime == null || "".equals(endtime) || endtime == null) {
            starttime = "2020-08-01 12:12:00:000";
            endtime = TimeData.DateToString(date);
        }
        try {
            String stringstarttime = TimeData.StringToTimestamp(starttime);
            String stringendtime = TimeData.StringToTimestamp(endtime);

            //查询datalog表  去重  ，拿到serialid
            List<ProjectData> selectdatalog = oProject.selectdatalog(stringstarttime, stringendtime);

            List<Map> list = new ArrayList<>();


            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
////                查看有多少个已注册设备
//                Integer userequipmenttotal = oRootm.userequipmenttotal(stringstarttime,stringendtime);
                //token

                if (requesttoken.equals(roottoken)) {
                    //datalog遍历出来  根据时间来做分页
                    for (ProjectData projectData : selectdatalog) {

                        String serialid = projectData.getSerialid();


                        //用serialid查询项目id
                        ProjectData selectprojectidtodata = oProject.selectprojectidtodata(serialid);
                        if (selectprojectidtodata == null) {
                            continue;
                        }
                        String projectid = selectprojectidtodata.getProjectid();

                        ProjectData projectInfo = oProject.getProjectInfo(projectid);
                        //项目负责人
                        String projectleader = projectInfo.getProjectleader();

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
                                co2map.put("projectleader", projectleader);
                                co2map.put("serialid", serialid);
                                co2map.put("total", co2alarm);
                                co2map.put("alarmdata", "co2浓度过高");
                                co2map.put("information", co2);
                                co2map.put("time", createtime);
                                co2map.put("sensorid", co2sensorid);

                                list.add(co2map);
                            }
                        } catch (Exception e) {

                        }

                        try {
                            Integer conductivityalarm = oSerial.selectconductivityalarm(conductivitysensorid);
                            if (conductivityalarm > 0) {


                                Map<String, Object> conductivitymap = new HashMap<>();

                                //获取最新的时间 conductivityalarm
                                AllDatalog alarmmaxtimetoconductivitytime = oSerial.alarmmaxtimetoconductivitytime(serialid);
                                String createtime = alarmmaxtimetoconductivitytime.getCreatetime();
                                //获取最大的值 conductivityalarm
                                AllDatalog alarmmaxtimetoconductivity = oSerial.alarmmaxtimetoconductivity(createtime);
                                double conductivity = alarmmaxtimetoconductivity.getConductivity();


                                conductivitymap.put("equipmentname", equipmentname);
                                conductivitymap.put("projectleader", projectleader);
                                conductivitymap.put("serialid", serialid);
                                conductivitymap.put("total", conductivityalarm);
                                conductivitymap.put("alarmdata", "导电率过高");
                                conductivitymap.put("information", conductivity);
                                conductivitymap.put("time", createtime);
                                conductivitymap.put("sensorid", conductivitysensorid);
                                list.add(conductivitymap);
                            }
                        } catch (Exception e) {

                        }


                        try {
                            Integer tempalarm = oSerial.selecttempalarm(tempsensorid);
                            if (tempalarm > 0) {

                                Map<String, Object> tempmap = new HashMap<>();

                                //获取最新的时间 tempalarm
                                AllDatalog tempmaxtime = oSerial.alarmmaxtimetotemptime(serialid);
                                String createtime = tempmaxtime.getCreatetime();
                                //获取最大的值 tempalarm
                                AllDatalog tempdata = oSerial.alarmmaxtimetotemp(createtime);
                                double temp = tempdata.getCo2();

                                tempmap.put("equipmentname", equipmentname);
                                tempmap.put("projectleader", projectleader);
                                tempmap.put("serialid", serialid);
                                tempmap.put("total", tempalarm);
                                tempmap.put("alarmdata", "温度过高");
                                tempmap.put("information", temp);
                                tempmap.put("time", createtime);
                                tempmap.put("sensorid", tempsensorid);
                                list.add(tempmap);
                            }
                        } catch (Exception e) {

                        }


                        try {
                            Integer illuminancealarm = oSerial.selectilluminancealarm(illuminancesensorid);
                            if (illuminancealarm > 0) {

                                Map<String, Object> illuminancemap = new HashMap<>();
                                //获取最新的时间 illuminancealarm
                                AllDatalog alarmmaxtimetoilluminancetime = oSerial.alarmmaxtimetoilluminancetime(serialid);
                                String createtime = alarmmaxtimetoilluminancetime.getCreatetime();
                                //获取最大的值 illuminancealarm
                                AllDatalog alarmmaxtimetoilluminance = oSerial.alarmmaxtimetoilluminance(createtime);
                                double illuminance = alarmmaxtimetoilluminance.getIlluminance();

                                illuminancemap.put("equipmentname", equipmentname);
                                illuminancemap.put("projectleader", projectleader);
                                illuminancemap.put("serialid", serialid);
                                illuminancemap.put("total", illuminancealarm);
                                illuminancemap.put("alarmdata", "光照强度过高");
                                illuminancemap.put("information", illuminance);
                                illuminancemap.put("time", createtime);
                                illuminancemap.put("sensorid", illuminancesensorid);

                                list.add(illuminancemap);
                            }
                        } catch (Exception e) {

                        }


                        try {


                            Integer humialarm = oSerial.selecthumialarm(humisensorid);
                            if (humialarm > 0) {


                                Map<String, Object> humimap = new HashMap<>();
                                //获取最新的时间 humialarm
                                AllDatalog alarmmaxtimetohumitime = oSerial.alarmmaxtimetohumitime(serialid);
                                String createtime = alarmmaxtimetohumitime.getCreatetime();
                                //获取最大的值 humialarm
                                AllDatalog alarmmaxtimetohumi = oSerial.alarmmaxtimetohumi(createtime);
                                double humi = alarmmaxtimetohumi.getHumi();


                                humimap.put("equipmentname", equipmentname);
                                humimap.put("projectleader", projectleader);
                                humimap.put("serialid", serialid);
                                humimap.put("total", humialarm);
                                humimap.put("alarmdata", "湿度过高");
                                humimap.put("information", humi);
                                humimap.put("time", createtime);
                                humimap.put("sensorid", humisensorid);

                                list.add(humimap);
                            }
                        } catch (Exception e) {

                        }


                        try {
                            Integer watertempalarm = oSerial.selectwatertempalarm(watertempsensorid);
                            if (watertempalarm > 0) {


                                Map<String, Object> watertempmap = new HashMap<>();
                                //获取最新的时间 watertempalarm
                                AllDatalog alarmmaxtimetowatertemptime = oSerial.alarmmaxtimetowatertemptime(serialid);
                                String createtime = alarmmaxtimetowatertemptime.getCreatetime();
                                //获取最大的值 watertempalarm
                                AllDatalog alarmmaxtimetowatertemp = oSerial.alarmmaxtimetowatertemp(createtime);
                                double watertemp = alarmmaxtimetowatertemp.getWatertemp();


                                watertempmap.put("equipmentname", equipmentname);
                                watertempmap.put("projectleader", projectleader);
                                watertempmap.put("serialid", serialid);
                                watertempmap.put("total", watertempalarm);
                                watertempmap.put("alarmdata", "水温过高");
                                watertempmap.put("information", watertemp);
                                watertempmap.put("time", createtime);
                                watertempmap.put("sensorid", watertempsensorid);

                                list.add(watertempmap);
                            }
                        } catch (Exception e) {

                        }

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
            return new Userreturn<>(errornoknow);
        }
    }

    @PostMapping("/root/delectbysenserid")
    public Userreturn deletesenserid(@RequestBody Delectbyonlyid delectbyonlyid, HttpServletRequest request) {
        try {
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    String[] senserid = delectbyonlyid.getSenserid();
                    int i = 0;
                    for (String allsenserid : senserid) {
                        Integer delectco2alarm = oSerial.delectco2alarm(allsenserid);
                        if (delectco2alarm > 0) {
                            i++;
                        }
                        Integer delecthumidityalarm = oSerial.delecthumidityalarm(allsenserid);
                        if (delecthumidityalarm > 0) {
                            i++;
                        }
                        Integer delecttwoconductivityalarm = oSerial.delecttwoconductivityalarm(allsenserid);
                        if (delecttwoconductivityalarm > 0) {
                            i++;
                        }
                        Integer delecttemperatureaalarm = oSerial.delecttemperatureaalarm(allsenserid);
                        if (delecttemperatureaalarm > 0) {
                            i++;
                        }
                        Integer delecttwowatertempalarm = oSerial.delecttwowatertempalarm(allsenserid);
                        if (delecttwowatertempalarm > 0) {
                            i++;
                        }
                        Integer delectilluminationalarm = oSerial.delectilluminationalarm(allsenserid);
                        if (delectilluminationalarm > 0) {
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
            return new Userreturn(errornoknow);
        }
        return new Userreturn<>("数据异常");
    }


    /**
     * 管理员修改用户阈值
     *
     * @param request   HttpServletRequest request
     * @param threshold serialid illumination humidity temperature
     * @return
     */
    @PostMapping("/root/updateEquipmentThreshold")
    public Userreturn updateEquipmentThreshold(HttpServletRequest request, @RequestBody Threshold threshold) {
        //当前时间戳
        long updatatime = System.currentTimeMillis();
        String serialid = threshold.getSerialid();
        String illumination = threshold.getIllumination();
        String humidity = threshold.getHumidity();
        String temperature = threshold.getTemperature();
        String co2 = threshold.getCo2();
        String watertemp = threshold.getWatertemp();
        String conductivity = threshold.getConductivity();
        String userid = threshold.getUserid();
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
            if ("1".equals(role) || "2".equals(role)) {
                String usernumber = tokenInfo.get("usernumber");
                User existuser = oUserm.existuser(usernumber);


                //从redis拿出数据，把Obj转实体类对象
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    //查看序列号和userid是否绑定了，绑定了说明有了阈值
                    Threshold existuserthreshold = oUserm.existuserthreshold(userid, serialid);
                    if (existuserthreshold == null) {
                        Integer integer = oUserm.insertuserEquipment(userid, serialid, illumination, temperature, humidity, co2, watertemp, conductivity, String.valueOf(updatatime), String.valueOf(updatatime));
                        if (integer == 1) {
                            return new Userreturn<>();
                        } else {
                            return new Userreturn<>(errornoknow);
                        }
                    } else {
                        Integer integer = oUserm.updateuserEquipmentthreshold(serialid, illumination, temperature, humidity, co2, watertemp, conductivity, String.valueOf(updatatime));
                        if (integer == 1) {
                            return new Userreturn<>();
                        } else {
                            return new Userreturn<>(errornoknow);
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
            return new Userreturn<>(errornoknow);
        }
    }

    /**
     * 管理员禁用设备
     *
     * @param request
     * @param projectData
     * @return
     */
    @PostMapping("/root/disableequipment")
    public Userreturn disable(HttpServletRequest request, @RequestBody ProjectData projectData) {
        try {
            String serialid = projectData.getSerialid();
            int disable = projectData.getDisable();
            String stringtime = Utils.stringtime();
            String requesttoken = request.getHeader("token");
            Map<String, String> tokenInfo = JWTUtils.getTokenInfo(requesttoken);

            if (tokenInfo == null) {
                return new Userreturn<>(unknowtoken);
            }
            //查看是否有权限
            String role = tokenInfo.get("role");
            if ("1".equals(role) || "2".equals(role)) {

                String usernumber = tokenInfo.get("usernumber");
                Object redistoken = redisutils.get("root_" + usernumber);
                ObjectMapper objectMapper = new ObjectMapper();
                RootRedis rootRedis = objectMapper.convertValue(redistoken, RootRedis.class);
                String roottoken = rootRedis.getRoottoken();
                if (requesttoken.equals(roottoken)) {
                    Integer disableequipment = oRootm.disableequipment(stringtime, serialid, disable);
                    if (disableequipment == 1) {
                        return new Userreturn<>();
                    } else {
                        return new Userreturn<>("数据异常");
                    }
                } else {
                    return new Userreturn<>(0.0);
                }
            } else {
                return new Userreturn<>(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Userreturn<>(errornoknow);
        }

    }
}
