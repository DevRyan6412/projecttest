package com.shop.entity;

import com.shop.constant.Role;
import com.shop.dto.MemberFormDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name="member")
@Getter
@Setter
@ToString
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(name = "address", nullable = true)
    private String address;

    @Column(nullable = false) // 우편번호 필드
    private String postcode;

    @Column(nullable = false) // 상세주소 필드
    private String detailAddress;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = true)
//    @Size(min = 10, max = 10, message = "사업자 등록번호는 정확히 10자리여야 합니다.")
    private String businessRegistrationNumber;

    public static Member createMember(MemberFormDto memberFormDto, PasswordEncoder passwordEncoder) {
        Member member = new Member();
        member.setName(memberFormDto.getName());
        member.setEmail(memberFormDto.getEmail());
        member.setAddress(memberFormDto.getAddress());
        member.setPostcode(memberFormDto.getPostcode());
        member.setDetailAddress(memberFormDto.getDetailAddress());
        String password = passwordEncoder.encode(memberFormDto.getPassword());
        member.setPassword(password);
        member.setRole(Role.ADMIN);
        return member;
    }
}
