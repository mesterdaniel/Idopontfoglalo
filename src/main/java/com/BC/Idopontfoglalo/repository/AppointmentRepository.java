package com.BC.Idopontfoglalo.repository;

import com.BC.Idopontfoglalo.entity.Appointment;
import com.BC.Idopontfoglalo.entity.AppointmentStatus;
import com.BC.Idopontfoglalo.entity.Department;
import com.BC.Idopontfoglalo.entity.User;
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

    @Query("SELECT COUNT(a) FROM Appointment a " +
            "WHERE a.appointmentType.department = :department " +
            "AND a.appointmentDate >= :currentDate " +
            "AND a.status = com.BC.Idopontfoglalo.entity.AppointmentStatus.CONFIRMED")
    long countUpcomingByDepartment(@Param("department") Department department,
                                   @Param("currentDate") LocalDateTime currentDate);

    // Egy adott felhasználó összes időpontja
    List<Appointment> findByUser(User user);

    // Egy adott felhasználó időpontjai státusz szerint
    List<Appointment> findByUserAndStatus(User user, AppointmentStatus status);

    // Időpontok dátum szerint (időrendi sorrendben)
    List<Appointment> findByUserOrderByAppointmentDateAsc(User user);

    // Jövőbeli időpontok egy felhasználónak
    @Query("SELECT a FROM Appointment a WHERE a.user = :user AND a.appointmentDate > :now ORDER BY a.appointmentDate ASC")
    List<Appointment> findUpcomingAppointmentsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    // Múltbeli időpontok egy felhasználónak
    @Query("SELECT a FROM Appointment a WHERE a.user = :user AND a.appointmentDate < :now ORDER BY a.appointmentDate DESC")
    List<Appointment> findPastAppointmentsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

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

    // Összes jövőbeli időpont (admin dashboard-hoz)
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate > :now ORDER BY a.appointmentDate ASC")
    List<Appointment> findAllUpcomingAppointments(@Param("now") LocalDateTime now);

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

}
