package net.devgrr.interp.log.api.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MemberUpdateRequest(
    @Schema(description = "고유 ID") Long id,
    @Schema(description = "비밀번호") String password,
    @Schema(description = "이름") String name,
    @Schema(description = "이메일") String email,
    @Schema(description = "이미지") String image,
    @Schema(description = "권한") String role) {}
