package com.shop.controller;

import com.shop.dto.MileageEarnRequestDTO;
import com.shop.dto.MileageHistoryDTO;
import com.shop.dto.MileageSummaryDTO;
import com.shop.dto.MileageUseRequestDTO;
import com.shop.service.MemberService;
import com.shop.service.MileageService;
import com.shop.entity.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MileageController.class)  // Controller 테스트
class MileageControllerTest {

    @MockBean  // Spring 컨텍스트에 'MemberService' 빈을 Mock으로 주입
    private MemberService memberService;

    @MockBean  // 'MileageService'도 MockBean으로 등록
    private MileageService mileageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // 테스트를 위한 MockMvc 객체 초기화
        mockMvc = MockMvcBuilders.standaloneSetup(new MileageController(mileageService, memberService)).build();
    }

    @Test
    void testGetMileageSummary() throws Exception {
        // 로그인된 사용자 Mocking
        Member mockMember = new Member();
        mockMember.setId(1L);  // 테스트용 사용자 ID 설정
        when(memberService.getCurrentLoggedInMember()).thenReturn(mockMember);

        // 마일리지 요약 정보 Mocking
        MileageSummaryDTO mockSummary = new MileageSummaryDTO();
        mockSummary.setTotalMileage(1000);  // 예상되는 마일리지 값 설정
        when(mileageService.getMileageSummary(1L)).thenReturn(mockSummary);

        // API 호출 및 검증
        mockMvc.perform(get("/api/mileage/summary"))
                .andExpect(status().isOk())  // 200 OK 응답
                .andExpect(jsonPath("$.totalMileage").value(1000));  // totalMileage 값 검증

        verify(mileageService, times(1)).getMileageSummary(1L);  // 서비스 호출 검증
    }

    @Test
    void testGetMileageHistory() throws Exception {
        // 로그인된 사용자 Mocking
        Member mockMember = new Member();
        mockMember.setId(1L);  // 테스트용 사용자 ID 설정
        when(memberService.getCurrentLoggedInMember()).thenReturn(mockMember);

        // 마일리지 내역 Mocking
        List<MileageHistoryDTO> mockHistory = List.of(
                new MileageHistoryDTO(500, "Earned", "Product Purchase", LocalDateTime.now())
        );
        when(mileageService.getMileageHistory(1L)).thenReturn(mockHistory);

        // API 호출 및 검증
        mockMvc.perform(get("/api/mileage/history"))
                .andExpect(status().isOk())  // 200 OK 응답
                .andExpect(jsonPath("$[0].amount").value(500))  // 첫 번째 내역의 amount 값 검증
                .andExpect(jsonPath("$[0].type").value("Earned"))  // 첫 번째 내역의 type 값 검증
                .andExpect(jsonPath("$[0].timestamp").exists());  // timestamp 값 검증

        verify(mileageService, times(1)).getMileageHistory(1L);  // 서비스 호출 검증
    }

    @Test
    void testEarnMileageAfterPurchase() throws Exception {
        // 로그인된 사용자 Mocking
        Member mockMember = new Member();
        mockMember.setId(1L);  // 테스트용 사용자 ID 설정
        when(memberService.getCurrentLoggedInMember()).thenReturn(mockMember);

        // 마일리지 적립 처리 Mocking
        MileageEarnRequestDTO requestDTO = new MileageEarnRequestDTO(10000);  // 10000원 구매
        doNothing().when(mileageService).earnMileage(eq(1L), eq(500), eq("Purchase Reward"));  // 500마일리지 적립

        // API 호출 및 검증
        mockMvc.perform(post("/api/mileage/earn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"purchaseAmount\":10000}"))
                .andExpect(status().isOk())  // 200 OK 응답
                .andExpect(content().string("Mileage earned successfully."));  // 응답 메시지 검증

        verify(mileageService, times(1)).earnMileage(eq(1L), eq(500), eq("Purchase Reward"));  // 서비스 호출 검증
    }

    @Test
    void testConfirmUseMileage_Success() throws Exception {
        // 로그인된 사용자 Mocking
        Member mockMember = new Member();
        mockMember.setId(1L);  // 테스트용 사용자 ID 설정
        when(memberService.getCurrentLoggedInMember()).thenReturn(mockMember);

        // 마일리지 사용 처리 Mocking
        MileageUseRequestDTO requestDTO = new MileageUseRequestDTO(300, "Purchase of product");
        doNothing().when(mileageService).useMileage(eq(1L), eq(300), eq("Purchase of product"));

        // API 호출 및 검증
        mockMvc.perform(post("/api/mileage/use/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":300, \"description\":\"Purchase of product\"}"))
                .andExpect(status().isOk())  // 200 OK 응답
                .andExpect(content().string("Mileage used successfully."));  // 응답 메시지 검증

        verify(mileageService, times(1)).useMileage(eq(1L), eq(300), eq("Purchase of product"));  // 서비스 호출 검증
    }

    @Test
    void testConfirmUseMileage_Failure_InsufficientMileage() throws Exception {
        // 로그인된 사용자 Mocking
        Member mockMember = new Member();
        mockMember.setId(1L);  // 테스트용 사용자 ID 설정
        when(memberService.getCurrentLoggedInMember()).thenReturn(mockMember);

        // 마일리지 부족 예외 Mocking
        MileageUseRequestDTO requestDTO = new MileageUseRequestDTO(2000, "Purchase of product");
        doThrow(new RuntimeException("Not enough mileage to complete the transaction."))
                .when(mileageService).useMileage(eq(1L), eq(2000), eq("Purchase of product"));

        // API 호출 및 검증
        mockMvc.perform(post("/api/mileage/use/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":2000, \"description\":\"Purchase of product\"}"))
                .andExpect(status().isBadRequest())  // 400 Bad Request 응답
                .andExpect(content().string("Error using mileage: Not enough mileage to complete the transaction."));  // 예외 메시지 검증

        verify(mileageService, times(1)).useMileage(eq(1L), eq(2000), eq("Purchase of product"));  // 서비스 호출 검증
    }
}
