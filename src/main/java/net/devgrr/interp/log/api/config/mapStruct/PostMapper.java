package net.devgrr.interp.log.api.config.mapStruct;

import java.util.List;
import java.util.Set;
import net.devgrr.interp.log.api.member.entity.Member;
import net.devgrr.interp.log.api.post.dto.PostRequest;
import net.devgrr.interp.log.api.post.dto.PostResponse;
import net.devgrr.interp.log.api.post.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

// import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PostMapper {
  //  PostMapper INSTANCE = Mappers.getMapper(PostMapper.class);

  @Named("likeToCount")
  static int likeToCount(Set<Member> likeMember) {
    return likeMember != null ? likeMember.size() : 0;
  }

  @Named("tagListToString")
  static String tagListToString(List<String> tag) {
    return tag != null ? String.join(",", tag) : null;
  }

  @Named("stringToTagList")
  static List<String> stringToTagList(String tag) {
    return tag != null ? List.of(tag.split(",")) : null;
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "member", target = "writer")
  @Mapping(source = "postRequest.tag", target = "tag", qualifiedByName = "tagListToString")
  Post toPost(PostRequest postRequest, Member member);

  @Mapping(target = "id", ignore = true)
  @Mapping(
      target = "title",
      source = "title",
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(
      target = "subTitle",
      source = "subTitle",
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(
      target = "content",
      source = "content",
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(
      target = "tag",
      source = "tag",
      qualifiedByName = "tagListToString",
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "updatedDate", expression = "java(java.time.LocalDateTime.now())")
  Post putPostMapper(PostRequest postRequest, @MappingTarget Post post);

  @Mapping(source = "post.writer.email", target = "writerEmail")
  @Mapping(source = "post.writer.name", target = "writerName")
  @Mapping(source = "post.likes", target = "likeCount", qualifiedByName = "likeToCount")
  @Mapping(source = "post.tag", target = "tag", qualifiedByName = "stringToTagList")
  PostResponse toResponse(Post post);
}
