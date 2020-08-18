package work.cxlm.http;

import org.json.CookieList;
import org.json.JSONArray;
import org.json.JSONObject;
import work.cxlm.util.Logger;

import java.util.Arrays;
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
    private JSONObject cookies;
    private HttpSession session;
    private static final Logger LOGGER = Logger.getLogger(HttpRequest.class);
    private boolean badRequest = false;

    public HttpRequest() {
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
    public String getParameter(String key) {
        return queryMap.get(key);
    }

    /**
     * 获取请求正文中的值
     *
     * @param key 键
     * @return 值
     */
    public String getAttribute(String key) {
        return requestBodyMap.get(key);
    }

    /**
     * 获取请求中的 Json 格式数据
     *
     * @return json 字符串解析后的 JsonObject，可能为 null
     */
    public JSONObject getJson() {
        return jsonData;
    }

    /**
     * 获取 cookie 的值
     *
     * @param key cookie name
     * @return cookie value
     */
    public String getCookie(String key) {
        return cookies.getString(key);
    }

    /**
     * 获取绑定的 Session
     * 注意，如果当前请求是新的请求，则通过 request 找不到新建的 session
     * 但是可以通过 response 得到新的 session，再一次请求时，可以获得到这个 session
     */
    public HttpSession getSession() {
        return session;
    }

    /**
     * 获取请求中的 Json 数组格式数据
     *
     * @return json 字符串解析后的 JsonArray，可能为 null
     */
    public JSONArray getJsonArray() {
        return jsonArray;
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
        if(requestLine.length != 3){
            LOGGER.debug(Arrays.toString(requestLine));
            badRequest = true;
            return;
        }
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
        // 处理 SessionList
        if (head.containsKey("Cookie")) {
            cookies = CookieList.toJSONObject(head.get("Cookie"));
            // 解析 Session
            if (cookies.has("SID")) {
                session = HttpSession.getSession(cookies.getString("SID"));
            }
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

    /**
     * @return 请求类型的字符串表示，比如 "GET", "POST
     */
    public String getMethod() {
        return type.name();
    }

    /**
     * @return 请求类型
     */
    public RequestType getType() {
        return type;
    }

    public String getURL() {
        return url;
    }

    public boolean isBadRequest(){
        return badRequest;
    }
}
