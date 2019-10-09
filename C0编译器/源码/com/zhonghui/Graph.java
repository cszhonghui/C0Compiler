package com.zhonghui;

import com.zhonghui.Main.*;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.concurrent.Semaphore;

import static com.zhonghui.Main.aimCode;
import static com.zhonghui.Main.tableStructs;

public class Graph extends JFrame {
    static boolean isOk=false;
    static Semaphore sem=new Semaphore(0);
    static String inputValue=new String("");
    static JButton jb_open=new JButton("打开");
    static JButton jb_compile=new JButton("编译");
    static JButton jb_in_ok=new JButton("确定输入");
    static JTextArea jt_in=new JTextArea();
    static JTextArea jt_out_nametable=new JTextArea();
    static JTextArea jt_out_console=new JTextArea();
    static JTextArea jt_out_aimcode=new JTextArea();
    static JTextField jt_in_console=new JTextField();
    static Font font=new Font("宋体",Font.BOLD,40);
    static Font font1=new Font("宋体",Font.BOLD,25);
    public void Layout(){
        Box box_left_bottom=Box.createHorizontalBox();
        box_left_bottom.add(jb_open);
        box_left_bottom.add(jb_compile);
        Box box_left=Box.createVerticalBox();
        JScrollPane jsp=new JScrollPane(jt_in);
        jsp.setPreferredSize(new Dimension(500,650));
        box_left.add(jsp);
        box_left.add(box_left_bottom);
        Box box_right_top=Box.createHorizontalBox();
        jsp=new JScrollPane(jt_out_console);
        jsp.setPreferredSize(new Dimension(300,650));
        box_right_top.add(new JScrollPane(jsp));
        jsp=new JScrollPane(jt_out_aimcode);
        jsp.setPreferredSize(new Dimension(300,200));
        box_right_top.add(new JScrollPane(jsp));
        Box box_right=Box.createVerticalBox();
        box_right.add(box_right_top);
        Box box_right_middle=Box.createHorizontalBox();
        box_right_middle.add(jt_in_console);
        box_right_middle.add(jb_in_ok);
        box_right.add(box_right_middle);
        jsp=new JScrollPane(jt_out_nametable);
        jsp.setPreferredSize(new Dimension(300,230));
        box_right.add(jsp);
        Box box_all=Box.createHorizontalBox();
        box_all.add(box_left);
        box_all.add(box_right);
        jb_compile.setFont(font);
        jb_open.setFont(font);
        jb_in_ok.setFont(font);
        jt_in.setBorder(new BorderUIResource.LineBorderUIResource(Color.red));
        jt_out_aimcode.setBorder(new BorderUIResource.LineBorderUIResource(Color.red));
        jt_out_console.setBorder(new BorderUIResource.LineBorderUIResource(Color.red));
        jt_out_nametable.setBorder(new BorderUIResource.LineBorderUIResource(Color.red));
        jt_in_console.setBorder(new BorderUIResource.LineBorderUIResource(Color.red));
        jt_out_console.setFont(font1);
        jt_in_console.setFont(font1);
        jt_in.setFont(font1);
        jt_out_nametable.setFont(font1);
        jt_out_aimcode.setFont(font1);
        getContentPane().add(box_all);
        pack();
        setSize(1500,1000);
        setVisible(true);
    }

    Graph(){
        setTitle("钟辉的C0编译器(V1.0)");
        setFont(font);
        Layout();
        jb_open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc=new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jfc.setCurrentDirectory(new File("tmp.txt"));
                jfc.showDialog(new JLabel(), "选择");
                File file=jfc.getSelectedFile();
                try {
                    BufferedReader br=new BufferedReader(new FileReader(file));
                    String tmp=null;
                    while((tmp=br.readLine())!=null){
                        jt_in.append(tmp+"\n");
                    }
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        jb_compile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                jt_out_console.setText("");
                jt_out_nametable.setText("");
                jt_out_aimcode.setText("");
                File file=new File("tmp.txt");
                String str=Graph.jt_in.getText();
                try {
                    BufferedWriter bw=new BufferedWriter(new FileWriter(file));
                    bw.write(str);
                    bw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                RunThread th=new RunThread();
                th.start();

            }
        });
        jb_in_ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!jt_in_console.getText().equals("")){
                    inputValue=jt_in_console.getText();
                    isOk=true;
                    sem.release();
                    jt_in_console.setText("");
                }
            }
        });
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jt_in_console.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyChar()==KeyEvent.VK_ENTER){
                    if(!jt_in_console.getText().equals("")){
                        inputValue=jt_in_console.getText();
                        isOk=true;
                        sem.release();
                        jt_in_console.setText("");
                    }
                }
            }
        });
    }
    public  static void main(String args[]){
        Graph graph=new Graph();
    }
}
