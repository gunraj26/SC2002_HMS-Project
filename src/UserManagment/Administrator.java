package UserManagment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import AppointmentManagement.Appointment;
import AppointmentManagement.AppointmentManager;
import InventoryManagement.Inventory;
import InventoryManagement.Medicine;

import java.io.File;

/**
 * Represents an Administrator user with capabilities to manage staff and inventory.
 */
public class Administrator extends User {
    private Inventory inventory;
    private List<Staff> staffList;
    private AppointmentManager appointmentManager;
    private static final String STAFF_FILE = "staff.txt";
    private static final String REPLENISHMENT_REQUEST_FILE = "replenishment_requests.txt";
    /**
     * Constructor for Administrator.
     *
     * @param userID             Unique identifier for the administrator.
     * @param password           Password for authentication.
     * @param role               Role of the user.
     * @param name               Name of the administrator.
     * @param email              Email address of the administrator.
     * @param contactNumber      Contact number of the administrator.
     * @param inventory          Inventory object to manage medicine inventory.
     * @param appointmentManager AppointmentManager object to manage appointments.
     */
    public Administrator(int userID, String password, String role, String name, String email, String contactNumber,
                         Inventory inventory, AppointmentManager appointmentManager) {
        super(userID, password, role, name, email, contactNumber);
        this.inventory = inventory;
        this.appointmentManager = appointmentManager;
    }
    /**
     * Approves a replenishment request for a specific medicine and updates its stock.
     *
     * @param medicine The Medicine object to replenish.
     * @param quantity The quantity to add to the stock.
     */
    public void approveReplenishment(Medicine medicine, int quantity) {
        inventory.updateStock(medicine, quantity);
        System.out.println("Replenishment approved for " + medicine.getName() + ". Quantity added: " + quantity);
    }
/**
 * Displays all medicines with stock below their respective thresholds.
 */
    public void checkLowStockAlerts() {
        inventory.checkLowStock();
    }

/**
 * Sets a new low stock alert level for a specific medicine.
 *
 * @param medicine    The Medicine object for which to set the alert level.
 * @param alertLevel  The new threshold level for low stock alerts.
 */
    public void setLowStockAlertLevel(Medicine medicine, int alertLevel) {
        inventory.setLowStockAlertLevel(medicine, alertLevel);
        System.out.println("Low stock alert level set for " + medicine.getName() + ": " + alertLevel);
    }

/**
 * Displays the current inventory of medicines.
 */
    public void viewInventory() {
        inventory.displayInventory();
    }

/**
 * Displays all pending replenishment requests.
 */
    public void viewPendingReplenishmentRequests() {
        System.out.println("Pending Replenishment Requests:");
        boolean pendingFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(REPLENISHMENT_REQUEST_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length == 2 && details[1].equalsIgnoreCase("Pending")) {
                    System.out.println("Medicine: " + details[0] + " | Status: " + details[1]);
                    pendingFound = true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading replenishment requests file: " + e.getMessage());
        }

        if (!pendingFound) {
            System.out.println("No pending replenishment requests found.");
        }
    }
/**
 * Allows the administrator to manage the medication inventory, including increasing stock.
 *
 * @param scanner Scanner object for user input.
 */
    public void manageMedicationInventory(Scanner scanner) {
        System.out.println("Current Inventory:");
        inventory.displayInventory();

        while (true) {
            System.out.print("Enter the name of the medicine to increase stock (or -1 to exit): ");
            String medicineName = scanner.nextLine();

            if (medicineName.equals("-1")) {
                break; // Exit the loop if -1 is entered
            }

            Medicine medicine = inventory.getMedicineByName(medicineName);
            if (medicine != null) {
                System.out.print("Enter the amount to increase stock by: ");
                int increaseAmount = 0;
                try {
                    increaseAmount = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a numeric value.");
                    continue;
                }

                if (increaseAmount > 0) {
                    inventory.updateStock(medicine, increaseAmount);
                    //System.out.println("Stock updated for " + medicineName + " by " + increaseAmount + " units.");
                } else {
                    System.out.println("Invalid amount. Please enter a positive number.");
                }
            } else {
                System.out.println("Medicine not found. Please enter a valid medicine name.");
            }
        }

        System.out.println("Inventory update complete.");
    }

    /**
 * Approves a specific replenishment request or rejects it if the stock is sufficient.
 *
 * @param medicineName The name of the medicine for which the replenishment request was made.
 */
    public void approveReplenishmentRequest(String medicineName) {
        boolean requestFound = false;
        Scanner scanner = new Scanner(System.in);

        File tempFile = new File("replenishment_requests_temp.txt");
        File originalFile = new File(REPLENISHMENT_REQUEST_FILE);

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length == 2 && details[0].equalsIgnoreCase(medicineName) && details[1].equalsIgnoreCase("Pending")) {
                    Medicine medicine = inventory.getMedicineByName(medicineName);

                    if (medicine != null) {
                        int currentStock = inventory.getStockLevel(medicine);
                        int threshold = inventory.getLowStockAlertLevel(medicine);

                        // Check if stock is actually below the threshold before approving
                        if (currentStock < threshold) {
                            System.out.print("Enter the quantity to replenish for " + medicineName + ": ");
                            int replenishAmount = 0;
                            try {
                                replenishAmount = Integer.parseInt(scanner.nextLine());
                                if (replenishAmount <= 0) {
                                    System.out.println("Invalid replenish amount. Skipping replenishment.");
                                    writer.write(medicineName + ",Rejected");
                                } else {
                                    inventory.updateStock(medicine, replenishAmount);
                                    writer.write(medicineName + ",Approved");
                                    System.out.println("Replenishment approved for " + medicineName + ". Stock increased by " + replenishAmount);
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input. Skipping replenishment.");
                                writer.write(medicineName + ",Rejected");
                            }
                        } else {
                            // Stock is sufficient; reject the replenishment request
                            writer.write(medicineName + ",Rejected");
                            System.out.println("Stock for " + medicineName + " is sufficient. Replenishment request rejected.");
                        }
                        requestFound = true;
                    } else {
                        // Medicine does not exist in inventory
                        System.out.println("Medicine '" + medicineName + "' does not exist in inventory.");
                        System.out.print("Do you want to approve adding it to inventory? (yes/no): ");
                        String response = scanner.nextLine();
                        if (response.equalsIgnoreCase("yes")) {
                            // Prompt admin to enter medicine details
                            System.out.print("Enter Medicine Name: ");
                            String name = scanner.nextLine();

                            // Ensure unique Medicine ID
                            String medicineID = generateUniqueMedicineID();

                            System.out.print("Enter Stock Quantity: ");
                            int stock = 0;
                            try {
                                stock = Integer.parseInt(scanner.nextLine());
                                if (stock < 0) {
                                    System.out.println("Stock cannot be negative. Setting stock to 0.");
                                    stock = 0;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input. Setting stock to 0.");
                            }

                            System.out.print("Enter Stock Threshold: ");
                            int thresholdInput = 0;
                            try {
                                thresholdInput = Integer.parseInt(scanner.nextLine());
                                if (thresholdInput < 0) {
                                    System.out.println("Threshold cannot be negative. Setting threshold to 0.");
                                    thresholdInput = 0;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input. Setting threshold to 0.");
                            }

                            System.out.print("Enter Price per Unit: "); // Added prompt for price
                            double price = 0.0;
                            try {
                                price = Double.parseDouble(scanner.nextLine());
                                if (price < 0) {
                                    System.out.println("Price cannot be negative. Setting price to $0.0.");
                                    price = 0.0;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid input. Setting price to $0.0.");
                            }

                            Medicine newMedicine = new Medicine(medicineID, name, stock, thresholdInput, price); // Added price
                            inventory.addMedicine(newMedicine);
                            //System.out.println("Medicine '" + name + "' added to inventory successfully.");

                            // Approve the replenishment request
                            writer.write(medicineName + ",Approved");
                            System.out.println("Replenishment request approved and medicine added to inventory.");
                        } else {
                            // Reject the replenishment request
                            writer.write(medicineName + ",Rejected");
                            System.out.println("Replenishment request rejected.");
                        }
                        requestFound = true;
                    }
                } else {
                    writer.write(line); // Write the line as is if it doesn't match the request
                }
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error updating replenishment request file: " + e.getMessage());
            return;
        }

        // Replace old file with updated temp file
        if (originalFile.delete()) {
            if (tempFile.renameTo(originalFile)) {
                if (!requestFound) {
                    System.out.println("No pending request found for " + medicineName + ".");
                }
            } else {
                System.out.println("Error renaming temporary replenishment request file.");
            }
        } else {
            System.out.println("Error deleting original replenishment request file.");
        }
    }

/**
 * Generates a unique Medicine ID that does not conflict with existing medicine IDs.
 *
 * @return A unique Medicine ID.
 */
    private String generateUniqueMedicineID() {
        // Assuming Medicine IDs start with 'M' followed by three digits
        int numericPart = 1;
        String medicineID;
        do {
            medicineID = "M" + String.format("%03d", numericPart);
            numericPart++;
        } while (inventory.medicineIDExists(medicineID));
        return medicineID;
    }
/**
 * Displays appointment details based on different filtering criteria (all, by patient ID, or by doctor ID).
 *
 * @param scanner Scanner object for user input.
 */
    public void viewAppointmentDetails(Scanner scanner) {
        appointmentManager.reloadAppointments(); // Ensure appointments are reloaded from file

        System.out.println("Choose an option to view appointments:");
        System.out.println("1. View All Appointments");
        System.out.println("2. View Appointments by Patient ID");
        System.out.println("3. View Appointments by Doctor ID");

        int choice = 0;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Returning to Administrator menu.");
            return;
        }

        switch (choice) {
            case 1:
                // View all appointments
                System.out.println("All Appointments:");
                if (appointmentManager.getAppointments().isEmpty()) {
                    System.out.println("No appointments available.");
                } else {
                    for (Appointment appointment : appointmentManager.getAppointments().values()) {
                        appointment.viewAppointmentDetails();
                    }
                }
                break;
            case 2:
                // View appointments by Patient ID
                System.out.print("Enter Patient ID: ");
                int patientID = 0;
                try {
                    patientID = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid Patient ID. Returning to Administrator menu.");
                    break;
                }
                List<Appointment> patientAppointments = appointmentManager.getAppointmentsByPatientID(patientID);
                if (patientAppointments.isEmpty()) {
                    System.out.println("No appointments found for Patient ID: " + patientID);
                } else {
                    for (Appointment appointment : patientAppointments) {
                        appointment.viewAppointmentDetails();
                    }
                }
                break;
            case 3:
                // View appointments by Doctor ID
                System.out.print("Enter Doctor ID: ");
                String doctorID = scanner.nextLine();
                List<Appointment> doctorAppointments = appointmentManager.getAppointmentsByDoctorID(doctorID);
                if (doctorAppointments.isEmpty()) {
                    System.out.println("No appointments found for Doctor ID: " + doctorID);
                } else {
                    for (Appointment appointment : doctorAppointments) {
                        appointment.viewAppointmentDetails();
                    }
                }
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    /**
 * Displays the Administrator-specific menu options.
 */
    public void displayMenu() {
        System.out.println("=====================================");
        System.out.println("         Administrator Portal        ");
        System.out.println("=====================================");
        System.out.println("1. View and Manage Hospital Staff");
        System.out.println("2. View Appointments Details");
        System.out.println("3. View and Manage Medication Inventory");
        System.out.println("4. View Pending Replenishment Requests");
        System.out.println("5. Approve Replenishment Requests");
        System.out.println("6. Logout");
        System.out.println("=====================================");
    }
/**
 * Manages the hospital staff, including viewing, adding, updating, and removing staff members.
 *
 * @param scanner Scanner object for user input.
 */

    public void manageHospitalStaff(Scanner scanner) {
        boolean backToMenu = false;
        while (!backToMenu) {
            System.out.println("Hospital Staff Management Menu:");
            System.out.println("1. View Staff List filtered by role ");
            System.out.println("2. Add New Staff");
            System.out.println("3. Update Staff Information");
            System.out.println("4. Remove Staff");
            System.out.println("5. Exit");

            int choice = 0;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric choice.");
                continue;
            }

            switch (choice) {
                case 1:
                    viewStaffList(scanner);
                    break;
                case 2:
                    addNewStaff(scanner);
                    break;
                case 3:
                    updateStaffInformation(scanner);
                    break;
                case 4:
                    removeStaff(scanner);
                    break;
                case 5:
                    System.out.println("Exiting Staff Management.");
                    backToMenu = true; // Exit the loop and return to the main Administrator menu
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
/**
 * Displays the list of staff members filtered by their roles.
 *
 * @param scanner Scanner object for user input.
 */
    private void viewStaffList(Scanner scanner) {
        System.out.println("Staff List:");

        List<String> doctorsList = new ArrayList<>();
        List<String> pharmacistsList = new ArrayList<>();
        List<String> receptionistsList = new ArrayList<>();
        List<String> adminList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(STAFF_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                String role = details[2].trim();

                if (role.equalsIgnoreCase("Doctor") && details.length >= 8) {
                    // Format doctor details
                    String doctorDetails = "User ID: " + details[0] + ", Staff ID: " + details[6] + ", Name: " + details[3] +
                            ", Email: " + details[4] + ", Contact Number: " + details[5] + ", Specialization: " + details[7];
                    doctorsList.add(doctorDetails);
                } else if (role.equalsIgnoreCase("Pharmacist") && details.length >= 7) {
                    // Format pharmacist details
                    String pharmacistDetails = "User ID: " + details[0] + ", Staff ID: " + details[6] + ", Name: " + details[3] +
                            ", Email: " + details[4] + ", Contact Number: " + details[5];
                    pharmacistsList.add(pharmacistDetails);
                }
                else if (role.equalsIgnoreCase("Administrator") && details.length >= 7) {
                    // Format pharmacist details
                    String adminDetails = "User ID: " + details[0] + ", Staff ID: " + details[6] + ", Name: " + details[3] +
                                ", Email: " + details[4] + ", Contact Number: " + details[5];
                    adminList.add(adminDetails);
                } else if (role.equalsIgnoreCase("Receptionist") && details.length >= 7) {
                    // Format receptionist details
                    String receptionistDetails = "User ID: " + details[0] + ", Staff ID: " + details[6] + ", Name: " + details[3] +
                            ", Email: " + details[4] + ", Contact Number: " + details[5];
                    receptionistsList.add(receptionistDetails);
                } else {
                    System.out.println("Skipping invalid entry in staff file: " + line);
                }
            }

            // Display doctors
            System.out.println("\nDoctors:");
            if (doctorsList.isEmpty()) {
                System.out.println("No doctors found.");
            } else {
                for (String doctor : doctorsList) {
                    System.out.println(doctor);
                    System.out.println("-----");
                }
            }

            // Display pharmacists
            System.out.println("\nPharmacists:");
            if (pharmacistsList.isEmpty()) {
                System.out.println("No pharmacists found.");
            } else {
                for (String pharmacist : pharmacistsList) {
                    System.out.println(pharmacist);
                    System.out.println("-----");
                }
            }

            // Display receptionists
            System.out.println("\nReceptionists:");
            if (receptionistsList.isEmpty()) {
                System.out.println("No receptionists found.");
            } else {
                for (String receptionist : receptionistsList) {
                    System.out.println(receptionist);
                    System.out.println("-----");
                }
            }
            System.out.println("\nAdministrators");
            if (receptionistsList.isEmpty()) {
                System.out.println("No Admins found.");
            } else {
                for (String admin : adminList) {
                    System.out.println(admin);
                    System.out.println("-----");
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading staff file: " + e.getMessage());
        }
    }
/**
 * Adds a new staff member to the system.
 *
 * @param scanner Scanner object for user input.
 */
    private void addNewStaff(Scanner scanner) {
        // Read existing User IDs and Staff IDs to ensure uniqueness
        Set<Integer> existingUserIDs = new HashSet<>();
        Set<String> existingStaffIDs = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(STAFF_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                int userID = Integer.parseInt(details[0].trim());
                String staffID = details[6].trim();
                existingUserIDs.add(userID);
                existingStaffIDs.add(staffID);
            }
        } catch (IOException e) {
            System.out.println("Error reading staff file: " + e.getMessage());
        }

        // Generate a new unique User ID
        int userID = generateUniqueUserID(existingUserIDs);

        System.out.println("Generated User ID: " + userID);

        // Set default password
        String password = "password123";

        System.out.print("Enter Role (Doctor/Pharmacist/Receptionist): ");
        String role = scanner.nextLine();

        if (!role.equalsIgnoreCase("Doctor") && !role.equalsIgnoreCase("Pharmacist") && !role.equalsIgnoreCase("Receptionist")) {
            System.out.println("Invalid role entered. Staff not added.");
            return;
        }

        System.out.print("Enter Name: ");
        String name = scanner.nextLine();

        System.out.print("Enter Email: ");
        String email = scanner.nextLine();

        System.out.print("Enter Contact Number: ");
        String contactNumber = scanner.nextLine();

        // Generate a new unique Staff ID
        String staffID = generateUniqueStaffID(existingStaffIDs, role);
        System.out.println("Generated Staff ID: " + staffID);

        String specialization = "";
        if (role.equalsIgnoreCase("Doctor")) {
            System.out.print("Enter Specialization: ");
            specialization = scanner.nextLine();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(STAFF_FILE, true))) {
            if (role.equalsIgnoreCase("Doctor")) {
                System.out.print("Enter Price per Unit for Medicine Specialization (if applicable, else enter 0): ");
                double price = 0.0;
                try {
                    price = Double.parseDouble(scanner.nextLine());
                    if (price < 0) {
                        System.out.println("Price cannot be negative. Setting price to $0.0.");
                        price = 0.0;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Setting price to $0.0.");
                }

                writer.write(userID + "," + password + "," + role + "," + name + "," + email + "," + contactNumber + "," + staffID + "," + specialization + "," + price);
            } else if (role.equalsIgnoreCase("Pharmacist") || role.equalsIgnoreCase("Receptionist")) {
                writer.write(userID + "," + password + "," + role + "," + name + "," + email + "," + contactNumber + "," + staffID);
            }
            writer.newLine();
            System.out.println("Staff member added successfully with default password: " + password);
        } catch (IOException e) {
            System.out.println("Error adding new staff to file: " + e.getMessage());
        }
    }
/**
 * Generates a unique User ID that does not conflict with existing User IDs.
 *
 * @param existingUserIDs A set of existing User IDs.
 * @return A unique User ID.
 */
    private int generateUniqueUserID(Set<Integer> existingUserIDs) {
        int newUserID = 100; // Starting User ID
        while (existingUserIDs.contains(newUserID)) {
            newUserID++;
        }
        return newUserID;
    }
/**
 * Generates a unique Staff ID based on the role and existing staff IDs.
 *
 * @param existingStaffIDs A set of existing Staff IDs.
 * @param role             The role of the staff member (e.g., Doctor, Pharmacist, Receptionist).
 * @return A unique Staff ID.
 */
    private String generateUniqueStaffID(Set<String> existingStaffIDs, String role) {
        String prefix;
        if (role.equalsIgnoreCase("Doctor")) {
            prefix = "D";
        } else if (role.equalsIgnoreCase("Pharmacist")) {
            prefix = "P";
        } else if (role.equalsIgnoreCase("Receptionist")) {
            prefix = "R";
        } else {
            prefix = "U"; // Unknown role
        }
        int numericPart = 1;
        String newStaffID;
        do {
            newStaffID = prefix + String.format("%03d", numericPart);
            numericPart++;
        } while (existingStaffIDs.contains(newStaffID));
        return newStaffID;
    }
/**
 * Updates the information of an existing staff member.
 *
 * @param scanner Scanner object for user input.
 */
    private void updateStaffInformation(Scanner scanner) {
        System.out.print("Enter User ID to update: ");
        int userID = 0;
        try {
            userID = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid User ID. Returning to Staff Management menu.");
            return;
        }

        File tempFile = new File("staff_temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(STAFF_FILE));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean updated = false;

            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                int existingUserID = Integer.parseInt(details[0].trim());

                if (existingUserID == userID) {
                    System.out.println("Updating information for User ID: " + userID);
                    String role = details[2].trim();

                    System.out.print("Enter new Email (or -1 to skip): ");
                    String newEmail = scanner.nextLine();
                    if (!newEmail.equals("-1")) {
                        details[4] = newEmail;
                    }

                    System.out.print("Enter new Contact Number (or -1 to skip): ");
                    String newContactNumber = scanner.nextLine();
                    if (!newContactNumber.equals("-1")) {
                        details[5] = newContactNumber;
                    }

                    if (role.equalsIgnoreCase("Doctor") && details.length >= 8) {
                        System.out.print("Enter new Specialization (or -1 to skip): ");
                        String newSpecialization = scanner.nextLine();
                        if (!newSpecialization.equals("-1")) {
                            details[7] = newSpecialization;
                        }

                        // If there's a price field, update it as well
                        if (details.length >= 9) {
                            System.out.print("Enter new Price per Unit (or -1 to skip): ");
                            String newPriceInput = scanner.nextLine();
                            if (!newPriceInput.equals("-1")) {
                                try {
                                    double newPrice = Double.parseDouble(newPriceInput);
                                    if (newPrice >= 0) {
                                        details[8] = String.valueOf(newPrice);
                                    } else {
                                        System.out.println("Price cannot be negative. Keeping existing price.");
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Invalid price input. Keeping existing price.");
                                }
                            }
                        }
                    }

                    line = String.join(",", details);
                    updated = true;
                }
                writer.write(line);
                writer.newLine();
            }

            if (updated) {
                System.out.println("Staff information updated successfully.");
            } else {
                System.out.println("User ID not found.");
            }

        } catch (IOException e) {
            System.out.println("Error updating staff information: " + e.getMessage());
        }

        // Replace the old file with updated file
        File originalFile = new File(STAFF_FILE);
        if (originalFile.delete()) {
            if (tempFile.renameTo(originalFile)) {
                System.out.println("Staff file updated successfully.");
            } else {
                System.out.println("Error renaming temporary staff file.");
            }
        } else {
            System.out.println("Error deleting original staff file.");
        }
    }
/**
 * Removes a staff member from the system based on their User ID.
 *
 * @param scanner Scanner object for user input.
 */
    private void removeStaff(Scanner scanner) {
        System.out.print("Enter User ID to remove: ");
        int userID = 0;
        try {
            userID = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid User ID. Returning to Staff Management menu.");
            return;
        }

        File tempFile = new File("staff_temp.txt");
        File originalFile = new File(STAFF_FILE);

        try (BufferedReader reader = new BufferedReader(new FileReader(originalFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean removed = false;

            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                int existingUserID = Integer.parseInt(details[0].trim());

                if (existingUserID != userID) {
                    writer.write(line);
                    writer.newLine();
                } else {
                    removed = true;
                }
            }

            if (removed) {
                System.out.println("Staff member removed successfully.");
            } else {
                System.out.println("User ID not found.");
            }

        } catch (IOException e) {
            System.out.println("Error removing staff member: " + e.getMessage());
            return;
        }

        // Replace the old file with updated file
        if (originalFile.delete()) {
            if (tempFile.renameTo(originalFile)) {
                System.out.println("Staff file updated successfully.");
            } else {
                System.out.println("Error renaming temporary staff file.");
            }
        } else {
            System.out.println("Error deleting original staff file.");
        }
    }
}
