import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class Client extends DaifugoApp implements IClient {
    private String name;
    private ClientConnection connect;

    public void Connect(String addr, int port) throws IOException {
        SocketChannel sc = SocketChannel.open(new InetSocketAddress(addr, port));
        sc.configureBlocking(false);
    }
}

interface IClient {

}

class Listner {

}