package cn.sl.ehub.console.grid.service;

import cn.enn.iot.cto.CmdSetDTO;
import cn.enn.iot.cto.CmdSetData;
import cn.enn.iot.service.IotSetService;
import cn.enn.iot.vo.IotResultVo;
import cn.sl.ehub.service.dto.ControlIssueDTO;
import cn.sl.ehub.service.dto.RetryIssueDTO;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.service.resp.AggregatorDeviceDeliveryPowerPercentDetail;
import cn.sl.ehub.console.service.*;
import cn.sl.ehub.service.service.ControlIssueLogService;
import cn.sl.ehub.service.service.ClearIssueLogService;
import cn.sl.ehub.service.service.RetryIssueLogService;
import cn.sl.ehub.service.service.IotCmdSetLogService;
import cn.sl.ehub.service.service.ControlIssueConfigService;
import cn.sl.ehub.common.utils.MathUtils;
import cn.sl.ehub.service.vo.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fanneng.requestlog.common.RequestHolder;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jws.WebService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */

@Service
@WebService(serviceName = "IssueWebService",// 与接口中指定的name一致
        targetNamespace = "http://ws.la.enn.cn/",// 与接口中的命名空间一致,一般是接口的包名倒置
        endpointInterface = "cn.sl.ehub.console.grid.service.IssueWebService"// 接口地址
)
@Slf4j
public class IssueWebServiceImpl implements IssueWebService {

    @Resource
    private ControlIssueLogService controlIssueLogService;

    @Resource
    private ClearIssueLogService clearIssueLogService;

    @Resource
    private RetryIssueLogService retryIssueLogService;

    @Resource
    private LoadAggregatorDeliveryService loadAggregatorDeliveryService;

    @Resource
    private IotSetService iotSetService;

    @Resource
    private IotCmdSetLogService iotCmdSetLogService;

    @Resource
    private ITripartDataSynchronService tripartDataService;

    @Resource
    private ControlIssueConfigService controlIssueConfigService;

    @Value("${iot.callBackUrl}")
    private String callBackUrl;


    /**
     * 控制下发
     * <p>
     * 下发入参
     * {
     * "remoteId": "91855fca-7186-4dea-bf86-ce88b3626d9d",
     * "remoteName": "HUABEISG",
     * "cmdData": {
     * "组号-1": 控制值,
     * "组号-2": 控制时间,
     * "组号-3": AGC投运状态,
     * "组号-4": AGC正控信号
     * }
     * }
     * <p>
     * <p>
     * AGC投运状态，AGC正控信号，其中AGC投运状态和AGC正控信号在正常控制下发是都为1，非1时则不进行相应
     * <p>
     * 市场正式运行阶段，控制下发的周期为1分钟。
     *
     * @param controlIssueRequest
     * @return
     */
    @Override
    public String controlIssue(String controlIssueRequest) {

        log.info("controlIssueRequest:{}", controlIssueRequest);
        ControlIssueDTO controlIssueDTO = JSONObject.parseObject(controlIssueRequest, ControlIssueDTO.class);
        String groupNo = "";
        Map<String, String> cmdData = controlIssueDTO.getCmdData();
        Set<String> strings = cmdData.keySet();
        Optional<String> firstKeyOptional = strings.stream().findFirst();
        if (firstKeyOptional.isPresent()) {
            String firstKey = firstKeyOptional.orElse("");
            groupNo = firstKey.split("-")[0];
        }

        // 投退状态
        String status = "0";
        // 正控信息
        String signal = "0";
        // 下发命令（具体的值）
        String orderValue = "";
        // 下发时间
        String issueTime = "";

//        switch (groupNo) {
//            case "25":
//                orderValue = cmdData.get("25-1");
//                issueTime = cmdData.get("25-2");
//                status = cmdData.get("25-3");
//                signal = cmdData.get("25-4");
//                break;
//            case "26":
//                orderValue = cmdData.get("26-1");
//                issueTime = cmdData.get("26-2");
//                status = cmdData.get("26-3");
//                signal = cmdData.get("26-4");
//                break;
//            case "27":
//                orderValue = cmdData.get("27-1");
//                issueTime = cmdData.get("27-2");
//                status = cmdData.get("27-3");
//                signal = cmdData.get("27-4");
//                break;
//            default:
//                break;
//        }

        orderValue = cmdData.get(groupNo+"-1");
        issueTime = cmdData.get(groupNo+"-2");
        status = cmdData.get(groupNo+"-3");
        signal = cmdData.get(groupNo+"-4");

        String requestId = RequestHolder.request().getRequestId();

        // 入参日志落库
        ControlIssueLog log = new ControlIssueLog();
        log.setCreateTime(new Date());
        log.setCmdData(JSONObject.toJSONString(controlIssueDTO.getCmdData()));
        log.setRemoteId(controlIssueDTO.getRemoteId());
        log.setRemoteName(controlIssueDTO.getRemoteName());
        log.setIssueStatus("1");
        log.setGroupNo(groupNo);
        log.setRequestId(requestId);
        controlIssueLogService.addLog(log);

        // 下发状态status和下发信号signal都为1时才下发到设备
        double deliveryPower = 0.0d;
        if (StringUtils.equals(status, signal) && StringUtils.equals(status, "1")) {
            // 获取比例
            ResultVO<List<AggregatorDeviceDeliveryPowerPercentDetail>> resultVO = null;
            try {
                resultVO = tripartDataService.getAggregatorDeviceDeliveryPowerPercentDetailList(groupNo, DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));

                if (resultVO.getCode().intValue() == StatusCode.SUCCESS.getCode().intValue()) {
                    Optional<AggregatorDeviceDeliveryPowerPercentDetail> firstOptional = resultVO.getData().stream().filter(x -> "1337627644413157376".equals(x.getIotDeviceBaseId())).findFirst();

                    if (firstOptional.isPresent()) {
                        AggregatorDeviceDeliveryPowerPercentDetail aggregatorDeviceDeliveryPowerPercentDetail = firstOptional.get();
                        // 申报功率
                        deliveryPower = aggregatorDeviceDeliveryPowerPercentDetail.getDeliveryPower();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String issuable = controlIssueConfigService.getIssueConfig();
            if ("1".equals(issuable)) {
                CmdSetDTO cmdSetDTO = new CmdSetDTO();
                cmdSetDTO.setCallback(callBackUrl);
                cmdSetDTO.setTimestamp(Instant.now().getEpochSecond());

                CmdSetData cmdSetData = new CmdSetData();
                cmdSetData.setCimId("1337627644413157376");
                cmdSetData.setMetric("Pset");
                // 正式上线后使用申报功率
                //double setValue = getSetValue(orderValue, power, percentValue);
                double setValue = deliveryPower;
                cmdSetData.setValue(setValue);
                cmdSetDTO.setData(cmdSetData);

                // 目前科技园才能下发
                IotResultVo resultVo = iotSetService.cmdSetResult(cmdSetDTO);

                IotCmdSetLog iotCmdSetLog = new IotCmdSetLog();
                iotCmdSetLog.setIotCode(resultVo.getCode());
                iotCmdSetLog.setIotResult(JSONObject.toJSONString(resultVo));
                // 使用下发时间用华北
                issueTime = issueTime.substring(0, 19);
                DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
                DateTime parse = DateTime.parse(issueTime, dateTimeFormatter);
                iotCmdSetLog.setCmdSetTime(parse.toDate());
                iotCmdSetLog.setSystemCode("PARK01_EMS18");
                iotCmdSetLog.setCimId(cmdSetData.getCimId());
                iotCmdSetLog.setMetricCode(cmdSetData.getMetric());
                // 真正的下发值
                iotCmdSetLog.setTargetValue(String.valueOf(setValue));
                iotCmdSetLog.setCreateTime(new Date());
                iotCmdSetLog.setRequestId(requestId);
                //下发日志
                if (resultVo.getCode() == 10000) {
                    JSONObject jsonObject = (JSONObject) resultVo.getData();
                    if (null != jsonObject) {
                        String uid = jsonObject.getString("uid");
                        iotCmdSetLog.setuId(uid);
                    }
                }
                iotCmdSetLogService.insertLog(iotCmdSetLog);

                return resultVo.getMsg();
            } else {
                return ResultVO.success().getMsg();
            }

        } else {
            return ResultVO.success().getMsg();
        }
    }

    /**
     * orderValue 华北下发值MW
     * power      额定功率kW
     * 返回值      设备功率kW
     *
     * @param orderValue
     * @param power
     * @param percentValue
     * @return
     */
    private double getSetValue(String orderValue, double power, double percentValue) {

        double setValue = 0.0;
        if (Double.compare(power, 0.0d) == 0) {
            power = Math.abs(Double.valueOf(orderValue) * 1000d);
        }
        if (Double.compare(percentValue, 0.0d) > 0) {
            setValue = MathUtils.mulDoubleNull(Double.valueOf(orderValue) * 1000d, percentValue, 2);
            setValue = Double.compare(Math.abs(setValue), power) > 0 ? power : setValue;
        } else {
            setValue = Double.compare(Math.abs(Double.valueOf(orderValue)) * 1000d, power) > 0 ? power : Double.valueOf(orderValue) * 1000d;
        }

        return new BigDecimal(setValue).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    /**
     * 出清下发
     * <p>
     * [
     * {“CP-组号-1”:”值:秒级时间戳”, “CP-组号-2”:”值:秒级时间戳”,…, “CP-组号-40”:”值:秒级时间戳”},
     * {“PR-组号-1”:”值:秒级时间戳”, “PR-组号-2”:”值:秒级时间戳”,…, “PR-组号-40”:”值:秒级时间戳”},
     * {“FEE-组号-1”:”值:秒级时间戳”, “FEE-组号-2”:”值:秒级时间戳”,…, “FEE-组号-40”:”值:秒级时间戳”},
     * {“DAP-组号-1”:”值:秒级时间戳”, “DAP-组号-2”:”值:秒级时间戳”,…, “DAP-组号-96”:”值:秒级时间戳”}
     * ]
     * 其中，CP表示出清价格，PR为调峰收益率，FEE为调峰收益，DAP为日前计划，具体值后边的秒级时间戳为值的对应时间。
     * <p>
     * <p>
     * 市场正式运行阶段，每日23点出清前一日的出清价格，调峰收益率，调峰收益和日前计划，在23：30时补发一次出清
     *
     * @param clearIssueRequest
     * @return
     */
    @Override
    public String clearIssue(String clearIssueRequest) {

        log.info("clearIssueRequest:{}", clearIssueRequest);

        ClearIssueLog clearIssueLog = new ClearIssueLog();
        clearIssueLog.setCmdData(clearIssueRequest);
        clearIssueLog.setCreateTime(new Date());
        clearIssueLog.setRequestId(RequestHolder.request().getRequestId());

        JSONObject requestJson = JSONObject.parseObject(clearIssueRequest);
        JSONArray data = requestJson.getJSONArray("data");

        String groupNo = "";
        Date clearDate = new Date();
        if (!data.isEmpty()) {
            LinkedHashMap<String, String> content = JSONObject.parseObject(data.get(0).toString(), new TypeReference<LinkedHashMap<String, String>>() {
            });
            Set<String> strings = content.keySet();
            Optional<String> firstKeyOptional = strings.stream().findFirst();
            if (firstKeyOptional.isPresent()) {
                String firstKey = firstKeyOptional.orElse("");
                groupNo = firstKey.split("-")[1];
                String valueAndTimestamp = content.get(firstKey);
                String timestamp = valueAndTimestamp.split(":")[1];
                clearDate.setTime(Long.parseLong(timestamp) * 1000);

                clearIssueLog.setClearDate(clearDate);
                clearIssueLog.setGroupNo(groupNo);
            }
        }

        clearIssueLogService.addLog(clearIssueLog);

        try {
            tripartDataService.dealData(clearIssueRequest);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("出清数据同步失败:{}", clearIssueRequest);
        }

        return ResultVO.success().getMsg();
    }

    /**
     * 总加补招下发
     * <p>
     * 入参：
     * {
     * "remoteId": "91855fca-7186-4dea-bf86-ce88b3626d9d",
     * "remoteName": "HUABEISG",
     * "group":"组号",
     * "type":"total",
     * "timestamp":"10位秒级时间戳"
     * }
     * <p>
     * 返回：
     * <p>
     * {
     * "组号-点号": 值,
     * "组号-点号": 值,
     * "组号-点号": 值
     * }
     * <p>
     * 数据缺失时手动下发，聚合商需要把相应总加数据保存2个月
     *
     * @param retryIssueRequest
     * @return
     */
    @Override
    public String totalRetryIssue(String retryIssueRequest) {

        log.info("retryIssueRequest:{}", retryIssueRequest);
        String request = retryIssueRequest.replace("total", "\"total\"");
        RetryIssueDTO retryIssueDTO = JSONObject.parseObject(request, RetryIssueDTO.class);
        String requestId = RequestHolder.request().getRequestId();
        log.info("requestId{}", requestId);
        ResultVO<String> response = loadAggregatorDeliveryService.totalData(retryIssueDTO);

        RetryIssueLog retryIssueLog = new RetryIssueLog();
        retryIssueLog.setCreateTime(new Date());
        retryIssueLog.setGroupNo(retryIssueDTO.getGroup());
        retryIssueLog.setRemoteId(retryIssueDTO.getRemoteId());
        retryIssueLog.setRemoteName(retryIssueDTO.getRemoteName());
        retryIssueLog.setTimestamp(Long.valueOf(retryIssueDTO.getTimestamp()));
        //retryIssueLog.setTimestamp(Instant.now().getEpochSecond());
        retryIssueLog.setType(retryIssueDTO.getType());
        retryIssueLog.setResponse(JSONObject.toJSONString(response));
        retryIssueLogService.addLog(retryIssueLog);

        return response.getMsg();

    }


    /**
     * 单体量测文件补招
     * <p>
     * 入参：
     * <p>
     * {
     * "remoteId": "91855fca-7186-4dea-bf86-ce88b3626d9d",
     * "remoteName": "HUABEISG",
     * "group":"组号",
     * "type":"single",
     * "timestamp":"10位秒级时间戳"
     * }
     * <p>
     * 返回：
     * String格式：将byte字节的文件内容以Base64.getEncoder().encodeToString()接口转为String
     *
     * @param retryIssueRequest
     * @return
     */
    @Override
    public String singleRetryIssue(String retryIssueRequest) {

        log.info("retryIssueRequest:{}", retryIssueRequest);
        String request = retryIssueRequest.replace("single", "\"single\"");
        RetryIssueDTO retryIssueDTO = JSONObject.parseObject(request, RetryIssueDTO.class);

        // 通过feign调用delivery项目接口
        ResultVO<String> response = loadAggregatorDeliveryService.singleMeas(retryIssueDTO);

        RetryIssueLog retryIssueLog = new RetryIssueLog();
        retryIssueLog.setCreateTime(new Date());
        retryIssueLog.setGroupNo(retryIssueDTO.getGroup());
        retryIssueLog.setRemoteId(retryIssueDTO.getRemoteId());
        retryIssueLog.setRemoteName(retryIssueDTO.getRemoteName());
        retryIssueLog.setTimestamp(Long.valueOf(retryIssueDTO.getTimestamp()));
        //retryIssueLog.setTimestamp(Instant.now().getEpochSecond());
        retryIssueLog.setType(retryIssueDTO.getType());
        retryIssueLog.setResponse(JSONObject.toJSONString(response));
        retryIssueLogService.addLog(retryIssueLog);

        return response.getMsg();
    }


}


