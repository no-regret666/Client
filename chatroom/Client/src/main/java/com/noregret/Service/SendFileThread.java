package com.noregret.Service;

import com.noregret.Utils;
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
        byte[] buffer = new byte[1024 * 1024];
        int len;
        long totalBytes = file.length();
        long bytesSent = 0;
        try (FileInputStream fis = new FileInputStream(file);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream())){

            while (bytesSent < totalBytes) {
            int len = fis.read(buffer);
            dos.write(buffer, 0, len);
            dos.flush();
            bytesSent += len;

            // 可以在这里添加进度报告等功能
            System.out.println("Sent " + bytesSent + " of " + totalBytes + " bytes");
                
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
