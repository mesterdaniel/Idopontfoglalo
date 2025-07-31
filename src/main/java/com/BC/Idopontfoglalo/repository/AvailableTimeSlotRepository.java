package com.BC.Idopontfoglalo.repository;

import com.BC.Idopontfoglalo.entity.AvailableTimeSlot;
import com.BC.Idopontfoglalo.entity.TimeSlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
}