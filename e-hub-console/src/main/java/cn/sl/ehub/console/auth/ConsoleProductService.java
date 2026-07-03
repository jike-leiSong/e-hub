package cn.sl.ehub.console.auth;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.common.utils.DateUtils;
import cn.sl.ehub.console.auth.model.ProductCustomerResp;
import cn.sl.ehub.console.auth.model.ProductOptionResp;
import cn.sl.ehub.service.mapper.ConsoleCustomerProductMapper;
import cn.sl.ehub.service.mapper.ConsoleUserMapper;
import cn.sl.ehub.service.vo.ConsoleCustomerProduct;
import cn.sl.ehub.service.vo.ConsoleUser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ConsoleProductService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleProductService.class);

    public static final String USER_TYPE_ADMIN = "ADMIN";
    public static final String USER_TYPE_CUSTOMER = "CUSTOMER";
    public static final String USER_TYPE_PLATFORM = "PLATFORM";
    public static final String USER_TYPE_AGGREGATOR = "AGGREGATOR";
    public static final String USER_TYPE_ENT = "ENT";

    public static final String PRODUCT_LOAD = "load_aggregation";
    public static final String PRODUCT_TARIFF = "tariff";

    private static final List<ProductOptionResp> PRODUCT_OPTIONS = Collections.unmodifiableList(Arrays.asList(
            new ProductOptionResp(PRODUCT_LOAD, "负荷聚合", "运营总览、调节情况、收益结算、物联管理、资源管理"),
            new ProductOptionResp(PRODUCT_TARIFF, "电价服务", "电网代理价格、接口能力、调用记录")
    ));

    private final ConsoleUserMapper consoleUserMapper;
    private final ConsoleCustomerProductMapper customerProductMapper;

    public ConsoleProductService(ConsoleUserMapper consoleUserMapper,
                                 ConsoleCustomerProductMapper customerProductMapper) {
        this.consoleUserMapper = consoleUserMapper;
        this.customerProductMapper = customerProductMapper;
    }

    public List<ProductOptionResp> productOptions() {
        return PRODUCT_OPTIONS;
    }

    public List<String> allProductCodes() {
        List<String> codes = new ArrayList<>();
        for (ProductOptionResp option : PRODUCT_OPTIONS) {
            codes.add(option.getCode());
        }
        return codes;
    }

    public List<String> enabledProducts(AuthUser user) {
        if (user == null) {
            return Collections.emptyList();
        }
        if (isAdmin(user.getUserType())) {
            return allProductCodes();
        }
        try {
            List<String> customerIdsList = customerIds(user.getAggregatorId(), user.getEntId(), user.getUserId());
            log.debug("Querying products for userId={}, customerIds={}", user.getUserId(), customerIdsList);
            List<String> productCodes = customerProductMapper.listEnabledProductCodes(
                    user.getUserId(),
                    customerIdsList
            );
            log.debug("Found product codes: {}", productCodes);
            List<String> normalized = normalizeProductCodes(productCodes, false);
            log.debug("Normalized product codes: {}", normalized);
            return normalized;
        } catch (Exception e) {
            log.error("Failed to query enabled products for user {}", user.getUserId(), e);
            return Collections.emptyList();
        }
    }

    public List<ProductCustomerResp> listCustomers(String keyword) {
        List<ConsoleUser> users = consoleUserMapper.listCustomers(StringUtils.trimToNull(keyword));
        if (isEmpty(users)) {
            return Collections.emptyList();
        }
        List<String> userIds = new ArrayList<>();
        List<String> customerIds = new ArrayList<>();
        for (ConsoleUser user : users) {
            if (StringUtils.isNotBlank(user.getUserId())) {
                userIds.add(user.getUserId());
            }
            addIfNotBlank(customerIds, customerId(user.getAggregatorId(), user.getEntId(), user.getUserId()));
        }
        List<ConsoleCustomerProduct> products = customerProductMapper.listByUserIdsOrCustomerIds(userIds, customerIds);
        Map<String, List<String>> productsByUserId = productsByKey(products, true);
        Map<String, List<String>> productsByCustomerId = productsByKey(products, false);
        List<ProductCustomerResp> result = new ArrayList<>();
        for (ConsoleUser user : users) {
            String customerId = customerId(user.getAggregatorId(), user.getEntId(), user.getUserId());
            ProductCustomerResp item = new ProductCustomerResp();
            item.setUserId(user.getUserId());
            item.setUsername(user.getUsername());
            item.setDisplayName(user.getDisplayName());
            item.setUserType(normalizeUserType(user.getUserType()));
            item.setAggregatorId(user.getAggregatorId());
            item.setEntId(user.getEntId());
            item.setCustomerId(customerId);
            item.setProducts(mergeProducts(productsByUserId.get(user.getUserId()), productsByCustomerId.get(customerId)));
            result.add(item);
        }
        return result;
    }

    public void saveCustomerProducts(String userId, List<String> productCodes) {
        if (StringUtils.isBlank(userId)) {
            throw new BaseException(StatusCode.C.getCode(), "用户ID为空");
        }
        ConsoleUser query = new ConsoleUser();
        query.setUserId(userId);
        ConsoleUser user = consoleUserMapper.selectOne(query);
        if (user == null) {
            throw new BaseException(StatusCode.C.getCode(), "客户不存在");
        }
        if (!isCustomer(user.getUserType())) {
            throw new BaseException(StatusCode.C.getCode(), "仅客户账号支持产品开通");
        }
        List<String> normalizedProducts = normalizeProductCodes(productCodes, true);
        customerProductMapper.deleteByUserId(userId);
        String now = DateUtils.getTime();
        String customerId = customerId(user.getAggregatorId(), user.getEntId(), user.getUserId());
        for (String productCode : normalizedProducts) {
            ConsoleCustomerProduct product = new ConsoleCustomerProduct();
            product.setUserId(userId);
            product.setCustomerId(customerId);
            product.setProductCode(productCode);
            product.setEnabled(1);
            product.setCreateTime(now);
            product.setUpdateTime(now);
            customerProductMapper.insertSelective(product);
        }
    }

    public String normalizeUserType(String userType) {
        if (isAdmin(userType)) {
            return USER_TYPE_ADMIN;
        }
        return USER_TYPE_CUSTOMER;
    }

    public boolean isAdmin(String userType) {
        return StringUtils.equalsIgnoreCase(USER_TYPE_ADMIN, userType)
                || StringUtils.equalsIgnoreCase(USER_TYPE_PLATFORM, userType);
    }

    public boolean isCustomer(String userType) {
        return StringUtils.equalsIgnoreCase(USER_TYPE_CUSTOMER, userType)
                || StringUtils.equalsIgnoreCase(USER_TYPE_AGGREGATOR, userType)
                || StringUtils.equalsIgnoreCase(USER_TYPE_ENT, userType);
    }

    private Map<String, List<String>> productsByKey(List<ConsoleCustomerProduct> products, boolean byUserId) {
        if (isEmpty(products)) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> result = new LinkedHashMap<>();
        for (ConsoleCustomerProduct product : products) {
            String key = byUserId ? product.getUserId() : product.getCustomerId();
            if (StringUtils.isBlank(key) || StringUtils.isBlank(product.getProductCode())) {
                continue;
            }
            result.computeIfAbsent(key, item -> new ArrayList<>()).add(product.getProductCode());
        }
        for (Map.Entry<String, List<String>> entry : result.entrySet()) {
            entry.setValue(normalizeProductCodes(entry.getValue(), false));
        }
        return result;
    }

    private List<String> mergeProducts(List<String> first, List<String> second) {
        Set<String> products = new LinkedHashSet<>();
        if (!isEmpty(first)) {
            products.addAll(first);
        }
        if (!isEmpty(second)) {
            products.addAll(second);
        }
        return normalizeProductCodes(new ArrayList<>(products), false);
    }

    private List<String> normalizeProductCodes(List<String> productCodes, boolean strict) {
        if (isEmpty(productCodes)) {
            return Collections.emptyList();
        }
        Set<String> requested = new LinkedHashSet<>();
        for (String productCode : productCodes) {
            String normalized = StringUtils.lowerCase(StringUtils.trim(productCode));
            if (StringUtils.isNotBlank(normalized)) {
                requested.add(normalized);
            }
        }
        List<String> result = new ArrayList<>();
        for (ProductOptionResp option : PRODUCT_OPTIONS) {
            if (requested.contains(option.getCode())) {
                result.add(option.getCode());
            }
        }
        if (strict && result.size() != requested.size()) {
            throw new BaseException(StatusCode.C.getCode(), "存在不支持的产品编码");
        }
        return result;
    }

    private List<String> customerIds(String aggregatorId, String entId, String userId) {
        List<String> ids = new ArrayList<>();
        addIfNotBlank(ids, entId);
        addIfNotBlank(ids, aggregatorId);
        addIfNotBlank(ids, userId);
        return ids;
    }

    private String customerId(String aggregatorId, String entId, String userId) {
        if (StringUtils.isNotBlank(entId)) {
            return entId;
        }
        if (StringUtils.isNotBlank(aggregatorId)) {
            return aggregatorId;
        }
        return userId;
    }

    private void addIfNotBlank(List<String> values, String value) {
        if (StringUtils.isBlank(value) || values.contains(value)) {
            return;
        }
        values.add(value);
    }

    private boolean isEmpty(List<?> values) {
        return values == null || values.isEmpty();
    }
}
