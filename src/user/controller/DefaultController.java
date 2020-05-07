package user.controller;

import work.cxlm.anno.Controller;
import work.cxlm.anno.Mapping;
import work.cxlm.http.HttpRequest;
import work.cxlm.http.HttpResponse;
import work.cxlm.util.Config;
import work.cxlm.util.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author cxlm
 * Created 2020/5/5 22:39
 * 默认路由
 */
@Controller
public class DefaultController {

    private static final Logger LOGGER = Logger.getLogger(DefaultController.class);

    static String staticPath;

    static {
        staticPath = Config.get("staticPath");
        if (!staticPath.endsWith("/") && !staticPath.isBlank()) {
            staticPath += "/";
        }
        if (!new File(staticPath).exists()) {
            LOGGER.log(Logger.Level.ERROR, "静态资源路径不存在");
        }
    }

    @Mapping(method = "GET", url = "/")
    public static File mappingIndex() {
        return new File(staticPath + "index.html");
    }

    @Mapping(method = "*", url = "/.+")
    public static File mappingStaticFile(HttpRequest request) {
        String targetFileName = request.getURL().substring(1);
        return new File(staticPath + targetFileName);
    }
}
