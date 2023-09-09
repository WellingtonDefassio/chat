package wdefassio.io.chat.providers;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.JwkException;

import java.security.PublicKey;

public interface KeyProvider {
    PublicKey getPublicKey(String key) throws JwkException;
}
