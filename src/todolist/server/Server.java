package todolist.server;


import todolist.command.CommandCreator;
import todolist.command.CommandExecutor;
import todolist.database.Database;
import todolist.exceptions.InvalidParametersException;
import todolist.exceptions.TaskAlreadyExistsException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;


public class Server {
    private static final int BUFFER_SIZE = 1024;
    private static final String HOST = "localhost";

    private final CommandExecutor commandExecutor;

    private static final String FILE_STACK_TRACES = "stacktrace2.txt";

    private static final File FILE = new File(FILE_STACK_TRACES);

    private final int port;
    private boolean isServerWorking;

    private ByteBuffer buffer;
    private Selector selector;

    public Server(int port, CommandExecutor commandExecutor) {
        this.port = port;
        this.commandExecutor = commandExecutor;
    }

    public void start() throws TaskAlreadyExistsException, InvalidParametersException {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {

            selector = Selector.open();
            configureServerSocketChannel(serverSocketChannel, selector);
            this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
            isServerWorking = true;
            while (isServerWorking) {

                try {


                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }



                    Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
                    while (keyIterator.hasNext()) {

                        SelectionKey key = keyIterator.next();
                        if (key.isReadable()) {
                            SocketChannel clientChannel = (SocketChannel) key.channel();
                            String clientInput = getClientInput(clientChannel);
                            System.out.println(clientInput);
                            if (clientInput == null) {
                                continue;
                            }
                            
                            String output = commandExecutor
                                    .execute(clientChannel, CommandCreator.newCommand(clientInput));

                            writeClientOutput(clientChannel, output);

                        } else if (key.isAcceptable()) {
                            accept(selector, key);
                        }

                        keyIterator.remove();
                    }
                } catch (IOException e) {

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

                    System.out.println("Error occurred while processing client request: " + e.getMessage());
                }
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException("failed to start server", e);
        }
    }

    public void stop() {
        this.isServerWorking = false;
        if (selector.isOpen()) {
            selector.wakeup();
        }
    }

    private void configureServerSocketChannel(ServerSocketChannel channel, Selector selector) throws IOException {
        channel.bind(new InetSocketAddress(HOST, this.port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    private String getClientInput(SocketChannel clientChannel) throws IOException {
        buffer.clear();

        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }

        buffer.flip();

        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);

        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void writeClientOutput(SocketChannel clientChannel, String output) throws IOException {
        buffer.clear();
        buffer.put(output.getBytes());
        buffer.flip();
        clientChannel.write(buffer);
    }

    private void accept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel sockChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientSocket = sockChannel.accept();
        clientSocket.configureBlocking(false);

        clientSocket.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) throws TaskAlreadyExistsException, InvalidParametersException {
        final int port = 7769;
        Database database = Database.getInstance();
        Server server = new Server(port, new CommandExecutor(database));
        server.start();

    }
}