package cn.sl.ehub.common.config;

import cn.sl.ehub.common.dto.WebSocketMessageDTO;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;

/**
 * redis消息订阅监听者
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@Component
public class RedisSubscribeListener implements MessageListener {

    private final WebSocket webSocket;
    private final RedisTemplate redisTemplate;

    public RedisSubscribeListener(WebSocket webSocket, RedisTemplate redisTemplate) {
        this.webSocket = webSocket;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
        log.info("消息订阅成功---------");
        RedisSerializer valueSerializer = redisTemplate.getValueSerializer();
        String body = (String) valueSerializer.deserialize(message.getBody());
        log.info("发布消息体：{}", body);
        try {
            webSocket.sendMessage(JSONObject.parseObject(body, WebSocketMessageDTO.class));
        } catch (IOException e) {
            log.error("[redis监听器]发布消息异常：{}", e);
        }
    }
}
