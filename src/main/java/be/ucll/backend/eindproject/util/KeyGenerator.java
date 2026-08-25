package be.ucll.backend.eindproject.util;

import java.security.SecureRandom;
import java.util.Base64;

public class KeyGenerator {
    public static void main(String[] args) {
        // Genereer 32 random bytes
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        // Converteer naar Base64 url encoding
        final var base64Key = Base64.getUrlEncoder().withoutPadding().encodeToString(key);
        // Output het resultaat
        System.out.println(base64Key);
    }
}