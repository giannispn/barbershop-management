package gr.uniwa.barbershop.service;

import gr.uniwa.barbershop.dao.UserDAO;
import gr.uniwa.barbershop.model.User;
import gr.uniwa.barbershop.session.SessionManager;
import gr.uniwa.barbershop.util.BusinessException;
import gr.uniwa.barbershop.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Authentication logic: verify credentials, start the session, record login.
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Attempts to log a user in. On success the user is stored in the
     * {@link SessionManager} and their last_login timestamp is updated.
     *
     * @throws BusinessException if the credentials are missing or invalid
     */
    public User login(String username, String password) throws BusinessException {
        if (username == null || username.isBlank()
            || password == null || password.isBlank()) {
            throw new BusinessException("Συμπληρώστε όνομα χρήστη και κωδικό.");
        }

        try {
            Optional<User> found = userDAO.findByUsername(username.trim());

            // Same generic message whether the user is missing or the password
            // is wrong, so we don't reveal which usernames exist.
            if (found.isEmpty()
                || !PasswordUtil.verify(password, found.get().getPasswordHash())) {
                throw new BusinessException("Λανθασμένο όνομα χρήστη ή κωδικός.");
            }

            User user = found.get();
            userDAO.updateLastLogin(user.getId());
            SessionManager.login(user);
            return user;

        } catch (SQLException e) {
            throw new BusinessException(
                "Σφάλμα επικοινωνίας με τη βάση δεδομένων.", e);
        }
    }

    public void logout() {
        SessionManager.logout();
    }
}
