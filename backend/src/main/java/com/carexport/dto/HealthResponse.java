package com.carexport.dto;

import java.util.List;

public record HealthResponse(
        String status,
        List<SourceHealthDto> sources
) {}