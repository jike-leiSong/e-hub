package cn.sl.ehub.service.service;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.dto.tariff.AgentPriceAreaOption;
import cn.sl.ehub.service.dto.tariff.AgentPriceAreaMenuNode;
import cn.sl.ehub.service.dto.tariff.AgentPriceDefaultMenuResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceDictItemResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceHeaderResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceOpenApiQueryReq;
import cn.sl.ehub.service.dto.tariff.AgentPriceOpenApiResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceOptionsResp;
import cn.sl.ehub.service.dto.tariff.AgentPricePeriodResp;
import cn.sl.ehub.service.dto.tariff.AgentPricePointResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceQueryReq;
import cn.sl.ehub.service.dto.tariff.AgentPriceSourceResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceValuePointResp;
import cn.sl.ehub.service.dto.tariff.AgentPriceVersionQueryReq;
import cn.sl.ehub.service.dto.tariff.AgentPriceVersionResp;
import cn.sl.ehub.service.dto.tariff.FpgjPointResp;
import cn.sl.ehub.service.dto.tariff.HaomaidianMenuReq;
import cn.sl.ehub.service.mapper.TariffAgentPriceMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TariffAgentPriceService {

    private static final BigDecimal RATE = new BigDecimal("0.001");
    private final TariffAgentPriceMapper tariffAgentPriceMapper;

    public TariffAgentPriceService(TariffAgentPriceMapper tariffAgentPriceMapper) {
        this.tariffAgentPriceMapper = tariffAgentPriceMapper;
    }

    public List<AgentPriceVersionResp> getVersions(AgentPriceVersionQueryReq req) {
        AgentPriceVersionQueryReq query = req == null ? new AgentPriceVersionQueryReq() : req;
        String version = StringUtils.isBlank(query.getYearMonth()) ? "" : resolveRequestedVersion(query.getYearMonth());
        List<AgentPriceVersionResp> versions = tariffAgentPriceMapper.selectVersionOptions(query.getProvinceCode(), version);
        if (CollectionUtils.isEmpty(versions)) {
            return Collections.emptyList();
        }
        for (AgentPriceVersionResp item : versions) {
            item.setYearMonth(toDisplayMonth(item.getVersion()));
            item.setStatus(StringUtils.defaultIfBlank(item.getStatus(), "PUBLISHED"));
            item.setEffectiveStart(resolveEffectiveStart(item.getVersion()));
            item.setEffectiveEnd(resolveEffectiveEnd(item.getVersion()));
        }
        return versions;
    }

    public List<AgentPriceAreaMenuNode> getOpenApiAreas(AgentPriceVersionQueryReq req) {
        if (req == null || StringUtils.isBlank(req.getYearMonth())) {
            throwParam("电费年月不能为空");
        }
        String version = resolveRequestedVersion(req.getYearMonth());
        List<AgentPriceAreaOption> areas = tariffAgentPriceMapper.selectAreaOptions(version);
        if (StringUtils.isNotBlank(req.getProvinceCode())) {
            areas = areas.stream()
                    .filter(item -> StringUtils.equals(req.getProvinceCode(), item.getProvinceCode()))
                    .collect(Collectors.toList());
        }
        return buildAreaTree(areas);
    }

    public AgentPriceOptionsResp getOpenApiOptions(AgentPriceQueryReq req) {
        if (req == null || StringUtils.isBlank(resolveDateOrMonth(req.getSelectedDate(), req.getYearMonth()))) {
            throwParam("电费日期不能为空");
        }
        return getOptions(req);
    }

    public AgentPriceOpenApiResp getOpenApiAgentPrices(AgentPriceOpenApiQueryReq req) {
        validateQuery(req);
        AgentPriceQueryReq query = copy(req);
        String dateOrMonth = resolveDateOrMonth(query.getSelectedDate(), query.getYearMonth());
        query.setYearMonth(resolveRequestedVersion(dateOrMonth));

        List<FpgjPointResp> fpgj = resolveFpgjData(query, dateOrMonth);
        AgentPriceQueryReq fallbackFpgjQuery = copy(query);
        fallbackFpgjQuery.setSecondType("不限");
        List<FpgjPointResp> fallbackFpgj = tariffAgentPriceMapper.selectFpgjData(fallbackFpgjQuery);

        List<BigDecimal> ddPrices = selectPriceData(query, "电度");
        if (CollectionUtils.isEmpty(ddPrices)) {
            throwNoData(dateOrMonth);
        }
        List<BigDecimal> spPrices = selectPriceData(query, "输配");
        List<BigDecimal> fjPrices = selectPriceData(query, "附加");
        List<BigDecimal> xsPrices = selectPriceData(query, "线损");
        List<BigDecimal> xtyxPrices = selectPriceData(query, "系统运行");

        AgentPriceHeaderResp header = tariffAgentPriceMapper.selectAgentPriceHeader(query);
        AgentPriceOpenApiResp resp = new AgentPriceOpenApiResp();
        resp.setVersion(query.getYearMonth());
        resp.setYearMonth(toDisplayMonth(query.getYearMonth()));
        resp.setProvinceCode(query.getProvinceCode());
        resp.setProvinceName(header == null ? query.getProvinceName() : header.getProvinceName());
        resp.setSecondType(query.getSecondType());
        resp.setThirdType(query.getThirdType());
        resp.setUserType(query.getUserType());
        resp.setDyLevel(query.getDyLevel());
        resp.setSfType(query.getSfType());
        if (header != null) {
            resp.setCapacityElectricityPrice(header.getCapacityElectricityPrice());
            resp.setDemandElectricityPrice(header.getDemandElectricityPrice());
        }

        Map<String, AgentPricePeriodResp> summary = new LinkedHashMap<>();
        summary.put("jian", buildPeriod("尖", fpgj, fallbackFpgj, ddPrices, spPrices, fjPrices, xsPrices, xtyxPrices));
        summary.put("feng", buildPeriod("峰", fpgj, fallbackFpgj, ddPrices, spPrices, fjPrices, xsPrices, xtyxPrices));
        summary.put("ping", buildPeriod("平", fpgj, fallbackFpgj, ddPrices, spPrices, fjPrices, xsPrices, xtyxPrices));
        summary.put("gu", buildPeriod("谷", fpgj, fallbackFpgj, ddPrices, spPrices, fjPrices, xsPrices, xtyxPrices));
        summary.put("shengu", buildPeriod("深谷", fpgj, fallbackFpgj, ddPrices, spPrices, fjPrices, xsPrices, xtyxPrices));
        resp.setPeriodSummary(summary);

        if (Boolean.TRUE.equals(req.getReturnPoints())) {
            resp.setPoints96(buildOpenApiPoints(query, fpgj, fallbackFpgj));
        }
        resp.setSource(resolveSource(query));
        return resp;
    }

    public Map<String, AgentPricePeriodResp> getAgentPrices(AgentPriceQueryReq req) {
        validateQuery(req);
        AgentPriceQueryReq query = copy(req);
        String dateOrMonth = resolveDateOrMonth(query.getSelectedDate(), query.getYearMonth());
        query.setYearMonth(resolveRequestedVersion(dateOrMonth));

        List<FpgjPointResp> fpgj = resolveFpgjData(query, dateOrMonth);
        AgentPriceQueryReq fallbackFpgjQuery = copy(query);
        fallbackFpgjQuery.setSecondType("不限");
        List<FpgjPointResp> fallbackFpgj = tariffAgentPriceMapper.selectFpgjData(fallbackFpgjQuery);

        query.setPriceType("电度");
        List<BigDecimal> ddPrices = tariffAgentPriceMapper.selectAgentPriceData(query);
        if (CollectionUtils.isEmpty(ddPrices)) {
            throwNoData(dateOrMonth);
        }
        query.setPriceType("输配");
        List<BigDecimal> spPrices = tariffAgentPriceMapper.selectAgentPriceData(query);
        query.setPriceType("附加");
        List<BigDecimal> fjPrices = tariffAgentPriceMapper.selectAgentPriceData(query);
        query.setPriceType("线损");
        List<BigDecimal> xsPrices = tariffAgentPriceMapper.selectAgentPriceData(query);
        query.setPriceType("系统运行");
        List<BigDecimal> xtyxPrices = tariffAgentPriceMapper.selectAgentPriceData(query);

        Map<String, AgentPricePeriodResp> result = new LinkedHashMap<>();
        result.put("jian", buildPeriod("尖", fpgj, fallbackFpgj, ddPrices, spPrices, fjPrices, xsPrices, xtyxPrices));
        result.put("feng", buildPeriod("峰", fpgj, fallbackFpgj, ddPrices, spPrices, fjPrices, xsPrices, xtyxPrices));
        result.put("ping", buildPeriod("平", fpgj, fallbackFpgj, ddPrices, spPrices, fjPrices, xsPrices, xtyxPrices));
        result.put("gu", buildPeriod("谷", fpgj, fallbackFpgj, ddPrices, spPrices, fjPrices, xsPrices, xtyxPrices));
        result.put("shengu", buildPeriod("深谷", fpgj, fallbackFpgj, ddPrices, spPrices, fjPrices, xsPrices, xtyxPrices));
        return result;
    }

    public AgentPriceOptionsResp getOptions(AgentPriceQueryReq req) {
        AgentPriceQueryReq query = req == null ? new AgentPriceQueryReq() : copy(req);
        String dateOrMonth = resolveDateOrMonth(query.getSelectedDate(), query.getYearMonth());
        List<String> versions = listAvailableVersions();
        String version = StringUtils.isBlank(dateOrMonth)
                ? resolveLatestVersion(versions)
                : resolveOptionVersion(dateOrMonth, versions);
        query.setYearMonth(version);

        AgentPriceOptionsResp resp = new AgentPriceOptionsResp();
        resp.setAreas(tariffAgentPriceMapper.selectAreaOptions(version));
        for (AgentPriceAreaOption area : resp.getAreas()) {
            area.setLabel(buildAreaLabel(area));
        }
        resp.setUserTypes(tariffAgentPriceMapper.selectUserTypes(query));
        resp.setSfTypes(tariffAgentPriceMapper.selectSfTypes(query));
        resp.setDyLevels(tariffAgentPriceMapper.selectDyLevels(query));
        return resp;
    }

    public AgentPriceDefaultMenuResp getDefaultMenus(HaomaidianMenuReq req) {
        HaomaidianMenuReq query = req == null ? new HaomaidianMenuReq() : req;
        List<String> versions = listAvailableVersions();
        AgentPriceDefaultMenuResp resp = new AgentPriceDefaultMenuResp();
        resp.getMap().put("yearMonth", buildMonthOptions(versions));
        if (CollectionUtils.isEmpty(versions)) {
            return resp;
        }

        String version = StringUtils.isBlank(query.getYearMonth())
                ? resolveLatestVersion(versions)
                : resolveOptionVersion(query.getYearMonth(), versions);
        List<AgentPriceAreaOption> areas = tariffAgentPriceMapper.selectAreaOptions(version);
        List<AgentPriceAreaMenuNode> tree = buildAreaTree(areas);
        if ("1".equals(query.getIsLimitProvince())) {
            tree = filterLimitedProvinces(tree);
        }
        resp.setList(tree);
        if (CollectionUtils.isEmpty(tree)) {
            return resp;
        }

        SelectedArea selectedArea = selectArea(tree, query.getProvinceCode(), query.getSecondType(), query.getThirdType());
        AgentPriceQueryReq scoped = new AgentPriceQueryReq();
        scoped.setYearMonth(version);
        scoped.setProvinceCode(selectedArea.getProvinceCode());
        scoped.setSecondType(selectedArea.getSecondType());
        scoped.setThirdType(selectedArea.getThirdType());

        List<String> userTypes = tariffAgentPriceMapper.selectUserTypes(scoped);
        resp.getMap().put("userType", userTypes);
        if (CollectionUtils.isEmpty(userTypes)) {
            resp.getMap().put("sfType", Collections.emptyList());
            resp.getMap().put("dyLevel", Collections.emptyList());
            return resp;
        }

        String userType = pickPreferred(query.getUserType(), userTypes);
        scoped.setUserType(userType);
        List<String> sfTypes = tariffAgentPriceMapper.selectSfTypes(scoped);
        resp.getMap().put("sfType", sfTypes);
        if (CollectionUtils.isEmpty(sfTypes)) {
            resp.getMap().put("dyLevel", Collections.emptyList());
            return resp;
        }

        String sfType = pickPreferred(query.getOtherType(), sfTypes);
        scoped.setSfType(sfType);
        resp.getMap().put("dyLevel", tariffAgentPriceMapper.selectDyLevels(scoped));
        return resp;
    }

    public Map<String, List<AgentPriceDictItemResp>> getDictByType(List<String> typeList) {
        Map<String, List<AgentPriceDictItemResp>> result = new LinkedHashMap<>();
        if (CollectionUtils.isEmpty(typeList)) {
            return result;
        }

        String version = resolveLatestVersion();
        AgentPriceQueryReq query = new AgentPriceQueryReq();
        query.setYearMonth(version);
        for (String type : typeList) {
            List<String> values = Collections.emptyList();
            if (StringUtils.equalsAny(type, "useElectricType", "userType")) {
                values = tariffAgentPriceMapper.selectUserTypes(query);
            } else if (StringUtils.equalsAny(type, "dyLevel")) {
                values = tariffAgentPriceMapper.selectDyLevels(query);
            } else if (StringUtils.equalsAny(type, "sfType", "otherType")) {
                values = tariffAgentPriceMapper.selectSfTypes(query);
            }
            result.put(type, toDictItems(values));
        }
        return result;
    }

    private List<BigDecimal> selectPriceData(AgentPriceQueryReq query, String priceType) {
        query.setPriceType(priceType);
        List<BigDecimal> rows = tariffAgentPriceMapper.selectAgentPriceData(query);
        return rows == null ? Collections.emptyList() : rows;
    }

    private List<AgentPriceValuePointResp> selectPricePointData(AgentPriceQueryReq query, String priceType) {
        query.setPriceType(priceType);
        List<AgentPriceValuePointResp> rows = tariffAgentPriceMapper.selectAgentPricePointData(query);
        return rows == null ? Collections.emptyList() : rows;
    }

    private AgentPriceSourceResp resolveSource(AgentPriceQueryReq query) {
        AgentPriceSourceResp source = null;
        try {
            source = tariffAgentPriceMapper.selectSourceDocument(query);
        } catch (Exception ignored) {
            // Source metadata is optional and must not affect price query availability.
        }
        if (hasSource(source)) {
            return source;
        }

        try {
            source = tariffAgentPriceMapper.selectSourceImportBatch(query);
        } catch (Exception ignored) {
            // Older deployments may not have import batch tables yet.
        }
        if (hasSource(source)) {
            return source;
        }

        try {
            source = tariffAgentPriceMapper.selectSourceConfigFallback(query);
        } catch (Exception ignored) {
            // Source config fallback is best-effort only.
        }
        return hasSource(source) ? source : null;
    }

    private boolean hasSource(AgentPriceSourceResp source) {
        return source != null
                && (StringUtils.isNotBlank(source.getSourceName())
                || StringUtils.isNotBlank(source.getSourceUrl())
                || StringUtils.isNotBlank(source.getSourceFileName())
                || StringUtils.isNotBlank(source.getImportBatchNo()));
    }

    private List<AgentPricePointResp> buildOpenApiPoints(AgentPriceQueryReq query,
                                                         List<FpgjPointResp> fpgj,
                                                         List<FpgjPointResp> fallbackFpgj) {
        Map<String, String> periodMap = toPeriodMap(fallbackFpgj);
        periodMap.putAll(toPeriodMap(fpgj));

        Map<String, BigDecimal> ddPriceMap = toScaledPriceMap(selectPricePointData(query, "电度"));
        Map<String, BigDecimal> spPriceMap = toScaledPriceMap(selectPricePointData(query, "输配"));
        Map<String, BigDecimal> fjPriceMap = toScaledPriceMap(selectPricePointData(query, "附加"));
        Map<String, BigDecimal> xsPriceMap = toScaledPriceMap(selectPricePointData(query, "线损"));
        Map<String, BigDecimal> xtyxPriceMap = toScaledPriceMap(selectPricePointData(query, "系统运行"));

        List<AgentPricePointResp> points = new ArrayList<>();
        for (int i = 0; i < 96; i++) {
            String time = indexTransBizTime(i);
            BigDecimal ddPrice = defaultPrice(ddPriceMap.get(time));
            BigDecimal spPrice = defaultPrice(spPriceMap.get(time));
            BigDecimal fjPrice = defaultPrice(fjPriceMap.get(time));
            BigDecimal xsPrice = defaultPrice(xsPriceMap.get(time));
            BigDecimal systemPrice = defaultPrice(xtyxPriceMap.get(time));
            BigDecimal dlPrice = ddPrice.subtract(spPrice).subtract(fjPrice).subtract(xsPrice).subtract(systemPrice);

            AgentPricePointResp point = new AgentPricePointResp();
            point.setTime(time);
            point.setPeriodType(StringUtils.defaultString(periodMap.get(time)));
            point.setDdPrice(formatPrice(ddPrice));
            point.setSpPrice(formatPrice(spPrice));
            point.setFjPrice(formatPrice(fjPrice));
            point.setXsPrice(formatPrice(xsPrice));
            point.setSystemPrice(formatPrice(systemPrice));
            point.setDlPrice(formatPrice(dlPrice));
            points.add(point);
        }
        return points;
    }

    private Map<String, String> toPeriodMap(List<FpgjPointResp> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return new LinkedHashMap<>();
        }
        return rows.stream()
                .filter(item -> item != null && StringUtils.isNotBlank(item.getBizTime()))
                .collect(Collectors.toMap(
                        FpgjPointResp::getBizTime,
                        item -> StringUtils.defaultString(item.getFgvalue()),
                        (left, right) -> right,
                        LinkedHashMap::new
                ));
    }

    private Map<String, BigDecimal> toScaledPriceMap(List<AgentPriceValuePointResp> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return new LinkedHashMap<>();
        }
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (AgentPriceValuePointResp row : rows) {
            if (row == null || StringUtils.isBlank(row.getBizTime())) {
                continue;
            }
            result.put(row.getBizTime(), defaultPrice(row.getPrice()).multiply(RATE).stripTrailingZeros());
        }
        return result;
    }

    private BigDecimal defaultPrice(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private List<FpgjPointResp> resolveFpgjData(AgentPriceQueryReq query, String dateOrMonth) {
        List<FpgjPointResp> fpgj = tariffAgentPriceMapper.selectFpgjData(query);
        if (CollectionUtils.isEmpty(fpgj) && isDateString(dateOrMonth)) {
            String month = LocalDate.parse(dateOrMonth).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            query.setYearMonth(resolveRequestedVersion(month));
            fpgj = tariffAgentPriceMapper.selectFpgjData(query);
        }
        return fpgj == null ? Collections.emptyList() : fpgj;
    }

    private List<AgentPriceDictItemResp> toDictItems(List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptyList();
        }
        return values.stream()
                .filter(StringUtils::isNotBlank)
                .distinct()
                .map(item -> new AgentPriceDictItemResp(item, item))
                .collect(Collectors.toList());
    }

    private List<AgentPriceAreaMenuNode> buildAreaTree(List<AgentPriceAreaOption> areas) {
        if (CollectionUtils.isEmpty(areas)) {
            return Collections.emptyList();
        }
        Map<String, AgentPriceAreaMenuNode> provinceMap = new LinkedHashMap<>();
        Map<String, Map<String, AgentPriceAreaMenuNode>> secondMap = new LinkedHashMap<>();
        for (AgentPriceAreaOption area : areas) {
            if (area == null || StringUtils.isBlank(area.getProvinceCode())) {
                continue;
            }
            AgentPriceAreaMenuNode province = provinceMap.computeIfAbsent(area.getProvinceCode(), key -> {
                AgentPriceAreaMenuNode node = new AgentPriceAreaMenuNode();
                node.setKey(area.getProvinceCode());
                node.setValue(StringUtils.defaultIfBlank(area.getProvinceName(), area.getProvinceCode()));
                node.setChildren(new ArrayList<>());
                secondMap.put(key, new LinkedHashMap<>());
                return node;
            });

            String secondType = StringUtils.defaultIfBlank(area.getSecondType(), "不限");
            Map<String, AgentPriceAreaMenuNode> provinceSeconds = secondMap.get(area.getProvinceCode());
            AgentPriceAreaMenuNode second = provinceSeconds.computeIfAbsent(secondType, key -> {
                AgentPriceAreaMenuNode node = new AgentPriceAreaMenuNode();
                node.setKey(key);
                node.setValue(key);
                node.setChildren(new ArrayList<>());
                province.getChildren().add(node);
                return node;
            });

            String thirdType = StringUtils.defaultIfBlank(area.getThirdType(), "不限");
            boolean exists = second.getChildren().stream().anyMatch(item -> StringUtils.equals(item.getKey(), thirdType));
            if (!exists) {
                AgentPriceAreaMenuNode third = new AgentPriceAreaMenuNode();
                third.setKey(thirdType);
                third.setValue(thirdType);
                third.setChildren(new ArrayList<>());
                second.getChildren().add(third);
            }
        }
        return new ArrayList<>(provinceMap.values());
    }

    private List<AgentPriceAreaMenuNode> filterLimitedProvinces(List<AgentPriceAreaMenuNode> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return Collections.emptyList();
        }
        Set<String> allowCodes = new HashSet<>();
        allowCodes.add("140000000000");
        allowCodes.add("370000000000");
        allowCodes.add("440000000000");
        return nodes.stream()
                .filter(item -> allowCodes.contains(item.getKey()))
                .collect(Collectors.toList());
    }

    private SelectedArea selectArea(List<AgentPriceAreaMenuNode> nodes, String provinceCode, String secondType, String thirdType) {
        AgentPriceAreaMenuNode province = nodes.stream()
                .filter(item -> StringUtils.equals(item.getKey(), provinceCode))
                .findFirst()
                .orElse(nodes.get(0));
        List<AgentPriceAreaMenuNode> seconds = CollectionUtils.isEmpty(province.getChildren())
                ? Collections.emptyList()
                : province.getChildren();
        AgentPriceAreaMenuNode second = CollectionUtils.isEmpty(seconds)
                ? null
                : seconds.stream().filter(item -> StringUtils.equals(item.getKey(), secondType)).findFirst().orElse(seconds.get(0));
        List<AgentPriceAreaMenuNode> thirds = second == null || CollectionUtils.isEmpty(second.getChildren())
                ? Collections.emptyList()
                : second.getChildren();
        AgentPriceAreaMenuNode third = CollectionUtils.isEmpty(thirds)
                ? null
                : thirds.stream().filter(item -> StringUtils.equals(item.getKey(), thirdType)).findFirst().orElse(thirds.get(0));
        return new SelectedArea(
                province.getKey(),
                second == null ? "" : second.getKey(),
                third == null ? "" : third.getKey()
        );
    }

    private String pickPreferred(String current, List<String> values) {
        if (CollectionUtils.isEmpty(values)) {
            return "";
        }
        return values.contains(current) ? current : values.get(0);
    }

    private List<String> listAvailableVersions() {
        List<String> versions = tariffAgentPriceMapper.selectVersions();
        if (CollectionUtils.isEmpty(versions)) {
            return Collections.emptyList();
        }
        return versions.stream()
                .filter(StringUtils::isNotBlank)
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    private List<String> buildMonthOptions(List<String> versions) {
        if (CollectionUtils.isEmpty(versions)) {
            return Collections.emptyList();
        }
        return versions.stream()
                .map(this::toDisplayMonth)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    private String resolveLatestVersion() {
        return resolveLatestVersion(listAvailableVersions());
    }

    private String resolveLatestVersion(List<String> versions) {
        if (CollectionUtils.isEmpty(versions)) {
            return resolveRequestedVersion(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        return versions.get(0);
    }

    private String resolveRequestedVersion(String dateOrMonth) {
        String input = StringUtils.defaultIfBlank(dateOrMonth, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        return resolveVersion(input);
    }

    private String resolveOptionVersion(String dateOrMonth, List<String> versions) {
        String exact = resolveRequestedVersion(dateOrMonth);
        if (!isDateString(dateOrMonth)) {
            return exact;
        }
        if (CollectionUtils.isNotEmpty(versions) && versions.contains(exact)) {
            return exact;
        }
        String month = LocalDate.parse(dateOrMonth).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return resolveRequestedVersion(month);
    }

    private String toDisplayMonth(String version) {
        if (StringUtils.isBlank(version)) {
            return "";
        }
        if (version.matches("\\d{4}")) {
            return "20" + version.substring(0, 2) + "-" + version.substring(2, 4);
        }
        if (version.matches("\\d{6}") && !version.startsWith("20")) {
            return "20" + version.substring(0, 2) + "-" + version.substring(2, 4);
        }
        if (version.matches("\\d{6}") && version.startsWith("20")) {
            return version.substring(0, 4) + "-" + version.substring(4, 6);
        }
        if (version.matches("\\d{8}")) {
            return version.substring(0, 4) + "-" + version.substring(4, 6);
        }
        if (version.matches("\\d{4}-\\d{2}")) {
            return version;
        }
        if (version.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return version.substring(0, 7);
        }
        return version;
    }

    private String resolveEffectiveStart(String version) {
        String displayDate = toDisplayDate(version);
        if (StringUtils.isNotBlank(displayDate)) {
            return displayDate;
        }
        String month = toDisplayMonth(version);
        return month.matches("\\d{4}-\\d{2}") ? month + "-01" : "";
    }

    private String resolveEffectiveEnd(String version) {
        String displayDate = toDisplayDate(version);
        if (StringUtils.isNotBlank(displayDate)) {
            return displayDate;
        }
        String month = toDisplayMonth(version);
        if (!month.matches("\\d{4}-\\d{2}")) {
            return "";
        }
        YearMonth yearMonth = YearMonth.parse(month);
        return yearMonth.atEndOfMonth().format(DateTimeFormatter.ISO_DATE);
    }

    private String toDisplayDate(String version) {
        if (StringUtils.isBlank(version)) {
            return "";
        }
        if (version.matches("\\d{6}") && !version.startsWith("20")) {
            return "20" + version.substring(0, 2) + "-" + version.substring(2, 4) + "-" + version.substring(4, 6);
        }
        if (version.matches("\\d{8}")) {
            return version.substring(0, 4) + "-" + version.substring(4, 6) + "-" + version.substring(6, 8);
        }
        if (version.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return version;
        }
        return "";
    }

    private String resolveDateOrMonth(String selectedDate, String yearMonth) {
        return StringUtils.defaultIfBlank(selectedDate, yearMonth);
    }

    private AgentPricePeriodResp buildPeriod(String type,
                                             List<FpgjPointResp> fpgj,
                                             List<FpgjPointResp> fallbackFpgj,
                                             List<BigDecimal> ddPrices,
                                             List<BigDecimal> spPrices,
                                             List<BigDecimal> fjPrices,
                                             List<BigDecimal> xsPrices,
                                             List<BigDecimal> xtyxPrices) {
        List<FpgjPointResp> periodPoints = filterByType(fpgj, type);
        if (CollectionUtils.isEmpty(periodPoints)) {
            periodPoints = filterByType(fallbackFpgj, type);
        }
        AgentPricePeriodResp resp = new AgentPricePeriodResp();
        if (CollectionUtils.isEmpty(periodPoints)) {
            resp.setDdPrice("0");
            resp.setDlPrice("0");
            resp.setSpPrice("0");
            resp.setFjPrice("0");
            resp.setTimes(Collections.emptyList());
            return resp;
        }

        int index = bizTimeTransIndex(periodPoints.get(0).getBizTime());
        BigDecimal ddPrice = scaledPrice(ddPrices, index);
        BigDecimal spPrice = scaledPrice(spPrices, index)
                .add(scaledPrice(xsPrices, index))
                .add(scaledPrice(xtyxPrices, index));
        BigDecimal fjPrice = scaledPrice(fjPrices, index);
        BigDecimal dlPrice = ddPrice.subtract(spPrice).subtract(fjPrice);

        resp.setDdPrice(formatPrice(ddPrice));
        resp.setDlPrice(formatPrice(dlPrice));
        resp.setSpPrice(formatPrice(spPrice));
        resp.setFjPrice(formatPrice(fjPrice));
        resp.setTimes(getBizTimeStartEnd(periodPoints.stream()
                .map(FpgjPointResp::getBizTime)
                .collect(Collectors.toList()), ","));
        return resp;
    }

    private List<FpgjPointResp> filterByType(List<FpgjPointResp> rows, String type) {
        if (CollectionUtils.isEmpty(rows)) {
            return Collections.emptyList();
        }
        return rows.stream()
                .filter(item -> StringUtils.equals(type, item.getFgvalue()))
                .collect(Collectors.toList());
    }

    private BigDecimal scaledPrice(List<BigDecimal> prices, int index) {
        if (CollectionUtils.isEmpty(prices) || index < 0 || index >= prices.size() || prices.get(index) == null) {
            return BigDecimal.ZERO;
        }
        return prices.get(index).multiply(RATE).stripTrailingZeros();
    }

    private String formatPrice(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).stripTrailingZeros().toPlainString();
    }

    private void validateQuery(AgentPriceQueryReq req) {
        if (req == null) {
            throwParam("查询参数不能为空");
        }
        if (StringUtils.isBlank(req.getProvinceCode())) {
            throwParam("省市区域不能为空");
        }
        if (StringUtils.isBlank(req.getSecondType())) {
            throwParam("二级分类不能为空");
        }
        if (StringUtils.isBlank(req.getThirdType())) {
            throwParam("三级分类不能为空");
        }
        if (StringUtils.isBlank(req.getUserType())) {
            throwParam("企业用电类别不能为空");
        }
        if (StringUtils.isBlank(req.getDyLevel())) {
            throwParam("企业用电压等级不能为空");
        }
        if (StringUtils.isBlank(req.getSfType())) {
            throwParam("收费类型不能为空");
        }
        if (StringUtils.isBlank(req.getSelectedDate()) && StringUtils.isBlank(req.getYearMonth())) {
            throwParam("电费日期不能为空");
        }
    }

    private AgentPriceQueryReq copy(AgentPriceQueryReq source) {
        AgentPriceQueryReq target = new AgentPriceQueryReq();
        if (source != null) {
            BeanUtils.copyProperties(source, target);
        }
        return target;
    }

    private String buildAreaLabel(AgentPriceAreaOption area) {
        if (area == null) {
            return "";
        }
        String province = StringUtils.defaultIfBlank(area.getProvinceName(), area.getProvinceCode());
        String city = StringUtils.defaultIfBlank(area.getThirdType(), area.getSecondType());
        if (StringUtils.isBlank(city) || StringUtils.equals(province, city)) {
            return province;
        }
        return province + " / " + city;
    }

    private static boolean isDateString(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException ignored) {
            return false;
        }
    }

    private static Integer bizTimeTransIndex(String bizTime) {
        if (StringUtils.isBlank(bizTime) || !bizTime.contains(":")) {
            return 0;
        }
        String[] arr = bizTime.split(":");
        return Integer.parseInt(arr[0]) * 4 + Integer.parseInt(arr[1]) / 15;
    }

    private static String indexTransBizTime(Integer index) {
        int hour = index / 4;
        int minute = index % 4;
        return (hour >= 10 ? String.valueOf(hour) : "0" + hour) + ":" + (minute * 15 > 0 ? minute * 15 : "00");
    }

    private static List<String> getBizTimeStartEnd(List<String> times, String separator) {
        List<String> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(times)) {
            return result;
        }
        String startTime = times.get(0);
        int startIndex = bizTimeTransIndex(startTime);
        if (times.size() == 1) {
            result.add(startTime + separator + indexTransBizTime(startIndex + 1));
            return result;
        }
        for (int i = 1; i < times.size(); i++) {
            int gap = bizTimeTransIndex(times.get(i)) - bizTimeTransIndex(times.get(i - 1));
            int endIndex = bizTimeTransIndex(times.get(i - 1)) + 1;
            String endTime = indexTransBizTime(endIndex);
            if (i == times.size() - 1) {
                if (gap > 1) {
                    result.add(startTime + separator + endTime);
                    startTime = times.get(i);
                }
                result.add(startTime + separator + indexTransBizTime(bizTimeTransIndex(times.get(i)) + 1));
            } else if (gap > 1) {
                result.add(startTime + separator + endTime);
                startTime = times.get(i);
            }
        }
        return result;
    }

    public static String resolveVersion(String yearMonth) {
        if (StringUtils.isNotBlank(yearMonth) && yearMonth.matches("\\d{4}|\\d{6}|\\d{8}")) {
            return yearMonth;
        }
        if (StringUtils.isNotBlank(yearMonth)) {
            Pattern pattern = Pattern.compile("^(\\d{4})-(\\d{2})(?:-(\\d{2}))?$");
            Matcher matcher = pattern.matcher(yearMonth);
            if (matcher.matches()) {
                String year = matcher.group(1).substring(2);
                String month = matcher.group(2);
                String day = matcher.group(3) == null ? "" : matcher.group(3);
                return StringUtils.isBlank(day) ? year + month : year + month + day;
            }
        }
        return "";
    }

    private void throwParam(String message) {
        throw new BaseException(StatusCode.C.getCode(), message);
    }

    private void throwNoData(String dateOrMonth) {
        String target = StringUtils.defaultIfBlank(dateOrMonth, "当前条件");
        throw new BaseException(StatusCode.TARIFF_NO_DATA.getCode(), "未查询到 " + target + " 对应的代理电价数据");
    }

    private static class SelectedArea {
        private final String provinceCode;
        private final String secondType;
        private final String thirdType;

        private SelectedArea(String provinceCode, String secondType, String thirdType) {
            this.provinceCode = provinceCode;
            this.secondType = secondType;
            this.thirdType = thirdType;
        }

        public String getProvinceCode() {
            return provinceCode;
        }

        public String getSecondType() {
            return secondType;
        }

        public String getThirdType() {
            return thirdType;
        }
    }
}
