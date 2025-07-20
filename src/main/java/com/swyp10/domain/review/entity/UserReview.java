package com.swyp10.domain.review.entity;

import com.swyp10.domain.festival.entity.Festival;
import com.swyp10.entity.User;
import com.swyp10.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_reviews")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserReview extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id", nullable = false)
    private Festival festival;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "userReview", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewLike> reviewLikes = new ArrayList<>();


    // 연관 관계 메서드
    public void setFestival(Festival festival) {
        this.festival = festival;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void addReviewLike(ReviewLike like) {
        this.reviewLikes.add(like);
        like.setUserReview(this);
    }

    // 수정 메서드
    public void updateContent(String content, int rating) {
        this.content = content;
        this.rating = rating;
    }

}
