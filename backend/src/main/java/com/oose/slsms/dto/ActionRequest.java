package com.oose.slsms.dto;

import jakarta.validation.constraints.NotBlank;

/** Payload for seat action endpoints (reserve, checkIn, …). */
public record ActionRequest(
        @NotBlank String userId
) {}
