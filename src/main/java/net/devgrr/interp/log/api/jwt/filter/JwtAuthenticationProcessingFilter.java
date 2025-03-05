package net.devgrr.interp.log.api.jwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import net.devgrr.interp.log.api.jwt.JwtService;
import net.devgrr.interp.log.api.member.MemberRepository;
import net.devgrr.interp.log.api.member.entity.Member;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final MemberRepository memberRepository;
  private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // RefreshToken 존재 -> 유효하면 AccessToken 재발급
    String refreshToken =
        jwtService.extractRefreshToken(request).filter(jwtService::isTokenValid).orElse(null);
    if (refreshToken != null) {
      checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
      return;
    }
    // AccessToken 만 존재 -> AccessToken 검사 후 유저정보 저장, 필터 계속 진행
    checkAccessTokenAndAuthentication(request, response, filterChain);
  }

  /// refresh token 만료 x -> 새로운 access
  /// refresh token 만료 o -> 저장되어있던 refresh token 삭제, 401 반환 (재로그인 필요)
  /// refresh token 만료 시간동안 클라이언트의 api 요청이 없으면 재로그인 필요
  private void checkRefreshTokenAndReIssueAccessToken(
      HttpServletResponse response, String refreshToken) {
    Optional<Member> member = memberRepository.findByRefreshToken(refreshToken);
    if (jwtService.isTokenValid(refreshToken)) {
      member.ifPresent(
          m -> {
            jwtService.sendAccessToken(response, jwtService.createAccessToken(m.getEmail()));
//            jwtService.sendRefreshToken(response, jwtService.createRefreshToken());
//            jwtService.updateRefreshToken(m.getEmail(), refreshToken);
          });
    } else {
      member.ifPresent(
          m -> {
            jwtService.destroyRefreshToken(m.getEmail());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          });
    }
  }

  private void checkAccessTokenAndAuthentication(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    jwtService
        .extractAccessToken(request)
        .filter(jwtService::isTokenValid)
        .flatMap(
            accessToken ->
                jwtService.extractUserId(accessToken).flatMap(memberRepository::findByEmail))
        .ifPresent(this::saveAuthentication);

    filterChain.doFilter(request, response);
  }

  private void saveAuthentication(Member member) {
    UserDetails user =
        User.builder()
            .username(member.getEmail())
            .password(member.getPassword())
            .roles(member.getRole().name())
            .build();

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(
            user, null, authoritiesMapper.mapAuthorities(user.getAuthorities()));

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);
  }
}
