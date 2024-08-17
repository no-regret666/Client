package com.noregret;

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

        try{
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            String filename = dis.readUTF();
            FileOutputStream fos = new FileOutputStream(filename);
            byte[] buffer = new byte[1024];
            int len;
            while((len = dis.read(buffer)) != -1){
                fos.write(buffer,0,len);
                fos.flush();
            }
            dis.close();
            fos.close();
            socket.close();

            System.out.println(Utils.getColoredString(33,1,"接收完成!"));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
