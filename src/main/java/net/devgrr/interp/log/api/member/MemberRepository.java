package net.devgrr.interp.log.api.member;

import java.util.List;
import java.util.Optional;

import jakarta.validation.constraints.Email;
import net.devgrr.interp.log.api.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Integer> {
  List<Member> findAllByIsActiveTrue();

  List<Member> findAllByIsActiveFalse();

  boolean existsByEmail(String email);

  Optional<Member> findByRefreshToken(String refreshToken);

  Optional<Member> findByEmail(String email);

  @Modifying
  @Query("UPDATE Member m SET m.isActive = false , m.updatedDate=NOW() WHERE m.email = :email")
  int deactivateByEmail(String email);

  @Modifying
  @Query("UPDATE Member m SET m.isActive = true, m.updatedDate=NOW() WHERE m.email = :email")
  int activeByEmail(String email);
}
