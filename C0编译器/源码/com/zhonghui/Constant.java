package com.zhonghui;

import static com.zhonghui.Constant.symbol.*;

/**
 * 常量的定义
 * Created by zhonghui on 2018/10/31 0031.
 */
public class Constant {
    public static final int MAX_SIZE = 1000;//运行栈的大小
    public static final symbol[] declbegsys = new symbol[]{intsym,voidsym};//声明开始符号集
    public static final symbol[] statbegsys = new symbol[]{ifsym,whilesym,voidsym,intsym,scanfsym,printfsym,semicolon,rbrace,ident};//语句开始符号集
    /**
     * 符号常量
     * 词法分析类型的定义
     */
    public static enum symbol {
        nul(""),
        ident("ident"),
        number("number"),
        plus("+"),
        minus("-"),
        times("*"),
        slash("/"),
        lparen("("),
        rparen(")"),
        comma(","),
        semicolon(";"),
        ifsym("if"),
        whilesym("while"),
        lbrace("{"),
        rbrace("}"),
        intsym("int"),
        voidsym("void"),
        elsesym("else"),
        scanfsym("scanf"),
        printfsym("printf"),
        returnsym("return"),
        becomes("=");
        private String text;

        symbol(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }


        @Override
        public String toString() {
            return this.text;
        }
    }
    public static final symbol[] keywords = new symbol[]{//关键字
            ifsym,whilesym,intsym,voidsym,elsesym,scanfsym,printfsym,returnsym
    };
    public static final symbol[] single = new symbol[]{//单字符
            becomes,plus, minus, times, slash, lparen, rparen, comma, semicolon, lbrace, rbrace
    };

    /**
     *
     */
    public static enum nameTable {
        VARIABLE,//变量
        VOID_PROCEDUR,//void过程
        INT_PROCEDUR//int过程
    }

    /**
     * 操作符常量的定义
     * Ced by zhonghui on 2016/10/31 0031.
     */
    public static enum Operator {
        LIT("LIT"),// 0 a	将常数值取到栈顶，a为常数值
        LOD("LOD"),// t a	将变量值取到栈顶，a为相对地址，t为层数
        STO("STO"),// t a	将栈顶内容送入某变量单元中，a为相对地址，t为层数
        CAL("CAL"),// 0 a	调用函数，a为函数地址
        INT("INT"),// 0 a	在运行栈中为被调用的过程开辟a个单元的数据区
        JMP("JMP"),// 0 a	无条件跳转至a地址
        JPC("JPC"),// 0 a	条件跳转，当栈顶值为0，则跳转至a地址，否则顺序执行
        ADD("ADD"),// 0 0	次栈顶与栈顶相加，退两个栈元素，结果值进栈
        SUB("SUB"),// 0 0	次栈顶减去栈顶，退两个栈元素，结果值进栈
        MUL("MUL"),// 0 0	次栈顶乘以栈顶，退两个栈元素，结果值进栈
        DIV("DIV"),// 0 0	次栈顶除以栈顶，退两个栈元素，结果值进栈
        RED("RED"),// 0 0	从命令行读入一个输入置于栈顶
        WRT("WRT"),// 0 0	栈顶值输出至屏幕并换行
        RET("RET");// 0 0	函数调用结束后,返回调用点并退栈

        private String text;

        Operator(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
