package com.oose.slsms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** Payload for /api/sensors/noise. */
public record NoiseRequest(
        @NotBlank String zoneId,
        @Positive double dB
) {}
