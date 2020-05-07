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
 * 主类，程序入口，程序通过本类中的 main 方法启动
 */
public class Application {
    private static final Logger LOGGER = Logger.getLogger(Application.class);

    public static final int BUFFER_SIZE = 1024;  // 本应用使用的缓冲区大小

    private static ExecutorService pool = null;

    public static void main(String[] args) throws ReflectiveOperationException {
        Class<?> process = null;
        Object instance = null;
        try {
            process = Class.forName("user.start.Process");
            Field processInstanceField = process.getDeclaredField("instance");
            processInstanceField.setAccessible(true);  // 暴力反射
            instance = processInstanceField.get(null);  // 获取单例
            Method beforeStartingMethod = process.getDeclaredMethod("beforeStarting");
            beforeStartingMethod.setAccessible(true);
            beforeStartingMethod.invoke(instance);
        } catch (ClassNotFoundException e) {
            LOGGER.log(Logger.Level.NORMAL, "已停用 Process 类");
        }
        initServer();
        try {
            ControllerLinker.resolveController();  // 注册 Controller
        } catch (IOException e) {
            LOGGER.log(Logger.Level.ERROR, "注册 Controller 失败");
            throw new Error(e.getMessage(), e);
        }
        if (process != null) {
            Method afterStartingMethod = process.getDeclaredMethod("afterStarting");
            afterStartingMethod.setAccessible(true);
            afterStartingMethod.invoke(instance);
        }
    }

    private static void initServer() {
        final int[] reactorId = {1};
        pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors(), 5, TimeUnit.MINUTES,  // 定长线程池
                new LinkedBlockingQueue<>(), run -> new Thread(run, "Reactor 线程" + reactorId[0]++));
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
