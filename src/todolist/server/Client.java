package todolist.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

// NIO, blocking
public class Client {

    private static final int SERVER_PORT = 7769;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 512;

    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    private static final String FILE_STACK_TRACES = "stacktrace1.txt";

    private static final File FILE = new File(FILE_STACK_TRACES);


    public static void main(String[] args) throws IOException {

        try (SocketChannel socketChannel = SocketChannel.open();
             Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");

            while (true) {
                System.out.print("Enter message: ");
                String message = scanner.nextLine(); // read a line from the console

                if ("quit".equals(message)) {
                    break;
                }

                System.out.println("Sending message <" + message + "> to the server...");

                buffer.clear(); // switch to writing mode
                buffer.put(message.getBytes()); // buffer fill
                buffer.flip(); // switch to reading mode
                socketChannel.write(buffer); // buffer drain

                buffer.clear(); // switch to writing mode
                socketChannel.read(buffer); // buffer fill
                buffer.flip(); // switch to reading mode

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, "UTF-8"); // buffer drain

                // if buffer is a non-direct one, is has a wrapped array and we can get it
                //String reply = new String(buffer.array(), 0, buffer.position(), "UTF-8"); // buffer drain

                System.out.println("The server replied <" + reply + ">");
            }

        }
        catch (IOException e) {

            FILE.createNewFile();

            try (FileWriter fr = new FileWriter(FILE, true)) {
                try (BufferedWriter br = new BufferedWriter(fr)) {
                    br.write(e.getStackTrace().toString());
                    br.write(System.lineSeparator());
                }
                catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }
}