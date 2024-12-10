package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.dto.PasswordUpdateDto;
import com.shop.entity.Member;
import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원가입 관련 메서드
     */
    @GetMapping("/new")
    public String showMemberForm(Model model) {
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }

    @PostMapping("/new")
    public String registerMember(@Valid MemberFormDto memberFormDto,
                                 BindingResult bindingResult,
                                 Model model) {
        // 기본 유효성 검사
        if (bindingResult.hasErrors()) {
            return "member/memberForm";
        }

        // 이메일 형식 및 중복 검사
        if (!memberService.validateEmailFormat(memberFormDto.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "잘못된 이메일 형식입니다.");
            return "member/memberForm";
        }

        if (memberService.isEmailDuplicated(memberFormDto.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 일치 검사
        if (!memberFormDto.getPassword().equals(memberFormDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword",
                    "비밀번호가 일치하지 않습니다.");
        }

        // 판매자 역할 시 사업자등록번호 검사
        if ("MANAGER".equals(memberFormDto.getRole()) &&
                (memberFormDto.getBusinessRegistrationNumber() == null ||
                        memberFormDto.getBusinessRegistrationNumber().isEmpty())) {
            bindingResult.rejectValue("businessRegistrationNumber",
                    "error.businessRegistrationNumber",
                    "사업자 등록번호를 입력해주세요.");
        }

        if (bindingResult.hasErrors()) {
            return "member/memberForm";
        }

        try {
            memberService.saveMember(memberFormDto);
            return "redirect:/";
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }
    }

    /**
     * 로그인 관련 메서드
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "/member/memberLoginForm";
    }

    @GetMapping("/login/error")
    public String handleLoginError(Model model) {
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        return "/member/memberLoginForm";
    }

    /**
     * 회원 정보 조회 관련 메서드
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailDuplicate(@RequestParam String email) {
        try {
            boolean isDuplicated = memberService.isEmailDuplicated(email);
            return ResponseEntity.ok(isDuplicated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(false);
        }
    }

    @GetMapping("/mypage")
    public String showMyInfo(Authentication authentication, Model model) {
        String email = extractEmailFromPrincipal(authentication.getPrincipal());
        if (email == null) {
            model.addAttribute("errorMessage", "인증 정보를 확인할 수 없습니다.");
            return "error/errorPage";
        }

        Member member = memberService.findMemberByEmail(email);
        model.addAttribute("member", member);
        return "member/mypage";
    }

    @GetMapping("/list")
    public String listMembers(Model model) {
        List<Member> members = memberService.findAllMembers();
        model.addAttribute("members", members);
        return "member/memberList";
    }

    /**
     * 회원 정보 수정 관련 메서드
     */
    @PatchMapping("/update/name")
    public ResponseEntity<?> updateName(@RequestBody Map<String, String> payload,
                                        Principal principal) {
        String email = extractEmailFromPrincipal(principal);
        if (email == null) {
            return ResponseEntity.badRequest().body("사용자 정보를 가져올 수 없습니다.");
        }

        String newName = payload.get("value");
        if (newName == null || newName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("이름은 필수 입력 값입니다.");
        }

        try {
            memberService.updateFieldByEmail(email, "name", newName);
            return ResponseEntity.ok("이름이 성공적으로 업데이트되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/update/email")
    public ResponseEntity<?> updateEmail(@RequestBody Map<String, String> payload,
                                         Principal principal) {
        String email = extractEmailFromPrincipal(principal);
        if (email == null) {
            return ResponseEntity.badRequest().body("사용자 정보를 가져올 수 없습니다.");
        }

        String newEmail = payload.get("value");
        if (newEmail == null || newEmail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("이메일은 필수 입력 값입니다.");
        }

        try {
            memberService.updateFieldByEmail(email, "email", newEmail);
            return ResponseEntity.ok("이메일이 성공적으로 업데이트되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/update/address")
    public ResponseEntity<?> updateAddress(@RequestBody Map<String, String> payload,
                                           Principal principal) {
        String email = extractEmailFromPrincipal(principal);
        if (email == null) {
            return ResponseEntity.badRequest().body("사용자 정보를 가져올 수 없습니다.");
        }

        try {
            memberService.updateAddressByEmail(
                    email,
                    payload.get("address"),
                    payload.get("postcode"),
                    payload.get("detailAddress")
            );
            return ResponseEntity.ok("주소가 성공적으로 업데이트되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/update/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateDto passwordUpdateDto,
                                            Principal principal) {
        String email = extractEmailFromPrincipal(principal);
        if (email == null) {
            return ResponseEntity.badRequest().body("사용자 정보를 가져올 수 없습니다.");
        }

        try {
            memberService.updatePassword(passwordUpdateDto, email);
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * 소셜 로그인 관련 메서드
     */
    @GetMapping("/kakao/callback")
    public String handleKakaoCallback(@AuthenticationPrincipal OAuth2User oAuth2User,
                                      Model model) {
        try {
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            if (kakaoAccount == null) {
                model.addAttribute("errorMessage", "카카오 계정 정보를 찾을 수 없습니다.");
                return "error/errorPage";
            }

            String email = (String) kakaoAccount.get("email");
            String nickname = (String) ((Map<String, Object>) oAuth2User
                    .getAttribute("properties")).get("nickname");

            if (email == null || email.isEmpty()) {
                model.addAttribute("errorMessage", "카카오 계정에서 이메일 정보를 찾을 수 없습니다.");
                return "error/errorPage";
            }

            Member member = memberService.saveKakaoMember(email, nickname);
            model.addAttribute("member", member);
            return "member/mypage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "카카오 로그인 처리 중 오류가 발생했습니다.");
            return "error/errorPage";
        }
    }

    /**
     * 유틸리티 메서드
     */
    private String extractEmailFromPrincipal(Object principal) {
        // Principal이 직접 User 타입인 경우
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        // Principal이 OAuth2User 타입인 경우
        else if (principal instanceof DefaultOAuth2User) {
            DefaultOAuth2User oauth2User = (DefaultOAuth2User) principal;
            Map<String, Object> attributes = oauth2User.getAttributes();

            if (attributes.containsKey("kakao_account")) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                return (String) kakaoAccount.get("email");
            }
            return (String) attributes.get("email");
        }
        // Principal이 Authentication 타입인 경우
        else if (principal instanceof Authentication) {
            return extractEmailFromPrincipal(((Authentication) principal).getPrincipal());
        }

        throw new IllegalArgumentException("이메일을 가져올 수 없습니다.");
    }
}