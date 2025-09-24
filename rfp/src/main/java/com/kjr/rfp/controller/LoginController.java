package com.kjr.rfp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(required = false) String redirect, Model model) {
        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }
        return "login";
    }

    @GetMapping("/login-success")
    public String loginSuccess(HttpServletRequest request) {
        String redirectUrl = request.getParameter("redirect");
        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/resumes/search";
    }

    @GetMapping("/logout-after-download")
    public String logoutAfterDownload(HttpServletRequest request) {
        // Invalidate the session completely
        request.getSession().invalidate();
        new SecurityContextLogoutHandler().logout(request, null, null);
        return "redirect:/login?logout";
    }

}


