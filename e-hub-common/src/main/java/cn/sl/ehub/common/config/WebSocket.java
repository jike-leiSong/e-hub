package cn.sl.ehub.common.config;

import cn.sl.ehub.common.dto.WebSocketMessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 *
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@Slf4j
@Component
@ServerEndpoint("/webSocket/{openId}/{entId}")
public class WebSocket {

    private static int onlineCount = 0;
    private static Map<String, WebSocket> clients = new ConcurrentHashMap<String, WebSocket>();
    private Session session;
    private String openId;
    private String entId;
    private String cacheKey;

    @OnOpen
    public void onOpen(@PathParam("openId") String openId, @PathParam("entId") String entId, Session session) throws IOException {
        this.openId = openId;
        this.entId = entId;
        this.session = session;
        this.cacheKey = openId + "," + entId;
        addOnlineCount();
        clients.put(cacheKey, this);
        log.info("用户连接：{}，当前在线人数为：{}", cacheKey, getOnlineCount());
    }

    @OnClose
    public void onClose() throws IOException {
        clients.remove(cacheKey);
        subOnlineCount();
        log.info("用户关闭：{}，当前在线人数为：{}", cacheKey, getOnlineCount());
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        if (message.equals("ping")) {
            log.info("用户心跳：{}， 当前在线人数为：{}", cacheKey, getOnlineCount());
            sendMessageTo("success", cacheKey);
        } else {
            log.info("接收消息：{}", message);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.info("用户异常：{}，当前在线人数为：{}", cacheKey, getOnlineCount());
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    public void sendMessage(WebSocketMessageDTO webSocketMessageDTO) throws IOException {
        if (null != webSocketMessageDTO) {
            log.info("发布消息内容：{}", webSocketMessageDTO.getMessage());
            if (StringUtils.isNotEmpty(webSocketMessageDTO.getTo()) && webSocketMessageDTO.getTo().equals("ALL")) {
                sendMessageAll(webSocketMessageDTO.getMessage());
            } else if (StringUtils.isNotEmpty(webSocketMessageDTO.getToEntId())) {
                sendMessageToEntId(webSocketMessageDTO.getMessage(), webSocketMessageDTO.getToEntId());
            } else {
                sendMessageTo(webSocketMessageDTO.getMessage(), webSocketMessageDTO.getTo());
            }
        }
    }

    public void sendMessageToEntId(String message, String toEntId) throws IOException {
        for (WebSocket item : clients.values()) {
            if (item.entId.equals(toEntId)) {
                item.session.getBasicRemote().sendText(message);
            }
        }
    }

    public void sendMessageTo(String message, String To) throws IOException {
        for (WebSocket item : clients.values()) {
            if (item.cacheKey.equals(To)) {
                item.session.getBasicRemote().sendText(message);
            }
        }
    }

    public void sendMessageAll(String message) throws IOException {
        for (WebSocket item : clients.values()) {
            item.session.getBasicRemote().sendText(message);
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocket.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocket.onlineCount--;
    }

    public static synchronized Map<String, WebSocket> getClients() {
        return clients;
    }
}
