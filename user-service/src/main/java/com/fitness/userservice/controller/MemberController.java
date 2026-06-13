package com.fitness.userservice.controller;

import com.fitness.userservice.dto.MemberRequest;
import com.fitness.userservice.dto.MemberResponse;
import com.fitness.userservice.service.MemberService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse create(@Valid @RequestBody MemberRequest request) {
        return memberService.create(request);
    }

    @GetMapping
    public List<MemberResponse> findAll() {
        return memberService.findAll();
    }

    @GetMapping("/page")
    public Page<MemberResponse> findAllPaged(Pageable pageable) {
        return memberService.findAll(pageable);
    }

    @GetMapping("/{memberId}")
    public MemberResponse findById(@PathVariable Long memberId) {
        return memberService.findById(memberId);
    }

    @PutMapping("/{memberId}")
    public MemberResponse update(@PathVariable Long memberId, @Valid @RequestBody MemberRequest request) {
        return memberService.update(memberId, request);
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long memberId) {
        memberService.deactivate(memberId);
    }
}
