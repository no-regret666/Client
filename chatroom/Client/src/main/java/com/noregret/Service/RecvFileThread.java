package com.noregret.Service;

import com.noregret.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@Slf4j
public class RecvFileThread extends Thread {
    private final int fromPort;

    public RecvFileThread(int fromPort) {
        this.fromPort = fromPort;
    }

    @Override
    public void run() {
        Socket socket = new Socket();
        try {
            String ip = Utils.getIP();
            socket.connect(new InetSocketAddress(ip, fromPort));
        }catch (IOException e){
            log.error(e.getMessage());
        }

        byte[] buffer = new byte[1024 * 1024];
        long totalBytes = 0;
        long bytesReceived = 0;
        
        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String filename = dis.readUTF();
            FileOutputStream fos = new FileOutputStream(filename);
            while (true) {
                int len = dis.read(buffer);
                if (len == -1) {
                    break;
                }
                fos.write(buffer, 0, len);
                fos.flush();
                bytesReceived += len;
                totalBytes += len;

                // 可以在这里添加进度报告等功能
                System.out.println("Received " + bytesReceived + " of " + totalBytes + " bytes");
                dis.close();
                fos.close();
                socket.close();

                System.out.println(Utils.getColoredString(33,1,"接收完成!"));
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
