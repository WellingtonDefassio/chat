package wdefassio.io.chat.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import wdefassio.io.chat.services.TicketService;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final TicketService ticketService;
    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("[afterConnectionEstablished] session id " + session.getId());
        Optional<String> optionalTicket = ticketOf(session);
        if (optionalTicket.isEmpty() || optionalTicket.get().isBlank()) {
            log.warn("session {}, without ticket", session.getId());
            close(session, CloseStatus.POLICY_VIOLATION);
            return;
        }

        Optional<String> userId = ticketService.getUserIdByTicket(optionalTicket.get());
        if (userId.isEmpty()) {
            log.warn("session {}, with invalid ticket", session.getId());
            close(session, CloseStatus.POLICY_VIOLATION);
            return;
        }

        sessionMap.put(userId.get(), session);

        log.info("session {} was bind to user " + userId.get());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("[handleTextMessage session id] " + message.getPayload());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[afterConnectionClosed session id] " + session.getId());
    }

    private void close(WebSocketSession session, CloseStatus status) {
        try {
            session.close(status);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Optional<String> ticketOf(WebSocketSession session) {
        return Optional
                .ofNullable(session.getUri())
                .map(UriComponentsBuilder::fromUri)
                .map(UriComponentsBuilder::build)
                .map(UriComponents::getQueryParams)
                .map(it -> it.get("ticket"))
                .flatMap(it -> it.stream().findFirst())
                .map(String::trim);
    }
}
