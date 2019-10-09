package com.zhonghui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static com.zhonghui.Constant.keywords;
import static com.zhonghui.Constant.single;

class TypeValue<T> {
    public Constant.symbol wordType;
    public T value;
    public TypeValue(Constant.symbol wordType, T value) {
        this.wordType = wordType;
        this.value = value;
    }

    public static void main(String[] args) {
        TypeValue a = new TypeValue(Constant.symbol.becomes,1);
        System.out.println(a.value);
    }
}

/**
 * 词法分析器实现
 * Created by zhonghui on 2018/10/31 0031.
 */
public class    Lex {
    private static String code;//全部代码
    private static int position = 0;//当前检测到的字符位置
    public static int count;
    public TypeValue tv;//上一个
    public Lex(String path) throws IOException {//初始化并读取全部代码
        BufferedReader br = new BufferedReader(new FileReader(path));
        StringBuilder sb = new StringBuilder();
        String temp;
        while ((temp = br.readLine()) != null) {
            sb.append(temp);
        }
        code = sb.toString();
    }

    /**
     * 识别一个单词
     *
     * @return 取出的单词
     */
    public TypeValue getsym(boolean back) {
        if(back){
            return tv;
        }
        StringBuilder sb = new StringBuilder();
        char achar;
        try {
            achar = code.charAt(position++);
        } catch (Exception e) {//如果没有代码就返回空
            return null;
        }

        while (achar == ' ' || achar == 10 || achar == 9) {//如果是空格换行和tab，则取下一个字符
            achar = code.charAt(position++);
        }
        if (Character.isLetter(achar)) {//如果是字母就获取完整单词
            //获取完整单词
            while (Character.isLetterOrDigit(achar)) {
                sb.append(achar);
                achar = code.charAt(position++);
            }
            --position;
            String newStr = sb.toString();
            for (Constant.symbol a : keywords) {//遍历关键字表
                if (newStr.equals(a.toString())) {//判断是哪个关键字
                    tv=new TypeValue(a, newStr);
                    return new TypeValue(a, newStr);
                }
            }
            tv=new TypeValue(Constant.symbol.ident, newStr);//搜索失败为名字
            return new TypeValue(Constant.symbol.ident, newStr);//搜索失败为名字
        } else if (Character.isDigit(achar)) {//如果是数字
            while (Character.isDigit(achar)) { //获取完整数字
                sb.append(achar);
                achar = code.charAt(position++);
            }
            --position;
            int value = Integer.parseInt(sb.toString());
            tv=new TypeValue(Constant.symbol.number, value);//返回常量
            return new TypeValue(Constant.symbol.number, value);//返回常量
        } else {
            for (Constant.symbol a : single) {//如果是单字符
                if (a.toString().charAt(0) == achar) {
                    if(achar == '{'){
                        count++;
                    }else if(achar == '}'){
                        count--;
                    }
                    tv=new TypeValue(a, achar);//返回操作符
                    return new TypeValue(a, achar);//返回操作符
                }

            }
        }
        return null;//都不是的话返回空
    }

    public static void main(String[] args) {
        String s = "1.txt";
        try {
            Lex l = new Lex(s);
            System.out.println();
            System.out.println(l.getsym(false).value);
            System.out.println(l.getsym(true).value);
            System.out.println(l.getsym(false).value);
            System.out.println(l.getsym(false).value);
            System.out.println(l.getsym(true).value);
            System.out.println(l.getsym(false).value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
