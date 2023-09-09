package wdefassio.io.chat.providers;

import com.auth0.jwk.JwkException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonWebTokenProvider implements TokenProvider {

    private final KeyProvider keyProvider;

    @Override
    public Map<String, String> decode(String token) throws JwkException {
        DecodedJWT jwt = JWT.decode(token);
        PublicKey publicKey = keyProvider.getPublicKey(jwt.getKeyId());
        Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) publicKey, null);
        algorithm.verify(jwt);
        boolean expired = jwt.getExpiresAtAsInstant()
                .atZone(ZoneId.systemDefault())
                .isBefore(ZonedDateTime.now());
        if (expired) throw new RuntimeException("token is expired");

        return Map.of(
                "id", jwt.getSubject(),
                "name", jwt.getClaim("name").toString(),
                "picture", jwt.getClaim("picture").toString());
    }
}
