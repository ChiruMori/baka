package user.start;

import work.cxlm.util.Logger;

/**
 * @author cxlm
 * Created 2020/5/3 19:51
 * 指示程序启动的阶段，不可以修改本类类名、方法名，可以在指定的方法中实现功能
 * 本类方法均通过反射运行
 */
@SuppressWarnings("unused")
public class Process {

    private static final Process instance = new Process();  // 饿汉式单例
    private final Logger logger = Logger.getLogger(Process.class);
    // 程序启动耗时
    private long startExpenseTime;

    /*// 程序进行状态
    private ProcessState state = ProcessState.STARTING;
    private enum ProcessState {
        STARTING, RUNNING
    }*/

    private Process() {
    }

    // 可以修改下列方法

    private void beforeStarting() {
        logger.log(Logger.Level.NORMAL, "正在启动 baka...");
        startExpenseTime = System.currentTimeMillis();
    }

    private void afterStarting() {
        startExpenseTime = System.currentTimeMillis() - startExpenseTime;
        logger.log(Logger.Level.NORMAL, "完成启动，耗时 " + startExpenseTime + " ms");
        // state = ProcessState.RUNNING;  // 修改运行状态
    }
}
