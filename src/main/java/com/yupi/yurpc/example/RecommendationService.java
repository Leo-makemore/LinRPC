package com.yupi.yurpc.example;

import java.util.List;

/**
 * 推荐服务
 */
public interface RecommendationService {

    /**
     * 获取推荐列表
     *
     * @param userId 用户 ID
     * @param limit  限制数量，<=0 返回全部
     * @return 推荐条目
     */
    List<RecommendationItemInfo> getRecommendations(Long userId, int limit);
}


