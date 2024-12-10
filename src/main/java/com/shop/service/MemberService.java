package com.shop.service;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import com.shop.dto.PasswordUpdateDto;
import com.shop.entity.Member;
import com.shop.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원 저장 관련 메서드
     */
    public Member saveMember(Member member) {
        validateDuplicateMember(member);
        return memberRepository.save(member);
    }

    public void saveMember(MemberFormDto memberFormDto) {
        validateMemberFormFields(memberFormDto);
        Member member = createMemberFromDto(memberFormDto);
        validateDuplicateMember(member);
        memberRepository.save(member);
    }

    private void validateMemberFormFields(MemberFormDto memberFormDto) {
        validateField(memberFormDto.getName(), "이름");
        validateField(memberFormDto.getEmail(), "이메일");
        validateField(memberFormDto.getPassword(), "비밀번호");
        validateField(memberFormDto.getAddress(), "주소");
        validateField(memberFormDto.getPostcode(), "우편번호");
        validateField(memberFormDto.getDetailAddress(), "상세주소");
    }

    private void validateField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(fieldName + "은(는) 필수 입력 값입니다.");
        }
    }

    private Member createMemberFromDto(MemberFormDto memberFormDto) {
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setPassword(passwordEncoder.encode(memberFormDto.getPassword()));
        member.setAddress(memberFormDto.getAddress());
        member.setPostcode(memberFormDto.getPostcode());
        member.setDetailAddress(memberFormDto.getDetailAddress());
        member.setRole(memberFormDto.getRole() != null ? memberFormDto.getRole() : Role.USER);
        member.setBusinessRegistrationNumber(memberFormDto.getBusinessRegistrationNumber());
        return member;
    }

    /**
     * 이메일 검증 관련 메서드
     */
    private void validateDuplicateMember(Member member) {
        Member existingMember = memberRepository.findByEmail(member.getEmail());
        if (existingMember != null) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }
    }

    public boolean validateEmailFormat(String email) {
        return email.matches("[^@]+@[^\\.]+\\..+");
    }

    public boolean isEmailDuplicated(String email) {
        return memberRepository.findByEmail(email) != null;
    }

    /**
     * 회원 조회 관련 메서드
     */
    public Member findMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new IllegalArgumentException("Member not found with email: " + email);
        }
        return member;
    }

    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    /**
     * 회원 정보 수정 관련 메서드
     */
    public void updateFieldByEmail(String email, String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("입력값이 비어있습니다.");
        }

        Member member = findMemberByEmail(email);
        updateMemberField(member, field, value.trim());
        memberRepository.save(member);
    }

    private void updateMemberField(Member member, String field, String value) {
        switch (field) {
            case "name":
                validateName(value);
                member.setName(value);
                break;
            case "email":
                validateEmail(value);
                member.setEmail(value);
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 필드입니다: " + field);
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수 입력 값입니다.");
        }
        if (name.trim().length() < 2) {
            throw new IllegalArgumentException("이름은 2자 이상이어야 합니다.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수 입력 값입니다.");
        }
        if (!validateEmailFormat(email)) {
            throw new IllegalArgumentException("잘못된 이메일 형식입니다.");
        }
        Member existingMember = memberRepository.findByEmail(email.trim());
        if (existingMember != null && !existingMember.getEmail().equals(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
    }

    public void updateAddressByEmail(String email, String address, String postcode, String detailAddress) {
        Member member = findMemberByEmail(email);
        member.setAddress(address);
        member.setPostcode(postcode);
        member.setDetailAddress(detailAddress);
        memberRepository.save(member);
    }

    public void updatePassword(PasswordUpdateDto passwordUpdateDto, String email) {
        Member member = findMemberByEmail(email);
        validatePasswordUpdate(passwordUpdateDto, member);
        member.setPassword(passwordEncoder.encode(passwordUpdateDto.getNewPassword()));
        memberRepository.save(member);
    }

    private void validatePasswordUpdate(PasswordUpdateDto passwordUpdateDto, Member member) {
        if (!passwordEncoder.matches(passwordUpdateDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        if (!passwordUpdateDto.getNewPassword().equals(passwordUpdateDto.getConfirmPassword())) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 카카오 로그인 관련 메서드
     */
    public Member saveKakaoMember(String email, String name) {
        Member member = new Member();
        member.setEmail(email);
        member.setName(name);
        return memberRepository.save(member);
    }

    /**
     * Spring Security 관련 메서드
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email);
        if (member == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                .roles(member.getRole().toString())
                .build();
    }
}