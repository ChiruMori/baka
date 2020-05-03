package work.cxlm.util;

import tester.Test;

/**
 * @author cxlm
 * 2020/5/2 20:53
 * 全局日志测试
 */
public class LoggerTest {
    static Logger outerLogger = Logger.getLogger(LoggerTest.class);

    static {
        outerLogger.log(Logger.Level.NORMAL, "静态上下文日志");
    }

    @Test
    public static void logTest() {
        Logger logger = Logger.getLogger(LoggerTest.class);
        logger.log(Logger.Level.NORMAL, "常规级别日志测试");
        logger.log(Logger.Level.ERROR, "报错级别日志测试");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.log(Logger.Level.NORMAL, "间隔日志测试");
    }
}
