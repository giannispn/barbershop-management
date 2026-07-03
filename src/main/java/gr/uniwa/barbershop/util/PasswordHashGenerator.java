package gr.uniwa.barbershop.util;

/**
 * One-off helper to generate a BCrypt hash for seeding the first admin user.
 *
 * Run it once (e.g. right-click > Run in your IDE), copy the printed hash, and
 * paste it into the users.password_hash value in schema.sql (or run an UPDATE).
 *
 *   UPDATE users SET password_hash = '<printed hash>' WHERE username = 'admin';
 *
 * Delete this class before shipping; it exists only for first-time setup.
 */
public class PasswordHashGenerator {
    public static void main(String[] args) {
        String plain = (args.length > 0) ? args[0] : "admin123";
        System.out.println("Plaintext: " + plain);
        System.out.println("BCrypt   : " + PasswordUtil.hash(plain));
    }
}
