package work.cxlm.http;

import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cxlm
 * Created 2020/5/4 22:41
 * 根据信道中得到的数据流构建 Http 请求实例
 */
public class RequestDealer {
    public static final int BUFFER_SIZE = 1024;

    private static final ConcurrentHashMap<SocketChannel, HttpRequest> REQUEST_MAP = new ConcurrentHashMap<>();

    public static void put(SocketChannel key, byte[] data, int readCount) {
        HttpRequest targetRequest;
        if (REQUEST_MAP.containsKey(key)) {
            targetRequest = REQUEST_MAP.get(key);
        } else {
            targetRequest = new HttpRequest();
            REQUEST_MAP.put(key, targetRequest);
        }
        targetRequest.putData(data, readCount);
    }

    public static HttpRequest getAndRemove(SocketChannel key) {
        HttpRequest request = REQUEST_MAP.get(key);
        REQUEST_MAP.remove(key);
        return request;
    }
}
