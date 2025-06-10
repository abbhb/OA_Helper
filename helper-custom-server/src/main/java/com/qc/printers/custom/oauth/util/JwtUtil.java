package com.qc.printers.custom.oauth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtUtil {
    private static final String ISSUER = "EasyOA";
    private static final long ID_TOKEN_EXPIRE_TIME = 3600 * 1000; // 1小时
    private static KeyPair keyPair;

    static {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            keyPair = generator.generateKeyPair();
        } catch (Exception e) {
            log.error("Failed to generate key pair", e);
        }
    }

    public static String generateIdToken(String issuer, String subject, String clientId, String nonce, String name, String email, String username) {
        try {
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
            return JWT.create()
                    .withIssuer(issuer)
                    .withSubject(subject)
                    .withAudience(clientId)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + ID_TOKEN_EXPIRE_TIME))
//                    .withClaim("nonce", generateRandomString(32)) 加上immich会报错
                    .withClaim("name", name) // 可以根据需要添加更多用户信息
                    .withClaim("email", email) // 可以根据需要添加更多用户信息
                    .withClaim("username", username) // 可以根据需要添加更多用户信息
                    .sign(algorithm);
        } catch (Exception e) {
            log.error("Failed to generate ID token", e);
            return null;
        }
    }

    public static Map<String, Object> getJwks() {
        Map<String, Object> jwks = new HashMap<>();
        Map<String, Object> key = new HashMap<>();
        
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        key.put("kty", "RSA");
        key.put("use", "sig");
        key.put("alg", "RS256");
        key.put("kid", "1"); // 密钥ID
        key.put("n", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray()));
        key.put("e", Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray()));
        
        jwks.put("keys", new Object[]{key});
        return jwks;
    }
    public static String generateRandomString(int length) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[length];
        secureRandom.nextBytes(randomBytes);

        // 使用 URL 安全的 Base64 编码
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public static DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build();
            return verifier.verify(token);
        } catch (Exception e) {
            log.error("Failed to verify token", e);
            return null;
        }
    }
} 