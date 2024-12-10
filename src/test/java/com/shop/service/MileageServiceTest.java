package com.shop.service;

import com.shop.entity.Member;
import com.shop.entity.MileageSummary;
import com.shop.repository.MemberRepository;
import com.shop.repository.MileageHistoryRepository;
import com.shop.repository.MileageSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class MileageServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MileageSummaryRepository mileageSummaryRepository;

    @Mock
    private MileageHistoryRepository mileageHistoryRepository;

    @InjectMocks
    private MileageService mileageService;

    private Member member;
    private MileageSummary mileageSummary;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 회원과 마일리지 요약 객체를 설정
        member = new Member();
        member.setId(1L);

        mileageSummary = new MileageSummary();
        mileageSummary.setMember(member);
        mileageSummary.setTotalMileage(1000);
        mileageSummary.setTotalEarned(500);
        mileageSummary.setTotalUsed(200);
    }

    @Test
    void testUseMileage_Success() {
        int amount = 300;
        String description = "Purchase of product";

        // Mock: memberRepository.findById 호출 시 member를 반환
        when(memberRepository.findById(member.getId())).thenReturn(java.util.Optional.of(member));
        when(mileageSummaryRepository.findByMember(member)).thenReturn(java.util.Optional.of(mileageSummary));

        // 서비스 메서드 호출
        mileageService.useMileage(member.getId(), amount, description);

        // Verify: 마일리지 요약이 업데이트 되었는지 확인
        verify(mileageSummaryRepository, times(1)).save(mileageSummary);
        assertEquals(700, mileageSummary.getTotalMileage());  // 총 마일리지 차감 후
        assertEquals(500, mileageSummary.getTotalUsed());    // 누적 사용 마일리지 증가
    }

    @Test
    void testUseMileage_InsufficientMileage() {
        int amount = 2000; // 부족한 마일리지
        String description = "Purchase of product";

        // Mock: memberRepository.findById 호출 시 member를 반환
        when(memberRepository.findById(member.getId())).thenReturn(java.util.Optional.of(member));
        when(mileageSummaryRepository.findByMember(member)).thenReturn(java.util.Optional.of(mileageSummary));

        // 예상되는 예외 처리
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            mileageService.useMileage(member.getId(), amount, description);
        });

        assertEquals("Not enough mileage to complete the transaction.", exception.getMessage());
    }
}
