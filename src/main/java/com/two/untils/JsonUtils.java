package com.two.untils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.two.pojo.AutoDataVO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonUtils {
    /**
     * Java对象序列化为json字符串
     */
    public void testObjtoJson() {
        AutoDataVO autoDataVO = new AutoDataVO();
        autoDataVO.setData("Ok");
        autoDataVO.setDeviceid("1");
        autoDataVO.setProductkey("00");
        //调用静态方法传递要转换的对象
        String toJSONString = JSON.toJSONString(autoDataVO);
        System.out.println(toJSONString);
    }


    public void ListtoJson() {
        List<AutoDataVO> list = new ArrayList<>();
        AutoDataVO autoDataVO = new AutoDataVO();
        autoDataVO.setData("Ok");
        autoDataVO.setDeviceid("1");
        autoDataVO.setProductkey("00");

        AutoDataVO autoDataV1 = new AutoDataVO();
        autoDataV1.setData("NO");
        autoDataV1.setDeviceid("5");
        autoDataV1.setProductkey("66");

        list.add(autoDataV1);
        list.add(autoDataVO);

        //调用静态方法传递要转换的对象  转换后的对象是数组
        String listtoJSONString = JSON.toJSONString(list);

        System.out.println(listtoJSONString);
    }

    /**
     * map转json格式
     */
    public void maptoJson() {
        Map<String, AutoDataVO> map = new HashMap<>();
        AutoDataVO autoDataVO = new AutoDataVO();
        autoDataVO.setData("Ok");
        autoDataVO.setDeviceid("1");
        autoDataVO.setProductkey("00");

        AutoDataVO autoDataV1 = new AutoDataVO();
        autoDataV1.setData("NO");
        autoDataV1.setDeviceid("5");
        autoDataV1.setProductkey("66");

        map.put("one", autoDataVO);
        map.put("two", autoDataV1);

        String maptoJSONString = JSON.toJSONString(map);
        System.out.println(maptoJSONString);
    }

    /**
     * json转java实体类对象
     */
    public void jsontoObj() {
        String jsonString = "{\"data\":\"NO\",\"deviceid\":\"5\",\"productkey\":\"66\"}";
        //传入实体类对象的class
        AutoDataVO autoDataVO = JSON.parseObject(jsonString, AutoDataVO.class);

        System.out.println(autoDataVO);
    }

    /**
     * json 反序列化为 list
     */
    public static AutoDataVO jsontoList(String ListString) {
//        String ListString = "[{\"data\":\"NO\",\"deviceid\":\"5\",\"productkey\":\"66\"},{\"data\":\"Ok\",\"deviceid\":\"1\",\"productkey\":\"00\"}]";

        //传递json字符串 转换为泛型的class对象
        List<AutoDataVO> list = JSON.parseArray(ListString, AutoDataVO.class);

        for (AutoDataVO autoDataVO : list) {
            System.out.println(autoDataVO);
            return autoDataVO;
        }
        return null;
    }

    /**
     * json 转map集合
     */
    public void JsontoMap() {
        String mapString = "{\"one\":{\"data\":\"Ok\",\"deviceid\":\"1\",\"productkey\":\"00\"},\"two\":{\"data\":\"NO\",\"deviceid\":\"5\",\"productkey\":\"66\"}}";
        //直接进行反序列化，map集合没有泛型，泛型没有是不安全的
        //转换后的集合，必须有泛型
        //调用 parseObject ，传递参数 ，TypeReference类型 ，在TypeReference类的泛型中，传递转换后的map集合
        Map<String, AutoDataVO> map = JSON.parseObject(mapString, new TypeReference<Map<String, AutoDataVO>>() {
        });
        for (String key : map.keySet()) {
            System.out.println(key + "==" + map.get(key));
        }
    }

}
