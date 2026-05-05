package com.fitness.gym.service.impl;

import com.fitness.gym.dto.CheckInRequest;
import com.fitness.gym.dto.CheckInResponse;
import com.fitness.gym.entity.CheckIn;
import com.fitness.gym.entity.Member;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.CheckInRepository;
import com.fitness.gym.repository.MemberRepository;
import com.fitness.gym.service.CheckInService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRepository checkInRepository;
    private final MemberRepository memberRepository;

    public CheckInServiceImpl(CheckInRepository checkInRepository, MemberRepository memberRepository) {
        this.checkInRepository = checkInRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public CheckInResponse create(CheckInRequest request) {
        Member member = memberRepository
                .findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Membru negasit: " + request.memberId()));
        CheckIn c = new CheckIn();
        c.setMember(member);
        c.setCheckinTime(request.checkinTime() != null ? request.checkinTime() : LocalDateTime.now());
        return toResponse(checkInRepository.save(c));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckInResponse> findAll() {
        return checkInRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CheckInResponse findById(Long checkinId) {
        return toResponse(load(checkinId));
    }

    @Override
    public CheckInResponse update(Long checkinId, CheckInRequest request) {
        CheckIn c = load(checkinId);
        Member member = memberRepository
                .findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Membru negasit: " + request.memberId()));
        c.setMember(member);
        c.setCheckinTime(request.checkinTime() != null ? request.checkinTime() : c.getCheckinTime());
        return toResponse(checkInRepository.save(c));
    }

    @Override
    public void delete(Long checkinId) {
        checkInRepository.deleteById(load(checkinId).getCheckinId());
    }

    private CheckIn load(Long id) {
        return checkInRepository.findById(id).orElseThrow(() -> new NotFoundException("Check-in negasit: " + id));
    }

    private CheckInResponse toResponse(CheckIn c) {
        return new CheckInResponse(c.getCheckinId(), c.getMember().getMemberId(), c.getCheckinTime());
    }
}
