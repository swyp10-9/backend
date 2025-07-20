package com.swyp10.domain.review.entity;

import com.swyp10.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_likes",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_review_like_user_review",
        columnNames = {"user_id", "review_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_like_id")
    private Long reviewLikeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private UserReview userReview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void markDeleted(){
        this.deletedAt = LocalDateTime.now();
    }

    public void setUserReview(UserReview userReview) {
        this.userReview = userReview;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
