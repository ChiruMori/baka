package work.cxlm.main;

import tester.Test;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author cxlm
 * Created 2020/5/3 20:34
 * 启动类、核心功能测试
 */
public class ApplicationTest {

    @Test
    public static void startMain() throws ReflectiveOperationException, IOException {
        Application.main(null);

        try(Socket socket = new Socket(InetAddress.getLocalHost(), 8547)){
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writer.write("this is a pen. this is a pen. this is a pen. this is a pen. this is a pen. this is a pen. this is a pen.");
            writer.flush();
            writer.close();
        }
    }

}
