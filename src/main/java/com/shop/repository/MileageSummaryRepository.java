package com.shop.repository;

import com.shop.entity.Member;
import com.shop.entity.MileageSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MileageSummaryRepository extends JpaRepository<MileageSummary, Long> {
    // 특정 회원의 마일리지 요약 정보를 가져옵니다.
    Optional<MileageSummary> findByMember(Member member);

    // 특정 회원의 마일리지 요약 정보를 memberId로 가져옵니다.
    Optional<MileageSummary> findByMemberId(Long memberId);
}
