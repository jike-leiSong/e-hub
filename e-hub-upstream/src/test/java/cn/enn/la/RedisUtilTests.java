package cn.sl.ehub.upstream;

import cn.sl.ehub.common.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@SpringBootTest
public class RedisUtilTests {

    @Resource
    private RedisUtil redisUtil;

    /**
     * 测试key-value存储
     */
    @Test
    public void testSet() {
        redisUtil.set("la-test-key", "la-test-value");
    }

    /**
     * 获取缓存失效时间 单位：秒
     */
    @Test
    public void testGetExpire() {
        long expire = redisUtil.getExpire("la-test-key");
        System.out.println("expire:" + expire);
    }

    /**
     * 缓存失效时间 单位：秒
     */
    @Test
    public void testExpire() {
        boolean expire = redisUtil.expire("la-test-key", 1000);
        System.out.println("expire:" + expire);
    }

    /**
     * 判断key是否存在
     */
    @Test
    public void testHashKey() {
        boolean expire = redisUtil.hasKey("la-test-key");
        System.out.println("expire:" + expire);
    }

    /**
     * 删除缓存
     */
    @Test
    public void testDel() {
        redisUtil.del("la-test-key");
    }

    /**
     * 删除缓存
     */
    @Test
    public void testGet() {
        redisUtil.del("la-test-key");
        String.valueOf(null);
    }






}
