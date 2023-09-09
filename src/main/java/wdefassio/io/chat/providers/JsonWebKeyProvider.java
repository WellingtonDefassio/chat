package wdefassio.io.chat.providers;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.PublicKey;

@Component
public class JsonWebKeyProvider implements KeyProvider {
    private final UrlJwkProvider provider;
    public JsonWebKeyProvider(@Value("${app.auth.jwks-url}") final String jwksUrl) throws MalformedURLException {
        this.provider = new UrlJwkProvider(new URL(jwksUrl));
    }

    @Cacheable("public-key")
    @Override
    public PublicKey getPublicKey(String key) throws JwkException {
        final Jwk jwk = provider.get(key);
        return jwk.getPublicKey();
    }
}
