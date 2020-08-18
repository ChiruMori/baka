package work.cxlm.http;

import org.json.JSONArray;
import org.json.JSONObject;
import work.cxlm.util.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author cxlm
 * Created 2020/5/5 13:50
 * Http 响应
 */
public class HttpResponse {
    // 享元：日期格式化对象
    private static final SimpleDateFormat DATE_FORMAT;
    private static final String BR = "\r\n";

    private static final Logger LOGGER = Logger.getLogger(HttpResponse.class);

    private static final HashMap<String, String> CONTENT_MAP;

    static {
        DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);  // 使用英文
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));  // 使用全球标准时间

        CONTENT_MAP = new HashMap<>();
        CONTENT_MAP.put(".ico", "image/x-icon");
        CONTENT_MAP.put(".png", "image/png");
        CONTENT_MAP.put(".jpg", "image/jpeg");
        CONTENT_MAP.put(".jpeg", "image/jpeg");
        CONTENT_MAP.put(".gif", "image/gif");
        CONTENT_MAP.put(".js", "application/javascript; charset=utf-8");
        CONTENT_MAP.put(".css", "text/css; charset=utf-8");
        // 未配置的均按照 text/html; charset=utf-8 解析
    }


    private ResponseStatus status;
    private final Map<String, String> headerMap;
    private JSONObject jsonBody;
    private JSONArray arrayBody;
    private byte[] bytesBody;
    public HttpSession session;

    private static final String VERSION = "HTTP/1.1 ";

    /**
     * 设置响应头
     */
    public void setHeader(String key, String val) {
        headerMap.put(key, val);
    }

    /**
     * 设置 Cookie，如果需要复杂的 cookie 超时、path 等功能需要手动拓展
     *
     * @param name cookie name
     * @param val  cookie value
     */
    public void setCookie(String name, String val) {
        setHeader("Set-Cookie", name + "=" + val);
    }

    public HttpResponse() {
        status = ResponseStatus.OK;
        headerMap = new HashMap<>();
        headerMap.put("Content-Type", "text/html; charset=utf-8");
        headerMap.put("Date", DATE_FORMAT.format(new Date()));
    }

    @Override
    public String toString() {
        // 状态行
        StringBuilder resultBuilder = new StringBuilder(VERSION);
        resultBuilder.append(status.getMsg()).append(BR);
        // 响应头
        headerMap.forEach((k, v) -> resultBuilder.append(k).append(": ").append(v).append(BR));
        // 空行
        resultBuilder.append(BR);
        return resultBuilder.toString();
    }

    public byte[] getHeaderRawData() {
        return toString().getBytes();
    }

    public byte[] getBodyRawData() {
        byte[] body;
        if (jsonBody != null) body = jsonBody.toString().getBytes();
        else if (arrayBody != null) body = arrayBody.toString().getBytes();
        else body = bytesBody;
        if (body == null) return new byte[0];
        return body;
    }

    public boolean setBody(Object data) {
        if (data instanceof JSONObject) {
            jsonBody = (JSONObject) data;
        } else if (data instanceof JSONArray) {
            arrayBody = (JSONArray) data;
        } else if (data instanceof byte[]) {
            bytesBody = (byte[]) data;
        } else if (data instanceof String) {
            bytesBody = ((String) data).getBytes();
        } else if (data instanceof File) {
            File target = (File) data;
            if (!target.exists()) {
                notFound();
            } else if (!target.isFile() || !target.canRead()) {
                forbidden();
            } else {
                try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(target))) {
                    writeBytesToBody(inputStream.readAllBytes());
                    String fileName = target.getName();
                    CONTENT_MAP.forEach((k, v) -> {
                        if (fileName.endsWith(k)) setHeader("Content-Type", v);
                    });
                } catch (IOException e) {
                    LOGGER.log(Logger.Level.ERROR, e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public String getStatus() {
        return status.getMsg();
    }

    public void writeBytesToBody(byte[] data) {
        bytesBody = data;
    }

    public boolean emptyBody() {
        return bytesBody == null && jsonBody == null && arrayBody == null;
    }

    /**
     * 设置此响应为 404 Not Found
     */
    public void notFound() {
        status = ResponseStatus.NOT_FOUND;
    }

    /**
     * 请求重定向，返回 302 ，并在响应头中指定重定向的地址，重定向由客户端完成，浏览器会重定向到指定的 URL
     *
     * @param newUrl 重定向的目标 URL
     */
    public void redirect(String newUrl) {
        status = ResponseStatus.FOUND;
        setHeader("Location", newUrl);
    }

    /**
     * 内部错误
     */
    public void internalError() {
        status = ResponseStatus.INTERNAL_ERROR;
    }

    /**
     * 请求错误，一般指示参数不匹配或 method 不匹配
     */
    public void badRequest() {
        status = ResponseStatus.BAD_REQUEST;
    }

    /**
     * 禁止访问
     */
    public void forbidden() {
        status = ResponseStatus.FORBIDDEN;
    }

    /**
     * 服务器忙
     */
    public void busy() {
        status = ResponseStatus.BUSY;
    }
}
