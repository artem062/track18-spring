package ru.track.prefork;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

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
        final OutputStream out = socket.getOutputStream();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String line = scanner.nextLine();
            if ("exit".equals(line)) {
                break;
            }

            out.write((line + "\n").getBytes());
            out.flush();

            InputStream inputStream = socket.getInputStream();

            byte[] buf = new byte[1024];
            int nRead = inputStream.read(buf);

            System.out.print(new String(buf, 0, nRead));
        }
        socket.close();
    }

    public static void main(String[] args) throws Exception {
        Client client = new Client(9000, "localhost");
        client.loop();
    }
}
