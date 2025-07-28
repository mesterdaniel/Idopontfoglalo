package com.BC.Idopontfoglalo.controller;

import com.BC.Idopontfoglalo.entity.Appointment;
import com.BC.Idopontfoglalo.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
@RequestMapping("/user")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // ========== FELHASZNÁLÓI OLDALAK ==========

    /**
     * Felhasználó összes időpontjának listázása
     */
    @GetMapping("/appointments")
    public String listUserAppointments(Model model, Authentication authentication) {
        try {
            List<Appointment> appointments = appointmentService.getUserAppointments();
            List<Appointment> upcomingAppointments = appointmentService.getUserUpcomingAppointments();
            List<Appointment> pastAppointments = appointmentService.getUserPastAppointments();

            model.addAttribute("allAppointments", appointments);
            model.addAttribute("upcomingAppointments", upcomingAppointments);
            model.addAttribute("pastAppointments", pastAppointments);
            model.addAttribute("username", authentication.getName());

            return "user/appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt az időpontok betöltésekor: " + e.getMessage());
            return "user/appointments";
        }
    }

    /**
     * Új időpont foglalási űrlap megjelenítése
     */
    @GetMapping("/new-appointment")
    public String showNewAppointmentForm(Model model, Authentication authentication) {
        model.addAttribute("username", authentication.getName());
        return "user/new-appointment";
    }

    /**
     * Új időpont létrehozása
     */
    @PostMapping("/new-appointment")
    public String createAppointment(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String appointmentDate,
            @RequestParam String appointmentTime,
            @RequestParam Integer durationMinutes,
            RedirectAttributes redirectAttributes) {

        try {
            // Dátum és idő összefűzése
            String dateTimeString = appointmentDate + "T" + appointmentTime;
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);

            // Időpont létrehozása
            Appointment appointment = appointmentService.createAppointment(
                    title,
                    description,
                    dateTime,
                    durationMinutes
            );

            redirectAttributes.addFlashAttribute("success",
                    "Időpont sikeresen létrehozva! ID: " + appointment.getId());
            return "redirect:/user/appointments";

        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Hibás dátum vagy idő formátum!");
            return "redirect:/user/new-appointment";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/new-appointment";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Váratlan hiba történt: " + e.getMessage());
            return "redirect:/user/new-appointment";
        }
    }

    /**
     * Időpont részleteinek megtekintése
     */
    @GetMapping("/appointment/{id}")
    public String viewAppointment(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);

            // Ellenőrizzük, hogy a felhasználó saját időpontját nézi-e
            if (!appointment.getUser().getUsername().equals(authentication.getName())) {
                model.addAttribute("error", "Nincs jogosultságod megtekinteni ezt az időpontot!");
                return "redirect:/user/appointments";
            }

            model.addAttribute("appointment", appointment);
            model.addAttribute("username", authentication.getName());
            return "user/appointment-detail";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/user/appointments";
        }
    }

    /**
     * Időpont szerkesztési űrlap megjelenítése
     */
    @GetMapping("/appointment/{id}/edit")
    public String showEditAppointmentForm(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);

            // Jogosultság ellenőrzése
            if (!appointment.getUser().getUsername().equals(authentication.getName())) {
                model.addAttribute("error", "Nincs jogosultságod szerkeszteni ezt az időpontot!");
                return "redirect:/user/appointments";
            }

            // Dátum és idő szétválasztása az űrlaphoz
            LocalDateTime dateTime = appointment.getAppointmentDate();
            String date = dateTime.toLocalDate().toString();
            String time = dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));

            model.addAttribute("appointment", appointment);
            model.addAttribute("appointmentDate", date);
            model.addAttribute("appointmentTime", time);
            model.addAttribute("username", authentication.getName());

            return "user/edit-appointment";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/user/appointments";
        }
    }

    /**
     * Időpont frissítése
     */
    @PostMapping("/appointment/{id}/edit")
    public String updateAppointment(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String appointmentDate,
            @RequestParam String appointmentTime,
            @RequestParam Integer durationMinutes,
            RedirectAttributes redirectAttributes) {

        try {
            // Dátum és idő összefűzése
            String dateTimeString = appointmentDate + "T" + appointmentTime;
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString);

            // Időpont frissítése
            Appointment updatedAppointment = appointmentService.updateAppointment(
                    id, title, description, dateTime, durationMinutes
            );

            redirectAttributes.addFlashAttribute("success", "Időpont sikeresen frissítve!");
            return "redirect:/user/appointment/" + id;

        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error", "Hibás dátum vagy idő formátum!");
            return "redirect:/user/appointment/" + id + "/edit";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user/appointment/" + id + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Váratlan hiba történt: " + e.getMessage());
            return "redirect:/user/appointment/" + id + "/edit";
        }
    }

    /**
     * Időpont lemondása
     */
    @PostMapping("/appointment/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            appointmentService.cancelAppointment(id);
            redirectAttributes.addFlashAttribute("success", "Időpont sikeresen lemondva!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt: " + e.getMessage());
        }

        return "redirect:/user/appointments";
    }

    // ========== SEGÉD METÓDUSOK ==========

    /**
     * Hibakezelő metódus
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", "Váratlan hiba történt: " + e.getMessage());
        return "error";
    }
}