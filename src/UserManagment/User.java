package UserManagment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import AppointmentManagement.AppointmentManager;
import InventoryManagement.Inventory;
import InventoryManagement.Pharmacist;

/**
 * Represents a generic user in the system. This is an abstract class
 * and serves as the base for different user roles like Doctor, Administrator, and Pharmacist.
 */
public abstract class User {
    protected int userID;
    protected String password;
    protected String role;
    protected String name;
    protected String email;
    protected String contactNumber;
/**
     * Default constructor for User.
     */
    public User() {

    }

     /**
     * Constructs a user with specified details.
     *
     * @param userID        The unique user ID.
     * @param password      The password for the user.
     * @param role          The role of the user (e.g., Doctor, Administrator).
     * @param name          The name of the user.
     * @param email         The email address of the user.
     * @param contactNumber The contact number of the user.
     */
    public User(int userID, String password, String role, String name, String email, String contactNumber) {
        this.userID = userID;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
    }

    /**
     * Checks if a given user ID exists for a specific role.
     *
     * @param userID The user ID to check.
     * @param role   The role to check against.
     * @return True if the user ID exists for the given role, otherwise false.
     */
    public static boolean checkUserIDExists(int userID, String role) {
        String fileName = "staff.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length >= 3) {
                    int fileUserID = Integer.parseInt(details[0].trim());
                    String fileRole = details[2].trim();
                    if (fileUserID == userID && fileRole.equalsIgnoreCase(role)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading staff file: " + e.getMessage());
        }
        return false;
    }
    

    /**
     * Validates login credentials for a user.
     *
     * @param inputUserID   The entered user ID.
     * @param inputPassword The entered password.
     * @return True if the credentials match, otherwise false.
     */
    public boolean login(int inputUserID, String inputPassword) {
        return this.userID == inputUserID && this.password.equals(inputPassword);
    }

    /**
     * Loads a Doctor object from the staff file if credentials match.
     *
     * @param userID             The user ID of the doctor.
     * @param password           The password of the doctor.
     * @param appointmentManager The AppointmentManager instance to associate with the doctor.
     * @return A Doctor object if found, otherwise null.
     */
    public static Doctor loadDoctorFromFile(int userID, String password, AppointmentManager appointmentManager) {
        String fileName = "staff.txt"; // This is the file where doctor details are stored
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length >= 8) { // Check if there are enough details in each line
                    int fileUserID = Integer.parseInt(details[0].trim());
                    String filePassword = details[1].trim();
                    String role = details[2].trim();
                    String name = details[3].trim();
                    String email = details[4].trim();
                    String contactNumber = details[5].trim();
                    String staffID = details[6].trim();
                    String specialization = details[7].trim().toUpperCase();
    
                    // If role is Doctor, and userID and password match, create a Doctor object
                    if (role.equalsIgnoreCase("Doctor") && fileUserID == userID && filePassword.equals(password)) {
                        return new Doctor(fileUserID, filePassword, role, name, email, contactNumber, staffID, specialization, appointmentManager);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading staff file: " + e.getMessage());
        }
        return null; // Return null if no matching doctor is found
    }

   /**
     * Loads an Administrator object from the staff file if credentials match.
     *
     * @param userID             The user ID of the administrator.
     * @param password           The password of the administrator.
     * @param inventory          The Inventory instance to associate with the administrator.
     * @param appointmentManager The AppointmentManager instance to associate with the administrator.
     * @return An Administrator object if found, otherwise null.
     */
public static Administrator loadAdministratorFromFile(int userID, String password, Inventory inventory, AppointmentManager appointmentManager) {
    String fileName = "staff.txt"; // File containing administrator details
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] details = line.split(",");
            if (details.length >= 7) {
                int fileUserID = Integer.parseInt(details[0].trim());
                String filePassword = details[1].trim();
                String role = details[2].trim();
                String name = details[3].trim();
                String email = details[4].trim();
                String contactNumber = details[5].trim();
                String staffID = details[6].trim();

                // If role is Administrator, and userID and password match, create an Administrator object
                if (role.equalsIgnoreCase("Administrator") && fileUserID == userID && filePassword.equals(password)) {
                    return new Administrator(fileUserID, filePassword, role, name, email, contactNumber, inventory, appointmentManager);
                }
            }
        }
    } catch (IOException e) {
        System.err.println("Error reading staff file: " + e.getMessage());
    }
    return null; // Return null if no matching administrator is found
}


/**
     * Loads a Pharmacist object from the staff file if credentials match.
     *
     * @param userID    The user ID of the pharmacist.
     * @param password  The password of the pharmacist.
     * @param inventory The Inventory instance to associate with the pharmacist.
     * @return A Pharmacist object if found, otherwise null.
     */
public static Pharmacist loadPharmacistFromFile(int userID, String password, Inventory inventory) {
    String fileName = "staff.txt"; // File containing pharmacist details
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] details = line.split(",");
            if (details.length >= 7) {
                int fileUserID = Integer.parseInt(details[0].trim());
                String filePassword = details[1].trim();
                String role = details[2].trim();
                String name = details[3].trim();
                String email = details[4].trim();
                String contactNumber = details[5].trim();
                String staffID = details[6].trim();
                // For Pharmacist, there might not be a specialization field

                // If role is Pharmacist, and userID and password match, create a Pharmacist object
                if (role.equalsIgnoreCase("Pharmacist") && fileUserID == userID && filePassword.equals(password)) {
                    return new Pharmacist(fileUserID, filePassword, role, name, email, contactNumber, staffID, inventory);
                }
            }
        }
    } catch (IOException e) {
        System.err.println("Error reading staff file: " + e.getMessage());
    }
    return null; // Return null if no matching pharmacist is found
}

/**
     * Gets the user ID of the user.
     *
     * @return The user ID.
     */
    public int getUserID() {
        return userID;
    }
    /**
     * Gets the name of the user.
     *
     * @return The name of the user.
     */
    public String getName() {
        return name;
    }
    /**
     * Gets the email of the user.
     *
     * @return The email address of the user.
     */

    public String getEmail() {
        return email;
    }
    /**
     * Gets the contact number of the user.
     *
     * @return The contact number of the user.
     */
    public String getContactNumber() {
        return contactNumber;
    }
/**
     * Updates the contact information of the user.
     *
     * @param email         The new email address.
     * @param contactNumber The new contact number.
     */
    public void updateContactInformation(String email, String contactNumber) {
        this.email = email;
        this.contactNumber = contactNumber;
        System.out.println("Contact information updated successfully.");
    }

    /**
     * Checks equality between this user and another object.
     *
     * @param obj The object to compare with.
     * @return True if the other object is a User with the same user ID, otherwise false.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return userID == user.userID;
    }
    /**
     * Updates the user's password in the staff file.
     *
     * @param newPassword The new password to set.
     * @return True if the password update is successful, otherwise false.
     */
    private boolean updatePasswordInFile(String newPassword) {
    File tempFile = new File("staff_temp.txt");
    boolean updated = false;

    try (BufferedReader reader = new BufferedReader(new FileReader("staff.txt"));
         BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

        String line;
        while ((line = reader.readLine()) != null) {
            String[] details = line.split(",");
            int fileUserID = Integer.parseInt(details[0].trim());

            if (fileUserID == this.userID) {
                // Update the password field (index 1)
                details[1] = newPassword;
                line = String.join(",", details);
                updated = true;
            }
            writer.write(line);
            writer.newLine();
        }

    } catch (IOException e) {
        System.out.println("Error updating password in file: " + e.getMessage());
        return false;
    }

    // Replace the old file with updated file
    File originalFile = new File("staff.txt");
    if (!originalFile.delete() || !tempFile.renameTo(originalFile)) {
        System.out.println("Error finalizing password update.");
        return false;
    }

    return updated;
}


    
/**
     * Allows the user to change their password interactively.
     */
    public void changePassword(String newP) {
    Scanner scanner = new Scanner(System.in);

    System.out.print("Enter your current password: ");
    String currentPassword = scanner.nextLine();

    if (!this.password.equals(currentPassword)) {
        System.out.println("Incorrect current password. Password change failed.");
        return;
    }

    System.out.print("Confirm your new password: ");
    String confirmPassword = scanner.nextLine();

    if (!newP.equals(confirmPassword)) {
        System.out.println("Passwords do not match. Password change failed.");
        return;
    }

    // Update the password in the staff.txt file
    if (updatePasswordInFile(newP)) {
        this.password = newP;
        System.out.println("Password changed successfully.");
    } else {
        System.out.println("Error updating password. Please try again.");
    }
}


    /**
     * Displays the role-specific menu for the user. 
     * Must be implemented by subclasses.
     */
    public abstract void displayMenu();
}
