// Inventory.java
package InventoryManagement;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the inventory of medicines.
 */
public class Inventory {
    private List<Medicine> medicines;
    private static final String INVENTORY_FILE = "inventory.txt";

   /**
     * Constructor initializes the inventory by loading existing medicines from the file.
     */
    public Inventory() {
        medicines = new ArrayList<>();
        loadInventory();
    }

    /**
     * Loads medicines from the inventory file.
     * If the file does not exist, an empty inventory is initialized.
     */
    private void loadInventory() {
        File file = new File(INVENTORY_FILE);
        if (!file.exists()) {
            // If file doesn't exist, initialize empty inventory
            System.out.println("Inventory file not found. Starting with an empty inventory.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Expected format: medicineID,name,stock,threshold,price
                String[] parts = parseCSVLine(line);
                if (parts.length >= 5) {
                    String medicineID = parts[0].trim();
                    String name = parts[1].trim();
                    int stock = Integer.parseInt(parts[2].trim());
                    int threshold = Integer.parseInt(parts[3].trim());
                    double price = Double.parseDouble(parts[4].trim());

                    // Create Medicine object with all required parameters
                    Medicine medicine = new Medicine(medicineID, name, stock, threshold, price);
                    medicines.add(medicine);
                } else {
                    System.out.println("Invalid line in inventory file: " + line);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading inventory: " + e.getMessage());
        }
    }

    /**
     * Parses a CSV line, handling commas within quoted strings.
     *
     * @param line The CSV line to parse.
     * @return An array of parsed fields.
     */
    private String[] parseCSVLine(String line) {
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
        return result.toArray(new String[0]);
    }

     /**
     * Saves all medicines to the inventory file.
     */
    private void saveInventory() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(INVENTORY_FILE))) {
            for (Medicine medicine : medicines) {
                writer.write(medicine.toFileString());
                writer.newLine();
            }
            System.out.println("Inventory saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving inventory: " + e.getMessage());
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
     * Checks if a medicine exists by its name.
     *
     * @param medicineName The name of the medicine.
     * @return True if the medicine exists, otherwise false.
     */
    public boolean medicineExists(String medicineName) {
        return getMedicineByName(medicineName) != null;
    }

    /**
     * Checks if a medicine exists by its ID.
     *
     * @param medicineID The ID of the medicine.
     * @return True if the medicine exists, otherwise false.
     */
    public boolean medicineIDExists(String medicineID) {
        for (Medicine medicine : medicines) {
            if (medicine.getMedicineID().equalsIgnoreCase(medicineID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new medicine to the inventory.
     *
     * @param medicine The Medicine object to add.
     */
    public void addMedicine(Medicine medicine) {
        // Ensure no duplicate medicine names
        if (medicineExists(medicine.getName())) {
            System.out.println("Medicine with name '" + medicine.getName() + "' already exists.");
            return;
        }
        medicines.add(medicine);
        saveInventory();
        System.out.println("Medicine '" + medicine.getName() + "' added successfully.");
    }

    /**
     * Retrieves a medicine by its name.
     *
     * @param name The name of the medicine.
     * @return The Medicine object if found, otherwise null.
     */
    public Medicine getMedicineByName(String name) {
        for (Medicine medicine : medicines) {
            if (medicine.getName().equalsIgnoreCase(name)) {
                return medicine;
            }
        }
        return null;
    }

    /**
     * Retrieves the price of a medicine by its name.
     *
     * @param name The name of the medicine.
     * @return The price per unit if found, otherwise 0.0.
     */
    public double getMedicinePrice(String name) {
        Medicine medicine = getMedicineByName(name);
        if (medicine != null) {
            return medicine.getPrice();
        }
        return 0.0;
    }

    /**
     * Gets the stock level of a medicine.
     *
     * @param medicine The Medicine object.
     * @return The current stock level.
     */
    public int getStockLevel(Medicine medicine) {
        return medicine.getStock();
    }

    /**
     * Updates the stock level of a medicine.
     *
     * @param medicine The Medicine object to update.
     * @param quantity The quantity to add (positive) or subtract (negative).
     */
    public void updateStock(Medicine medicine, int quantity) {
        medicine.setStock(medicine.getStock() + quantity);
        saveInventory();
        System.out.println("Updated stock for '" + medicine.getName() + "' by " + quantity + " units.");
    }

    /**
     * Gets the low stock alert level of a medicine.
     *
     * @param medicine The Medicine object.
     * @return The threshold value.
     */
    public int getLowStockAlertLevel(Medicine medicine) {
        return medicine.getThreshold();
    }

    /**
     * Sets the low-stock alert level for a medicine.
     *
     * @param medicine   The Medicine object to update.
     * @param alertLevel The new threshold value.
     */
    public void setLowStockAlertLevel(Medicine medicine, int alertLevel) {
        medicine.setThreshold(alertLevel);
        saveInventory();
        System.out.println("Set new threshold for '" + medicine.getName() + "' to " + alertLevel + ".");
    }

    /**
     * Checks and displays medicines that are below their threshold stock level.
     */
    public void checkLowStock() {
        System.out.println("Low Stock Medicines:");
        boolean found = false;
        for (Medicine medicine : medicines) {
            if (medicine.getStock() < medicine.getThreshold()) {
                System.out.println(medicine);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No medicines are below the threshold.");
        }
    }

    /**
     * Displays the entire inventory, including medicine ID, name, stock, threshold, and price.
     */
    public void displayInventory() {
        System.out.println("=============================================================");
        System.out.println("| Medicine ID | Name         | Stock | Threshold | Price    |");
        System.out.println("=============================================================");
    
        for (Medicine medicine : medicines) {
            System.out.printf("| %-11s | %-12s | %-5d | %-9d | $%-7.2f |%n",
                              medicine.getMedicineID(),
                              medicine.getName(),
                              medicine.getStock(),
                              medicine.getThreshold(),
                              medicine.getPrice());
        }
    
        System.out.println("=============================================================");
    }
    
}
