package UserManagment;
/**
 * Interface defining essential methods for user management.
 */
public interface UserInterface {

    /**
     * Logs the user in by verifying userID and password.
     *
     * @param userID The ID of the user attempting to log in.
     * @param password The password of the user.
     * @return true if login is successful, false otherwise.
     */
    boolean login(int userID, String password);

    public abstract void start();
    public abstract void close();

    /**
     * Changes the user's password.
     *
     * @param newPassword The new password to be set.
     */
    void changePassword(String newPassword);

    /**
     * Logs the user out.
     */
    void logout();

    /**
     * Checks if the user is currently logged in.
     *
     * @return true if the user is logged in, false otherwise.
     */
    boolean isLoggedIn();
}
