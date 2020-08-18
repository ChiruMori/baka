package work.cxlm.main;

import work.cxlm.anno.Controller;
import work.cxlm.anno.Mapping;
import work.cxlm.http.HttpRequest;
import work.cxlm.http.HttpResponse;
import work.cxlm.http.HttpSession;
import work.cxlm.http.RequestType;
import work.cxlm.util.Config;
import work.cxlm.util.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author cxlm
 * Created 2020/5/5 22:03
 * 处理 Controller 链的静态类
 */
public class ControllerLinker {
    private static final LinkedList<ControllerToken> listenLink = new LinkedList<>();  // 监听链

    private static final Logger LOGGER = Logger.getLogger(ControllerLinker.class);

    static class ControllerToken {
        private final Method  method;
        private final String url;  // 映射的 URL（正则表达式）
        private final RequestType type;

        ControllerToken(Method method, String url, String type) {
            this.method = method;
            this.url = url;
            this.type = RequestType.getType(type);
        }
    }

    // 链式处理请求
    public static void dispatch(HttpRequest request, HttpResponse response) {
        String requestUrl = request.getURL();
        RequestType requestType = request.getType();
        boolean partMatch = false;
        for (ControllerToken token : listenLink) {
            if (!requestUrl.matches(token.url)) continue;
            partMatch = true;
            if (requestType != token.type && token.type != RequestType.ALL) continue;
            // 命中，进行处理
            try {
                Class<?>[] parameterTypes = token.method.getParameterTypes();
                Object[] params = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    // 如果拓展更灵活的参数，需要修改这里
                    params[i] = parameterTypes[i] == HttpRequest.class ? request : response;
                }
                // 绑定 session，如果没有 session 则生成 session 并绑定到 response，注意
                // 注意如果没有 session 则通过 request 找不到 session，但是可以通过 response 得到新建的 session 实例
                HttpSession session = request.getSession();
                if (session == null) {
                    session = new HttpSession();
                    response.setCookie("SID", session.getId());
                } else {
                    session.extend();  // session 续命
                }
                response.session = session;
                // 调用方法并处理返回值
                Object result = token.method.invoke(null, params);
                if (result != null && response.emptyBody()) {
                    if (!response.setBody(result)) {
                        LOGGER.log(Logger.Level.ERROR, "不被支持的返回值类型：" + result.getClass().getName());
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                LOGGER.log(Logger.Level.ERROR, e.getLocalizedMessage());
                e.printStackTrace();
                response.writeBytesToBody(e.getLocalizedMessage().getBytes());
                response.internalError();
            }
            return;
        }
        if (partMatch || request.isBadRequest()) response.badRequest();
        else response.notFound();
    }

    public static void resolveController() throws IOException {
        // 本方法与测试用的 TestMain 耦合度较高
        ClassLoader rootClassLoader = Thread.currentThread().getContextClassLoader();
        Set<Class<?>> classSet = new HashSet<>();
        String packageName = Config.get("controllers");
        Enumeration<URL> resources = rootClassLoader.getResources(packageName.replace(".", "/"));
        // 获取所有类对象
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if (url == null) continue;
            String protocol = url.getProtocol();
            if (!protocol.equals("file")) continue;
            String packagePath = url.getPath().replace("%20", " ");
            getAndAddClasses(classSet, packagePath, packageName, rootClassLoader);
        }
        // 扫描每个类中的 Mapping
        classSet.forEach(cls -> {
            Method[] methods = cls.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Mapping.class)) {
                    Mapping mapping = method.getAnnotation(Mapping.class);
                    ControllerToken token = new ControllerToken(method, mapping.url(), mapping.method());
                    listenLink.addFirst(token);  // 后来居上
                    LOGGER.log(Logger.Level.NORMAL, String.format("Mapping %s: \"%s\" to %s.%s",
                            mapping.method(), mapping.url(), cls.getSimpleName(), method.getName()));
                }
            }
        });
    }

    private static void getAndAddClasses(Set<Class<?>> classSet, String packagePath, String packageName, ClassLoader loader) {
        // 列出所有目录、class 文件
        File[] files = new File(packagePath).listFiles(pathname ->
                (pathname.isFile() && pathname.getName().endsWith(".class")) || pathname.isDirectory());
        for (File file : Objects.requireNonNull(files)) {
            String fileName = file.getName();
            if (file.isFile()) {
                String className = packageName + "." + fileName.substring(0, fileName.lastIndexOf('.'));
                try {
                    Class<?> cls = Class.forName(className, false, loader);
                    // 将 Controller 注解的类添加到集合
                    if (cls.isAnnotationPresent(Controller.class))
                        classSet.add(cls);
                } catch (ClassNotFoundException e) { // do nothing
                }
            } else {
                getAndAddClasses(classSet, packagePath + "/" + fileName, packageName + "." + fileName, loader);
            }
        }
    }
}
