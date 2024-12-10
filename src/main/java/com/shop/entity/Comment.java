package com.shop.entity;

import com.shop.constant.CommentStar;
import com.shop.dto.CommentDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="comment")
@Getter
@Setter
@ToString
public class Comment extends BaseEntity{

    @Id
    @Column(name="comment_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long rid; //댓글 번호

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false, length = 400)
    private String content; //상품평 내용

    @Enumerated(EnumType.STRING)
    private CommentStar commentStar;//별점(enum 타입)


    public Comment() {

    }

    public Comment(String content, Item item, CommentStar commentStar){
        this.content = content;
        this.item = item;
        this.commentStar = commentStar;
    }

    public void updateComment(CommentDto commentDto){
        this.commentStar = commentDto.getCommentStar();
        this.content = commentDto.getContent();
    }
}
