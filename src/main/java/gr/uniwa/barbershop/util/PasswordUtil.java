package gr.uniwa.barbershop.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Thin wrapper around BCrypt for hashing and verifying passwords.
 * Plaintext passwords are never stored or logged.
 */
public final class PasswordUtil {

    private PasswordUtil() {
    }

    /**
     * Hashes a plaintext password with a freshly generated salt.
     *
     * @param plain the plaintext password
     * @return a BCrypt hash suitable for storing in users.password_hash
     */
    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt(10));
    }

    /**
     * Verifies a plaintext password against a stored BCrypt hash.
     *
     * @param plain  the password the user typed
     * @param hashed the stored hash
     * @return true if the password matches
     */
    public static boolean verify(String plain, String hashed) {
        if (plain == null || hashed == null || hashed.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plain, hashed);
        } catch (IllegalArgumentException e) {
            // hashed string is not a valid BCrypt hash
            return false;
        }
    }
}
