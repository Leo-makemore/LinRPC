package com.yupi.yurpc.example;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推荐条目
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationItemInfo {

    private String title;

    private String category;

    private double score;
}


