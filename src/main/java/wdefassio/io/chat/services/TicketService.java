package wdefassio.io.chat.services;

import com.auth0.jwk.JwkException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import wdefassio.io.chat.data.User;
import wdefassio.io.chat.data.UserRepository;
import wdefassio.io.chat.providers.TokenProvider;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TokenProvider provider;
    private final UserRepository userRepository;

    public String buildAndSaveTicket(String token) throws JwkException {
        if(token == null || token.isBlank()) throw new RuntimeException("missing token");
        String ticket = UUID.randomUUID().toString();
        Map<String, String> user = provider.decode(token);
        String id = user.get("id");
        redisTemplate.opsForValue().set(ticket, id, Duration.ofSeconds(10L));
        saveUser(user);
        return ticket;
    }

    private void saveUser(Map<String, String> user) {
        userRepository.save(new User(user.get("id"), user.get("name"), user.get("picture")));
    }

    public Optional<String> getUserIdByTicket(String ticket) {
        return Optional.ofNullable(redisTemplate.opsForValue().getAndDelete(ticket));
    }


}
