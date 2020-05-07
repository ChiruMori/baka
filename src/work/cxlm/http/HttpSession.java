package work.cxlm.http;

import work.cxlm.util.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author cxlm
 * Created 2020/5/7 16:27
 * Session 支持，自动销毁
 */
public class HttpSession implements Comparable<HttpSession> {

    // 类静态数据
    private static final Logger LOGGER = Logger.getLogger(HttpSession.class);
    private static final ConcurrentHashMap<String, HttpSession> sessions = new ConcurrentHashMap<>();
    private static final PriorityQueue<HttpSession> sessionHeap = new PriorityQueue<>();
    private static final String idPrefix = "SID";
    private static final long DEFAULT_TIME = 1800_000; // 30 min
//    private static final long DEFAULT_TIME = 10_000; // 测试用 10s

    static {
        Timer timer = new Timer();
        // 30 min 后第一次执行，以后每隔 5 min 执行一次 session 清除
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator<HttpSession> it = sessionHeap.iterator();
                while (it.hasNext()) {
                    HttpSession thisSession = it.next();
                    if (now > thisSession.deadline) {
                        it.remove();
                        sessions.remove(thisSession.sessionId);
                        LOGGER.log(Logger.Level.DEBUG, "清除 Session: " + thisSession.sessionId);
                    }
                }
            }
//        }, 5000, 10_000);  // 测试用
        }, DEFAULT_TIME, 300_000);
    }

    // 类实例变量
    private final String sessionId;
    private final HashMap<String, Object> sessionData;
    private final long extendTime;
    private long deadline;

    public static HttpSession getSession(String sid) {
        return sessions.get(sid);
    }

    /**
     * 创建一个 Session
     *
     * @param lifetime   声明周期，单位：ms，如果为 -1 则表示永不过期
     * @param extendTime 续命时长，这里的续命指当前 session 在当前时间 + 续命时间后过期
     */
    public HttpSession(long lifetime, long extendTime) {
        sessionId = idPrefix + System.currentTimeMillis();
        sessionData = new HashMap<>();
        this.extendTime = extendTime;
        deadline = System.currentTimeMillis() + lifetime;
        sessions.put(sessionId, this);
        if (lifetime != -1) sessionHeap.offer(this);
    }

    /**
     * 创建一个 Session，30 min 生命周期，每次续命为 30 min
     * 这里的续命指当前 session 在当前时间 + 续命时间后过期
     */
    public HttpSession() {
        this(DEFAULT_TIME, DEFAULT_TIME);
    }

    /**
     * 向 Session 中设置值
     *
     * @param key 键
     * @param val 值
     */
    public void set(String key, Object val) {
        sessionData.put(key, val);
    }

    /**
     * 从 Session 中获取值
     *
     * @param key 键
     * @return 值，如果不存在返回 null
     */
    public Object get(String key) {
        return sessionData.get(key);
    }

    // 续命
    public void extend() {
        if (deadline == -1) return;
        sessionHeap.remove(this); // 更新
        deadline = System.currentTimeMillis() + extendTime;
        sessionHeap.offer(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpSession that = (HttpSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return sessionId.hashCode();
    }

    @Override
    public int compareTo(HttpSession o) {
        return (int) (deadline - o.deadline);
    }

    /**
     * @return session ID
     */
    public String getId() {
        return sessionId;
    }
}
