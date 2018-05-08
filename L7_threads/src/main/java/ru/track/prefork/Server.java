package ru.track.prefork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * - multithreaded +
 * - atomic counter +
 * - setName() +
 * - thread -> Worker +
 * - save threads
 * - broadcast (fail-safe)
 */
public class Server {
    static Logger log = LoggerFactory.getLogger(Server.class);

    private int port;
    public Server(int port) {
        this.port = port;
    }

    public void serve(Socket socket) throws Exception {
//        ServerSocket serverSocket = new ServerSocket(port, 10, InetAddress.getByName("localhost"));

//        final Socket socket = serverSocket.accept();
        log.info("connected");
        while (true) {
            InputStream inputStream = socket.getInputStream();
            byte[] buf = new byte[1024];

            try {
                int nRead = inputStream.read(buf);
                final OutputStream out = socket.getOutputStream();
                log.info("send:");
                out.write(buf);
                out.flush();
                System.out.print(new String(buf, 0, nRead));
            } catch (Exception e) {
                socket.close();
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(9000);
        ServerSocket serverSocket = new ServerSocket(server.port, 10, InetAddress.getByName("localhost"));

        int ID = 1;
        while (true) {
            final Socket socket = serverSocket.accept();
            Thread t = new Thread(() -> {
                try {
                    server.serve(socket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            t.setName("Client[" + ID + "]@" + serverSocket.getInetAddress().getHostAddress() + ":" + server.port);
            t.start();
            ++ID;
        }
    }
}
