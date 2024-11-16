package MedicalRecords;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import UserManagment.Patient;

/**
 * Represents the medical records of a patient.
 * Handles loading, viewing, adding, and updating medical records.
 */
public class MedicalRecord {
    private Patient patient;
    private List<MedicalRecordEntry> pastDiagnosesAndTreatments;
    private static final String MEDICAL_RECORD_FILE = "medicalrecord.txt";

    /**
     * Constructor for a new patient. Initializes the medical record.
     *
     * @param patient The patient whose medical record is being managed.
     */
    public MedicalRecord(Patient patient) {
        this.patient = patient;
        this.pastDiagnosesAndTreatments = new ArrayList<>();
        loadMedicalRecords(); // Load past records from file upon initialization
    }

    /**
     * Nested class representing a single medical record entry.
     */
    private static class MedicalRecordEntry {
        String appointmentID;
        String diagnosis;
        String treatmentPlan;
        
        /**
         * Constructor for a medical record entry.
         *
         * @param appointmentID The ID of the appointment.
         * @param diagnosis     The diagnosis details.
         * @param treatmentPlan The treatment plan details.
         */
        MedicalRecordEntry(String appointmentID, String diagnosis, String treatmentPlan) {
            this.appointmentID = appointmentID;
            this.diagnosis = diagnosis;
            this.treatmentPlan = treatmentPlan;
        }

        /**
         * Returns a string representation of the medical record entry.
         *
         * @return A formatted string of the entry.
         */
        @Override
        public String toString() {
            return "Appointment ID: " + appointmentID + "\nDiagnosis: " + diagnosis + "\nTreatment Plan: " + treatmentPlan + "\n-----";
        }
    }

    /**
     * Adds a new medical record entry for an appointment.
     *
     * @param appointmentID  The ID of the appointment.
     * @param diagnosis      The diagnosis provided.
     * @param treatmentPlan  The treatment plan prescribed.
     */
    public void addMedicalRecordEntry(String appointmentID, String diagnosis, String treatmentPlan) {
        // Check if an entry with the same appointmentID already exists
        MedicalRecordEntry existingEntry = findEntryByAppointmentID(appointmentID);
        if (existingEntry != null) {
            System.out.println("An entry for Appointment ID " + appointmentID + " already exists. Use updateMedicalRecordEntry to modify it.");
            return;
        }

        MedicalRecordEntry newEntry = new MedicalRecordEntry(appointmentID, diagnosis, treatmentPlan);
        pastDiagnosesAndTreatments.add(newEntry);
        saveMedicalRecordEntryToFile(newEntry);
        //System.out.println("Medical record entry added successfully for Appointment ID: " + appointmentID);
    }

    /**
     * Updates an existing medical record entry for an appointment.
     *
     * @param appointmentID  The ID of the appointment to update.
     * @param diagnosis      The new diagnosis.
     * @param treatmentPlan  The new treatment plan.
     * @return True if the update was successful; False if the entry was not found.
     */
    public boolean updateMedicalRecordEntry(String appointmentID, String diagnosis, String treatmentPlan) {
        MedicalRecordEntry existingEntry = findEntryByAppointmentID(appointmentID);
        if (existingEntry != null) {
            existingEntry.diagnosis = diagnosis;
            existingEntry.treatmentPlan = treatmentPlan;
            saveAllMedicalRecordsToFile();
            System.out.println("Medical record entry updated successfully for Appointment ID: " + appointmentID);
            return true;
        } else {
            System.out.println("No medical record entry found for Appointment ID: " + appointmentID);
            return false;
        }
    }

    /**
     * Finds a medical record entry by appointment ID.
     *
     * @param appointmentID The appointment ID to search for.
     * @return The MedicalRecordEntry if found; otherwise, null.
     */
    private MedicalRecordEntry findEntryByAppointmentID(String appointmentID) {
        for (MedicalRecordEntry entry : pastDiagnosesAndTreatments) {
            if (entry.appointmentID.equalsIgnoreCase(appointmentID)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Saves a single medical record entry to the file.
     *
     * @param entry The medical record entry to save.
     */
    private void saveMedicalRecordEntryToFile(MedicalRecordEntry entry) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MEDICAL_RECORD_FILE, true))) {
            writer.write(patient.getUserID() + "," + entry.appointmentID + "," + escapeCommas(entry.diagnosis) + "," + escapeCommas(entry.treatmentPlan));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving medical record entry: " + e.getMessage());
        }
    }

    /**
     * Escapes commas in strings to prevent CSV parsing issues.
     *
     * @param input The input string.
     * @return The escaped string.
     */
    private String escapeCommas(String input) {
        if (input.contains(",")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    /**
     * Loads all medical records for the patient from the file.
     */
    private void loadMedicalRecords() {
        File file = new File(MEDICAL_RECORD_FILE);
        if (!file.exists()) {
            // If the file doesn't exist, there's nothing to load.
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(MEDICAL_RECORD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Handle cases where diagnosis or treatment plan contain commas
                List<String> details = parseCSVLine(line);
                if (details.size() < 4) {
                    System.err.println("Skipping malformed line in medical record file: " + line);
                    continue;
                }

                int fileUserID;
                try {
                    fileUserID = Integer.parseInt(details.get(0).trim());
                } catch (NumberFormatException e) {
                    System.err.println("Invalid User ID in line: " + line);
                    continue;
                }

                if (fileUserID != patient.getUserID()) {
                    continue; // Skip records that don't belong to this patient
                }

                String appointmentID = details.get(1).trim();
                String diagnosis = details.get(2).trim();
                String treatmentPlan = details.get(3).trim();

                MedicalRecordEntry entry = new MedicalRecordEntry(appointmentID, diagnosis, treatmentPlan);
                pastDiagnosesAndTreatments.add(entry);
            }
        } catch (IOException e) {
            System.out.println("Error loading medical records: " + e.getMessage());
        }
    }

    /**
     * Parses a CSV line, handling commas within quoted strings.
     *
     * @param line The CSV line to parse.
     * @return A list of parsed fields.
     */
    private List<String> parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (c == '\"') {
                inQuotes = !inQuotes; // Toggle state
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0); // Reset the buffer
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim()); // Add the last field
        return result;
    }

    /**
     * Saves all medical records to the file, overwriting existing data.
     */
    private void saveAllMedicalRecordsToFile() {
        File tempFile = new File("medicalrecord_temp.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            for (MedicalRecordEntry entry : pastDiagnosesAndTreatments) {
                writer.write(patient.getUserID() + "," + entry.appointmentID + "," + escapeCommas(entry.diagnosis) + "," + escapeCommas(entry.treatmentPlan));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving medical records: " + e.getMessage());
            return;
        }

        // Replace the original file with the temp file
        File originalFile = new File(MEDICAL_RECORD_FILE);
        if (originalFile.delete()) {
            if (tempFile.renameTo(originalFile)) {
                // Successfully replaced the original file
            } else {
                System.out.println("Error renaming temporary medical record file.");
            }
        } else {
            System.out.println("Error deleting original medical record file.");
        }
    }

    /**
     * Displays the medical record for the patient.
     */
    public void viewRecord() {
        System.out.println("=====================================");
        System.out.println("        Medical Record Summary       ");
        System.out.println("=====================================");
        System.out.println("Patient ID: " + patient.getUserID());
        System.out.println("Name: " + patient.getName());
        System.out.println("Date of Birth: " + patient.getDob());
        System.out.println("Gender: " + patient.getGender());
        System.out.println("Contact: " + patient.getContactNumber());
        System.out.println("Email: " + patient.getEmail());
        System.out.println("Blood Type: " + patient.getBloodType());
        System.out.println("-------------------------------------");

        if (pastDiagnosesAndTreatments.isEmpty()) {
            System.out.println("No past diagnoses or treatments available.");
        } else {
            System.out.println("Past Diagnoses and Treatments:");
            for (MedicalRecordEntry entry : pastDiagnosesAndTreatments) {
                System.out.println(entry.toString());
            }
        }
        System.out.println("=====================================");
    }
}
