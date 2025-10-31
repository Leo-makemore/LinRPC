package com.yupi.yurpc.example;

import lombok.extern.slf4j.Slf4j;

/**
 * 用户服务实现（示例）
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Slf4j
public class UserServiceImpl implements UserService {

    @Override
    public String getUserName(Long userId) {
        log.info("调用 getUserName 方法，userId: {}", userId);
        return "User_" + userId;
    }

    @Override
    public Integer getUserCount() {
        log.info("调用 getUserCount 方法");
        return 100;
    }
}

