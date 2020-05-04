package tester;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author cxlm
 * 2020/5/2 21:25
 * 测试总控类
 */
public class TestMain {
    private static final boolean ENABLE_TEST = true;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (!ENABLE_TEST) return;
        ClassLoader rootClassLoader = Thread.currentThread().getContextClassLoader();
        Set<Class<?>> classSet = new HashSet<>();
        String packageName = "work.cxlm";
        Enumeration<URL> resources = rootClassLoader.getResources(packageName.replace(".", "/"));
        // 获取所有测试类 class 对象
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if (url == null) continue;
            String protocol = url.getProtocol();
            if (!protocol.equals("file")) continue;
            String packagePath = url.getPath().replace("%20", " ");
            getAndAddClasses(classSet, packagePath, packageName, rootClassLoader);
        }
        // 运行所有测试类中 @Test 标注的方法，只允许共有静态方法，如需运行其他作用域方法需要修改代码
        classSet.forEach(cls->{
            for (Method method : cls.getDeclaredMethods()) {
                if(method.isAnnotationPresent(Test.class)){
                    try {
                        method.invoke(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        System.out.println("完成测试");
    }

    // 通过类加载器获取所有 class 文件，并解析出所有 class 对象（*Test.class）
    private static void getAndAddClasses(Set<Class<?>> classSet, String packagePath, String packageName, ClassLoader loader) throws ClassNotFoundException {
        // 列出所有目录、class 文件
        File[] files = new File(packagePath).listFiles(pathname ->
                (pathname.isFile() && pathname.getName().endsWith("Test.class")) || pathname.isDirectory());
        for (File file : Objects.requireNonNull(files)) {
            String fileName = file.getName();
            if (file.isFile()) {
                String className = packageName + "." + fileName.substring(0, fileName.lastIndexOf('.'));
                classSet.add(Class.forName(className, false, loader));
            } else {
                getAndAddClasses(classSet, packagePath + "/" + fileName, packageName + "." + fileName, loader);
            }
        }
    }
}
