package com.pharmasiu.mobile.data.local;

import org.junit.Test;
import static org.junit.Assert.*;

public class PasswordCodecTest {
    @Test public void hashUsesSaltAndVerifiesPassword() {
        String secret = "correct horse battery";
        String first = PasswordCodec.hash(secret);
        String second = PasswordCodec.hash(secret);
        assertNotEquals("Different salt each time",first,second);
        assertTrue(PasswordCodec.matches(secret,first));
        assertTrue(PasswordCodec.matches(secret,second));
        assertFalse(PasswordCodec.matches("incorrect",first));
    }
    @Test public void malformedHashesAreRejected() {
        assertFalse(PasswordCodec.matches("anything",null));
        assertFalse(PasswordCodec.matches("anything","plaintext"));
        assertFalse(PasswordCodec.matches(null,"pbkdf2_sha256$210000$bogus$bogus"));
        assertFalse(PasswordCodec.matches("anything","pbkdf2_sha256$1$abc$abc"));
    }
    @Test(expected=IllegalArgumentException.class)
    public void shortRegistrationPasswordIsRejected() {
        PasswordCodec.hash("tiny");
    }
}
