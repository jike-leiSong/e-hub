package cn.sl.ehub.upstream.service;

import cn.sl.ehub.common.utils.DingUtil;
import cn.sl.ehub.common.utils.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description: 华北Url选择服务
 * @Author sl
 * @Date 2026-05-28
 */
@Service
@Slf4j
public class HuabeiUrlService {

    @Value("${dingding.secret}")
    private String secret;

    @Value("${dingding.webHook}")
    private String webHook;

    @Autowired
    private Environment environment;

    private final String[] ENVS = {"pro", "prod"};

    private String alert = "注意，华北服务不可用：";


    public String getAvailableUrl(List<String> urlList) {
        String result = "";
        int size = urlList.size();
        for (int i = 0; i < size; i++) {
            try {
                URL url = new URL(urlList.get(i));
                String host = url.getHost();
                int port = url.getPort();
                boolean hostConnectable = NetUtil.isHostConnectable(host, port);
                if (hostConnectable) {
                    result = urlList.get(i);
                    break;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        if (StringUtils.isBlank(result)) {
            huabeiUrlAlertToDingDing(urlList);
        }

        return result;
    }

    public List<String> getAllAvailableUrl(List<String> urlList) {

        List<String> resultList = new ArrayList<>();

        urlList.forEach(urlStr -> {
            try {
                URL url = new URL(urlStr);
                String host = url.getHost();
                int port = url.getPort();
                boolean hostConnectable = NetUtil.isHostConnectable(host, port);
                if (hostConnectable) {
                    resultList.add(urlStr);
                } else {
                    log.info("url:{}不可用", urlStr);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        if (CollectionUtils.isEmpty(resultList) && isProdEnv()) {
            huabeiUrlAlertToDingDing(urlList);
        }

        return resultList;
    }

    public void huabeiUrlAlertToDingDing(List<String> urlList) {

        try {
            DingUtil.sendMsgAll(secret, webHook, alert + urlList.stream().collect(Collectors.joining(";")));
        } catch (Exception e) {
            e.printStackTrace();
            log.info("华北url不可用告警失败！");
        }

    }

    private boolean isProdEnv() {
        String env = this.environment.getProperty("env");
        if (StringUtils.equalsAnyIgnoreCase(env, ENVS)) {
            return true;
        }
        return Boolean.FALSE.booleanValue();
    }
}
