package com.shop.service;

import com.shop.constant.Role;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

        // 사용자 정보 가져오기
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

        String email = (String) kakaoAccount.get("email");
        String nickname = (String) ((Map<String, Object>) attributes.get("properties")).get("nickname");

        // 이메일 확인 및 로그
        logger.info("카카오 이메일: {}", email);
        logger.info("카카오 닉네임: {}", nickname);

        if (email == null) {
            throw new IllegalArgumentException("카카오 계정에 이메일 정보가 없습니다.");
        }

        // 회원 찾기 또는 생성
        Member member = getOrCreateMember(email, nickname);

        // 권한 설정
        String role = member.getRole().name();

        // OAuth2User 반환
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(role)),
                attributes,
                "id"
        );
    }

    /**
     * 이메일로 회원 찾기 또는 새 회원 생성
     *
     * @param email    카카오 이메일
     * @param nickname 카카오 닉네임
     * @return Member 객체
     */
    private Member getOrCreateMember(String email, String nickname) {
        // 이메일로 기존 회원 검색
        Member member = memberRepository.findByEmail(email);

        // 회원이 없으면 새로 생성
        if (member == null) {
            member = saveKakaoMember(email, nickname);
        }

        return member;
    }

    /**
     * 새 카카오 회원 저장 메서드
     *
     * @param email    카카오 이메일
     * @param nickname 카카오 닉네임
     * @return 저장된 Member 객체
     */
    private Member saveKakaoMember(String email, String nickname) {
        Member member = new Member();
        member.setEmail(email);
        member.setName(nickname);
        member.setRole(Role.USER); // 기본 역할 설정

        // 기본값 설정
        member.setPostcode("00000");
        member.setAddress("카카오 회원");
        member.setDetailAddress("카카오 회원 기본 주소");

        // 기본 비밀번호 설정 및 암호화
        member.setPassword(passwordEncoder.encode("goott7533"));

        logger.info("새로운 카카오 회원 생성: {}", member);
        return memberRepository.save(member);
    }
}

