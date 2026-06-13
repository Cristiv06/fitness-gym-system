package com.fitness.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.stereotype.Controller;

/**
 * Minimal login page that exposes the CSRF token as a hidden input.
 * The React frontend does GET /login to read this token before POST /login (form login).
 */
@Controller
public class LoginPageController {

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String login(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        String paramName = "_csrf";
        String tokenValue = "";
        if (csrfToken != null) {
            paramName = csrfToken.getParameterName();
            tokenValue = csrfToken.getToken(); // forces generation + writes XSRF-TOKEN cookie
        }
        return "<!DOCTYPE html><html><head><title>Login</title></head><body>"
                + "<form method=\"post\" action=\"/login\">"
                + "<input type=\"hidden\" name=\"" + paramName + "\" value=\"" + tokenValue + "\"/>"
                + "</form></body></html>";
    }
}
