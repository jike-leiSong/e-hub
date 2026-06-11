package cn.sl.ehub.upstream.config;

import java.net.MalformedURLException;
import java.util.List;

import javax.annotation.Resource;

import cn.sl.ehub.upstream.service.HuabeiUrlService;
import cn.sl.ehub.upstream.ws.Greeter;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import cn.sl.ehub.common.enums.StatusCode;
import cn.sl.ehub.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class ClientConfig {

    @Resource
    private HuabeiUrlService huabeiUrlService;

    @Resource
    private WSS4JOutInterceptor clientWssOut;

    @Resource
    private LoggingInInterceptor loggingInInterceptor;

    @Resource
    private LoggingOutInterceptor loggingOutInterceptor;

    @Resource
    private TLSClientParameters tlsClientParameters;

    @Value("${nari.url.connectionTimeout:30000}")
    private long connectionTimeout;

    @Value("${nari.url.receiveTimeout:90000}")
    private long receiveTimeout;


    public Greeter greeter(List<String> urlList) throws MalformedURLException {
        return greeter(urlList, null);
    }

    /**
     * 创建Greeter实例，支持指定URL
     * @param urlList URL列表
     * @param specifiedUrl 指定的URL，如果为null则自动选择可用URL
     * @return Greeter实例
     * @throws MalformedURLException
     */
    public Greeter greeter(List<String> urlList, String specifiedUrl) throws MalformedURLException {
        JaxWsProxyFactoryBean jaxWsProxyFactoryBean =
                new JaxWsProxyFactoryBean();
        jaxWsProxyFactoryBean.setServiceClass(Greeter.class);
        String address = StringUtils.isNotBlank(specifiedUrl) ? specifiedUrl : huabeiUrlService.getAvailableUrl(urlList);
        log.info("华北调用url:{}", address);

        if (StringUtils.isBlank(address)) {
            throw new BaseException(StatusCode.F_URL_UNAVAILABLE.getCode(), StatusCode.F_URL_UNAVAILABLE.getMsg());
        }
        jaxWsProxyFactoryBean.setAddress(address);

        jaxWsProxyFactoryBean.getOutInterceptors().add(clientWssOut);
        jaxWsProxyFactoryBean.getOutInterceptors().add(new SAAJOutInterceptor());
        jaxWsProxyFactoryBean.getInInterceptors().add(new SAAJInInterceptor());
        jaxWsProxyFactoryBean.getInInterceptors().add(loggingInInterceptor);
        jaxWsProxyFactoryBean.getOutInterceptors().add(loggingOutInterceptor);
        Greeter greeter = (Greeter) jaxWsProxyFactoryBean.create();

        Client client = ClientProxy.getClient(greeter);

        HTTPConduit greetConduit = (HTTPConduit) client.getConduit();

        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout(connectionTimeout);
        policy.setReceiveTimeout(receiveTimeout);
        policy.setAllowChunking(false);

        greetConduit.setClient(policy);
        greetConduit.setTlsClientParameters(tlsClientParameters);

        return greeter;
    }

}
