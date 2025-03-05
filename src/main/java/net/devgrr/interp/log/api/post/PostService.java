package net.devgrr.interp.log.api.post;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.log.api.config.exception.BaseException;
import net.devgrr.interp.log.api.config.exception.ErrorCode;
import net.devgrr.interp.log.api.config.mapStruct.PostMapper;
import net.devgrr.interp.log.api.member.MemberService;
import net.devgrr.interp.log.api.member.entity.Member;
import net.devgrr.interp.log.api.post.dto.PostRequest;
import net.devgrr.interp.log.api.post.entity.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PostService {

  private final PostRepository postRepository;
  private final MemberService memberService;
  private final PostMapper postMapper;
  @PersistenceContext private EntityManager entityManager;

  public List<Post> getPosts() {
    return postRepository.findAllByIsDraftFalse();
  }

  public Post existPostsById(Integer id) throws BaseException {
    return postRepository
        .findById(id)
        .orElseThrow(
            () ->
                new BaseException(ErrorCode.INVALID_INPUT_VALUE, "Post not found with id: " + id));
  }

  @Transactional
  public Post getPostsById(Integer id, String email) throws BaseException {
    Post post = existPostsById(id);
    if (!post.getWriter().getEmail().equals(email)) {
      postRepository.incrementViewCount(id);
      entityManager.refresh(post);
    }
    return post;
  }

  public List<Post> getPostsByKeywords(String title, String subTitle, String content, String tag) {
    return postRepository
        .findAllByIsDraftFalseAndTitleContainingOrSubTitleContainingOrContentContainingOrTagContaining(
            title, subTitle, content, tag);
  }

  public List<Post> getPostsByUser(String email) throws BaseException {
    Member member = memberService.getUsersByEmail(email);
    return postRepository.findAllByWriterAndIsDraftFalse(member);
  }

  @Transactional
  public Post setPosts(PostRequest req, String email) throws BaseException {
    Member member = memberService.getUsersByEmail(email);
    Post post = postMapper.toPost(req, member);
    postRepository.save(post);
    return post;
  }

  @Transactional
  public void putPostsById(Integer id, PostRequest req, String email) throws BaseException {
    Post post = existPostsById(id);
    if (!post.getWriter().getEmail().equals(email)) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "수정 권한이 없습니다.");
    }
    Post updPost = postMapper.putPostMapper(req, post);
    postRepository.save(updPost);
  }

  @Transactional
  public void delPostsById(Integer id, String email) throws BaseException {
    Post post = existPostsById(id);
    if (!post.getWriter().getEmail().equals(email)) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "삭제 권한이 없습니다.");
    }
    postRepository.delete(post);
  }

  @Transactional
  public void likePostsById(Integer id, String email) throws BaseException {
    Post post = existPostsById(id);

    if (post.getWriter().getEmail().equals(email)) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "본인 게시글은 추천할 수 없습니다.");
    }

    if (post.getLikes().stream().anyMatch(member -> member.getEmail().equals(email))) {
      // unlike
      post.getLikes().removeIf(member -> member.getEmail().equals(email));
      postRepository.save(post);
    } else {
      // like
      post.getLikes().add(memberService.getUsersByEmail(email));
      postRepository.save(post);
    }
  }
}
