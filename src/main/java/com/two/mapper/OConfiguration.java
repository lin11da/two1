package com.two.mapper;

import com.two.pojo.Configuration;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OConfiguration {
    Configuration getInfoconfiguration();

    Integer insertconfiguration(String sitetitle, String websitedescription, String websitekeywords, String websitelogo
            , String contact, String contactaddress, String websiteregistrationnumber, String announcementoftheinformation
            , String updatetime, String createtime);

    Integer updateconfiguration(int id, String sitetitle, String websitedescription, String websitekeywords, String websitelogo
            , String contact, String contactaddress, String websiteregistrationnumber, String announcementoftheinformation
            , String updatetime);
}
