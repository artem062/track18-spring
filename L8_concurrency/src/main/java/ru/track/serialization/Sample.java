package ru.track.serialization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 *
 */
public class Sample {

    static class Hello extends HelloParent implements Serializable {
        String text = "deadbeaf";
        String hello = "hello";

        @Override
        public String toString() {
            return "Hello{" +
                    "text='" + text + '\'' +
                    ", hello='" + hello + '\'' +
                    '}';
        }
    }

    static class HelloParent {

    }

    //ru.track.serialization.Sample$Hello���dX}�LtexttLjava/lang/String;xpdeadbeaf

    public static void main(String[] args) throws Exception {

        Hello hello = new Hello();

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStream outStream = new FileOutputStream("/Users/dmirty/Dima/track18-spring/data.bin");
        ObjectOutputStream objOut = new ObjectOutputStream(outStream);
        objOut.writeObject(hello);


        InputStream inputStream = new FileInputStream("/Users/dmirty/Dima/track18-spring/data.bin");
        ObjectInputStream objIn = new ObjectInputStream(inputStream);
        Object o = objIn.readObject();
        System.out.println(o.getClass().getSimpleName());

    }

}
