package ru.track.prefork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;


/**
 * - multithreaded +
 * - atomic counter +
 * - setName() +
 * - thread -> Worker +
 * - save threads
 * - broadcast (fail-safe)
 */

class Message {
    private long ts;
    private String data;
    private String author;

    public Message(long ts, String data, String author) {
        this.ts = ts;
        this.data = data;
        this.author = author;
    }

    public String getData() {
        return data;
    }

    public String getAuthor() {
        return author;
    }

    public long getTs() {
        return ts;
    }
}

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
                    Date date = new Date();
                    String line = new String(buf, 0, nRead);
                    Message message = new Message(date.getTime(), line, this.getName());
                    sender.send(message);
                    System.out.println(client + " > " + line);
                } catch (Exception e) {
                    log.info("disconnected");
                    sender.disconnected(this);
                    socket.close();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Message message) {
        try {
            final OutputStream out = socket.getOutputStream();
            out.write((message.getAuthor() + " > " + message.getData()).getBytes());
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class SenderThread extends Thread {
    private ArrayList<ServerThread> threads = new ArrayList<>();

    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (!isInterrupted()) {
            String line = scanner.nextLine();

            if (line.equals("list")) {
                System.out.println("List of clients:");
                for (ServerThread thread : threads)
                    System.out.println("\t" + thread.getName());
            }
        }
    }

    public void send(Message message) {
        String author = "Client" + message.getAuthor().substring(message.getAuthor().indexOf("@"));
        Message updatedMessage = new Message(message.getTs(), message.getData(), author);
        for (ServerThread thread : threads) {
            if (!thread.getName().equals(message.getAuthor())) {
                thread.send(updatedMessage);
            }
        }
    }

    public void update(ServerThread t) {
        threads.add(t);
    }

    public void disconnected(ServerThread t) {
        threads.remove(t);
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
