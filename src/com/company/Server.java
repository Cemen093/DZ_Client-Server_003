package com.company;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class Server {
    private static final String DIR = "file server/";
    public static final String EXIT = "exit";
    public static final String SEND_LIST_FILES = "send list files";
    public static final String GET_FILE = "get file";
    public static final String SEND_FILE_BY_NAME = "send file";
    public static void main(String[] args) throws IOException, InterruptedException {
        try (ServerSocket server = new ServerSocket(4000)){
            System.out.println("Server Started");

            while(true){
                try (Connector connector = new Connector(server)){
                    System.out.println("client connected");
                    HashMap<String, Command> com = new HashMap<>(){
                        @Override
                        public Command get(Object key){
                            Command com = super.get(key);
                            return com != null ? com : new NotACommand();
                        }
                    };
                    com.put(EXIT, new Exit());
                    com.put(SEND_LIST_FILES, new SendListFiles());
                    com.put(GET_FILE, new getFile());
                    com.put(SEND_FILE_BY_NAME, new SendFileByName());

                    while (true) {
                        String command = connector.readLine();
                        if (command != null) {
                            System.out.println("got command: " + command);
                            com.get(command.toLowerCase(Locale.ROOT)).execute(connector);
                        }
                        Thread.sleep(1000);
                    }

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class NotACommand implements Command {
        @Override
        public void execute(Connector connector) {
            System.out.println("Not a command");
        }
    }

    private static class Exit implements Command{
        @Override
        public void execute(Connector connector){
            System.out.println("By");
            try {
                connector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }

    private static class SendListFiles implements Command {
        @Override
        public void execute(Connector connector) {
            StringBuilder list = new StringBuilder();
                    File dir = new File(DIR);
            File[] arrFiles = dir.listFiles();
            if (arrFiles != null) {
                for (File f : arrFiles) {
                    list.append(f.getName()).append("\n");
                }
            }
            connector.writeLine(list.toString());
        }
    }

    private static class getFile implements Command {
        @Override
        public void execute(Connector connector) {
            connector.readFile(DIR);
        }
    }

    private static class SendFileByName implements Command {
        @Override
        public void execute(Connector connector) {
            String fileName = connector.readLine();
            connector.writeFile(new File(DIR + fileName));
        }
    }
}
