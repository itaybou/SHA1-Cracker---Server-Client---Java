package client;

import java.io.IOException;
import java.net.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import protocol.Protocol;
import protocol.Message;

public class UDPClient implements Runnable {

    private final int SERVER_PORT = 3117;
    private final int GATHER_WAIT_TIME = 1;
    private final int TIMEOUT = 50;
    private final String BROADCAST_IP = "255.255.255.255";

    private DatagramSocket socket;
    private Protocol protocol;
    private PacketHandler handler;
    private Thread serverListener, serverResponder;
    private BlockingQueue<Message> serverResponses;
    private List<Message> activeServers;
    private boolean running;

    public final static Object LOCK = new Object();

    public UDPClient() {
        this.protocol = new Protocol();
        this.serverResponses = new LinkedBlockingDeque<>();
        this.activeServers = new LinkedList<>();
        this.handler = new PacketHandler(this.protocol, this);
    }


    private void start() {
        try {
            this.socket = new DatagramSocket();
            socket.setBroadcast(true);
        } catch(SocketException e) {
            System.out.println("Error initiating client.");
        }
    }

    public void startServerListener() {
        this.serverListener = new Thread(() -> {
            while(running) {
                byte[] buffer = new byte[protocol.getMessageSize()];
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    //socket.setSoTimeout(TIMEOUT*1000);
                    socket.receive(packet);
                    serverResponses.add(new Message(packet.getAddress(), packet.getPort(), packet.getData()));
                } catch (SocketTimeoutException e) {
                    if(Thread.currentThread().isInterrupted()) {
                        System.out.println("Server listener terminated!");
                    } else {
                        System.out.println("Receive timed out.");
                        Message message = removeServer();
                        message.setIgnore();
                        if (activeServers.size() == 0) {
                            System.out.println("No result in time.");
                            ((LinkedBlockingDeque) serverResponses).offerLast(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        serverListener.start();
    }

    public void startServerResponder() {
        this.serverResponder = new Thread(() -> {
            while (running) {
                try {
                    Message message = serverResponses.take();
                    if(!message.isIgnore()) {
                        this.handler.parseMessage(message);
                    }
                } catch (InterruptedException e) {
                   System.out.println("Server responder terminated!");
                }
            }
        });
        serverResponder.start();
    }

    public void broadcastRequest(IOHandler IOHandler) {
        try {
            final InetAddress BROADCAST_ADDRESS = InetAddress.getByName(BROADCAST_IP);
            byte[] discoverMessage = protocol.createMessage(IOHandler.getType(), IOHandler.getHash(), IOHandler.getOriginalRangeStart(), IOHandler.getOriginalRangeEnd(), IOHandler.getOriginalLength());
            socket.send(new DatagramPacket(discoverMessage, discoverMessage.length, BROADCAST_ADDRESS, SERVER_PORT));
            synchronized (this) {
                this.wait(GATHER_WAIT_TIME*1000);
            }
        } catch(IOException | InterruptedException e) {
            System.out.println("Error sending broadcast to servers.");
        }
    }

    private boolean sendRequestRanges(IOHandler ioHandler) throws IOException {
        Deque<Range> ranges = handler.divideEqualStringRanges(ioHandler.getOriginalLength(), activeServers.size());
        if(ranges != null) {
            for (Message serverDetails : activeServers) {
                Range currRange = ranges.poll();
                ioHandler.setRanges(currRange.from, currRange.to);
                byte[] message = protocol.createMessage(ioHandler.getType(), ioHandler.getHash(), ioHandler.getOriginalRangeStart(), ioHandler.getOriginalRangeEnd(), ioHandler.getOriginalLength());
                socket.send(new DatagramPacket(message, message.length, serverDetails.getAddress(), serverDetails.getPort()));
            }
        }
        return ranges != null;
    }

    private void terminate() {
        try {
            synchronized (LOCK) {
                while (!activeServers.isEmpty()) LOCK.wait();
            }
            this.running = false;
            serverResponder.interrupt();
            serverListener.interrupt();
            socket.close();
            System.out.println("Client terminated!");
        } catch(InterruptedException e) {
            System.out.println("Client failed to terminate.");
        }
    }

    @Override
    public void run() {
        start();
        boolean validInput;
        do {
            IOHandler request = new IOHandler(protocol.getTeamName(), protocol);
            validInput = request.getUserInput();
            if(validInput) {
                this.running = true;
                startServerListener();
                startServerResponder();
                broadcastRequest(request);
                request.setRequest();
                try {
                    if (!sendRequestRanges(request)) {
                        System.out.println("No server responded to message.");
                    }
                } catch (IOException e) {
                    System.out.println("Error while sending.");
                    terminate();
                }
            } else System.out.println("Invalid input provided!");
        } while (!validInput);
        //terminate();
    }

    public void addActiveServer(Message message) {
        this.activeServers.add(message);
    }

    public Message removeServer() {
        Message message = this.activeServers.remove(0);
        synchronized (LOCK) {
            LOCK.notify();
        }
        return message;
    }

    public void removeAllServers() {
        this.activeServers.clear();
        synchronized (LOCK) {
            LOCK.notify();
        }
    }
}
