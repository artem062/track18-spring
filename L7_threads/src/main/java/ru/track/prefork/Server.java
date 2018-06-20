package ru.track.prefork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;


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
                    Date date = new Date();
                    Message message = new Message(date.getTime(), mess.data, this.getName());
                    sender.send(message);
                } catch (SocketException | EOFException e) { break; }
            }
            disconnect();
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
        log.info("disconnected");
        sender.disconnected(this);
        try {
            out.close();
        } catch (IOException e) {}
        try {
            socket.close();
        } catch (IOException e) {}
    }
}

class SenderThread extends Thread {
    private ArrayList<ServerThread> threads = new ArrayList<>();
    private DAO dao;

    SenderThread (DAO dao) {
        this.dao = dao;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (!isInterrupted()) {
            String line = scanner.nextLine();

            if (line.equals("list")) {
                System.out.println("List of clients:");
                for (ServerThread thread : threads)
                    System.out.println("\t" + thread.getName());
            } else if (line.length() > 11 && line.substring(0, 11).equals("getHistory ")) {
                long time = new Date().getTime();
                long minute = 60000;
                int count = Integer.parseInt(line.substring(11));
                List<Message> history = dao.getHistory(time - minute * count, time, 20);
                for (Message mes : history) {
                    System.out.println("[" + new Date(mes.ts) + "] " + mes.author + " > " + mes.data);
                }
            } else if (line.length() > 12 && line.substring(0, 12).equals("userHistory ")) {
                List<Message> history = dao.getByUser(line.substring(12), 10);
                for (Message mes : history) {
                    System.out.println("[" + new Date(mes.ts).toString() + "] " + mes.author + " > " + mes.data);
                }
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
        long id = dao.store(message);
        System.out.println("(" + id + ") " + message.author + " > " + message.data);
        String author = "Client" + message.author.substring(message.author.indexOf("@"));
        Message updatedMessage = new Message(message.ts, message.data, author);
        for (ServerThread thread : threads) {
            if (!thread.getName().equals(message.author)) {
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
        DAO dao = new DAO();
        SenderThread sender = new SenderThread(dao);
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
