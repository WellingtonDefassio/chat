package wdefassio.io.chat.events;

import wdefassio.io.chat.data.User;

public record ChatMessage(User from, User to, String text) {
}
