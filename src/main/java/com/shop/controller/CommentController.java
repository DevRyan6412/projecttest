package com.shop.controller;

import com.shop.dto.CommentDto;
import com.shop.entity.Comment;
import com.shop.entity.Item;
import com.shop.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequestMapping("/item/{itemId}")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 상품평 작성 처리
    @PostMapping("/comments")//
    public String createComment(@PathVariable Long itemId, @Valid @ModelAttribute CommentDto commentDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors() || commentDto.getContent() == null || commentDto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("상품평을 입력해주세요.");
        }

        System.out.println("Selected Star: " + commentDto.getCommentStar());

        Item item = new Item();
        item.setId(itemId);

        commentService.createComment(commentDto.getContent().trim(), item, commentDto.getCreatedBy(), commentDto.getCommentStar());
        return "redirect:/item/" + itemId;   // 상품평 작성 후 해당 아이템의 댓글 목록으로 리디렉션
    }

    // 상품평 수정 처리
    @PostMapping("/comments/{rid}/edit")
    public String updateComment(@PathVariable Long itemId,
                                @PathVariable Long rid,
                                @ModelAttribute CommentDto commentDto,
                                Principal principal,
                                BindingResult bindingResult) {
        Comment comment = commentService.getComment(rid);

        if(!comment.getCreatedBy().equals(principal.getName())) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }
        if (bindingResult.hasErrors() || commentDto.getContent() == null || commentDto.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("상품평을 입력해주세요.");
        }
        comment.setContent(commentDto.getContent());
        comment.setCommentStar(commentDto.getCommentStar());
        commentService.updateComment(comment);// 댓글 수정 내용 받아오기

        return "redirect:/item/" + itemId ;  // 수정 후 해당 아이템의 상세보기페이지로 리디렉션
    }


    // 상품평 삭제
    @PostMapping("/comments/{rid}/delete")
    public String deleteComment(@PathVariable Long rid, @RequestParam Long itemId, Principal principal) {
        System.out.println("rid값은 받았니???: " + rid);
        System.out.println("Item ID: " + itemId);
        System.out.println("Principal Name: " + principal.getName());
        Comment comment = commentService.getComment(rid);
        if(!comment.getCreatedBy().equals(principal.getName())){
            throw new IllegalStateException("삭제 권한이 없습니다.");
        } else {
            commentService.deleteComment(comment.getRid());  // 상품평 삭제
        }
        return "redirect:/item/" + itemId;  // 삭제 후 해당 아이템으로 이동
    }

    //모달창을 띄우기위한 상품평 조회
    @GetMapping("/comments/{rid}")
    @ResponseBody
    public CommentDto getComment(@PathVariable Long rid) {
        Comment comment = commentService.getComment(rid);
        return CommentDto.of(comment);  // 상품평들을 DTO로 변환하여 반환
    }

}
