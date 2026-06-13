package com.fitness.userservice.controller;

import com.fitness.userservice.dto.MemberProfileRequest;
import com.fitness.userservice.dto.MemberProfileResponse;
import com.fitness.userservice.service.MemberProfileService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member-profiles")
public class MemberProfileController {

    private final MemberProfileService profileService;

    public MemberProfileController(MemberProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberProfileResponse create(@Valid @RequestBody MemberProfileRequest request) {
        return profileService.create(request);
    }

    @GetMapping
    public List<MemberProfileResponse> findAll() {
        return profileService.findAll();
    }

    @GetMapping("/{memberId}")
    public MemberProfileResponse findById(@PathVariable Long memberId) {
        return profileService.findById(memberId);
    }

    @PutMapping("/{memberId}")
    public MemberProfileResponse update(@PathVariable Long memberId,
            @Valid @RequestBody MemberProfileRequest request) {
        return profileService.update(memberId, request);
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long memberId) {
        profileService.delete(memberId);
    }
}
