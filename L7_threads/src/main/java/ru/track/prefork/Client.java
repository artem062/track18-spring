package ru.track.prefork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

class ListenThread extends Thread {
    private Socket socket;
    private WriteThread writer;

    ListenThread (Socket socket, WriteThread writer){
        this.socket = socket;
        this.writer = writer;
    }

    @Override
    public void run() {
        try {
            final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());

            while(!isInterrupted()) {
                try {
                    Message mess = (Message) input.readObject();
                    System.out.println(mess.author + " > " + mess.data);
                } catch (SocketException | EOFException e) { break; }
            }
            System.out.println("DISCONNECTED");
            input.close();
            writer.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class WriteThread extends Thread {
    private Socket socket;
    private ListenThread listen;

    WriteThread (Socket socket){
        this.socket = socket;
    }

    public void setListen(ListenThread listen) {
        this.listen = listen;
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
                try {
                    out.writeObject(new Message(0, line, "me"));
                    out.flush();
                } catch (SocketException e) { break; }
            }
            try {
                out.close();
            } catch (SocketException e) {}
            listen.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        writer.start();
        listen.start();
        writer.setListen(listen);

        writer.join();
        listen.join();

        socket.close();
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client(9000, "localhost");
        client.loop();
    }
}
