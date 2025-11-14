package com.yupi.yurpc.gateway.controller;

import com.yupi.yurpc.grpc.proto.CreateUserRequest;
import com.yupi.yurpc.grpc.proto.DeleteUserRequest;
import com.yupi.yurpc.grpc.proto.Empty;
import com.yupi.yurpc.grpc.proto.ListUsersRequest;
import com.yupi.yurpc.grpc.proto.ListUsersResponse;
import com.yupi.yurpc.grpc.proto.OperationStatus;
import com.yupi.yurpc.grpc.proto.ToggleUserActiveRequest;
import com.yupi.yurpc.grpc.proto.UserNameResponse;
import com.yupi.yurpc.grpc.proto.UserRequest;
import com.yupi.yurpc.grpc.proto.UserServiceGrpc.UserServiceBlockingStub;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserGatewayController {

    private final UserServiceBlockingStub userStub;

    public UserGatewayController(UserServiceBlockingStub userStub) {
        this.userStub = userStub;
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable long userId) {
        UserNameResponse response = userStub.getUserName(UserRequest.newBuilder()
                .setUserId(userId)
                .build());
        return new UserDto(userId, response.getUserName(), response.getMetadata().getStrategy().getSerializer());
    }

    @GetMapping
    public List<UserSummaryDto> listUsers(ListUsersRequestDto requestDto) {
        int limit = requestDto.limit != null ? requestDto.limit : 0;
        ListUsersResponse response = userStub.listUsers(ListUsersRequest.newBuilder().setLimit(limit).build());
        return response.getUsersList().stream()
                .map(user -> new UserSummaryDto(
                        user.getUserId(),
                        user.getName(),
                        user.getAge(),
                        user.getActive()))
                .collect(Collectors.toList());
    }

    @PostMapping
    public CreatedUserDto createUser(@Valid @RequestBody CreateUserDto request) {
        var resp = userStub.createUser(CreateUserRequest.newBuilder()
                .setName(request.name)
                .setAge(request.age)
                .build());
        return new CreatedUserDto(resp.getUserId(), resp.getMetadata().getStrategy().getSerializer());
    }

    @PostMapping("/{userId}/toggle")
    public OperationResult toggle(@PathVariable long userId, @Valid @RequestBody ToggleRequest request) {
        OperationStatus status = userStub.toggleUserActive(ToggleUserActiveRequest.newBuilder()
                .setUserId(userId)
                .setActive(request.active)
                .build());
        return new OperationResult(status.getSuccess(), status.getMessage());
    }

    @PostMapping("/{userId}/delete")
    public OperationResult deleteUser(@PathVariable long userId) {
        OperationStatus status = userStub.deleteUser(DeleteUserRequest.newBuilder()
                .setUserId(userId)
                .build());
        return new OperationResult(status.getSuccess(), status.getMessage());
    }

    @GetMapping("/count")
    public CountDto count() {
        int count = userStub.getUserCount(Empty.getDefaultInstance()).getCount();
        return new CountDto(count);
    }

    public record UserDto(long userId, String userName, String serializer) {
    }

    public record UserSummaryDto(long userId, String name, int age, boolean active) {
    }

    public record CreatedUserDto(long userId, String serializer) {
    }

    public record OperationResult(boolean success, String message) {
    }

    public record CountDto(int count) {
    }

    @Data
    public static class CreateUserDto {
        @NotBlank
        private String name;

        @NotNull
        private Integer age;
    }

    @Data
    public static class ToggleRequest {
        @NotNull
        private Boolean active;
    }

    @Data
    public static class ListUsersRequestDto {
        private Integer limit;
    }
}


