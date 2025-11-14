package com.yupi.yurpc.grpc.config;

import com.yupi.yurpc.example.CalcService;
import com.yupi.yurpc.example.CalcServiceImpl;
import com.yupi.yurpc.example.RecommendationService;
import com.yupi.yurpc.example.RecommendationServiceImpl;
import com.yupi.yurpc.example.UserService;
import com.yupi.yurpc.example.UserServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("provider")
public class ProviderConfiguration {

    @Bean
    public UserService userService() {
        return new UserServiceImpl();
    }

    @Bean
    public RecommendationService recommendationService(UserService userService) {
        return new RecommendationServiceImpl(userService);
    }

    @Bean
    public CalcService calcService() {
        return new CalcServiceImpl();
    }
}


