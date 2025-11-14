package com.yupi.yurpc.grpc.service;

import com.yupi.yurpc.example.UserInfo;
import com.yupi.yurpc.example.UserService;
import com.yupi.yurpc.grpc.proto.CreateUserRequest;
import com.yupi.yurpc.grpc.proto.CreateUserResponse;
import com.yupi.yurpc.grpc.proto.DeleteUserRequest;
import com.yupi.yurpc.grpc.proto.Empty;
import com.yupi.yurpc.grpc.proto.ListUsersRequest;
import com.yupi.yurpc.grpc.proto.ListUsersResponse;
import com.yupi.yurpc.grpc.proto.OperationStatus;
import com.yupi.yurpc.grpc.proto.ResponseMetadata;
import com.yupi.yurpc.grpc.proto.ToggleUserActiveRequest;
import com.yupi.yurpc.grpc.proto.User;
import com.yupi.yurpc.grpc.proto.UserCountResponse;
import com.yupi.yurpc.grpc.proto.UserNameResponse;
import com.yupi.yurpc.grpc.proto.UserRequest;
import com.yupi.yurpc.grpc.proto.UserServiceGrpc;
import com.yupi.yurpc.grpc.support.GrpcMetadataBuilder;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Profile;

/**
 * gRPC 服务实现。
 */
@Slf4j
@GrpcService
@Profile("provider")
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    @Override
    public void getUserName(UserRequest request, StreamObserver<UserNameResponse> responseObserver) {
        String userName = userService.getUserName(request.getUserId());
        log.info("Returning username for userId={}", request.getUserId());
        UserNameResponse response = UserNameResponse.newBuilder()
                .setUserName(userName)
                .setMetadata(buildMetadata())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getUserCount(Empty request, StreamObserver<UserCountResponse> responseObserver) {
        Integer count = userService.getUserCount();
        log.info("Returning user count={}", count);
        UserCountResponse response = UserCountResponse.newBuilder()
                .setCount(count)
                .setMetadata(buildMetadata())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        Long userId = userService.createUser(request.getName(), request.getAge());
        log.info("Created user via gRPC userId={}", userId);
        CreateUserResponse response = CreateUserResponse.newBuilder()
                .setUserId(userId)
                .setMetadata(buildMetadata())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<OperationStatus> responseObserver) {
        boolean success = userService.deleteUser(request.getUserId());
        OperationStatus response = OperationStatus.newBuilder()
                .setSuccess(success)
                .setMessage(success ? "用户删除成功" : "用户不存在")
                .setMetadata(buildMetadata())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        ListUsersResponse.Builder builder = ListUsersResponse.newBuilder()
                .setMetadata(buildMetadata());
        for (UserInfo info : userService.listUsers(request.getLimit())) {
            User user = User.newBuilder()
                    .setUserId(info.getUserId())
                    .setName(info.getName())
                    .setAge(info.getAge())
                    .setActive(info.isActive())
                    .build();
            builder.addUsers(user);
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void toggleUserActive(ToggleUserActiveRequest request, StreamObserver<OperationStatus> responseObserver) {
        boolean success = userService.toggleUserActive(request.getUserId(), request.getActive());
        OperationStatus response = OperationStatus.newBuilder()
                .setSuccess(success)
                .setMessage(success ? "状态更新成功" : "用户不存在")
                .setMetadata(buildMetadata())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private ResponseMetadata buildMetadata() {
        return GrpcMetadataBuilder.build();
    }
}


