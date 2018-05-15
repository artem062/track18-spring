package ru.track.prefork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

class ListenThread extends Thread {
    private Socket socket;

    ListenThread (Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            final InputStream inputStream = socket.getInputStream();

            byte[] buf = new byte[1024];

            while(!isInterrupted()) {
                try {
                    int nRead = inputStream.read(buf);
                    System.out.println(new String(buf, 0, nRead));
                } catch (Exception e) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class WriteThread extends Thread {
    private Socket socket;

    WriteThread (Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            final OutputStream out = socket.getOutputStream();
            Scanner scanner = new Scanner(System.in);

            while (!isInterrupted()) {
                String line = scanner.nextLine();
                if (line.equals("exit")) {
                    break;
                }

                out.write((line).getBytes());
                out.flush();
            }
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

        Thread listen = new ListenThread(socket);
        Thread write = new WriteThread(socket);

        listen.start();
        write.start();

        write.join();

        socket.close();
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client(9000, "localhost");
        client.loop();
    }
}
