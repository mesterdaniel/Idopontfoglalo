package com.BC.Idopontfoglalo.contoller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/adminDashboard")
    public String adminDashboard() {
        return "adminDashboard";
    }

    @GetMapping("/userDashboard")
    public String userDashboard() {
        return "userDashboard";
    }
}
