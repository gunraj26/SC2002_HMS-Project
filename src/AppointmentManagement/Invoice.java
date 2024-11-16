package AppointmentManagement;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an invoice for a patient's appointment, including consultation fees, medicines, and total cost.
 */
public class Invoice {
    private String appointmentID; // ID of the associated appointment
    private int patientID; // ID of the patient
    private List<String> medicineNames; // Names of prescribed medicines
    private List<Integer> quantities; // Quantities of each prescribed medicine
    private List<Double> prices; // Prices per unit of each prescribed medicine
    private double consultationFee; // Fee charged for the consultation
    private double totalAmount; // Total amount of the invoice

    /**
     * Constructs an Invoice object with the specified appointment ID, patient ID, and consultation fee.
     *
     * @param appointmentID   The ID of the associated appointment.
     * @param patientID       The ID of the patient.
     * @param consultationFee The fee charged for the consultation.
     */
    public Invoice(String appointmentID, int patientID, double consultationFee) {
        this.appointmentID = appointmentID;
        this.patientID = patientID;
        this.consultationFee = consultationFee;
        this.medicineNames = new ArrayList<>();
        this.quantities = new ArrayList<>();
        this.prices = new ArrayList<>();
    }

    /**
     * Adds a medicine to the invoice with its name, quantity, and price per unit.
     *
     * @param medicineName The name of the medicine.
     * @param quantity     The quantity of the medicine.
     * @param pricePerUnit The price per unit of the medicine.
     */
    public void addMedicine(String medicineName, int quantity, double pricePerUnit) {
        medicineNames.add(medicineName);
        quantities.add(quantity);
        prices.add(pricePerUnit);
    }

    /**
     * Calculates the total amount for the invoice, including consultation fees and medicines.
     */
    public void calculateTotalAmount() {
        totalAmount = consultationFee;
        for (int i = 0; i < medicineNames.size(); i++) {
            totalAmount += quantities.get(i) * prices.get(i);
        }
    }

    /**
     * Retrieves the total amount of the invoice.
     *
     * @return The total amount of the invoice.
     */
    public double getTotalAmount() {
        return totalAmount;
    }

    /**
     * Displays the invoice details, including consultation fee, medicines, and total amount.
     */
    public void displayInvoice() {
        System.out.println("------- Invoice -------");
        System.out.println("Appointment ID: " + appointmentID);
        System.out.println("Patient ID: " + patientID);
        System.out.println("Consultation Fee: $" + consultationFee);
        System.out.println("Medicines:");
        System.out.printf("%-20s %-10s %-10s%n", "Medicine", "Quantity", "Price");
        for (int i = 0; i < medicineNames.size(); i++) {
            System.out.printf("%-20s %-10d $%-10.2f%n", medicineNames.get(i), quantities.get(i), prices.get(i) * quantities.get(i));
        }
        System.out.println("-----------------------");
        System.out.printf("Total Amount: $%.2f%n", totalAmount);
    }

    /**
     * Retrieves the invoice details as a string formatted for storage or file writing.
     *
     * @return A string containing the invoice details.
     */
    public String getInvoiceDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append(appointmentID).append(",").append(patientID).append(",").append(consultationFee).append(",");
        for (int i = 0; i < medicineNames.size(); i++) {
            sb.append(medicineNames.get(i)).append(";").append(quantities.get(i)).append(";").append(prices.get(i));
            if (i != medicineNames.size() - 1) {
                sb.append("|");
            }
        }
        sb.append(",").append(totalAmount);
        return sb.toString();
    }
}
