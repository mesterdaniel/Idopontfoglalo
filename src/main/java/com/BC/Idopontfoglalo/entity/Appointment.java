package com.BC.Idopontfoglalo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Az időpont címe/neve

    @Column(length = 500)
    private String description; // Részletes leírás

    @Column(name = "appointment_date", nullable = false)
    private LocalDateTime appointmentDate; // Mikor lesz az időpont

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes; // Mennyi ideig tart (percben)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status; // Az időpont állapota

    // Kapcsolat a User entitással - ki foglalta le
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Konstruktorok
    public Appointment() {
        this.createdAt = LocalDateTime.now();
        this.status = AppointmentStatus.PENDING;
    }

    public Appointment(String title, String description, LocalDateTime appointmentDate,
                       Integer durationMinutes, User user) {
        this();
        this.title = title;
        this.description = description;
        this.appointmentDate = appointmentDate;
        this.durationMinutes = durationMinutes;
        this.user = user;
    }

    // Getterek és Setterek
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    // Kényelmi metódusok
    public LocalDateTime getEndTime() {
        return appointmentDate.plusMinutes(durationMinutes);
    }

    public boolean isActive() {
        return status == AppointmentStatus.CONFIRMED &&
                appointmentDate.isAfter(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", appointmentDate=" + appointmentDate +
                ", status=" + status +
                '}';
    }
}

