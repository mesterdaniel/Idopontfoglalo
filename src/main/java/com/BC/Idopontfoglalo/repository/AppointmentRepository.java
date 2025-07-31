package com.BC.Idopontfoglalo.repository;

import com.BC.Idopontfoglalo.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    long countByAppointmentType_Department(Department department);

    long countByAppointmentType_DepartmentAndStatus(Department department, AppointmentStatus status);
/*
    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.appointmentType.department = :department " +
            "AND a.appointmentDate >= :currentDate " +
            "AND a.status = com.BC.Idopontfoglalo.entity.AppointmentStatus.CONFIRMED")
    long countUpcomingByDepartment(@Param("department") Department department,
                                   @Param("currentDate") LocalDateTime currentDate);
*/
    // Egy adott felhasználó összes időpontja
    List<Appointment> findByUser(User user);

    // Egy adott felhasználó időpontjai státusz szerint
    List<Appointment> findByUserAndStatus(User user, AppointmentStatus status);

    // Időpontok dátum szerint (időrendi sorrendben)
    List<Appointment> findByUserOrderByAppointmentDateAsc(User user);

    // Jövőbeli időpontok egy felhasználónak
    @Query("SELECT a FROM Appointment a WHERE a.user = :user AND a.appointmentDate > :now ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingAppointmentsByUser(@Param("user") User user, @Param("now") LocalDateTime now);
/*
    // Múltbeli időpontok egy felhasználónak
    @Query("SELECT a FROM Appointment a WHERE a.user = :user AND a.appointmentDate < :now ORDER BY a.appointmentDate DESC")
    List<Appointment> findPastAppointmentsByUser(@Param("user") User user, @Param("now") LocalDateTime now);
*/
    // Összes időpont státusz szerint (admin funkcióhoz)
    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    // Időpontok egy adott időintervallumon belül
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startDate AND :endDate ORDER BY a.appointmentDate ASC")
    List<Appointment> findAppointmentsBetweenDates(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    // Ütközés ellenőrzése - egyszerűbb megközelítés
    @Query("SELECT a FROM Appointment a WHERE " +
            "(a.status = 'PENDING' OR a.status = 'CONFIRMED') AND " +
            "a.appointmentDate < :endTime AND " +
            "a.appointmentDate >= :startTime")
    List<Appointment> findConflictingAppointments(@Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    // Mai nap időpontjai
    @Query("SELECT a FROM Appointment a WHERE DATE(a.appointmentDate) = DATE(:date) ORDER BY a.appointmentDate ASC")
    List<Appointment> findAppointmentsByDate(@Param("date") LocalDateTime date);
/*
    // Összes jövőbeli időpont (admin dashboard-hoz)
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate > :now ORDER BY a.appointmentDate ASC")
    List<Appointment> findAllUpcomingAppointments(@Param("now") LocalDateTime now);
*/
    // Függőben lévő időpontok száma (admin értesítéshez)
    long countByStatus(AppointmentStatus status);

    // Új metódusok részleg szerinti statisztikákhoz
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentType.department = :department")
    long countByAppointmentTypeDepartment(@Param("department") Department department);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentType.department = :department AND a.status = :status")
    long countByAppointmentTypeDepartmentAndStatus(@Param("department") Department department,
                                                   @Param("status") AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentType.department = :department AND a.appointmentDate > :date")
    long countByAppointmentTypeDepartmentAndAppointmentDateAfter(@Param("department") Department department,
                                                                 @Param("date") LocalDateTime date);



    //Új


    // ========== ALAPVETŐ LEKÉRDEZÉSEK ==========




    /**
     * Időpontok részleg szerint
     */
    List<Appointment> findByAppointmentType_DepartmentOrderByAppointmentDateAsc(Department department);

    // ========== JÖVŐBELI/MÚLTBELI IDŐPONTOK ==========



    /**
     * Felhasználó múltbeli időpontjai
     */
    @Query("SELECT a FROM Appointment a WHERE a.user = :user AND a.appointmentDate < :now " +
            "ORDER BY a.appointmentDate DESC")
    List<Appointment> findPastAppointmentsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Összes jövőbeli időpont
     */
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate > :now " +
            "AND a.status != 'CANCELLED' ORDER BY a.appointmentDate ASC")
    List<Appointment> findAllUpcomingAppointments(@Param("now") LocalDateTime now);

    /**
     * Felhasználó jövőbeli időpontjai típus információkkal
     */
    @Query("SELECT a FROM Appointment a " +
            "JOIN FETCH a.appointmentType at " +
            "JOIN FETCH at.department " +
            "WHERE a.user = :user AND a.appointmentDate > :now " +
            "AND a.status != 'CANCELLED' " +
            "ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingAppointmentsByUserWithType(@Param("user") User user, @Param("now") LocalDateTime now);

    // ========== IDŐSZAK ALAPÚ LEKÉRDEZÉSEK ==========

    /**
     * Felhasználó időpontjai adott időszakban, státusz kizárásával
     */
    List<Appointment> findByUserAndAppointmentDateBetweenAndStatusNotOrderByAppointmentDateAsc(
            User user,
            LocalDateTime startTime,
            LocalDateTime endTime,
            AppointmentStatus excludeStatus
    );

    /**
     * Időpontok adott időszakban
     */
    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    /**
     * Részleg időpontjai adott időszakban
     */
    @Query("SELECT a FROM Appointment a WHERE a.appointmentType.department = :department " +
            "AND a.appointmentDate BETWEEN :startTime AND :endTime " +
            "ORDER BY a.appointmentDate ASC")
    List<Appointment> findByDepartmentAndDateRange(
            @Param("department") Department department,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // ========== ÜTKÖZÉS ELLENŐRZÉS ==========

    /**
     * Ütköző időpontok keresése

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate < :endTime " +
            "AND FUNCTION('DATE_ADD', a.appointmentDate, FUNCTION('INTERVAL', a.durationMinutes, 'MINUTE')) > :startTime " +
            "AND a.status != 'CANCELLED'")
    List<Appointment> findConflictingAppointments(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
*/
    /**
     * Felhasználó ütköző időpontjainak ellenőrzése
     */
    boolean existsByUserAndAppointmentDateBetweenAndStatusNot(
            User user,
            LocalDateTime startTime,
            LocalDateTime endTime,
            AppointmentStatus status
    );

    // ========== SZÁMLÁLÁSOK ==========



    /**
     * Felhasználó összes időpontjainak száma
     */
    long countByUser(User user);

    /**
     * Felhasználó időpontjainak száma státusz szerint
     */
    long countByUserAndStatus(User user, AppointmentStatus status);

    /**
     * Felhasználó jövőbeli időpontjainak száma
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.user = :user " +
            "AND a.appointmentDate > :now AND a.status != 'CANCELLED'")
    long countUpcomingByUser(@Param("user") User user, @Param("now") LocalDateTime now);





    /**
     * Részleg jövőbeli időpontjainak száma
     */
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentType.department = :department " +
            "AND a.appointmentDate > :now AND a.status != 'CANCELLED'")
    long countUpcomingByDepartment(@Param("department") Department department, @Param("now") LocalDateTime now);

    // ========== SPECIÁLIS LEKÉRDEZÉSEK ==========

    /**
     * Mai időpontok
     */
    @Query("SELECT a FROM Appointment a WHERE DATE(a.appointmentDate) = DATE(:today) " +
            "AND a.status != 'CANCELLED' ORDER BY a.appointmentDate ASC")
    List<Appointment> findTodayAppointments(@Param("today") LocalDateTime today);

    /**
     * Felhasználó mai időpontjai
     */
    @Query("SELECT a FROM Appointment a WHERE a.user = :user " +
            "AND DATE(a.appointmentDate) = DATE(:today) " +
            "AND a.status != 'CANCELLED' ORDER BY a.appointmentDate ASC")
    List<Appointment> findTodayAppointmentsByUser(@Param("user") User user, @Param("today") LocalDateTime today);

    /**
     * Időpont keresése típus és időpont alapján (TimeSlot frissítéséhez)
     */
    @Query("SELECT a FROM Appointment a WHERE a.appointmentType = :type " +
            "AND a.appointmentDate = :dateTime AND a.status != 'CANCELLED'")
    List<Appointment> findByAppointmentTypeAndDateTime(
            @Param("type") AppointmentType appointmentType,
            @Param("dateTime") LocalDateTime dateTime
    );

    /**
     * Hamarosan kezdődő időpontok (emlékeztetőkhöz)
     */
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :now AND :futureTime " +
            "AND a.status = 'CONFIRMED' ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingInTimeRange(
            @Param("now") LocalDateTime now,
            @Param("futureTime") LocalDateTime futureTime
    );

    /**
     * Elmulasztott időpontok (státusz frissítéshez)
     */
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate < :now " +
            "AND a.status IN ('CONFIRMED', 'PENDING') " +
            "ORDER BY a.appointmentDate ASC")
    List<Appointment> findMissedAppointments(@Param("now") LocalDateTime now);

    /**
     * Felhasználó aktív időpontjai (nem lemondott, nem befejezett)
     */
    @Query("SELECT a FROM Appointment a WHERE a.user = :user " +
            "AND a.status NOT IN ('CANCELLED', 'COMPLETED') " +
            "ORDER BY a.appointmentDate ASC")
    List<Appointment> findActiveAppointmentsByUser(@Param("user") User user);

    /**
     * Legutóbbi időpontok (dashboard-hoz)
     */
    @Query("SELECT a FROM Appointment a WHERE a.status != 'CANCELLED' " +
            "ORDER BY a.createdAt DESC")
    List<Appointment> findRecentAppointments();
}
