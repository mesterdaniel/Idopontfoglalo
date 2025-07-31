package com.BC.Idopontfoglalo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "appointment_types")
public class AppointmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(name = "default_duration_minutes", nullable = false)
    private Integer defaultDurationMinutes;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "buffer_minutes")
    private Integer bufferMinutes = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "requires_approval")
    private boolean requiresApproval = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @OneToMany(mappedBy = "appointmentType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    public AppointmentType() {
        this.createdAt = LocalDateTime.now();
    }

    public AppointmentType(String name, String description, Integer defaultDurationMinutes,
                           Integer maxParticipants, Department department) {
        this();
        this.name = name;
        this.description = description;
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.maxParticipants = maxParticipants;
        this.department = department;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getDefaultDurationMinutes() {
        return defaultDurationMinutes;
    }

    public void setDefaultDurationMinutes(Integer defaultDurationMinutes) {
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getBufferMinutes() {
        return bufferMinutes;
    }

    public void setBufferMinutes(Integer bufferMinutes) {
        this.bufferMinutes = bufferMinutes;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isRequiresApproval() {
        return requiresApproval;
    }

    public void setRequiresApproval(boolean requiresApproval) {
        this.requiresApproval = requiresApproval;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    // Convenience methods
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appointment.setAppointmentType(this);
    }

    public void removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
        appointment.setAppointmentType(null);
    }

    public boolean hasAvailableSlots(LocalDateTime dateTime) {
        long currentBookings = appointments.stream()
                .filter(a -> a.getAppointmentDate().equals(dateTime))
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING ||
                        a.getStatus() == AppointmentStatus.CONFIRMED)
                .count();

        return currentBookings < maxParticipants;
    }

    public int getAvailableSlots(LocalDateTime dateTime) {
        long currentBookings = appointments.stream()
                .filter(a -> a.getAppointmentDate().equals(dateTime))
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING ||
                        a.getStatus() == AppointmentStatus.CONFIRMED)
                .count();

        return Math.max(0, maxParticipants - (int)currentBookings);
    }

    @Override
    public String toString() {
        return "AppointmentType{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", maxParticipants=" + maxParticipants +
                ", active=" + active +
                '}';
    }
}