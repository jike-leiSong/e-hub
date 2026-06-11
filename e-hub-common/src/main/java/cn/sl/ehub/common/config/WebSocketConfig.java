package cn.sl.ehub.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket配置文件
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Configuration
public class WebSocketConfig {

    /**
     * 注入一个ServerEndpointExporter
     * 该Bean会自动注册使用@ServerEndpoint
     * 注解申明的websocket endpoint
     *
     * @return
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
