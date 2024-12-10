package com.shop.dto;

import com.shop.constant.CommentStar;
import com.shop.entity.Comment;
import lombok.Getter;
import lombok.Setter;
import org.modelmapper.ModelMapper;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDto {

    private Long rid;//댓글번호

    private Long id;//상품번호

    private CommentStar commentStar;//별점 Enum

    @NotNull(message = "상품평을 입력해주세요.")
    private String content;//상품평 내용

    private String createdBy;//상품평 작성자

    private LocalDateTime regTime;//상품평 작성일

    private LocalDateTime updateTime;//상품평 수정일

    private static ModelMapper modelMapper = new ModelMapper();

    public Comment createComment(){
        return modelMapper.map(this, Comment.class);
    }

    public static CommentDto of(Comment comment){
//        return modelMapper.map(comment, CommentDto.class);
        CommentDto dto = modelMapper.map(comment, CommentDto.class);
        dto.setId(comment.getItem().getId()); // 모달에서 id값을 못받아와서 id 값을 지정 (Comment Entity에서 Item과의 관계에서 id값을 받아오기때문에 따로 지정해줘야 id값을 받아올 수 있음)
        return dto;
    }
}
