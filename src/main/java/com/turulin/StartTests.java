package com.turulin;

import org.testng.TestNG;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class StartTests {
    public static void main(String[] args) throws URISyntaxException, IOException {
        TestNG testNG = new TestNG(true);
        List<String> suites = new ArrayList<>();
        InputStream inputStream = StartTests.class.getClassLoader().getResourceAsStream("TestNG.xml");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String path = Files.createTempFile("temp", "xml").toString();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(path));
        while (bufferedReader.ready()) {
            bufferedWriter.write(bufferedReader.readLine());
            bufferedWriter.newLine();
        }
        bufferedReader.close();
        bufferedWriter.close();

        suites.add(path);
        testNG.setTestSuites(suites);
        testNG.run();


        inputStream.close();

    }
}
