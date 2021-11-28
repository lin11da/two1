package com.two.untils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class JWTUtils {

//    public static void main(String[] args) {
//        String token = getToken("ddd123", "123");
//        System.out.println(token);
//        getTokenInfo(token);
//
//        DecodedJWT verify = verify(token);
//        System.out.println(verify);
//    }

    /**
     * 签名
     */
    private static final String SING = "sdsadas——+）##*";


    /**
     * @param usernumber 手机号码
     * @param password   密码
     * @return 返回token
     * instance.getTime() 指定令牌过期时间
     * SING签名  String类型
     */
    public static String getToken(String usernumber, String password, String role) {

        Calendar instance = Calendar.getInstance();
        //默认1天过期
        instance.add(Calendar.DATE, 1);

        String token = JWT.create()
                .withClaim("usernumber", usernumber)
                .withClaim("password", password)
                .withClaim("role", role)
                .withExpiresAt(instance.getTime())
                .sign(Algorithm.HMAC256(SING));

        return token;
    }

    public static DecodedJWT verify(String token) {
        return JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
    }

    /**
     * 获取token里的信息
     *
     * @param token token
     */
    public static Map<String, String> getTokenInfo(String token) {
        try {
            Map<String, String> map = new HashMap<>();
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(SING)).build();
            DecodedJWT verify = jwtVerifier.verify(token);
            System.out.println(verify.getClaim("usernumber").asString() + " token 已使用");
//        System.out.println(verify.getClaim("password").asString());
//        System.out.println(verify.getExpiresAt()+"过期时间");
            map.put("usernumber", verify.getClaim("usernumber").asString());
            map.put("role", verify.getClaim("role").asString());
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
