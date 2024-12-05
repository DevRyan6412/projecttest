package com.shop.controller;

import com.shop.dto.MemberFormDto;
import com.shop.dto.PasswordUpdateDto;
import com.shop.entity.Member;
import com.shop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // ====================== 회원가입 관련 ======================

    /**
     * 회원가입 폼 화면
     */
    @GetMapping("/new")
    public String memberForm(Model model) {
        model.addAttribute("memberFormDto", new MemberFormDto());
        return "member/memberForm";
    }

    /**
     * 회원가입 요청 처리
     */
    @PostMapping("/new")
    public String memberForm(@Valid MemberFormDto memberFormDto, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "member/memberForm";
        }

        if (memberService.isEmailDuplicated(memberFormDto.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "이미 사용 중인 이메일입니다.");
        }

        if (!memberFormDto.getPassword().equals(memberFormDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.confirmPassword", "비밀번호가 일치하지 않습니다.");
        }

        if ("MANAGER".equals(memberFormDto.getRole()) &&
                (memberFormDto.getBusinessRegistrationNumber() == null || memberFormDto.getBusinessRegistrationNumber().isEmpty())) {
            bindingResult.rejectValue("businessRegistrationNumber", "error.businessRegistrationNumber", "사업자 등록번호를 입력해주세요.");
        }

        if (bindingResult.hasErrors()) {
            return "member/memberForm";
        }

        try {
            memberService.saveMember(memberFormDto);
        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "member/memberForm";
        }

        return "redirect:/"; // 성공 시 메인 페이지로 리다이렉트
    }

    // ====================== 로그인 관련 ======================

    /**
     * 로그인 화면
     */
    @GetMapping("/login")
    public String loginMember() {
        return "/member/memberLoginForm";
    }

    /**
     * 로그인 에러 처리
     */
    @GetMapping("/login/error")
    public String loginError(Model model) {
        model.addAttribute("loginErrorMsg", "아이디 또는 비밀번호를 확인해주세요");
        return "/member/memberLoginForm";
    }

    // ====================== 이메일 중복 확인 ======================

    /**
     * 이메일 중복 확인 API
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

    // ====================== 내 정보 조회 및 수정 ======================

    /**
     * 내 정보 조회 페이지
     */
    @GetMapping("/mypage")
    public String myInfo(Authentication authentication, Model model) {
        try {
            String email = extractEmailFromPrincipal(authentication.getPrincipal());

            if (email == null || email.isEmpty()) {
                model.addAttribute("errorMessage", "인증 정보를 확인할 수 없습니다.");
                return "error/errorPage";
            }

            Member member = memberService.findMemberByEmail(email);
            if (member == null) {
                model.addAttribute("errorMessage", "회원 정보를 찾을 수 없습니다.");
                return "error/errorPage";
            }

            model.addAttribute("member", member);
            return "member/mypage";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "회원 정보를 불러오는 중 오류가 발생했습니다.");
            return "error/errorPage";
        }
    }

    /**
     * 내 정보 수정 폼
     */
    @GetMapping("/edit")
    public String editMember(@AuthenticationPrincipal Object principal, Model model) {
        try {
            String email = extractEmailFromPrincipal(principal);
            if (email == null || email.isEmpty()) {
                model.addAttribute("errorMessage", "이메일 정보를 찾을 수 없습니다.");
                return "error/errorPage";
            }

            Member member = memberService.findMemberByEmail(email);
            if (member == null) {
                model.addAttribute("errorMessage", "회원 정보를 찾을 수 없습니다.");
                return "error/errorPage";
            }

            MemberFormDto memberFormDto = new MemberFormDto();
            memberFormDto.setName(member.getName());
            memberFormDto.setEmail(member.getEmail());
            memberFormDto.setAddress(member.getAddress());
            memberFormDto.setDetailAddress(member.getDetailAddress());
            memberFormDto.setPostcode(member.getPostcode());

            model.addAttribute("memberFormDto", memberFormDto);
               return "member/editForm";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "회원 정보를 불러오는 중 오류가 발생했습니다.");
            return "error/errorPage";
        }
    }

    /**
     * 내 정보 수정 요청 처리
     */
    @PatchMapping("/update/{field}")
    public ResponseEntity<?> updateMember(
            @PathVariable String field,
            @RequestBody Map<String, String> payload,
            Principal principal) {
        // OAuth2AuthenticationToken 처리
        String email = null;

        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) principal;
            Map<String, Object> attributes = oAuth2Token.getPrincipal().getAttributes();

            // 카카오 계정에서 이메일 추출
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");
        } else {
            // 일반 계정인 경우
            email = principal.getName();
        }

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("이메일을 가져올 수 없습니다.");
        }

        String value = payload.get("value");

        if (value == null || value.isEmpty()) {
            return ResponseEntity.badRequest().body("값이 비어있습니다.");
        }

        try {
            memberService.updateFieldByEmail(email, field, value);
            return ResponseEntity.ok().body("업데이트 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PatchMapping("/update/address")
    public ResponseEntity<?> updateAddress(
            @RequestBody Map<String, String> payload,
            Principal principal) {
        // OAuth2AuthenticationToken 처리
        String email = null;

        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oAuth2Token = (OAuth2AuthenticationToken) principal;
            Map<String, Object> attributes = oAuth2Token.getPrincipal().getAttributes();

            // 카카오 계정에서 이메일 추출
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            email = (String) kakaoAccount.get("email");
        } else {
            // 일반 계정인 경우
            email = principal.getName();
        }

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("이메일을 가져올 수 없습니다.");
        }

        // payload에서 주소 관련 데이터 추출
        String address = payload.get("address");
        String postcode = payload.get("postcode");
        String detailAddress = payload.get("detailAddress");

        if (address == null || postcode == null || detailAddress == null ||
                address.isEmpty() || postcode.isEmpty() || detailAddress.isEmpty()) {
            return ResponseEntity.badRequest().body("주소 정보가 비어있습니다.");
        }

        try {
            memberService.updateAddressByEmail(email, address, postcode, detailAddress);
            return ResponseEntity.ok().body("주소 업데이트 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/members/update/password")
    public ResponseEntity<?> updatePassword(@RequestBody PasswordUpdateDto passwordUpdateDto, Principal principal) {
        System.out.println("PATCH /members/update/password 요청 도달");
        System.out.println("전달된 데이터: " + passwordUpdateDto);
        String username = principal.getName();
        System.out.println("현재 사용자: " + username);

        try {
            memberService.changePassword(username, passwordUpdateDto.getCurrentPassword(),
                    passwordUpdateDto.getNewPassword(), passwordUpdateDto.getConfirmPassword());
            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("에러 발생: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }





    // ====================== 유틸리티 메서드 ======================

    private String extractEmailFromPrincipal(Object principal) {
        if (principal instanceof User) {
            return ((User) principal).getUsername();
        } else if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            if (kakaoAccount != null) {
                return (String) kakaoAccount.get("email");
            }
        }
        return null;
    }

    // ====================== 카카오 로그인 ======================

    /**
     * 카카오 로그인 인증 콜백
     */
    @GetMapping("/kakao/callback")
    public String kakaoCallback(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        try {
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            if (kakaoAccount == null) {
                model.addAttribute("errorMessage", "카카오 계정 정보를 찾을 수 없습니다.");
                return "error/errorPage";
            }

            String email = (String) kakaoAccount.get("email");
            String nickname = (String) ((Map<String, Object>) oAuth2User.getAttribute("properties")).get("nickname");

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

    // ====================== 회원 목록 조회 ======================

    /**
     * 회원 목록 조회
     */
    @GetMapping("/list")
    public String listMembers(Model model) {
        List<Member> members = memberService.findAllMembers();
        model.addAttribute("members", members);
        return "member/memberList";
    }
}
