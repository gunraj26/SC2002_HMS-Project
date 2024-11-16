package UserManagment;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import AppointmentManagement.Appointment;
import AppointmentManagement.AppointmentManager;
import MedicalRecords.MedicalRecord;

/**
 * Represents a patient in the hospital management system.
 * Extends the User class and provides specific functionality for patients,
 * including managing appointments, medical records, and updating personal details.
 */
public class Patient extends User {
    private LocalDate dob;
    private String gender;
    private String bloodType;
    private MedicalRecord medicalRecord;
    private AppointmentManager appointmentManager;
    private static final String PATIENT_FILE = "patients.txt";

    /**
 * Constructor for creating a new patient with an AppointmentManager.
 *
 * @param userID             The unique user ID.
 * @param password           The password.
 * @param role               The role (should be "Patient").
 * @param name               The patient's name.
 * @param email              The patient's email.
 * @param contactNumber      The patient's contact number.
 * @param appointmentManager The AppointmentManager object.
 */
    public Patient(int userID, String password, String role, String name, String email, String contactNumber, AppointmentManager appointmentManager) {
        super(userID, password, role, name, email, contactNumber);
        this.appointmentManager = appointmentManager;
        this.medicalRecord = new MedicalRecord(this);
    }
/**
 * Default constructor for a patient.
 */
    public Patient() {
        super(); // Calls User constructor
    }

/**
 * Constructor for an existing patient using their user ID and AppointmentManager.
 *
 * @param userID             The unique user ID.
 * @param appointmentManager The AppointmentManager object.
 */
    public Patient(int userID, AppointmentManager appointmentManager) {
        super(userID, "", "Patient", "", "", "");
        this.appointmentManager = appointmentManager;
        this.medicalRecord = new MedicalRecord(this);
        loadPatientData();
    }

    /**
 * Changes the patient's password after validating the current password and confirming the new one.
 *
 * @param newPassword The new password to set.
 */
    public void changePassword(String newPassword) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your current password: ");
        String currentPassword = scanner.nextLine();

        if (!this.password.equals(currentPassword)) {
            System.out.println("Incorrect current password. Password change failed.");
            return;
        }

        //System.out.print("Enter your new password: ");
        String newPass = newPassword; // Assuming newPassword is passed correctly

        //System.out.print("Confirm your new password: ");
        String confirmPassword = newPassword; // Assuming confirmation is handled elsewhere

        if (!newPass.equals(confirmPassword)) {
            System.out.println("Passwords do not match. Password change failed.");
            return;
        }

        // Update the password in the patients.txt file
        if (updatePasswordInFile(newPass)) {
            this.password = newPass;
            System.out.println("Password changed successfully.");
        } else {
            System.out.println("Error updating password. Please try again.");
        }
    }

    /**
 * Views the final bill for a specified appointment ID by reading from the invoices file.
 *
 * @param appointmentID The ID of the appointment for which to view the bill.
 */
    public void viewFinalBill(String appointmentID) {
        try (BufferedReader reader = new BufferedReader(new FileReader("invoices.txt"))) {
            String line;
            boolean billFound = false;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[0].equals(appointmentID) && parts[1].equals(String.valueOf(this.userID))) {
                    billFound = true;

                    // Parse invoice details
                    String appointmentIDFromFile = parts[0];
                    int patientIDFromFile = Integer.parseInt(parts[1]);
                    double consultationFee = Double.parseDouble(parts[2]);

                    String medicineDetailsStr = parts[3];
                    String[] medicineEntries = medicineDetailsStr.split("\\|");

                    List<String> medicineNames = new ArrayList<>();
                    List<Integer> quantities = new ArrayList<>();
                    List<Double> prices = new ArrayList<>();

                    for (String entry : medicineEntries) {
                        String[] medicineParts = entry.split(";");
                        if (medicineParts.length == 3) {
                            medicineNames.add(medicineParts[0]);
                            quantities.add(Integer.parseInt(medicineParts[1]));
                            prices.add(Double.parseDouble(medicineParts[2]));
                        }
                    }

                    double totalAmount = Double.parseDouble(parts[4]);

                    // Display the invoice
                    System.out.println("------- Invoice -------");
                    System.out.println("Appointment ID: " + appointmentIDFromFile);
                    System.out.println("Patient ID: " + patientIDFromFile);
                    System.out.println("Consultation Fee: $" + consultationFee);
                    System.out.println("Medicines:");
                    System.out.printf("%-20s %-10s %-10s%n", "Medicine", "Quantity", "Price");
                    for (int i = 0; i < medicineNames.size(); i++) {
                        double totalPrice = quantities.get(i) * prices.get(i);
                        System.out.printf("%-20s %-10d $%-10.2f%n", medicineNames.get(i), quantities.get(i), totalPrice);
                    }
                    System.out.println("-----------------------");
                    System.out.printf("Total Amount: $%.2f%n", totalAmount);

                    break;
                }
            }
            if (!billFound) {
                System.out.println("Invoice not found. Please check if the bill has been generated.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/**
 * Updates the patient's password in the patients file.
 *
 * @param newPassword The new password to update.
 * @return True if the password was successfully updated; false otherwise.
 */
    private boolean updatePasswordInFile(String newPassword) {
        File tempFile = new File("patients_temp.txt");
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(PATIENT_FILE));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length >= 9) {
                    int fileUserID = Integer.parseInt(details[0].trim());

                    if (fileUserID == this.userID) {
                        // Update the password field (index 1)
                        details[1] = newPassword;
                        line = String.join(",", details);
                        updated = true;
                    }
                }
                writer.write(line);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error updating password in file: " + e.getMessage());
            return false;
        }

        // Replace the old file with updated file
        File originalFile = new File(PATIENT_FILE);
        if (originalFile.delete()) {
            if (tempFile.renameTo(originalFile)) {
                return updated;
            } else {
                System.out.println("Error renaming temporary patient file.");
                return false;
            }
        } else {
            System.out.println("Error deleting original patient file.");
            return false;
        }
    }
/**
 * Views all upcoming appointments for the patient by fetching them from the AppointmentManager.
 */
    public void viewUpcomingAppointments() {
        appointmentManager.reloadAppointments();
        List<Appointment> appointments = appointmentManager.getAppointmentsByPatientID(getUserID());
        boolean hasUpcoming = false;
        for (Appointment appointment : appointments) {
            if (!appointment.getStatus().equalsIgnoreCase("Completed")) {
                appointment.viewAppointmentDetails();
                hasUpcoming = true;
            }
        }
        if (!hasUpcoming) {
            System.out.println("There are no upcoming appointments.");
        }
    }
/**
 * Views the outcome of a past appointment for a specified appointment ID, if completed.
 */
    public void viewPastAppointmentOutcomes() {
        appointmentManager.reloadAppointments();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Appointment ID to view outcome: ");
        String appointmentID = scanner.nextLine();

        Appointment appointment = appointmentManager.getAppointments().get(appointmentID);
        if (appointment != null && appointment.getStatus().equalsIgnoreCase("Completed")) {
            appointment.viewAppointmentDetails();
        } else {
            System.out.println("No completed appointment found with the specified ID.");
        }
    }

   /**
 * Displays the full medical record of the patient (read-only).
 */
    public void viewMedicalRecord() {
        medicalRecord.viewRecord();
    }

  /**
 * Gets the name of the patient.
 *
 * @return The patient's name.
 */
    public String getName() {
        return name;
    }
/**
 * Gets the date of birth of the patient.
 *
 * @return The patient's date of birth.
 */
    public LocalDate getDob() {
        return dob;
    }
/**
 * Gets the email of the patient.
 *
 * @return The patient's email.
 */
    public String getEmail() {
        return email;
    }
/**
 * Gets the gender of the patient.
 *
 * @return The patient's gender.
 */
    public String getGender() {
        return gender;
    }
/**
 * Gets the contact number of the patient.
 *
 * @return The patient's contact number.
 */
    public String getContact() {
        return contactNumber;
    }
/**
 * Gets the blood type of the patient.
 *
 * @return The patient's blood type.
 */
    public String getBloodType() {
        return bloodType;
    }
/**
 * Gets the medical record of the patient.
 *
 * @return The patient's MedicalRecord object.
 */
    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }

    /**
 * Updates the patient's contact information (email or phone number) interactively.
 */
    public void updatePatientData() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose the information you want to update:");
        System.out.println("1. Email");
        System.out.println("2. Contact");
        int choice = 0;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice. Returning to Patient menu.");
            return;
        }

        switch (choice) {
            case 1:
                System.out.print("Enter new email: ");
                String newEmail = scanner.nextLine();
                updateContactInformation(newEmail, this.contactNumber);
                break;
            case 2:
                System.out.print("Enter new contact number: ");
                String newContact = scanner.nextLine();
                updateContactInformation(this.email, newContact);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }

        // Save updated data back to the file
        updatePatientDataInFile();
        System.out.println("Patient Information updated successfully!");
    }

    /**
 * Saves the updated patient data back to the file.
 */
    private void updatePatientDataInFile() {
        File inputFile = new File(PATIENT_FILE);
        File tempFile = new File("patients_temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length >= 9 && details[0].equals(String.valueOf(this.userID))) {
                    // Write updated patient data to the temp file
                    writer.write(this.userID + "," + this.password + "," + this.role + "," +
                            this.name + "," + this.dob + "," + this.gender + "," +
                            this.email + "," + this.contactNumber + "," + this.bloodType);
                } else {
                    // Write original line for other patients
                    writer.write(line);
                }
                writer.newLine();
            }

        } catch (IOException e) {
            System.err.println("Error updating patient data in file: " + e.getMessage());
            return;
        }

        // Replace the original file with the updated file
        if (inputFile.delete()) {
            if (tempFile.renameTo(inputFile)) {
                //System.out.println("Patient file updated successfully.");
            } else {
                System.out.println("Error renaming temporary patient file.");
            }
        } else {
            System.out.println("Error deleting original patient file.");
        }
    }

    // Method to load patient data from the file based on userID
    /**
 * Loads patient data from the file based on the user ID.
 */
    private void loadPatientData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PATIENT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length < 9) {
                    System.err.println("Skipping malformed line: " + line);
                    continue;
                }

                if (Integer.parseInt(details[0]) == this.userID) {
                    // Set the patient attributes properly
                    this.password = details[1];
                    this.role = details[2];
                    this.name = details[3];

                    try {
                        this.dob = LocalDate.parse(details[4]);
                    } catch (Exception e) {
                        System.err.println("Error parsing date of birth for patient ID " + this.userID + ": " + e.getMessage());
                    }

                    this.gender = details[5];
                    this.email = details[6];
                    this.contactNumber = details[7];
                    this.bloodType = details[8];
                    break; // Break the loop once the patient is found and data is loaded
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading patient data: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in patient data file: " + e.getMessage());
        }
    }

    // Method to update contact information
    /**
 * Updates the patient's contact information.
 *
 * @param newEmail        The new email address.
 * @param newContactNumber The new contact number.
 */
    public void updateContactInformation(String newEmail, String newContactNumber) {
        this.email = newEmail;
        this.contactNumber = newContactNumber;
    }

    /**
 * Displays the patient-specific menu for actions like viewing medical records, appointments, and billing.
 */
@Override
    public void displayMenu() {
        System.out.println("=====================================");
        System.out.println("            Patient Portal           ");
        System.out.println("=====================================");
        System.out.println("1. View Medical Record");
        System.out.println("2. Update Contact Information");
        System.out.println("3. View Available Appointment Slots");
        System.out.println("4. Schedule an Appointment");
        System.out.println("5. Reschedule an Appointment");
        System.out.println("6. Cancel an Appointment");
        System.out.println("7. View Upcoming Appointments");
        System.out.println("8. View Past Appointment Outcomes");
        System.out.println("9. Change Password");
        System.out.println("10. View Final Bill");
        System.out.println("11. Logout");
        System.out.println("=====================================");
    }
}
