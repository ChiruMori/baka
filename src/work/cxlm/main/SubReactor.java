package work.cxlm.main;

import work.cxlm.http.HttpRequest;
import work.cxlm.http.HttpResponse;
import work.cxlm.util.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author cxlm
 * Created 2020/5/3 23:50
 * 从属 Reactor
 */
public class SubReactor implements Runnable {
    private final Selector selector;
    private static final Logger LOGGER = Logger.getLogger(SubReactor.class);
    private final AtomicBoolean hasWork;
    private final ReentrantLock mutex;  // 代码块互斥控制，与自旋锁效果相同，问题相同
    private final ReentrantLock blockingFlag;

    SubReactor() throws IOException {
        selector = Selector.open();
        hasWork = new AtomicBoolean(false);
        mutex = new ReentrantLock();
        blockingFlag = new ReentrantLock();
    }

    void dispatch(SocketChannel channel) throws ClosedChannelException {
        mutex.lock();
        channel.register(selector, SelectionKey.OP_READ);  // 当前选择器将负责指定信道的读取、写入消息
        if (!hasWork.get()) {
            hasWork.set(true);
            if (blockingFlag.hasQueuedThreads()) {
                blockingFlag.unlock();
            }
        }
        mutex.unlock();
    }

    @SuppressWarnings("all")
    public void run() {
        for (; ; ) {
            try {
                if (selector.select(100) <= 0) continue;  // 不在这里阻塞
                Set<SelectionKey> keys = selector.selectedKeys();
                mutex.lock();
                hasWork.set(false);
                mutex.unlock();
                var iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(Application.BUFFER_SIZE);
                        SocketChannel clientInfoChannel = (SocketChannel) key.channel();
                        int readCount = clientInfoChannel.read(buffer);
                        if (readCount > 0) {  // 记录请求
                            HttpRequest target = (HttpRequest) key.attachment();
                            if (target == null) {
                                target = new HttpRequest();
                                key.attach(target);
                            }
                            target.putData(buffer.array(), readCount);
                        }
                        if (readCount < Application.BUFFER_SIZE) {
                            HttpRequest request = (HttpRequest) key.attachment();
                            clientInfoChannel.shutdownInput();
                            if (request != null) {
                                request.resolve();
                                clientInfoChannel.register(selector, SelectionKey.OP_WRITE, request);  // 注册为可写
                            } else {  // 测试时发现，很多客户端发送为空的请求，如果不处理将导致从 Reactor 所在的整个线程死亡
                                LOGGER.log(Logger.Level.DEBUG, "非法请求: "+request);
                                clientInfoChannel.close();
                                key.cancel();
                            }
                        }
                        // LOGGER.log(Logger.Level.NORMAL, clientInfoChannel.hashCode() + " read " + new String(buffer.array(), 0, readCount));
                    } else if (key.isWritable()) {
                        SocketChannel clientInfoChannel = (SocketChannel) key.channel();
                        // LOGGER.log(Logger.Level.NORMAL, clientInfoChannel.hashCode() + " writing...");
                        HttpRequest request = (HttpRequest) key.attachment();
                        HttpResponse response = new HttpResponse();
                        if(request == null || request.isBadRequest()){
                            response.badRequest();
                        }else {
                            ControllerLinker.dispatch(request, response);  // 处理用户请求
                            LOGGER.log(Logger.Level.NORMAL, request.getMethod() + " " + request.getURL() + " " + response.getStatus());
                        }
                        byteToChannel(response.getHeaderRawData(), clientInfoChannel);
                        byteToChannel(response.getBodyRawData(), clientInfoChannel);
                        clientInfoChannel.close();
                        key.cancel();
                    }
                }
            } catch (Throwable e) {
                // 捕获所有歪门邪道的情况，如果不进行捕获，可能线程咋死的都不知道
                LOGGER.log(Logger.Level.ERROR, "异常终止: " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

    private static void byteToChannel(byte[] bytes, SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Application.BUFFER_SIZE);
        int offset = 0, groupSize, total = bytes.length;
        while (offset < total) {
            groupSize = offset + Application.BUFFER_SIZE < total ?
                    Application.BUFFER_SIZE : total % Application.BUFFER_SIZE;
            buffer.put(bytes, offset, groupSize);
            buffer.flip();
            offset += groupSize;
            client.write(buffer);
            buffer.clear();
        }
    }
}
