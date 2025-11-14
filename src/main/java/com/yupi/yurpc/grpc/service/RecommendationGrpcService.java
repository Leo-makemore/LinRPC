package com.yupi.yurpc.grpc.service;

import com.yupi.yurpc.example.RecommendationItemInfo;
import com.yupi.yurpc.example.RecommendationService;
import com.yupi.yurpc.grpc.proto.RecommendationItem;
import com.yupi.yurpc.grpc.proto.RecommendationRequest;
import com.yupi.yurpc.grpc.proto.RecommendationResponse;
import com.yupi.yurpc.grpc.proto.RecommendationServiceGrpc;
import com.yupi.yurpc.grpc.proto.ResponseMetadata;
import com.yupi.yurpc.grpc.support.GrpcMetadataBuilder;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Profile;

/**
 * 推荐服务 gRPC 实现
 */
@Slf4j
@GrpcService
@Profile("provider")
@RequiredArgsConstructor
public class RecommendationGrpcService extends RecommendationServiceGrpc.RecommendationServiceImplBase {

    private final RecommendationService recommendationService;

    @Override
    public void getRecommendations(RecommendationRequest request,
                                   StreamObserver<RecommendationResponse> responseObserver) {
        ResponseMetadata metadata = GrpcMetadataBuilder.build();
        RecommendationResponse.Builder builder = RecommendationResponse.newBuilder()
                .setMetadata(metadata);
        for (RecommendationItemInfo item : recommendationService.getRecommendations(request.getUserId(), request.getLimit())) {
            builder.addItems(RecommendationItem.newBuilder()
                    .setTitle(item.getTitle())
                    .setCategory(item.getCategory())
                    .setScore(item.getScore())
                    .build());
        }
        log.info("返回推荐列表 userId={}, count={}", request.getUserId(), builder.getItemsCount());
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}


