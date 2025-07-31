package com.BC.Idopontfoglalo.repository;

import com.BC.Idopontfoglalo.entity.AppointmentType;
import com.BC.Idopontfoglalo.entity.AvailableTimeSlot;
import com.BC.Idopontfoglalo.entity.TimeSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AvailableTimeSlotRepository extends JpaRepository<AvailableTimeSlot, Long> {

    List<AvailableTimeSlot> findByAppointmentType_Department_IdAndStartTimeBetween(
            Long departmentId, LocalDateTime start, LocalDateTime end);

    List<AvailableTimeSlot> findByStartTimeBetweenAndStatus(
            LocalDateTime start, LocalDateTime end, TimeSlotStatus status);

    boolean existsByAppointmentType_IdAndStartTime(Long appointmentTypeId, LocalDateTime startTime);


    /**
     * Elérhető időpont slot-ok egy adott részlegben
     */
    @Query("SELECT ats FROM AvailableTimeSlot ats " +
            "WHERE ats.appointmentType.department.id = :departmentId " +
            "AND ats.startTime BETWEEN :startTime AND :endTime " +
            "AND ats.status = 'AVAILABLE' " +
            "AND ats.appointmentType.active = true " +
            "AND ats.appointmentType.department.active = true " +
            "ORDER BY ats.startTime ASC")
    List<AvailableTimeSlot> findAvailableSlotsByDepartmentAndTimeRange(
            @Param("departmentId") Long departmentId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Időpont slot keresése típus és időpont alapján
     */
    List<AvailableTimeSlot> findByAppointmentTypeAndStartTime(
            AppointmentType appointmentType,
            LocalDateTime startTime
    );

    /**
     * Jövőbeli elérhető slot-ok egy adott típushoz
     */
    @Query("SELECT ats FROM AvailableTimeSlot ats " +
            "WHERE ats.appointmentType = :appointmentType " +
            "AND ats.startTime > :now " +
            "AND ats.status = 'AVAILABLE' " +
            "ORDER BY ats.startTime ASC")
    List<AvailableTimeSlot> findUpcomingAvailableSlotsByType(
            @Param("appointmentType") AppointmentType appointmentType,
            @Param("now") LocalDateTime now
    );

    /**
     * Slot-ok száma státusz alapján
     */
    long countByAppointmentType_Department_IdAndStatus(
            Long departmentId,
            TimeSlotStatus status
    );

    /**
     * Ma elérhető slot-ok
     */
    @Query("SELECT ats FROM AvailableTimeSlot ats " +
            "WHERE DATE(ats.startTime) = DATE(:today) " +
            "AND ats.status = 'AVAILABLE' " +
            "AND ats.appointmentType.department.active = true " +
            "ORDER BY ats.startTime ASC")
    List<AvailableTimeSlot> findTodayAvailableSlots(@Param("today") LocalDateTime today);
}