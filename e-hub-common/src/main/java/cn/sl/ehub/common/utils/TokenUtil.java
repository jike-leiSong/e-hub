package cn.sl.ehub.common.utils;

import cn.sl.ehub.common.dto.TokenDTO;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Map;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
public class TokenUtil {

    /**
     * 生成token
     *
     * @param tokenDTO
     * @return
     */
    public static String generateToken(TokenDTO tokenDTO) {

        String accessToken = JWT.create().withIssuer(tokenDTO.getIssuer())
                .withSubject(tokenDTO.getSubject())
                .withIssuedAt(tokenDTO.getIssueAt())
                .withExpiresAt(tokenDTO.getExpiresAt())
                .withAudience(tokenDTO.getAudience())
                .withJWTId(tokenDTO.getJwtId())
                .sign(tokenDTO.getAlgorithm());
        return accessToken;
    }

    /**
     * 校验token
     * <p>
     * 入参为包装后的算法
     *
     * @param token
     * @param algorithm
     * @return
     */
    public static Map<String, Claim> verifyToken(String token, Algorithm algorithm) {

        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = jwtVerifier.verify(token);

        return decodedJWT.getClaims();
    }

    /**
     * 校验token
     * <p>
     * 入参为appSecret
     *
     * @param token
     * @param appSecret
     * @return
     */
    public static Map<String, Claim> verifyToken(String token, String appSecret) {

        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(appSecret)).build();
        DecodedJWT decodedJWT = jwtVerifier.verify(token);

        return decodedJWT.getClaims();
    }

}
