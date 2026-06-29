package cn.sl.ehub.console.auth;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.mapper.AggregatorEntMapper;
import cn.sl.ehub.service.vo.AggregatorEnt;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class LoadAggregationScopeService {

    private final AggregatorEntMapper aggregatorEntMapper;

    public LoadAggregationScopeService(AggregatorEntMapper aggregatorEntMapper) {
        this.aggregatorEntMapper = aggregatorEntMapper;
    }

    public Scope resolveQueryScope(String aggregatorId, String entId) {
        return resolveQueryScope(AuthContext.get(), aggregatorId, entId);
    }

    public Scope resolveQueryScope(AuthUser user, String aggregatorId, String entId) {
        if (user == null || isAdmin(user)) {
            return new Scope(true, trimToNull(aggregatorId), trimToNull(entId));
        }
        if (isEntCustomer(user)) {
            return new Scope(false, user.getAggregatorId(), user.getEntId());
        }
        if (isAggregatorCustomer(user)) {
            validateAggregatorIfPresent(user, aggregatorId);
            validateEntBelongsIfPresent(user.getAggregatorId(), entId);
            return new Scope(false, user.getAggregatorId(), trimToNull(entId));
        }
        throwNoPermission();
        return new Scope(false, null, null);
    }

    public String resolveAggregatorId(String aggregatorId) {
        return resolveQueryScope(aggregatorId, null).getAggregatorId();
    }

    public void validateScope(String aggregatorId, String entId) {
        validateScope(AuthContext.get(), aggregatorId, entId);
    }

    public void validateScope(AuthUser user, String aggregatorId, String entId) {
        if (user == null || isAdmin(user)) {
            return;
        }
        if (isEntCustomer(user)) {
            if (StringUtils.isNotBlank(aggregatorId) && !StringUtils.equals(user.getAggregatorId(), aggregatorId)) {
                throwNoPermission();
            }
            if (StringUtils.isBlank(entId) || !StringUtils.equals(user.getEntId(), entId)) {
                throwNoPermission();
            }
            return;
        }
        if (isAggregatorCustomer(user)) {
            validateAggregatorIfPresent(user, aggregatorId);
            validateEntBelongsIfPresent(user.getAggregatorId(), entId);
            return;
        }
        throwNoPermission();
    }

    public void validateRequestScope(AuthUser user, String[] aggregatorIds, String[] entIds) {
        if (user == null || isAdmin(user)) {
            return;
        }
        if (isEntCustomer(user)) {
            validateAllEqual(user.getAggregatorId(), aggregatorIds);
            validateAllEqual(user.getEntId(), entIds);
            return;
        }
        if (isAggregatorCustomer(user)) {
            validateAllEqual(user.getAggregatorId(), aggregatorIds);
            if (entIds != null) {
                for (String entId : entIds) {
                    validateEntBelongsIfPresent(user.getAggregatorId(), entId);
                }
            }
            return;
        }
        throwNoPermission();
    }

    public void fillAggregatorEntSaveScope(AggregatorEnt aggregatorEnt, boolean update) {
        if (aggregatorEnt == null) {
            throw new BaseException(StatusCode.C.getCode(), "企业信息不能为空");
        }
        AuthUser user = AuthContext.get();
        if (user == null || isAdmin(user)) {
            return;
        }
        if (isEntCustomer(user)) {
            if (!update) {
                throwNoPermission();
            }
            aggregatorEnt.setAggregatorId(user.getAggregatorId());
            aggregatorEnt.setEntId(user.getEntId());
            return;
        }
        if (isAggregatorCustomer(user)) {
            aggregatorEnt.setAggregatorId(user.getAggregatorId());
            return;
        }
        throwNoPermission();
    }

    public boolean isAdmin(AuthUser user) {
        return user != null && (StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_ADMIN, user.getUserType())
                || StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_PLATFORM, user.getUserType()));
    }

    public boolean isEntCustomer(AuthUser user) {
        return isCustomer(user) && StringUtils.isNotBlank(user.getEntId());
    }

    public boolean isAggregatorCustomer(AuthUser user) {
        return isCustomer(user) && StringUtils.isNotBlank(user.getAggregatorId()) && StringUtils.isBlank(user.getEntId());
    }

    public boolean isCustomer(AuthUser user) {
        return user != null && (StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_CUSTOMER, user.getUserType())
                || StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_AGGREGATOR, user.getUserType())
                || StringUtils.equalsIgnoreCase(ConsoleProductService.USER_TYPE_ENT, user.getUserType()));
    }

    private void validateAggregatorIfPresent(AuthUser user, String aggregatorId) {
        if (StringUtils.isNotBlank(aggregatorId) && !StringUtils.equals(user.getAggregatorId(), aggregatorId)) {
            throwNoPermission();
        }
    }

    private void validateAllEqual(String expected, String[] actualValues) {
        if (actualValues == null) {
            return;
        }
        for (String actual : actualValues) {
            if (StringUtils.isBlank(actual)) {
                continue;
            }
            if (StringUtils.isBlank(expected) || !StringUtils.equals(expected, actual)) {
                throwNoPermission();
            }
        }
    }

    private void validateEntBelongsIfPresent(String aggregatorId, String entId) {
        if (StringUtils.isBlank(entId)) {
            return;
        }
        if (StringUtils.isBlank(aggregatorId)) {
            throwNoPermission();
        }
        AggregatorEnt query = new AggregatorEnt();
        query.setAggregatorId(aggregatorId);
        query.setEntId(entId);
        if (aggregatorEntMapper.selectCount(query) <= 0) {
            throwNoPermission();
        }
    }

    private String trimToNull(String value) {
        return StringUtils.trimToNull(value);
    }

    private void throwNoPermission() {
        throw new BaseException(StatusCode.U.getCode(), StatusCode.U.getMsg());
    }

    public static class Scope {
        private final boolean admin;
        private final String aggregatorId;
        private final String entId;

        private Scope(boolean admin, String aggregatorId, String entId) {
            this.admin = admin;
            this.aggregatorId = aggregatorId;
            this.entId = entId;
        }

        public boolean isAdmin() {
            return admin;
        }

        public String getAggregatorId() {
            return aggregatorId;
        }

        public String getEntId() {
            return entId;
        }
    }
}
