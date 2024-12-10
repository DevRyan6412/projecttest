package com.shop.service;

import com.shop.entity.Member;
import com.shop.entity.MileageSummary;
import com.shop.repository.MileageHistoryRepository;
import com.shop.repository.MileageSummaryRepository;
import com.shop.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class MileageServiceTests {

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
        member = new Member();
        member.setId(1L);
        mileageSummary = new MileageSummary();
        mileageSummary.setMember(member);
        mileageSummary.setTotalMileage(1000);
    }

    @Test
    void earnMileage_ShouldIncreaseTotalMileage_WhenEarned() {
        int amount = 500;
        String description = "Test earning mileage";

        when(memberRepository.findById(1L)).thenReturn(java.util.Optional.of(member));
        when(mileageSummaryRepository.findByMember(member)).thenReturn(java.util.Optional.of(mileageSummary));

        mileageService.earnMileage(1L, amount, description);

        assertEquals(1500, mileageSummary.getTotalMileage()); // Total mileage should increase by 500
        verify(mileageSummaryRepository, times(1)).save(mileageSummary); // verify that save was called
    }

    @Test
    void useMileage_ShouldDecreaseTotalMileage_WhenUsed() {
        int amount = 300;
        String description = "Test using mileage";

        when(memberRepository.findById(1L)).thenReturn(java.util.Optional.of(member));
        when(mileageSummaryRepository.findByMember(member)).thenReturn(java.util.Optional.of(mileageSummary));

        mileageService.useMileage(1L, amount, description);

        assertEquals(700, mileageSummary.getTotalMileage()); // Total mileage should decrease by 300
        verify(mileageSummaryRepository, times(1)).save(mileageSummary); // verify that save was called
    }

    @Test
    void processOrder_ShouldUseAndEarnMileage() {
        int purchaseAmount = 1000;
        int mileageUsed = 200;

        when(memberRepository.findById(1L)).thenReturn(java.util.Optional.of(member));
        when(mileageSummaryRepository.findByMember(member)).thenReturn(java.util.Optional.of(mileageSummary));

        mileageService.processOrder(1L, purchaseAmount, mileageUsed);

        assertEquals(700, mileageSummary.getTotalMileage()); // Total mileage should decrease by 200
        assertEquals(200, mileageSummary.getTotalUsed()); // Total used mileage should increase by 200
        verify(mileageSummaryRepository, times(1)).save(mileageSummary); // verify that save was called
    }
}
