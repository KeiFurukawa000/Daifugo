import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Client extends DaifugoApp {
    private ClientConnection connect;

    public void Connect(String addr, int port) throws IOException {
        SocketChannel sc = SocketChannel.open(new InetSocketAddress(addr, port));
        sc.configureBlocking(false);
        //connect = new ClientConnection(sc);
        System.out.println("Successed to conenct to the server");
    }
}

interface IClient {
    void RequestCreateAccount(String name);
}