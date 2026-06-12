package com.oose.slsms.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

/** Payload for /api/sensors/temperature. */
public record TemperatureRequest(
        @NotBlank String zoneId,
        @DecimalMin("-50.0") @DecimalMax("100.0") double celsius
) {}
