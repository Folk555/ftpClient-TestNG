import com.turulin.Student;
import com.turulin.UserFtpClient;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;

public class BaseFunctionalTests {
    UserFtpClient userFtpClient;
    HashMap<String, Student> correctStds = new HashMap<>();
    String filePath = "students_test.txt";

    @BeforeClass
    public void setup() throws Exception {
        userFtpClient = new UserFtpClient("aleksandr", "root", "192.168.143.129");
        correctStds.put("1", new Student(1, "std1"));
        correctStds.put("2", new Student(2, "std2"));
        correctStds.put("3", new Student(3, "std3"));
        Collections.unmodifiableMap(correctStds);
    }

    @Test(priority=10)
    public void downloadFile_should() throws Exception {
        String localDownloadedFile = userFtpClient.downloadFile(filePath);

        Assert.assertNotNull(localDownloadedFile);
    }

    @Test(priority=20)
    public void getCacheStudents_should() {
        HashMap<String, Student> students = userFtpClient.getCacheStudents();

        Assert.assertEquals(students, correctStds);
    }

    @Test(priority=30)
    public void getStudentById_should() {
        Student student = userFtpClient.getStudentById(1);

        Assert.assertEquals(student, new Student(1, "std1"));
    }

    @Test(priority=40)
    public void addStudent_should() {
        userFtpClient.addStudent(new Student(0, "std4"));

        Student actualStudent = userFtpClient.getCacheStudents().get("4");

        Assert.assertEquals(actualStudent, new Student(4, "std4"));
    }

    @Test(priority=50)
    public void removeStudentById_should() {
        userFtpClient.addStudent(new Student(0, "std5"));

        userFtpClient.removeStudentById(5);

        Assert.assertEquals(userFtpClient.getCacheStudents().size(), 4);
    }

    @Test(priority=60)
    public void updateFile_should() throws Exception {
        userFtpClient.updateFile();
    }

    @Test(priority=70)
    public void close_should() throws Exception {
        userFtpClient.close();
    }

    @Test(priority=80)
    public void remoteFileShouldBeEdited() throws Exception {
        userFtpClient = new UserFtpClient("aleksandr", "root", "192.168.143.129");
        downloadFile_should();

        Assert.assertEquals(userFtpClient.getCacheStudents().size(), 4);
    }

    @AfterClass
    public void updateFileToDefault() throws Exception {
        userFtpClient = new UserFtpClient("aleksandr", "root", "192.168.143.129");
        userFtpClient.downloadFile(filePath);
        userFtpClient.getCacheStudents().clear();
        userFtpClient.getCacheStudents().putAll(correctStds);
        userFtpClient.updateFile();
        userFtpClient.close();
    }

}
