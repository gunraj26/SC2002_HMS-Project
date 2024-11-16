package MainApp;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// Import your custom classes accordingly
// Ensure that the package names match your project structure
import AppointmentManagement.Appointment;
import AppointmentManagement.AppointmentManager;
import InventoryManagement.Inventory;
import InventoryManagement.Pharmacist; // Ensure correct spelling: Pharmacist
import UserManagment.Administrator;
import UserManagment.Doctor;
import UserManagment.Patient;
import UserManagment.Reception;
import UserManagment.User;

public class Main {
    private static Map<String, Doctor> doctors = new HashMap<>(); // Store doctors by ID

    /**
     * Waits for the user to press "ENTER" to continue.
     */
    public static void promptEnterKey(){
        System.out.println("Press \"ENTER\" to continue...");
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the console screen.
     */
    public static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Initialize Inventory and AppointmentManager
        Inventory inventory = new Inventory();
        AppointmentManager appointmentManager = new AppointmentManager(doctors);
        loadDoctors(appointmentManager);

        // Initialize Receptionist
        Reception receptionist = new Reception(5, "recep123", "Receptionist", "Mike Johnson",
                "mikej@example.com", "7890123456", appointmentManager, doctors, inventory);

        // Display Header
        displayHeader();
        promptEnterKey();

        boolean exit = false;
        while (!exit) {
            clearConsole();
            displayMainMenu();

            int roleChoice = getValidRoleChoice(scanner, 1, 6);

            User user = null;

            switch (roleChoice) {
                case 1:
                    user = handleAdministratorLogin(scanner, inventory, appointmentManager);
                    break;

                case 2:
                    user = handleDoctorLogin(scanner, appointmentManager);
                    break;

                case 3:
                    user = handlePatientLogin(scanner, receptionist, appointmentManager);
                    break;

                case 4:
                    user = handlePharmacistLogin(scanner, inventory);
                    break;

                case 5:
                    user = handleReceptionistLogin(scanner, appointmentManager, doctors, inventory);
                    break;

                case 6:
                    exit = true;
                    System.out.println("");
                    System.out.println("Exiting the HMS. Goodbye!");
                    System.out.println("");
                    continue;

                default:
                    // This case should never be reached due to input validation
                    System.out.println("Invalid choice. Please select a number between 1 and 6.");
                    promptEnterKey();
                    continue;
            }

            if (user != null) {
                displayUserMenu(user, scanner, inventory, appointmentManager, receptionist);
            }
        }

        scanner.close();
    }

    /**
     * Displays the system header.
     */
    private static void displayHeader() {
        System.out.println("***********************************");
        System.out.println("*                                 *");
        System.out.println("*   ██╗  ██╗███╗   ███╗███████╗   *");
        System.out.println("*   ██║  ██║████╗ ████║██╔════╝   *");
        System.out.println("*   ███████║██╔████╔██║███████    *");
        System.out.println("*   ██╔══██║██║╚██╔╝██║     ██╗   *");
        System.out.println("*   ██║  ██║██║ ╚═╝ ██║███████║   *");
        System.out.println("*   ╚═╝  ╚═╝╚═╝     ╚═╝╚══════╝   *");
        System.out.println("*                                 *");
        System.out.println("*   HOSPITAL MANAGEMENT SYSTEM    *");
        System.out.println("*                                 *");
        System.out.println("***********************************");
    }

    /**
     * Displays the main menu options.
     */
    private static void displayMainMenu() {
        System.out.println("=====================================");
        System.out.println("     HOSPITAL Management System      ");
        System.out.println("=====================================");
        System.out.println("1. Administrator");
        System.out.println("2. Doctor");
        System.out.println("3. Patient");
        System.out.println("4. Pharmacist");
        System.out.println("5. Receptionist");
        System.out.println("6. Exit");
        System.out.println("=====================================");
    }

    /**
     * Prompts the user to enter a valid role choice within the specified range.
     *
     * @param scanner Scanner object for input
     * @param min     Minimum valid value (inclusive)
     * @param max     Maximum valid value (inclusive)
     * @return A valid integer within the specified range
     */
    private static int getValidRoleChoice(Scanner scanner, int min, int max) {
        int roleChoice = 0;
        boolean valid = false;

        while (!valid) {
            System.out.print("Choose your role: ");
            if (scanner.hasNextInt()) {
                roleChoice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (roleChoice >= min && roleChoice <= max) {
                    valid = true;
                } else {
                    System.out.println("Invalid choice. Please select a number between " + min + " and " + max + ".");
                }
            } else {
                System.out.println("Invalid input. Please enter a numeric choice (" + min + "-" + max + ").");
                scanner.nextLine(); // Clear the invalid input
            }
        }

        return roleChoice;
    }

    /**
     * Handles Administrator login.
     *
     * @param scanner           Scanner object for input
     * @param inventory         Inventory object
     * @param appointmentManager AppointmentManager object
     * @return An Administrator object if login is successful; otherwise, null
     */
    private static User handleAdministratorLogin(Scanner scanner, Inventory inventory, AppointmentManager appointmentManager) {
        int adminAttempts = 0;
        final int MAX_ADMIN_ATTEMPTS = 3;
        Administrator admin = null;

        while (adminAttempts < MAX_ADMIN_ATTEMPTS) {
            int adminUserID = -1;
            boolean validInput = false;

            // Loop until a valid User ID is entered
            while (!validInput) {
                System.out.print("Enter Administrator User ID: ");
                if (scanner.hasNextInt()) {
                    adminUserID = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    validInput = true;
                } else {
                    System.out.println("Invalid input. Please enter a numeric User ID.");
                    scanner.nextLine(); // Clear the invalid input
                }
            }

            // Check if User ID exists for Administrator role
            boolean userIDExists = User.checkUserIDExists(adminUserID, "Administrator");
            if (!userIDExists) {
                adminAttempts++;
                if (adminAttempts < MAX_ADMIN_ATTEMPTS) {
                    System.out.println("Invalid Administrator User ID. Please try again.");
                } else {
                    System.out.println("Too many failed attempts. Returning to the main menu.");
                    return null;
                }
                continue;
            }

            System.out.print("Enter Administrator Password: ");
            String adminPassword = scanner.nextLine();

            admin = User.loadAdministratorFromFile(adminUserID, adminPassword, inventory, appointmentManager);
            if (admin != null) {
                System.out.println("Administrator logged in successfully!");
                promptEnterKey();
                return admin;
            } else {
                adminAttempts++;
                if (adminAttempts < MAX_ADMIN_ATTEMPTS) {
                    System.out.println("Invalid Administrator password. Please try again.");
                } else {
                    System.out.println("Too many failed login attempts. Returning to the main menu.");
                }
            }
        }

        return null; // Return to the main menu after failed attempts
    }

    /**
     * Handles Doctor login.
     *
     * @param scanner           Scanner object for input
     * @param appointmentManager AppointmentManager object
     * @return A Doctor object if login is successful; otherwise, null
     */
    private static User handleDoctorLogin(Scanner scanner, AppointmentManager appointmentManager) {
        int doctorAttempts = 0;
        final int MAX_DOCTOR_ATTEMPTS = 3;
        Doctor doctor = null;

        while (doctorAttempts < MAX_DOCTOR_ATTEMPTS) {
            int doctorUserID = -1;
            boolean validInput = false;

            // Loop until a valid User ID is entered
            while (!validInput) {
                System.out.print("Enter Doctor User ID: ");
                if (scanner.hasNextInt()) {
                    doctorUserID = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    validInput = true;
                } else {
                    System.out.println("Invalid input. Please enter a numeric User ID.");
                    scanner.nextLine(); // Clear the invalid input
                }
            }

            // Check if User ID exists for Doctor role
            boolean userIDExists = User.checkUserIDExists(doctorUserID, "Doctor");
            if (!userIDExists) {
                doctorAttempts++;
                if (doctorAttempts < MAX_DOCTOR_ATTEMPTS) {
                    System.out.println("Invalid Doctor User ID. Please try again.");
                } else {
                    System.out.println("Too many failed attempts. Returning to the main menu.");
                    return null;
                }
                continue;
            }

            System.out.print("Enter Doctor Password: ");
            String doctorPassword = scanner.nextLine();

            doctor = User.loadDoctorFromFile(doctorUserID, doctorPassword, appointmentManager);
            if (doctor != null) {
                System.out.println("Doctor logged in successfully!");
                promptEnterKey();
                return doctor;
            } else {
                doctorAttempts++;
                if (doctorAttempts < MAX_DOCTOR_ATTEMPTS) {
                    System.out.println("Invalid Doctor password. Please try again.");
                } else {
                    System.out.println("Too many failed login attempts. Returning to the main menu.");
                }
            }
        }

        return null; // Return to the main menu after failed attempts
    }

    /**
     * Handles Patient login or registration.
     *
     * @param scanner           Scanner object for input
     * @param receptionist      Reception object
     * @param appointmentManager AppointmentManager object
     * @return A Patient object if login or registration is successful; otherwise, null
     */
    private static User handlePatientLogin(Scanner scanner, Reception receptionist, AppointmentManager appointmentManager) {
        while (true) { // Continuous loop until a valid login or registration occurs
            System.out.print("Enter your User ID (Enter 0 if you are a new Patient): ");
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a numeric User ID.");
                scanner.nextLine(); // Clear invalid input
                continue;
            }
            int patientUserID = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            if (patientUserID == 0) {
                // Registration logic
                Patient patient = receptionist.registerNewPatient();
                if (patient != null) {
                    System.out.println("Registration successful!");
                    promptEnterKey();
                    return patient;
                } else {
                    System.out.println("Registration failed. Returning to main menu.");
                    promptEnterKey();
                    return null;
                }
            } else if (patientUserID > 0 && receptionist.patientExists(patientUserID)) {
                Patient patient = receptionist.loginExistingPatient(patientUserID);
                if (patient != null) {
                    //System.out.println("Patient logged in successfully!");
                    promptEnterKey();
                    return patient;
                } else {
                    System.out.println("Invalid Patient credentials. Please try again.");
                    promptEnterKey();
                    // Continue the loop to prompt again
                }
            } else {
                System.out.println("User ID not found. Please enter a valid User ID or 0 to register.");
                promptEnterKey();
                // Continue the loop to prompt again
            }
        }
    }

    /**
     * Handles Pharmacist login.
     *
     * @param scanner    Scanner object for input
     * @param inventory  Inventory object
     * @return A Pharmacist object if login is successful; otherwise, null
     */
    private static User handlePharmacistLogin(Scanner scanner, Inventory inventory) {
        int pharmacistAttempts = 0;
        final int MAX_PHARMACIST_ATTEMPTS = 3;
        Pharmacist loggedInPharmacist = null;

        while (pharmacistAttempts < MAX_PHARMACIST_ATTEMPTS) {
            int pharmacistUserID = -1;
            boolean validInput = false;

            // Loop until a valid User ID is entered
            while (!validInput) {
                System.out.print("Enter Pharmacist User ID: ");
                if (scanner.hasNextInt()) {
                    pharmacistUserID = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    validInput = true;
                } else {
                    System.out.println("Invalid input. Please enter a numeric User ID.");
                    scanner.nextLine(); // Clear the invalid input
                }
            }

            // Check if User ID exists for Pharmacist role
            boolean userIDExists = User.checkUserIDExists(pharmacistUserID, "Pharmacist");
            if (!userIDExists) {
                pharmacistAttempts++;
                if (pharmacistAttempts < MAX_PHARMACIST_ATTEMPTS) {
                    System.out.println("Invalid Pharmacist User ID. Please try again.");
                } else {
                    System.out.println("Too many failed attempts. Returning to the main menu.");
                    return null; // Return to the main menu
                }
                continue;
            }

            System.out.print("Enter Pharmacist Password: ");
            String pharmacistPassword = scanner.nextLine();

            loggedInPharmacist = User.loadPharmacistFromFile(pharmacistUserID, pharmacistPassword, inventory);
            if (loggedInPharmacist != null) {
                System.out.println("Pharmacist logged in successfully!");
                promptEnterKey();
                return loggedInPharmacist;
            } else {
                pharmacistAttempts++;
                if (pharmacistAttempts < MAX_PHARMACIST_ATTEMPTS) {
                    System.out.println("Invalid Pharmacist password. Please try again.");
                } else {
                    System.out.println("Too many failed login attempts. Returning to the main menu.");
                }
            }
        }

        return null; // Return to the main menu after failed attempts
    }

    /**
     * Handles Receptionist login.
     *
     * @param scanner            Scanner object for input
     * @param appointmentManager AppointmentManager object
     * @param doctors            Map of doctors
     * @param inventory          Inventory object
     * @return A Receptionist object if login is successful; otherwise, null
     */
    private static User handleReceptionistLogin(Scanner scanner, AppointmentManager appointmentManager, Map<String, Doctor> doctors, Inventory inventory) {
        int receptionistAttempts = 0;
        final int MAX_RECEPTIONIST_ATTEMPTS = 3;
        Reception loggedInReceptionist = null;

        while (receptionistAttempts < MAX_RECEPTIONIST_ATTEMPTS) {
            int recepUserID = -1;
            boolean validInput = false;

            // Loop until a valid User ID is entered
            while (!validInput) {
                System.out.print("Enter Receptionist User ID: ");
                if (scanner.hasNextInt()) {
                    recepUserID = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    validInput = true;
                } else {
                    System.out.println("Invalid input. Please enter a numeric User ID.");
                    scanner.nextLine(); // Clear the invalid input
                }
            }

            // Check if User ID exists for Receptionist role
            boolean userIDExists = User.checkUserIDExists(recepUserID, "Receptionist");
            if (!userIDExists) {
                receptionistAttempts++;
                if (receptionistAttempts < MAX_RECEPTIONIST_ATTEMPTS) {
                    System.out.println("Invalid Receptionist User ID. Please try again.");
                } else {
                    System.out.println("Too many failed attempts. Returning to the main menu.");
                    return null; // Return to the main menu
                }
                continue;
            }

            System.out.print("Enter Receptionist Password: ");
            String recepPassword = scanner.nextLine();

            loggedInReceptionist = loadReceptionistFromFile(recepUserID, recepPassword, appointmentManager, doctors, inventory);

            if (loggedInReceptionist != null) {
                System.out.println("Receptionist logged in successfully!");
                promptEnterKey();
                return loggedInReceptionist;
            } else {
                receptionistAttempts++;
                if (receptionistAttempts < MAX_RECEPTIONIST_ATTEMPTS) {
                    System.out.println("Invalid Receptionist password. Please try again.");
                } else {
                    System.out.println("Too many failed login attempts. Returning to the main menu.");
                }
            }
        }

        return null; // Return to the main menu after failed attempts
    }

    /**
     * Displays the user-specific menu and handles user actions.
     *
     * @param user               The logged-in user
     * @param scanner            Scanner object for input
     * @param inventory          Inventory object
     * @param appointmentManager AppointmentManager object
     * @param receptionist       Reception object (needed for patient actions)
     */
    private static void displayUserMenu(User user, Scanner scanner, Inventory inventory, AppointmentManager appointmentManager, Reception receptionist) {
        boolean back = false;
        while (!back) {
            clearConsole();
            user.displayMenu();
            System.out.print("Choose an action: ");

            int choice = 0;
            boolean validChoice = false;

            // Input validation for user menu choice
            while (!validChoice) {
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    validChoice = true;
                } else {
                    System.out.println("Invalid input. Please enter a numeric choice.");
                    scanner.nextLine(); // Clear invalid input
                    System.out.print("Choose an action: ");
                }
            }

            boolean stayInMenu = true;

            switch (user.getClass().getSimpleName()) {
                case "Administrator":
                    stayInMenu = handleAdminActions((Administrator) user, choice, scanner, inventory);
                    break;
                case "Doctor":
                    stayInMenu = handleDoctorActions((Doctor) user, choice, scanner, appointmentManager);
                    break;
                case "Pharmacist":
                    stayInMenu = handlePharmacistActions((Pharmacist) user, choice, scanner, inventory, appointmentManager);
                    break;
                case "Reception":
                    stayInMenu = handleReceptionistActions((Reception) user, choice, scanner, appointmentManager);
                    break;
                case "Patient":
                    stayInMenu = handlePatientActions((Patient) user, receptionist, appointmentManager, scanner, choice);
                    break;
                default:
                    System.out.println("Unknown user role. Logging out.");
                    stayInMenu = false;
                    break;
            }

            if (!stayInMenu) {
                back = true; // Exit the menu loop
            }
        }
    }

    /**
     * Handles Administrator-specific actions.
     *
     * @param admin     Administrator object
     * @param choice    User's menu choice
     * @param scanner   Scanner object for input
     * @param inventory Inventory object
     * @return True to stay in the menu; False to exit
     */
    private static boolean handleAdminActions(Administrator admin, int choice, Scanner scanner, Inventory inventory) {
        switch (choice) {
            case 1:
                admin.manageHospitalStaff(scanner);
                break;
            case 2:
                admin.viewAppointmentDetails(scanner);
                break;
            case 3:
                admin.manageMedicationInventory(scanner);
                break;
            case 4:
                admin.viewPendingReplenishmentRequests();
                break;
            case 5:
                System.out.print("Enter the name of the medicine to approve replenishment: ");
                String medicineName = scanner.nextLine();
                admin.approveReplenishmentRequest(medicineName);
                break;
            case 6:
                System.out.println("Logging out of Administrator.");
                return false;
            default:
                System.out.println("Invalid choice. Try again.");
        }
        promptEnterKey();
        return true;
    }

    /**
     * Handles Doctor-specific actions.
     *
     * @param doctor             Doctor object
     * @param choice             User's menu choice
     * @param scanner            Scanner object for input
     * @param appointmentManager AppointmentManager object
     * @return True to stay in the menu; False to exit
     */
    private static boolean handleDoctorActions(Doctor doctor, int choice, Scanner scanner, AppointmentManager appointmentManager) {
        switch (choice) {
            case 1:
                System.out.print("Enter Patient User ID to view record: ");
                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a numeric User ID.");
                    scanner.nextLine(); // Clear invalid input
                    break;
                }
                int userID = scanner.nextInt();
                scanner.nextLine();
                if (userID <= 0) {
                    System.out.println("Invalid User ID. Please try again.");
                    break;
                }
                Patient patient = new Patient(userID, appointmentManager);
                doctor.viewPatientMedicalRecord(patient);
                break;
            case 2:
                // Update patient medical record
                boolean have = doctor.viewCompletedAppointments();
                if (have){
                    System.out.print("Enter Appointment ID to update medical record: ");
                    String appointmentID = scanner.nextLine();

                    System.out.print("Enter Patient ID: ");
                    if (!scanner.hasNextInt()) {
                        System.out.println("Invalid input. Please enter a numeric Patient ID.");
                        scanner.nextLine(); // Clear invalid input
                        break;
                    }
                    int patientID = scanner.nextInt();
                    scanner.nextLine();

                    if (patientID <= 0) {
                        System.out.println("Invalid Patient ID. Please try again.");
                        break;
                    }

                    Patient patientForRecord = new Patient(patientID, appointmentManager);
                    doctor.updatePatientMedicalRecord(patientForRecord, appointmentID);
                }
                break;
            case 3:
                LocalDate scheduleDate = null;
                while (scheduleDate == null) {
                    try {
                        System.out.print("Enter date to view your schedule (yyyy-mm-dd): ");
                        scheduleDate = LocalDate.parse(scanner.nextLine());
                        if (scheduleDate.isBefore(LocalDate.now())) {
                            System.out.println("Date cannot be in the past. Please enter a future date.");
                            scheduleDate = null;
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format. Please enter the date in yyyy-mm-dd format.");
                    }
                }
                doctor.viewUpcomingAppointments();
                doctor.ensureDailySlotsInitialized(scheduleDate);
                doctor.loadUnavailableSlots(scheduleDate);
                doctor.showAvailableTimeSlots();
                break;
            case 4:
                LocalDate unavailableDate = null;
                while (unavailableDate == null) {
                    try {
                        System.out.print("Enter date to set unavailability (yyyy-mm-dd): ");
                        unavailableDate = LocalDate.parse(scanner.nextLine());
                        if (unavailableDate.isBefore(LocalDate.now())) {
                            System.out.println("Date cannot be in the past. Please enter a future date.");
                            unavailableDate = null;
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid date format. Please enter the date in yyyy-mm-dd format.");
                    }
                }

                LocalTime startTime = null;
                while (startTime == null) {
                    try {
                        System.out.print("Enter start time of the slot to set as unavailable (HH:mm): ");
                        startTime = LocalTime.parse(scanner.nextLine());
                    } catch (DateTimeParseException e) {
                        System.out.println("Invalid time format. Please enter the time in HH:mm format.");
                    }
                }
                doctor.setSlotUnavailable(unavailableDate, startTime);
                break;
            case 5:
                // Respond to appointment requests
                doctor.viewUpcomingAppointments();
                System.out.print("Enter Appointment ID to respond to: ");
                String respondAppointmentID = scanner.nextLine();
                System.out.print("Confirm appointment? (yes/no): ");
                String response = scanner.nextLine();
                boolean confirm = response.equalsIgnoreCase("yes");
                doctor.respondToAppointmentRequest(respondAppointmentID, confirm);
                break;
            case 6:
                doctor.viewUpcomingAppointments();
                break;
            case 7:
                // Record appointment outcome
                doctor.viewConfirmedAppointments();
                System.out.print("Enter Appointment ID to record outcome: ");
                String outcomeAppointmentID = scanner.nextLine();
                doctor.recordAppointmentOutcome(outcomeAppointmentID);
                break;
            case 8:
                doctor.viewCompletedAppointments();
                break;
            case 9:
                doctor.viewConfirmedAppointments();
                break;
            case 10:
                System.out.print("Enter new password: ");
                String newPassword = scanner.nextLine();
                doctor.changePassword(newPassword);
                break;
            case 11:
                System.out.println("Logging out of Doctor.");
                return false;
            default:
                System.out.println("Invalid choice. Try again.");
        }
        promptEnterKey();
        return true; // Continue the menu loop
    }

    /**
     * Handles Patient-specific actions.
     *
     * @param patient            Patient object
     * @param receptionist       Reception object
     * @param appointmentManager AppointmentManager object
     * @param scanner            Scanner object for input
     * @return True to stay in the menu; False to exit
     */
    private static boolean handlePatientActions(Patient patient, Reception receptionist, AppointmentManager appointmentManager, Scanner scanner, int choice) {
        //while (!back) {
            /* 
            clearConsole();
            patient.displayMenu();
            System.out.print("Choose an action: ");
            int choice = 0;
            boolean validChoice = false;

            // Input validation for patient menu choice
            while (!validChoice) {
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                    validChoice = true;
                } else {
                    System.out.println("Invalid input. Please enter a numeric choice.");
                    scanner.nextLine(); // Clear invalid input
                    System.out.print("Choose an action: ");
                }
            }
            */

            switch (choice) {
                case 1:
                    clearConsole();
                    System.out.println("=== Medical Record ===");
                    patient.viewMedicalRecord();
                    promptEnterKey();
                    break;
                case 2:
                    patient.updatePatientData();
                    promptEnterKey();
                    break;
                case 3:
                    LocalDate dateToView = null;
                    while (dateToView == null) {
                        System.out.print("Enter date to view available slots (yyyy-mm-dd): ");
                        String inputDate = scanner.nextLine();
                        try {
                            dateToView = LocalDate.parse(inputDate);
                            if (dateToView.isBefore(LocalDate.now())) {
                                System.out.println("Date cannot be in the past. Please enter a future date.");
                                dateToView = null;
                            }
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date format. Please enter the date in yyyy-mm-dd format.");
                        }
                    }
                    receptionist.viewAvailableSlots(dateToView);
                    promptEnterKey();
                    break;
                case 4:
                    System.out.print("Enter the Doctor ID to book an appointment: ");
                    String doctorID = scanner.nextLine();

                    LocalDate appointmentDate = null;
                    while (appointmentDate == null) {
                        try {
                            System.out.print("Enter date (yyyy-mm-dd) for appointment: ");
                            appointmentDate = LocalDate.parse(scanner.nextLine());
                            if (appointmentDate.isBefore(LocalDate.now())) {
                                System.out.println("Date cannot be in the past. Please enter a future date.");
                                appointmentDate = null;
                            }
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid date format. Please enter the date in yyyy-mm-dd format.");
                        }
                    }

                    LocalTime time = null;
                    while (time == null) {
                        try {
                            System.out.print("Enter start time (HH:mm) for appointment: ");
                            time = LocalTime.parse(scanner.nextLine());
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid time format. Please enter the time in HH:mm format.");
                        }
                    }
                    // Check if time is not in lunch time, e.g., 12:00-14:00
                    if (time.isAfter(LocalTime.of(12, 59)) && time.isBefore(LocalTime.of(14, 0))) {
                        System.out.println("Cannot book appointments during lunch time (13:00 - 14:00). Please choose a different time.");
                    } 
                    else if (time.isAfter(LocalTime.of(01, 00)) && time.isBefore(LocalTime.of(8, 59))) {
                        System.out.println("Invalid Time slot");
                    }
                    else if (time.isAfter(LocalTime.of(17, 00)) && time.isBefore(LocalTime.of(24, 59))) {
                        System.out.println("Invalid Time slot");
                    }
                    else {
                        receptionist.scheduleAppointment(patient, doctorID, appointmentDate, time);
                    }
                    promptEnterKey();
                    break;
                case 5:
                    patient.viewUpcomingAppointments();
                    appointmentManager.reloadAppointments();
                    System.out.print("Enter the Appointment ID to reschedule: ");
                    String appointmentID = scanner.nextLine();
                    Appointment appointment = appointmentManager.getAppointments().get(appointmentID);
                    if (appointment != null && appointment.getPatientID() == patient.getUserID()) {
                        LocalDate newDate = null;
                        while (newDate == null) {
                            try {
                                System.out.print("Enter new date (yyyy-mm-dd): ");
                                newDate = LocalDate.parse(scanner.nextLine());
                                if (newDate.isBefore(LocalDate.now())) {
                                    System.out.println("Date cannot be in the past. Please enter a future date.");
                                    newDate = null;
                                }
                            } catch (DateTimeParseException e) {
                                System.out.println("Invalid date format. Please enter the date in yyyy-mm-dd format.");
                            }
                        }
                        LocalTime newTime = null;
                        while (newTime == null) {
                            try {
                                System.out.print("Enter new time (HH:mm): ");
                                newTime = LocalTime.parse(scanner.nextLine());
                            } catch (DateTimeParseException e) {
                                System.out.println("Invalid time format. Please enter the time in HH:mm format.");
                            }
                        }
                        // Check lunch time
                        if (newTime.isAfter(LocalTime.of(11, 59)) && newTime.isBefore(LocalTime.of(14, 0))) {
                            System.out.println("Cannot reschedule appointments during lunch time (12:00 - 14:00).");
                        } else {
                            appointment.rescheduleAppointment(newDate, newTime);
                        }
                    } else {
                        System.out.println("Appointment not found or does not belong to you.");
                    }
                    promptEnterKey();
                    break;
                case 6:
                    patient.viewUpcomingAppointments();
                    appointmentManager.reloadAppointments();
                    System.out.print("Enter the Appointment ID to cancel: ");
                    String cancelAppointmentID = scanner.nextLine();
                    Appointment cancelAppointment = appointmentManager.getAppointments().get(cancelAppointmentID);
                    if (cancelAppointment != null && cancelAppointment.getPatientID() == patient.getUserID()) {
                        cancelAppointment.cancelAppointment();
                    } else {
                        System.out.println("Appointment not found or does not belong to you.");
                    }
                    promptEnterKey();
                    break;
                case 7:
                    System.out.println("Viewing Upcoming Appointments:");
                    patient.viewUpcomingAppointments();
                    promptEnterKey();
                    break;
                case 8:
                    patient.viewPastAppointmentOutcomes();
                    promptEnterKey();
                    break;
                case 9:
                    System.out.print("Enter new password: ");
                    String newPassword = scanner.nextLine();
                    patient.changePassword(newPassword);
                    promptEnterKey();
                    break;
                case 10:
                    System.out.print("Enter Appointment ID to view final bill: ");
                    String billAppointmentID = scanner.nextLine();
                    patient.viewFinalBill(billAppointmentID);
                    promptEnterKey();
                    break;
                case 11:
                    System.out.println("Logging out...");
                    return false;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    promptEnterKey();
            }
        //}
        return true; // Continue the menu loop
    }

    /**
     * Handles Pharmacist-specific actions.
     *
     * @param pharmacist         Pharmacist object
     * @param choice             User's menu choice
     * @param scanner            Scanner object for input
     * @param inventory          Inventory object
     * @param appointmentManager AppointmentManager object
     * @return True to stay in the menu; False to exit
     */
    private static boolean handlePharmacistActions(Pharmacist pharmacist, int choice, Scanner scanner, Inventory inventory, AppointmentManager appointmentManager) {
        switch (choice) {
            case 1:
                System.out.print("Enter Patient ID to view prescriptions: ");
                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a numeric Patient ID.");
                    scanner.nextLine(); // Clear invalid input
                    break;
                }
                int patientID = scanner.nextInt();
                scanner.nextLine();
                if (patientID <= 0) {
                    System.out.println("Invalid Patient ID. Please try again.");
                    break;
                }
                pharmacist.viewPrescriptionsByPatientID(patientID, appointmentManager);
                break;
            case 2:
                System.out.print("Enter Patient ID to dispense medicines: ");
                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a numeric Patient ID.");
                    scanner.nextLine(); // Clear invalid input
                    break;
                }
                int pID = scanner.nextInt();
                scanner.nextLine(); // Consume newline
                if (pID <= 0) {
                    System.out.println("Invalid Patient ID. Please try again.");
                    break;
                }
                pharmacist.dispenseMedicines(pID, appointmentManager);
                break;
            case 3:
                pharmacist.checkInventory();
                break;
            case 4:
                System.out.print("Enter the name of the medicine to request replenishment: ");
                String medicineName = scanner.nextLine();
                pharmacist.sendReplenishmentRequest(medicineName);
                break;
            case 5:
                System.out.print("Enter the name of the medicine for which you want to check the replenishment request status: ");
                String m = scanner.nextLine();
                pharmacist.checkReplenishmentRequestStatus(m);
                break;
            case 6:
                System.out.print("Enter new password: ");
                String newPassword = scanner.nextLine();
                pharmacist.changePassword(newPassword);
                break;
            case 7:
                System.out.println("Logging out of Pharmacist.");
                promptEnterKey();
                return false;
            default:
                System.out.println("Invalid choice. Try again.");
                promptEnterKey();
        }
        promptEnterKey();
        return true; // Continue the menu loop
    }

    /**
     * Handles Receptionist-specific actions.
     *
     * @param receptionist       Reception object
     * @param choice             User's menu choice
     * @param scanner            Scanner object for input
     * @param appointmentManager AppointmentManager object
     * @return True to stay in the menu; False to exit
     */
    private static boolean handleReceptionistActions(Reception receptionist, int choice, Scanner scanner, AppointmentManager appointmentManager) {
        switch (choice) {
            case 1:
                // Register New Patient
                receptionist.registerNewPatient();
                break;
            case 2:
                // Generate Bill
                System.out.print("Enter Patient ID: ");
                if (!scanner.hasNextInt()) {
                    System.out.println("Invalid input. Please enter a numeric Patient ID.");
                    scanner.nextLine(); // Clear invalid input
                    break;
                }
                int patientID = scanner.nextInt();
                scanner.nextLine();
                System.out.print("Enter Appointment ID: ");
                String appointmentID = scanner.nextLine();
                if (appointmentID.isEmpty()) {
                    System.out.println("Appointment ID cannot be empty.");
                    break;
                }
                receptionist.generateBill(patientID, appointmentID);
                break;
            case 3:
                // View Available Slots
                System.out.print("Enter date to view available slots (yyyy-mm-dd): ");
                String dateInput = scanner.nextLine();
                LocalDate date;
                try {
                    date = LocalDate.parse(dateInput);
                    receptionist.viewAvailableSlots(date);
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Please use yyyy-mm-dd.");
                }
                break;
            case 4:
                System.out.println("Logging out of Receptionist.");
                promptEnterKey();
                return false;
            default:
                System.out.println("Invalid choice. Try again.");
                promptEnterKey();
        }
        promptEnterKey();
        return true; // Continue the menu loop
    }


    /**
     * Loads a Receptionist from the staff file.
     *
     * @param userID            Receptionist User ID
     * @param password          Receptionist Password
     * @param appointmentManager AppointmentManager object
     * @param doctors           Map of doctors
     * @param inventory         Inventory object
     * @return A Receptionist object if credentials are valid; otherwise, null
     */
    public static Reception loadReceptionistFromFile(int userID, String password,
                                                     AppointmentManager appointmentManager,
                                                     Map<String, Doctor> doctors, Inventory inventory) {
        String fileName = "staff.txt"; // File containing staff details
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length >= 7) {
                    int fileUserID = Integer.parseInt(details[0].trim());
                    String filePassword = details[1].trim();
                    String role = details[2].trim();
                    String name = details[3].trim();
                    String email = details[4].trim();
                    String contactNumber = details[5].trim();
                    // Ignoring staffID for receptionist

                    // If role is Receptionist, and userID and password match, create a Receptionist object
                    if (role.equalsIgnoreCase("Receptionist") && fileUserID == userID && filePassword.equals(password)) {
                        return new Reception(fileUserID, filePassword, role, name, email, contactNumber, appointmentManager, doctors, inventory);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading staff file: " + e.getMessage());
        }
        return null; // Return null if no matching receptionist is found
    }

    /**
     * Loads doctors from the staff file.
     *
     * @param appointmentManager AppointmentManager object
     */
    private static void loadDoctors(AppointmentManager appointmentManager) {
        try (BufferedReader reader = new BufferedReader(new FileReader("staff.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] details = line.split(",");
                if (details.length < 8 || !details[2].trim().equalsIgnoreCase("Doctor")) {
                    // Skipping non-doctor or malformed lines
                    continue;
                }

                try {
                    String doctorID = details[6].trim();
                    String specialization = details[7].trim();
                    Doctor doctor = new Doctor(
                            Integer.parseInt(details[0].trim()),   // userID
                            details[1].trim(),                    // password
                            details[2].trim(),                    // role
                            details[3].trim(),                    // name
                            details[4].trim(),                    // email
                            details[5].trim(),                    // contactNumber
                            doctorID,                             // staffID
                            specialization,                       // specialization
                            appointmentManager                    // Pass AppointmentManager instance here
                    );
                    doctors.put(doctorID, doctor);
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing doctor data: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading doctors: " + e.getMessage());
        }
    }
}
