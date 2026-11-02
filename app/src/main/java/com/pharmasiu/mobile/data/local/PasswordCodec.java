package com.pharmasiu.mobile.data.local;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Password verifier for LOCAL accounts. No sample passwords, no reversible storage. */
public final class PasswordCodec {
    private PasswordCodec() {}
    private static final int ITERATIONS = 210000;
    private static final int HASH_BITS = 256;

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("Secure password hashing unavailable", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static String hash(String password) {
        if (password == null || password.length() < 8 || password.length() > 128)
            throw new IllegalArgumentException("Password must contain 8-128 characters");
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return "pbkdf2_sha256$" + ITERATIONS + "$" +
                Base64.getEncoder().withoutPadding().encodeToString(salt) + "$" +
                Base64.getEncoder().withoutPadding().encodeToString(
                        derive(password.toCharArray(), salt, ITERATIONS));
    }

    public static boolean matches(String password, String encoded) {
        if (password == null || encoded == null) return false;
        try {
            String[] fields = encoded.split("\\$", -1);
            if (fields.length != 4 || !"pbkdf2_sha256".equals(fields[0])) return false;
            int iterations = Integer.parseInt(fields[1]);
            if (iterations < 100000 || iterations > 1000000) return false;
            byte[] salt = Base64.getDecoder().decode(fields[2]);
            byte[] expected = Base64.getDecoder().decode(fields[3]);
            if (salt.length != 16 || expected.length != 32) return false;
            return MessageDigest.isEqual(expected,
                    derive(password.toCharArray(), salt, iterations));
        } catch (Exception invalid) {
            return false;
        }
    }
}
