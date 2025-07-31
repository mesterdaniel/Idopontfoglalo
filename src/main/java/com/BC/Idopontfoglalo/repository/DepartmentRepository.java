package com.BC.Idopontfoglalo.repository;

import com.BC.Idopontfoglalo.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * Aktív részlegek lekérése
     */
    List<Department> findByActiveTrue();

    /**
     * Részleg keresése név alapján
     */
    Optional<Department> findByName(String name);

    /**
     * Aktív részlegek név alapján rendezve
     */
    List<Department> findByActiveTrueOrderByNameAsc();

    /**
     * Részlegek száma
     */
    long countByActiveTrue();

    /**
     * Ellenőrzi, hogy létezik-e már ilyen nevű részleg
     */
    boolean existsByName(String name);

    /**
     * Ellenőrzi, hogy létezik-e már ilyen nevű aktív részleg
     */
    boolean existsByNameAndActiveTrue(String name);

    /**
     * Department admin-okkal együtt lekérés
     */
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.departmentAdmins WHERE d.id = :id")
    Optional<Department> findByIdWithAdmins(@Param("id") Long id);

    /**
     * Időpont típusokkal együtt lekérés
     */
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.appointmentTypes WHERE d.id = :id")
    Optional<Department> findByIdWithAppointmentTypes(@Param("id") Long id);

    /**
     * Teljes részleg információ lekérése (admin-okkal és típusokkal)
     */
    @Query("SELECT DISTINCT d FROM Department d " +
            "LEFT JOIN FETCH d.departmentAdmins " +
            "LEFT JOIN FETCH d.appointmentTypes " +
            "WHERE d.id = :id")
    Optional<Department> findByIdWithFullDetails(@Param("id") Long id);

}