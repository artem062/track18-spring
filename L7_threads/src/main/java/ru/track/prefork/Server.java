package ru.track.prefork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


/**
 * - multithreaded +
 * - atomic counter +
 * - setName() +
 * - thread -> Worker +
 * - save threads
 * - broadcast (fail-safe)
 */
class ServerThread extends Thread {
    private Logger log;
    private Socket socket;
    private SenderThread sender;

    ServerThread(Socket socket, Logger log, SenderThread sender){
        this.socket = socket;
        this.log = log;
        this.sender = sender;
    }

    @Override
    public void run() {
        log.info("connected");
        String client = Thread.currentThread().getName();
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] buf = new byte[1024];
            while (true) {
                try {
                    int nRead = inputStream.read(buf);
                    String line = new String(buf, 0, nRead);
                    sender.send(this.getName(), line);
                    System.out.println(client + " > " + line);
                } catch (Exception e) {
                    log.info("disconnected");
                    socket.close();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(String message) {
        try {
            final OutputStream out = socket.getOutputStream();
            out.write(message.getBytes());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class SenderThread extends Thread {
    private ArrayList<ServerThread> threads = new ArrayList<>();

    public void send(String author, String line) {
        String message = "Client" + author.substring(author.indexOf("@")) + " > " + line;
        for (ServerThread thread : threads) {
            if (!thread.getName().equals(author)) {
                thread.send(message);
            }
        }
    }

    public void update(ServerThread t) {
        threads.add(t);
    }
}

public class Server {
    private static Logger log = LoggerFactory.getLogger(Server.class);

    private int port;
    public Server(int port) {
        this.port = port;
    }

    public void serve() throws Exception {
        ServerSocket serverSocket = new ServerSocket(port, 10, InetAddress.getByName("localhost"));
        SenderThread sender = new SenderThread();
        sender.start();

        for (int ID = 1; true; ++ID) {
            final Socket socket = serverSocket.accept();
            ServerThread t = new ServerThread(socket, log, sender);
            t.setName("Client[" + ID + "]@" + serverSocket.getInetAddress().getHostAddress() + ":" + port);
            sender.update(t);
            t.start();
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server(9000);
        server.serve();
    }
}
