package com.fitness.gym.service;

import com.fitness.gym.dto.MemberRequest;
import com.fitness.gym.dto.MemberResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {
    MemberResponse create(MemberRequest request);

    List<MemberResponse> findAll();

    Page<MemberResponse> findAll(Pageable pageable);

    MemberResponse findById(Long memberId);

    MemberResponse update(Long memberId, MemberRequest request);

    void deactivate(Long memberId);
}
