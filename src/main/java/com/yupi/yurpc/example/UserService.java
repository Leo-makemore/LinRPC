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

    /**
     * 创建用户
     *
     * @param name 用户名
     * @param age  年龄
     * @return 新用户 ID
     */
    Long createUser(String name, Integer age);

    /**
     * 删除用户
     *
     * @param userId 用户 ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);

    /**
     * 切换用户启用状态
     *
     * @param userId 用户 ID
     * @param active 启用状态
     * @return 是否成功
     */
    boolean toggleUserActive(Long userId, boolean active);

    /**
     * 获取用户列表
     *
     * @param limit 限制返回数量，<=0 返回全部
     * @return 用户列表
     */
    java.util.List<UserInfo> listUsers(int limit);

    /**
     * 查找用户
     *
     * @param userId 用户 ID
     * @return 用户信息（如果存在）
     */
    java.util.Optional<UserInfo> findUser(Long userId);
}

