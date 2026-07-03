package gr.uniwa.barbershop.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PasswordUtil}. No database required.
 */
class PasswordUtilTest {

    @Test
    void hashIsNotPlaintext() {
        String hash = PasswordUtil.hash("secret123");
        assertNotNull(hash);
        assertNotEquals("secret123", hash, "Ο κωδικός δεν πρέπει να αποθηκεύεται ως απλό κείμενο");
        assertTrue(hash.startsWith("$2"), "Πρέπει να είναι BCrypt hash");
    }

    @Test
    void verifyAcceptsCorrectPassword() {
        String hash = PasswordUtil.hash("myPassword");
        assertTrue(PasswordUtil.verify("myPassword", hash));
    }

    @Test
    void verifyRejectsWrongPassword() {
        String hash = PasswordUtil.hash("myPassword");
        assertFalse(PasswordUtil.verify("wrongPassword", hash));
    }

    @Test
    void verifyHandlesNullsSafely() {
        assertFalse(PasswordUtil.verify(null, "x"));
        assertFalse(PasswordUtil.verify("x", null));
        assertFalse(PasswordUtil.verify("x", ""));
    }

    @Test
    void sameInputProducesDifferentHashes() {
        // BCrypt uses a random salt, so two hashes of the same password differ
        String h1 = PasswordUtil.hash("abc");
        String h2 = PasswordUtil.hash("abc");
        assertNotEquals(h1, h2);
        // ...but both still verify correctly
        assertTrue(PasswordUtil.verify("abc", h1));
        assertTrue(PasswordUtil.verify("abc", h2));
    }
}
