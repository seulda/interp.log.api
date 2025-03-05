package net.devgrr.interp.log.api.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "게시글 응답")
public record PostResponse(
    @Schema(description = "게시글 ID") Long id,
    @Schema(description = "게시글 제목") String title,
    @Schema(description = "게시글 부제목") String subTitle,
    @Schema(description = "게시글 내용") String content,
    @Schema(description = "임시저장 여부") Boolean isDraft,
    @Schema(description = "작성자 Email") String writerEmail,
    @Schema(description = "작성자 이름") String writerName,
    @Schema(description = "게시글 추천 수") Integer likeCount,
    @Schema(description = "태그") List<String> tag,
    @Schema(description = "게시글 조회 수") Integer viewCount,
    @Schema(description = "게시글 작성 일자") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
    @Schema(description = "게시글 수정 일자") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedDate) {}
