package work.cxlm.main;

import work.cxlm.util.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author cxlm
 * Created 2020/5/3 20:21
 * 主类，程序通过本类中的 main 方法启动
 */
public class Application {
    private static final Logger LOGGER = Logger.getLogger(Application.class);

    private static ExecutorService pool = null;

    public static void main(String[] args) throws ReflectiveOperationException {
        Class<?> process = Class.forName("user.start.Process");
        Field processInstanceField = process.getDeclaredField("instance");
        processInstanceField.setAccessible(true);  // 暴力反射
        Object instance = processInstanceField.get(null);  // 获取单例
        Method beforeStartingMethod = process.getDeclaredMethod("beforeStarting");
        beforeStartingMethod.setAccessible(true);
        beforeStartingMethod.invoke(instance);
        initServer();
        Method afterStartingMethod = process.getDeclaredMethod("afterStarting");
        afterStartingMethod.setAccessible(true);
        afterStartingMethod.invoke(instance);
        try {
            // TODO 监听
            LOGGER.log(Logger.Level.NORMAL, "监听服务代码");
        } finally {
            Method beforeTerminateMethod = process.getDeclaredMethod("beforeTerminate");
            beforeTerminateMethod.setAccessible(true);
            beforeTerminateMethod.invoke(instance);
        }
    }

    private static void initServer() {
        pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() << 1, 500, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
        try {
            MainReactor mainReactor = new MainReactor();
            pool.submit(mainReactor);
            for (SubReactor subReactor : mainReactor.subReactors) {
                pool.submit(subReactor);
            }
        } catch (IOException e) {
            LOGGER.log(Logger.Level.ERROR, "服务启动失败: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        LOGGER.log(Logger.Level.NORMAL, "绑定 Reactor");
    }
}
