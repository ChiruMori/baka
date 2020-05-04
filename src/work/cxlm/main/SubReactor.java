package work.cxlm.main;

import work.cxlm.http.HttpRequest;
import work.cxlm.http.RequestDealer;
import work.cxlm.util.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * @author cxlm
 * Created 2020/5/3 23:50
 * 从属 Reactor
 */
public class SubReactor implements Runnable {
    private final Selector selector;
    private static final Logger LOGGER = Logger.getLogger(SubReactor.class);
    boolean restartFlag = false;

    SubReactor() throws IOException {
        selector = Selector.open();
    }

    void dispatch(SocketChannel channel) throws ClosedChannelException {
        channel.register(selector, SelectionKey.OP_READ);  // 当前选择器将负责指定信道的读取、写入消息
    }

    // 当新事件注册时，需要唤醒阻塞，当注册完成后再进行监听
    void wakeup() {
        selector.wakeup();
    }

    @SuppressWarnings("all")
    public void run() {
        for (; ; ) {
            if (restartFlag) {
                Thread.onSpinWait();
                continue;
            }
            try {
                if (selector.select() <= 0) continue;
                Set<SelectionKey> keys = selector.selectedKeys();
                var iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(RequestDealer.BUFFER_SIZE);
                        SocketChannel clientInfoChannel = (SocketChannel) key.channel();
                        int readCount = clientInfoChannel.read(buffer);
                        if (readCount > 0) {  // 记录请求
                            RequestDealer.put(clientInfoChannel, buffer.array(), readCount);
                        }
                        if (readCount < RequestDealer.BUFFER_SIZE) {
                            clientInfoChannel.shutdownInput();
                            clientInfoChannel.register(selector, SelectionKey.OP_WRITE);  // 注册为可写
                        }
                    } else if (key.isWritable()) {
                        SocketChannel clientInfoChannel = (SocketChannel) key.channel();
                        HttpRequest request = RequestDealer.getAndRemove(clientInfoChannel);
                        LOGGER.log(Logger.Level.NORMAL, request.toString());
                        // TODO 处理用户请求、返回
                        byteToChannel("HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\n\r\nResponse 通了".getBytes(), clientInfoChannel);
                        clientInfoChannel.close();
                        key.cancel();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void byteToChannel(byte[] bytes, SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(RequestDealer.BUFFER_SIZE);
        int offset = 0, groupSize, total = bytes.length;
        while (offset < total) {
            groupSize = offset + RequestDealer.BUFFER_SIZE < total ?
                    RequestDealer.BUFFER_SIZE : total % RequestDealer.BUFFER_SIZE;
            buffer.put(bytes, offset, groupSize);
            buffer.flip();
            offset += groupSize;
            client.write(buffer);
            buffer.clear();
        }
    }
}
