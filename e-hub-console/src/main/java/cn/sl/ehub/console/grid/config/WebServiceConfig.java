package cn.sl.ehub.console.grid.config;

import cn.sl.ehub.console.grid.component.ServerPasswordCallback;
import cn.sl.ehub.console.grid.service.IssueWebService;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.xml.ws.Endpoint;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: WebService配置
 * @Author sl
 * @Date 2026-05-28
 */

@Configuration
public class WebServiceConfig {

    @Value("${server.keystore-alias}")
    private String keystoreAlias;

    @Resource
    private IssueWebService issueWebService;

    //* 此方法被注释后:wsdl访问地址为http://127.0.0.1:8089/services/issue?wsdl
    //     * 去掉注释后：wsdl访问地址为：http://127.0.0.1:8089/webservice/issue?wsdl
    @Bean
    public ServletRegistrationBean cxfServlet() {
        return new ServletRegistrationBean(new CXFServlet(), "/webservice/*");
    }

    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {

        return new SpringBus();

    }

    @Bean
    public Endpoint endpoint() {
        EndpointImpl endpoint = new EndpointImpl(springBus(), issueWebService);
        endpoint.publish("/issue");

        // add the WSS4J IN interceptor to verify the signature on the request message
        //endpoint.getInInterceptors().add(serverWssIn());
        // add the WSS4J OUT interceptor to sign the response message
        //endpoint.getOutInterceptors().add(serverWssOut());

        // log the request and response messages
        endpoint.getInInterceptors()
                .add(loggingInInterceptor());
        endpoint.getOutInterceptors()
                .add(loggingOutInterceptor());

        return endpoint;
    }


    @Bean
    public Map<String, Object> serverInProps() {
        Map<String, Object> serverInProps = new HashMap<>();
        serverInProps.put(WSHandlerConstants.ACTION,
                WSHandlerConstants.TIMESTAMP + " "
                        + WSHandlerConstants.SIGNATURE);
        serverInProps.put(WSHandlerConstants.SIG_PROP_FILE,
                "server_trust.properties");

        return serverInProps;
    }

    @Bean
    public WSS4JInInterceptor serverWssIn() {
        WSS4JInInterceptor serverWssIn = new WSS4JInInterceptor();
        serverWssIn.setProperties(serverInProps());

        return serverWssIn;
    }

    @Bean
    public Map<String, Object> serverOutProps() {
        Map<String, Object> serverOutProps = new HashMap<>();
        serverOutProps.put(WSHandlerConstants.ACTION,
                WSHandlerConstants.TIMESTAMP + " "
                        + WSHandlerConstants.SIGNATURE);
        serverOutProps.put(WSHandlerConstants.USER, keystoreAlias);
        serverOutProps.put(WSHandlerConstants.PW_CALLBACK_CLASS,
                ServerPasswordCallback.class.getName());
        serverOutProps.put(WSHandlerConstants.SIG_PROP_FILE,
                "server_sign.properties");

        return serverOutProps;
    }

    @Bean
    public WSS4JOutInterceptor serverWssOut() {
        WSS4JOutInterceptor serverWssOut = new WSS4JOutInterceptor();
        serverWssOut.setProperties(serverOutProps());

        return serverWssOut;
    }

    @Bean
    public LoggingInInterceptor loggingInInterceptor() {
        return new LoggingInInterceptor();
    }

    @Bean
    public LoggingOutInterceptor loggingOutInterceptor() {
        return new LoggingOutInterceptor();
    }
}