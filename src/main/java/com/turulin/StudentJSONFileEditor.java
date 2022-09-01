package com.turulin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class StudentJSONFileEditor {
    private String pathFile;
    private HashMap<String, Student> students = new LinkedHashMap<>();
    private int maxId = 0;

    public StudentJSONFileEditor(String pathFile) throws IOException {
        if (!Files.exists(Paths.get(pathFile)))
            throw new NoSuchFileException("File don't exist.");
        this.pathFile = pathFile;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(pathFile));
        while (bufferedReader.ready()) {
            String line = bufferedReader.readLine();
            if (!line.contains("id"))
                continue;
            line = line.replaceAll("[\\p{Space}]|[\"]|[,]", "");
            int id = Integer.valueOf(line.substring(line.indexOf(":") + 1));
            line = bufferedReader.readLine();
            if (!line.contains("name"))
                continue;
            line = line.replaceAll("[\\p{Space}]|[\"]|[,]", "");
            String name = line.substring(line.indexOf(":") + 1);
            students.put(String.valueOf(id), new Student(id, name));
            if (maxId < id)
                maxId = id;
        }
        bufferedReader.close();
    }

    public HashMap<String, Student> getCacheStudents() {
        return students;
    }

    public Student getStudentById(int id) throws NullPointerException {
        if (!students.containsKey(String.valueOf(id)))
            throw new NullPointerException("Student with id " + id + " not exist");
        return students.get(String.valueOf(id));
    }

    public void addStudent(Student student) {
        students.put(String.valueOf(maxId+1), new Student(maxId+1, student.getName()));
        ++maxId;
    }

    public void removeStudentById(int id) throws  NullPointerException {
        if (!students.containsKey(String.valueOf(id)))
            throw new NullPointerException("Student with id " + id + " not exist");
        students.remove(String.valueOf(id));
    }

    public void updateJSONFile() throws IOException {
        String start = "{\n\t\"students\": [\n";
        String format = "\t\t{\n" +
                "\t\t\t\"id\":%s,\n" +
                "\t\t\t\"name\":\"%s\"\n" +
                "\t\t}";
        String end = "\n\t]\n}";
        //Files.writeString(Path.of(pathFile), start);
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathFile));
        bufferedWriter.write(start);
        Iterator<Map.Entry<String, Student>> iterator = students.entrySet().iterator();
        Map.Entry<String, Student> entry;
        for (int i = 0; i < students.size() - 1; ++i) {
            entry = iterator.next();
            bufferedWriter.write(String.format(format, entry.getKey(), entry.getValue().getName()));
            bufferedWriter.write(",\n");
        }
        entry = iterator.next();
        bufferedWriter.write(String.format(format, entry.getKey(), entry.getValue().getName()));
        bufferedWriter.write(end);
        bufferedWriter.flush();
        bufferedWriter.close();
//        Files.writeString(Path.of(pathFile), String.format(format, entry.getKey(), entry.getValue().getName()), StandardOpenOption.APPEND);
//        Files.writeString(Path.of(pathFile), end, StandardOpenOption.APPEND);
    }
}
