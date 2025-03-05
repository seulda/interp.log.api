package net.devgrr.interp.log.api.config.mapStruct;

import java.util.List;
import java.util.Set;
import net.devgrr.interp.log.api.comment.dto.CommentRequest;
import net.devgrr.interp.log.api.comment.dto.CommentResponse;
import net.devgrr.interp.log.api.comment.entity.Comment;
import net.devgrr.interp.log.api.member.entity.Member;
import net.devgrr.interp.log.api.post.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

// import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CommentMapper {
  //  CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

  @Named("likeToCount")
  static int likeToCount(Set<Member> likeMember) {
    return likeMember != null ? likeMember.size() : 0;
  }

  @Mapping(source = "comment.writer.email", target = "writerEmail")
  @Mapping(source = "comment.writer.name", target = "writerName")
  @Mapping(source = "comment.post.id", target = "postId")
  @Mapping(source = "comment.likes", target = "likeCount", qualifiedByName = "likeToCount")
  CommentResponse toResponse(Comment comment);

  @Mapping(source = "comment.writer.email", target = "writerEmail")
  @Mapping(source = "comment.writer.name", target = "writerName")
  @Mapping(source = "comment.post.id", target = "postId")
  @Mapping(source = "comment.likes", target = "likeCount", qualifiedByName = "likeToCount")
  @Mapping(source = "childComments", target = "childComment")
  CommentResponse toResponseWithChildren(Comment comment, List<CommentResponse> childComments);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "post", ignore = true)
  @Mapping(target = "parentCommentId", ignore = true)
  @Mapping(
      source = "content",
      target = "content",
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  Comment putCommentMapper(CommentRequest commentRequest, @MappingTarget Comment comment);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "likes", ignore = true)
  @Mapping(source = "commentRequest.content", target = "content")
  @Mapping(source = "post", target = "post")
  @Mapping(source = "member", target = "writer")
  Comment toComment(CommentRequest commentRequest, Post post, Member member);
}
