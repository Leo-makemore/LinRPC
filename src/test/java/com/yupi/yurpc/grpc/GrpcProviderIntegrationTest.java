package com.yupi.yurpc.grpc;

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
import com.yupi.yurpc.grpc.proto.ToggleUserActiveRequest;
import com.yupi.yurpc.grpc.proto.UserNameResponse;
import com.yupi.yurpc.grpc.proto.UserRequest;
import com.yupi.yurpc.grpc.proto.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = GrpcProviderApplication.class, properties = {
        "grpc.server.port=19091",
        "spring.main.web-application-type=none",
        "rpc.telemetry.enabled=false",
        "rpc.registry-config.registry=local"
})
@ActiveProfiles("provider")
class GrpcProviderIntegrationTest {

    @Value("${grpc.server.port}")
    private int port;

    private ManagedChannel channel;

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
    }

    @Test
    void userServiceResponds() {
        channel = ManagedChannelBuilder.forAddress("localhost", port)
                .usePlaintext()
                .build();
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
        RecommendationServiceGrpc.RecommendationServiceBlockingStub recommendationStub =
                RecommendationServiceGrpc.newBlockingStub(channel);
        CalcServiceGrpc.CalcServiceBlockingStub calcStub = CalcServiceGrpc.newBlockingStub(channel);
        UserNameResponse response = stub.getUserName(UserRequest.newBuilder()
                .setUserId(123L)
                .build());
        Assertions.assertTrue(response.getUserName().contains("User_"));
        Assertions.assertTrue(response.hasMetadata());
        Assertions.assertTrue(response.getMetadata().hasStrategy());
        int originalCount = stub.getUserCount(Empty.getDefaultInstance()).getCount();
        CreateUserResponse created = stub.createUser(CreateUserRequest.newBuilder()
                .setName("IntegrationTester")
                .setAge(99)
                .build());
        Assertions.assertNotNull(created);
        long newUserId = created.getUserId();
        Assertions.assertEquals(originalCount + 1, stub.getUserCount(Empty.getDefaultInstance()).getCount());

        ListUsersResponse listUsersResponse = stub.listUsers(ListUsersRequest.newBuilder().setLimit(10).build());
        Assertions.assertTrue(listUsersResponse.getUsersList().stream()
                .anyMatch(user -> user.getUserId() == newUserId && "IntegrationTester".equals(user.getName())));
        Assertions.assertTrue(listUsersResponse.hasMetadata());

        OperationStatus toggleStatus = stub.toggleUserActive(ToggleUserActiveRequest.newBuilder()
                .setUserId(newUserId)
                .setActive(false)
                .build());
        Assertions.assertTrue(toggleStatus.getSuccess());
        Assertions.assertTrue(toggleStatus.hasMetadata());

        OperationStatus deleteStatus = stub.deleteUser(DeleteUserRequest.newBuilder()
                .setUserId(newUserId)
                .build());
        Assertions.assertTrue(deleteStatus.getSuccess());
        Assertions.assertEquals(originalCount, stub.getUserCount(Empty.getDefaultInstance()).getCount());

        RecommendationResponse recommendations = recommendationStub.getRecommendations(
                RecommendationRequest.newBuilder()
                        .setUserId(newUserId)
                        .setLimit(2)
                        .build());
        Assertions.assertTrue(recommendations.getItemsCount() > 0);
        Assertions.assertTrue(recommendations.hasMetadata());
        Assertions.assertFalse(recommendations.getMetadata().getStrategy().getSerializer().isBlank());

        ComputeResponse computeResponse = calcStub.compute(ComputeRequest.newBuilder()
                .setLeft(12.0)
                .setRight(3.0)
                .setOperator("mul")
                .build());
        Assertions.assertEquals(36.0, computeResponse.getResult());
        Assertions.assertTrue(computeResponse.hasMetadata());
        Assertions.assertTrue(computeResponse.getMetadata().getStrategy().getLoadBalancer().length() > 0);
    }
}


