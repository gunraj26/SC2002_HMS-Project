// AppointmentManager.java
package AppointmentManagement;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import AppointmentManagement.TimeSlot.SlotStatus;
import UserManagment.Doctor; // Corrected package name to match Doctor.java

/**
 * Manages all appointment-related operations including scheduling, updating, and retrieving appointments.
 */
public class AppointmentManager {
    private Map<String, Appointment> appointments = new HashMap<>();
    private Map<String, Doctor> doctors; // This will hold doctor information

    /**
     * Constructor to initialize the AppointmentManager with a map of doctors.
     *
     * @param doctors A map containing Doctor objects with their IDs as keys.
     */
    public AppointmentManager(Map<String, Doctor> doctors) {
        this.doctors = doctors; // Initialize the doctors map
        reloadAppointments();
    }

    /**
     * Retrieves the current map of appointments.
     *
     * @return A map of appointment IDs to Appointment objects.
     */
    public Map<String, Appointment> getAppointments() {
        return appointments;
    }

    /**
     * Schedules a new appointment.
     *
     * @param appointmentID Unique identifier for the appointment.
     * @param patientID     Unique identifier for the patient.
     * @param doctorID      Unique identifier for the doctor.
     * @param date          Date of the appointment.
     * @param time          Time of the appointment.
     * @return The newly created Appointment object or null if the slot is unavailable.
     */
    public Appointment scheduleAppointment(String appointmentID, int patientID, String doctorID, LocalDate date, LocalTime time) {
        if (!isTimeSlotAvailable(doctorID, date, time)) {
            System.out.println("The selected time slot is already booked. Please choose a different time.");
            return null; // Return null or handle as needed
        }
        else{
            Appointment appointment = new Appointment(appointmentID, patientID, doctorID, date, time, this);
            appointment.saveAppointmentToFile(); // Update the appointments.txt file
            appointments.put(appointmentID, appointment);
            return appointment;
        }
    }
    

    /**
     * Updates an existing appointment in the file.
     *
     * @param appointment The Appointment object with updated details.
     */
    public synchronized void updateAppointmentInFile(Appointment appointment) {
        File inputFile = new File(Appointment.APPOINTMENT_FILE);
        File tempFile = new File("appointments_temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean found = false;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details[0].equals(appointment.getAppointmentID())) {
                    // Reconstruct the appointment line with updated details
                    writer.write(appointment.getAppointmentID() + "," + appointment.getPatientID() + "," +
                            appointment.getDoctorID() + "," + appointment.getAppointmentDate() + "," +
                            appointment.getAppointmentTime() + "," + appointment.getStatus());

                    if (appointment.getStatus().equalsIgnoreCase("Completed")) {
                        writer.write("," + appointment.getServiceType() + "," +
                                appointment.getConsultationNotes() + "," +
                                String.join(";", appointment.getPrescribedMedicines()) + "," +
                                String.join(";", appointment.getPrescribedMedicineQuantities().stream()
                                        .map(String::valueOf).toArray(String[]::new)) + "," +
                                appointment.getPrescribedMedicineStatus());
                    }
                    found = true;
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }

            if (!found) {
                // If the appointment wasn't found, append it as a new entry
                writer.write(appointment.getAppointmentID() + "," + appointment.getPatientID() + "," +
                        appointment.getDoctorID() + "," + appointment.getAppointmentDate() + "," +
                        appointment.getAppointmentTime() + "," + appointment.getStatus());

                if (appointment.getStatus().equalsIgnoreCase("Completed")) {
                    writer.write("," + appointment.getServiceType() + "," +
                            appointment.getConsultationNotes() + "," +
                            String.join(";", appointment.getPrescribedMedicines()) + "," +
                            String.join(";", appointment.getPrescribedMedicineQuantities().stream()
                                    .map(String::valueOf).toArray(String[]::new)) + "," +
                            appointment.getPrescribedMedicineStatus());
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error updating appointment: " + e.getMessage());
        }

        // Replace the original file with the updated temp file
        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            System.out.println("Error replacing appointment file.");
        }
    }

    /**
     * Removes an appointment from the file based on its ID.
     *
     * @param appointmentID The ID of the appointment to remove.
     */
    public synchronized void removeAppointmentFromFile(String appointmentID) {
        File inputFile = new File(Appointment.APPOINTMENT_FILE);
        File tempFile = new File("appointments_temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (!details[0].equals(appointmentID)) {
                    writer.write(line); // Retain appointments that don't match the ID
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error removing appointment: " + e.getMessage());
        }

        // Replace the original file with the updated temp file
        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            System.out.println("Error replacing appointment file.");
        }
    }

    /**
     * Updates the status of an appointment and handles associated slot availability.
     *
     * @param appointmentID The ID of the appointment to update.
     * @param newStatus     The new status to set (e.g., "Confirmed", "Cancelled").
     */
    public synchronized void updateStatusInFile(String appointmentID, String newStatus) {
        File inputFile = new File(Appointment.APPOINTMENT_FILE);
        File tempFile = new File("appointments_temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            LocalDate appointmentDate = null;
            LocalTime appointmentTime = null;
            String doctorID = null;

            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details[0].equals(appointmentID)) {
                    // Extract appointment details
                    appointmentDate = LocalDate.parse(details[3]);
                    appointmentTime = LocalTime.parse(details[4]);
                    doctorID = details[2];
                    details[5] = newStatus; // Update the status

                    // Reconstruct the appointment line with updated status
                    line = String.join(",", details);

                    // If the status is confirmed, mark the slot as unavailable
                    if (newStatus.equalsIgnoreCase("Confirmed") && doctorID != null) {
                        Doctor doctor = doctors.get(doctorID);
                        if (doctor != null) {
                            doctor.setSlotUnavailable(appointmentDate, appointmentTime);
                        }
                    }
                }
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error updating appointment status: " + e.getMessage());
        }

        // Replace the original file with the updated temp file
        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            System.out.println("Error replacing appointment file.");
        }
    }

    /**
     * Sets a specific time slot as unavailable for a doctor.
     *
     * @param doctorID The ID of the doctor.
     * @param date     The date of the slot.
     * @param time     The start time of the slot.
     */
    public synchronized void setSlotUnavailable(String doctorID, LocalDate date, LocalTime time) {
        // Retrieve the doctor from the map
        Doctor doctor = doctors.get(doctorID);
        if (doctor != null) {
            doctor.ensureDailySlotsInitialized(date); // Ensure slots are initialized
            for (TimeSlot slot : doctor.getAvailableTimeSlots()) {
                if (slot.getDate().equals(date) && slot.getStartTime().equals(time)) {
                    slot.setStatus(TimeSlot.SlotStatus.UNAVAILABLE);
                    doctor.saveUnavailableSlots(); // Persist the change
                    break;
                }
            }
        }
    }

    /**
     * Removes a time slot's unavailability status for a doctor.
     *
     * @param doctorID The ID of the doctor.
     * @param date     The date of the slot.
     * @param time     The start time of the slot.
     */
    public synchronized void removeUnavailableSlot(String doctorID, LocalDate date, LocalTime time) {
        File inputFile = new File("unavailable_slots.txt");
        File tempFile = new File("unavailable_slots_temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                // Retain lines that do not match the specified doctor, date, and time
                if (!(details[0].equals(doctorID) &&
                        LocalDate.parse(details[1]).equals(date) &&
                        LocalTime.parse(details[2]).equals(time))) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error removing unavailable slot: " + e.getMessage());
        }

        // Replace the original file with the updated temp file
        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            System.out.println("Error replacing unavailable slots file.");
        }
    }

   /**
     * Reloads all appointments from the appointments file into memory.
     */
    public synchronized void reloadAppointments() {
        appointments.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(Appointment.APPOINTMENT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length >= 6) {
                    String appointmentID = details[0];
                    int patientID = Integer.parseInt(details[1]);
                    String doctorID = details[2];
                    LocalDate date = LocalDate.parse(details[3]);
                    LocalTime time = LocalTime.parse(details[4]);
                    String status = details[5];

                    Appointment appointment = new Appointment(appointmentID, patientID, doctorID, date, time, this);
                    appointment.setStatus(status);

                    if (status.equalsIgnoreCase("Completed") && details.length >= 11) {
                        String serviceType = details[6];
                        String consultationNotes = details[7];
                        List<String> medicines = Arrays.asList(details[8].split(";"));
                        List<Integer> medicineQuantities = new ArrayList<>();
                        for (String quantity : details[9].split(";")) {
                            try {
                                medicineQuantities.add(Integer.parseInt(quantity.trim()));
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid medicine quantity format in appointment ID: " + appointmentID);
                                medicineQuantities.add(0); // Default value or handle accordingly
                            }
                        }
                        String medicineStatus = details[10];

                        // Ensure all diagnosis details are present
                        if (serviceType != null && consultationNotes != null && !medicines.isEmpty()) {
                            appointment.setOutcomeRecord(serviceType, consultationNotes, medicines, medicineQuantities, medicineStatus);
                        } else {
                            System.out.println("Incomplete diagnosis details for appointment ID: " + appointmentID + ". Marking as not completed.");
                            appointment.setStatus("Scheduled"); // Revert status
                        }
                    }
                    appointments.put(appointmentID, appointment);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reloading appointments: " + e.getMessage());
        }
    }

    /**
     * Retrieves a list of upcoming appointments for a specific doctor.
     *
     * @param doctorID The ID of the doctor.
     * @return A list of upcoming Appointment objects.
     */
    public List<Appointment> getUpcomingAppointmentsByDoctor(String doctorID) {
        List<Appointment> upcomingAppointments = new ArrayList<>();

        for (Appointment appointment : appointments.values()) {
            if (appointment.getDoctorID().equals(doctorID) &&
                    (appointment.getStatus().equalsIgnoreCase("Confirmed") ||
                            appointment.getStatus().equalsIgnoreCase("Scheduled"))) {
                upcomingAppointments.add(appointment);
            }
        }
        return upcomingAppointments;
    }

    /**
     * Updates the prescription status of a specific appointment.
     *
     * @param appointmentID The ID of the appointment.
     * @param newStatus     The new prescription status to set.
     */
    public synchronized void updatePrescriptionStatus(String appointmentID, String newStatus) {
        File inputFile = new File(Appointment.APPOINTMENT_FILE);
        File tempFile = new File("appointments_temp.txt");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details[0].equals(appointmentID)) {
                    // Update the prescribed medicine status
                    if (details.length >= 11) {
                        details[10] = newStatus;
                    }
                    writer.write(String.join(",", details));
                } else {
                    writer.write(line);
                }
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error updating prescription status in file: " + e.getMessage());
        }

        // Replace the original file with the updated temp file
        if (!inputFile.delete() || !tempFile.renameTo(inputFile)) {
            System.out.println("Error replacing appointments file.");
        }
    }

    /**
     * Retrieves a list of appointments for a specific patient.
     *
     * @param patientID The ID of the patient.
     * @return A list of Appointment objects associated with the patient.
     */
    public List<Appointment> getAppointmentsByPatientID(int patientID) {
        // Ensure appointments are reloaded to maintain data consistency
        reloadAppointments();

        List<Appointment> patientAppointments = new ArrayList<>();

        for (Appointment appointment : appointments.values()) {
            if (appointment.getPatientID() == patientID) {
                patientAppointments.add(appointment);
            }
        }

        return patientAppointments;
    }

    /**
     * Retrieves a list of appointments for a specific doctor.
     *
     * @param doctorID The ID of the doctor.
     * @return A list of Appointment objects associated with the doctor.
     */
    public List<Appointment> getAppointmentsByDoctorID(String doctorID) {
        reloadAppointments();
        List<Appointment> doctorAppointments = new ArrayList<>();

        for (Appointment appointment : appointments.values()) {
            if (appointment.getDoctorID().equalsIgnoreCase(doctorID)) {
                doctorAppointments.add(appointment);
            }
        }

        return doctorAppointments;
    }

    /**
     * Checks if a specific time slot is available for a doctor.
     *
     * @param doctorID The ID of the doctor.
     * @param date     The date of the appointment.
     * @param time     The time of the appointment.
     * @return True if the time slot is available; otherwise, false.
     */
    public boolean isTimeSlotAvailable(String doctorID, LocalDate date, LocalTime time) {
        List<Appointment> doctorAppointments = getAppointmentsByDoctorID(doctorID);
        for (Appointment appt : doctorAppointments) {
            if (appt.getAppointmentDate().equals(date) && appt.getAppointmentTime().equals(time) &&
                    (appt.getStatus().equalsIgnoreCase("Scheduled") || appt.getStatus().equalsIgnoreCase("Confirmed"))) {
                return false; // Slot is already booked
            }
        }
        return true; // Slot is available
    }
}
