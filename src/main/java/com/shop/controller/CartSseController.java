package com.shop.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;


@RestController
public class CartSseController {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    // 클라이언트가 SSE를 구독하는 엔드포인트
    @GetMapping("/sse/cart")
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L); // 무제한 시간 설정
        emitters.add(emitter);

        // 연결 종료 시 리스트에서 제거
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    // 클라이언트에게 장바구니 갯수 업데이트 전송
    public void sendCartCountUpdate(int count) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("cart-count-update") // 이벤트 이름
                        .data(count), MediaType.TEXT_PLAIN);
            } catch (IOException e) {
                emitters.remove(emitter); // 실패한 연결 제거
            }
        }
    }
}


