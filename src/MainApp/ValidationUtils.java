package MainApp;
/**
 * A utility class for validating common input formats such as email addresses
 * and contact numbers in the Hospital Management System.
 *
 * <p>This class provides static methods for validation and ensures input data
 * conforms to the required formats using regular expressions.</p>
 *
 * @author [Your Name]
 * @version 1.0
 */
public class ValidationUtils {

    /**
     * A regular expression pattern for validating email addresses.
     * <p>The regex allows for alphanumeric characters, dots, underscores,
     * and plus signs before the "@" symbol, and a domain name after it.</p>
     */
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    /**
     * Validates if the given email address matches the expected email format.
     *
     * @param email The email address to validate.
     * @return {@code true} if the email address is valid; {@code false} otherwise.
     */
    public static boolean isValidEmail(String email) {
        return email.matches(EMAIL_REGEX);
    }

    /**
     * Validates if the given contact number is valid.
     * <p>The contact number should consist of 1 to 8 digits only.</p>
     *
     * @param contactNumber The contact number to validate.
     * @return {@code true} if the contact number is valid; {@code false} otherwise.
     */
    public static boolean isValidContactNumber(String contactNumber) {
        return contactNumber.matches("\\d{1,8}");
    }
}
