package com.zhonghui;

import java.io.IOException;
import java.util.List;

import static com.zhonghui.Constant.Operator.*;
import static com.zhonghui.Constant.nameTable.INT_PROCEDUR;
import static com.zhonghui.Constant.nameTable.VARIABLE;
import static com.zhonghui.Constant.nameTable.VOID_PROCEDUR;
import static com.zhonghui.Constant.symbol.*;

/**
 * Created by zhonghui on 2018/11/28 0028.
 */
class FuncStruct {//未提前定义的函数回填使用
    public TypeValue typevalue;
    public Integer pos;
    public FuncStruct(TypeValue tv,Integer a){
        typevalue=tv;
        pos=a;
    }
}
class TableStruct {
    public String name;//标识符
    public Constant.nameTable type;//标识符的类型
    public int level;//所在层
    public int addr;//地址
    public int size;//大小
    public String belong;//所属
    @Override
    public String toString() {
        return ("类型:" + type + " 标识符:" + name + " 所在层" + level + " 相对地址" + addr + " 大小" + size+"  所属"+belong);
    }
}
public class Program {
    Lex lex;//词法分析器
    Gen gen = new Gen();//虚拟机代码生成
    TypeValue typeValue;//lex的返回值
    List<TableStruct> nametable;//名字表
    String identVale;//保存标识符
    boolean hasElse=false;
    /**
     * @param path 要读的文件路径
     */
    public Program(String path) {
        nametable = Main.tableStructs;
        try {
            lex = new Lex(path);
        } catch (IOException e) {
            Graph.jt_out_console.append("文件错误");
            return;
        }
    }
    /**
     * 分程序处理
     */
    public void block(boolean isMain) {
        gen.doGen(INT,0,3);
        int varAssignPos;//变量名字分配到的相对地址    //dx
        int cx1,cx2;//用于保存虚拟机指令位置
        cx2 = nametable.size()-1;
        varAssignPos = 3;
        if(isMain)
        {
            for (TableStruct nt:nametable) {
                if(nt.level==0&&nt.type==VARIABLE)
                    varAssignPos++;
            }
        }
        cx1 = Main.aimCode.size()-1;

        while (true) {
            varAssignPos = statement(varAssignPos);//语句序列
            if(typeValue.wordType.equals(rbrace)&&Lex.count==0){//直到读到右花括号，该分程序结束
                break;
            }
        }
        Main.aimCode.get(cx1).second = varAssignPos;
        nametable.get(cx2).size = varAssignPos;
        gen.doGen(RET, 0, 0);
    }

    /**
     * 执行程序入口
     */
    public void excute() {
        int flag = 0;//用于检测主函数
        String belong="none";
        int varAssignPos = 3;//变量名字分配到的相对地址
        gen.doGen(JMP, 0, 0);//虚拟机代码调到主函数默认为调到第一个
        while ((typeValue = lex.getsym(false)) != null) {
            if (typeValue.wordType.equals(intsym)) {//先检测int符号
                typeValue = lex.getsym(false);
                if (typeValue.wordType.equals(ident)) {//是否是标识符
                    identVale = (String) typeValue.value;
                    typeValue = lex.getsym(false);
                    if (!typeValue.wordType.equals(lparen)) {//如果检测到的不是括号证明是变量的声明
                        varAssignPos = intDeal(0, varAssignPos, identVale);//进行全局变量声明
                    } else {//如果是左括号就进行int分程序的声明
                        typeValue = lex.getsym(false);
                        enter(INT_PROCEDUR, 0, identVale,belong);//int分程序声明
                        if (typeValue.wordType.equals(rparen)) {
                            typeValue = lex.getsym(false);
                            if (typeValue.wordType.equals(lbrace)) {
                                block(false);//进行函数内部的处理，也就是分程序
                            } else {
                                Graph.jt_out_console.append("缺少左花括号错误："+"Error at "+typeValue.value);
                            }
                        } else {
                            Graph.jt_out_console.append("缺少左括号错误："+"Error at "+typeValue.value);
                        }
                    }
                } else {//不是标识符的错误
                    Graph.jt_out_console.append("标识符定义错误："+"Error at "+typeValue.value);
                }
            } else if (typeValue.wordType.equals(voidsym)) {
                boolean isMain=false;
                typeValue = lex.getsym(false);
                if (typeValue.wordType.equals(ident)) {
                    if (typeValue.value.equals("main")) {
                        if (flag == 1) {
                            Graph.jt_out_console.append("已经存在了主函数 "+"Error at "+typeValue.value);
                        }
                        flag = 1;
                        Main.aimCode.get(0).second = Main.aimCode.size();//修改主函数的跳转地址
                        isMain=true;
                    }
                    enter(VOID_PROCEDUR, 0, (String) typeValue.value,belong);
                    typeValue = lex.getsym(false);
                    paren();//判断小括号是否左右搭配
                    if (typeValue.wordType.equals(lbrace)) {
                        block(isMain);//进行函数内部的处理
                    } else {
                        Graph.jt_out_console.append("缺少左花括号错误："+"Error at "+typeValue.value);
                    }
                } else {
                    Graph.jt_out_console.append("未定义变量错误："+"Error at "+typeValue.value);
                }
            }
        }
        if (flag == 0) {
           Graph.jt_out_console.append("缺少主函数错误："+"Error at ");
        }

        for(FuncStruct fs:Main.undefiedFunc){//未提前声明的函数回填
            boolean v=false;
            for(TableStruct ts:Main.tableStructs){
                if(fs.typevalue.value.equals(ts.name)&&(ts.type.equals(VOID_PROCEDUR))){
                    v=true;
                    Main.aimCode.get(fs.pos).operator=CAL;
                    Main.aimCode.get(fs.pos).second=ts.addr;
                }
                else if(fs.typevalue.value.equals(ts.name)&&(ts.type.equals(INT_PROCEDUR))){
                    v=true;
                   Main.aimCode.get(fs.pos-1).operator=CAL;
                   Main.aimCode.get(fs.pos-1).second=ts.addr;
                   Main.aimCode.get(fs.pos).operator=LOD;
                   Main.aimCode.get(fs.pos).frist=0;
                   Main.aimCode.get(fs.pos).second=999;
                }
            }
            if(v==false){
                Graph.jt_out_console.append("函数未定义错误："+"Error at   "+fs.typevalue.value);break;
            }
        }
    }
    /**
     * 查找标识符在名字表的位置
     * @param tv 要查找的TypeValue
     * @return 找到则返回在名字表中的位置否则返回-1
     */
    int position(TypeValue tv,String range) {
        for (TableStruct table : nametable) {
            if (table.name.equals(tv.value)&&table.belong.equals(range)) {
                return nametable.indexOf(table);//先找当前作用域的
            }
            if (table.name.equals(tv.value)&&table.belong.equals("all")) {
                return nametable.indexOf(table);//找全局变量
            }
        }
        return -1;
    }
    /**
     * 赋值语句处理//函数调用
     */
    private void assignDeal() {

        String range="all";
        for(int ii=nametable.size()-1;ii>=0;ii--){
            if(nametable.get(ii).type.equals(INT_PROCEDUR) ||nametable.get(ii).type.equals(VOID_PROCEDUR) ){
                range=nametable.get(ii).name;
                break;
            }
        }
        int i = position( typeValue,range);//保存取得的变量在名字表中的位置
        if (i == -1) {//如果不是变量
            TypeValue tmp=typeValue;
            typeValue = lex.getsym(false);
            if(typeValue.wordType.equals(lparen)){//如果是未声明的函数调用
                gen.doGen(JMP, 0, Main.aimCode.size()+1);//生成jmp指令
                gen.doGen(JMP, 0, Main.aimCode.size()+1);//生成jmp指令
                Main.undefiedFunc.add(new FuncStruct(tmp,Main.aimCode.size()-1));//用于后续修改
            }
            else{
                Graph.jt_out_console.append("不是变量错误："+"Error at "+typeValue.value);
            }
        } else if (nametable.get(i).type != Constant.nameTable.VARIABLE && nametable.get(i).type != VOID_PROCEDUR) {//如果不是变量或分程序
            Graph.jt_out_console.append("应以变量开头："+"Error at "+typeValue.value);
        } else if (nametable.get(i).type == Constant.nameTable.VARIABLE) {//如果是变量
            typeValue = lex.getsym(false);
            if (typeValue.wordType.equals(becomes)) {//检测到等号
                typeValue = lex.getsym(false);
            } else {
                Graph.jt_out_console.append("缺少等于符号错误："+"Error at "+typeValue.value);
            }
            expression();//处理复制符号右侧的表达式
            if (i != -1) {
                gen.doGen(STO, nametable.get(i).level, nametable.get(i).addr);//生成sto指令
            }
            if (typeValue.wordType.equals(semicolon)) {

            }else{
                Graph.jt_out_console.append("缺少分号错误："+"Error at "+typeValue.value);
            }
        } else if (nametable.get(i).type.equals(VOID_PROCEDUR)) {
            typeValue = lex.getsym(false);
            if (nametable.get(i).type.equals(VOID_PROCEDUR)) {
                gen.doGen(CAL, 0, nametable.get(i).addr);//生成call指令
            } else {
                Graph.jt_out_console.append("非函数错误："+"Error at "+typeValue.value);
            }
        }
        else if (nametable.get(i).type.equals(INT_PROCEDUR)) {
            typeValue = lex.getsym(false);
            if (nametable.get(i).type.equals(INT_PROCEDUR)) {
                gen.doGen(CAL, 0, nametable.get(i).addr);//生成call指令
            } else {
                Graph.jt_out_console.append("非函数错误："+"Error at "+typeValue.value);
            }
        }
    }

    /**
     * 输入语句处理
     */
    private void scanfDeal() {
        int i;//保存取得的变量在名字表中的位置

        typeValue = lex.getsym(false);
        if (typeValue.wordType.equals(lparen)) {
            typeValue = lex.getsym(false);
            if (typeValue.wordType.equals(ident)) {
                //查找当前的作用范围
                String range="main";
                for(int ii=nametable.size()-1;ii>=0;ii--){
                    if(nametable.get(ii).type.equals(INT_PROCEDUR) ||nametable.get(ii).type.equals(VOID_PROCEDUR) ){
                        range=nametable.get(ii).name;
                        break;
                    }
                }
                i = position(typeValue,range);
                if (i == -1) {
                    Graph.jt_out_console.append("变量未定义错误："+"Error at "+typeValue.value);
                }
                else {
                    gen.doGen(RED, 0, 0);
                    gen.doGen(STO, nametable.get(i).level, nametable.get(i).addr);
                    typeValue = lex.getsym(false);
                    if (typeValue.wordType.equals(rparen)) {
                        typeValue = lex.getsym(false);
                        if (typeValue.wordType.equals(semicolon)) {

                        } else {
                            Graph.jt_out_console.append("缺少分号错误："+"Error at "+typeValue.value);
                        }
                    } else {
                        Graph.jt_out_console.append("缺少右括号错误："+"Error at "+typeValue.value);
                    }
                }
            } else {
                Graph.jt_out_console.append("非变量错误："+"Error at "+typeValue.value);
            }
        } else {
            Graph.jt_out_console.append("缺少左括号错误："+"Error at "+typeValue.value);
        }
    }

    /**
     * 打印语句处理
     */
    private void printDeal() {
        typeValue = lex.getsym(false);
        if (typeValue.wordType.equals(lparen)) {
            typeValue = lex.getsym(false);
            expression();//表达式处理
            gen.doGen(WRT, 0, 0);
            if (typeValue.wordType.equals(rparen)) {
                typeValue = lex.getsym(false);
                if (typeValue.wordType.equals(semicolon)) {

                } else {
                    Graph.jt_out_console.append("缺少分号错误："+"Error at "+typeValue.value);
                }
            } else {
                Graph.jt_out_console.append("缺少右括号错误："+"Error at "+typeValue.value);
            }
        } else {
            Graph.jt_out_console.append("缺少左括号错误："+"Error at "+typeValue.value);
        }
    }

    /**
     * 条件语句处理
     * @param varAssignPos 变量声明的相对位置
     */
    private void ifDeal(int varAssignPos) {
        int cx1;//保存虚拟机代码指针
        typeValue = lex.getsym(false);
        if (typeValue.wordType.equals(lparen)) {
            typeValue = lex.getsym(false);
            expression();
            cx1 = Main.aimCode.size();//保存当前指令地址
            gen.doGen(JPC, 0, 0);//生成条件跳转指令跳转地址暂时先填0
            if (typeValue.wordType.equals(rparen)) {
                typeValue = lex.getsym(false);
                if (typeValue.wordType.equals(lbrace)) {
                    while (!typeValue.wordType.equals(rbrace)) {
                        varAssignPos = statement(varAssignPos);//如果不是有大括号就一直进行语句处理
                    }
                } else {
                    Graph.jt_out_console.append("缺少左大括号错误："+"Error at "+typeValue.value);
                }
                typeValue = lex.getsym(false);
                if (typeValue.wordType.equals(elsesym)) {
                    Main.aimCode.get(cx1).second = Main.aimCode.size()+1;//修改之前生成的跳转地址
                    typeValue = lex.getsym(false);
                    if (typeValue.wordType.equals(lbrace)) {
                        gen.doGen(JMP, 0, 0);
                        cx1=Main.aimCode.size()-1;

                        while (!typeValue.wordType.equals(rbrace)) {
                            varAssignPos = statement(varAssignPos);//不是右大括号一直进行语句处理
                        }
                        if(typeValue.wordType.equals(rbrace))//注意这里！！！！！！
                        {
                            typeValue=lex.getsym(false);
                            hasElse=true;
                        }
                        Main.aimCode.get(cx1).second = Main.aimCode.size();
                    } else {//与先下面一样
                        Graph.jt_out_console.append("缺少左大括号错误："+"Error at "+typeValue.value);
                    }
                }
                else {
                    hasElse=true;
                    Main.aimCode.get(cx1).second = Main.aimCode.size();//修改之前生成的跳转地址
                }
            } else {
                Graph.jt_out_console.append("缺少右括号错误："+"Error at "+typeValue.value);
            }

        } else {
            Graph.jt_out_console.append("缺少左括号错误："+"Error at "+typeValue.value);
        }
    }

    /**
     * 循环语句处理
     * @param varAssignPos 变量声明的相对位置
     */
    private void whileDeal(int varAssignPos) {
        int cx1, cx2;//保存虚拟机指令地址
        typeValue = lex.getsym(false);
        if (typeValue.wordType.equals(lparen)) {
            typeValue = lex.getsym(false);
            cx2 = Main.aimCode.size();
            expression(); //括号里的表达式处理
            cx1 = Main.aimCode.size();
            gen.doGen(JPC, 0, 0);//生成条件跳转指令地址暂时为0
            if (typeValue.wordType.equals(rparen)) {//右括号
                typeValue = lex.getsym(false);
                if (typeValue.wordType.equals(lbrace)) {//左花括号
                    while (!typeValue.wordType.equals(rbrace)) {
                        varAssignPos = statement(varAssignPos);//不是右大括号一直进行语句处理
                    }
            }
            } else {
                Graph.jt_out_console.append("缺少右括号错误："+"Error at "+typeValue.value);
            }
            gen.doGen(JMP, 0, cx2);
            Main.aimCode.get(cx1).second = Main.aimCode.size();
        } else {
            Graph.jt_out_console.append("缺少左括号错误："+"Error at "+typeValue.value);
        }
    }

    /**
     * 变量声明语句处理
     * @param lev          当前所在层，0代表全局变量，1代表局部变量
     * @param varAssignPos 变量声明的相对位置
     * @param identVale    标识符的值
     * @return 变量声明的位置
     */
    private int intDeal(int lev, int varAssignPos, String identVale) {
        String belong="all";
        for(int i=nametable.size()-1;i>=0;i--)
        {
            if(nametable.get(i).type==Constant.nameTable.VOID_PROCEDUR ||nametable.get(i).type==Constant.nameTable.INT_PROCEDUR)
            {
                belong=nametable.get(i).name;
                break;
            }
        }
        enter(Constant.nameTable.VARIABLE, lev, varAssignPos, identVale,belong);  //填写名字表
        varAssignPos++;
        while (typeValue.wordType.equals(comma)) {//如果检测到逗号就接着声明变量
            typeValue = lex.getsym(false);
            if (typeValue.wordType.equals(ident)) {//检测名字
                identVale = (String) typeValue.value;
                enter(Constant.nameTable.VARIABLE, lev, varAssignPos, identVale,belong);  //填写名字表
                varAssignPos++;
                typeValue = lex.getsym(false);
            }
        }
        if (typeValue.wordType.equals(semicolon)) {//检测分号

        } else {
            Graph.jt_out_console.append("缺少分号错误："+"Error at "+typeValue.value);
        }
        return varAssignPos;
    }
    /**
     * 返回语句处理
     */
    private void returnDeal() {
        typeValue = lex.getsym(false);
        expression();
        gen.doGen(STO, 0, 999);
        if (typeValue.wordType.equals(semicolon)) {
            typeValue = lex.getsym(false);
        } else {
            Graph.jt_out_console.append("缺少分号错误："+"Error at "+typeValue.value);
        }
    }

    /**
     * 语句处理
     * @param varAssignPos 变量声明的相对位置
     */
    private int statement(int varAssignPos) {
        if(hasElse){//针对else的情况特殊处理
            typeValue = lex.getsym(true);
            hasElse=false;
        }
        else {
            typeValue = lex.getsym(false);
        }
        if(typeValue ==null){
            Graph.jt_out_console.append("缺少右花括号错误："+"Error at "+typeValue.value);
        }
        if (typeValue.wordType.equals(ident)) {//准备按照赋值和函数调用语句处理
            assignDeal();
        } else if (typeValue.wordType.equals(scanfsym)) {//处理输入语句
            scanfDeal();
        } else if (typeValue.wordType.equals(printfsym)) {//处理打印
            printDeal();
        } else if (typeValue.wordType.equals(ifsym)) {//条件语句处理
            ifDeal(varAssignPos);
        } else if (typeValue.wordType.equals(whilesym)) {//循环语句处理
            whileDeal(varAssignPos);
        } else if (typeValue.wordType.equals(returnsym)) {//return语句处理
            returnDeal();
        } else if (typeValue.wordType.equals(intsym)) {//声明语句处理
            typeValue = lex.getsym(false);
            if (typeValue.wordType.equals(ident)) {
                identVale = (String) typeValue.value;
                typeValue = lex.getsym(false);
                varAssignPos = intDeal(1, varAssignPos, identVale);
            } else {
                Graph.jt_out_console.append("标识符定义错误："+"Error at "+typeValue.value);
            }
        }
        return varAssignPos;
    }

    /**
     * 表达式处理
     */
    private void expression() {
        Constant.symbol addop; //用于保存正负号
        if (typeValue.wordType.equals(plus) || typeValue.wordType.equals(minus)) {
            addop = typeValue.wordType;
            typeValue = lex.getsym(false);
            if (addop.equals(minus)) {
                gen.doGen(LIT, 0, 0);
            }
            term();
            if (addop.equals(minus)) {
                gen.doGen(SUB, 0, 0);
            }
        } else {
            term();
        }
        while (typeValue.wordType.equals(plus) || typeValue.wordType.equals(minus)) {
            addop = typeValue.wordType;
            typeValue = lex.getsym(false);
            term();
            if (addop.equals(plus)) {
                gen.doGen(ADD, 0, 0);
            } else {
                gen.doGen(SUB, 0, 0);
            }
        }
    }

    /**
     * 项处理
     */
    private void term() {
        Constant.symbol mul;
        factor();
        while (typeValue.wordType.equals(times) || typeValue.wordType.equals(slash)) {//如果是乘除
            mul = typeValue.wordType;
            typeValue = lex.getsym(false);
            factor();
            if (mul.equals(times)) {
                gen.doGen(MUL, 0, 0);//生成乘法指令
            } else if (mul.equals(slash)) {
                gen.doGen(DIV, 0, 0);//生成除法指令
            }
        }
    }

    /**
     * 检测括号
     * @return
     */
    int paren() {
        if (typeValue.wordType.equals(lparen)) {
            typeValue = lex.getsym(false);
            if (!typeValue.wordType.equals(rparen)) {
                Graph.jt_out_console.append("缺少右括号错误："+"Error at "+typeValue.value);
            }
        } else {
            Graph.jt_out_console.append("缺少左括号错误："+"Error at "+typeValue.value);
        }
        typeValue = lex.getsym(false);
        return 0;
    }

    /**
     * 因子处理
     */
    private void factor() {
        int i;//存放名字表中的位置
        if (typeValue.wordType.equals(ident)) {
            //查找当前的作用范围
            String range="main";
            for(int ii=nametable.size()-1;ii>=0;ii--){
                if(nametable.get(ii).type.equals(INT_PROCEDUR) ||nametable.get(ii).type.equals(VOID_PROCEDUR) ){
                    range=nametable.get(ii).name;
                    break;
                }
            }
            i = position(typeValue,range);//查找名字表
            if (i == -1) {//该标识符未声明
                TypeValue tmp=typeValue;
                typeValue = lex.getsym(false);
                if(typeValue.wordType.equals(lparen)){//如果是未声明的函数调用

                    gen.doGen(JMP, 0, Main.aimCode.size()+1);//生成jmp指令
                    gen.doGen(JMP, 0, Main.aimCode.size()+1);//生成jmp指令
                    Main.undefiedFunc.add(new FuncStruct(tmp,Main.aimCode.size()-1));//用于后续修改
                    typeValue = lex.getsym(false);
                    if(typeValue.wordType.equals(rparen)){

                    }
                    else{
                        Graph.jt_out_console.append("缺少右括号错误："+"Error at "+typeValue.value);
                    }
                    typeValue = lex.getsym(false);
                }
                else{
                    Graph.jt_out_console.append("变量未定义错误："+"Error at "+typeValue.value);
                }

            } else {
                TableStruct current = nametable.get(i);
                switch (current.type) {
                    case VARIABLE:
                        gen.doGen(LOD, current.level, current.addr);
                        typeValue = lex.getsym(false);
                        break;
                    case INT_PROCEDUR:
                        gen.doGen(CAL, 0, current.addr);
                        gen.doGen(LOD, 0, 999);
                        typeValue = lex.getsym(false);
                        paren();
                        break;
                    case VOID_PROCEDUR:
                        Graph.jt_out_console.append("返回值为空的函数不能做因子错误："+"Error at "+typeValue.value);
                        typeValue = lex.getsym(false);
                        break;
                }
            }
        } else if (typeValue.wordType.equals(number)) {//如果因子是数字
            gen.doGen(LIT, 0, (int) typeValue.value);
            typeValue = lex.getsym(false);
        } else if (typeValue.wordType.equals(lparen)) {
            typeValue = lex.getsym(false);
            expression();
            if (typeValue.wordType.equals(rparen)) {
                typeValue = lex.getsym(false);
            } else {
                Graph.jt_out_console.append("缺少右括号错误："+"Error at "+typeValue.value);
            }
        }
    }

    /**
     * 在名字表中加入一项
     *
     * @param type 类型（变量声明还是分程序声明）
     * @param lev  所在层
     */
    private void enter(Constant.nameTable type, int lev, String identVale,String belong) {
        this.enter(type, lev, 0, identVale,belong);
    }

    /**
     * 在名字表中加入一项
     *
     * @param type         类型（变量声明还是分程序声明）
     * @param lev          所在层
     * @param varAssignPos 变量在该层的相对地址
     */
    private void enter(Constant.nameTable type, int lev, int varAssignPos, String identVale,String belong) {
        TableStruct tableStruct = new TableStruct();
        tableStruct.name = identVale;
        tableStruct.type = type;
        if (type.equals(Constant.nameTable.VARIABLE)) {//是变量声明
            tableStruct.level = lev;
            tableStruct.addr = varAssignPos;//变量地址
        } else if (type.equals(VOID_PROCEDUR)) {//是分程序声明
            tableStruct.level = 0;
            tableStruct.addr = Main.aimCode.size();//分程序地址==当前指针的值
            tableStruct.size = 0;    //初始默认为0
        } else if (type.equals(INT_PROCEDUR)) {//是分程序声明
            tableStruct.level = 0;
            tableStruct.addr = Main.aimCode.size();//分程序地址==当前指针的值
            tableStruct.size = 0;    //初始默认为0
        }
        tableStruct.belong = belong;
        Main.tableStructs.add(tableStruct);
    }
}