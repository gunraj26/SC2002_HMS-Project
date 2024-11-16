package InventoryManagement;

import AppointmentManagement.Invoice;

/**
 * Handles billing operations, including calculating total bills and generating invoices for appointments.
 */
public class Billing {
    private Inventory inventory; // Reference to the Inventory object for accessing medicine data
    private double consultationFee; // Standard consultation fee for appointments

    /**
     * Constructor for Billing.
     *
     * @param inventory The Inventory object to interact with medicine data.
     */
    public Billing(Inventory inventory) {
        this.inventory = inventory;
        this.consultationFee = 50.0; // Default consultation fee
    }

    /**
     * Calculates the total bill amount for an appointment based on prescribed medicines and their quantities.
     *
     * @param medicines  An array of medicine names.
     * @param quantities An array of quantities corresponding to each medicine.
     * @return The total bill amount, including the consultation fee and the cost of medicines.
     */
    public double calculateTotalBill(String[] medicines, int[] quantities) {
        double total = consultationFee;
        for (int i = 0; i < medicines.length; i++) {
            String medicineName = medicines[i];
            int quantity = quantities[i];
            double pricePerUnit = inventory.getMedicinePrice(medicineName);

            if (pricePerUnit == 0.0) {
                System.out.println("Medicine " + medicineName + " not found in inventory. Skipping.");
                continue;
            }
            total += pricePerUnit * quantity;
        }
        return total;
    }

    /**
     * Generates an invoice for a specific appointment and patient, including prescribed medicines and their costs.
     *
     * @param appointmentID The ID of the appointment.
     * @param patientID     The ID of the patient.
     * @param medicines     An array of medicine names.
     * @param quantities    An array of quantities corresponding to each medicine.
     * @return The generated Invoice object, containing details of the consultation and prescribed medicines.
     */
    public Invoice generateInvoice(String appointmentID, int patientID, String[] medicines, int[] quantities) {
        Invoice invoice = new Invoice(appointmentID, patientID, consultationFee);
        for (int i = 0; i < medicines.length; i++) {
            String medicineName = medicines[i];
            int quantity = quantities[i];
            double pricePerUnit = inventory.getMedicinePrice(medicineName);

            if (pricePerUnit == 0.0) {
                System.out.println("Medicine " + medicineName + " not found in inventory. Skipping.");
                continue;
            }
            invoice.addMedicine(medicineName, quantity, pricePerUnit);
        }
        invoice.calculateTotalAmount();
        return invoice;
    }
}
