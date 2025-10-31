package com.yupi.yurpc.example;

/**
 * 用户服务接口（示例）
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface UserService {

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户名
     */
    String getUserName(Long userId);

    /**
     * 获取用户数量
     *
     * @return 用户数量
     */
    Integer getUserCount();
}

