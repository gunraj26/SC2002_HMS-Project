package InventoryManagement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import AppointmentManagement.Appointment;
import AppointmentManagement.AppointmentManager;
import UserManagment.Staff;

/**
 * Represents a Pharmacist who manages prescriptions, dispenses medicines,
 * handles inventory replenishment requests, and monitors stock levels.
 */
public class Pharmacist extends Staff {
    private Inventory inventory; // Direct dependency on Inventory
    private static final String REPLENISHMENT_REQUEST_FILE = "replenishment_requests.txt";

    /**
     * Constructs a Pharmacist object with the specified details.
     *
     * @param userID        Unique identifier for the pharmacist.
     * @param password      The password for login.
     * @param role          The role of the user (e.g., Pharmacist).
     * @param name          Name of the pharmacist.
     * @param email         Email of the pharmacist.
     * @param contactNumber Contact number of the pharmacist.
     * @param staffID       Staff ID of the pharmacist.
     * @param inventory     Reference to the inventory for medicine management.
     */
    public Pharmacist(int userID, String password, String role, String name, String email, String contactNumber, String staffID, Inventory inventory) {
        super(userID, password, role, name, email, contactNumber, staffID);
        this.inventory = inventory;
    }

    /**
     * Views prescriptions for a specific patient, filtering by completed appointments with prescribed medicines.
     *
     * @param patientID         The ID of the patient.
     * @param appointmentManager The AppointmentManager object for managing appointments.
     */
    public void viewPrescriptionsByPatientID(int patientID, AppointmentManager appointmentManager) {
        List<Appointment> appointments = appointmentManager.getAppointmentsByPatientID(patientID);
        System.out.println("Prescriptions for Patient ID " + patientID + ":");

        boolean prescriptionsFound = false;
        for (Appointment appointment : appointments) {
            // Check if the appointment has prescribed medicine status as "Prescribed"
            if (appointment.getStatus().equalsIgnoreCase("Completed") &&
                    "prescribed".equalsIgnoreCase(appointment.getPrescribedMedicineStatus())) {
                System.out.println("Appointment ID: " + appointment.getAppointmentID());
                System.out.println("Date: " + appointment.getAppointmentDate());
                System.out.println("Service Type: " + appointment.getServiceType());
                System.out.println("Consultation Notes: " + appointment.getConsultationNotes());
                System.out.println("Prescribed Medicines:");
                List<String> medicines = appointment.getPrescribedMedicines();
                List<Integer> quantities = appointment.getPrescribedMedicineQuantities();
                for (int i = 0; i < medicines.size(); i++) {
                    System.out.println("- " + medicines.get(i) + ": Quantity " + quantities.get(i));
                }
                System.out.println("Medicine Status: " + appointment.getPrescribedMedicineStatus());
                System.out.println("-----");
                prescriptionsFound = true;
            }
        }

        if (!prescriptionsFound) {
            System.out.println("No prescriptions found with status 'Prescribed' for Patient ID " + patientID);
        }
    }

    /**
     * Dispenses medicines for prescriptions of a specific patient, updating stock and prescription statuses.
     *
     * @param patientID         The ID of the patient.
     * @param appointmentManager The AppointmentManager object for managing appointments.
     */
    public void dispenseMedicines(int patientID, AppointmentManager appointmentManager) {
        List<Appointment> appointments = appointmentManager.getAppointmentsByPatientID(patientID);
        boolean canDispense = true;
        Medicine lowStockMedicine = null;

        // Check stock for each prescribed medicine
        for (Appointment appointment : appointments) {
            if (appointment.getStatus().equalsIgnoreCase("Completed") &&
                    "prescribed".equalsIgnoreCase(appointment.getPrescribedMedicineStatus())) {
                List<String> medicines = appointment.getPrescribedMedicines();
                List<Integer> quantities = appointment.getPrescribedMedicineQuantities();

                for (int i = 0; i < medicines.size(); i++) {
                    String medicineName = medicines.get(i);
                    int requiredQuantity = quantities.get(i);

                    // Find the medicine in the inventory by name
                    Medicine medicine = inventory.getMedicineByName(medicineName);

                    if (medicine == null) {
                        System.out.println("Medicine with name " + medicineName + " not found in inventory.");
                        canDispense = false;
                        break;
                    }

                    int currentStock = inventory.getStockLevel(medicine);
                    int threshold = inventory.getLowStockAlertLevel(medicine);

                    // Check if stock is below the threshold or insufficient for dispensing
                    if (currentStock < threshold || currentStock < requiredQuantity) {
                        canDispense = false;
                        lowStockMedicine = medicine;
                        System.out.println("Can't dispense medicines. LOW STOCK for " + lowStockMedicine.getName());
                        break; // Stop checking further if any medicine is below threshold
                    }
                }
            }
            if (!canDispense) {
                break; // Exit if any medicine has insufficient stock or is missing
            }
        }

        // Dispense medicines if all stocks are sufficient
        if (canDispense) {
            for (Appointment appointment : appointments) {
                if (appointment.getStatus().equalsIgnoreCase("Completed") &&
                        "prescribed".equalsIgnoreCase(appointment.getPrescribedMedicineStatus())) {
                    List<String> medicines = appointment.getPrescribedMedicines();
                    List<Integer> quantities = appointment.getPrescribedMedicineQuantities();

                    for (int i = 0; i < medicines.size(); i++) {
                        String medicineName = medicines.get(i);
                        int quantity = quantities.get(i);

                        // Update stock in inventory
                        Medicine medicine = inventory.getMedicineByName(medicineName);
                        if (medicine != null) { // Additional check to prevent null pointer
                            inventory.updateStock(medicine, -quantity); // Reduce stock by prescribed quantity
                        }
                    }

                    // Update prescription status to "Dispensed"
                    appointment.setPrescribedMedicineStatus("Dispensed");
                    appointmentManager.updateAppointmentInFile(appointment);
                }
            }
            System.out.println("Medicines dispensed and inventory updated successfully.");
        } else if (lowStockMedicine != null) {
            System.out.println("Can't dispense medicines. LOW STOCK for " + lowStockMedicine.getName());
        }
    }

    /**
     * Sends a replenishment request for a specific medicine if it does not exist in the inventory.
     *
     * @param medicineName The name of the medicine to replenish.
     */
    public void sendReplenishmentRequest(String medicineName) {
        if (inventory.medicineExists(medicineName)) {
            System.out.println("Medicine already exists in inventory. No need to replenish.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(REPLENISHMENT_REQUEST_FILE, true))) {
            // Append the new replenishment request
            writer.write(medicineName + ",Pending");
            writer.newLine();
            System.out.println("Replenishment request for " + medicineName + " added to file.");
        } catch (IOException e) {
            System.out.println("Error writing replenishment request to file: " + e.getMessage());
        }
    }

    /**
     * Checks the status of a replenishment request for a specific medicine.
     *
     * @param medicineName The name of the medicine to check.
     */
    public void checkReplenishmentRequestStatus(String medicineName) {
        boolean requestFound = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(REPLENISHMENT_REQUEST_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length == 2 && details[0].equalsIgnoreCase(medicineName)) {
                    System.out.println("Replenishment Request Status for " + medicineName + ": " + details[1]);
                    requestFound = true;
                    break;
                }
            }

            if (!requestFound) {
                System.out.println("No replenishment request found for " + medicineName);
            }

        } catch (IOException e) {
            System.out.println("Error reading replenishment request file: " + e.getMessage());
        }
    }

    /**
     * Displays the current inventory, including low stock alerts and medicine details.
     */
    public void checkInventory() {
        inventory.displayInventory();
    }

    /**
     * Displays the Pharmacist-specific menu options.
     */
    @Override
    public void displayMenu() {
        System.out.println("=====================================");
        System.out.println("          Pharmacist Portal          ");
        System.out.println("=====================================");
        System.out.println("1. View Prescriptions by Patient ID");
        System.out.println("2. Dispense Medicine for Prescription");
        System.out.println("3. View Inventory");
        System.out.println("4. Submit Replenishment Request");
        System.out.println("5. Check Replenishment Request Status");
        System.out.println("6. Change Password");
        System.out.println("7. Logout");
        System.out.println("=====================================");
    }
    
}
