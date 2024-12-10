package com.shop.controller;

import com.shop.entity.Member;
import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    // 로그인한 회원 정보 반환
    @GetMapping("/info")
    public Long getLoggedInMemberId(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new IllegalStateException("로그인한 사용자가 없습니다.");
        }
        Member member = memberService.findByEmail(user.getUsername());
        if (member == null) {
            throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
        }
        return member.getId(); // 현재 로그인한 사용자의 ID 반환
    }
}
