package com.BC.Idopontfoglalo.controller;

import com.BC.Idopontfoglalo.entity.Department;
import com.BC.Idopontfoglalo.service.AppointmentService;
import com.BC.Idopontfoglalo.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class LoginController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private AppointmentService appointmentService;


    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        return "admin/dashboard";
    }

    @GetMapping("/appointments")
    public String listDepartments(Model model, Authentication authentication) {
        try {
            List<Department> activeDepartments = departmentService.getAllActiveDepartments();
            model.addAttribute("departments", activeDepartments);
            model.addAttribute("username", authentication.getName());
            return "user/user-departments";
        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt a részlegek betöltésekor: " + e.getMessage());
            return "user/user-departments";
        }
    }


    @GetMapping("/department-admin/dashboard")
    public String depAdminDashboard(Authentication authentication, Model model) {
        model.addAttribute("username", authentication.getName());
        Department department = departmentService.getCurrentUserManagedDepartment();
        model.addAttribute("department", department);
        // Statisztikák hozzáadása (ezeket implementálni kell a service-ben)
        //model.addAttribute("totalAppointments", appointmentService.getTotalAppointmentsForDepartment(department.getId()));
       // model.addAttribute("pendingAppointments", appointmentService.getPendingAppointmentsForDepartment(department.getId()));
        //model.addAttribute("upcomingAppointments", appointmentService.getUpcomingAppointmentsForDepartment(department.getId()));


        return "department-admin/dashboard";
    }
}