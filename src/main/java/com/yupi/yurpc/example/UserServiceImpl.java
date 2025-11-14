package com.yupi.yurpc.example;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户服务实现（示例）
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @learn <a href="https://codefather.cn">编程宝典</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Slf4j
public class UserServiceImpl implements UserService {

    private final Map<Long, UserInfo> userStore = new ConcurrentHashMap<>();

    private final AtomicLong idGenerator = new AtomicLong(1000L);

    private static final Comparator<UserInfo> USER_ORDER = Comparator.comparing(UserInfo::getUserId);

    public UserServiceImpl() {
        // 初始化一些示例数据
        createUser("Alice", 26);
        createUser("Bob", 31);
    }

    @Override
    public String getUserName(Long userId) {
        log.info("调用 getUserName 方法，userId: {}", userId);
        return findUser(userId)
                .map(UserInfo::getName)
                .orElse("User_" + userId);
    }

    @Override
    public Integer getUserCount() {
        log.info("调用 getUserCount 方法");
        return userStore.size();
    }

    @Override
    public Long createUser(String name, Integer age) {
        long userId = idGenerator.incrementAndGet();
        UserInfo userInfo = new UserInfo(userId, name, age, true);
        userStore.put(userId, userInfo);
        log.info("创建用户 userId={}, name={}, age={}", userId, name, age);
        return userId;
    }

    @Override
    public boolean deleteUser(Long userId) {
        UserInfo removed = userStore.remove(userId);
        log.info("删除用户 userId={}, success={}", userId, removed != null);
        return removed != null;
    }

    @Override
    public boolean toggleUserActive(Long userId, boolean active) {
        Optional<UserInfo> infoOptional = findUser(userId);
        if (infoOptional.isEmpty()) {
            log.warn("切换用户状态失败，用户不存在 userId={}", userId);
            return false;
        }
        UserInfo info = infoOptional.get();
        info.setActive(active);
        log.info("切换用户状态 userId={}, active={}", userId, active);
        return true;
    }

    @Override
    public List<UserInfo> listUsers(int limit) {
        List<UserInfo> result = new ArrayList<>(userStore.values());
        result.sort(USER_ORDER);
        if (limit > 0 && limit < result.size()) {
            return new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }

    @Override
    public Optional<UserInfo> findUser(Long userId) {
        return Optional.ofNullable(userStore.get(userId));
    }
}

