package com.yupi.yurpc.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private Long userId;

    private String name;

    private Integer age;

    private boolean active;
}


