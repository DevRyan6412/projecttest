package com.shop.service;

import com.shop.config.CustomSecurityConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    private final CustomSecurityConfig kakaoConfig;

    // 1. Access Token 요청
    public String getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 파라미터 설정
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", kakaoConfig.getKakaoApiKey());
        params.put("redirect_uri", kakaoConfig.getKakaoRedirectUri());
        params.put("code", code);

        // POST 요청 보내기
        Map<String, Object> response = restTemplate.postForObject(
                "https://kauth.kakao.com/oauth/token",
                params,
                Map.class
        );

        // Access Token 추출
        if (response != null && response.containsKey("access_token")) {
            return (String) response.get("access_token");
        } else {
            throw new RuntimeException("Access Token 요청 실패: " + response);
        }
    }

    // 2. 사용자 정보 요청
    public String getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        // HTTP 헤더 설정
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + accessToken);

        // 사용자 정보 요청
        Map<String, Object> response = restTemplate.getForObject(
                "https://kapi.kakao.com/v2/user/me",
                Map.class,
                headers
        );

        // 사용자 정보 반환
        if (response != null) {
            return response.toString();
        } else {
            throw new RuntimeException("사용자 정보 요청 실패");
        }
    }
}
