package net.devgrr.interp.log.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ResultResponse(
        @Schema(description = "전달 할 메세지(실행한 메소드 명, 경고 등)")
        String message,
        @Schema(description = "결과 - boolean")
        boolean result
) {}
