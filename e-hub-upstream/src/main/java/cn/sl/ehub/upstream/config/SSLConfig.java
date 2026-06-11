package cn.sl.ehub.upstream.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.dom.handler.WSHandlerConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: 客户端配置
 * @Author sl
 * @Date 2026-05-28
 */
@Configuration
@Slf4j
public class SSLConfig {

    @Value("${client.keystore-alias}")
    private String keystoreAlias;

    /**
     * SSL配置参数
     */
    @Value("${client.key-store}")
    private Resource keyStoreResource;

    @Value("${client.trust-store}")
    private Resource trustStoreResource;

    @Value("${client.store-password}")
    private String storePassword;


    @Bean
    public Map<String, Object> clientOutProps() {
        Map<String, Object> clientOutProps = new HashMap<>();
        clientOutProps.put(WSHandlerConstants.ACTION,
                WSHandlerConstants.TIMESTAMP + " "
                        + WSHandlerConstants.SIGNATURE);
        clientOutProps.put(WSHandlerConstants.USER, keystoreAlias);
        clientOutProps.put(WSHandlerConstants.MUST_UNDERSTAND, "false");
        clientOutProps.put(WSHandlerConstants.PW_CALLBACK_CLASS,
                cn.sl.ehub.upstream.config.ClientPasswordCallback.class.getName());
        clientOutProps.put(WSHandlerConstants.SIG_PROP_FILE,
                "client_sign.properties");
        clientOutProps.put(WSHandlerConstants.SIG_VER_PROP_FILE,
                "client_trust.properties");

        return clientOutProps;
    }

    @Bean
    public WSS4JOutInterceptor clientWssOut() {
        WSS4JOutInterceptor clientWssOut = new WSS4JOutInterceptor();
        clientWssOut.setProperties(clientOutProps());

        return clientWssOut;
    }


    @Bean
    public LoggingInInterceptor loggingInInterceptor() {
        return new LoggingInInterceptor();
    }

    @Bean
    public LoggingOutInterceptor loggingOutInterceptor() {
        return new LoggingOutInterceptor();
    }





    @Bean
    public TLSClientParameters tlsClientParameters()
            throws Exception {
        TLSClientParameters tlsClientParameters =
                new TLSClientParameters();
        tlsClientParameters.setSecureSocketProtocol("TLS");
        tlsClientParameters.setDisableCNCheck(true);
        tlsClientParameters.setTrustManagers(trustManagers());
        tlsClientParameters.setKeyManagers(keyManagers());
        tlsClientParameters.setCipherSuitesFilter(cipherSuitesFilter());
        tlsClientParameters.setHostnameVerifier(new CustomHostnameVerifier());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers(), trustManagers(), null);
        tlsClientParameters.setSslContext(sslContext);

        return tlsClientParameters;
    }

    @Bean
    public HostnameVerifier customHostnameVerifier() {
        HostnameVerifier hostnameVerifier = new CustomHostnameVerifier();
        return hostnameVerifier;
    }

    @Bean
    public TrustManager[] trustManagers()
            throws NoSuchAlgorithmException, KeyStoreException,
            IOException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore());

        return trustManagerFactory.getTrustManagers();
    }

    /**
     * 测试
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws IOException
     * @throws UnrecoverableKeyException
     */
    @Bean
    public KeyManager[] keyManagers()
            throws NoSuchAlgorithmException, KeyStoreException,
            IOException, UnrecoverableKeyException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore(), storePassword.toCharArray());

        return keyManagerFactory.getKeyManagers();
    }

    @Bean
    public KeyStore trustStore() throws KeyStoreException, IOException {
        KeyStore trustStore = KeyStore.getInstance("jks");

        InputStream inputStream = trustStoreResource.getInputStream();

        try {
            trustStore.load(inputStream,
                    storePassword.toCharArray());
            log.info("==========trustStore处理成功");
        } catch (IOException e) {
            log.info("=======trustStore IO异常");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            log.info("========trustStore 算法异常");
            e.printStackTrace();
        } catch (CertificateException e) {
            log.info("======trustStore 证书异常");
            e.printStackTrace();
        }
        return trustStore;
    }

    @Bean
    public KeyStore keyStore() throws KeyStoreException, IOException {
        KeyStore trustStore = KeyStore.getInstance("pkcs12");

        InputStream inputStream = keyStoreResource.getInputStream();

        try {
            trustStore.load(inputStream,
                    storePassword.toCharArray());
            log.info("=========keyStore处理成功");
        } catch (IOException e) {
            log.info("==========keyStore IO异常");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            log.info("===========keyStore 算法异常");
            e.printStackTrace();
        } catch (CertificateException e) {
            log.info("==========keyStore 证书异常");
            e.printStackTrace();
        }
        return trustStore;
    }

    @Bean
    public FiltersType cipherSuitesFilter() {
        FiltersType filter = new FiltersType();
        filter.getInclude().add("TLS_ECDHE_RSA_.*");
        filter.getInclude().add("TLS_DHE_RSA_.*");

        return filter;
    }

}
