package work.cxlm.http;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cxlm
 * Created 2020/5/4 22:56
 * http 请求类
 */
public class HttpRequest {

    private byte[] rawData;
    private RequestType type;
    private String url;
    private Map<String, String> head; // 请求头
    private Map<String, String> queryMap; // 查询
    private Map<String, String> requestBodyMap;  // 请求正文
    private JSONObject jsonData;
    private JSONArray jsonArray;

    HttpRequest() {
    }

    /**
     * 获取请求头中的信息
     *
     * @param key 键
     * @return 值
     */
    public String getHead(String key) {
        return head.get(key);
    }

    /**
     * 获取内联查询中的值
     *
     * @param key 键
     * @return 值
     */
    public String getQuery(String key) {
        return queryMap.get(key);
    }

    /**
     * 获取请求正文中的值
     *
     * @param key 键
     * @return 值
     */
    public String getBodyData(String key) {
        return requestBodyMap.get(key);
    }

    // 将拆分的数据重新组合，这不是必须的，通过增大 BUFFER_SIZE 以减少本操作的消耗
    public void putData(byte[] data, int readCount) {
        if (rawData == null) {
            rawData = Arrays.copyOf(data, readCount);
        } else {
            int originLength = rawData.length;
            rawData = Arrays.copyOf(rawData, originLength + readCount);
            System.arraycopy(data, 0, rawData, originLength, readCount);
        }
    }

    // 解析原始数据为实例对象，并释放字节数组
    // 在调用 putData 结束后需要调用本方法解析源字节
    public void resolve() {
        var lines = new String(rawData).lines().iterator();
        // 请求行: GET /cx?lm=9 HTTP/1.1
        String[] requestLine = lines.next().split(" ");
        type = RequestType.getType(requestLine[0]);
        // 解析 url 和查询
        String urlQuery = requestLine[1];
        String[] queries = urlQuery.split("\\?");
        url = queries[0];
        if (queries.length == 2)
            queryMap = resolveQueries(queries[1]);
        // 解析 head
        head = new HashMap<>();
        for (String nowHeader = lines.next(); !nowHeader.isBlank(); nowHeader = lines.next()) { String[] kv = nowHeader.split(": ");
            head.put(kv[0], kv[1]);
        }
        // 解析请求正文
        while (lines.hasNext()) {
            String lineString = lines.next();
            // 在接受字节流时可能有问题
            if (lineString.charAt(0) == '{')
                jsonData = new JSONObject(lineString);
            else if (lineString.charAt(0) == '[')
                jsonArray = new JSONArray(lineString);
            else
                requestBodyMap = resolveQueries(lineString);
        }
        rawData = null;  // 释放源字节数组，辅助 GC
    }

    private Map<String, String> resolveQueries(String queriesString) {
        String[] queries = queriesString.split("&");
        Map<String, String> result = new HashMap<>(queries.length);
        for (String query : queries) {
            String[] kv = query.split("=");
            result.put(kv[0], kv[1]);
        }
        return result;
    }

    @Override
    public String toString() {
        return type.name() + " : " + url;
    }

    public String getMethod() {
        return type.name();
    }

    public String getURL() {
        return url;
    }
}
