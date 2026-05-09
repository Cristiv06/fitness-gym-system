package com.fitness.gym.controller;

import com.fitness.gym.dto.AdminCreateAccountRequest;
import com.fitness.gym.dto.AuthMeResponse;
import com.fitness.gym.dto.ClassEnrollmentResponse;
import com.fitness.gym.dto.GymClassResponse;
import com.fitness.gym.dto.RegisterAccountRequest;
import com.fitness.gym.dto.SubscriptionResponse;
import com.fitness.gym.service.AuthAccountService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthAccountService authAccountService;

    public AuthController(AuthAccountService authAccountService) {
        this.authAccountService = authAccountService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthMeResponse register(@Valid @RequestBody RegisterAccountRequest request) {
        return authAccountService.registerUser(request);
    }

    @PostMapping("/admin/create-account")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthMeResponse createAdmin(@Valid @RequestBody AdminCreateAccountRequest request) {
        return authAccountService.createAdminAccount(request);
    }

    @GetMapping("/me")
    public AuthMeResponse me(Authentication authentication) {
        return authAccountService.getMe(
                authentication.getName(),
                authentication.getAuthorities().stream().map(a -> a.getAuthority()).toList());
    }

    @GetMapping("/me/subscriptions")
    public List<SubscriptionResponse> mySubscriptions(Authentication authentication) {
        return authAccountService.getMySubscriptions(authentication.getName());
    }

    @GetMapping("/me/classes")
    public List<GymClassResponse> myClasses(Authentication authentication) {
        return authAccountService.getMyClasses(authentication.getName());
    }

    @GetMapping("/me/enrollments")
    public List<ClassEnrollmentResponse> myEnrollments(Authentication authentication) {
        return authAccountService.getMyEnrollments(authentication.getName());
    }
}
