package InventoryManagement;

/**
 * Represents a medicine in the inventory, including details such as ID, name, stock, threshold, and price.
 */
public class Medicine {
    private String medicineID; // Unique identifier for the medicine
    private String name; // Name of the medicine
    private int stock; // Current stock level
    private int threshold; // Threshold for low stock alerts
    private double price; // Price per unit of the medicine

    /**
     * Constructor for Medicine.
     *
     * @param medicineID Unique identifier for the medicine.
     * @param name       Name of the medicine.
     * @param stock      Current stock level.
     * @param threshold  Threshold for low stock alerts.
     * @param price      Price per unit of the medicine.
     */
    public Medicine(String medicineID, String name, int stock, int threshold, double price) {
        this.medicineID = medicineID;
        this.name = name.toLowerCase(); // Store name in lowercase for consistent retrieval
        this.stock = stock;
        this.threshold = threshold;
        this.price = price;
    }

    // Getters

    /**
     * Retrieves the unique ID of the medicine.
     *
     * @return The medicine ID.
     */
    public String getMedicineID() {
        return medicineID;
    }

    /**
     * Retrieves the name of the medicine.
     *
     * @return The name of the medicine.
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves the current stock level of the medicine.
     *
     * @return The current stock level.
     */
    public int getStock() {
        return stock;
    }

    /**
     * Retrieves the threshold for low stock alerts.
     *
     * @return The threshold value.
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Retrieves the price per unit of the medicine.
     *
     * @return The price per unit.
     */
    public double getPrice() {
        return price;
    }

    // Setters

    /**
     * Sets the stock level of the medicine.
     *
     * @param stock The new stock level.
     */
    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Sets the threshold for low stock alerts.
     *
     * @param threshold The new threshold value.
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    // Stock Management Methods

    /**
     * Decreases the stock level by a specified quantity.
     *
     * @param quantity The quantity to decrease.
     */
    public void decreaseStock(int quantity) {
        if (quantity <= stock) {
            stock -= quantity;
        } else {
            System.out.println("Attempted to decrease stock below zero for medicine: " + name);
        }
    }

    /**
     * Increases the stock level by a specified quantity.
     *
     * @param quantity The quantity to increase.
     */
    public void increaseStock(int quantity) {
        if (quantity > 0) {
            stock += quantity;
        } else {
            System.out.println("Invalid quantity. Cannot increase stock by a negative value.");
        }
    }

    // Utility Methods

    /**
     * Converts the medicine details into a comma-separated string suitable for file storage.
     *
     * @return A string representation of the medicine details for storage.
     */
    public String toFileString() {
        return medicineID + "," + name + "," + stock + "," + threshold + "," + price;
    }

    /**
     * Returns a human-readable string representation of the medicine.
     *
     * @return A string with the medicine's details.
     */
    @Override
    public String toString() {
        return "MedicineID: " + medicineID + ", Name: " + name + ", Stock: " + stock +
                ", Threshold: " + threshold + ", Price: $" + String.format("%.2f", price);
    }
}
