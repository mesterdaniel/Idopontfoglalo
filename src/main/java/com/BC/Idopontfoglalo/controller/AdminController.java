package com.BC.Idopontfoglalo.controller;

import com.BC.Idopontfoglalo.entity.Appointment;
import com.BC.Idopontfoglalo.entity.User;
import com.BC.Idopontfoglalo.service.AppointmentService;
import com.BC.Idopontfoglalo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")  // Csak admin férhet hozzá
public class AdminController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private UserRepository userRepository;

    // ========== IDŐPONT KEZELÉS ==========

    /**
     * Admin időpont áttekintő oldal
     */
    @GetMapping("/appointments")
    public String listAllAppointments(Model model, Authentication authentication) {
        try {
            List<Appointment> allAppointments = appointmentService.getAllAppointments();
            List<Appointment> upcomingAppointments = appointmentService.getAllUpcomingAppointments();
            List<Appointment> pendingAppointments = appointmentService.getPendingAppointments();
            long pendingCount = appointmentService.getPendingAppointmentsCount();

            model.addAttribute("allAppointments", allAppointments);
            model.addAttribute("upcomingAppointments", upcomingAppointments);
            model.addAttribute("pendingAppointments", pendingAppointments);
            model.addAttribute("pendingCount", pendingCount);
            model.addAttribute("username", authentication.getName());

            return "admin/appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt az időpontok betöltésekor: " + e.getMessage());
            return "admin/appointments";
        }
    }

    /**
     * Függőben lévő időpontok (jóváhagyásra várók)
     */
    @GetMapping("/appointments/pending")
    public String listPendingAppointments(Model model, Authentication authentication) {
        try {
            List<Appointment> pendingAppointments = appointmentService.getPendingAppointments();
            model.addAttribute("pendingAppointments", pendingAppointments);
            model.addAttribute("username", authentication.getName());
            return "admin/pending-appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt: " + e.getMessage());
            return "admin/pending-appointments";
        }
    }

    /**
     * Időpont jóváhagyása
     */
    @PostMapping("/appointment/{id}/approve")
    public String approveAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.approveAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Időpont jóváhagyva!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt: " + e.getMessage());
        }

        return "redirect:/admin/appointments/pending";
    }

    /**
     * Időpont elutasítása
     */
    @PostMapping("/appointment/{id}/reject")
    public String rejectAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.rejectAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Időpont elutasítva!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt: " + e.getMessage());
        }

        return "redirect:/admin/appointments/pending";
    }

    /**
     * Időpont részletes megtekintése (admin)
     */
    @GetMapping("/appointment/{id}")
    public String viewAppointmentDetail(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            model.addAttribute("appointment", appointment);
            model.addAttribute("username", authentication.getName());
            return "admin/appointment-detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/appointments";
        }
    }

    // ========== FELHASZNÁLÓ KEZELÉS ==========

    /**
     * Összes felhasználó listázása
     */
    @GetMapping("/users")
    public String listAllUsers(Model model, Authentication authentication) {
        try {
            List<User> allUsers = userRepository.findAll();
            model.addAttribute("users", allUsers);
            model.addAttribute("username", authentication.getName());
            return "admin/users";
        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt a felhasználók betöltésekor: " + e.getMessage());
            return "admin/users";
        }
    }

    /**
     * Felhasználó részletes megtekintése
     */
    @GetMapping("/user/{id}")
    public String viewUserDetail(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Nem található felhasználó ezzel az ID-val: " + id));

            // Felhasználó időpontjainak lekérése
            List<Appointment> userAppointments = user.getAppointments();

            model.addAttribute("user", user);
            model.addAttribute("userAppointments", userAppointments);
            model.addAttribute("username", authentication.getName());

            return "admin/user-detail";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    /**
     * Felhasználó engedélyezése/letiltása
     */
    @PostMapping("/user/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Nem található felhasználó"));

            user.setEnabled(!user.isEnabled());
            userRepository.save(user);

            String status = user.isEnabled() ? "engedélyezve" : "letiltva";
            redirectAttributes.addFlashAttribute("success",
                    "Felhasználó (" + user.getUsername() + ") sikeresen " + status + "!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // ========== DASHBOARD ==========

    /**
     * Admin dashboard - statisztikák
     */
    @GetMapping("/dashboard")
    public String adminDashboard(Model model, Authentication authentication) {
        try {
            // Statisztikák gyűjtése
            long totalUsers = userRepository.count();
            long totalAppointments = appointmentService.getAllAppointments().size();
            long pendingAppointments = appointmentService.getPendingAppointmentsCount();
            long upcomingAppointments = appointmentService.getAllUpcomingAppointments().size();

            // Legújabb időpontok
            List<Appointment> recentAppointments = appointmentService.getAllAppointments()
                    .stream()
                    .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                    .limit(5)
                    .toList();

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalAppointments", totalAppointments);
            model.addAttribute("pendingAppointments", pendingAppointments);
            model.addAttribute("upcomingAppointments", upcomingAppointments);
            model.addAttribute("recentAppointments", recentAppointments);
            model.addAttribute("username", authentication.getName());

            return "admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt a dashboard betöltésekor: " + e.getMessage());
            return "admin/dashboard";
        }
    }

    // ========== HIBAKEZELÉS ==========

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", "Admin hiba történt: " + e.getMessage());
        return "error";
    }
}