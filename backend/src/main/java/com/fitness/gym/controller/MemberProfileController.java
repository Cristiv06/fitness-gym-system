package com.fitness.gym.controller;

import com.fitness.gym.dto.MemberProfileRequest;
import com.fitness.gym.dto.MemberProfileResponse;
import com.fitness.gym.service.MemberProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member-profiles")
public class MemberProfileController {

    private final MemberProfileService service;

    public MemberProfileController(MemberProfileService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberProfileResponse create(@Valid @RequestBody MemberProfileRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<MemberProfileResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{memberId}")
    public MemberProfileResponse findById(@PathVariable Long memberId) {
        return service.findByMemberId(memberId);
    }

    @PutMapping("/{memberId}")
    public MemberProfileResponse update(@PathVariable Long memberId, @Valid @RequestBody MemberProfileRequest request) {
        return service.update(memberId, request);
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long memberId) {
        service.delete(memberId);
    }
}
