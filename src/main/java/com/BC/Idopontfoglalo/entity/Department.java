package com.BC.Idopontfoglalo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // Részleg neve pl. "Orvosi rendelő"

    @Column(length = 500)
    private String description; // Leírás

    @Column(nullable = false)
    private boolean active = true; // Aktív-e a részleg

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Department admin felhasználók
    @OneToMany(mappedBy = "managedDepartment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<User> departmentAdmins = new HashSet<>();

    // Időpont típusok
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<AppointmentType> appointmentTypes = new HashSet<>();

    // Konstruktorok
    public Department() {
        this.createdAt = LocalDateTime.now();
    }

    public Department(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    // Getterek és Setterek
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public Set<User> getDepartmentAdmins() {
        return departmentAdmins;
    }

    public void setDepartmentAdmins(Set<User> departmentAdmins) {
        this.departmentAdmins = departmentAdmins;
    }

    public Set<AppointmentType> getAppointmentTypes() {
        return appointmentTypes;
    }

    public void setAppointmentTypes(Set<AppointmentType> appointmentTypes) {
        this.appointmentTypes = appointmentTypes;
    }

    // Kényelmi metódusok
    public void addDepartmentAdmin(User admin) {
        departmentAdmins.add(admin);
        admin.setManagedDepartment(this);
    }

    public void removeDepartmentAdmin(User admin) {
        departmentAdmins.remove(admin);
        admin.setManagedDepartment(null);
    }

    public void addAppointmentType(AppointmentType appointmentType) {
        appointmentTypes.add(appointmentType);
        appointmentType.setDepartment(this);
    }

    public void removeAppointmentType(AppointmentType appointmentType) {
        appointmentTypes.remove(appointmentType);
        appointmentType.setDepartment(null);
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}