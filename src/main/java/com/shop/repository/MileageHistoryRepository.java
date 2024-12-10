package com.shop.repository;

import com.shop.entity.Member;
import com.shop.entity.MileageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MileageHistoryRepository extends JpaRepository<MileageHistory, Long> {
    // 특정 회원의 마일리지 내역을 가져옵니다.
    List<MileageHistory> findByMember(Member member);
}
