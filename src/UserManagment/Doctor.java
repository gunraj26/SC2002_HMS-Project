package UserManagment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException; // Added import
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import AppointmentManagement.Appointment;
import AppointmentManagement.AppointmentManager;
import AppointmentManagement.TimeSlot;
import AppointmentManagement.TimeSlot.SlotStatus;
import MedicalRecords.MedicalRecord;

/**
 * Represents a Doctor user with capabilities to manage appointments and patient records.
 */
public class Doctor extends Staff {
    private String specialization;
    private AppointmentManager appointmentManager;
    private List<TimeSlot> availableTimeSlots; // Declare without initialization

    /**
     * Constructor with AppointmentManager.
     *
     * @param userID             The unique user ID.
     * @param password           The password.
     * @param role               The role (should be "Doctor").
     * @param name               The doctor's name.
     * @param email              The doctor's email.
     * @param contactNumber      The doctor's contact number.
     * @param staffID            The doctor's staff ID.
     * @param specialization     The doctor's specialization.
     * @param appointmentManager The AppointmentManager object.
     */
    public Doctor(int userID, String password, String role, String name, String email, String contactNumber, String staffID, String specialization, AppointmentManager appointmentManager) {
        super(userID, password, role, name, email, contactNumber, staffID);
        this.specialization = specialization;
        this.appointmentManager = appointmentManager;
        this.availableTimeSlots = new ArrayList<>();
        // Removed the incorrect MedicalRecord instantiation
    }

   /**
 * Constructor without AppointmentManager.
 *
 * @param userID         The unique user ID.
 * @param password       The password.
 * @param role           The role (should be "Doctor").
 * @param name           The doctor's name.
 * @param email          The doctor's email.
 * @param contactNumber  The doctor's contact number.
 * @param staffID        The doctor's staff ID.
 * @param specialization The doctor's specialization.
 */
    public Doctor(int userID, String password, String role, String name, String email, String contactNumber, String staffID, String specialization) {
        super(userID, password, role, name, email, contactNumber, staffID);
        this.specialization = specialization;
        this.availableTimeSlots = new ArrayList<>();
        // Removed the incorrect MedicalRecord instantiation
    }

   /**
 * Ensures that daily slots are initialized for the given date.
 *
 * @param date The date for which to initialize slots.
 */
    public void ensureDailySlotsInitialized(LocalDate date) {
        if (availableTimeSlots == null || availableTimeSlots.isEmpty() ||
                (availableTimeSlots.size() > 0 && !availableTimeSlots.get(0).getDate().equals(date))) {
            initializeDailySlots(date);
        }
    }

   /**
 * Initializes time slots for a specific date.
 *
 * @param date The date for which to initialize slots.
 */
    public void initializeDailySlots(LocalDate date) {
        // Check if slots are already initialized for the date to avoid resetting
        if (availableTimeSlots.isEmpty() || !availableTimeSlots.get(0).getDate().equals(date)) {
            availableTimeSlots = TimeSlot.generateDailySlots(date); // Initialize only if empty or for a new date
        }
    }

    /**
 * Sets a specific time slot as unavailable.
 *
 * @param date      The date of the slot.
 * @param startTime The start time of the slot.
 */
    public void setSlotUnavailable(LocalDate date, LocalTime startTime) {
        ensureDailySlotsInitialized(date); // Ensure slots are initialized

        if (availableTimeSlots == null) {
            System.out.println("No available time slots to process.");
            return; // Avoid processing if slots are not initialized
        }

        for (TimeSlot slot : availableTimeSlots) {
            if (slot.getDate().equals(date) && slot.getStartTime().equals(startTime)) {
                slot.setStatus(TimeSlot.SlotStatus.UNAVAILABLE);
                System.out.println("Slot set to unavailable: " + slot);
                saveUnavailableSlots(); // Save the updated slot status to file
                return;
            }
        }
        System.out.println("No matching slot found for date and start time.");
    }

    /**
     * Saves unavailable slots to a file.
     */
    public void saveUnavailableSlots() {
        // Check if availableTimeSlots is initialized before accessing
        if (availableTimeSlots != null && !availableTimeSlots.isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("unavailable_slots.txt", true))) {
                for (TimeSlot slot : availableTimeSlots) {
                    if (slot.getStatus() == SlotStatus.UNAVAILABLE) {
                        writer.write(this.getDoctorID() + "," + slot.getDate() + "," + slot.getStartTime() + ",UNAVAILABLE");
                        writer.newLine();
                    }
                }
            } catch (IOException e) {
                System.out.println("Error saving unavailable slots: " + e.getMessage());
            }
        } else {
            System.out.println("No available time slots to save.");
        }
    }

    /**
     * Loads unavailable slots from a file.
     *
     * @param date The date for which to load unavailable slots.
     */
    public void loadUnavailableSlots(LocalDate date) {
        try (BufferedReader reader = new BufferedReader(new FileReader("unavailable_slots.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length == 4 &&
                        details[0].equalsIgnoreCase(this.getDoctorID()) &&
                        LocalDate.parse(details[1]).equals(date)) {
                    LocalTime startTime = LocalTime.parse(details[2]);
                    for (TimeSlot slot : availableTimeSlots) {
                        if (slot.getDate().equals(date) && slot.getStartTime().equals(startTime)) {
                            slot.setStatus(SlotStatus.UNAVAILABLE);
                        }
                    }
                }
            }
        } catch (IOException | DateTimeParseException e) {
            System.out.println("Error loading unavailable slots: " + e.getMessage());
        }
    }

    /**
     * Views all completed appointments.
     */
    public boolean viewCompletedAppointments() {
        List<Appointment> completedAppointments = new ArrayList<>();

        // Fetch appointments by doctor ID
        List<Appointment> doctorAppointments = appointmentManager.getAppointmentsByDoctorID(getDoctorID());

        // Filter for completed appointments
        for (Appointment appointment : doctorAppointments) {
            if ("Completed".equalsIgnoreCase(appointment.getStatus())) {
                completedAppointments.add(appointment);
            }
        }

        // Display completed appointments
        if (completedAppointments.isEmpty()) {
            System.out.println("No completed appointments.");
            return false;
        } else {
            System.out.println("Completed Appointments:");
            for (Appointment appointment : completedAppointments) {
                appointment.viewAppointmentDetails();
            }
        }
        return true;
    }

    /**
     * Views all confirmed appointments.
     */
    public void viewConfirmedAppointments() {
        List<Appointment> confirmedAppointments = new ArrayList<>();

        // Fetch appointments by doctor ID
        List<Appointment> doctorAppointments = appointmentManager.getAppointmentsByDoctorID(getDoctorID());

        // Filter for confirmed appointments
        for (Appointment appointment : doctorAppointments) {
            if ("Confirmed".equalsIgnoreCase(appointment.getStatus())) {
                confirmedAppointments.add(appointment);
            }
        }

        // Display confirmed appointments
        if (confirmedAppointments.isEmpty()) {
            System.out.println("No confirmed appointments.");
        } else {
            System.out.println("Confirmed Appointments:");
            for (Appointment appointment : confirmedAppointments) {
                appointment.viewAppointmentDetails();
            }
        }
    }

    /**
     * Views all upcoming appointments.
     */
    public void viewUpcomingAppointments() {
        // Reload appointments to ensure we have the latest data
        appointmentManager.reloadAppointments();

        // Fetch upcoming appointments for this doctor
        List<Appointment> upcomingAppointments = appointmentManager.getUpcomingAppointmentsByDoctor(getDoctorID());

        if (upcomingAppointments.isEmpty()) {
            System.out.println("There are no upcoming appointments.");
        } else {
            System.out.println("Upcoming Appointments:");
            for (Appointment appointment : upcomingAppointments) {
                appointment.viewAppointmentDetails();
            }
        }
    }

    /**
     * Responds to an appointment request by accepting or rejecting it.
     *
     * @param appointmentID The ID of the appointment.
     * @param accept        True to accept, False to reject.
     */
    public void respondToAppointmentRequest(String appointmentID, boolean accept) {
        // Use the existing appointmentManager instance
        if (accept) {
            appointmentManager.updateStatusInFile(appointmentID, "Confirmed");
            System.out.println("Appointment confirmed.");
        } else {
            appointmentManager.updateStatusInFile(appointmentID, "Cancelled");
            System.out.println("Appointment cancelled.");
        }
    }

    /**
     * Shows available time slots for the doctor.
     */
    public void showAvailableTimeSlots() {
        if (availableTimeSlots.isEmpty()) {
            System.out.println("No available time slots.");
            return;
        }
    
        System.out.println("Available Time Slots for Doctor " + this.getName() + " on " + availableTimeSlots.get(0).getDate() + ":");
        System.out.println("====================================================");
        System.out.println("| Date       | Start Time | End Time | Status       |");
        System.out.println("====================================================");
    
        for (TimeSlot slot : availableTimeSlots) {
            if (slot.getStatus() == SlotStatus.AVAILABLE) {
                System.out.printf("| %-10s | %-10s | %-8s | %-12s |%n", 
                                  slot.getDate(), 
                                  slot.getStartTime(), 
                                  slot.getEndTime(), 
                                  slot.getStatus());
            }
        }
    
        System.out.println("====================================================");
    }
    

    /**
     * Views a patient's medical record.
     *
     * @param patient The Patient object.
     */
    public void viewPatientMedicalRecord(Patient patient) {
        if (patient.getDob() != null)
            patient.viewMedicalRecord(); // Dependency on Patient class for accessing medical records
        else
            System.out.println("Patient not found");
    }

    /**
     * Updates a patient's medical record.
     *
     * @param patient        The Patient object.
     * @param appointmentID  The appointment ID to update.
     */
    public void updatePatientMedicalRecord(Patient patient, String appointmentID) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter diagnosis: ");
        String diagnosis = scanner.nextLine();
    
        System.out.print("Enter treatment plan: ");
        String treatmentPlan = scanner.nextLine();
    
        String medicalRecordFile = "medicalrecord.txt";
        String patientID = String.valueOf(patient.getUserID());
        boolean entryFound = false;
    
        List<String> fileContent = new ArrayList<>();
    
        // Read the file and check for the entry
        try (BufferedReader reader = new BufferedReader(new FileReader(medicalRecordFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] recordParts = line.split(","); // Assuming CSV format
                if (recordParts.length >= 4 && recordParts[0].equals(patientID) && recordParts[1].equals(appointmentID)) {
                    // Found the entry, update it
                    String updatedRecord = patientID + "," + appointmentID + "," + diagnosis + "," + treatmentPlan;
                    fileContent.add(updatedRecord);
                    entryFound = true;
                } else {
                    // Keep the original record
                    fileContent.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading medical record file: " + e.getMessage());
            return;
        }
    
        // If no entry was found, add the new entry
        if (!entryFound) {
            String newRecord = patientID + "," + appointmentID + "," + diagnosis + "," + treatmentPlan;
            fileContent.add(newRecord);
            System.out.println("No existing record found. Adding a new record for Patient ID: " + patientID);
        }
    
        // Write the updated content back to the file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(medicalRecordFile))) {
            for (String record : fileContent) {
                writer.write(record);
                writer.newLine();
            }
            System.out.println("Medical record successfully updated/added for Appointment ID: " + appointmentID);
        } catch (IOException e) {
            System.out.println("Error writing to medical record file: " + e.getMessage());
        }
    }
    
    

    /**
     * Records the outcome of a completed appointment.
     *
     * @param appointmentID The ID of the appointment.
     */
    public void recordAppointmentOutcome(String appointmentID) {
        appointmentManager.reloadAppointments();

        Appointment appointment = appointmentManager.getAppointments().get(appointmentID);
        if (appointment == null) {
            System.out.println("Appointment ID not found.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter service type: ");
        String serviceType = scanner.nextLine();

        System.out.print("Enter consultation notes: ");
        String consultationNotes = scanner.nextLine();

        List<String> prescribedMedicines = new ArrayList<>();
        List<Integer> medicineQuantities = new ArrayList<>();
        int medicineCount = 1;
        while (true) {
            System.out.print("Enter prescribed medicine " + medicineCount + " (or enter -1 to finish): ");
            String medicine = scanner.nextLine();
            if (medicine.equals("-1")) break;
            prescribedMedicines.add(medicine);

            System.out.print("Enter quantity for " + medicine + ": ");
            int quantity = 0;
            try {
                quantity = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid quantity. Setting quantity to 1.");
                quantity = 1;
            }
            medicineQuantities.add(quantity);
            medicineCount++;
        }

        System.out.print("Enter prescribed medicine status: ");
        String prescribedMedicineStatus = scanner.nextLine();

        appointment.setOutcomeRecord(serviceType, consultationNotes, prescribedMedicines, medicineQuantities, prescribedMedicineStatus);
        appointmentManager.updateAppointmentInFile(appointment);
    }

    /**
     * Gets the doctor's unique ID.
     *
     * @return The doctor's staff ID.
     */
    public String getDoctorID() {
        return staffID;
    }

    /**
     * Gets the doctor's specialization.
     *
     * @return The specialization string.
     */
    public String getSpecialization() {
        return specialization;
    }

    /**
     * Gets the list of available time slots.
     *
     * @return The list of TimeSlot objects.
     */
    public List<TimeSlot> getAvailableTimeSlots() {
        return availableTimeSlots;
    }

    /**
     * Displays the Doctor menu.
     */
    @Override
    public void displayMenu() {
        System.out.println("=====================================");
        System.out.println("            Doctor Portal            ");
        System.out.println("=====================================");
        System.out.println("1. View Patient Medical Records");
        System.out.println("2. Update Patient Medical Records");
        System.out.println("3. View Personal Schedule");
        System.out.println("4. Set Unavailability for Appointments");
        System.out.println("5. Respond to Appointment Requests");
        System.out.println("6. View Upcoming Appointments");
        System.out.println("7. Record Appointment Outcome");
        System.out.println("8. View Completed Appointments");
        System.out.println("9. View Confirmed Appointments");
        System.out.println("10. Change Password");
        System.out.println("11. Logout");
        System.out.println("=====================================");
    }
}
