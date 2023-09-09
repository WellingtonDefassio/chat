package wdefassio.io.chat.events;

public record Event<T>(EventType type, T payload) {
}
