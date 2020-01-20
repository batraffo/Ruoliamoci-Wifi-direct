package com.example.ruoliamoci;


import java.io.File;

/**
 * represents a type of operation that I send
 */
public class Operation {

    Type distaip;
    File f;
    long time;

    public void setFile(File f){
        this.f=f;
    }

    public void setTime(long time){
        this.time=time;
    }

    public Operation(Type taip){
        distaip=taip;
    }

    public enum Type{
        TEST,OK,FILE,DATE
    }

}