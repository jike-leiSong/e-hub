package cn.sl.ehub.common.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * 钉钉工具类
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
public class DingUtil {

    /**
     * 自定义机器人推送
     *
     * @param secret
     * @param webHook
     * @param content
     * @param isAtAll
     * @param mobileList
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
    public static void sendMsg(String secret, String webHook, String content, boolean isAtAll, List<String> mobileList) throws Exception {
        Long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        String dingUrl = webHook + "&timestamp=" + timestamp + "&sign=" + sign;
        try {
            String reqStr = buildReqStr(content, isAtAll, mobileList);
            log.info("钉钉发送消息地址：{}，参数：{}", dingUrl, reqStr);
            String result = HttpUtil.post(dingUrl, reqStr);
            log.info("钉钉发送消息返回结果：{}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义机器人推送所有人
     *
     * @param secret
     * @param webHook
     * @param content
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
    public static void sendMsgAll(String secret, String webHook, String content) throws Exception {
        Long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        String dingUrl = webHook + "&timestamp=" + timestamp + "&sign=" + sign;
        try {
            String reqStr = buildReqStr(content, true, Lists.newArrayList());
            log.info("钉钉发送消息地址：{}，参数：{}", dingUrl, reqStr);
            String result = HttpUtil.post(dingUrl, reqStr);
            log.info("钉钉发送消息返回结果：{}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自定义机器人推送指定人员
     *
     * @param secret
     * @param webHook
     * @param content
     * @param mobileList
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeyException
     */
    public static void sendMsgByMobile(String secret, String webHook, String content, List<String> mobileList) throws Exception {
        if (CollectionUtils.isEmpty(mobileList)) {
            throw new Exception("推送人员手机号不能为空");
        }
        Long timestamp = System.currentTimeMillis();
        String stringToSign = timestamp + "\n" + secret;
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes("UTF-8"), "HmacSHA256"));
        byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
        String sign = URLEncoder.encode(new String(Base64.encodeBase64(signData)), "UTF-8");
        String dingUrl = webHook + "&timestamp=" + timestamp + "&sign=" + sign;
        try {
            String reqStr = buildReqStr(content, false, mobileList);
            log.info("钉钉发送消息地址：{}，参数：{}", dingUrl, reqStr);
            String result = HttpUtil.post(dingUrl, reqStr);
            log.info("钉钉发送消息返回结果：{}", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 组装请求报文
     *
     * @param content
     * @return
     */
    private static String buildReqStr(String content, boolean isAtAll, List<String> mobileList) {
        //消息内容
        Map<String, String> contentMap = Maps.newHashMap();
        contentMap.put("content", content);
        //通知人
        Map<String, Object> atMap = Maps.newHashMap();
        //1.是否通知所有人
        atMap.put("isAtAll", isAtAll);
        //2.通知具体人的手机号码列表
        atMap.put("atMobiles", mobileList);
        Map<String, Object> reqMap = Maps.newHashMap();
        reqMap.put("msgtype", "text");
        reqMap.put("text", contentMap);
        reqMap.put("at", atMap);
        return JSON.toJSONString(reqMap);
    }
}
