package work.cxlm.main;

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
    private Selector selector;
    private static final Logger LOGGER = Logger.getLogger(SubReactor.class);
    boolean restartFlag = false;

    SubReactor() throws IOException {
        selector = Selector.open();
    }

    void dispatch(SocketChannel channel) throws ClosedChannelException {
        channel.register(selector, SelectionKey.OP_READ);  // 注册可读处理
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
                    if (key.isValid() && key.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        SocketChannel readChannel = (SocketChannel) key.channel();
                        int readCount = readChannel.read(buffer);
                        if (readCount > 0) {
                            // TODO 拼接 TCP 请求报文
                            System.out.println(new String(buffer.array(), 0, readCount));
                        } else if (readCount < 0) {
                            readChannel.close();
                            key.cancel();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
