package net.devgrr.interp.log.api.comment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.log.api.comment.dto.CommentRequest;
import net.devgrr.interp.log.api.comment.dto.CommentResponse;
import net.devgrr.interp.log.api.comment.entity.Comment;
import net.devgrr.interp.log.api.config.exception.BaseException;
import net.devgrr.interp.log.api.config.exception.ErrorCode;
import net.devgrr.interp.log.api.config.mapStruct.CommentMapper;
import net.devgrr.interp.log.api.member.MemberService;
import net.devgrr.interp.log.api.member.entity.Member;
import net.devgrr.interp.log.api.post.PostService;
import net.devgrr.interp.log.api.post.entity.Post;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

  private final CommentRepository commentRepository;
  private final CommentMapper commentMapper;

  private final PostService postService;
  private final MemberService memberService;

  public List<Comment> getCommentsByPostId(Integer postId) {
    return commentRepository.findAllByPostId(postId);
  }

  /*
   * TODO: 2depth 이상 대댓글 또한 중첩 구조로 조회한다.
   * */
  public List<CommentResponse> getCommentsByPostIdNested(Integer postId) {
    List<Comment> comments = getCommentsByPostId(postId);
    return comments.stream()
        .filter(comment -> comment.getParentCommentId() == null)
        .map(
            rootComment -> {
              List<CommentResponse> childComments =
                  comments.stream()
                      .filter(
                          comment ->
                              Objects.equals(comment.getParentCommentId(), rootComment.getId()))
                      .map(commentMapper::toResponse)
                      .collect(Collectors.toList());
              return commentMapper.toResponseWithChildren(rootComment, childComments);
            })
        .collect(Collectors.toList());
  }

  public List<CommentResponse> getCommentsRecursively(Integer postId) {
    List<Comment> comments = getCommentsByPostId(postId);

    List<Comment> rootComments = comments.stream()
            .filter(comment -> comment.getParentCommentId() == null)
            .toList();

    return rootComments.stream()
            .map(rootComment -> buildCommentHierarchy(rootComment, comments))
            .toList();
  }

  private CommentResponse buildCommentHierarchy(Comment rootComment, List<Comment> comments) {
      List<CommentResponse> childComments = comments.stream()
              .filter(comment -> Objects.equals(comment.getParentCommentId(), rootComment.getId()))
              .map(child -> buildCommentHierarchy(child, comments))
              .toList();
      return commentMapper.toResponseWithChildren(rootComment, childComments);
  }

  public Comment existCommentById(Integer id) throws BaseException {
    return commentRepository
        .findById(id)
        .orElseThrow(
            () ->
                new BaseException(
                    ErrorCode.INVALID_INPUT_VALUE, "Comment not found with id: " + id));
  }

  @Transactional
  public Comment setComments(CommentRequest req, String email) throws BaseException {
    Member member = memberService.getUsersByEmail(email);
    Post post = postService.existPostsById(req.postId());
    Comment comment = commentMapper.toComment(req, post, member);
    commentRepository.save(comment);
    return comment;
  }

  @Transactional
  public void putCommentsById(CommentRequest req, String email) throws BaseException {
    Comment comment = existCommentById(req.id());
    if (!comment.getWriter().getEmail().equals(email)) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "수정 권한이 없습니다.");
    }
    Comment updComment = commentMapper.putCommentMapper(req, comment);
    commentRepository.save(updComment);
  }

  @Transactional
  public void delCommentsById(Integer id, String email) throws BaseException {
    Comment comment = existCommentById(id);
    if (!comment.getWriter().getEmail().equals(email)) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "삭제 권한이 없습니다.");
    }
    commentRepository.delete(comment);
  }

  @Transactional
  public void likeCommentsById(Integer id, String email) throws BaseException {
    Comment comment = existCommentById(id);

    if (comment.getWriter().getEmail().equals(email)) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "본인 댓글에는 추천할 수 없습니다.");
    }

    if (comment.getLikes().stream().anyMatch(member -> member.getEmail().equals(email))) {
      // unlike
      comment.getLikes().removeIf(member -> member.getEmail().equals(email));
    } else {
      // like
      comment.getLikes().add(memberService.getUsersByEmail(email));
    }
    commentRepository.save(comment);
  }
}
