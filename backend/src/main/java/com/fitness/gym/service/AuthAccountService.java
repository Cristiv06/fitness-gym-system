package com.fitness.gym.service;

import com.fitness.gym.dto.AccountType;
import com.fitness.gym.dto.AdminCreateAccountRequest;
import com.fitness.gym.dto.AuthMeResponse;
import com.fitness.gym.dto.ClassEnrollmentResponse;
import com.fitness.gym.dto.CreateMyGymClassRequest;
import com.fitness.gym.dto.EnrollMyClassRequest;
import com.fitness.gym.dto.GymClassResponse;
import com.fitness.gym.dto.RegisterAccountRequest;
import com.fitness.gym.dto.SubscriptionResponse;
import com.fitness.gym.entity.ClassEnrollment;
import com.fitness.gym.entity.GymClass;
import com.fitness.gym.entity.Member;
import com.fitness.gym.entity.Room;
import com.fitness.gym.entity.Trainer;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.ConflictException;
import com.fitness.gym.repository.ClassEnrollmentRepository;
import com.fitness.gym.repository.GymClassRepository;
import com.fitness.gym.repository.MemberRepository;
import com.fitness.gym.repository.RoomRepository;
import com.fitness.gym.repository.SubscriptionRepository;
import com.fitness.gym.repository.TrainerRepository;
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
    private final TrainerRepository trainerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final GymClassRepository gymClassRepository;
    private final RoomRepository roomRepository;

    public AuthAccountService(
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder,
            MemberRepository memberRepository,
            TrainerRepository trainerRepository,
            SubscriptionRepository subscriptionRepository,
            ClassEnrollmentRepository classEnrollmentRepository,
            GymClassRepository gymClassRepository,
            RoomRepository roomRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.classEnrollmentRepository = classEnrollmentRepository;
        this.gymClassRepository = gymClassRepository;
        this.roomRepository = roomRepository;
    }

    public AuthMeResponse registerUser(RegisterAccountRequest request) {
        ensureNewUsername(request.username());
        createUserCredentials(request.username(), request.password(), List.of("ROLE_USER"));

        if (request.accountType() == AccountType.MEMBER) {
            if (request.dateOfBirth() == null) {
                throw new BadRequestException("dateOfBirth este obligatoriu pentru MEMBER.");
            }
            memberRepository.findByEmail(request.email()).ifPresent(member -> {
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

        trainerRepository.findByEmail(request.email()).ifPresent(trainer -> {
            throw new ConflictException("Exista deja antrenor cu acest email.");
        });
        Trainer trainer = new Trainer();
        trainer.setUsername(request.username());
        trainer.setEmail(request.email());
        trainer.setFullName(request.fullName());
        trainer.setPhone(request.phone());
        trainer.setSpecialization(request.specialization());
        Trainer saved = trainerRepository.save(trainer);
        return new AuthMeResponse(request.username(), List.of("ROLE_USER"), null, saved.getTrainerId());
    }

    public AuthMeResponse createAdminAccount(AdminCreateAccountRequest request) {
        ensureNewUsername(request.username());
        createUserCredentials(request.username(), request.password(), List.of("ROLE_USER", "ROLE_ADMIN"));

        trainerRepository.findByEmail(request.email()).ifPresent(trainer -> {
            throw new ConflictException("Exista deja antrenor cu acest email.");
        });

        Trainer trainer = new Trainer();
        trainer.setUsername(request.username());
        trainer.setEmail(request.email());
        trainer.setFullName(request.fullName());
        trainer.setPhone(request.phone());
        trainer.setSpecialization(request.specialization());
        Trainer saved = trainerRepository.save(trainer);
        return new AuthMeResponse(request.username(), List.of("ROLE_USER", "ROLE_ADMIN"), null, saved.getTrainerId());
    }

    @Transactional(readOnly = true)
    public AuthMeResponse getMe(String username, List<String> roles) {
        Long memberId = memberRepository.findByUsername(username).map(Member::getMemberId).orElse(null);
        Long trainerId = trainerRepository.findByUsername(username).map(Trainer::getTrainerId).orElse(null);
        return new AuthMeResponse(username, roles, memberId, trainerId);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getMySubscriptions(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Acest cont nu are profil de membru."));
        return subscriptionRepository.findByMember_Username(username).stream()
                .map(subscription -> new SubscriptionResponse(
                        subscription.getSubscriptionId(),
                        member.getMemberId(),
                        subscription.getPlan().getPlanId(),
                        subscription.getPlan().getName(),
                        subscription.getStartDate(),
                        subscription.getEndDate(),
                        subscription.getStatus(),
                        subscription.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GymClassResponse> getMyClasses(String username) {
        if (memberRepository.findByUsername(username).isPresent()) {
            return classEnrollmentRepository.findByMember_Username(username).stream()
                    .map(ClassEnrollment::getGymClass)
                    .map(this::toGymClassResponse)
                    .distinct()
                    .toList();
        }
        if (trainerRepository.findByUsername(username).isPresent()) {
            return gymClassRepository.findByTrainer_Username(username).stream()
                    .map(this::toGymClassResponse)
                    .toList();
        }
        throw new BadRequestException("Contul nu are profil de membru sau antrenor.");
    }

    @Transactional(readOnly = true)
    public List<GymClassResponse> getTrainerClassesForMember(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Acest cont nu are profil de membru."));
        return gymClassRepository.findAll().stream()
                .filter(gymClass -> gymClass.getMaxParticipants() == null
                        || classEnrollmentRepository.countByGymClass_ClassId(gymClass.getClassId()) < gymClass.getMaxParticipants())
                .map(this::toGymClassResponse)
                .toList();
    }

    public GymClassResponse createMyClass(String username, CreateMyGymClassRequest request) {
        Trainer trainer = trainerRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Acest cont nu are profil de antrenor."));
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new BadRequestException("Sala nu exista: " + request.roomId()));
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("Ora de final trebuie sa fie dupa ora de start.");
        }
        GymClass gymClass = new GymClass();
        gymClass.setTrainer(trainer);
        gymClass.setRoom(room);
        gymClass.setTitle(request.title());
        gymClass.setStartTime(request.startTime());
        gymClass.setEndTime(request.endTime());
        gymClass.setMaxParticipants(request.maxParticipants());
        return toGymClassResponse(gymClassRepository.save(gymClass));
    }

    public ClassEnrollmentResponse enrollToClass(String username, EnrollMyClassRequest request) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Acest cont nu are profil de membru."));
        GymClass gymClass = gymClassRepository.findById(request.classId())
                .orElseThrow(() -> new BadRequestException("Clasa nu exista: " + request.classId()));

        classEnrollmentRepository.findByMember_MemberIdAndGymClass_ClassId(member.getMemberId(), gymClass.getClassId())
                .ifPresent(existing -> {
                    throw new ConflictException("Esti deja inscris la aceasta clasa.");
                });

        long currentEnrollments = classEnrollmentRepository.countByGymClass_ClassId(gymClass.getClassId());
        if (gymClass.getMaxParticipants() != null && currentEnrollments >= gymClass.getMaxParticipants()) {
            throw new BadRequestException("Clasa este deja completa.");
        }

        ClassEnrollment enrollment = new ClassEnrollment();
        enrollment.setMember(member);
        enrollment.setGymClass(gymClass);
        ClassEnrollment saved = classEnrollmentRepository.save(enrollment);
        return new ClassEnrollmentResponse(
                saved.getEnrollmentId(),
                saved.getMember().getMemberId(),
                saved.getGymClass().getClassId(),
                saved.getEnrolledAt());
    }

    @Transactional(readOnly = true)
    public List<ClassEnrollmentResponse> getMyEnrollments(String username) {
        return classEnrollmentRepository.findByMember_Username(username).stream()
                .map(enrollment -> new ClassEnrollmentResponse(
                        enrollment.getEnrollmentId(),
                        enrollment.getMember().getMemberId(),
                        enrollment.getGymClass().getClassId(),
                        enrollment.getEnrolledAt()))
                .toList();
    }

    private GymClassResponse toGymClassResponse(GymClass gymClass) {
        return new GymClassResponse(
                gymClass.getClassId(),
                gymClass.getTrainer().getTrainerId(),
                gymClass.getRoom().getRoomId(),
                gymClass.getTitle(),
                gymClass.getStartTime(),
                gymClass.getEndTime(),
                gymClass.getMaxParticipants());
    }

    private void ensureNewUsername(String username) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = ?",
                Integer.class,
                username);
        if (count != null && count > 0) {
            throw new ConflictException("Username deja existent.");
        }
    }

    private void createUserCredentials(String username, String rawPassword, List<String> authorities) {
        jdbcTemplate.update(
                "INSERT INTO users (username, password, enabled) VALUES (?, ?, ?)",
                username,
                passwordEncoder.encode(rawPassword),
                true);

        for (String authority : authorities) {
            jdbcTemplate.update(
                    "INSERT INTO authorities (username, authority) VALUES (?, ?)",
                    username,
                    authority);
        }
    }
}
