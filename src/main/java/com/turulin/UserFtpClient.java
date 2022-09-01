package com.turulin;


import java.util.HashMap;

public class UserFtpClient {

    private StudentJSONFileEditor studentJSONFile;
    private FTPSocketClient ftp = null;


    public UserFtpClient(String user, String password, String ipAddress) throws Exception {
        ftp = new FTPSocketClient(ipAddress, user, password);
    }

    public void passiveMode() {
        ftp.activePASVMode();
    }

    public String downloadFile(String remoteFile) throws Exception {
        String localFile = ftp.downloadFileIntoTempFile(remoteFile);
        studentJSONFile = new StudentJSONFileEditor(localFile);
        return localFile;
    }

    public HashMap<String, Student> getCacheStudents() {
        return studentJSONFile.getCacheStudents();
    }

    public Student getStudentById(int id) throws NullPointerException {
        return studentJSONFile.getStudentById(id);
    }

    public void addStudent(Student student) {
        studentJSONFile.addStudent(student);
    }

    public void removeStudentById(int id) throws NullPointerException {
        studentJSONFile.removeStudentById(id);
    }

    public void updateFile() throws Exception {
        studentJSONFile.updateJSONFile();
        ftp.updateRemoteFileByTempFile();
    }

    public void close() throws Exception {
        ftp.close();
    }

}
