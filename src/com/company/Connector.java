package com.company;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Connector implements Closeable{
    private final Socket socket;
    private final BufferedInputStream input;
    private final BufferedOutputStream output;
    private final int LEN = 2566;

    public Connector(String ip, int port){
        try {
            this.socket = new Socket(ip, port);
            this.input = createInput();
            this.output = createOutput();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public Connector(ServerSocket server){
        try {
            this.socket = server.accept();
            this.input = createInput();
            this.output = createOutput();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private BufferedInputStream createInput() throws IOException {
        return new BufferedInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    private BufferedOutputStream createOutput() throws IOException {
        return new BufferedOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void writeLine(String message){
        try{
            output.write(Arrays.copyOf(message.getBytes(StandardCharsets.UTF_8), LEN));
            output.flush();
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String readLine(){
        try{
            String str = (new String(input.readNBytes(2566), StandardCharsets.UTF_8)).trim();
            return str.equals("") ? null : str;
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeFile(File file){
        try{
            BufferedInputStream inFromFile = new BufferedInputStream(new FileInputStream(file.getPath()));


            byte[] arr = new byte[2566];
            Map<byte[], Integer> fileContent = new HashMap<>();
            int lenContent = 0;
            int in = 0;
            while ((in = inFromFile.read(arr)) != -1){
                fileContent.put(arr, in);
                lenContent += in;
            }

            writeLine(file.getName());
            writeLine(String.valueOf(lenContent));

            for (Map.Entry<byte[], Integer> entry : fileContent.entrySet()) {
                output.write(entry.getKey(), 0, entry.getValue());
            }
            output.flush();
            inFromFile.close();
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public void readFile(String dir){
        try {

            String fileName = readLine();
            int lenContent = Integer.parseInt(readLine());
            byte[] fileContent = input.readNBytes(lenContent);

            BufferedOutputStream outToFile = new BufferedOutputStream(new FileOutputStream(dir + fileName));
            outToFile.write(fileContent);
            outToFile.flush();

            outToFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws IOException {
        input.close();
        output.close();
        socket.close();
    }
}
