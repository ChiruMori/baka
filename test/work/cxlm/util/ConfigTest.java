package work.cxlm.util;

import tester.Test;

/**
 * @author cxlm
 * Created 2020/5/2 21:21
 * Modified 2020/5/2 21:21 by cxlm
 * Cirno bless, there is no bug in the code.
 * Description 配置加载测试
 */
public class ConfigTest {

    @Test
    public static void parseConfig() {
        assert Config.get("port").equals("8547") : "配置文件解析不正确";
        assert Config.get("2147483648") == null : "解析到错误的值：[" + Config.get("214748368") + "]";
        System.out.println(Config.get("port"));
    }
}
