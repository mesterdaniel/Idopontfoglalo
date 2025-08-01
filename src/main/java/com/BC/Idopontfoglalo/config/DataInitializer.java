package com.BC.Idopontfoglalo.config;

import com.BC.Idopontfoglalo.entity.Role;
import com.BC.Idopontfoglalo.entity.User;
import com.BC.Idopontfoglalo.entity.Department;
import com.BC.Idopontfoglalo.entity.AppointmentType;
import com.BC.Idopontfoglalo.repository.RoleRepository;
import com.BC.Idopontfoglalo.repository.UserRepository;
import com.BC.Idopontfoglalo.repository.DepartmentRepository;
import com.BC.Idopontfoglalo.repository.AppointmentTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      RoleRepository roleRepository,
                                      DepartmentRepository departmentRepository,
                                      AppointmentTypeRepository appointmentTypeRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {

            // ========== SZEREPKÖRÖK LÉTREHOZÁSA ==========
            if (roleRepository.count() == 0) {
                Role superAdminRole = new Role();
                superAdminRole.setRoleName("ROLE_SUPER_ADMIN");

                Role departmentAdminRole = new Role();
                departmentAdminRole.setRoleName("ROLE_DEPARTMENT_ADMIN");

                Role adminRole = new Role();
                adminRole.setRoleName("ROLE_ADMIN");

                Role userRole = new Role();
                userRole.setRoleName("ROLE_USER");

                roleRepository.save(superAdminRole);
                roleRepository.save(departmentAdminRole);
                roleRepository.save(adminRole);
                roleRepository.save(userRole);
            }

            // ========== FŐ ADMIN LÉTREHOZÁSA ==========
            if (userRepository.count() == 0) {
                Role superAdminRole = roleRepository.findByRoleName("ROLE_SUPER_ADMIN");
                User superAdmin = new User();
                superAdmin.setUsername("superadmin");
                superAdmin.setPassword(passwordEncoder.encode("password"));
                superAdmin.setEnabled(true);
                superAdmin.setRoles(Collections.singleton(superAdminRole));

                userRepository.save(superAdmin);

                // Régi admin felhasználó is maradjon
                Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN");
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setEnabled(true);
                admin.setRoles(Collections.singleton(adminRole));

                userRepository.save(admin);
            }

            // ========== PÉLDA RÉSZLEGEK LÉTREHOZÁSA ==========
            if (departmentRepository.count() == 0) {
                // Orvosi rendelő
                Department medicalDept = new Department();
                medicalDept.setName("Orvosi rendelő");
                medicalDept.setDescription("Általános orvosi vizsgálatok és kezelések");
                departmentRepository.save(medicalDept);

                // Fogorvosi rendelő
                Department dentalDept = new Department();
                dentalDept.setName("Fogorvosi rendelő");
                dentalDept.setDescription("Fogászati kezelések és szájsebészet");
                departmentRepository.save(dentalDept);

                // Laboratórium
                Department labDept = new Department();
                labDept.setName("Laboratórium");
                labDept.setDescription("Laboratóriumi vizsgálatok és elemzések");
                departmentRepository.save(labDept);

                // ========== DEPARTMENT ADMINOK LÉTREHOZÁSA ==========
                Role departmentAdminRole = roleRepository.findByRoleName("ROLE_DEPARTMENT_ADMIN");

                // Orvosi admin
                User medicalAdmin = new User();
                medicalAdmin.setUsername("orvosi_admin");
                medicalAdmin.setPassword(passwordEncoder.encode("password"));
                medicalAdmin.setEnabled(true);
                medicalAdmin.setRoles(Collections.singleton(departmentAdminRole));
                medicalAdmin.setManagedDepartment(medicalDept);
                userRepository.save(medicalAdmin);

                // Fogorvosi admin
                User dentalAdmin = new User();
                dentalAdmin.setUsername("fogorvosi_admin");
                dentalAdmin.setPassword(passwordEncoder.encode("password"));
                dentalAdmin.setEnabled(true);
                dentalAdmin.setRoles(Collections.singleton(departmentAdminRole));
                dentalAdmin.setManagedDepartment(dentalDept);
                userRepository.save(dentalAdmin);

                // Lab admin
                User labAdmin = new User();
                labAdmin.setUsername("lab_admin");
                labAdmin.setPassword(passwordEncoder.encode("password"));
                labAdmin.setEnabled(true);
                labAdmin.setRoles(Collections.singleton(departmentAdminRole));
                labAdmin.setManagedDepartment(labDept);
                userRepository.save(labAdmin);

                // ========== PÉLDA IDŐPONT TÍPUSOK ==========
                if (appointmentTypeRepository.count() == 0) {
                    // Orvosi időpont típusok
                    AppointmentType checkup = new AppointmentType();
                    checkup.setName("Általános vizsgálat");
                    checkup.setDescription("Általános orvosi vizsgálat és konzultáció");
                    checkup.setDefaultDurationMinutes(30);
                    checkup.setMaxParticipants(1);
                    checkup.setDepartment(medicalDept);
                    appointmentTypeRepository.save(checkup);

                    AppointmentType bloodTest = new AppointmentType();
                    bloodTest.setName("Vérvétel");
                    bloodTest.setDescription("Laboratóriumi vérvétel különböző vizsgálatokhoz");
                    bloodTest.setDefaultDurationMinutes(15);
                    bloodTest.setMaxParticipants(5); // 5 ember egyszerre
                    bloodTest.setDepartment(medicalDept);
                    bloodTest.setRequiresApproval(false); // Nincs szükség jóváhagyásra
                    appointmentTypeRepository.save(bloodTest);

                    // Fogorvosi időpont típusok
                    AppointmentType dentalCheckup = new AppointmentType();
                    dentalCheckup.setName("Fogászati kontroll");
                    dentalCheckup.setDescription("Általános fogászati vizsgálat");
                    dentalCheckup.setDefaultDurationMinutes(30);
                    dentalCheckup.setMaxParticipants(1);
                    dentalCheckup.setDepartment(dentalDept);
                    appointmentTypeRepository.save(dentalCheckup);

                    AppointmentType dentalCleaning = new AppointmentType();
                    dentalCleaning.setName("Fogkő eltávolítás");
                    dentalCleaning.setDescription("Professzionális fogkő eltávolítás és tisztítás");
                    dentalCleaning.setDefaultDurationMinutes(45);
                    dentalCleaning.setMaxParticipants(1);
                    dentalCleaning.setBufferMinutes(15); // 15 perc puffer
                    dentalCleaning.setDepartment(dentalDept);
                    appointmentTypeRepository.save(dentalCleaning);

                    // Lab időpont típusok
                    AppointmentType labTest = new AppointmentType();
                    labTest.setName("Laboratóriumi vizsgálat");
                    labTest.setDescription("Különböző laboratóriumi vizsgálatok");
                    labTest.setDefaultDurationMinutes(20);
                    labTest.setMaxParticipants(3); // 3 ember egyszerre
                    labTest.setDepartment(labDept);
                    labTest.setRequiresApproval(false);
                    appointmentTypeRepository.save(labTest);

                    AppointmentType ecg = new AppointmentType();
                    ecg.setName("EKG vizsgálat");
                    ecg.setDescription("Szív elektrokardiográfia vizsgálat");
                    ecg.setDefaultDurationMinutes(25);
                    ecg.setMaxParticipants(2); // 2 ember egyszerre
                    ecg.setDepartment(labDept);
                    appointmentTypeRepository.save(ecg);
                }
            }

            // ========== TESZT FELHASZNÁLÓ ==========
            if (!userRepository.findByUsername("testuser").isPresent()) {
                Role userRole = roleRepository.findByRoleName("ROLE_USER");
                User testUser = new User();
                testUser.setUsername("testuser");
                testUser.setPassword(passwordEncoder.encode("password"));
                testUser.setEnabled(true);
                testUser.setRoles(Collections.singleton(userRole));
                userRepository.save(testUser);
            }
        };
    }
}