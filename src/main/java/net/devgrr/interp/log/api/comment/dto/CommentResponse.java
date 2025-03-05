package net.devgrr.interp.log.api.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "댓글 응답")
public record CommentResponse(
    @Schema(description = "댓글 ID") Integer id,
    @Schema(description = "부모 댓글 ID") Integer parentCommentId,
    @Schema(description = "댓글 내용") String content,
    @Schema(description = "작성자 Email") String writerEmail,
    @Schema(description = "작성자 이름") String writerName,
    @Schema(description = "댓글 추천 수") Integer likeCount,
    @Schema(description = "게시글 ID") Integer postId,
    @Schema(description = "댓글 작성 일자") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
    @Schema(description = "댓글 수정 일자") @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedDate,
    @Schema(description = "하위 댓글") @JsonInclude(JsonInclude.Include.NON_NULL)
        List<CommentResponse> childComment) {}
