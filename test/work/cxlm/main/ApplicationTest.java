package work.cxlm.main;

import tester.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author cxlm
 * Created 2020/5/3 20:34
 * 启动类、核心功能测试
 */
public class ApplicationTest {

    @Test
    public static void startMain() throws ReflectiveOperationException, IOException {
        Application.main(null);

        try (Socket socket = new Socket(InetAddress.getLocalHost(), 8547)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write("GET / HTTP/1.1\r\nHost: 127.0.0.1:8547\r\nAccept: text/html\r\n\r\n");  // 模拟一个简单的 Get 请求
            writer.flush(); // 不可缺少
            //writer.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            char[] buffer = new char[1024];
            int readChars = reader.read(buffer);
            System.out.println("接受到响应：" + new String(buffer, 0, readChars));
        }
    }

}
