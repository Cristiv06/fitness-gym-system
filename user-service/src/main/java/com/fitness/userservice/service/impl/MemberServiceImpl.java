package com.fitness.userservice.service.impl;

import com.fitness.userservice.dto.MemberRequest;
import com.fitness.userservice.dto.MemberResponse;
import com.fitness.userservice.entity.Member;
import com.fitness.userservice.exception.ConflictException;
import com.fitness.userservice.exception.NotFoundException;
import com.fitness.userservice.repository.MemberRepository;
import com.fitness.userservice.service.MemberService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberResponse create(MemberRequest request) {
        memberRepository.findByEmail(request.email()).ifPresent(existing -> {
            throw new ConflictException("A member with this email already exists.");
        });
        Member member = new Member();
        applyRequest(member, request);
        return toResponse(memberRepository.save(member));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        return memberRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MemberResponse> findAll(Pageable pageable) {
        return memberRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse findById(Long memberId) {
        return toResponse(load(memberId));
    }

    @Override
    public MemberResponse update(Long memberId, MemberRequest request) {
        Member member = load(memberId);
        memberRepository.findByEmail(request.email())
                .filter(existing -> !existing.getMemberId().equals(memberId))
                .ifPresent(existing -> {
                    throw new ConflictException("Another member already uses this email.");
                });
        applyRequest(member, request);
        return toResponse(memberRepository.save(member));
    }

    @Override
    public void deactivate(Long memberId) {
        Member member = load(memberId);
        member.setActive(false);
        memberRepository.save(member);
    }

    private Member load(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found: " + memberId));
    }

    private void applyRequest(Member member, MemberRequest request) {
        member.setEmail(request.email());
        member.setFullName(request.fullName());
        member.setPhone(request.phone());
        member.setDateOfBirth(request.dateOfBirth());
    }

    private MemberResponse toResponse(Member member) {
        return new MemberResponse(member.getMemberId(), member.getEmail(), member.getFullName(),
                member.getPhone(), member.getDateOfBirth(), member.getActive(), member.getCreatedAt());
    }
}
