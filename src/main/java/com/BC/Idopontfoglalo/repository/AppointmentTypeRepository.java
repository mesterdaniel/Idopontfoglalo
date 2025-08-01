package com.BC.Idopontfoglalo.repository;

import com.BC.Idopontfoglalo.entity.AppointmentType;
import com.BC.Idopontfoglalo.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, Long> {

    /**
     * Aktív időpont típusok lekérése
     */
    List<AppointmentType> findByActiveTrue();

    /**
     * Egy részleg aktív időpont típusai
     */
    List<AppointmentType> findByDepartmentAndActiveTrueOrderByNameAsc(Department department);

    /**
     * Egy részleg összes időpont típusa
     */
    List<AppointmentType> findByDepartmentOrderByNameAsc(Department department);

    /**
     * Időpont típus keresése név és részleg alapján
     */
    Optional<AppointmentType> findByNameAndDepartment(String name, Department department);

    /**
     * Ellenőrzi, hogy létezik-e már ilyen nevű típus az adott részlegben
     */
    boolean existsByNameAndDepartment(String name, Department department);

    /**
     * Aktív időpont típusok száma egy részlegben
     */
    long countByDepartmentAndActiveTrue(Department department);

    /**
     * Időpont típus lekérése a hozzá tartozó időpontokkal
     */
    @Query("SELECT at FROM AppointmentType at LEFT JOIN FETCH at.appointments WHERE at.id = :id")
    Optional<AppointmentType> findByIdWithAppointments(@Param("id") Long id);

    /**
     * Egy adott időpontban foglalt helyek száma lekérése
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE " +
            "a.appointmentType = :appointmentType AND " +
            "a.appointmentDate = :dateTime AND " +
            "(a.status = 'PENDING' OR a.status = 'CONFIRMED')")
    long countBookedSlotsAtDateTime(@Param("appointmentType") AppointmentType appointmentType,
                                    @Param("dateTime") LocalDateTime dateTime);

    /**
     * Szabad helyek számának lekérése egy adott időpontban
     */
    @Query("SELECT (at.maxParticipants - COUNT(a)) FROM AppointmentType at " +
            "LEFT JOIN at.appointments a ON (a.appointmentDate = :dateTime AND " +
            "(a.status = 'PENDING' OR a.status = 'CONFIRMED')) " +
            "WHERE at = :appointmentType " +
            "GROUP BY at.maxParticipants")
    Optional<Integer> getAvailableSlotsAtDateTime(@Param("appointmentType") AppointmentType appointmentType,
                                                  @Param("dateTime") LocalDateTime dateTime);

    Optional<AppointmentType> findById(Long id);
}