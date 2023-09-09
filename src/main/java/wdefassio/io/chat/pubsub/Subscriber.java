package wdefassio.io.chat.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import wdefassio.io.chat.config.RedisConfig;
import wdefassio.io.chat.events.ChatMessage;
import wdefassio.io.chat.handler.WebSocketHandler;

@Component
@Slf4j
@RequiredArgsConstructor
public class Subscriber {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final WebSocketHandler webSocketHandler;

    private final ObjectMapper mapper;

    @PostConstruct
    private void init() {
        redisTemplate
                .listenTo(ChannelTopic.of(RedisConfig.CHAT_MESSAGES_CHANNEL))
                .map(ReactiveSubscription.Message::getMessage)
                .subscribe(this::onChatMessage);
    }

    private void onChatMessage(final String chatMessageSerialized) {
        log.info("chat message was received");
        try{
            ChatMessage chatMessage = mapper.readValue(chatMessageSerialized, ChatMessage.class);
            webSocketHandler.notify(chatMessage);
        }catch(Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }


    }

}
