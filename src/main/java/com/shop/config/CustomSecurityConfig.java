package com.shop.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@Order(101)
public class CustomSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests() // authorizeHttpRequests()는 제거
                .antMatchers("/member/login").permitAll() // 로그인 페이지는 모든 사용자에게 허용
                .anyRequest().authenticated() // 나머지 요청은 인증 필요
                .and()
                .formLogin()
                .loginPage("/member/login")
                .permitAll()
                .and()
                .csrf().disable(); // 테스트용으로 CSRF 비활성화
    }



    public String getKakaoApiKey() {
        return "ba2346a2d161c7902c3dadc74d581c6f";
    }

    public String getKakaoRedirectUri() {
        return "http://localhost:8080/login/oauth2/code/kakao"; // 실제 리디렉션 URI
    }
}
