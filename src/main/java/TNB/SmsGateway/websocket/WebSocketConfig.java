package TNB.SmsGateway.websocket;
import TNB.SmsGateway.websocket.handler.DeviceWebSocketHandler;
import TNB.SmsGateway.websocket.interceptor.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * CONFIGURATION: WebSocketConfig
 *
 * DESCRIPTION: Configuration du WebSocket pour les devices Android
 * - Point d'entrée: /ws/device
 * - Intercepteur d'authentification
 * - CORS autorisé pour toutes les origines
 *
 * SCÉNARIOS:
 * 1. Connexion: l'app mobile se connecte au point d'entrée
 * 2. Authentification: l'intercepteur extrait deviceId et secretToken
 * 3. Communication: échange de messages via WebSocket
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DeviceWebSocketHandler deviceWebSocketHandler;
    private final WebSocketAuthInterceptor authInterceptor;

    public WebSocketConfig(DeviceWebSocketHandler deviceWebSocketHandler,
                           WebSocketAuthInterceptor authInterceptor) {
        this.deviceWebSocketHandler = deviceWebSocketHandler;
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(deviceWebSocketHandler, "/ws/device")
                .addInterceptors(authInterceptor)
                .setAllowedOrigins("*");
    }
}