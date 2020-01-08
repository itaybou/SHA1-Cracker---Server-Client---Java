package client;

import utils.HashCracker;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

public class ClientRunner {

    public static void main(String[] args) {
        UDPClient client = new UDPClient();
        client.run();
    }
}
