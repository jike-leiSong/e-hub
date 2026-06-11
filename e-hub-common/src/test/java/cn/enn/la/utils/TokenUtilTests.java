package cn.sl.ehub.common.utils;

import cn.sl.ehub.common.dto.TokenDTO;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public class TokenUtilTests {

    @Test
    public void testGenerateToken() {
        TokenDTO tokenDTO = new TokenDTO();
        tokenDTO.setAppKey("guangzhoutest");
        tokenDTO.setAppSecret("xinaofannengguangzhoutest");
        tokenDTO.setTimestamp(DateTime.now().getMillis());
        tokenDTO.setSign(Md5Crypt.md5Crypt((tokenDTO.getAppKey() + tokenDTO.getAppSecret() + tokenDTO.getTimestamp()).getBytes(StandardCharsets.UTF_8)));
        tokenDTO.setIssuer("enn");
        tokenDTO.setSubject("guangzhou-grid");
        tokenDTO.setAudience("guangzhou");
        tokenDTO.setIssueAt(new Date());
        tokenDTO.setExpiresAt(DateUtils.addSeconds(new Date(tokenDTO.getTimestamp()), 7200));
        tokenDTO.setAlgorithm(Algorithm.HMAC256(tokenDTO.getAppSecret()));
        tokenDTO.setJwtId(UUID.randomUUID().toString());

        String token = TokenUtil.generateToken(tokenDTO);
        System.out.println(token);

    }
}
