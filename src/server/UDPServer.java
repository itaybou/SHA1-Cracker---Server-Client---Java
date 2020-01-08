package server;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.*;
import protocol.Message;
import protocol.Protocol;

public class UDPServer implements Runnable {

    private final int PORT = 3117;
    private final int TIMEOUT = 500;
    private final String GENERAL_IP = "0.0.0.0";
    private final int MAX_CLIENTS = 10;

    private boolean running;
    private DatagramSocket socket;
    private Protocol protocol;
    private BlockingQueue<Message> messageToSend;

    private ScheduledExecutorService tpc;
    private Thread receiver, responder;

    public final static Object LOCK = new Object();

    public UDPServer() {
        this.messageToSend = new LinkedBlockingDeque<>();
        this.protocol = new Protocol();
        tpc = Executors.newScheduledThreadPool(MAX_CLIENTS);
        socket = null;
        running = false;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(PORT);
            socket.setBroadcast(true);
            running = true;
            startReceiver();
            startResponder();
            System.out.println("Started UDP server on port " + this.PORT + ".");
        } catch(SocketException e) {
            System.out.println("Server already active on this machine.");
        }
    }

    private void startReceiver() {
        this.receiver = new Thread(() -> {
            while(running) {
                byte[] buffer = new byte[protocol.getMessageSize()];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(packet);
                    final Future request = tpc.submit(new PacketHandler(packet, this));
                    tpc.schedule(() -> {
                        if(!request.isDone()) {
                            request.cancel(true);
                            System.out.println("Request timed out.");
                        }
                    }, TIMEOUT, TimeUnit.SECONDS);
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });
        receiver.start();
    }

    private void startResponder() {
        this.responder = new Thread(() -> {
            while(running) {
                try {
                    synchronized (LOCK) {
                        while (messageToSend.isEmpty()) LOCK.wait();
                    }
                    Message message = messageToSend.poll();
                    byte[] data = message.getData();
                    socket.send(new DatagramPacket(data, data.length, message.getAddress(), message.getPort()));
                } catch (IOException | InterruptedException  e) {
                    e.printStackTrace();
                }
            }
        });
        responder.start();
    }

    private void terminate() {
        if (!this.running) return;
        this.running = false;
        try {
            socket.close();
            receiver.join();
            responder.join();
            this.tpc.awaitTermination(60, TimeUnit.SECONDS);
            this.tpc.shutdownNow();
        } catch (final InterruptedException e) {
            this.tpc.shutdownNow();
        }
        System.out.println("Server terminated.");
    }

    public void addMessage(Message message) {
        this.messageToSend.add(message);
        synchronized (LOCK) {
            LOCK.notifyAll();
        }
    }

    public Protocol getProtocol() {
        return this.protocol;
    }
}
