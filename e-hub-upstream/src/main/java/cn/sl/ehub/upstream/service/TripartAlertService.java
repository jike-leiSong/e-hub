package cn.sl.ehub.upstream.service;

import cn.sl.ehub.common.enums.StateGridEnum;
import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import cn.sl.ehub.service.mapper.TripartServiceAlertMapper;
import cn.sl.ehub.common.vo.ResultVO;
import cn.sl.ehub.service.vo.TripartServiceAlert;
// import cn.enn.sms.req.ApplicationTokenReq;
// import cn.enn.sms.req.SendMessageReq;
// import cn.enn.sms.req.SmsRange;
// import cn.enn.sms.resp.SmsResultVO;
import cn.sl.ehub.upstream.service.SmsAlertService;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 告警服务
 * @Author sl
 * @Date 2026-05-28
 */
@Service
@Slf4j
public class TripartAlertService {

    @Value(value = "${sms.applicationName}")
    private String applicationName;
    @Value(value = "${sms.appId}")
    private String appId;
    @Value(value = "${sms.appSecret}")
    private String appSecret;
    @Value(value = "${sms.alertTemplateCode}")
    private String alertTemplateCode;
    @Value("${nari.url.total}")
    private List<String> huabeiUrl;

    @Resource
    private HuabeiUrlService urlCheckService;

    @Resource
    private TripartServiceAlertMapper tripartServiceAlertMapper;

    @Resource
    private SmsAlertService smsService;

    @Async
    public void sendDeliveryAlert(String tripartCode){
        smsSendBatch(tripartCode);
    }

    public ResultVO<String> getHeartBeat(String tripartCode) {

        if (!StateGridEnum.getCodes().contains(tripartCode)) {
            return ResultVO.fail(StatusCode.F_CODE_NOT_EXIST.getCode(), StatusCode.F_CODE_NOT_EXIST.getMsg());
        }

        List<String> toBeCheckedUrls = Lists.newArrayList();
        switch (StateGridEnum.getEnumByCode(tripartCode)) {
            case HUABEI:
                toBeCheckedUrls = huabeiUrl;
                break;
            default:
                break;
        }
        List<String> allAvailableUrl = urlCheckService.getAllAvailableUrl(toBeCheckedUrls);
        String tripartName = "";
        if (CollectionUtils.isEmpty(allAvailableUrl)) {
            tripartName = StateGridEnum.getEnumByCode(tripartCode).getName();
            smsSendBatch(tripartCode);
        }
        return ResultVO.success(StringUtils.isNotEmpty(tripartName) ? tripartName + "服务不可用！" : tripartName);
    }

    public ResultVO<String> smsSendBatch(String tripartCode) {
        try {
            log.warn("SMS服务已禁用，跳过发送告警短信 - tripartCode: {}", tripartCode);
            // SMS服务依赖已删除，暂时禁用短信告警功能
            // TODO: 如需启用，请实现新的短信服务
        } catch (Exception e) {
            e.printStackTrace();
            log.info("告警短信发送异常");
        }
        return ResultVO.success();
    }

    public String getToken() {
        log.warn("SMS服务已禁用");
        return "";
        // SMS服务依赖已删除
        // TODO: 如需启用，请实现新的短信服务
    }

    /**
     * 根据编码获取告警联系人
     *
     * @param tripartCode
     * @return
     */
    public List<TripartServiceAlert> getReceiversByCode(String tripartCode) {
        Example example = new Example(TripartServiceAlert.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tripartCode", tripartCode);
        criteria.andEqualTo("status", "1");
        return tripartServiceAlertMapper.selectByExample(example);
    }
}
