package com.BC.Idopontfoglalo.controller;

import com.BC.Idopontfoglalo.entity.*;
import com.BC.Idopontfoglalo.service.AppointmentService;
import com.BC.Idopontfoglalo.service.AppointmentTypeService;
import com.BC.Idopontfoglalo.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/appointments")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentTypeService appointmentTypeService;



    @GetMapping("/my-appointments")
    public String myAppointments(Model model, Authentication authentication) {
        try {
            List<Appointment> userAppointments = appointmentService.getUserAppointments();
            model.addAttribute("appointments", userAppointments);
            model.addAttribute("username", authentication.getName());
            return "user/user-my-appointments";
        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt az időpontok betöltésekor: " + e.getMessage());
            return "user/user-my-appointments";
        }
    }

    @GetMapping("/department/{departmentId}")
    public String viewDepartmentAppointments(@PathVariable Long departmentId, 
                                           Model model, 
                                           Authentication authentication) {
        try {
            Department department = departmentService.getDepartmentById(departmentId);
            
            if (!department.isActive()) {
                return "redirect:/appointments?error=Ez+a+részleg+jelenleg+nem+elérhető";
            }

            List<AppointmentType> activeTypes = appointmentTypeService.getActiveAppointmentTypesByDepartment(department);
            
            model.addAttribute("department", department);
            model.addAttribute("appointmentTypes", activeTypes);
            model.addAttribute("username", authentication.getName());
            
            return "user/user-dept-appointments";
        } catch (Exception e) {
            return "redirect:/appointments?error=" + e.getMessage();
        }
    }

    @GetMapping("/department/{departmentId}/weekly")
    @ResponseBody
    public Map<String, List<AppointmentSlotDTO>> getWeeklyAppointments(
            @PathVariable Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) Long appointmentTypeId) {

        try {
            if (appointmentTypeId != null) {
                return appointmentService.getWeeklyAppointmentSlots(departmentId, startDate);
            } else {
                return appointmentService.getWeeklyAppointmentSlots(departmentId, startDate);
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Hiba történt: " + e.getMessage());
        }
    }
    /*
        @PostMapping("/book/{appointmentId}")
        public String bookAppointment(@PathVariable Long appointmentId,
                                    @RequestParam(required = false) String notes,
                                    RedirectAttributes redirectAttributes,
                                    Authentication authentication) {
            try {
                appointmentService.bookAppointment(appointmentId, authentication.getName(), notes);
                redirectAttributes.addFlashAttribute("success", "Időpont sikeresen lefoglalva!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Hiba történt: " + e.getMessage());
            }
            return edirect:/appointments/my-appointments";
        }
    /*
        @PostMapping("/cancel/{appointmentId}")
        public String cancelAppointment(@PathVariable Long appointmentId,
                                      RedirectAttributes redirectAttributes,
                                      Authentication authentication) {
            try {
                appointmentService.cancelAppointment(appointmentId);
                redirectAttributes.addFlashAttribute("success", "Időpont sikeresen lemondva!");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Hiba történt: " + e.getMessage());
            }
            return "redirect:/appointments/my-appointments";
        }
    */
    @GetMapping("/appointment/{appointmentId}/details")
    @ResponseBody
    public Appointment getAppointmentDetails(@PathVariable Long appointmentId) {
        try {
            return appointmentService.getAppointmentById(appointmentId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Időpont nem található");
        }
    }

    // UserController.java frissítve

    @PostMapping("/book/{timeSlotId}")
    public String bookAppointment(@PathVariable Long timeSlotId,
                                  @RequestParam(required = false) String notes,
                                  RedirectAttributes redirectAttributes,
                                  Authentication authentication) {
        try {
            appointmentService.bookAppointment(timeSlotId, authentication.getName(), notes);
            redirectAttributes.addFlashAttribute("success", "Időpont sikeresen lefoglalva!");
            return "redirect:/appointments/my-appointments";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            // Vissza a részleg nézethez
            return "redirect:/appointments";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Váratlan hiba történt: " + e.getMessage());
            return "redirect:/appointments";
        }
    }

    @PostMapping("/cancel/{appointmentId}")
    public String cancelAppointment(@PathVariable Long appointmentId,
                                    RedirectAttributes redirectAttributes,
                                    Authentication authentication) {
        try {
            appointmentService.cancelAppointment(appointmentId);
            redirectAttributes.addFlashAttribute("success", "Időpont sikeresen lemondva!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Váratlan hiba történt: " + e.getMessage());
        }
        return "redirect:/appointments/my-appointments";
    }

    /**
     * AJAX endpoint időpont foglaláshoz (opcionális, frontend fejlesztéshez)
     */
    @PostMapping("/book/{timeSlotId}/ajax")
    @ResponseBody
    public ResponseEntity<?> bookAppointmentAjax(@PathVariable Long timeSlotId,
                                                 @RequestParam(required = false) String notes,
                                                 Authentication authentication) {
        try {
            appointmentService.bookAppointment(timeSlotId, authentication.getName(), notes);
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "Időpont sikeresen lefoglalva!"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "Váratlan hiba történt"
            ));
        }
    }

    /**
     * Részletes időpont információ modal-hoz
     */
    @GetMapping("/slot/{timeSlotId}/details")
    @ResponseBody
    public ResponseEntity<?> getTimeSlotDetails(@PathVariable Long timeSlotId) {
        try {
            AvailableTimeSlot timeSlot = appointmentService.getTimeSlotById(timeSlotId);

            Map<String, Object> details = new HashMap<>();
            details.put("id", timeSlot.getId());
            details.put("typeName", timeSlot.getAppointmentType().getName());
            details.put("description", timeSlot.getAppointmentType().getDescription());
            details.put("startTime", timeSlot.getStartTime());
            details.put("endTime", timeSlot.getStartTime().plusMinutes(timeSlot.getDurationMinutes()));
            details.put("duration", timeSlot.getDurationMinutes());
            details.put("maxAttendees", timeSlot.getMaxAttendees());
            details.put("currentAttendees", timeSlot.getCurrentAttendees());
            details.put("availableSlots", timeSlot.getMaxAttendees() - timeSlot.getCurrentAttendees());
            details.put("requiresApproval", timeSlot.getAppointmentType().isRequiresApproval());
            details.put("departmentName", timeSlot.getAppointmentType().getDepartment().getName());

            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "Időpont nem található"
            ));
        }
    }
}