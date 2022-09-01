package com.turulin;

public class Student {
    private int id;
    private String name;

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Student() {
    }

    public Student(int id, String name) {
        this.id=id;
        this.name=name;
    }

    @Override
    public String toString() {
        return String.format("Id: %d Name: %s", id, name);
    }

    @Override
    public boolean equals(Object obj) {
        Student student = (Student) obj;
        return this.id==student.id && this.name.equals(student.name);
    }
}
