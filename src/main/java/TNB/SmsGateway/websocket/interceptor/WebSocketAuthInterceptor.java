package TNB.SmsGateway.websocket.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // Récupérer le deviceId et secretToken de l'URL
        // Exemple: ws://localhost:8080/ws/device?deviceId=xxx&secretToken=yyy
        String query = request.getURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    if ("deviceId".equals(keyValue[0])) {
                        attributes.put("deviceId", keyValue[1]);
                    } else if ("secretToken".equals(keyValue[0])) {
                        attributes.put("secretToken", keyValue[1]);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // Rien
    }
}