package com.zhonghui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.zhonghui.Main.aimCode;
import static com.zhonghui.Main.tableStructs;

public class RunThread extends Thread {
    @Override
    public void run() {

        Program program = new Program("tmp.txt");
        program.excute();
        int ins_count=0;
        for(Instruction ins:aimCode){
            Graph.jt_out_aimcode.append("第"+(ins_count++)+"个："+ins+"\n");
        }
        ins_count=0;
        for(TableStruct ts:tableStructs){
            Graph.jt_out_nametable.append(ts+"\n");
        }
        File file=new File("nametable.txt");
        BufferedWriter bw= null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            for (TableStruct t : tableStructs) {
                bw.write(t.toString()+"\n");
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file=new File("code.txt");
        try {
            bw=new BufferedWriter(new FileWriter(file));
            int j=0;
            for (Instruction i : aimCode) {
                bw.write("第"+j+"个 ");
                bw.write(i.toString()+"\n");
                j++;
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Interpreter interpreter = new Interpreter(aimCode);
        interpreter.paser();
    }
}
