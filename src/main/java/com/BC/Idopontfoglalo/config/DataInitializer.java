package com.BC.Idopontfoglalo.config;

import com.BC.Idopontfoglalo.entity.Role;
import com.BC.Idopontfoglalo.entity.User;
import com.BC.Idopontfoglalo.repository.RoleRepository;
import com.BC.Idopontfoglalo.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      RoleRepository roleRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (roleRepository.count() == 0) {
                Role adminRole = new Role();
                adminRole.setRoleName("ROLE_ADMIN");

                Role userRole = new Role();
                userRole.setRoleName("ROLE_USER");

                roleRepository.save(adminRole);
                roleRepository.save(userRole);
            }

            if (userRepository.count() == 0) {
                Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN");
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setEnabled(true);
                admin.setRoles(Collections.singleton(adminRole));

                userRepository.save(admin);
            }
        };
    }
}
