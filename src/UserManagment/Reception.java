package UserManagment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import AppointmentManagement.Appointment;
import AppointmentManagement.AppointmentManager;
import AppointmentManagement.Invoice;
import InventoryManagement.Billing;
import InventoryManagement.Inventory;
/**
 * Represents a Receptionist in the hospital management system. 
 * Provides functionality for managing patient registration, 
 * appointment scheduling, billing, and interacting with doctors.
 */
public class Reception extends User {
    private AppointmentManager appointmentManager;
    private Map<String, Doctor> doctors;
    private static final String PATIENT_FILE = "patients.txt";
    private Inventory inventory;
    private Billing billing;

    /**
 * Constructor for Receptionist.
 *
 * @param userID             The unique user ID.
 * @param password           The password.
 * @param role               The role (should be "Receptionist").
 * @param name               The receptionist's name.
 * @param email              The receptionist's email.
 * @param contactNumber      The receptionist's contact number.
 * @param appointmentManager The AppointmentManager instance.
 * @param doctors            A map of doctors, with their IDs as keys.
 * @param inventory          The inventory object for managing medicines.
 */
    public Reception(int userID, String password, String role, String name, String email, String contactNumber,
    AppointmentManager appointmentManager, Map<String, Doctor> doctors, Inventory inventory) {
        super(userID, password, role, name, email, contactNumber);
        this.appointmentManager = appointmentManager;
        this.doctors = doctors;
        this.inventory = inventory;
        this.billing = new Billing(inventory);
        }

    Scanner sc = new Scanner(System.in);


/**
 * Generates a bill for a patient's appointment, updates the appointment record, and stores the invoice.
 *
 * @param patientID     The patient's ID.
 * @param appointmentID The appointment's ID.
 */
public void generateBill(int patientID, String appointmentID) {
    try {
        File inputFile = new File("appointments.txt");
        File tempFile = new File("appointments_temp.txt");

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        String line;
        boolean appointmentFound = false;

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts[0].equals(appointmentID) && parts[1].equals(String.valueOf(patientID))) {
                appointmentFound = true;
                String lastField = parts[parts.length - 1];
                boolean billAlreadyGenerated = false;
                try {
                    Double.parseDouble(lastField);
                    billAlreadyGenerated = true;
                } catch (NumberFormatException e) {
                    billAlreadyGenerated = false;
                }

                if (billAlreadyGenerated) {
                    System.out.println("Bill has already been generated for this appointment.");
                } else {
                    // Get medicines and quantities
                    String medicinesStr = parts[8];
                    String quantitiesStr = parts[9];

                    String[] medicines = medicinesStr.split(";");
                    String[] quantitiesStrArr = quantitiesStr.split(";");
                    int[] quantities = new int[quantitiesStrArr.length];
                    for (int i = 0; i < quantitiesStrArr.length; i++) {
                        quantities[i] = Integer.parseInt(quantitiesStrArr[i]);
                    }

                    // Generate Invoice
                    Invoice invoice = billing.generateInvoice(appointmentID, patientID, medicines, quantities);

                    // After generating the invoice
invoice.displayInvoice();

// Debug statement to check invoice details
System.out.println("Invoice Details to be saved: " + invoice.getInvoiceDetails());

// Store invoice details to a file
try (BufferedWriter invoiceWriter = new BufferedWriter(new FileWriter("invoices.txt", true))) {
    invoiceWriter.write(invoice.getInvoiceDetails());
    invoiceWriter.newLine();
} catch (IOException e) {
    System.out.println("Error writing invoice details: " + e.getMessage());
}

                    // Update line with total bill
                    String newLine = line + "," + invoice.getTotalAmount();
                    writer.write(newLine);
                    writer.newLine();

                    continue; // Skip writing the old line
                }
            } else {
                writer.write(line);
                writer.newLine();
            }
        }
        reader.close();
        writer.close();

        // Replace original file with updated temp file
        if (!inputFile.delete()) {
            System.out.println("Could not delete original appointmentRecord.txt");
            return;
        }
        if (!tempFile.renameTo(inputFile)) {
            System.out.println("Could not rename temp file to appointmentRecord.txt");
        }

        if (!appointmentFound) {
            System.out.println("Appointment not found.");
        }

    } catch (IOException e) {
        e.printStackTrace();
    }
}

    

/**
 * Creates or retrieves an existing patient. If the user ID is new, prompts for registration.
 *
 * @return A `Patient` object representing the patient.
 */
    public Patient createPatient() {
        System.out.print("Enter your User ID (Enter 0 if you are a new Patient): ");
        int userID = this.sc.nextInt();
        this.sc.nextLine(); // Consume newline
    
        if (userID == 0) {
            return this.registerNewPatient();
        } else if (this.patientExists(userID)) {
            Patient patient = this.loginExistingPatient(userID);
            // If patient is null, login attempts were exhausted
            return patient;
        } else {
            createPatient();
            System.out.println("User ID not found. Registering as a new patient.");
            return this.registerNewPatient();
        }
    }
    

/**
 * Registers a new patient by prompting for their details and adding them to the patient file.
 *
 * @return A `Patient` object representing the newly registered patient.
 */
    public Patient registerNewPatient() {
        // Prompt for User ID
        System.out.print("Create a new User ID (numeric, unique): ");
        String userIDInput = this.sc.nextLine();
        int userID = -1;
    
        while (true) {
            // Validate that the input is numeric
            if (!userIDInput.matches("\\d+")) {
                System.out.print("Invalid User ID. Please enter numeric digits only: ");
            } else {
                userID = Integer.parseInt(userIDInput);
                // Check if User ID is unique
                if (patientExists(userID)) {
                    System.out.print("User ID already exists. Please choose a different User ID: ");
                } else {
                    break; // User ID is valid and unique
                }
            }
            userIDInput = this.sc.nextLine();
        }
    
        // Proceed with the rest of the registration
        System.out.print("Enter your name (letters and spaces only): ");
        String name = this.sc.nextLine();
        while (!name.matches("[a-zA-Z ]+")) {
            System.out.print("Invalid name. Please enter letters and spaces only: ");
            name = this.sc.nextLine();
        }
    
        System.out.print("Enter your date of birth (YYYY-MM-DD): ");
        String dobString = this.sc.nextLine();
        LocalDate dob = null;
        while (dob == null) {
            try {
                dob = LocalDate.parse(dobString);
            } catch (DateTimeParseException e) {
                System.out.print("Invalid date format. Please enter in YYYY-MM-DD format: ");
                dobString = this.sc.nextLine();
            }
        }
    
        System.out.print("Enter your gender (M/F): ");
        String gender = this.sc.nextLine().toUpperCase();
        while (!gender.equals("M") && !gender.equals("F")) {
            System.out.print("Invalid gender. Please enter 'M' or 'F': ");
            gender = this.sc.nextLine().toUpperCase();
        }
    
        System.out.print("Enter your email: ");
        String email = this.sc.nextLine();
        while (!isValidEmail(email)) {
            System.out.print("Invalid email format. Please enter a valid email: ");
            email = this.sc.nextLine();
        }
    
        System.out.print("Enter your contact number (digits only): ");
        String contact = this.sc.nextLine();
        while (!contact.matches("\\d+")) {
            System.out.print("Invalid contact number. Please enter digits only: ");
            contact = this.sc.nextLine();
        }
    
        System.out.print("Enter your blood group (e.g., A+, O-): ");
        String bloodGroup = this.sc.nextLine();
        while (!isValidBloodGroup(bloodGroup)) {
            System.out.print("Invalid blood group. Please enter a valid blood group (e.g., A+, O-): ");
            bloodGroup = this.sc.nextLine();
        }
    
        System.out.print("Create a password: ");
        String password = this.sc.nextLine();
    
        // Add new patient to file
        addNewPatientToFile(userID, password, name, dob.toString(), gender, email, contact, bloodGroup);
        System.out.println("Registration successful! Please log in.");
    
        // Proceed to login
        return loginExistingPatient(userID);
    }
    
/**
 * Validates if the given email has a valid format.
 *
 * @param email The email to validate.
 * @return True if the email is valid, false otherwise.
 */
private boolean isValidEmail(String email) {
    String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    return email.matches(emailRegex);
}
/**
 * Validates if the given blood group is valid.
 *
 * @param bloodGroup The blood group to validate.
 * @return True if the blood group is valid, false otherwise.
 */
private boolean isValidBloodGroup(String bloodGroup) {
    Set<String> validBloodGroups = new HashSet<>(Arrays.asList(
        "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    ));
    return validBloodGroups.contains(bloodGroup.toUpperCase());
}

/**
 * Logs in an existing patient by validating their credentials.
 *
 * @param userID The patient's user ID.
 * @return A `Patient` object if login is successful, null otherwise.
 */

    // Log in an existing patient
    public Patient loginExistingPatient(int userID) {
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;
        Patient patient = null;
    
        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Enter your password: ");
            String password = this.sc.nextLine();
            if (this.authenticatePatient(userID, password)) {
                System.out.println("Login successful!");
                patient = new Patient(userID, this.appointmentManager);
                return patient; // Successful login
            } else {
                attempts++;
                if (attempts < MAX_ATTEMPTS) {
                    System.out.println("Invalid credentials. Please try again.");
                }
            }
        }
    
        System.out.println("Too many failed login attempts. Returning to the main menu.");
        return null; // Failed to log in after MAX_ATTEMPTS
    }
    
/**
 * Checks if a patient exists in the file based on their user ID.
 *
 * @param userID The patient's user ID.
 * @return True if the patient exists, false otherwise.
 */
    // Check if a patient exists based on ID
    public boolean patientExists(int userID) {
        try (BufferedReader reader = new BufferedReader(new FileReader(PATIENT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (Integer.parseInt(details[0]) == userID) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading patient file: " + e.getMessage());
        }
        return false;
    }
    /**
 * Authenticates a patient based on their user ID and password.
 *
 * @param patientID The patient's user ID.
 * @param password  The password to validate.
 * @return True if authentication is successful, false otherwise.
 */
    // Authenticate patient based on ID and password
    private boolean authenticatePatient(int patientID, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(PATIENT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (Integer.parseInt(details[0]) == patientID && details[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading patient file: " + e.getMessage());
        }
        return false;
    }

/**
 * Adds a new patient's details to the patient file.
 *
 * @param userID     The unique user ID.
 * @param password   The patient's password.
 * @param name       The patient's name.
 * @param dob        The patient's date of birth.
 * @param gender     The patient's gender.
 * @param email      The patient's email.
 * @param contact    The patient's contact number.
 * @param bloodGroup The patient's blood group.
 */
    private void addNewPatientToFile(int userID, String password, String name, String dob, String gender, String email, String contact, String bloodGroup) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PATIENT_FILE, true))) {
            writer.write(userID + "," + password + ",Patient," + name + "," + dob + "," + gender + "," + email + "," + contact + "," + bloodGroup);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing new patient data: " + e.getMessage());
        }
    }
    


/**
 * Views available appointment slots for a specific doctor on a given date.
 *
 * @param date The date for which to view available slots.
 */
    public void viewAvailableSlots(LocalDate date) {
        System.out.println("=====================================================");
        System.out.println("| Doctor ID | Name                 | Specialization  |");
        System.out.println("=====================================================");

    for (Map.Entry<String, Doctor> entry : doctors.entrySet()) {
        Doctor doctor = entry.getValue();
        System.out.printf("| %-9s | %-20s | %-16s |%n", 
                          doctor.getDoctorID(), 
                          doctor.getName(), 
                          doctor.getSpecialization());
    }

    System.out.println("=====================================================");
        System.out.print("Enter the Doctor ID to view available time slots: ");
        String doctorID = sc.nextLine();

        Doctor doctor = doctors.get(doctorID);
        if (doctor != null) {
            doctor.ensureDailySlotsInitialized(date); // Ensure slots are initialized
            doctor.loadUnavailableSlots(date); // Load unavailable slots from file
            doctor.showAvailableTimeSlots();
        } else {
            System.out.println("Doctor with ID " + doctorID + " not found.");
        }
    }

/**
 * Schedules an appointment for a patient with a specified doctor, date, and time.
 *
 * @param patient   The patient object.
 * @param doctorID  The doctor's ID.
 * @param date      The date of the appointment.
 * @param startTime The start time of the appointment.
 */
    public void scheduleAppointment(Patient patient, String doctorID, LocalDate date, LocalTime startTime) {
        Doctor doctor = doctors.get(doctorID);
        if (doctor != null) {
            String appointmentID = "A" + System.currentTimeMillis();
            Appointment appointment = appointmentManager.scheduleAppointment(appointmentID, patient.getUserID(), doctorID, date, startTime);
            if (appointment != null){
                System.out.println("Appointment scheduled successfully with Dr. " + doctor.getName() + " on " + date + " at " + startTime);
            }
            else{
                System.out.println("Appointment cannot be scheduled with Dr. " + doctor.getName() + " on " + date + " at " + startTime);
            }
        } else {
            System.out.println("Doctor with ID " + doctorID + " not found.");
        }
    }

/**
 * Displays the Receptionist-specific menu options for user interaction.
 */
@Override
public void displayMenu() {
    System.out.println("=====================================");
    System.out.println("          Receptionist Portal        ");
    System.out.println("=====================================");
    System.out.println("1. Register New Patient");
    System.out.println("2. Generate Bill");
    System.out.println("3. View Available Slots");
    System.out.println("4. Logout");
    System.out.println("=====================================");
}

}
