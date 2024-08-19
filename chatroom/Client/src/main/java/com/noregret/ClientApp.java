package com.noregret;

import java.net.UnknownHostException;
import java.util.Scanner;

public class ClientApp {
    public static void main(String[] args) throws UnknownHostException {
        String ip = Utils.getIP();
        Client client = new Client();
        client.init(ip, 8080);
    }
}