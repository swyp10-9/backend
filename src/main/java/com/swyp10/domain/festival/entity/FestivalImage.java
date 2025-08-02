package com.swyp10.domain.festival.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "festival_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FestivalImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imgid;

    @Column(columnDefinition = "TEXT")
    private String originimgurl;

    @Column(columnDefinition = "TEXT")
    private String smallimageurl;

    private String serialnum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "festival_id")
    private Festival festival;

    public void setFestival(Festival festival) {
        this.festival = festival;
    }
}
