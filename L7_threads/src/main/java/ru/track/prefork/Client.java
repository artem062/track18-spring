package ru.track.prefork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

class ListenThread extends Thread {
    private Socket socket;
    private WriteThread writer;
    private boolean isWork;

    ListenThread (Socket socket, WriteThread writer){
        this.socket = socket;
        this.writer = writer;
        isWork = true;
    }

    @Override
    public void run() {
        try {
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            while(!isInterrupted()) {
                try {
                    Message mess = (Message) input.readObject();
                    if (!mess.isConnected()) {
                        writer.interrupt();
                        break;
                    }
                    System.out.println(mess.getAuthor() + " > " + mess.getData());
                } catch (EOFException e) {}
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void off() {
        isWork = false;
    }
}

class WriteThread extends Thread {
    private Socket socket;
    private ListenThread listener;
    private boolean isWork;

    WriteThread (Socket socket){
        this.socket = socket;
        isWork = true;
    }

    @Override
    public void run() {
        try {
            final ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            Scanner scanner = new Scanner(System.in);

            while (!isInterrupted()) {
                String line = scanner.nextLine();
                if (line.equals("exit")) {
                    break;
                }
                out.writeObject(new Message(0, line, "me", true));
                out.flush();
            }
            out.writeObject(new Message(0, "", "", false));
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setListener(ListenThread listener) {
        this.listener = listener;
    }
}

public class Client {
    static Logger log = LoggerFactory.getLogger(Client.class);

    private int port;
    private String host;

    public Client(int port, String host) {
        this.port = port;
        this.host = host;
    }

    public void loop() throws Exception {
        Socket socket = new Socket(host, port);

        WriteThread writer = new WriteThread(socket);
        ListenThread listen = new ListenThread(socket, writer);

        listen.start();
        writer.start();
        writer.setListener(listen);

        writer.join();
        listen.join();

        socket.close();
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client(9000, "localhost");
        client.loop();
    }
}
