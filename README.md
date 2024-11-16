# 🏥 Hospital Management System (HMS) - SC2002 Object-Oriented Design & Programming

Welcome to the **Hospital Management System (HMS)**, a project designed to manage the core operations of a hospital while adhering to **Object-Oriented Programming (OOP)** and **SOLID Design Principles**. This system includes modules for user management, inventory handling, appointment scheduling, billing, and medical records.

---
# Link
[JavaDoc](https://qianyuha.github.io/SC2002-project-HMS/)

## 📋 Team Members

| **NAME**          | **MATRICULATION NUMBER**|**STUDENT NUMBER**|
|-------------------|-------------------------|------------------|
| Kumar Advaith     | U2323503B               | 14               |
| Lam Wai Jun       | U2321478F               | 15               |
| Shu Xuanyu        | U2320246L               | 25               |
| Singh Gunraj      | U2323772B               | 26               |
| Tan Zhi Xin       | U2321371F               | 27               |

---

## 📦 Project Features

### 👥 **User Roles**
- **Administrator**: Full control over staff, inventory, and system configurations.
- **Doctor**: Manage appointments and patient medical records.
- **Pharmacist**: Handle prescriptions and notify about inventory shortages.
- **Receptionist**: Manage patient registrations, appointments, and billing.
- **Patient**: Schedule and View appointments and medical history.

---

## ✨ Key Features

### 📝 1. Patient Registration
- Patients can register with essential details such as:
  - Name, date of birth, gender, blood type, email, and contact number.
- Patients create a unique user ID (validated to ensure no duplicates).
- Patients can set their own passwords and change them later.

---

### 👨‍⚕️ 2. Staff Registration (Doctors, Pharmacists, Receptionists)
- The Administrator can register (add) new staff members.
- Each staff member is assigned a default, non-changeable Staff ID.
- Staff can change their default passwords after login.

---

### 📅 3. Appointment Management
- **Schedule Management**:
  - Doctors can set unavailability for specific time slots and view their personal schedules with upcoming appointments for a selected date.
  - Patients can view available slots for scheduling appointments with a specific doctor on a selected date.
- **Booking and Rescheduling**:
  - Patients can schedule, reschedule, or cancel appointments.
  - Doctors can confirm, decline, or view upcoming appointments.
- **Restrictions**:
  - Appointments cannot be scheduled in the past.

---

### 🩺 4. Medical Records Management
- **Patient Medical Records**:
  - Doctors can add, view, and update patients’ medical records, including:
    - Diagnosis
    - Treatment plan
- **Appointment Outcomes**:
  - Doctors can record outcomes of appointments, including:
    - Service Type (e.g., Blood Test)
    - Consultation Notes (e.g., Pale Skin)
    - Prescribed Medications, Quantity, and Medication Status.
  - Doctors can later update medical records with updated diagnosis and treatment plans, for example:
    - **Diagnosis**: Low Haemoglobin
    - **Treatment Plan**: Medication (e.g., iron supplements).

---

### 💊 5. Inventory and Replenishment Management
- **Medicine Inventory**:
  - Administrators can view, add, update, or remove medicines from the inventory.
  - Prescribed Medicines that are not in the inventory, can be added **implicitly** (via replenishment requests) 
- **Low Stock Alerts**:
  - When Pharmacist tries to dispense medicines, the system alerts them when stock levels fall below the threshold.
  - Pharmacists can then send replenishment requests to the administrator for that medicine. 
- **Replenishment Requests**:
  - Pharmacists can submit requests for medicines that are out of stock or low in quantity.
  - Administrators can approve or decline replenishment requests and update the inventory accordingly.

---

## 📋 **Core Functionalities**
- **Dynamic Inventory Management**: Add or replenish medicines on demand.
- **Flexible Appointment Booking**: Validate and schedule appointments seamlessly.
- **Billing System**: Generate detailed bills for patients.
- **Medical Records Management**: Maintain a comprehensive history of patient treatments.
- **User Login**: Each user is authenticated through secure login. All staff members and patients can change their passwords. 

---

## 🛠️ Setup Instructions

### 1️⃣ **Unpack the Project**
Download and unzip the project package. Ensure the following essential files are available in the project directory:
- `staff.txt`
- `patients.txt`
- `inventory.txt`

### 2️⃣ **Pre-Populated Data**
- **Staff Data**: Includes pre-defined roles for easy testing (doctors, administrator, etc.).
- **Inventory Data**: Some medicines are pre-added for demonstration purposes.
- **Patient Records**: Contains sample patient data for testing.

### 3️⃣ **Administrator Login**
Use the following credentials to access the administrator account:
- **User ID**: `1`
- **Password**: `admin123`

### 4️⃣ **Run the Program**
Launch the main file (e.g., `Main.java`) to initialize the system. This will load all pre-existing data.

---

## 🚀 Usage Guide

### 🔑 Login
- Log in as different roles using credentials provided in `staff.txt` or the administrator account to explore functionalities.

### 🏥 Role-Specific Operations
- **Administrator**: Add staff, manage inventory, approve replenishment requests.
- **Pharmacist**: Notify and request replenishment for out-of-stock medicines.
- **Receptionist**: Manage patient interactions, billing, and appointment schedules.
- **Doctor**: Access and update patient medical records.

### 📝 Adding New Data
- Staff and inventory can be dynamically updated via the Administrator's role.

---


## 📜 License
This project is developed as part of the **SC2002 Object-Oriented Design & Programming** module at **Nanyang Technological University (NTU)** and is intended for educational purposes only.

---
