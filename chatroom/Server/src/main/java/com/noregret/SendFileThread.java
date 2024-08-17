package com.noregret;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public class SendFileThread extends Thread {
    private final int toPort;
    private final int fileID;
    private final String filename;

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
            log.error(e.getMessage());
        }
    }
}
