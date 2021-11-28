package com.two.untils;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.two.pojo.AutoDataVO;
import com.two.pojo.Rdata;
import com.two.pojo.UserRedis;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class AllUtils {


    /**
     * json 反序列化为 list
     */
    public static Rdata jsontoList(String ListString) {

        //传递json字符串 转换为泛型的class对象
        List<Rdata> list = JSON.parseArray(ListString, Rdata.class);

        for (Rdata rdata : list) {
            System.out.println(rdata);
            return rdata;
        }
        return null;
    }

    /**
     * 转换 CGSNdata
     * json 反序列化为 list
     */
    public static AutoDataVO CGSNjsontoList(String ListString) {


        //传递json字符串 转换为泛型的class对象
        List<AutoDataVO> list = JSON.parseArray(ListString, AutoDataVO.class);

        for (AutoDataVO cgsNdata : list) {
            System.out.println(cgsNdata);
            return cgsNdata;
        }
        return null;
    }

    /**
     * 去掉字符串出现的字符
     *
     * @param message message
     * @return String
     */
    public static String replaceA(String message) {
        String replace = message.replace("\\", "");
        String replace2 = replace.replace("r", "");
        String replace3 = replace2.replace("n", "");
        return replace3;

    }


}
