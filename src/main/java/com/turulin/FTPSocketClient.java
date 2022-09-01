package com.turulin;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

public class FTPSocketClient {

    private static final int BUFFER_SIZE = 4096;
    private boolean IS_PASV = false;
    private final Socket socketCmd;
    private final PrintWriter printWriterToSocketCmd;
    private final BufferedReader bufferedReaderFromSocketCmd;
    private Socket socketData;
    private ServerSocket clientServerSocketForActiveModeFTP;
    private OutputStream socketDataOutputStream;
    private InputStream inputStreamFromSocketData;
    private String tempStudentFile = null;
    private String originalNameOfTempStudentFile = null;
    //Стек для логов сообщений от сервера
    private Deque<String> logs = new ArrayDeque<>();

    public FTPSocketClient(String host, String username, String userPassword) throws Exception {
        socketCmd = new Socket(host, 21);
        bufferedReaderFromSocketCmd = new BufferedReader(new InputStreamReader(socketCmd.getInputStream()));
        printWriterToSocketCmd = new PrintWriter(socketCmd.getOutputStream());
        do
            logs.add(bufferedReaderFromSocketCmd.readLine());
        while (bufferedReaderFromSocketCmd.ready());
        if (!logs.getLast().contains("220"))
            throw new NotExpectedResponseStatusException("FTP error connection status. Expected response status: 220, actual: " + logs.getLast());
        sendCommandToFTP("USER " + username + "\n");
        if (!logs.getLast().contains("331"))
            throw new NotExpectedResponseStatusException("FTP error user. Expected response status: 331, actual: " + logs.getLast());
        sendCommandToFTP("PASS " + userPassword + "\n");
        if (!logs.getLast().contains("230"))
            throw new NotExpectedResponseStatusException("FTP error password. Expected response status: 230, actual: " + logs.getLast());
    }

    /**
     * This method activate data transmit by active mode.
     * In active mode FTPServer will be tried to connect to client data socket.
     *
     * @return ClientServerSocket to which FTP server will try to connect.
     * @throws Exception
     */
    private ServerSocket ftpCmdPort() throws Exception {
        ServerSocket serverSocket = new ServerSocket(0, 1, socketCmd.getLocalAddress()); //0 to automatically allocate
        String addrAndPort = socketCmd.getLocalAddress().getHostAddress().replace(".", ",") + "," +
                serverSocket.getLocalPort() / 256 + "," + serverSocket.getLocalPort() % 256;
        sendCommandToFTP("PORT " + addrAndPort + "\n");
        if (!logs.getLast().contains("200"))
            throw new NotExpectedResponseStatusException("FTP error active mode. Expected response status: 200, actual " + logs.getLast());
        return serverSocket;
    }

    private String ftpCmdPasv() throws Exception {
        sendCommandToFTP("PASV\n");
        if (!logs.getLast().contains("227"))
            throw new NotExpectedResponseStatusException("FTP error passive mode. Expected response status: 227, actual " + logs.getLast());
        String[] ipAndNonNormalizePort = logs.getLast().substring(logs.getLast().indexOf("(") + 1, logs.getLast().indexOf(")")).split(",");

        return ipAndNonNormalizePort[0] + "." + ipAndNonNormalizePort[1] + "." + ipAndNonNormalizePort[2] + "." + ipAndNonNormalizePort[3]
                + ":" + (Integer.valueOf(ipAndNonNormalizePort[4]) * 256 + Integer.valueOf(ipAndNonNormalizePort[5]));

    }

    public String downloadFileIntoTempFile(String remoteFile) throws Exception {
        originalNameOfTempStudentFile = Paths.get(remoteFile).getFileName().toString();
        clientServerSocketForActiveModeFTP = null;
        if (IS_PASV) {
            String ipAndPortOfFtpData = ftpCmdPasv();
            String host = ipAndPortOfFtpData.substring(0, ipAndPortOfFtpData.indexOf(":"));
            String port = ipAndPortOfFtpData.substring(ipAndPortOfFtpData.indexOf(":") + 1);
            socketData = new Socket(host, Integer.valueOf(port));
            inputStreamFromSocketData = socketData.getInputStream();
            sendCommandToFTP("RETR " + remoteFile + "\n");
            if ((!logs.getLast().contains("150")) && (!logs.getLast().contains("226")))
                throw new NotExpectedResponseStatusException("FTP error download file. Expected response status: 150 or 226, actual " + logs.getLast());
        } else {
            clientServerSocketForActiveModeFTP = ftpCmdPort();
            sendCommandToFTP("RETR " + remoteFile + "\n");
            if ((!logs.getLast().contains("150")) && (!logs.getLast().contains("226")))
                throw new NotExpectedResponseStatusException("FTP error download file. Expected response status: 150 or 226, actual " + logs.getLast());
            socketData = clientServerSocketForActiveModeFTP.accept();
            inputStreamFromSocketData = socketData.getInputStream();
        }

        if (tempStudentFile != null)
            Files.deleteIfExists(Paths.get(tempStudentFile));
        tempStudentFile = Files.createTempFile("tempStudentFile", null).toString();
        OutputStream outputStreamToTempFile = Files.newOutputStream(Paths.get(tempStudentFile));
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStreamFromSocketData.read(buffer, 0, BUFFER_SIZE)) != -1) {
            outputStreamToTempFile.write(buffer, 0, bytesRead);
        }
        outputStreamToTempFile.close();
        Thread.sleep(10);
        while (bufferedReaderFromSocketCmd.ready())
            logs.add(bufferedReaderFromSocketCmd.readLine());
        inputStreamFromSocketData.close();
        socketData.close();
        if (clientServerSocketForActiveModeFTP != null)
            clientServerSocketForActiveModeFTP.close();
        return tempStudentFile;
    }

    private void uploadFile(String localeFile) throws Exception {
        clientServerSocketForActiveModeFTP = null;
        if (IS_PASV) {
            String ipAndPortOfFtpData = ftpCmdPasv();
            String host = ipAndPortOfFtpData.substring(0, ipAndPortOfFtpData.indexOf(":"));
            String port = ipAndPortOfFtpData.substring(ipAndPortOfFtpData.indexOf(":") + 1);
            socketData = new Socket(host, Integer.valueOf(port));
            socketDataOutputStream = socketData.getOutputStream();
            sendCommandToFTP("STOR " + originalNameOfTempStudentFile + "\n");
            if ((!logs.getLast().contains("150")) && (!logs.getLast().contains("226")))
                throw new NotExpectedResponseStatusException("FTP error upload file. Expected response status: 150 or 226, actual " + logs.getLast());
        } else {
            clientServerSocketForActiveModeFTP = ftpCmdPort();
            sendCommandToFTP("STOR " + originalNameOfTempStudentFile + "\n");
            if ((!logs.getLast().contains("150")) && (!logs.getLast().contains("226")))
                throw new NotExpectedResponseStatusException("FTP error upload file. Expected response status: 150 or 226, actual " + logs.getLast());
            socketData = clientServerSocketForActiveModeFTP.accept();
            socketDataOutputStream = socketData.getOutputStream();
        }

        InputStream inputStreamFile = new FileInputStream(localeFile);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStreamFile.read(buffer, 0, BUFFER_SIZE)) != -1) {
            socketDataOutputStream.write(buffer, 0, bytesRead);
        }
        inputStreamFile.close();
        socketData.close();
        if (clientServerSocketForActiveModeFTP != null)
            clientServerSocketForActiveModeFTP.close();
        Thread.sleep(10);
        while (bufferedReaderFromSocketCmd.ready())
            logs.add(bufferedReaderFromSocketCmd.readLine());
    }

    public void updateRemoteFileByTempFile() throws Exception {
        if (tempStudentFile == null)
            throw new FileNotFoundException("Temp file not exist.");
        uploadFile(getTempStudentFile());
    }

    public void close() throws Exception {
        if (tempStudentFile != null) Files.deleteIfExists(Paths.get(tempStudentFile));
        if ((bufferedReaderFromSocketCmd != null) && ((printWriterToSocketCmd != null))) {
            sendCommandToFTP("QUIT\n");
            if (!logs.getLast().contains("221")) {
                if (bufferedReaderFromSocketCmd != null)
                    bufferedReaderFromSocketCmd.close();
                if (printWriterToSocketCmd != null)
                    printWriterToSocketCmd.close();
                throw new NotExpectedResponseStatusException("FTP error log out. Expected response status: 150 or 226, actual " + logs.getLast());
            }
        }
        if (inputStreamFromSocketData != null)
            inputStreamFromSocketData.close();
        if (socketDataOutputStream != null)
            socketDataOutputStream.close();
        if (socketCmd != null)
            socketCmd.close();
        if (clientServerSocketForActiveModeFTP != null)
            clientServerSocketForActiveModeFTP.close();
        //logs.forEach(System.out::println);
    }

    public String getTempStudentFile() {
        return tempStudentFile;
    }

    private void sendCommandToFTP(String command) throws IOException, InterruptedException {
        printWriterToSocketCmd.write(command);
        printWriterToSocketCmd.flush();
        do {
            Thread.sleep(10);
            logs.add(bufferedReaderFromSocketCmd.readLine());
        } while (bufferedReaderFromSocketCmd.ready());
    }

    public boolean activePASVMode() {
        IS_PASV = true;
        return IS_PASV;
    }

    public boolean activeActiveMode() {
        IS_PASV = false;
        return !IS_PASV;
    }
}
