package com.fitness.userservice.service.impl;

import com.fitness.userservice.dto.MemberProfileRequest;
import com.fitness.userservice.dto.MemberProfileResponse;
import com.fitness.userservice.entity.Member;
import com.fitness.userservice.entity.MemberProfile;
import com.fitness.userservice.exception.NotFoundException;
import com.fitness.userservice.repository.MemberProfileRepository;
import com.fitness.userservice.repository.MemberRepository;
import com.fitness.userservice.service.MemberProfileService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MemberProfileServiceImpl implements MemberProfileService {

    private final MemberProfileRepository profileRepository;
    private final MemberRepository memberRepository;

    public MemberProfileServiceImpl(MemberProfileRepository profileRepository, MemberRepository memberRepository) {
        this.profileRepository = profileRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public MemberProfileResponse create(MemberProfileRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Member not found: " + request.memberId()));
        MemberProfile profile = new MemberProfile();
        profile.setMember(member);
        profile.setEmergencyContact(request.emergencyContact());
        profile.setNotes(request.notes());
        return toResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberProfileResponse> findAll() {
        return profileRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberProfileResponse findById(Long memberId) {
        return toResponse(load(memberId));
    }

    @Override
    public MemberProfileResponse update(Long memberId, MemberProfileRequest request) {
        MemberProfile profile = load(memberId);
        profile.setEmergencyContact(request.emergencyContact());
        profile.setNotes(request.notes());
        return toResponse(profileRepository.save(profile));
    }

    @Override
    public void delete(Long memberId) {
        profileRepository.deleteById(load(memberId).getMemberId());
    }

    private MemberProfile load(Long memberId) {
        return profileRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member profile not found: " + memberId));
    }

    private MemberProfileResponse toResponse(MemberProfile p) {
        return new MemberProfileResponse(p.getMemberId(), p.getEmergencyContact(), p.getNotes());
    }
}
