package wdefassio.io.chat.providers;

import com.auth0.jwk.JwkException;

import java.util.Map;

public interface TokenProvider {
    Map<String, String> decode(String token) throws JwkException;
}
