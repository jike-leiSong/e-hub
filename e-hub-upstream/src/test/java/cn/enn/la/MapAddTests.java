package cn.sl.ehub.upstream;

import cn.hutool.core.lang.ObjectId;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapAddTests {

    @Test
    public void testMapAdd(){
        Map<String, String> cmdData = new LinkedHashMap<>();
        Map<String,String> vppCmdData = Maps.newHashMap();
        Map<String,String> ehCmdData = Maps.newHashMap();
        Map<String,String> desCmdData = Maps.newHashMap();
        cmdData.putAll(vppCmdData);
        cmdData.putAll(ehCmdData);
        cmdData.putAll(desCmdData);
        System.out.println(cmdData.size());

    }
}
