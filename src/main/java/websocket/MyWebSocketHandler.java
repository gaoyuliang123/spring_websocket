package websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entity.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket消息处理器
 */
@Component
public class MyWebSocketHandler implements WebSocketHandler {

    private static final Map<Long, WebSocketSession> userWebSocketSessionMap = new HashMap<Long, WebSocketSession>();
    /**
     * 建立连接后
     */
    public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
        Long uid = (Long) webSocketSession.getAttributes().get("uid");
        if (null != uid) {
            userWebSocketSessionMap.put(uid, webSocketSession);
            System.out.println("用户[ID:" + uid + "]成功进入了系统。");
        }
    }
    /**
     * 消息处理，在客户端通过Websocket API发送的消息会经过这里，然后进行相应的处理。
     * 将消息进行转化，因为是消息是json数据，可能里面包含了发送给某个人的信息，所以需要用json相关的工具类处理之后再封装成TextMessage，
     * 消息的封装格式一般有{from:xxxx,to:xxxxx,msg:xxxxx，......}，来自哪里，发送给谁，什么消息等等
     */
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
        if (webSocketMessage.getPayloadLength() == 0) {
            return;
        }
        Message msg = new Gson().fromJson(webSocketMessage.getPayload().toString(), Message.class);
        msg.setDate(new Date());
        sendMessageToUser(msg.getTo(), new TextMessage(new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create().toJson(msg)));

    }
    /**
     * 消息传输错误处理
     */
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
        if (webSocketSession.isOpen()) {
            webSocketSession.close();
        }
        for (Map.Entry<Long, WebSocketSession> entry : userWebSocketSessionMap.entrySet()) {
            if (entry.getValue().getId().equals(webSocketSession.getId())) {
                userWebSocketSessionMap.remove(entry.getKey());
                System.out.println("WebSocket会话已经移除:用户ID:" + entry.getKey());
                break;
            }
        }
    }

    /**
     * 关闭连接后
     * @param webSocketSession
     * @param closeStatus
     * @throws Exception
     */
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
        System.out.println("websocket:" + webSocketSession.getId() + "已经关闭");
        for (Map.Entry<Long, WebSocketSession> entry : userWebSocketSessionMap.entrySet()) {
            if (entry.getValue().getId().equals(webSocketSession.getId())) {
                userWebSocketSessionMap.remove(entry.getKey());
                System.out.println("WebSocket会话已经移除:用户ID:" + entry.getKey());
                break;
            }
        }
    }

    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 给某个用户发送消息
     * @param uid
     * @param message
     */
    private void sendMessageToUser(Long uid, TextMessage message) {
        WebSocketSession webSocketSession = userWebSocketSessionMap.get(uid);
        if (null != webSocketSession && webSocketSession.isOpen()) {
            try {
                webSocketSession.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给所有用户发送消息
     * @param message
     * @throws Exception
     */
    public void broadcast(final TextMessage message) throws Exception {
        userWebSocketSessionMap.forEach((k, v) ->{
            if (v.isOpen()) {
                // 多线程群发
                new Thread(() ->{
                    try {
                        v.sendMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }
}
