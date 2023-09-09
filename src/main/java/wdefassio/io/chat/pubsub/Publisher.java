package wdefassio.io.chat.pubsub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import wdefassio.io.chat.config.RedisConfig;
import wdefassio.io.chat.data.User;
import wdefassio.io.chat.data.UserRepository;
import wdefassio.io.chat.events.ChatMessage;

@Component
@RequiredArgsConstructor
@Slf4j
public class Publisher {


    private final UserRepository userRepository;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishChatMessage(String userIdFrom, String userIdTo, String text) throws JsonProcessingException {

        User userFrom = userRepository.findById(userIdFrom).orElseThrow();
        User userTo = userRepository.findById(userIdTo).orElseThrow();
        ChatMessage chatMessage = new ChatMessage(userFrom, userTo, text);
        String chatMessageSerialized = objectMapper.writeValueAsString(chatMessage);
        redisTemplate
                .convertAndSend(RedisConfig.CHAT_MESSAGES_CHANNEL, chatMessageSerialized)
                .subscribe();

        log.info("chat message was published");
    }


}
