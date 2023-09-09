package wdefassio.io.chat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import wdefassio.io.chat.data.User;
import wdefassio.io.chat.events.ChatMessage;
import wdefassio.io.chat.events.Event;
import wdefassio.io.chat.events.EventType;
import wdefassio.io.chat.pubsub.Publisher;
import wdefassio.io.chat.services.TicketService;
import wdefassio.io.chat.services.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final TicketService ticketService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> userIds = new ConcurrentHashMap<>();

    private final Publisher publisher;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        log.info("[afterConnectionEstablished] session id " + session.getId());
        Optional<String> optionalTicket = ticketOf(session);
        if (optionalTicket.isEmpty() || optionalTicket.get().isBlank()) {
            log.warn("session {}, without ticket", session.getId());
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        Optional<String> userId = ticketService.getUserIdByTicket(optionalTicket.get());
        if (userId.isEmpty()) {
            log.warn("session {}, with invalid ticket", session.getId());
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        sessions.put(userId.get(), session);
        userIds.put(session.getId(), userId.get());
        log.info("session {} was bind to user " + userId.get());
        sendChatUsers(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        log.info("[handleTextMessage session id] " + message.getPayload());
        if (message.getPayload().equals("ping")) {
            session.sendMessage(new TextMessage("pong"));
            return;
        }
        MessagePayload messagePayload = objectMapper.readValue(message.getPayload(), MessagePayload.class);
        String userIdFrom = userIds.get(session.getId());
        publisher.publishChatMessage(userIdFrom, messagePayload.to(), messagePayload.text());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("[afterConnectionClosed session id] " + session.getId());
        String userId = userIds.get(session.getId());
        sessions.remove(userId);
        userIds.remove(session.getId());
    }

    private void sendChatUsers(WebSocketSession session) {
        List<User> chatUsers = userService.findChatUsers();
        Event<List<User>> event = new Event<>(EventType.CHAT_USERS_WERE_UPDATED, chatUsers);
        sendEvent(session, event);
    }

    private void sendEvent(WebSocketSession session, Event<?> event) {
        try {
            String eventSerialized = objectMapper.writeValueAsString(event);
            session.sendMessage(new TextMessage(eventSerialized));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
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

    public void notify(ChatMessage chatMessage) {
        Event<ChatMessage> event = new Event<>(EventType.CHAT_USERS_WERE_UPDATED, chatMessage);
        List<String> userIds = List.of(chatMessage.from().id(), chatMessage.to().id());
        userIds.stream().distinct().map(sessions::get)
                .filter(Objects::nonNull)
                .forEach(session -> sendEvent(session, event));

        log.info("chat message was notified");

    }
}
