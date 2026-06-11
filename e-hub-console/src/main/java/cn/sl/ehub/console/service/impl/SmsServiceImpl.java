package cn.sl.ehub.console.service.impl;

import cn.sl.ehub.common.enums.SmsRoleTypeEnum;
import cn.sl.ehub.common.enums.SmsTimeTypeEnum;
import cn.sl.ehub.common.enums.StateGridEnum;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.resp.AggregatorEntProfitResp;
import cn.sl.ehub.service.resp.AggregatorProfitResp;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.service.vo.AggregatorSms;
import cn.sl.ehub.service.vo.AggregatorSmsLog;
import cn.sl.ehub.common.vo.ResultVO;
import cn.enn.sms.req.ApplicationTokenReq;
import cn.enn.sms.req.DemandResponseGuangZhouReq;
import cn.enn.sms.req.SendMessageReq;
import cn.enn.sms.req.SmsRange;
import cn.enn.sms.resp.AliveClient;
import cn.enn.sms.resp.SmsResultVO;
import cn.enn.sms.service.FnwMessage;
import cn.enn.sms.service.SmsService;
import cn.enn.sms.vo.TemplateParamsVO;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Service
@Slf4j
public class SmsServiceImpl implements ISmsService {

    @Value(value = "${sms.applicationName}")
    private String applicationName;
    @Value(value = "${sms.appId}")
    private String appId;
    @Value(value = "${sms.appSecret}")
    private String appSecret;
    @Value(value = "${sms.templateCode}")
    private String templateCode;
    @Value(value = "${sms.userTemplateCode}")
    private String userTemplateCode;
    @Resource
    private SmsService smsService;
    @Resource
    private AggregatorSmsLogService aggregatorSmsLogService;
    @Resource
    private AggregatorSmsService aggregatorSmsService;
    @Resource
    private FnwMessage fnwMessage;
    @Autowired
    private IDataService dataService;
    @Resource
    private IAggregatorSmsService guangzhouAggregatorSmsService;

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * type
     * MM - 月
     * YY - 年
     *
     * @return
     */
    @Override
    public ResultVO<String> smsSendBatch() {

        DateTime now = DateTime.now();
        DateTime.Property property = now.monthOfYear();
        int month = property.get();

        // 每年1月1日08:00,发送去年收益,配置月度时定时任务时直接返回成功
        // 从2月开始，每月1日08:00，发送上月收益
        String type = SmsTimeTypeEnum.MM.getCode();
        if (1 == month) {
            type = SmsTimeTypeEnum.YY.getCode();
        }

        List<AggregatorSms> receivers = aggregatorSmsService.getReceivers(SmsRoleTypeEnum.AGG.getCode(), StateGridEnum.HUABEI.getCode());

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_TIME_FORMAT);

        DateTime begin = DateTime.parse(now.toString("yyyy-MM-dd 00:00:00"), dateTimeFormatter);
        DateTime end = DateTime.parse(now.toString("yyyy-MM-dd 23:59:59"), dateTimeFormatter);

        // 上月
        DateTime lastDateOfMonth = end.minusMonths(1).dayOfMonth().withMaximumValue();
        DateTime firstDateOfMonth = begin.minusMonths(1).dayOfMonth().withMinimumValue();

        // 上年
        DateTime lastDateOfYear = end.minusYears(1).dayOfYear().withMaximumValue();
        DateTime firstDateOfYear = begin.minusYears(1).dayOfYear().withMinimumValue();

        String startDate = "";
        String endDate = "";

        switch (type) {
            case "MM":
                startDate = firstDateOfMonth.toString();
                endDate = lastDateOfMonth.toString();
                break;
            case "YY":
                startDate = firstDateOfYear.toString();
                endDate = lastDateOfYear.toString();
                break;
            default:
                break;
        }

        try {
            AggregatorProfitResp aggregatorProfitResp = dataService.getAggregatorProfitResp(startDate, endDate);
            String token = getToken();
            TemplateParamsVO templateParams = new TemplateParamsVO();
            // 一位小数
            BigDecimal issueIncome = new BigDecimal(String.valueOf(null == aggregatorProfitResp.getIssueProfit() ? 0.0d : aggregatorProfitResp.getIssueProfit())).setScale(1, BigDecimal.ROUND_HALF_UP);
            // 一位小数 Kwh  转Mwh
            BigDecimal regulation = new BigDecimal(String.valueOf(null == aggregatorProfitResp.getElectricQuantity() ? 0.0d : aggregatorProfitResp.getElectricQuantity())).divide(BigDecimal.valueOf(1000), 1, BigDecimal.ROUND_HALF_UP);
            // 一位小数
            templateParams.setIssueIncome(issueIncome.toString());
            templateParams.setRegulation(regulation.toString());
            templateParams.setTime(SmsTimeTypeEnum.getEnumByCode(type).getDesc());

            SendMessageReq sendMessageReq = new SendMessageReq();
            sendMessageReq.setApplicationName(applicationName);
            sendMessageReq.setToken(token);

            receivers.forEach(receiver -> {
                Map<String, String> metrics = new HashMap<>();
                metrics.put("name", receiver.getName());
                metrics.put("time", templateParams.getTime());
                metrics.put("regulation", templateParams.getRegulation());
                metrics.put("issueIncome", templateParams.getIssueIncome());

                SmsRange smsRange = new SmsRange();
                smsRange.setPhones(Lists.newArrayList(receiver.getPhone()));
                smsRange.setTemplateCode(templateCode);
                smsRange.setMetrics(Lists.newArrayList(metrics));
                sendMessageReq.setSmsRange(smsRange);
                sendMessageReq.setEntId(receiver.getEntId());

                SmsResultVO<Object> result = smsService.messageSend(sendMessageReq);
                String status = "0";
                if (result.getCode().intValue() == StatusCode.SUCCESS.getCode()) {
                    status = "1";
                }

                AggregatorSmsLog log = new AggregatorSmsLog();
                log.setCreateTime(new Date());
                log.setName(receiver.getName());
                log.setPhone(receiver.getPhone());
                log.setStatus(status);
                log.setRole(receiver.getRole());
                log.setContent(JSONObject.toJSONString(sendMessageReq));
                log.setGridCode(StateGridEnum.HUABEI.getCode());
                aggregatorSmsLogService.addSmsLog(log);
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.info("聚合商短信批量发送异常");
        }
        return ResultVO.success();

    }

    @Override
    public ResultVO<String> smsSendSpecified(String type, String phone) {

        DateTime.Property property = DateTime.now().monthOfYear();
        int month = property.get();

        // 一月直发年度短信
        if (StringUtils.equalsIgnoreCase(type, SmsTimeTypeEnum.MM.getCode()) && 1 == month) {
            return ResultVO.success();
        }
        if (StringUtils.equalsIgnoreCase(type, SmsTimeTypeEnum.YY.getCode()) && 1 != month) {
            return ResultVO.success();
        }

        AggregatorSms receiver = aggregatorSmsService.getReceiverByPhone(phone);

        if (null == receiver) {
            return ResultVO.fail(StatusCode.UAC_SMS_NO_PHONE.getCode(), StatusCode.UAC_SMS_NO_PHONE.getMsg());
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_TIME_FORMAT);
        DateTime now = DateTime.now();
        DateTime begin = DateTime.parse(now.toString("yyyy-MM-dd 00:00:00"), dateTimeFormatter);
        DateTime end = DateTime.parse(now.toString("yyyy-MM-dd 23:59:59"), dateTimeFormatter);

        DateTime lastDateOfMonth = end.minusMonths(1).dayOfMonth().withMaximumValue();
        DateTime firstDateOfMonth = begin.minusMonths(1).dayOfMonth().withMinimumValue();

        DateTime lastDateOfYear = end.minusYears(1).dayOfYear().withMaximumValue();
        DateTime firstDateOfYear = begin.minusYears(1).dayOfYear().withMinimumValue();

        String startDate = "";
        String endDate = "";

        switch (type) {
            case "MM":
                startDate = firstDateOfMonth.toString();
                endDate = lastDateOfMonth.toString();
                break;
            case "YY":
                startDate = firstDateOfYear.toString();
                endDate = lastDateOfYear.toString();
                break;
            default:
                break;
        }

        try {
            AggregatorProfitResp aggregatorProfitResp = dataService.getAggregatorProfitResp(startDate, endDate);
            String token = getToken();

            TemplateParamsVO templateParams = new TemplateParamsVO();

            // 一位小数
            BigDecimal issueIncome = new BigDecimal(String.valueOf(null == aggregatorProfitResp.getIssueProfit() ? 0.0d : aggregatorProfitResp.getIssueProfit())).setScale(1, BigDecimal.ROUND_HALF_UP);
            // 一位小数 Kwh  转Mwh
            BigDecimal regulation = new BigDecimal(String.valueOf(null == aggregatorProfitResp.getElectricQuantity() ? 0.0d : aggregatorProfitResp.getElectricQuantity())).divide(BigDecimal.valueOf(1000), 1, BigDecimal.ROUND_HALF_UP);
            templateParams.setIssueIncome(issueIncome.toString());
            templateParams.setRegulation(regulation.toString());
            templateParams.setTime(SmsTimeTypeEnum.getEnumByCode(type).getDesc());

            SendMessageReq sendMessageReq = new SendMessageReq();
            sendMessageReq.setApplicationName(applicationName);
            sendMessageReq.setToken(token);

            Map<String, String> metrics = new HashMap<>();
            metrics.put("name", receiver.getName());
            metrics.put("time", templateParams.getTime());
            metrics.put("regulation", templateParams.getRegulation());
            metrics.put("issueIncome", templateParams.getIssueIncome());

            SmsRange smsRange = new SmsRange();
            smsRange.setPhones(Lists.newArrayList(receiver.getPhone()));
            smsRange.setTemplateCode(templateCode);
            smsRange.setMetrics(Lists.newArrayList(metrics));
            sendMessageReq.setSmsRange(smsRange);
            sendMessageReq.setEntId(receiver.getEntId());

            SmsResultVO<Object> result = smsService.messageSend(sendMessageReq);
            String status = "0";
            if (result.getCode().intValue() == StatusCode.SUCCESS.getCode()) {
                status = "1";
            }

            AggregatorSmsLog log = new AggregatorSmsLog();
            log.setCreateTime(new Date());
            log.setName(receiver.getName());
            log.setPhone(phone);
            log.setStatus(status);
            log.setRole(receiver.getRole());
            log.setContent(JSONObject.toJSONString(sendMessageReq));
            log.setGridCode(StateGridEnum.HUABEI.getCode());
            aggregatorSmsLogService.addSmsLog(log);

        } catch (Exception e) {
            e.printStackTrace();
            log.info("聚合商短信发送异常");
        }

        return ResultVO.success();

    }

    @Override
    public String getToken() {
        ApplicationTokenReq applicationTokenReq = new ApplicationTokenReq(appId, appSecret, String.valueOf(System.currentTimeMillis()));
        SmsResultVO<String> result = smsService.getToken(applicationTokenReq);
        if (result.getCode().intValue() != StatusCode.SUCCESS.getCode().intValue()) {
            throw new BaseException(StatusCode.SMS_TOKEN_ERROR.getCode(), StatusCode.SMS_TOKEN_ERROR.getMsg());
        }
        return result.getData();
    }

    @Override
    public List<AliveClient> getAliveClientList(String entId) {
        ResultVO<List<AliveClient>> result = fnwMessage.getAliveClients(entId);
        if (null == result || !result.getCode().equals(StatusCode.SUCCESS.getCode())) {
            throw new BaseException(StatusCode.ERROR.getCode(), "查询企业登录用户失败");
        }
        return result.getData();
    }

    @Override
    public ResultVO<Boolean> sendSocket(SendMessageReq req) {
        try {
            String token = getToken();
            if (StringUtils.isNotEmpty(token)) {
                req.setToken(token);
            }
            List<AliveClient> aliveClientList = getAliveClientList(req.getEntId());
            if (CollectionUtils.isNotEmpty(aliveClientList)) {
                List<String> openIdList = aliveClientList.stream().map(AliveClient::getOpenId).distinct().collect(Collectors.toList());
                req.setOpenIds(openIdList);
            }
            if (StringUtils.isEmpty(req.getApplicationName())) {
                req.setApplicationName("common");
            }
            req.setMessageType("SOCKET");
            log.info("推送消息,{}", JSONObject.toJSONString(req));
            smsService.messageSend(req);
        } catch (Exception e) {
            throw new BaseException(StatusCode.ERROR.getCode(), "发送消息失败");
        }
        return ResultVO.success(true);
    }

    @Override
    public ResultVO<String> smsEntSendBatch() {

        DateTime.Property property = DateTime.now().monthOfYear();
        int month = property.get();

        // 每年1月1日08:00,发送去年收益,配置月度时定时任务时直接返回成功
        // 从2月开始，每月1日08:00，发送上月收益
        String timeType = SmsTimeTypeEnum.MM.getCode();
        if (1 == month) {
            timeType = SmsTimeTypeEnum.YY.getCode();
        }

        List<AggregatorSms> receivers = aggregatorSmsService.getReceivers(SmsRoleTypeEnum.ENT.getCode(), StateGridEnum.HUABEI.getCode());

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_TIME_FORMAT);
        DateTime now = DateTime.now();
        DateTime begin = DateTime.parse(now.toString("yyyy-MM-dd 00:00:00"), dateTimeFormatter);
        DateTime end = DateTime.parse(now.toString("yyyy-MM-dd 23:59:59"), dateTimeFormatter);

        DateTime lastDateOfMonth = end.minusMonths(1).dayOfMonth().withMaximumValue();
        DateTime firstDateOfMonth = begin.minusMonths(1).dayOfMonth().withMinimumValue();

        DateTime lastDateOfYear = end.minusYears(1).dayOfYear().withMaximumValue();
        DateTime firstDateOfYear = begin.minusYears(1).dayOfYear().withMinimumValue();

        String startDate = "";
        String endDate = "";

        switch (timeType) {
            case "MM":
                startDate = firstDateOfMonth.toString();
                endDate = lastDateOfMonth.toString();
                break;
            case "YY":
                startDate = firstDateOfYear.toString();
                endDate = lastDateOfYear.toString();
                break;
            default:
                break;
        }

        try {
            Map<String, AggregatorEntProfitResp> entProfitRespMap = dataService.getEntProfitRespMap(startDate, endDate);
            String token = getToken();
            SendMessageReq sendMessageReq = new SendMessageReq();
            sendMessageReq.setApplicationName(applicationName);
            sendMessageReq.setToken(token);

            String finalTimeType = timeType;
            receivers.forEach(receiver -> {
                AggregatorEntProfitResp aggregatorEntProfitResp = entProfitRespMap.getOrDefault(receiver.getEntId(), new AggregatorEntProfitResp());

                TemplateParamsVO templateParams = new TemplateParamsVO();
                // 一位小数
                BigDecimal issueIncome = new BigDecimal(String.valueOf(null == aggregatorEntProfitResp.getEntProfit() ? 0.0d : aggregatorEntProfitResp.getEntProfit())).setScale(1, BigDecimal.ROUND_HALF_UP);
                // 一位小数 Kwh  转Mwh
                BigDecimal regulation = new BigDecimal(String.valueOf(null == aggregatorEntProfitResp.getElectricQuantity() ? 0.0d : aggregatorEntProfitResp.getElectricQuantity())).divide(BigDecimal.valueOf(1000), 1, BigDecimal.ROUND_HALF_UP);
                templateParams.setIssueIncome(issueIncome.toString());
                templateParams.setRegulation(regulation.toString());
                templateParams.setTime(SmsTimeTypeEnum.getEnumByCode(finalTimeType).getDesc());

                Map<String, String> metrics = new HashMap<>();
                metrics.put("name", receiver.getName());
                metrics.put("time", templateParams.getTime());
                metrics.put("regulation", templateParams.getRegulation());
                metrics.put("issueIncome", templateParams.getIssueIncome());

                SmsRange smsRange = new SmsRange();
                smsRange.setPhones(Lists.newArrayList(receiver.getPhone()));
                smsRange.setTemplateCode(userTemplateCode);
                smsRange.setMetrics(Lists.newArrayList(metrics));
                sendMessageReq.setSmsRange(smsRange);

                SmsResultVO<Object> result = smsService.messageSend(sendMessageReq);
                String status = "0";
                if (result.getCode().intValue() == StatusCode.SUCCESS.getCode()) {
                    status = "1";
                }

                AggregatorSmsLog log = new AggregatorSmsLog();
                log.setCreateTime(new Date());
                log.setName(receiver.getName());
                log.setPhone(receiver.getPhone());
                log.setStatus(status);
                log.setRole(receiver.getRole());
                log.setContent(JSONObject.toJSONString(sendMessageReq));
                log.setGridCode(StateGridEnum.HUABEI.getCode());
                aggregatorSmsLogService.addSmsLog(log);
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.info("聚合商用户短信批量发送异常");
        }
        return ResultVO.success();
    }

    @Override
    public ResultVO<String> smsEntSendSpecified(String timeType, String phone) {

        DateTime.Property property = DateTime.now().monthOfYear();
        int month = property.get();

        // 一月直发年度短信
        if (StringUtils.equalsIgnoreCase(timeType, SmsTimeTypeEnum.MM.getCode()) && 1 == month) {
            return ResultVO.success();
        }
        if (StringUtils.equalsIgnoreCase(timeType, SmsTimeTypeEnum.YY.getCode()) && 1 != month) {
            return ResultVO.success();
        }

        AggregatorSms receiver = aggregatorSmsService.getReceiverByPhone(phone);
        if (null == receiver) {
            return ResultVO.fail(StatusCode.UAC_SMS_NO_PHONE.getCode(), StatusCode.UAC_SMS_NO_PHONE.getMsg());
        }

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(DATE_TIME_FORMAT);
        DateTime now = DateTime.now();
        DateTime begin = DateTime.parse(now.toString("yyyy-MM-dd 00:00:00"), dateTimeFormatter);
        DateTime end = DateTime.parse(now.toString("yyyy-MM-dd 23:59:59"), dateTimeFormatter);

        DateTime lastDateOfMonth = end.minusMonths(1).dayOfMonth().withMaximumValue();
        DateTime firstDateOfMonth = begin.minusMonths(1).dayOfMonth().withMinimumValue();

        DateTime lastDateOfYear = end.minusYears(1).dayOfYear().withMaximumValue();
        DateTime firstDateOfYear = begin.minusYears(1).dayOfYear().withMinimumValue();

        String startDate = "";
        String endDate = "";

        switch (timeType) {
            case "MM":
                startDate = firstDateOfMonth.toString();
                endDate = lastDateOfMonth.toString();
                break;
            case "YY":
                startDate = firstDateOfYear.toString();
                endDate = lastDateOfYear.toString();
                break;
            default:
                break;
        }

        try {
            Map<String, AggregatorEntProfitResp> entProfitRespMap = dataService.getEntProfitRespMap(startDate, endDate);
            String token = getToken();
            AggregatorEntProfitResp aggregatorEntProfitResp = entProfitRespMap.get(receiver.getEntId());
            TemplateParamsVO templateParams = new TemplateParamsVO();

            // 一位小数
            BigDecimal issueIncome = new BigDecimal(String.valueOf(null == aggregatorEntProfitResp.getEntProfit() ? 0.0d : aggregatorEntProfitResp.getEntProfit())).setScale(1, BigDecimal.ROUND_HALF_UP);
            // 一位小数 Kwh  转Mwh
            BigDecimal regulation = new BigDecimal(String.valueOf(null == aggregatorEntProfitResp.getElectricQuantity() ? 0.0d : aggregatorEntProfitResp.getElectricQuantity())).divide(BigDecimal.valueOf(1000), 1, BigDecimal.ROUND_HALF_UP);
            // 一位小数
            templateParams.setIssueIncome(issueIncome.toString());
            templateParams.setRegulation(regulation.toString());
            templateParams.setTime(SmsTimeTypeEnum.getEnumByCode(timeType).getDesc());

            SendMessageReq sendMessageReq = new SendMessageReq();
            sendMessageReq.setApplicationName(applicationName);
            sendMessageReq.setEntId(receiver.getEntId());
            sendMessageReq.setToken(token);

            Map<String, String> metrics = new HashMap<>();
            metrics.put("name", receiver.getName());
            metrics.put("time", templateParams.getTime());
            metrics.put("regulation", templateParams.getRegulation());
            metrics.put("issueIncome", templateParams.getIssueIncome());

            SmsRange smsRange = new SmsRange();
            smsRange.setPhones(Lists.newArrayList(receiver.getPhone()));
            smsRange.setTemplateCode(userTemplateCode);
            smsRange.setMetrics(Lists.newArrayList(metrics));
            sendMessageReq.setSmsRange(smsRange);

            SmsResultVO<Object> result = smsService.messageSend(sendMessageReq);
            String status = "0";
            if (result.getCode().intValue() == StatusCode.SUCCESS.getCode()) {
                status = "1";
            }

            AggregatorSmsLog log = new AggregatorSmsLog();
            log.setCreateTime(new Date());
            log.setName(receiver.getName());
            log.setPhone(phone);
            log.setStatus(status);
            log.setRole(receiver.getRole());
            log.setContent(JSONObject.toJSONString(sendMessageReq));
            log.setGridCode(StateGridEnum.HUABEI.getCode());
            aggregatorSmsLogService.addSmsLog(log);

        } catch (Exception e) {
            e.printStackTrace();
            log.info("聚合商短信发送异常");
        }

        return ResultVO.success();

    }

    @Override
    public ResultVO<String> smsEntWarningSend(String entId) {
        List<AggregatorSms> entWarningSend = guangzhouAggregatorSmsService.getEntWarningSend(entId);
        if (!CollectionUtils.isNotEmpty(entWarningSend)) {
            return ResultVO.fail(StatusCode.UAC_SMS_NO_PHONE.getCode(), StatusCode.UAC_SMS_NO_PHONE.getMsg());
        }
        try {

            String token = getToken();
            TemplateParamsVO templateParams = new TemplateParamsVO();

            SendMessageReq sendMessageReq = new SendMessageReq();
            sendMessageReq.setApplicationName(applicationName);
            sendMessageReq.setEntId(entId);
            sendMessageReq.setToken(token);

            entWarningSend.forEach(e -> {
                Map<String, String> metrics = new HashMap<>();
                metrics.put("name", e.getName());
                metrics.put("time", templateParams.getTime());
                metrics.put("regulation", templateParams.getRegulation());
                metrics.put("issueIncome", templateParams.getIssueIncome());

                SmsRange smsRange = new SmsRange();
                smsRange.setPhones(Lists.newArrayList(e.getPhone()));
                smsRange.setTemplateCode(userTemplateCode);
                smsRange.setMetrics(Lists.newArrayList(metrics));
                sendMessageReq.setSmsRange(smsRange);

                SmsResultVO<Object> result = smsService.messageSend(sendMessageReq);
                String status = "0";
                if (result.getCode().intValue() == StatusCode.SUCCESS.getCode()) {
                    status = "1";
                }

                AggregatorSmsLog log = new AggregatorSmsLog();
                log.setCreateTime(new Date());
                log.setName(e.getName());
                log.setPhone(e.getPhone());
                log.setStatus(status);
                log.setRole(e.getRole());
                log.setContent(JSONObject.toJSONString(sendMessageReq));
                log.setGridCode(StateGridEnum.HUABEI.getCode());
                aggregatorSmsLogService.addSmsLog(log);
            });

        } catch (Exception e) {
            e.printStackTrace();
            log.info("聚合商短信发送异常");
        }

        return ResultVO.success();
    }

}
