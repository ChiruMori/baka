package work.cxlm.http;

import org.json.JSONArray;
import org.json.JSONObject;

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

    static {
        DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);  // 使用英文
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));  // 使用全球标准时间
    }

    private ResponseStatus status;
    private final Map<String, String> headerMap;
    private JSONObject jsonBody;
    private JSONArray arrayBody;
    private byte[] bytesBody;

    private static final String VERSION = "HTTP/1.1 ";

    public void setHeader(String key, String val) {
        headerMap.put(key, val);
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

    public String getStatus() {
        return status.getMsg();
    }

    public void writeBytesToBody(byte[] data) {
        bytesBody = data;
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
