package wdefassio.io.chat.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wdefassio.io.chat.data.User;
import wdefassio.io.chat.data.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findChatUsers() {
        return userRepository.findAll();
    }
}
