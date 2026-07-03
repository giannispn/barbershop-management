package gr.uniwa.barbershop.util;

/**
 * Thrown by the service layer when a business rule is violated (e.g. attempting
 * to double-book, deleting a customer with active appointments, or acting
 * without the required role). Controllers catch this to show a friendly,
 * localised message to the user.
 */
public class BusinessException extends Exception {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
