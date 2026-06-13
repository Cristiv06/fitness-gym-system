package com.fitness.userservice.dto;

public record TrainerWithUsernameRequest(String username, String fullName, String specialization, String phone, String email) {}
