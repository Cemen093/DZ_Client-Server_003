package com.company;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;

public class Client {
    private static final String DIR = "file client/";
    public static final String EXIT = "exit";
    public static final String GET_LIST_FILES = "get a list of files";
    public static final String TRANSFER_FILE = "transfer file";
    public static final String GET_FILE_BY_NAME = "get file by name";

    public static void main(String[] args) {
//        Написать свой сервер для хранения файлов переданных от клиента.
//                Клиент может :
//        - Получить список файлов
//        - Передать файл
//        - Получить файл по имени

        try (Connector connector = new Connector("127.0.0.1",4000)){
            System.out.println("Connected to server");

            HashMap<String, Command> com = new HashMap<>(){
                @Override
                public Command get(Object key){
                    Command com = super.get(key);
                    return com != null ? com : new NotACommand();
                }
            };
            com.put(EXIT, new Exit());
            com.put(GET_LIST_FILES, new GetListFiles());
            com.put(TRANSFER_FILE, new TransferFile());
            com.put(GET_FILE_BY_NAME, new GetFileByName());

            Scanner scanner = new Scanner(System.in);
            while (true){
                System.out.println("\ncommand: \n"+EXIT+"\n"+GET_LIST_FILES+"\n"+TRANSFER_FILE+"\n"+GET_FILE_BY_NAME);
                System.out.println("input a command");
                System.out.print(">> ");
                com.get(scanner.nextLine().toLowerCase(Locale.ROOT)).execute(connector);
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    private static class GetListFiles implements Command{
        @Override
        public void execute(Connector connector){
            connector.writeLine("send list files");
            System.out.println("list file on server\n"+connector.readLine());
        }
    }

    private static class TransferFile implements Command{
        @Override
        public void execute(Connector connector){
            Scanner scanner = new Scanner(System.in);
            System.out.print("Input filename\n>> ");

            File file = new File(DIR + scanner.nextLine());
            if (file.isFile()) {
                connector.writeLine("get file");
                connector.writeFile(file);
            } else {
                System.out.println("file not fount");
            }
        }
    }

    private static class GetFileByName implements Command{
        @Override
        public void execute(Connector connector){
            Scanner scanner = new Scanner(System.in);
            System.out.print("input name file\n>> ");
            String fileName = scanner.nextLine();

            connector.writeLine("send file");
            connector.writeLine(fileName);
            connector.readFile(DIR);
        }
    }
}

