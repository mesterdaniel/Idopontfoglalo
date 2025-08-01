package com.BC.Idopontfoglalo.entity;

import jakarta.persistence.*;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;
    private String password;
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    // Időpontokhoz való kapcsolat
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Appointment> appointments = new ArrayList<>();

    // ÚJ: Department admin kapcsolat
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "managed_department_id")
    private Department managedDepartment; // Ha ez a user egy department admin

    // Konstruktorok
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.enabled = true;
    }

    // Getterek és Setterek
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

    // ÚJ: Department management
    public Department getManagedDepartment() {
        return managedDepartment;
    }

    public void setManagedDepartment(Department managedDepartment) {
        this.managedDepartment = managedDepartment;
    }

    // Kényelmi metódusok
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appointment.setUser(this);
    }

    public void removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
        appointment.setUser(null);
    }

    /**
     * Ellenőrzi, hogy a felhasználó főadmin-e
     */
    public boolean isSuperAdmin() {
        return roles.stream()
                .anyMatch(role -> "ROLE_SUPER_ADMIN".equals(role.getRoleName()));
    }

    /**
     * Ellenőrzi, hogy a felhasználó department admin-e
     */
    public boolean isDepartmentAdmin() {
        return roles.stream()
                .anyMatch(role -> "ROLE_DEPARTMENT_ADMIN".equals(role.getRoleName()))
                && managedDepartment != null;
    }

    /**
     * Ellenőrzi, hogy a felhasználó admin jogosultságokkal rendelkezik-e
     */
    public boolean hasAdminRole() {
        return isSuperAdmin() || isDepartmentAdmin() ||
                roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getRoleName()));
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", enabled=" + enabled +
                ", managedDepartment=" + (managedDepartment != null ? managedDepartment.getName() : "none") +
                '}';
    }
}