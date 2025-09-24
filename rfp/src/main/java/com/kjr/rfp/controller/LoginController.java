package com.kjr.rfp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/login-success")
    public String loginSuccess() {
        return "redirect:/resumes/search"; // Redirect to search page after login
    }
    @GetMapping("/logout-after-download")
    public String logoutAfterDownload(HttpServletRequest request) {
        // Invalidate the session completely
        request.getSession().invalidate();
        new SecurityContextLogoutHandler().logout(request, null, null);
        return "redirect:/login?logout";
    }

}


