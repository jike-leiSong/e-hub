package cn.sl.ehub.common.dto;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @Description: 工业负荷转换类
 * @Author sl
 * @Date 2026-05-28
 * [{"area":"北京","capacity":"1000","innerUserId":"0000000001","owner":"新奥","userType":"type1","username":"新奥数能"},{"area":"北京","capacity":"1200","innerUserId":"0000000002","owner":"新奥","userType":"type2","username":"新奥数能"}]
 */

public class SingleModeIndustrialLoadDTO {

    private String username;

    private String capacity;

    private String area;

    private String userType;

    private String owner;

    private String innerUserId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getInnerUserId() {
        return innerUserId;
    }

    public void setInnerUserId(String innerUserId) {
        this.innerUserId = innerUserId;
    }

    public SingleModeIndustrialLoadDTO() {
    }

    public SingleModeIndustrialLoadDTO(String username, String capacity, String area, String userType, String owner, String innerUserId) {
        this.username = username;
        this.capacity = capacity;
        this.area = area;
        this.userType = userType;
        this.owner = owner;
        this.innerUserId = innerUserId;
    }

    public static void main(String[] args) {
        SingleModeIndustrialLoadDTO singleModeIndustrialLoad1 = new SingleModeIndustrialLoadDTO();
        singleModeIndustrialLoad1.setUsername("新奥数能");
        singleModeIndustrialLoad1.setCapacity("1000");
        singleModeIndustrialLoad1.setArea("北京");
        singleModeIndustrialLoad1.setUserType("type1");
        singleModeIndustrialLoad1.setOwner("新奥");
        singleModeIndustrialLoad1.setInnerUserId("0000000001");

        SingleModeIndustrialLoadDTO singleModeIndustrialLoad2 = new SingleModeIndustrialLoadDTO();
        singleModeIndustrialLoad2.setUsername("新奥数能");
        singleModeIndustrialLoad2.setCapacity("1200");
        singleModeIndustrialLoad2.setArea("北京");
        singleModeIndustrialLoad2.setUserType("type2");
        singleModeIndustrialLoad2.setOwner("新奥");
        singleModeIndustrialLoad2.setInnerUserId("0000000002");

        List<SingleModeIndustrialLoadDTO> detailList = Lists.newArrayList(singleModeIndustrialLoad1, singleModeIndustrialLoad2);

        String s = "[{\"area\":\"北京\",\"capacity\":\"1000\",\"innerUserId\":\"0000000001\",\"owner\":\"新奥\"," +
                "\"userType\":\"type1\",\"username\":\"新奥数能\"},{\"area\":\"北京\",\"capacity\":\"1200\"," +
                "\"innerUserId\":\"0000000002\",\"owner\":\"新奥\",\"userType\":\"type2\",\"username\":\"新奥数能\"}]";
        System.out.println(JSONObject.toJSONString(detailList));
    }
}
