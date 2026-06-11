package cn.sl.ehub.upstream.config;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * @Description:
 * @Author sl
 * @Date 2026-05-28
 */
@Slf4j
public class CustomHostnameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostname, SSLSession sslSession) {

        log.info("============hostname:" + hostname);
        log.info("============sslSession#peerHost:" + sslSession.getPeerHost());
        log.info("============sslSession#peerPort:" + sslSession.getPeerPort());

        return true;
    }
}
