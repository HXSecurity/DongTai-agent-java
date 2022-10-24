package io.dongtai.iast.core.utils;

/**
 * 获取堆栈hook点的应用堆栈情况
 *
 * @author dongzhiyong@huoxian.cn
 */
public class StackUtils {
    public static StackTraceElement[] createCallStack(int stackStartPos) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement[] selfCallStack = new StackTraceElement[0];
        if (stackTraceElements.length - stackStartPos >= 0) {
            selfCallStack = new StackTraceElement[stackTraceElements.length - stackStartPos];
            System.arraycopy(stackTraceElements, stackStartPos, selfCallStack, 0, stackTraceElements.length - stackStartPos);
        }
        return selfCallStack;
    }

    public static StackTraceElement getLatestStack(int stackStartPos) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        return stackTraceElements[stackStartPos];
    }
}
