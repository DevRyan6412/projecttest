package com.shop.service;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder; // 비밀번호 암호화를 위한 의존성

    public boolean isEmailDuplicate(String email) {
        return memberRepository.existsByEmail(email);
    }

    // 기존 saveMember: Member 객체를 직접 받아 저장
    public Member saveMember(Member member) {
        validateDuplicateMember(member); // 중복 회원 검증
        return memberRepository.save(member);
    }

    // 새로운 saveMember: MemberFormDto를 받아 저장
    public void saveMember(MemberFormDto memberFormDto) {
        System.out.println("서비스 레벨에서 처리된 이메일: [" + memberFormDto.getEmail() + "]");


        // DTO 데이터를 엔티티로 변환
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setPassword(passwordEncoder.encode(memberFormDto.getPassword())); // 비밀번호 암호화
        member.setAddress(memberFormDto.getAddress());
        member.setPostcode(memberFormDto.getPostcode());
        member.setDetailAddress(memberFormDto.getDetailAddress());
        member.setRole(Role.valueOf(memberFormDto.getRole())); // USER 또는 MANAGER 설정

        // 사업자 등록번호 저장 (판매자일 경우)
        if ("MANAGER".equals(memberFormDto.getRole())) {
            member.setBusinessRegistrationNumber(memberFormDto.getBusinessRegistrationNumber());
        }

        validateDuplicateMember(member); // 중복 검증
        memberRepository.save(member); // 저장
    }

    public Member saveKakaoMember(String email, String name) {
        Member member = new Member();
        member.setEmail(email);
        member.setName(name);
        member.setRole(Role.USER); // 기본적으로 USER 설정
        return memberRepository.save(member);
    }


    // 중복 회원 검증
    private void validateDuplicateMember(Member member) {
        if (memberRepository.findByEmail(member.getEmail()) != null) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
    }

    // Spring Security의 UserDetailsService 구현: 인증 처리
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);

        if (member == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // 디버깅용 로그
        System.out.println("User found: " + member.getEmail());
        System.out.println("Password in DB: " + member.getPassword());

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }



    // 이메일 중복 여부 확인 메서드
    public boolean isEmailDuplicated(String email) {
        if (!email.matches("[^@]+@[^\\.]+\\..+")) {
            throw new IllegalArgumentException("잘못된 이메일 형식입니다.");
        }

        Member member = memberRepository.findByEmail(email);
        return member != null; // 중복된 이메일이 있으면 true 반환
    }

    //내정보 조회 메서드
    public Member findMemberByEmail(String email) {
        System.out.println("Looking for member with email: " + email); // 디버깅용 메시지

        // 회원 조회
        Member member = memberRepository.findByEmail(email);

        // 회원이 없을 경우 예외 처리
        if (member == null) {
            System.out.println("Member not found for email: " + email);
            throw new IllegalArgumentException("Member not found with email: " + email);
        }

        System.out.println("Found member: " + member); // 조회된 회원 정보 출력
        return member; // 회원 반환
    }



    //내정보 수정

    public void updateFieldByEmail(String email, String field, String value) {
        // 이메일로 회원 찾기
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 필드 업데이트
        switch (field) {
            case "name":
                member.setName(value);
                break;
            case "email":
                member.setEmail(value);
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 필드입니다: " + field);
        }

        // 변경 사항 강제 저장
        memberRepository.save(member);
    }



    public void updateAddressByEmail(String email, String address, String postcode, String detailAddress) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        member.setAddress(address);
        member.setPostcode(postcode);
        member.setDetailAddress(detailAddress);
    }

    public void changePassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        System.out.println("서비스 호출됨 - email: " + email);

        // 사용자 정보 조회
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            System.out.println("사용자를 찾을 수 없습니다.");
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            System.out.println("현재 비밀번호가 일치하지 않습니다.");
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호와 확인 비밀번호 비교
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            throw new IllegalArgumentException("새 비밀번호가 서로 일치하지 않습니다.");
        }

        // 비밀번호 변경
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
        System.out.println("비밀번호 변경 성공!");
    }








    //==================================================== 회원 조회 =====================================================
        public List<Member> findAllMembers() {
            return memberRepository.findAll();
        }



}

