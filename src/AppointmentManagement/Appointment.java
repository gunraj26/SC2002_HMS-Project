// Appointment.java
package AppointmentManagement;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an appointment in the system, storing details such as date, time, status, and prescribed medicines.
 * Provides methods for managing the lifecycle of appointments, including saving, rescheduling, canceling, 
 * and recording outcomes.
 */
public class Appointment {
    private String appointmentID;
    private int patientID;
    private String doctorID;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;  // Status can be "Scheduled", "Confirmed", "Rescheduled", "Cancelled", "Completed"
    private String serviceType;
    private String consultationNotes;
    private List<String> prescribedMedicines;
    private List<Integer> prescribedMedicineQuantities;
    private String prescribedMedicineStatus;
    public static final String APPOINTMENT_FILE = "appointments.txt";
    private AppointmentManager appointmentManager;

    /**
     * Constructs an Appointment with an AppointmentManager instance.
     *
     * @param appointmentID The unique ID for the appointment.
     * @param patientID The ID of the patient.
     * @param doctorID The ID of the doctor.
     * @param appointmentDate The date of the appointment.
     * @param appointmentTime The time of the appointment.
     * @param appointmentManager The AppointmentManager instance for managing appointments.
     */
    public Appointment(String appointmentID, int patientID, String doctorID, LocalDate appointmentDate, LocalTime appointmentTime, AppointmentManager appointmentManager) {
        this.appointmentID = appointmentID;
        this.patientID = patientID;
        this.doctorID = doctorID;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = "Scheduled";
        this.prescribedMedicines = new ArrayList<>();
        this.prescribedMedicineQuantities = new ArrayList<>();
        this.appointmentManager = appointmentManager;
    }

    /**
     * Constructs an Appointment without an AppointmentManager instance.
     *
     * @param appointmentID The unique ID for the appointment.
     * @param patientID The ID of the patient.
     * @param doctorID The ID of the doctor.
     * @param appointmentDate The date of the appointment.
     * @param appointmentTime The time of the appointment.
     */
    public Appointment(String appointmentID, int patientID, String doctorID, LocalDate appointmentDate, LocalTime appointmentTime) {
        this.appointmentID = appointmentID;
        this.patientID = patientID;
        this.doctorID = doctorID;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = "Scheduled";
        this.prescribedMedicines = new ArrayList<>();
        this.prescribedMedicineQuantities = new ArrayList<>();
    }

    /**
     * Saves the appointment details to a file.
     */
    public void saveAppointmentToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(APPOINTMENT_FILE, true))) {
            writer.write(appointmentID + "," + patientID + "," + doctorID + "," + appointmentDate + "," + appointmentTime + "," + status);
            if (status.equals("Completed")) {
                writer.write("," + serviceType + "," + consultationNotes + "," + String.join(";", prescribedMedicines) + "," + String.join(";", prescribedMedicineQuantities.stream().map(String::valueOf).toArray(String[]::new)) + "," + prescribedMedicineStatus);
            }
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving appointment: " + e.getMessage());
        }
    }
    /**
     * Updates the status of an appointment and reloads data.
     *
     * @param appointmentID The unique ID of the appointment to update.
     * @param newStatus The new status for the appointment.
     */
    public void updateAppointmentStatus(String appointmentID, String newStatus) {
        this.appointmentManager.updateStatusInFile(appointmentID, newStatus);
        this.appointmentManager.reloadAppointments(); // Ensure updated data is reloaded
    }

    /**
     * Displays the details of the appointment.
     */
    public void viewAppointmentDetails() {
        System.out.println("Appointment ID: " + appointmentID);
        System.out.println("Patient ID: " + patientID);
        System.out.println("Doctor ID: " + doctorID);
        System.out.println("Date: " + appointmentDate);
        System.out.println("Time: " + appointmentTime);
        System.out.println("Status: " + status);

        if (status.equals("Completed")) {
            System.out.println("Service Provided: " + (serviceType != null ? serviceType : "No service type recorded"));
            System.out.println("Consultation Notes: " + (consultationNotes != null ? consultationNotes : "No consultation notes available"));

            if (prescribedMedicines != null && !prescribedMedicines.isEmpty()) {
                System.out.println("Prescribed Medicines:");
                for (int i = 0; i < prescribedMedicines.size(); i++) {
                    System.out.println("- " + prescribedMedicines.get(i) + ": Quantity " + prescribedMedicineQuantities.get(i));
                }
            } else {
                System.out.println("No medicines prescribed.");
            }

            System.out.println("Medicine Status: " + (prescribedMedicineStatus != null ? prescribedMedicineStatus : "No medicine status available"));
        }
        System.out.println("-----");
    }

    /**
     * Reschedules the appointment to a new date and time.
     *
     * @param newDate The new date for the appointment.
     * @param newTime The new time for the appointment.
     */
    public void rescheduleAppointment(LocalDate newDate, LocalTime newTime) {
        if (status.equals("Scheduled") || status.equals("Confirmed")) {
            // Validate new date
            LocalDate minDate = LocalDate.of(2024, 11, 20);
            if (newDate.isBefore(minDate)) {
                System.out.println("Appointments can only be rescheduled on or after " + minDate + ". Please choose a valid date.");
                return;
            }

            // Validate new time
            if (!isWithinOperatingHours(newTime)) {
                System.out.println("Invalid appointment time. Operating hours are from 09:00 to 17:00, excluding 13:00.");
                return;
            }

            // Check lunch time
            if (isLunchTime(newTime)) {
                System.out.println("Cannot reschedule appointments during lunch time (13:00).");
                return;
            }

            // Check if the new slot is available
            if (!appointmentManager.isTimeSlotAvailable(this.doctorID, newDate, newTime)) {
                System.out.println("The selected time slot is already booked or unavailable. Please choose a different time.");
                return;
            }

            // Remove the old slot from unavailable slots
            appointmentManager.removeUnavailableSlot(this.doctorID, this.appointmentDate, this.appointmentTime);

            // Update the appointment details
            this.appointmentDate = newDate;
            this.appointmentTime = newTime;
            this.status = "Scheduled"; // Reset status for rescheduling approval

            // Save the updated appointment to the file
            appointmentManager.updateAppointmentInFile(this);

            // Mark the new slot as unavailable
            //appointmentManager.setSlotUnavailable(this.doctorID, newDate, newTime);

            System.out.println("Appointment rescheduled to " + newDate + " at " + newTime);
        } else {
            System.out.println("Cannot reschedule as the appointment is " + status);
        }
    }

    /**
     * Cancels the appointment and updates the file.
     */
    public void cancelAppointment() {
        if (status.equals("Scheduled") || status.equals("Confirmed")) {
            this.status = "Cancelled"; // Mark as cancelled
            appointmentManager.updateAppointmentInFile(this); // Update status in appointments.txt

            // Make the slot available by removing it from unavailable slots
            appointmentManager.removeUnavailableSlot(this.doctorID, this.appointmentDate, this.appointmentTime);

            System.out.println("Appointment cancelled.");
        } else {
            System.out.println("Cannot cancel as the appointment is " + status);
        }
    }

    /**
     * Sets a new status for the appointment after validation.
     *
     * @param newStatus The new status for the appointment.
     */
    public void setStatus(String newStatus) {
        // Add checks to avoid invalid state transitions
        if (isValidStatusTransition(this.status, newStatus)) {
            this.status = newStatus;
            appointmentManager.updateAppointmentInFile(this);
        } else {
            //System.out.println("Invalid status transition from " + this.status + " to " + newStatus + ".");
        }
    }
    /**
     * Checks if a status transition is valid based on the current status.
     *
     * @param currentStatus The current status of the appointment.
     * @param newStatus The new status being transitioned to.
     * @return true if the status transition is valid, false otherwise.
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        switch (currentStatus) {
            case "Scheduled":
                return newStatus.equals("Confirmed") || newStatus.equals("Cancelled");
            case "Confirmed":
                return newStatus.equals("Completed") || newStatus.equals("Cancelled");
            case "Completed":
                return false; // No transitions from Completed
            case "Cancelled":
                return false; // No transitions from Cancelled
            default:
                return false;
        }
    }
    /**
     * Sets the prescribed medicine status for the appointment.
     *
     * @param status The new status for the prescribed medicines.
     */
    public void setPrescribedMedicineStatus(String status) {
        this.prescribedMedicineStatus = status;
    }

    /**
     * Records the outcome of the appointment after it is completed.
     *
     * @param serviceType The type of service provided during the appointment.
     * @param consultationNotes The notes recorded during the consultation.
     * @param medicines The list of prescribed medicines.
     * @param medicineQuantities The quantities of the prescribed medicines.
     * @param medicineStatus The status of the prescribed medicines.
     */
    public void setOutcomeRecord(String serviceType, String consultationNotes, List<String> medicines, List<Integer> medicineQuantities, String medicineStatus) {
        // Set outcome details regardless of the current status
        this.serviceType = serviceType;
        this.consultationNotes = consultationNotes;
        this.prescribedMedicines = medicines;
        this.prescribedMedicineQuantities = medicineQuantities;
        this.prescribedMedicineStatus = medicineStatus;

        // If the appointment was not already marked as completed, update it now
        if (!this.status.equals("Completed")) {
            this.status = "Completed";
            appointmentManager.updateAppointmentInFile(this); // Save to file if new completion
        }
    }

    /**
     * Checks if the provided time is during lunch hours.
     *
     * @param time The time to check.
     * @return true if the time is lunch time, false otherwise.
     */
    private boolean isLunchTime(LocalTime time) {
        return time.equals(LocalTime.of(13, 0));
    }
    /**
     * Checks if the provided time is within operating hours.
     *
     * @param time The time to check.
     * @return true if the time is within operating hours, false otherwise.
     */
    private boolean isWithinOperatingHours(LocalTime time) {
        LocalTime openingTime = LocalTime.of(9, 0);
        LocalTime closingTime = LocalTime.of(17, 0);

        // Exclude lunch time (13:00)
        if (isLunchTime(time)) {
            return false;
        }

        // Check if time is within operating hours
        return !time.isBefore(openingTime) && !time.isAfter(closingTime.minusMinutes(30)); // Assuming 30-minute appointments
    }

    // Getters and Setters
    /**
     * Retrieves the appointment ID.
     *
     * @return The appointment ID.
     */
    public String getAppointmentID() {
        return appointmentID;
    }
    /**
     * Retrieves the service type for the appointment.
     *
     * @return The service type.
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Retrieves the consultation notes for the appointment.
     *
     * @return The consultation notes.
     */
    public String getConsultationNotes() {
        return consultationNotes;
    }

    /**
     * Retrieves the list of prescribed medicines for the appointment.
     *
     * @return The list of prescribed medicines.
     */
    public List<String> getPrescribedMedicines() {
        return prescribedMedicines;
    }

    /**
     * Retrieves the status of the prescribed medicines.
     *
     * @return The status of the prescribed medicines.
     */
    public String getPrescribedMedicineStatus() {
        return prescribedMedicineStatus;
    }

    /**
     * Retrieves the quantities of prescribed medicines.
     *
     * @return The list of prescribed medicine quantities.
     */
    public List<Integer> getPrescribedMedicineQuantities() {
        return prescribedMedicineQuantities;
    }

    /**
     * Retrieves the patient ID associated with the appointment.
     *
     * @return The patient ID.
     */
    public int getPatientID() {
        return patientID;
    }

    /**
     * Retrieves the doctor ID associated with the appointment.
     *
     * @return The doctor ID.
     */
    public String getDoctorID() {
        return doctorID;
    }

    /**
     * Retrieves the appointment date.
     *
     * @return The appointment date.
     */
    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    /**
     * Retrieves the appointment time.
     *
     * @return The appointment time.
     */
    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    /**
     * Retrieves the current status of the appointment.
     *
     * @return The current status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Retrieves the outcome record for the appointment, if completed.
     *
     * @return The outcome record, or a message indicating no outcome if not completed.
     */
    public String getOutcomeRecord() {
        if (status.equals("Completed")) {
            return "Service Provided: " + serviceType + ", Consultation Notes: " + consultationNotes;
        } else {
            return "No outcome record available as the appointment is not completed.";
        }
    }
}
