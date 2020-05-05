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
    private Map<String, String> headerMap;
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

    public byte[] getHeaderData() {
        return toString().getBytes();
    }

    public byte[] getBodyData() {
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
}
