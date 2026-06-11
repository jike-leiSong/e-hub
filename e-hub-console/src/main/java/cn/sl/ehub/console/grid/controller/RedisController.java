package cn.sl.ehub.console.grid.controller;

import cn.sl.ehub.common.utils.RedisUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description:
 * @author sl
 * @email: ouyushan@hotmail.com
 * @date 2026-05-28
 */
@RestController
@Api(tags = "redis测试接口")
@Slf4j
public class RedisController {

    @Resource
    private RedisUtil redisUtil;

    @GetMapping("/set")
    @ApiOperation(value = "设置字段")
    public void redisSet(String value) {
        redisUtil.set("gps:value", value);
    }

    @GetMapping("/get")
    @ApiOperation(value = "获取字段")
    public String redisGet() {
        Object o = redisUtil.get("gps:value");
        return String.valueOf(o);
    }
}
