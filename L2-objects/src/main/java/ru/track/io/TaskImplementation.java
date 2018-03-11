package ru.track.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.track.io.vendor.Bootstrapper;
import ru.track.io.vendor.FileEncoder;

import java.io.*;
import java.net.InetSocketAddress;

public final class TaskImplementation implements FileEncoder {

    /**
     * @param finPath  where to read binary data from
     * @param foutPath where to write encoded data. if null, please create and use temporary file.
     * @return file to read encoded data from
     * @throws IOException is case of input/output errors
     */
    
    @NotNull
    public File encodeFile(@NotNull String finPath, @Nullable String foutPath) throws IOException {
        /* XXX: https://docs.oracle.com/javase/8/docs/api/java/io/File.html#deleteOnExit-- */

        File fin = new File(finPath);
        File fout;

        if (foutPath != null) {
            fout = new File(foutPath);
        } else {
            fout = File.createTempFile("useless", ".txt");
            fout.deleteOnExit();
        }

        try (
                InputStream in = new FileInputStream(fin);
                FileWriter writer = new FileWriter(fout)
        ) {
            int count = (int) fin.length() % 3;
            byte[] input = new byte[3];

            while (in.read(input, 0, 3) == 3) {
                writer.append(toBase64[input[0] >> 2 & 0b111111]);
                writer.append(toBase64[(input[0] << 4 | input[1] >> 4 & 0b1111) & 0b111111]);
                writer.append(toBase64[(input[1] << 2 | input[2] >> 6 & 0b11) & 0b111111]);
                writer.append(toBase64[input[2] & 0b111111]);
            }

            if (count == 1) {
                writer.append(toBase64[input[0] >> 2 & 0b111111]);
                writer.append(toBase64[input[0] << 4 & 0b111111]);
                writer.write("==");
            } else if (count == 2) {
                writer.append(toBase64[input[0] >> 2 & 0b111111]);
                writer.append(toBase64[(input[0] << 4 | input[1] >> 4 & 0b1111) & 0b111111]);
                writer.append(toBase64[input[1] << 2 & 0b111111]);
                writer.append('=');
            }

            return fout;
        }
    }

    private static final char[] toBase64 = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    public static void main(String[] args) throws Exception {
        final FileEncoder encoder = new TaskImplementation();
        // NOTE: open http://localhost:9000/ in your web browser
        (new Bootstrapper(args, encoder))
                .bootstrap("", new InetSocketAddress("127.0.0.1", 9000));
    }

}
