package work.cxlm.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * @author cxlm
 * 2020/5/2 19:57
 * 配置读取与默认配置
 */
public class Config {

    /**
     * 默认配置文件路径，如需使用自定义配置文件需要修改本字段
     */
    private static final String CONFIG_FILE_PATH = "config.properties";

    private static final Logger LOGGER = Logger.getLogger(Config.class);
    private static Properties dict = null;

    static {
        parseConfig();
    }

    private static void parseConfig() {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(CONFIG_FILE_PATH)))) {
            dict = new Properties();
            dict.load(reader);
        } catch (IOException e) {
            System.err.println("加载配置文件出错" + e.getLocalizedMessage());
            throw new Error("加载配置文件失败", e);
        }
        LOGGER.log(Logger.Level.NORMAL, "共解析 " + dict.size() + " 条配置");
    }

    /**
     * 获取在 config.properties 中配置的信息
     *
     * @param key 键
     * @return 指定 key 对应的字符串值
     */
    public static String get(String key) {
        return (String) dict.get(key);
    }
}
