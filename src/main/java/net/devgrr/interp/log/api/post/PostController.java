package net.devgrr.interp.log.api.post;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.log.api.config.exception.BaseException;
import net.devgrr.interp.log.api.config.mapStruct.PostMapper;
import net.devgrr.interp.log.api.post.dto.PostRequest;
import net.devgrr.interp.log.api.post.dto.PostResponse;
import net.devgrr.interp.log.api.post.dto.PostValidationGroups.articleGroup;
import net.devgrr.interp.log.api.post.entity.Post;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "PostController", description = "게시판 API")
@RestController
public class PostController {

  private final PostService postService;
  private final PostMapper postMapper;

  @Operation(
      description =
          "게시글 목록을 조회한다. <br>userId가 있을 경우 해당 사용자가 등록한 게시글 목록을 조회한다. <br>그 외 검색 조건이 있다면 해당 조건에 대해 키워드가 포함된 게시글을 조회한다.\n")
  @GetMapping
  public List<PostResponse> getPosts(
      @RequestParam(value = "email", required = false) @Parameter(description = "사용자 Email")
          String email,
      @RequestParam(value = "title", required = false) @Parameter(description = "게시글 제목")
          String title,
      @RequestParam(value = "subTitle", required = false) @Parameter(description = "게시글 부제목")
          String subTitle,
      @RequestParam(value = "content", required = false) @Parameter(description = "게시글 내용")
          String content,
      @RequestParam(value = "tag", required = false) @Parameter(description = "게시글 태그") String tag)
      throws BaseException {
    List<Post> posts;
    if (email != null && !email.trim().isEmpty()) {
      posts = postService.getPostsByUser(email);
    } else {
      posts =
          (title != null && !title.trim().isEmpty())
                  || (subTitle != null && !subTitle.trim().isEmpty())
                  || (content != null && !content.trim().isEmpty())
                  || (tag != null && !tag.trim().isEmpty())
              ? postService.getPostsByKeywords(title, subTitle, content, tag)
              : postService.getPosts();
    }
    return posts.isEmpty()
        ? null
        : posts.stream().map(postMapper::toResponse).collect(Collectors.toList());
  }

  @Operation(description = "게시글을 조회한다.")
  @GetMapping("/{id}")
  public PostResponse getPostsById(
      @PathVariable("id") @Parameter(description = "게시글 ID") Integer id, Principal principal)
      throws BaseException {
    Post post = postService.getPostsById(id, principal.getName());
    return postMapper.toResponse(post);
  }

  @Operation(description = "게시글을 등록한다.")
  @JsonView(articleGroup.class)
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PostResponse setPosts(
      @Validated(articleGroup.class) @RequestBody PostRequest req, Principal principal)
      throws BaseException {
    Post post = postService.setPosts(req, principal.getName());
    return postMapper.toResponse(post);
  }

  @Operation(description = "게시글을 수정한다.")
  @PutMapping("/{id}")
  public void putPostsById(
      @PathVariable("id") @Parameter(description = "게시글 ID") Integer id,
      @Validated(articleGroup.class) @RequestBody PostRequest req,
      Principal principal)
      throws BaseException {
    postService.putPostsById(id, req, principal.getName());
  }

  @Operation(description = "게시글을 삭제한다.")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delPostsById(
      @PathVariable("id") @Parameter(description = "게시글 ID") Integer id, Principal principal)
      throws BaseException {
    postService.delPostsById(id, principal.getName());
  }

  @Operation(description = "게시글을 추천 또는 취소한다.")
  @PutMapping("/{id}/likes")
  public void likePostsById(
      @PathVariable("id") @Parameter(description = "게시글 ID") Integer id, Principal principal)
      throws BaseException {
    postService.likePostsById(id, principal.getName());
  }
}
