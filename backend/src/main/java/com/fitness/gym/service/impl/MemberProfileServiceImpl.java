package com.fitness.gym.service.impl;

import com.fitness.gym.dto.MemberProfileRequest;
import com.fitness.gym.dto.MemberProfileResponse;
import com.fitness.gym.entity.Member;
import com.fitness.gym.entity.MemberProfile;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.ConflictException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.MemberProfileRepository;
import com.fitness.gym.repository.MemberRepository;
import com.fitness.gym.service.MemberProfileService;
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
        Member member = memberRepository
                .findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Membru negasit: " + request.memberId()));
        profileRepository.findById(request.memberId()).ifPresent(p -> {
            throw new ConflictException("Profilul pentru acest membru exista deja.");
        });
        MemberProfile profile = new MemberProfile();
        profile.setMember(member);
        profile.setEmergencyContact(request.emergencyContact());
        profile.setNotes(request.notes());
        member.setProfile(profile);
        return toResponse(profileRepository.save(profile));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberProfileResponse> findAll() {
        return profileRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberProfileResponse findByMemberId(Long memberId) {
        return toResponse(load(memberId));
    }

    @Override
    public MemberProfileResponse update(Long memberId, MemberProfileRequest request) {
        if (!request.memberId().equals(memberId)) {
            throw new BadRequestException("memberId din corp trebuie sa coincida cu cel din URL.");
        }
        MemberProfile profile = load(memberId);
        profile.setEmergencyContact(request.emergencyContact());
        profile.setNotes(request.notes());
        return toResponse(profileRepository.save(profile));
    }

    @Override
    public void delete(Long memberId) {
        MemberProfile profile = load(memberId);
        Member member = profile.getMember();
        member.setProfile(null);
        profileRepository.delete(profile);
    }

    private MemberProfile load(Long memberId) {
        return profileRepository
                .findById(memberId)
                .orElseThrow(() -> new NotFoundException("Profil negasit pentru membru: " + memberId));
    }

    private MemberProfileResponse toResponse(MemberProfile p) {
        return new MemberProfileResponse(p.getMemberId(), p.getEmergencyContact(), p.getNotes());
    }
}
