package com.fitness.gymservice.service.impl;

import com.fitness.gymservice.client.UserServiceClient;
import com.fitness.gymservice.dto.CheckInRequest;
import com.fitness.gymservice.dto.CheckInResponse;
import com.fitness.gymservice.dto.MemberResponse;
import com.fitness.gymservice.entity.CheckIn;
import com.fitness.gymservice.exception.BadRequestException;
import com.fitness.gymservice.exception.NotFoundException;
import com.fitness.gymservice.repository.CheckInRepository;
import com.fitness.gymservice.service.CheckInService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRepository checkInRepository;
    private final UserServiceClient userServiceClient;

    public CheckInServiceImpl(CheckInRepository checkInRepository, UserServiceClient userServiceClient) {
        this.checkInRepository = checkInRepository;
        this.userServiceClient = userServiceClient;
    }

    @Override
    public CheckInResponse create(CheckInRequest request) {
        MemberResponse member = userServiceClient.getMemberById(request.memberId());
        if (Boolean.FALSE.equals(member.active())) {
            throw new BadRequestException("Membrul este inactiv.");
        }
        CheckIn checkIn = new CheckIn();
        checkIn.setMemberId(request.memberId());
        return toResponse(checkInRepository.save(checkIn));
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
    public void delete(Long checkinId) {
        checkInRepository.deleteById(load(checkinId).getCheckinId());
    }

    private CheckIn load(Long id) {
        return checkInRepository.findById(id).orElseThrow(() -> new NotFoundException("Check-in negasit: " + id));
    }

    private CheckInResponse toResponse(CheckIn c) {
        return new CheckInResponse(c.getCheckinId(), c.getMemberId(), c.getCheckinTime());
    }
}
