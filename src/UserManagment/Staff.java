package UserManagment;
/**
 * Abstract class representing a staff member in the system.
 * Inherits from the `User` class and includes a staff-specific ID.
 */
public abstract class Staff extends User {
    protected String staffID;

    /**
     * Constructor for initializing a staff member.
     *
     * @param userID        The unique user ID.
     * @param password      The password for the staff member.
     * @param role          The role of the staff member (e.g., Doctor, Receptionist).
     * @param name          The name of the staff member.
     * @param email         The email address of the staff member.
     * @param contactNumber The contact number of the staff member.
     * @param staffID       The unique staff ID.
     */
    public Staff(int userID, String password, String role, String name, String email, String contactNumber, String staffID) {
        super(userID, password, role, name, email, contactNumber);
        this.staffID = staffID;
    }

    // Getter for staff ID
    /**
     * Gets the staff ID of the staff member.
     *
     * @return The staff ID as a string.
     */
    public String getStaffID() {
        return staffID;
    }
/**
     * Gets the role of the staff member.
     *
     * @return The role as a string.
     */
    public String getRole() {
        return role;
    }
/**
     * Gets the password of the staff member.
     *
     * @return The password as a string.
     */
    public String getPassword(){
        return password;
    }

}
