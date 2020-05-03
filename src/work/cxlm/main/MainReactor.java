package work.cxlm.main;

import work.cxlm.util.Config;
import work.cxlm.util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * @author cxlm
 * Created 2020/5/3 23:41
 * 主 Reactor, 处理请求监听、分发
 */
public class MainReactor implements Runnable{

    private static final Logger LOGGER = Logger.getLogger(MainReactor.class);

    private Selector selector;
    private ServerSocketChannel channel;
    SubReactor[] subReactors;

    MainReactor() throws IOException {
        init();  // 初始化 channel, selector 配置
        allocateSubReactors();  // 分配从 Reactor
    }

    private void init() throws IOException {
        selector = Selector.open();
        channel = ServerSocketChannel.open();
        channel.configureBlocking(false);  // 配置非阻塞
        int listenPort = Integer.parseInt(Config.get("port"));
        channel.socket().bind(new InetSocketAddress(listenPort));
        channel.register(selector, SelectionKey.OP_ACCEPT);  // 当前通道接受 socket 操作
    }

    private void allocateSubReactors() throws IOException {
        int coreCount = Runtime.getRuntime().availableProcessors();
        subReactors = new SubReactor[coreCount];  // 根据处理器数目分配从 Reactor
        for (int i = 0; i < subReactors.length; i++) {
            subReactors[i] = new SubReactor();
        }
    }

    @SuppressWarnings("all")
    public void run() {
        int nowSubReactorIndex = 0;
        int subReactorCount = subReactors.length;
        for (; ; ) {
            try {
                if (selector.select() <= 0) continue;  // 阻塞，直到至少一个 key 可用
                Set<SelectionKey> readyKeys = selector.selectedKeys();  // 就绪操作集
                var iterator = readyKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey nowKey = iterator.next();
                    iterator.remove();
                    if (nowKey.isValid() && nowKey.isAcceptable()) {
                        ServerSocketChannel readableChannel = (ServerSocketChannel) nowKey.channel();
                        SocketChannel socketChannel = readableChannel.accept();  // 接受请求
                        socketChannel.configureBlocking(false);
                        if (nowSubReactorIndex >= subReactorCount) nowSubReactorIndex = 0;
                        SubReactor target = subReactors[nowSubReactorIndex++];
                        target.restartFlag = true;
                        // FIXME: 注意，这里可能遗漏一次 wakeup，但是逻辑上并不缺少
                        target.dispatch(socketChannel);
                        target.wakeup();
                        target.restartFlag = false;
                    }
                }
            } catch (IOException e) {
                LOGGER.log(Logger.Level.ERROR, e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    }

}
