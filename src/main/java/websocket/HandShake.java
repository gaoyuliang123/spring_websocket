package websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Socket建立连接（握手）和断开
 * 拦截用户登录信息，并将用户登录信息交给websocket的WebSocketSession来管理，
 * 因为这样websocket就可以知道用户是否在线，是否不在线了，而且还能给别的用户发送消息
 */
public class HandShake implements HandshakeInterceptor {
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        System.out.println("WebSocket:用户[ID:" + ((ServletServerHttpRequest)request).getServletRequest().getSession(false).getAttribute("uid") +"]已经建立连接");
        if (request instanceof ServletServerHttpRequest) {
            //将ServerHttpRequest转换成request请求相关的类，用来获取request域中的用户信息
            ServletServerHttpRequest servletServer = (ServletServerHttpRequest)request;
            HttpSession session = servletServer.getServletRequest().getSession(false);
            //标记用户
            Long uid = (Long)session.getAttribute("uid");
            if (null != uid) {
                attributes.put("uid",uid);
            } else {
                return false;
            }
        }
        return true;
    }

    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

    }
}
