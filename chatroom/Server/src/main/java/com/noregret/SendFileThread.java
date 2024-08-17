package com.noregret;

import com.noregret.Mapper.FileMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SendFileThread extends Thread {
    private final int toPort;
    private int fileID;
    private String filename;

    public SendFileThread(int fromPort,int fileID,String filename) {
        this.toPort = fromPort;
        this.fileID = fileID;
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(toPort);
            Socket socket = serverSocket.accept();

            FileInputStream fis = new FileInputStream("/home/noregret/chatroom_file/" + fileID);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());


            dos.writeUTF(filename);

            byte[] buffer = new byte[1024];
            int len;
            while((len = fis.read(buffer)) != -1){
                dos.write(buffer,0,len);
                dos.flush();
            }
            fis.close();
            dos.close();
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
