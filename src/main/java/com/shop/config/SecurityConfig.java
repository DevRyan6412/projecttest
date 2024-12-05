package com.shop.config;

import com.shop.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Lazy
    @Autowired
    private MemberService memberService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 로그인 설정
        http.formLogin()
                .loginPage("/members/login") // 사용자 정의 로그인 페이지 경로
                .defaultSuccessUrl("/") // 로그인 성공 후 이동 경로
                .usernameParameter("email") // 로그인 시 이메일 사용
                .failureUrl("/members/login/error") // 로그인 실패 시 이동 경로
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/members/logout")) // 로그아웃 경로
                .logoutSuccessUrl("/") // 로그아웃 성공 후 이동 경로
                .invalidateHttpSession(true); // 세션 무효화

        // OAuth2 로그인 설정
        http.oauth2Login()
                .loginPage("/members/login") // OAuth 로그인 페이지 경로
                .defaultSuccessUrl("/") // 로그인 성공 후 이동 경로
                .failureUrl("/members/login/error"); // 로그인 실패 시 이동 경로

        // 권한 설정
        http.authorizeRequests()
                .mvcMatchers("/", "/members/**", "/item/**", "/images/**", "/oauth2/**").permitAll() // 인증 없이 접근 가능
                .mvcMatchers("/seller/**").hasRole("MANAGER") // 판매자 권한만 접근 가능
                .mvcMatchers("/admin/**").hasRole("ADMIN") // 관리자 권한만 접근 가능
                .mvcMatchers("/mypage", "/members/update/password").authenticated() // /mypage는 인증 필요
                .anyRequest().authenticated(); // 그 외 모든 요청은 인증 필요

        // CSRF 설정: 쿠키 기반으로 설정
        http.csrf()
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());

        // 인증되지 않은 사용자 처리
        http.exceptionHandling()
                .authenticationEntryPoint(new CustomAuthenticationEntryPoint());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 정적 자원 인증 무시
        web.ignoring().antMatchers("/css/**", "/js/**", "/img/**");
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 사용자 인증 서비스와 암호화 방식 설정
        auth.userDetailsService(memberService)
                .passwordEncoder(passwordEncoder());
    }

    // 비밀번호 암호화 빈 등록
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
