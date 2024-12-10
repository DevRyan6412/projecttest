package com.shop.service;

import com.shop.dto.MileageHistoryDTO;
import com.shop.dto.MileageSummaryDTO;
import com.shop.entity.Member;
import com.shop.entity.MileageHistory;
import com.shop.entity.MileageSummary;
import com.shop.repository.MemberRepository;
import com.shop.repository.MileageHistoryRepository;
import com.shop.repository.MileageSummaryRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MileageService {

    private final MemberRepository memberRepository;
    private final MileageHistoryRepository mileageHistoryRepository;
    private final MileageSummaryRepository mileageSummaryRepository;

    public MileageService(MemberRepository memberRepository, MileageHistoryRepository mileageHistoryRepository, MileageSummaryRepository mileageSummaryRepository) {
        this.memberRepository = memberRepository;
        this.mileageHistoryRepository = mileageHistoryRepository;
        this.mileageSummaryRepository = mileageSummaryRepository;
    }

    // 마일리지 적립
    @Transactional
    public void earnMileage(Long memberId, int amount, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Mileage amount must be greater than 0");
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found with ID: " + memberId));

        MileageSummary summary = mileageSummaryRepository.findByMember(member)
                .orElseGet(() -> {
                    MileageSummary newSummary = new MileageSummary();
                    newSummary.setMember(member);
                    newSummary.setTotalMileage(0);
                    newSummary.setTotalEarned(0);
                    newSummary.setTotalUsed(0);
                    newSummary.setLastUpdated(LocalDateTime.now());
                    return mileageSummaryRepository.save(newSummary);
                });

        summary.setTotalMileage(summary.getTotalMileage() + amount);
        summary.setTotalEarned(summary.getTotalEarned() + amount);
        summary.setLastUpdated(LocalDateTime.now());
        mileageSummaryRepository.save(summary);

        MileageHistory history = new MileageHistory();
        history.setMember(member);
        history.setAmount(amount);
        history.setType("earn");
        history.setDescription(description);
        history.setCreatedAt(LocalDateTime.now());
        mileageHistoryRepository.save(history);
    }

    // 마일리지 사용
    @Transactional
    public void useMileage(Long memberId, int amount, String description) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        MileageSummary summary = mileageSummaryRepository.findByMember(member)
                .orElseThrow(() -> new RuntimeException("No mileage summary found for the member."));

        if (summary.getTotalMileage() < amount) {
            throw new RuntimeException("Not enough mileage to complete the transaction.");
        }

        summary.setTotalMileage(summary.getTotalMileage() - amount);
        summary.setTotalUsed(summary.getTotalUsed() + amount);
        summary.setLastUpdated(LocalDateTime.now());
        mileageSummaryRepository.save(summary);

        MileageHistory history = new MileageHistory();
        history.setMember(member);
        history.setAmount(-amount);
        history.setType("use");
        history.setDescription(description);
        history.setCreatedAt(LocalDateTime.now());
        mileageHistoryRepository.save(history);
    }

    // 결제 완료 시 사용 및 적립 처리
    @Transactional
    public void processOrder(Long memberId, int purchaseAmount, int mileageUsed) {
        if (mileageUsed > 0) {
            useMileage(memberId, mileageUsed, "결제 사용");
        }

        int netPaymentAmount = purchaseAmount - mileageUsed;

        if (netPaymentAmount > 0) {
            int mileageToEarn = (int) Math.round(netPaymentAmount * 0.05);

            if (mileageToEarn > 0) {
                earnMileage(memberId, mileageToEarn, "결제 적립");
            }
        }
    }

    // 마일리지 요약 조회
    @Transactional
    public MileageSummaryDTO getMileageSummary(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        MileageSummary summary = mileageSummaryRepository.findByMember(member)
                .orElseThrow(() -> new RuntimeException("Mileage summary not found"));

        return new MileageSummaryDTO(
                summary.getTotalMileage(),
                summary.getTotalEarned(),
                summary.getTotalUsed(),
                summary.getLastUpdated()
        );
    }

    // 마일리지 내역 조회
    @Transactional
    public List<MileageHistoryDTO> getMileageHistory(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        List<MileageHistory> histories = mileageHistoryRepository.findByMember(member);
        return histories.stream().map(history -> new MileageHistoryDTO(
                history.getAmount(),
                history.getType(),
                history.getDescription(),
                history.getCreatedAt()
        )).collect(Collectors.toList());
    }
}
