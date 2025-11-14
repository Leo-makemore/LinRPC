package com.yupi.yurpc.grpc.client;

import com.yupi.yurpc.grpc.proto.CalcServiceGrpc;
import com.yupi.yurpc.grpc.proto.ComputeRequest;
import com.yupi.yurpc.grpc.proto.ComputeResponse;
import com.yupi.yurpc.grpc.proto.CreateUserRequest;
import com.yupi.yurpc.grpc.proto.CreateUserResponse;
import com.yupi.yurpc.grpc.proto.DeleteUserRequest;
import com.yupi.yurpc.grpc.proto.Empty;
import com.yupi.yurpc.grpc.proto.ListUsersRequest;
import com.yupi.yurpc.grpc.proto.ListUsersResponse;
import com.yupi.yurpc.grpc.proto.OperationStatus;
import com.yupi.yurpc.grpc.proto.RecommendationRequest;
import com.yupi.yurpc.grpc.proto.RecommendationResponse;
import com.yupi.yurpc.grpc.proto.RecommendationServiceGrpc;
import com.yupi.yurpc.grpc.proto.ResponseMetadata;
import com.yupi.yurpc.grpc.proto.ToggleUserActiveRequest;
import com.yupi.yurpc.grpc.proto.UserNameResponse;
import com.yupi.yurpc.grpc.proto.UserRequest;
import com.yupi.yurpc.grpc.proto.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("client")
@RequiredArgsConstructor
public class UserGrpcClientRunner implements CommandLineRunner {

    @GrpcClient("userService")
    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;

    @GrpcClient("recommendationService")
    private RecommendationServiceGrpc.RecommendationServiceBlockingStub recommendationStub;

    @GrpcClient("calcService")
    private CalcServiceGrpc.CalcServiceBlockingStub calcStub;

    private final ApplicationContext applicationContext;

    @Override
    public void run(String... args) {
        UserNameResponse userNameResponse = userServiceStub.getUserName(UserRequest.newBuilder()
                .setUserId(134L)
                .build());
        log.info("Received username response: {}", userNameResponse.getUserName());
        logMetadata("UserService.GetUserName", userNameResponse.getMetadata());

        var countResponse = userServiceStub.getUserCount(Empty.getDefaultInstance());
        log.info("Received user count response: {}", countResponse.getCount());
        logMetadata("UserService.GetUserCount", countResponse.getMetadata());

        CreateUserResponse createResp = userServiceStub.createUser(CreateUserRequest.newBuilder()
                .setName("Charlie")
                .setAge(28)
                .build());
        long createdUserId = createResp.getUserId();
        log.info("Created user id={}", createdUserId);
        logMetadata("UserService.CreateUser", createResp.getMetadata());

        OperationStatus toggleResp = userServiceStub.toggleUserActive(ToggleUserActiveRequest.newBuilder()
                .setUserId(createdUserId)
                .setActive(false)
                .build());
        log.info("Toggle active result: success={}, message={}", toggleResp.getSuccess(), toggleResp.getMessage());
        logMetadata("UserService.ToggleUserActive", toggleResp.getMetadata());

        ListUsersResponse listResponse = userServiceStub.listUsers(ListUsersRequest.newBuilder().setLimit(5).build());
        logMetadata("UserService.ListUsers", listResponse.getMetadata());
        listResponse.getUsersList().forEach(user ->
                log.info("User snapshot -> id={}, name={}, age={}, active={}",
                        user.getUserId(), user.getName(), user.getAge(), user.getActive()));

        RecommendationResponse recommendationResponse = recommendationStub.getRecommendations(
                RecommendationRequest.newBuilder()
                        .setUserId(createdUserId)
                        .setLimit(3)
                        .build());
        logMetadata("RecommendationService.GetRecommendations", recommendationResponse.getMetadata());
        recommendationResponse.getItemsList().forEach(item ->
                log.info("Recommendation -> title={}, category={}, score={}",
                        item.getTitle(), item.getCategory(), item.getScore()));

        ComputeResponse computeResponse = calcStub.compute(ComputeRequest.newBuilder()
                .setLeft(42.0)
                .setRight(3.0)
                .setOperator("div")
                .build());
        log.info("CalcService.Compute result: {}", computeResponse.getResult());
        logMetadata("CalcService.Compute", computeResponse.getMetadata());

        OperationStatus deleteStatus = userServiceStub.deleteUser(DeleteUserRequest.newBuilder()
                .setUserId(createdUserId)
                .build());
        log.info("Delete user result: success={}, message={}", deleteStatus.getSuccess(), deleteStatus.getMessage());
        logMetadata("UserService.DeleteUser", deleteStatus.getMetadata());

        // 客户端模式执行完毕即可退出
        int exitCode = org.springframework.boot.SpringApplication.exit(applicationContext, () -> 0);
        System.exit(exitCode);
    }

    private void logMetadata(String action, ResponseMetadata metadata) {
        if (metadata == null || !metadata.hasStrategy()) {
            log.warn("{} metadata is unavailable", action);
            return;
        }
        log.info("{} -> strategy(serializer={}, loadBalancer={}, retry={}, tolerant={}), timestamp={}",
                action,
                metadata.getStrategy().getSerializer(),
                metadata.getStrategy().getLoadBalancer(),
                metadata.getStrategy().getRetryStrategy(),
                metadata.getStrategy().getTolerantStrategy(),
                metadata.getTimestamp());
    }
}

