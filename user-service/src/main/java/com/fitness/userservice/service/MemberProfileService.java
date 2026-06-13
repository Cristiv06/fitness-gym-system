package com.fitness.userservice.service;

import com.fitness.userservice.dto.MemberProfileRequest;
import com.fitness.userservice.dto.MemberProfileResponse;
import java.util.List;

public interface MemberProfileService {
    MemberProfileResponse create(MemberProfileRequest request);
    List<MemberProfileResponse> findAll();
    MemberProfileResponse findById(Long memberId);
    MemberProfileResponse update(Long memberId, MemberProfileRequest request);
    void delete(Long memberId);
}
