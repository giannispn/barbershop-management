package gr.uniwa.barbershop.session;

import gr.uniwa.barbershop.model.User;

/**
 * Holds the currently authenticated {@link User} for the lifetime of the
 * application session. A simple singleton is appropriate here because the
 * desktop app has exactly one logged-in user at a time.
 */
public final class SessionManager {

    private static User currentUser;

    private SessionManager() {
    }

    public static void login(User user) {
        currentUser = user;
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /** @return the id of the logged-in user, or null if nobody is logged in. */
    public static Integer currentUserId() {
        return currentUser == null ? null : currentUser.getId();
    }
}
