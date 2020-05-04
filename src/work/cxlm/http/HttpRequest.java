package work.cxlm.http;

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
    private Map<String, String> head;

    public HttpRequest() {
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
    public void resolve() {
        String[] lines = new String(rawData).split("\r\n");
        // 请求行: GET /cx?lm=9 HTTP/1.1
        String[] requestLine = lines[0].split(" ");
        type = RequestType.getType(requestLine[0]);
        // FIXME 解析查询
        url = requestLine[1];
        head = new HashMap<>();
        for (int i = 1; !lines[i].isBlank(); i++) {
            String[] kv = lines[i].split(": ");
            head.put(kv[0], kv[1]);
        }
        if (type == RequestType.POST) {
            // TODO 解析请求正文
        }
    }

    @Override
    public String toString() {
        // TODO Http 类完成时，修改本方法为易读型
        return new String(rawData);
    }
}
