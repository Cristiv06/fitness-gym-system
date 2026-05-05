package com.fitness.gym.service;

import com.fitness.gym.dto.MemberProfileRequest;
import com.fitness.gym.dto.MemberProfileResponse;
import java.util.List;

public interface MemberProfileService {

    MemberProfileResponse create(MemberProfileRequest request);

    List<MemberProfileResponse> findAll();

    MemberProfileResponse findByMemberId(Long memberId);

    MemberProfileResponse update(Long memberId, MemberProfileRequest request);

    void delete(Long memberId);
}
