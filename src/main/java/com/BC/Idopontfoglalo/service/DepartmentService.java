package com.BC.Idopontfoglalo.service;

import com.BC.Idopontfoglalo.entity.Department;
import com.BC.Idopontfoglalo.entity.AppointmentStatus;
import com.BC.Idopontfoglalo.entity.User;
import com.BC.Idopontfoglalo.entity.Role;
import com.BC.Idopontfoglalo.repository.DepartmentRepository;
import com.BC.Idopontfoglalo.repository.UserRepository;
import com.BC.Idopontfoglalo.repository.RoleRepository;
import com.BC.Idopontfoglalo.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AppointmentRepository appointmentRepository;

    // ========== DEPARTMENT MŰVELETEK ==========

    public Department createDepartment(String name, String description) {
        if (departmentRepository.existsByName(name)) {
            throw new IllegalArgumentException("Már létezik részleg ezzel a névvel: " + name);
        }

        Department department = new Department(name, description);
        return departmentRepository.save(department);
    }

    public Department updateDepartment(Long departmentId, String name, String description) {
        Department department = getDepartmentById(departmentId);

        if (!department.getName().equals(name) && departmentRepository.existsByName(name)) {
            throw new IllegalArgumentException("Már létezik részleg ezzel a névvel: " + name);
        }

        department.setName(name);
        department.setDescription(description);
        return departmentRepository.save(department);
    }

    public void toggleDepartmentStatus(Long departmentId) {
        Department department = getDepartmentById(departmentId);
        department.setActive(!department.isActive());
        departmentRepository.save(department);
    }

    public User createOrAssignDepartmentAdmin(Long departmentId, String username, String password, boolean createNew) {
        Department department = getDepartmentById(departmentId);
        Role departmentAdminRole = roleRepository.findByRoleName("ROLE_DEPARTMENT_ADMIN");

        if (departmentAdminRole == null) {
            throw new IllegalStateException("ROLE_DEPARTMENT_ADMIN szerepkör nem található!");
        }

        User admin;

        if (createNew) {
            if (userRepository.findByUsername(username).isPresent()) {
                throw new IllegalArgumentException("Már létezik felhasználó ezzel a névvel: " + username);
            }

            admin = new User();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(password));
            admin.setEnabled(true);
            admin.setRoles(Collections.singleton(departmentAdminRole));
        } else {
            admin = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Nem található felhasználó: " + username));

            if (admin.getManagedDepartment() != null) {
                throw new IllegalArgumentException("Ez a felhasználó már adminisztrálja a(z) " +
                        admin.getManagedDepartment().getName() + " részleget!");
            }

            admin.getRoles().add(departmentAdminRole);
        }

        admin.setManagedDepartment(department);
        return userRepository.save(admin);
    }

    public void removeDepartmentAdmin(Long departmentId, Long adminId) {
        Department department = getDepartmentById(departmentId);
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Nem található admin felhasználó"));

        if (!department.equals(admin.getManagedDepartment())) {
            throw new IllegalArgumentException("Ez a felhasználó nem adminisztrálja ezt a részleget!");
        }

        admin.setManagedDepartment(null);

        Role departmentAdminRole = roleRepository.findByRoleName("ROLE_DEPARTMENT_ADMIN");
        admin.getRoles().remove(departmentAdminRole);

        userRepository.save(admin);
    }

    // ========== LEKÉRDEZÉS MŰVELETEK ==========

    public List<Department> getAllActiveDepartments() {
        return departmentRepository.findByActiveTrueOrderByNameAsc();
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nem található részleg ezzel az ID-val: " + id));
    }

    public Department getDepartmentWithAdmins(Long id) {
        return departmentRepository.findByIdWithAdmins(id)
                .orElseThrow(() -> new IllegalArgumentException("Nem található részleg ezzel az ID-val: " + id));
    }

    public Department getDepartmentWithAppointmentTypes(Long id) {
        return departmentRepository.findByIdWithAppointmentTypes(id)
                .orElseThrow(() -> new IllegalArgumentException("Nem található részleg ezzel az ID-val: " + id));
    }

    public Department getDepartmentWithFullDetails(Long id) {
        return departmentRepository.findByIdWithFullDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Nem található részleg ezzel az ID-val: " + id));
    }

    public Department getCurrentUserManagedDepartment() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Assuming a User has a managedDepartment field
        if (user.getManagedDepartment() == null) {
            return null;
        }

        return departmentRepository.findById(user.getManagedDepartment().getId())
                .orElse(null);
    }

    // ========== STATISZTIKAI MŰVELETEK ==========

    public long countAppointmentsByDepartment(Department department) {
        return appointmentRepository.countByAppointmentTypeDepartment(department);
    }

    public long countPendingAppointmentsByDepartment(Department department) {
        return appointmentRepository.countByAppointmentTypeDepartmentAndStatus(department, AppointmentStatus.PENDING);
    }

    public long countUpcomingAppointmentsByDepartment(Department department) {
        return appointmentRepository.countByAppointmentTypeDepartmentAndAppointmentDateAfter(department, LocalDateTime.now());
    }

    // ========== VALIDÁCIÓ ÉS SEGÉD METÓDUSOK ==========

    public boolean canManageDepartment(Long departmentId) {
        User currentUser = getCurrentUser();

        if (currentUser.isSuperAdmin()) {
            return true;
        }

        if (currentUser.isDepartmentAdmin()) {
            return currentUser.getManagedDepartment() != null &&
                    currentUser.getManagedDepartment().getId().equals(departmentId);
        }

        return false;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Nem található felhasználó: " + username));
    }

    public boolean canDeleteDepartment(Long departmentId) {
        Department department = getDepartmentWithFullDetails(departmentId);
        return department.getAppointmentTypes().isEmpty() &&
                department.getDepartmentAdmins().isEmpty();
    }

    public void deleteDepartment(Long departmentId) {
        if (!canDeleteDepartment(departmentId)) {
            throw new IllegalArgumentException("A részleg nem törölhető, mert vannak hozzá kapcsolódó adatok!");
        }

        Department department = getDepartmentById(departmentId);
        departmentRepository.delete(department);
    }
}