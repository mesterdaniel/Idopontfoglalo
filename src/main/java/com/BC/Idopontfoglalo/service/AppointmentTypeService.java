package com.BC.Idopontfoglalo.service;

import com.BC.Idopontfoglalo.entity.AppointmentType;
import com.BC.Idopontfoglalo.entity.Department;
import com.BC.Idopontfoglalo.entity.User;
import com.BC.Idopontfoglalo.repository.AppointmentTypeRepository;
import com.BC.Idopontfoglalo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AppointmentTypeService {

    @Autowired
    private AppointmentTypeRepository appointmentTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentService departmentService;

    // ========== IDŐPONT TÍPUS MŰVELETEK ==========

    /**
     * Új időpont típus létrehozása (department admin)
     */
    public AppointmentType createAppointmentType(String name, String description,
                                                 Integer defaultDurationMinutes, Integer maxParticipants,
                                                 Integer bufferMinutes, boolean requiresApproval, Long departmentId) {




        User currentUser = getCurrentUser();
        Department department;

        // SUPER_ADMIN esetén a departmentId alapján keressük a részleget
        if (currentUser.isSuperAdmin()||currentUser.hasAdminRole()) {
            department = departmentService.getDepartmentById(departmentId);
        }
        // DEPARTMENT_ADMIN esetén a saját részlegét használjuk
        else {
            department = getCurrentUserDepartment();
            // Opcionális: ellenőrizhetjük, hogy egyezik-e a departmentId
            if (!department.getId().equals(departmentId)) {
                throw new IllegalArgumentException("Nincs jogosultságod ehhez a részleghez!");
            }
        }

        // Ellenőrizzük, hogy van-e már ilyen nevű típus ebben a részlegben
        if (appointmentTypeRepository.existsByNameAndDepartment(name, department)) {
            throw new IllegalArgumentException("Már létezik időpont típus ezzel a névvel ebben a részlegben: " + name);
        }



        // Validációk
        validateAppointmentTypeData(name, defaultDurationMinutes, maxParticipants);

        AppointmentType appointmentType = new AppointmentType();
        appointmentType.setName(name);
        appointmentType.setDescription(description);
        appointmentType.setDefaultDurationMinutes(defaultDurationMinutes);
        appointmentType.setMaxParticipants(maxParticipants);
        appointmentType.setBufferMinutes(bufferMinutes != null ? bufferMinutes : 0);
        appointmentType.setRequiresApproval(requiresApproval);
        appointmentType.setDepartment(department);

        return appointmentTypeRepository.save(appointmentType);
    }

    /**
     * Időpont típus frissítése
     */
    public AppointmentType updateAppointmentType(Long appointmentTypeId, String name, String description,
                                                 Integer defaultDurationMinutes, Integer maxParticipants,
                                                 Integer bufferMinutes, boolean requiresApproval) {
        AppointmentType appointmentType = getAppointmentTypeById(appointmentTypeId);

        // Jogosultság ellenőrzése
        validateUserCanManageAppointmentType(appointmentType);

        // Név egyediség ellenőrzése (csak ha változott a név)
        if (!appointmentType.getName().equals(name) &&
                appointmentTypeRepository.existsByNameAndDepartment(name, appointmentType.getDepartment())) {
            throw new IllegalArgumentException("Már létezik időpont típus ezzel a névvel ebben a részlegben: " + name);
        }

        // Validációk
        validateAppointmentTypeData(name, defaultDurationMinutes, maxParticipants);

        appointmentType.setName(name);
        appointmentType.setDescription(description);
        appointmentType.setDefaultDurationMinutes(defaultDurationMinutes);
        appointmentType.setMaxParticipants(maxParticipants);
        appointmentType.setBufferMinutes(bufferMinutes != null ? bufferMinutes : 0);
        appointmentType.setRequiresApproval(requiresApproval);

        return appointmentTypeRepository.save(appointmentType);
    }

    /**
     * Időpont típus aktiválása/deaktiválása
     */
    public void toggleAppointmentTypeStatus(Long appointmentTypeId) {
        AppointmentType appointmentType = getAppointmentTypeById(appointmentTypeId);
        validateUserCanManageAppointmentType(appointmentType);

        appointmentType.setActive(!appointmentType.isActive());
        appointmentTypeRepository.save(appointmentType);
    }

    /**
     * Időpont típus törlése
     */
    public void deleteAppointmentType(Long appointmentTypeId) {
        AppointmentType appointmentType = getAppointmentTypeById(appointmentTypeId);
        validateUserCanManageAppointmentType(appointmentType);

        // Ellenőrizzük, hogy vannak-e aktív időpontok
        if (!appointmentType.getAppointments().isEmpty()) {
            throw new IllegalArgumentException("Az időpont típus nem törölhető, mert vannak hozzá kapcsolódó időpontok!");
        }

        appointmentTypeRepository.delete(appointmentType);
    }

    // ========== LEKÉRDEZÉS MŰVELETEK ==========

    /**
     * Összes aktív időpont típus
     */
    public List<AppointmentType> getAllActiveAppointmentTypes() {
        return appointmentTypeRepository.findByActiveTrue();
    }

    /**
     * Egy részleg aktív időpont típusai
     */
    public List<AppointmentType> getActiveAppointmentTypesByDepartment(Department department) {
        return appointmentTypeRepository.findByDepartmentAndActiveTrueOrderByNameAsc(department);
    }

    /**
     * Aktuális felhasználó részlegének időpont típusai
     */
    public List<AppointmentType> getCurrentUserDepartmentAppointmentTypes() {
        Department department = getCurrentUserDepartment();
        return appointmentTypeRepository.findByDepartmentOrderByNameAsc(department);
    }

    /**
     * Időpont típus lekérése ID alapján
     */
    public AppointmentType getAppointmentTypeById(Long id) {
        return appointmentTypeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nem található időpont típus ezzel az ID-val: " + id));
    }

    /**
     * Időpont típus lekérése az időpontokkal együtt
     */
    public AppointmentType getAppointmentTypeWithAppointments(Long id) {
        return appointmentTypeRepository.findByIdWithAppointments(id)
                .orElseThrow(() -> new IllegalArgumentException("Nem található időpont típus ezzel az ID-val: " + id));
    }

    // ========== IDŐPONT FOGLALÁSI LOGIKA ==========

    /**
     * Szabad helyek számának lekérése egy adott időpontban
     */
    public int getAvailableSlots(Long appointmentTypeId, LocalDateTime dateTime) {
        AppointmentType appointmentType = getAppointmentTypeById(appointmentTypeId);
        return appointmentType.getAvailableSlots(dateTime);
    }

    /**
     * Ellenőrzi, hogy van-e szabad hely egy adott időpontban
     */
    public boolean hasAvailableSlots(Long appointmentTypeId, LocalDateTime dateTime) {
        AppointmentType appointmentType = getAppointmentTypeById(appointmentTypeId);
        return appointmentType.hasAvailableSlots(dateTime);
    }

    /**
     * Foglalt helyek számának lekérése
     */
    public long getBookedSlots(Long appointmentTypeId, LocalDateTime dateTime) {
        AppointmentType appointmentType = getAppointmentTypeById(appointmentTypeId);
        return appointmentTypeRepository.countBookedSlotsAtDateTime(appointmentType, dateTime);
    }

    /**
     * Következő elérhető időpont keresése
     */
    public LocalDateTime findNextAvailableSlot(Long appointmentTypeId, LocalDateTime startFrom) {
        AppointmentType appointmentType = getAppointmentTypeById(appointmentTypeId);

        LocalDateTime checkTime = startFrom;
        // Legfeljebb 30 napig keresünk
        LocalDateTime maxDate = startFrom.plusDays(30);

        while (checkTime.isBefore(maxDate)) {
            if (hasAvailableSlots(appointmentTypeId, checkTime)) {
                return checkTime;
            }

            // Következő időpont: alapértelmezett időtartam + puffer
            int incrementMinutes = appointmentType.getDefaultDurationMinutes() +
                    appointmentType.getBufferMinutes();
            checkTime = checkTime.plusMinutes(incrementMinutes);
        }

        return null; // Nem található szabad időpont
    }

    // ========== VALIDÁCIÓ ÉS SEGÉD METÓDUSOK ==========

    /**
     * Időpont típus adatok validálása
     */
    private void validateAppointmentTypeData(String name, Integer defaultDurationMinutes, Integer maxParticipants) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Az időpont típus neve nem lehet üres!");
        }

        if (defaultDurationMinutes == null || defaultDurationMinutes <= 0) {
            throw new IllegalArgumentException("Az alapértelmezett időtartam legalább 1 perc kell legyen!");
        }

        if (defaultDurationMinutes > 480) { // 8 óra
            throw new IllegalArgumentException("Az alapértelmezett időtartam maximum 8 óra lehet!");
        }

        if (maxParticipants == null || maxParticipants <= 0) {
            throw new IllegalArgumentException("A maximum résztvevők száma legalább 1 kell legyen!");
        }

        if (maxParticipants > 100) {
            throw new IllegalArgumentException("A maximum résztvevők száma nem lehet több mint 100!");
        }
    }

    /**
     * Ellenőrzi, hogy a felhasználó jogosult-e az időpont típus kezelésére
     */
    private void validateUserCanManageAppointmentType(AppointmentType appointmentType) {
        User currentUser = getCurrentUser();

        // Super admin mindent kezelhet
        if (currentUser.isSuperAdmin()) {
            return;
        }

        // Department admin csak a saját részlegét
        if (currentUser.isDepartmentAdmin()) {
            if (currentUser.getManagedDepartment() == null ||
                    !currentUser.getManagedDepartment().equals(appointmentType.getDepartment())) {
                throw new IllegalArgumentException("Nincs jogosultságod ennek az időpont típusnak a kezeléséhez!");
            }
            return;
        }

        throw new IllegalArgumentException("Nincs jogosultságod időpont típusok kezeléséhez!");
    }

    /**
     * Aktuális felhasználó részlegének lekérése
     */
    private Department getCurrentUserDepartment() {
        User currentUser = getCurrentUser();

        if (currentUser.isDepartmentAdmin()) {
            if (currentUser.getManagedDepartment() == null) {
                throw new IllegalStateException("A department admin felhasználónak nincs hozzárendelt részlege!");
            }
            return currentUser.getManagedDepartment();
        }

        throw new IllegalArgumentException("Csak department admin-ok használhatják ezt a funkciót!");
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
}