package com.yupi.yurpc.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 推荐服务内存实现
 */
@Slf4j
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private static final List<CatalogSeed> CATALOG = List.of(
            new CatalogSeed("Kubernetes Starter Lab", "cloud-native", 0.78),
            new CatalogSeed("gRPC Best Practices", "distributed-system", 0.74),
            new CatalogSeed("Spring Boot Observability Pack", "observability", 0.81),
            new CatalogSeed("High Throughput RPC Patterns", "architecture", 0.76),
            new CatalogSeed("Serverless Cost Optimizer", "finops", 0.69)
    );

    private final Map<Long, List<RecommendationItemInfo>> cache = new ConcurrentHashMap<>();

    private final UserService userService;

    @Override
    public List<RecommendationItemInfo> getRecommendations(Long userId, int limit) {
        List<RecommendationItemInfo> items = cache.computeIfAbsent(userId != null ? userId : -1L,
                key -> generateRecommendations(userId));
        if (limit > 0 && limit < items.size()) {
            return new ArrayList<>(items.subList(0, limit));
        }
        return new ArrayList<>(items);
    }

    private List<RecommendationItemInfo> generateRecommendations(Long userId) {
        double userFactor = userService.findUser(userId)
                .map(UserInfo::getAge)
                .map(age -> Math.abs(Math.sin(age / 10.0)))
                .orElse(0.42);
        double idFactor = userId != null ? Math.abs(Math.cos(userId)) : 0.51;

        List<RecommendationItemInfo> generated = new ArrayList<>();
        for (int i = 0; i < CATALOG.size(); i++) {
            CatalogSeed seed = CATALOG.get(i);
            double adjustment = (userFactor + idFactor) * (i + 1) * 0.05;
            double score = Math.min(0.99, seed.baseScore() + adjustment);
            generated.add(RecommendationItemInfo.builder()
                    .title(seed.title())
                    .category(seed.category())
                    .score(round(score))
                    .build());
        }
        generated.sort(Comparator.comparing(RecommendationItemInfo::getScore).reversed());
        log.info("计算推荐列表 userId={}, items={}", userId, generated);
        return generated;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value)
                .setScale(4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private record CatalogSeed(String title, String category, double baseScore) {
    }
}


