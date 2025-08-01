package com.BC.Idopontfoglalo.controller;

import com.BC.Idopontfoglalo.entity.AppointmentSlotDTO;
import com.BC.Idopontfoglalo.entity.AppointmentType;
import com.BC.Idopontfoglalo.entity.Department;
import com.BC.Idopontfoglalo.entity.User;
import com.BC.Idopontfoglalo.service.AppointmentService;
import com.BC.Idopontfoglalo.service.AppointmentTypeService;
import com.BC.Idopontfoglalo.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/department-admin")
@PreAuthorize("hasRole('DEPARTMENT_ADMIN')")
public class DepartmentAdminController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentTypeService appointmentTypeService;
/*
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        try {
            Department department = departmentService.getCurrentUserManagedDepartment();
            model.addAttribute("department", department);
            model.addAttribute("username", authentication.getName());

            // Statisztikák hozzáadása (ezeket implementálni kell a service-ben)
            // model.addAttribute("totalAppointments", appointmentService.getTotalAppointmentsForDepartment(department.getId()));
            // model.addAttribute("pendingAppointments", appointmentService.getPendingAppointmentsForDepartment(department.getId()));
            // model.addAttribute("upcomingAppointments", appointmentService.getUpcomingAppointmentsForDepartment(department.getId()));

            if (department != null && department.getAppointmentTypes() != null) {
                model.addAttribute("appointmentTypes", department.getAppointmentTypes());
            }

            return "department-admin/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt: " + e.getMessage());
            return "department-admin/dashboard";
        }
    }
*/
    @GetMapping("/edit")
    public String showEditDepartmentForm(Model model, Authentication authentication) {
        try {
            Department department = departmentService.getCurrentUserManagedDepartment();
            model.addAttribute("department", department);
            model.addAttribute("username", authentication.getName());
            return "department-admin/edit-department";
        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt: " + e.getMessage());
            return "redirect:/department-admin/dashboard";
        }
    }

    @GetMapping("/departments/{departmentId}/edit-type/{typeId}")
    public String showEditAppointmentTypeForm(
            @PathVariable Long departmentId,
            @PathVariable Long typeId,
            Model model,
            Authentication authentication) {

        try {
            Department currentDepartment = departmentService.getCurrentUserManagedDepartment();
            if (!currentDepartment.getId().equals(departmentId)) {
                return "redirect:/department-admin/dashboard?error=Nem+van+jogosultságod+ehhez+a+részleghez";
            }

            AppointmentType appointmentType = appointmentTypeService.getAppointmentTypeById(typeId);
            if (!appointmentType.getDepartment().getId().equals(departmentId)) {
                return "redirect:/department-admin/departments/" + departmentId + "?error=Nem+található+az+időpont+típus";
            }

            model.addAttribute("department", currentDepartment);
            model.addAttribute("appointmentType", appointmentType);
            model.addAttribute("username", authentication.getName());

            return "department-admin/edit-appointment-type";

        } catch (Exception e) {
            return "redirect:/department-admin/departments/" + departmentId + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/{departmentId}/update-appointment-type/{typeId}")
    public String updateAppointmentType(
            @PathVariable Long departmentId,
            @PathVariable Long typeId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Integer defaultDurationMinutes,
            @RequestParam Integer maxParticipants,
            @RequestParam(required = false) Integer bufferMinutes,
            @RequestParam(defaultValue = "true") boolean requiresApproval,
            @RequestParam(defaultValue = "true") boolean active,
            RedirectAttributes redirectAttributes) {

        try {
            Department currentDepartment = departmentService.getCurrentUserManagedDepartment();
            if (!currentDepartment.getId().equals(departmentId)) {
                redirectAttributes.addFlashAttribute("error", "Nem van jogosultságod ehhez a részleghez");
                return "redirect:/department-admin/dashboard";
            }

            appointmentTypeService.updateAppointmentType(
                    typeId,
                    name,
                    description,
                    defaultDurationMinutes,
                    maxParticipants,
                    bufferMinutes != null ? bufferMinutes : 0,
                    requiresApproval,
                    active
            );

            redirectAttributes.addFlashAttribute("success", "Időpont típus sikeresen frissítve!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt: " + e.getMessage());
        }

        return "redirect:/department-admin/departments/" + departmentId;
    }

    @PostMapping("/update")
    public String updateDepartment(@RequestParam String description,
                                   RedirectAttributes redirectAttributes,
                                   Authentication authentication) {
        try {
            Department department = departmentService.getCurrentUserManagedDepartment();
            departmentService.updateDepartment(department.getId(), department.getName(), description);
            redirectAttributes.addFlashAttribute("success", "Részleg sikeresen frissítve!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt: " + e.getMessage());
        }
        return "redirect:/department-admin/dashboard";
    }

    @GetMapping("/departments/{id}")
    public String viewDepartmentDetail(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Department department = departmentService.getDepartmentWithFullDetails(id);
            Department currentDepartment = departmentService.getCurrentUserManagedDepartment();

            // Ellenőrizzük, hogy a felhasználónak joga van-e ehhez a részleghez
            if (!currentDepartment.getId().equals(id)) {
                return "redirect:/department-admin/dashboard?error=Nem+van+jogosultságod+ehhez+a+részleghez";
            }

            model.addAttribute("department", department);
            model.addAttribute("username", authentication.getName());

            return "department-admin/department-detail";
        } catch (Exception e) {
            return "redirect:/department-admin/dashboard?error=" + e.getMessage();
        }
    }

    @GetMapping("/departments/{id}/new-type")
    public String showCreateAppointmentTypeForm(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            Department department = departmentService.getDepartmentById(id);
            Department currentDepartment = departmentService.getCurrentUserManagedDepartment();

            // Ellenőrizzük, hogy a felhasználónak joga van-e ehhez a részleghez
            if (!currentDepartment.getId().equals(id)) {
                return "redirect:/department-admin/dashboard?error=Nem+van+jogosultságod+ehhez+a+részleghez";
            }

            model.addAttribute("department", department);
            model.addAttribute("username", authentication.getName());
            return "department-admin/new-appointment-type";

        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt: " + e.getMessage());
            return "redirect:/department-admin/departments/" + id;
        }
    }

    @PostMapping("/{departmentId}/create-appointment-type")
    public String createAppointmentType(
            @PathVariable("departmentId") Long departmentId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Integer defaultDurationMinutes,
            @RequestParam Integer maxParticipants,
            @RequestParam(required = false) Integer bufferMinutes,
            @RequestParam(defaultValue = "true") boolean requiresApproval,
            RedirectAttributes redirectAttributes) {

        try {
            Department currentDepartment = departmentService.getCurrentUserManagedDepartment();

            // Ellenőrizzük, hogy a felhasználónak joga van-e ehhez a részleghez
            if (!currentDepartment.getId().equals(departmentId)) {
                redirectAttributes.addFlashAttribute("error", "Nem van jogosultságod ehhez a részleghez");
                return "redirect:/department-admin/dashboard";
            }

            appointmentTypeService.createAppointmentType(
                    name,
                    description,
                    defaultDurationMinutes,
                    maxParticipants,
                    bufferMinutes != null ? bufferMinutes : 0,
                    requiresApproval,
                    departmentId
            );
            redirectAttributes.addFlashAttribute("success", "Időpont típus sikeresen létrehozva!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt: " + e.getMessage());
        }
        return "redirect:/department-admin/departments/" + departmentId;
    }

    @PostMapping("/{departmentId}/create-appointments")
    public String createBulkAppointments(
            @PathVariable Long departmentId,
            @RequestParam Long appointmentTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam Integer duration,
            @RequestParam List<DayOfWeek> selectedDays,
            RedirectAttributes redirectAttributes) {

        try {
            Department currentDepartment = departmentService.getCurrentUserManagedDepartment();

            // Ellenőrizzük, hogy a felhasználónak joga van-e ehhez a részleghez
            if (!currentDepartment.getId().equals(departmentId)) {
                redirectAttributes.addFlashAttribute("error", "Nem van jogosultságod ehhez a részleghez");
                return "redirect:/department-admin/dashboard";
            }

            // Ha nincs vég dátum, akkor csak egy napra
            if (endDate == null) {
                endDate = startDate;
            }

            appointmentService.createBulkAppointments(
                    appointmentTypeId,
                    startDate,
                    endDate,
                    startTime,
                    endTime,
                    duration,
                    selectedDays
            );

            redirectAttributes.addFlashAttribute("success", "Időpontok sikeresen létrehozva!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Hiba történt: " + e.getMessage());
        }

        return "redirect:/department-admin/departments/" + departmentId;
    }

    @GetMapping("/{departmentId}/appointments/weekly")
    @ResponseBody
    public Map<String, List<AppointmentSlotDTO>> getWeeklyAppointments(
            @PathVariable Long departmentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {

        try {
            Department currentDepartment = departmentService.getCurrentUserManagedDepartment();

            // Ellenőrizzük, hogy a felhasználónak joga van-e ehhez a részleghez
            if (!currentDepartment.getId().equals(departmentId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nem van jogosultságod ehhez a részleghez");
            }

            return appointmentService.getWeeklyAppointmentSlots(departmentId, startDate);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Hiba történt: " + e.getMessage());
        }
    }
}