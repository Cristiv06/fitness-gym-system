package com.fitness.gym.service;

import com.fitness.gym.dto.MemberRequest;
import com.fitness.gym.dto.MemberResponse;
import java.util.List;

public interface MemberService {
    MemberResponse create(MemberRequest request);

    List<MemberResponse> findAll();

    MemberResponse findById(Long memberId);

    MemberResponse update(Long memberId, MemberRequest request);

    void deactivate(Long memberId);
}
