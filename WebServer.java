
import java.io.IOException;
import java.net.ServerSocket;

public class WebServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        final int port = 6789; // port usat per connectar-se amb aquest servidor, TCP

        ServerSocket ss = new ServerSocket(port); // obrim una connexio
        while (true) {
            HttpRequest hr = new HttpRequest(ss.accept()); // acceptam la connexio
            Thread t = new Thread(hr); // "tiram" un fil i esperam a una nova connexio
            t.start();
        }

    }
}
