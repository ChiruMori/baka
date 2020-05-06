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
    private static final HashMap<String, String> CONTENT_MAP;

    static String staticPath;

    static {
        staticPath = Config.get("staticPath");
        if (!staticPath.endsWith("/") && !staticPath.isBlank()) {
            staticPath += "/";
        }
        if (!new File(staticPath).exists()) {
            LOGGER.log(Logger.Level.ERROR, "静态资源路径不存在");
        }
        CONTENT_MAP = new HashMap<>();
        CONTENT_MAP.put(".ico", "image/x-icon");
        CONTENT_MAP.put(".png", "image/png");
        CONTENT_MAP.put(".jpg", "image/jpeg");
        CONTENT_MAP.put(".jpeg", "image/jpeg");
        CONTENT_MAP.put(".gif", "image/gif");
        CONTENT_MAP.put(".js", "application/javascript; charset=utf-8");
        CONTENT_MAP.put(".css", "text/css; charset=utf-8");
    }

    @Mapping(method = "GET", url = "/")
    public static void mappingIndex(HttpResponse response) {
        File indexFile = new File(staticPath + "index.html");
        fileResponse(response, indexFile);
    }

    @Mapping(method = "*", url = "/.+")
    public static void mappingStaticFile(HttpRequest request, HttpResponse response) {
        String targetFileName = request.getURL().substring(1);
        File staticFile = new File(staticPath + targetFileName);
        fileResponse(response, staticFile);
    }

    private static void fileResponse(HttpResponse response, File target) {
        if (!target.exists()) {
            response.notFound();
            return;
        }
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(target))) {
            response.writeBytesToBody(inputStream.readAllBytes());
            String fileName = target.getName();
            CONTENT_MAP.forEach((k, v) -> {
                if (fileName.endsWith(k)) response.setHeader("Content-Type", v);
            });
        } catch (IOException e) {
            LOGGER.log(Logger.Level.ERROR, e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
