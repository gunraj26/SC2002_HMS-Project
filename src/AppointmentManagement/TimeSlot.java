package AppointmentManagement;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a time slot for appointments, including its date, start and end time, and availability status.
 */
public class TimeSlot {
    private LocalDate date; // The date of the time slot
    private LocalTime startTime; // The start time of the time slot
    private LocalTime endTime; // The end time of the time slot
    private SlotStatus status; // The status of the time slot (e.g., AVAILABLE, UNAVAILABLE, BOOKED)

    /**
     * Enum representing the status of a time slot.
     */
    public enum SlotStatus {
        AVAILABLE, UNAVAILABLE, BOOKED
    }

    /**
     * Constructs a TimeSlot object with the specified date, start time, and end time.
     * The default status is set to AVAILABLE.
     *
     * @param date      The date of the time slot.
     * @param startTime The start time of the time slot.
     * @param endTime   The end time of the time slot.
     */
    public TimeSlot(LocalDate date, LocalTime startTime, LocalTime endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = SlotStatus.AVAILABLE; // Default status is AVAILABLE
    }

    /**
     * Retrieves the date of the time slot.
     *
     * @return The date of the time slot.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Retrieves the start time of the time slot.
     *
     * @return The start time of the time slot.
     */
    public LocalTime getStartTime() {
        return startTime;
    }

    /**
     * Retrieves the end time of the time slot.
     *
     * @return The end time of the time slot.
     */
    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * Retrieves the current status of the time slot.
     *
     * @return The status of the time slot.
     */
    public SlotStatus getStatus() {
        return status;
    }

    /**
     * Sets the status of the time slot.
     *
     * @param status The new status of the time slot (e.g., AVAILABLE, UNAVAILABLE, BOOKED).
     */
    public void setStatus(SlotStatus status) {
        this.status = status;
    }

    /**
     * Generates a list of 30-minute time slots for a given date.
     * The slots range from 09:00 to 17:00, with lunch slots (13:00 to 13:30) marked as UNAVAILABLE.
     *
     * @param date The date for which to generate the time slots.
     * @return A list of generated TimeSlot objects.
     */
    public static List<TimeSlot> generateDailySlots(LocalDate date) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime[] startTimes = {
                LocalTime.of(9, 0),
                LocalTime.of(9, 30),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                LocalTime.of(11, 0),
                LocalTime.of(11, 30),
                LocalTime.of(12, 0),
                LocalTime.of(12, 30),
                LocalTime.of(13, 0),  // Lunch slot
                LocalTime.of(13, 30), // Lunch slot
                LocalTime.of(14, 0),
                LocalTime.of(14, 30),
                LocalTime.of(15, 0),
                LocalTime.of(15, 30),
                LocalTime.of(16, 0),
                LocalTime.of(16, 30)
        };

        for (LocalTime startTime : startTimes) {
            LocalTime endTime = startTime.plusMinutes(30);
            TimeSlot slot = new TimeSlot(date, startTime, endTime);
            if (startTime.equals(LocalTime.of(13, 0)) || startTime.equals(LocalTime.of(13, 30))) {
                slot.setStatus(SlotStatus.UNAVAILABLE); // Mark lunch slots as UNAVAILABLE
            }
            slots.add(slot);
        }
        return slots;
    }

    /**
     * Returns a string representation of the time slot, including its date, start time, end time, and status.
     *
     * @return A formatted string representation of the time slot.
     */
    @Override
    public String toString() {
        return String.format("| %-10s | %-5s | %-5s | %-10s |", date, startTime, endTime, status);
    }
}
