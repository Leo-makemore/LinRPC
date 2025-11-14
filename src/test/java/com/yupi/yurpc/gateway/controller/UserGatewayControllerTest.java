package com.yupi.yurpc.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yupi.yurpc.grpc.proto.CreateUserResponse;
import com.yupi.yurpc.grpc.proto.ListUsersResponse;
import com.yupi.yurpc.grpc.proto.ResponseMetadata;
import com.yupi.yurpc.grpc.proto.StrategySnapshot;
import com.yupi.yurpc.grpc.proto.User;
import com.yupi.yurpc.grpc.proto.UserNameResponse;
import com.yupi.yurpc.grpc.proto.UserServiceGrpc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserGatewayControllerTest {

    private final UserServiceGrpc.UserServiceBlockingStub userStub = Mockito.mock(UserServiceGrpc.UserServiceBlockingStub.class);
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UserGatewayController(userStub)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void getUserReturnsMetadata() throws Exception {
        UserNameResponse response = UserNameResponse.newBuilder()
                .setUserName("User_1")
                .setMetadata(metadata("json"))
                .build();
        Mockito.when(userStub.getUserName(any())).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("User_1"))
                .andExpect(jsonPath("$.serializer").value("json"));
    }

    @Test
    void listUsersReturnsContent() throws Exception {
        ListUsersResponse response = ListUsersResponse.newBuilder()
                .addAllUsers(List.of(
                        User.newBuilder().setUserId(1).setName("alice").setAge(20).setActive(true).build(),
                        User.newBuilder().setUserId(2).setName("bob").setAge(24).setActive(false).build()))
                .build();
        Mockito.when(userStub.listUsers(any())).thenReturn(response);

        mockMvc.perform(get("/api/users").param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("alice"))
                .andExpect(jsonPath("$[1].active").value(false));
    }

    @Test
    void createUserReturnsId() throws Exception {
        CreateUserResponse response = CreateUserResponse.newBuilder()
                .setUserId(123)
                .setMetadata(metadata("kryo"))
                .build();
        Mockito.when(userStub.createUser(any())).thenReturn(response);

        UserGatewayController.CreateUserDto payload = new UserGatewayController.CreateUserDto();
        payload.setName("Jack");
        payload.setAge(30);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(123))
                .andExpect(jsonPath("$.serializer").value("kryo"));
    }

    private ResponseMetadata metadata(String serializer) {
        return ResponseMetadata.newBuilder()
                .setStrategy(StrategySnapshot.newBuilder()
                        .setSerializer(serializer)
                        .setLoadBalancer("roundRobin")
                        .setRetryStrategy("fixed")
                        .setTolerantStrategy("failFast")
                        .build())
                .setTimestamp("2025-01-01T00:00:00Z")
                .build();
    }
}


