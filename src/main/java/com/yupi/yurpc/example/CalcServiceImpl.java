package com.yupi.yurpc.example;

import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

/**
 * Calc 服务内存实现
 */
@Slf4j
public class CalcServiceImpl implements CalcService {

    @Override
    public double compute(double left, double right, String operator) {
        String normalized = operator == null ? "add" : operator.trim().toLowerCase(Locale.ROOT);
        double result;
        switch (normalized) {
            case "add":
            case "+":
                result = left + right;
                break;
            case "sub":
            case "-":
                result = left - right;
                break;
            case "mul":
            case "*":
                result = left * right;
                break;
            case "div":
            case "/":
                if (right == 0) {
                    throw new IllegalArgumentException("cannot divide by zero");
                }
                result = left / right;
                break;
            case "mod":
            case "%":
                if (right == 0) {
                    throw new IllegalArgumentException("cannot modulo by zero");
                }
                result = left % right;
                break;
            case "pow":
            case "^":
                result = Math.pow(left, right);
                break;
            default:
                throw new IllegalArgumentException("unsupported operator: " + operator);
        }
        log.info("Calc compute {} {} {} = {}", left, normalized, right, result);
        return result;
    }
}


