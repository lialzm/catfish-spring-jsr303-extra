package com.catfish.util;

/**
 * Created by A on 2017/4/10.
 */
public class ReflectUtils {

    /**
     * 获取方法调用类
     */
    public static String getCaller() {
        StackTraceElement[] stacks = (new Throwable()).getStackTrace();
        int i = 0;
        for (StackTraceElement ste : stacks
                ) {
            String className = ste.getClassName();
            String methodName = ste.getMethodName();
            if (className.equals("com.catfish.validator.FlowValidator") && methodName.equals("valida")) {
                break;
            }
            i++;
        }
        StackTraceElement stackTraceElement = stacks[i + 1];
        if (stackTraceElement.getClassName().equals("com.catfish.validator.FlowValidator")) {
            stackTraceElement = stacks[i + 2];
        }
        return stackTraceElement.getClassName();
    }


}
