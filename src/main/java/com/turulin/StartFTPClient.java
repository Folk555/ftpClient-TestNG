package com.turulin;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class StartFTPClient {

    private static String file = "students.txt";
    private static Scanner scanner = new Scanner(System.in);
    private static UserFtpClient userFtpClient = null;
    private static String command;

    public static void main(String[] args) throws Exception {
        System.out.println("FTP клиент.");
        while (userFtpClient == null) {
            if (args.length == 3) {
                System.out.println("Попытка подключения");
                try {
                    userFtpClient = new UserFtpClient(args[0], args[1], args[2]);
                } catch (NotExpectedResponseStatusException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Повторите ввод: [ЛОГИН] [ПОРОЛЬ] [IP_FTP_СЕРВЕРА]");
                    args = scanner.nextLine().split(" ");
                } catch (UnknownHostException | SocketException e) {
                    System.out.println("Incorrect host address");
                    System.out.println("Повторите ввод: [ЛОГИН] [ПОРОЛЬ] [IP_FTP_СЕРВЕРА]");
                    args = scanner.nextLine().split(" ");
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                System.out.println("Повторите ввод: [ЛОГИН] [ПОРОЛЬ] [IP_FTP_СЕРВЕРА]");
                args = scanner.nextLine().split(" ");
            }
        }
        System.out.println("FTP сервер подключен.");

        System.out.println("Режим обмена с FTP сервером по умолчанию активный. Желаете изменить на пассивный? д/н");
        if ("д".equalsIgnoreCase(scanner.nextLine())) {
            userFtpClient.passiveMode();
            System.out.println("Выбран пассивный режим");
        } else {
            System.out.println("Выбран активный режим");
        }


        boolean idDownloadCorrectly = false;
        while (!idDownloadCorrectly)
            try {
                System.out.println("Укажите файл на FTP сервере: ");
                userFtpClient.downloadFile(scanner.nextLine());
                idDownloadCorrectly = true;
            } catch (NotExpectedResponseStatusException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        while (!Commands.quite.toString().equals(command)) {
            printMenu();
            command = scanner.nextLine();
            System.out.println();
            String[] commandWords = command.split(" ");
            Commands mainCmd = null;
            try {
                mainCmd = Commands.valueOf(commandWords[0]);
            } catch (Exception e) {
                System.out.println("Неизвестная команда: " + commandWords[0]);
                continue;
            }
            switch (mainCmd) {
                case stdall: {
                    userFtpClient.getCacheStudents().forEach((key, value) -> System.out.println(value));
                    break;
                }
                case getstd: {
                    if (commandWords.length < 2) {
                        System.out.println("Missing argument");
                        break;
                    }
                    try {
                        System.out.println(userFtpClient.getStudentById(Integer.valueOf(commandWords[1])));
                    } catch (NumberFormatException e) {
                        System.out.println("Error id format");
                    } catch (NullPointerException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                }
                case addstd: {
                    if (commandWords.length < 2) {
                        System.out.println("Missing argument");
                        break;
                    }
                    userFtpClient.addStudent(new Student(0, commandWords[1]));
                    break;
                }
                case rmstd: {
                    if (commandWords.length < 2) {
                        System.out.println("Missing argument");
                        break;
                    }
                    try {
                        userFtpClient.removeStudentById(Integer.valueOf(commandWords[1]));
                    } catch (NullPointerException e) {
                        System.out.println(e.getMessage());
                    } catch (NumberFormatException e) {
                        System.out.println("Error id format");
                    }
                    break;
                }
                case quite: {
                    try {
                        userFtpClient.updateFile();
                    } catch (NotExpectedResponseStatusException e) {
                        System.out.println(e.getMessage());
                    }
                    userFtpClient.close();
                    break;
                }
            }
        }
    }

    static void printMenu() {
        System.out.println();
        System.out.println("Введите желаемую команду");
        System.out.println(Commands.stdall + " - вывести список студентов по именам");
        System.out.println(Commands.getstd + " [id] - вывести информацию о студенте по его id");
        System.out.println(Commands.addstd + " [name] - добавление студента с именем name");
        System.out.println(Commands.rmstd + " [id] - удаление студента по id");
        System.out.println(Commands.quite + " - сохранить изменения на FTP сервер и завершить работу");
    }

    enum Commands {
        stdall,
        getstd,
        addstd,
        rmstd,
        quite
    }
}
