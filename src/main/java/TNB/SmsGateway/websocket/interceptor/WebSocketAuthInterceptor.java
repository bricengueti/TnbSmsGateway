package TNB.SmsGateway.websocket.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        String query = request.getURI().getRawQuery(); // raw pour ne pas double-décoder
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                int idx = param.indexOf('='); // ⚠️ premier '=' seulement, pas split("=")
                if (idx > 0) {
                    String key = param.substring(0, idx);
                    String value = URLDecoder.decode(param.substring(idx + 1), StandardCharsets.UTF_8);
                    if ("deviceId".equals(key)) {
                        attributes.put("deviceId", value);
                    } else if ("secretToken".equals(key)) {
                        attributes.put("secretToken", value);
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
    }
}