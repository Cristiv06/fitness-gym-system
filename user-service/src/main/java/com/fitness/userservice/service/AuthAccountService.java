package com.fitness.userservice.service;

import com.fitness.userservice.client.GymServiceClientWrapper;
import com.fitness.userservice.dto.*;
import com.fitness.userservice.entity.Member;
import com.fitness.userservice.exception.BadRequestException;
import com.fitness.userservice.exception.ConflictException;
import com.fitness.userservice.repository.MemberRepository;
import com.fitness.userservice.repository.SubscriptionRepository;
import feign.FeignException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthAccountService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final GymServiceClientWrapper gymServiceClientWrapper;

    public AuthAccountService(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder,
            MemberRepository memberRepository, SubscriptionRepository subscriptionRepository,
            GymServiceClientWrapper gymServiceClientWrapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.gymServiceClientWrapper = gymServiceClientWrapper;
    }

    public AuthMeResponse registerUser(RegisterAccountRequest request) {
        ensureNewUsername(request.username());
        createUserCredentials(request.username(), request.password(), List.of("ROLE_USER"));

        if (request.accountType() == AccountType.MEMBER) {
            if (request.dateOfBirth() == null) {
                throw new BadRequestException("dateOfBirth este obligatoriu pentru MEMBER.");
            }
            memberRepository.findByEmail(request.email()).ifPresent(m -> {
                throw new ConflictException("Exista deja membru cu acest email.");
            });
            Member member = new Member();
            member.setUsername(request.username());
            member.setEmail(request.email());
            member.setFullName(request.fullName());
            member.setPhone(request.phone());
            member.setDateOfBirth(request.dateOfBirth());
            Member saved = memberRepository.save(member);
            return new AuthMeResponse(request.username(), List.of("ROLE_USER"), saved.getMemberId(), null);
        }

        TrainerResponse trainer = gymServiceClientWrapper.createTrainerWithUsername(
                new TrainerWithUsernameRequest(request.username(), request.fullName(),
                        request.specialization(), request.phone(), request.email()));
        return new AuthMeResponse(request.username(), List.of("ROLE_USER"), null, trainer.trainerId());
    }

    public AuthMeResponse createAdminAccount(AdminCreateAccountRequest request) {
        ensureNewUsername(request.username());
        createUserCredentials(request.username(), request.password(), List.of("ROLE_USER", "ROLE_ADMIN"));

        TrainerResponse trainer = gymServiceClientWrapper.createTrainerWithUsername(
                new TrainerWithUsernameRequest(request.username(), request.fullName(),
                        request.specialization(), request.phone(), request.email()));
        return new AuthMeResponse(request.username(), List.of("ROLE_USER", "ROLE_ADMIN"), null, trainer.trainerId());
    }

    @Transactional(readOnly = true)
    public AuthMeResponse getMe(String username, List<String> roles) {
        Long memberId = memberRepository.findByUsername(username).map(Member::getMemberId).orElse(null);
        Long trainerId = findTrainerIdByUsername(username);
        return new AuthMeResponse(username, roles, memberId, trainerId);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getMySubscriptions(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Acest cont nu are profil de membru."));
        return subscriptionRepository.findByMember_Username(username).stream()
                .map(s -> new SubscriptionResponse(s.getSubscriptionId(), member.getMemberId(),
                        s.getPlan().getPlanId(), s.getStartDate(), s.getEndDate(), s.getStatus(), s.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GymClassResponse> getMyClasses(String username) {
        var memberOpt = memberRepository.findByUsername(username);
        if (memberOpt.isPresent()) {
            return gymServiceClientWrapper.getClassesEnrolledByMember(memberOpt.get().getMemberId());
        }
        Long trainerId = findTrainerIdByUsername(username);
        if (trainerId != null) {
            return gymServiceClientWrapper.getClassesByTrainer(trainerId);
        }
        throw new BadRequestException("Contul nu are profil de membru sau antrenor.");
    }

    @Transactional(readOnly = true)
    public List<GymClassResponse> getTrainerClassesForMember(String username) {
        memberRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Acest cont nu are profil de membru."));
        return gymServiceClientWrapper.getAvailableClasses();
    }

    public GymClassResponse createMyClass(String username, CreateMyGymClassRequest request) {
        Long trainerId = findTrainerIdByUsername(username);
        if (trainerId == null) {
            throw new BadRequestException("Acest cont nu are profil de antrenor.");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("Ora de final trebuie sa fie dupa ora de start.");
        }
        return gymServiceClientWrapper.createClass(new GymClassRequest(trainerId, request.roomId(),
                request.title(), request.startTime(), request.endTime(), request.maxParticipants()));
    }

    public ClassEnrollmentResponse enrollToClass(String username, EnrollMyClassRequest request) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Acest cont nu are profil de membru."));
        return gymServiceClientWrapper.createEnrollment(new ClassEnrollmentRequest(member.getMemberId(), request.classId()));
    }

    @Transactional(readOnly = true)
    public List<ClassEnrollmentResponse> getMyEnrollments(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Acest cont nu are profil de membru."));
        return gymServiceClientWrapper.getEnrollmentsByMember(member.getMemberId());
    }

    private Long findTrainerIdByUsername(String username) {
        try {
            TrainerResponse trainer = gymServiceClientWrapper.findTrainerByUsername(username);
            return trainer != null ? trainer.trainerId() : null;
        } catch (FeignException.NotFound e) {
            return null;
        }
    }

    private void ensureNewUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?", Integer.class, username);
        if (count != null && count > 0) {
            throw new ConflictException("Username deja existent.");
        }
    }

    private void createUserCredentials(String username, String rawPassword, List<String> authorities) {
        jdbcTemplate.update("INSERT INTO users (username, password, enabled) VALUES (?, ?, ?)",
                username, passwordEncoder.encode(rawPassword), true);
        for (String authority : authorities) {
            jdbcTemplate.update("INSERT INTO authorities (username, authority) VALUES (?, ?)",
                    username, authority);
        }
    }
}
