package com.zhonghui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by zhonghui on 2018/11/28 0028.
 */
public class Main {
    public static List<TableStruct> tableStructs = new ArrayList<TableStruct>();//名字表的数组
    public static ArrayList<Instruction> aimCode = new ArrayList<Instruction>();//存放虚拟机生成的代码
    public static ArrayList<FuncStruct> undefiedFunc = new ArrayList<FuncStruct>();//存放未定義的
    // public static int aim_codePos = 0;
    public static void main(String[] args) throws IOException {
        Program program = new Program("test2.txt");
        program.excute();
        Interpreter interpreter = new Interpreter(aimCode);
        System.out.println("是否打印名字表？");
        Scanner cin=new Scanner(System.in);
        String a=cin.next();
        if(!a.equals("n"))
            System.out.println("名字表如下：");
        File file=new File("nametable.txt");
        BufferedWriter bw=new BufferedWriter(new FileWriter(file));
        for (TableStruct t : tableStructs) {
            if(!a.equals("n"))
                System.out.println(t);
            bw.write(t.toString()+"\n");
        }
        bw.flush();
        bw.close();
        System.out.println();
        System.out.println("是否打印目标代码？");
        a=cin.next();
        if(!a.equals("n"))
            System.out.println("目标代码如下：");
        file=new File("code.txt");
        bw=new BufferedWriter(new FileWriter(file));
        int j=0;
        for (Instruction i : aimCode) {
            if(!a.equals("n")){
                System.out.print("第"+j+"个 ");
                System.out.println(i);
            }
            bw.write("第"+j+"个 ");
            bw.write(i.toString()+"\n");
            j++;
        }
        bw.flush();
        bw.close();
        interpreter.paser();
    }
}
