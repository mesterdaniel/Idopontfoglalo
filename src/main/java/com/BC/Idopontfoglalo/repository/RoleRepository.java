package com.BC.Idopontfoglalo.repository;

import com.BC.Idopontfoglalo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByRoleName(String roleName);
}
