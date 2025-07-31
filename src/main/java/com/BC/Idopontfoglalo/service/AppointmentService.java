package com.BC.Idopontfoglalo.service;

import com.BC.Idopontfoglalo.entity.*;
import com.BC.Idopontfoglalo.repository.AppointmentRepository;
import com.BC.Idopontfoglalo.repository.AvailableTimeSlotRepository;
import com.BC.Idopontfoglalo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Transactional
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentTypeService appointmentTypeService;

    @Autowired
    private AvailableTimeSlotRepository availableTimeSlotRepository;

    // ========== FELHASZNÁLÓI MŰVELETEK ==========

    /**
     * Új időpont létrehozása
     */
    public Appointment createAppointment(String title, String description,
                                         LocalDateTime appointmentDate, Integer durationMinutes, AppointmentType appointmentType) {
        // Aktuális felhasználó lekérése
        User currentUser = getCurrentUser();

        // Validációk
        validateAppointmentData(title, appointmentDate, durationMinutes);

        // Ütközés ellenőrzése
        LocalDateTime endTime = appointmentDate.plusMinutes(durationMinutes);
        if (hasConflictingAppointment(appointmentDate, endTime)) {
            throw new IllegalArgumentException("Az adott időpontban már van foglalás!");
        }

        // Új időpont létrehozása
        Appointment appointment = new Appointment(title, description, appointmentDate, durationMinutes, currentUser, appointmentType);

        return appointmentRepository.save(appointment);
    }

    /**
     * Felhasználó saját időpontjainak lekérése
     */
    public List<Appointment> getUserAppointments() {
        User currentUser = getCurrentUser();
        return appointmentRepository.findByUserOrderByAppointmentDateAsc(currentUser);
    }

    /**
     * Felhasználó jövőbeli időpontjai
     */
    public List<Appointment> getUserUpcomingAppointments() {
        User currentUser = getCurrentUser();
        return appointmentRepository.findUpcomingAppointmentsByUser(currentUser, LocalDateTime.now());
    }

    /**
     * Felhasználó múltbeli időpontjai
     */
    public List<Appointment> getUserPastAppointments() {
        User currentUser = getCurrentUser();
        return appointmentRepository.findPastAppointmentsByUser(currentUser, LocalDateTime.now());
    }

    /**
     * Időpont lemondása (csak saját időpont)
     */
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);
        User currentUser = getCurrentUser();

        // Ellenőrizzük, hogy a felhasználó saját időpontját akarja lemondani
        if (!appointment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Csak saját időpontot lehet lemondani!");
        }

        // Csak aktív időpontot lehet lemondani
        if (appointment.getStatus() == AppointmentStatus.CANCELLED ||
                appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalArgumentException("Ez az időpont már nem mondható le!");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    /**
     * Időpont módosítása (csak saját időpont)
     */
    public Appointment updateAppointment(Long appointmentId, String title, String description,
                                         LocalDateTime appointmentDate, Integer durationMinutes) {
        Appointment appointment = getAppointmentById(appointmentId);
        User currentUser = getCurrentUser();

        // Ellenőrizzük a jogosultságot
        if (!appointment.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Csak saját időpontot lehet módosítani!");
        }

        // Validációk
        validateAppointmentData(title, appointmentDate, durationMinutes);

        // Ütközés ellenőrzése (kivéve saját időpont)
        LocalDateTime endTime = appointmentDate.plusMinutes(durationMinutes);
        if (!appointment.getAppointmentDate().equals(appointmentDate) ||
                !appointment.getDurationMinutes().equals(durationMinutes)) {

            if (hasConflictingAppointment(appointmentDate, endTime, appointmentId)) {
                throw new IllegalArgumentException("Az adott időpontban már van foglalás!");
            }
        }

        // Módosítások alkalmazása
        appointment.setTitle(title);
        appointment.setDescription(description);
        appointment.setAppointmentDate(appointmentDate);
        appointment.setDurationMinutes(durationMinutes);
        appointment.setUpdatedAt(LocalDateTime.now());

        return appointmentRepository.save(appointment);
    }

    // ========== ADMIN MŰVELETEK ==========

    /**
     * Összes időpont lekérése (admin)
     */
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    /**
     * Összes jövőbeli időpont (admin)
     */
    public List<Appointment> getAllUpcomingAppointments() {
        return appointmentRepository.findAllUpcomingAppointments(LocalDateTime.now());
    }

    /**
     * Függőben lévő időpontok (admin jóváhagyáshoz)
     */
    public List<Appointment> getPendingAppointments() {
        return appointmentRepository.findByStatusOrderByAppointmentDateAsc(AppointmentStatus.PENDING);
    }

    /**
     * Időpont jóváhagyása (admin)
     */
    public void approveAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);
    }

    /**
     * Időpont elutasítása (admin)
     */
    public void rejectAppointment(Long appointmentId) {
        Appointment appointment = getAppointmentById(appointmentId);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    /**
     * Függőben lévő időpontok száma (dashboard-hoz)
     */
    public long getPendingAppointmentsCount() {
        return appointmentRepository.countByStatus(AppointmentStatus.PENDING);
    }

    // ========== SEGÉD METÓDUSOK ==========

    /**
     * Időpont lekérése ID alapján
     */
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nem található időpont ezzel az ID-val: " + id));
    }

    /**
     * Aktuális bejelentkezett felhasználó lekérése
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Nem található felhasználó: " + username));
    }

    /**
     * Ütközés ellenőrzése - van-e már időpont ebben az időszakban
     */
    private boolean hasConflictingAppointment(LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> conflicting = appointmentRepository.findConflictingAppointments(startTime, endTime);

        // Részletes ellenőrzés: valóban ütközik-e az időpont
        for (Appointment existing : conflicting) {
            LocalDateTime existingEnd = existing.getAppointmentDate().plusMinutes(existing.getDurationMinutes());

            // Ha az időpontok átfednek
            if (startTime.isBefore(existingEnd) && endTime.isAfter(existing.getAppointmentDate())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ütközés ellenőrzése módosításkor (saját időpontot kizárva)
     */
    private boolean hasConflictingAppointment(LocalDateTime startTime, LocalDateTime endTime, Long excludeId) {
        List<Appointment> conflicting = appointmentRepository.findConflictingAppointments(startTime, endTime);

        for (Appointment existing : conflicting) {
            // Saját időpontot kihagyjuk
            if (existing.getId().equals(excludeId)) {
                continue;
            }

            LocalDateTime existingEnd = existing.getAppointmentDate().plusMinutes(existing.getDurationMinutes());

            if (startTime.isBefore(existingEnd) && endTime.isAfter(existing.getAppointmentDate())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Időpont adatok validálása
     */
    private void validateAppointmentData(String title, LocalDateTime appointmentDate, Integer durationMinutes) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Az időpont címe nem lehet üres!");
        }

        if (appointmentDate == null) {
            throw new IllegalArgumentException("Az időpont dátuma kötelező!");
        }

        if (appointmentDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Nem lehet múltbeli időpontra foglalni!");
        }

        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Az időtartam legalább 1 perc kell legyen!");
        }

        if (durationMinutes > 480) { // 8 óra
            throw new IllegalArgumentException("Az időtartam maximum 8 óra lehet!");
        }
    }

    /**
     * Count all appointments for a specific department
     */
    public long countByDepartment(Department department) {
        return appointmentRepository.countByAppointmentType_Department(department);
    }

    /**
     * Count pending appointments for a specific department
     */
    public long countPendingByDepartment(Department department) {
        return appointmentRepository.countByAppointmentType_DepartmentAndStatus(
                department,
                AppointmentStatus.PENDING
        );
    }

    /**
     * Count upcoming appointments for a specific department
     */
    public long countUpcomingByDepartment(Department department) {
        return appointmentRepository.countUpcomingByDepartment(
                department,
                LocalDateTime.now()
        );
    }

    @Transactional
    public void createBulkAppointments(Long appointmentTypeId, LocalDate startDate, LocalDate endDate,
                                       LocalTime startTime, LocalTime endTime, Integer durationMinutes,
                                       List<DayOfWeek> selectedDays) {

        AppointmentType appointmentType = appointmentTypeService.getAppointmentTypeById(appointmentTypeId);

        // Ellenőrizzük, hogy a részleg aktív-e
        if (!appointmentType.getDepartment().isActive()) {
            throw new IllegalArgumentException("Inaktív részleghez nem lehet időpontot létrehozni!");
        }

        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // Ellenőrizzük, hogy ez a nap szerepel-e a kiválasztott napok között
            if (selectedDays.contains(currentDate.getDayOfWeek())) {
                createAppointmentsForDay(appointmentType, currentDate, startTime, endTime, durationMinutes);
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    private void createAppointmentsForDay(AppointmentType appointmentType, LocalDate date,
                                          LocalTime startTime, LocalTime endTime, Integer durationMinutes) {

        LocalTime currentTime = startTime;

        while (currentTime.plusMinutes(durationMinutes).isBefore(endTime) ||
                currentTime.plusMinutes(durationMinutes).equals(endTime)) {

            LocalDateTime appointmentDateTime = LocalDateTime.of(date, currentTime);

            // Ellenőrizzük, hogy nincs-e már időpont ebben az időszakban
            if (!hasConflictingAppointment(appointmentDateTime,
                    appointmentDateTime.plusMinutes(durationMinutes))) {

                // Üres időpont létrehozása (nincs hozzárendelve felhasználó)
                AvailableTimeSlot timeSlot = new AvailableTimeSlot();
                timeSlot.setAppointmentType(appointmentType);
                timeSlot.setStartTime(appointmentDateTime);
                timeSlot.setDurationMinutes(durationMinutes);
                timeSlot.setMaxAttendees(appointmentType.getMaxParticipants());
                timeSlot.setCurrentAttendees(0);
                timeSlot.setStatus(TimeSlotStatus.AVAILABLE);

                availableTimeSlotRepository.save(timeSlot);
            }

            currentTime = currentTime.plusMinutes(durationMinutes);
        }
    }

    public Map<String, List<AppointmentSlotDTO>> getWeeklyAppointmentSlots(Long departmentId, LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);

        List<AvailableTimeSlot> timeSlots = availableTimeSlotRepository
                .findByAppointmentType_Department_IdAndStartTimeBetween(
                        departmentId,
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59)
                );

        Map<String, List<AppointmentSlotDTO>> result = new HashMap<>();

        // Initialize all days
        String[] days = {"monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday"};
        for (String day : days) {
            result.put(day, new ArrayList<>());
        }

        // Group by day of week
        for (AvailableTimeSlot slot : timeSlots) {
            DayOfWeek dayOfWeek = slot.getStartTime().getDayOfWeek();
            String dayKey = dayOfWeek.name().toLowerCase();

            AppointmentSlotDTO dto = new AppointmentSlotDTO();
            dto.setId(slot.getId());
            dto.setStartTime(slot.getStartTime());
            dto.setEndTime(slot.getStartTime().plusMinutes(slot.getDurationMinutes()));
            dto.setTypeName(slot.getAppointmentType().getName());
            dto.setMaxAttendees(slot.getMaxAttendees());
            dto.setCurrentAttendees(slot.getCurrentAttendees());
            dto.setStatus(slot.getStatus().name());

            result.get(dayKey).add(dto);
        }

        // Sort each day's appointments by time
        for (List<AppointmentSlotDTO> daySlots : result.values()) {
            daySlots.sort(Comparator.comparing(AppointmentSlotDTO::getStartTime));
        }

        return result;
    }


}