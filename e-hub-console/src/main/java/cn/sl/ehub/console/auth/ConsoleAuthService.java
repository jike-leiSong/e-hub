package cn.sl.ehub.console.auth;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.console.auth.model.AuthLoginReq;
import cn.sl.ehub.console.auth.model.AuthLoginResp;
import cn.sl.ehub.service.mapper.ConsoleUserMapper;
import cn.sl.ehub.service.vo.ConsoleUser;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConsoleAuthService implements AuthService {

    private static final String ISSUER = "e-hub-console";
    private static final String AUDIENCE = "console";

    private final ConsoleAuthProperties properties;
    private final ConsolePermissionService permissionService;
    private final ConsoleUserMapper consoleUserMapper;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, AuthSession> sessions = new ConcurrentHashMap<>();

    public ConsoleAuthService(ConsoleAuthProperties properties,
                              ConsolePermissionService permissionService,
                              ConsoleUserMapper consoleUserMapper) {
        this.properties = properties;
        this.permissionService = permissionService;
        this.consoleUserMapper = consoleUserMapper;
    }

    @Override
    public AuthLoginResp login(AuthLoginReq req) {
        if (req == null || StringUtils.isBlank(req.getUsername()) || StringUtils.isBlank(req.getPassword())) {
            throw new BaseException(StatusCode.C.getCode(), "用户名或密码为空");
        }

        String username = StringUtils.trim(req.getUsername());
        ConsoleUser consoleUser = consoleUserMapper.getByUsername(username);
        if (consoleUser == null || !passwordMatches(req.getPassword(), consoleUser)) {
            throw new BaseException(StatusCode.G.getCode(), StatusCode.G.getMsg());
        }
        if (!Integer.valueOf(1).equals(consoleUser.getStatus())) {
            throw new BaseException(StatusCode.W.getCode(), StatusCode.W.getMsg());
        }

        AuthUser authUser = buildAuthUser(consoleUser);

        long now = System.currentTimeMillis();
        long expireAt = now + expireMinutes() * 60_000L;
        String nonce = randomHex(16);
        String token = JWT.create()
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(authUser.getUsername())
                .withClaim("userId", authUser.getUserId())
                .withClaim("displayName", authUser.getDisplayName())
                .withClaim("userType", authUser.getUserType())
                .withClaim("aggregatorId", authUser.getAggregatorId())
                .withClaim("entId", authUser.getEntId())
                .withClaim("tenantId", authUser.getTenantId())
                .withIssuedAt(new Date(now))
                .withExpiresAt(new Date(expireAt))
                .withJWTId(nonce)
                .sign(algorithm());

        sessions.put(token, new AuthSession(authUser, expireAt));
        cleanExpiredSessions(now);
        updateLastLoginTime(consoleUser);

        AuthLoginResp resp = new AuthLoginResp();
        resp.setToken(token);
        resp.setTokenType("Bearer");
        resp.setUserId(authUser.getUserId());
        resp.setUsername(authUser.getUsername());
        resp.setDisplayName(authUser.getDisplayName());
        resp.setUserType(authUser.getUserType());
        resp.setAggregatorId(authUser.getAggregatorId());
        resp.setEntId(authUser.getEntId());
        resp.setTenantId(authUser.getTenantId());
        resp.setExpireAt(expireAt);
        resp.setExpireSeconds(expireMinutes() * 60);
        permissionService.fillLoginResp(resp, authUser);
        return resp;
    }

    @Override
    public AuthUser verify(String token) {
        if (Boolean.FALSE.equals(properties.getEnabled())) {
            return new AuthUser(null, "anonymous", "anonymous", ConsoleProductService.USER_TYPE_ADMIN, null, null, null);
        }
        if (StringUtils.isBlank(token)) {
            throw new BaseException(StatusCode.L.getCode(), StatusCode.L.getMsg());
        }
        try {
            JWTVerifier verifier = JWT.require(algorithm())
                    .withIssuer(ISSUER)
                    .withAudience(AUDIENCE)
                    .build();
            verifier.verify(token);
            AuthSession session = sessions.get(token);
            long now = System.currentTimeMillis();
            if (session == null || session.getExpireAt() <= now) {
                sessions.remove(token);
                throw new BaseException(StatusCode.D.getCode(), StatusCode.D.getMsg());
            }
            return session.getUser();
        } catch (BaseException e) {
            throw e;
        } catch (JWTVerificationException e) {
            throw new BaseException(StatusCode.D.getCode(), StatusCode.D.getMsg());
        }
    }

    @Override
    public void logout(String token) {
        if (StringUtils.isNotBlank(token)) {
            sessions.remove(token);
        }
    }

    private AuthUser buildAuthUser(ConsoleUser consoleUser) {
        return new AuthUser(
                consoleUser.getUserId(),
                consoleUser.getUsername(),
                consoleUser.getDisplayName(),
                normalizeUserType(consoleUser.getUserType()),
                consoleUser.getAggregatorId(),
                consoleUser.getEntId(),
                consoleUser.getTenantId()
        );
    }

    private String normalizeUserType(String userType) {
        if (StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_ADMIN, userType)
                || StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_PLATFORM, userType)) {
            return ConsoleProductService.USER_TYPE_ADMIN;
        }
        return ConsoleProductService.USER_TYPE_CUSTOMER;
    }

    private void updateLastLoginTime(ConsoleUser consoleUser) {
        ConsoleUser update = new ConsoleUser();
        update.setId(consoleUser.getId());
        update.setLastLoginTime(DateUtils.getTime());
        update.setUpdateTime(DateUtils.getTime());
        consoleUserMapper.updateByPrimaryKeySelective(update);
    }

    private boolean passwordMatches(String rawPassword, ConsoleUser consoleUser) {
        if (consoleUser == null || StringUtils.isBlank(consoleUser.getPasswordHash())) {
            return false;
        }
        String actualHash = StringUtils.lowerCase(sha256(StringUtils.defaultString(consoleUser.getPasswordSalt()) + rawPassword));
        String expectedHash = StringUtils.lowerCase(StringUtils.trim(consoleUser.getPasswordHash()));
        return MessageDigest.isEqual(
                actualHash.getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8));
    }

    private Algorithm algorithm() {
        return Algorithm.HMAC256(StringUtils.defaultIfBlank(properties.getTokenSecret(), "e-hub-console-default-token-secret"));
    }

    private long expireMinutes() {
        return properties.getExpireMinutes() == null ? 720L : properties.getExpireMinutes();
    }

    private String randomHex(int byteLength) {
        byte[] bytes = new byte[byteLength];
        secureRandom.nextBytes(bytes);
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    private void cleanExpiredSessions(long now) {
        Iterator<Map.Entry<String, AuthSession>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().getExpireAt() <= now) {
                iterator.remove();
            }
        }
    }

    private static class AuthSession {
        private final AuthUser user;
        private final long expireAt;

        private AuthSession(AuthUser user, long expireAt) {
            this.user = user;
            this.expireAt = expireAt;
        }

        public AuthUser getUser() {
            return user;
        }

        public long getExpireAt() {
            return expireAt;
        }
    }
}
