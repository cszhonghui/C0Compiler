package com.zhonghui;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.Thread.sleep;

/**
 * C0语言的解释程序
 * Created by zhonghui on 2018/10/26 0026.
 */
 class Instruction {
    public Constant.Operator operator;
    public int frist;
    public int second;
    /**
     * 一段代码
     *
     * @param operator 操作符
     * @param frist  第一个操作数
     * @param second   第二个操作数
     */
    public Instruction(Constant.Operator operator, int frist, int second) {
        this.operator = operator;
        this.frist = frist;
        this.second = second;
    }
    /**
     * 一段代码
     * @param operator 字符串类型操作符
     * @param frist  第一个操作数
     * @param second   第二个操作数
     */
    public Instruction(String operator, int frist, int second) {
        this.operator = Enum.valueOf(Constant.Operator.class, operator);
        this.frist = frist;
        this.second = second;
    }

    @Override
    public String toString() {
        return operator+" "+frist+" "+second;
    }


}
class Gen {
    public void doGen(Constant.Operator operator,int frist ,int second){
        Instruction aimCode = new Instruction(operator,frist,second);
        Main.aimCode.add(aimCode);
    }
}
 public class Interpreter {
    private ArrayList<Instruction> code;//程序地址寄存器
    private int[] runStack;//运行栈
    private int top_Addr = 0;//栈顶指针
    private int base_Addr = 0;//基址寄存器
    private int current_Addr = 0;//指令寄存器当前正在解释执行的目标指令
    private int i = 0;

    public Interpreter() {
        code = new ArrayList<Instruction>();
        runStack = new int[Constant.MAX_SIZE];

        readAll();
    }

    public Interpreter(ArrayList<Instruction> code) {
        this.code = code;
        runStack = new int[Constant.MAX_SIZE];


    }

    /**
     * 读取文件中代码的一行
     *
     * @return 代码的一行
     */
    private Instruction readLine(BufferedReader br) {
        String line = null;
        try {
            line = br.readLine();
            if (line == null) {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String[] split = line.trim().split(" ");//分割代码
        Instruction aline = null;
        try {
            aline = new Instruction(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]));//构建一条指令
        } catch (Exception e) {
            return null;
        }
        return aline;
    }
    /**
     * 读取全部代码存入code程序地址寄存器
     */
    private void readAll() {
        Graph.jt_out_console.append("请输入文件名\n");
        try {
            wait(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Scanner sc = new Scanner(System.in);
        String path = sc.nextLine();
        File f = new File(path);
        if (!f.isFile()) {
            Graph.jt_out_console.append("请正确输入\n");
            try {
                wait(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        BufferedReader br = null;//读取文件的流
        try {
            br = new BufferedReader(new FileReader(f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Instruction aline;
        while ((aline = readLine(br)) != null) {//循环读取所有的代码
            code.add(aline);
        }
        try {
            if (br != null)
                br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 获取变量地址
     * 如果层数是0就返回全局变量地址
     * 否则就返回当前函数的变量地址
     *
     * @param currentCode 当前代码
     * @return 取到的变量的地址
     */
    private int getLocation(Instruction currentCode) {
        if (currentCode.frist == 0) {
            return currentCode.second;
        } else
            return base_Addr + currentCode.second;
    }
    /**
     *执行解释
     */
       public void paser() {
          Thread th=Thread.currentThread();
          th.setPriority(Thread.MIN_PRIORITY);
        do {
            Instruction currentCode = code.get(current_Addr);
            current_Addr++;
            switch (currentCode.operator.toString()) {
                case "LIT"://LIT 0 a  将常数值取到栈顶，a为常数值
                    runStack[top_Addr] = currentCode.second;
                    top_Addr++;
                    break;
                case "LOD"://LOD t a 将变量值取到栈顶，a为相对地址，t为层数
                    runStack[top_Addr] = runStack[getLocation(currentCode)];
                    top_Addr++;
                    break;
                case "STO"://STO t a 将栈顶内容送入某变量单元中，a为相对地址，t为层数
                    top_Addr--;
                    runStack[getLocation(currentCode)] = runStack[top_Addr];
                    break;
                case "CAL"://CAL 0 a 调用函数，a为函数地址
                    if(i==0){
                        runStack[top_Addr] = base_Addr;        //当前基址入栈(用作动态链）
                        runStack[top_Addr + 1] = current_Addr; //当前指令地址入栈 RA(用作返回地址)
                        i++;
                    }else{
                        runStack[top_Addr] = base_Addr;        //当前基址入栈(用作动态链）
                        runStack[top_Addr + 1] = current_Addr; //当前指令地址入栈 RA(用作返回地址)
                    }
                    base_Addr = top_Addr;                  //新基址
                    current_Addr = currentCode.second;
                    break;
                case "INT"://INT 0 a 在运行栈中为被调用的过程开辟a个单元的数据区
                    top_Addr = top_Addr + currentCode.second;
                    break;
                case "JMP"://JMP 0 a 无条件跳转至a地址
                    current_Addr = currentCode.second;
                    break;
                case "JPC"://JPC 0 a 条件跳转，当栈顶值为0，则跳转至a地址，否则顺序执行
                    top_Addr--;
                    if (runStack[top_Addr] == 0) {
                        current_Addr = currentCode.second;
                    }
                    break;
                case "ADD"://ADD 0 0 次栈顶与栈顶相加，退两个栈元素，结果值进栈
                    top_Addr--;
                    runStack[top_Addr - 1] = runStack[top_Addr - 1] + runStack[top_Addr];
                    break;
                case "SUB"://SUB 0 0 次栈顶减去栈顶，退两个栈元素，结果值进栈
                    top_Addr--;
                    runStack[top_Addr - 1] = runStack[top_Addr - 1] - runStack[top_Addr];
                    break;
                case "MUL"://MUL 0 0 次栈顶乘以栈顶，退两个栈元素，结果值进栈
                    top_Addr--;
                    runStack[top_Addr - 1] = runStack[top_Addr - 1] * runStack[top_Addr];
                    break;
                case "DIV"://DIV 0 0 次栈顶除以栈顶，退两个栈元素，结果值进栈
                    top_Addr--;
                    if (runStack[top_Addr] == 0) {
                        Graph.jt_out_console.append("被除数不能为0\n");
                        try {
                            wait(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.exit(0);
                    }
                    runStack[top_Addr - 1] = runStack[top_Addr - 1] / runStack[top_Addr];
                    break;
                case "RED"://RED 0 0 从命令行读入一个输入置于栈顶
                    Graph.jt_out_console.append("请输入:");
                    Thread.yield();
                    try {
                        Graph.sem.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    boolean flag = true;
                    while (flag) {
                        try {
                            runStack[top_Addr] = Integer.parseInt(Graph.inputValue);//只能输入数字
                            Graph.jt_out_console.append(Graph.inputValue+"\n");
                            flag = false;
                        } catch (Exception e) {
                            Graph.jt_out_console.append("输入错误，请正确输入\n");
                            try {
                                wait(1);
                            } catch (InterruptedException k) {
                                k.printStackTrace();
                            }
                        }
                    }
                    top_Addr++;
                    break;
                case "WRT"://WRT 0 0 栈顶值输出至屏幕并换行
                    top_Addr--;
                    Graph.jt_out_console.append(Integer.toString(runStack[top_Addr])+"\n");
                    Thread.yield();
                    break;
                case "RET"://RET 0 0 函数调用结束后,返回调用点并退栈
                    top_Addr = base_Addr;
                    current_Addr = runStack[top_Addr + 1];
                    base_Addr = runStack[top_Addr];
                    break;
            }
        } while (!(current_Addr == 0&&base_Addr==0));
    }

    public static void main(String s[]) {
        Interpreter a = new Interpreter();
        a.paser();
    }
}
