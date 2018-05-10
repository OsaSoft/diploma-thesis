package cz.cvut.fel.hernaosc.dp.msgr.websocket.config

import cz.cvut.fel.hernaosc.dp.msgr.websocket.service.WebSocketService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableWebSocket
class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    WebSocketService webSocketService

    @Override
    void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketService, "/ws/*").allowedOrigins = "*"
    }
}
