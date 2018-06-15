package ru.track.prefork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

class Message implements Serializable{
    private long ts;
    private String data;
    private String author;
    private boolean connected;

    public Message(long ts, String data, String author, boolean con) {
        this.ts = ts;
        this.data = data;
        this.author = author;
        connected = con;
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

    public boolean isConnected() {
        return connected;
    }
}

class ServerThread extends Thread {
    private Logger log;
    private Socket socket;
    private SenderThread sender;
    private final ObjectOutputStream out;

    ServerThread(Socket socket, Logger log, SenderThread sender) throws IOException {
        this.socket = socket;
        this.log = log;
        this.sender = sender;
        this.out = new ObjectOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        log.info("connected");
        String client = this.getName();
        try {
            ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
            while (!isInterrupted()) {
                try {
                    Message mess = (Message) inputStream.readObject();
                    if (!mess.isConnected()) {
//                        disconnect();
                        break;
                    }
                    Date date = new Date();
                    Message message = new Message(date.getTime(), mess.getData(), this.getName(), true);
                    sender.send(message);
                    System.out.println(client + " > " + mess.getData());
                } catch (EOFException e) { break; }
            }
            log.info("disconnected");
            sender.disconnected(this);
            try {
                out.close();
            } catch (IOException e) {}
            try {
                socket.close();
            } catch (IOException e) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            out.writeObject(new Message(0, "", "", false));
            out.flush();
        } catch (IOException e) {
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
            } else if (line.length() > 5 && line.substring(0, 5).equals("drop ")) {
                String ID = "[" + line.substring(5);
                for (ServerThread thread : threads) {
                    if (ID.equals(thread.getName().substring(thread.getName().indexOf("["), thread.getName().indexOf("]")))) {
                        thread.disconnect();
                        break;
                    }
                }
            }
        }
    }

    public void send(Message message) {
        String author = "Client" + message.getAuthor().substring(message.getAuthor().indexOf("@"));
        Message updatedMessage = new Message(message.getTs(), message.getData(), author, true);
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
