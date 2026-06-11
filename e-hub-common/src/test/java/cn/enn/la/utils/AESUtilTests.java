package cn.sl.ehub.common.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.util.Base64;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public class AESUtilTests {

    /**
     * 初始化key
     *
     * @throws Exception
     */
    @Test
    public void testInitKey() throws Exception {

        StringBuilder stringBuilder = new StringBuilder("36d16314ecd8c3ccd8fea3472e8a0744");
        stringBuilder.append("2f326959d69136e75538cb715d29f7b1");
        stringBuilder.append("1629789725106");

        String md5Hex = DigestUtils.md5Hex(stringBuilder.toString());

        System.out.println(md5Hex);

    }

}
