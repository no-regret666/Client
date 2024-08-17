package com.noregret;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class SendFileThread extends Thread {
    private final int toPort;
    private final String ip;
    private final File file;
    public SendFileThread(int toPort, String ip, File file) {
        this.toPort = toPort;
        this.ip = ip;
        this.file = file;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, toPort));
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        String filename = file.getName();
        try (FileInputStream fis = new FileInputStream(file);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())){

            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, len);
                dos.flush();
            }

            socket.close();

            System.out.println(Utils.getColoredString(33,1,filename + " 发送完毕!"));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
