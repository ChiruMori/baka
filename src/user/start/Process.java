package user.start;

import work.cxlm.util.Logger;

/**
 * @author cxlm
 * Created 2020/5/3 19:51
 * 指示程序启动的阶段，不可以修改本类类名、方法名，可以在指定的方法中实现功能
 * 本类方法均通过反射运行，如果不需要相关功能，可以直接移除本类
 */
@SuppressWarnings("unused")
public class Process {

    private static final Process instance = new Process();  // 饿汉式单例
    private final Logger logger = Logger.getLogger(Process.class);
    // 程序启动耗时
    private long startExpenseTime;

    private Process() {
    }

    // -------------------------
    // 不可修改下列方法名，但是可以修改方法体
    // -------------------------

    private void beforeStarting() {
        logger.log(Logger.Level.NORMAL, "正在启动 baka...");
        startExpenseTime = System.currentTimeMillis();
    }

    private void afterStarting() {
        startExpenseTime = System.currentTimeMillis() - startExpenseTime;
        logger.log(Logger.Level.NORMAL, "完成启动，耗时 " + startExpenseTime + " ms");
    }
}
