package com.yupi.yurpc.example;

/**
 * 简单算术服务
 */
public interface CalcService {

    /**
     * 计算表达式
     *
     * @param left     左操作数
     * @param right    右操作数
     * @param operator 运算符 add/sub/mul/div/mod/pow
     * @return 结果
     */
    double compute(double left, double right, String operator);
}


