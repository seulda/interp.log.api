package net.devgrr.interp.log.api.member;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.log.api.config.exception.BaseException;
import net.devgrr.interp.log.api.config.exception.ErrorCode;
import net.devgrr.interp.log.api.config.mapStruct.MemberMapper;
import net.devgrr.interp.log.api.member.dto.MemberRequest;
import net.devgrr.interp.log.api.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberMapper memberMapper;

  public List<Member> getUsers(String isActive) throws BaseException {
    if (isActive == null) {
      return memberRepository.findAll();
    } else if (isActive.equals("true")) {
      return memberRepository.findAllByIsActiveTrue();
    } else if (isActive.equals("false")) {
      return memberRepository.findAllByIsActiveFalse();
    } else {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
    }
  }

  public Member getUsersById(String userId) throws BaseException {
    Member member = memberRepository.findByUserId(userId).orElse(null);
    if (member == null) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 ID 입니다.");
    }
    return member;
  }

  @Transactional
  public Member setUsers(MemberRequest req) throws BaseException {
    if (memberRepository.existsByUserId(req.userId())) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "이미 존재하는 ID 입니다.");
    } else if (memberRepository.existsByEmail(req.email())) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "이미 존재하는 Email 입니다.");
    }

    Member member = memberMapper.toMember(req);
    try {
      memberRepository.save(member);
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
    }
    return member;
  }

  public Member getUsersByEmail(String email) throws BaseException {
    Member member = memberRepository.findByEmail(email).orElse(null);
    if (member == null) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 Email 입니다.");
    }
    return member;
  }

  @Transactional
  public Member putUsers(UserDetails userDetails, MemberUpdateRequest req) throws BaseException {
    Member member =
        memberRepository
            .findByEmail(userDetails.getUsername())
            .orElseThrow(
                () -> new BaseException(ErrorCode.INVALID_INPUT_VALUE, "저장된 로그인 정보가 없습니다."));

    if (!member.getId().equals(req.id())) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "회원 정보가 일치하지 않습니다.");
    }
    if (StringUtils.hasText(req.email())) {
      if (memberRepository.existsByEmail(req.email())) {
        throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "이미 존재하는 Email 입니다.");
      }
    }
    memberMapper.updateMember(req, member);
    try {
      memberRepository.save(member);
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
    }
    memberRepository.deactivateByUserId(userId);
    return member;
  }

  }
}
