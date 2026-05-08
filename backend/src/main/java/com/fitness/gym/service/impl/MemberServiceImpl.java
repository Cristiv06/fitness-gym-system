package com.fitness.gym.service.impl;

import com.fitness.gym.dto.MemberRequest;
import com.fitness.gym.dto.MemberResponse;
import com.fitness.gym.entity.Member;
import com.fitness.gym.exception.ConflictException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.MemberRepository;
import com.fitness.gym.service.MemberService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
            log.warn("Duplicate email registration attempt: {}", request.email());
            throw new ConflictException("A member with this email already exists.");
        });

        Member member = new Member();
        applyRequest(member, request);
        MemberResponse response = toResponse(memberRepository.save(member));
        log.info("Member created: id={}, email={}", response.memberId(), response.email());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> findAll() {
        List<MemberResponse> members = memberRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
        log.debug("Fetched {} members", members.size());
        return members;
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
                    log.warn("Email conflict on update: memberId={}, email={}", memberId, request.email());
                    throw new ConflictException("Another member already uses this email.");
                });

        applyRequest(member, request);
        log.info("Member updated: id={}", memberId);
        return toResponse(memberRepository.save(member));
    }

    @Override
    public void deactivate(Long memberId) {
        Member member = load(memberId);
        member.setActive(false);
        memberRepository.save(member);
        log.info("Member deactivated: id={}", memberId);
    }

    private Member load(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> {
                    log.error("Member not found: id={}", memberId);
                    return new NotFoundException("Member not found: " + memberId);
                });
    }

    private void applyRequest(Member member, MemberRequest request) {
        member.setEmail(request.email());
        member.setFullName(request.fullName());
        member.setPhone(request.phone());
        member.setDateOfBirth(request.dateOfBirth());
    }

    private MemberResponse toResponse(Member member) {
        return new MemberResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getFullName(),
                member.getPhone(),
                member.getDateOfBirth(),
                member.getActive(),
                member.getCreatedAt()
        );
    }
}
