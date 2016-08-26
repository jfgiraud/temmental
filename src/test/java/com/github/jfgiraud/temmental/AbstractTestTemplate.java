package com.github.jfgiraud.temmental;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractTestTemplate {

    // ===============================================================================================================================================
    // functions for testing...
    // ===============================================================================================================================================

    Cursor c(int line, int column) {
        return new Cursor(p(line, column));
    }

    String p(int line, int column) {
        return "-:l" + line + ":c" + column;
    }

    List<Object> list(Object... objects) {
        return Arrays.asList(objects);
    }

    Array array(String position, Object... objects) {
        return new Array(Arrays.asList(objects), new Cursor(position));
    }

    BracketTok bracket(char name, String position) {
        return new BracketTok(name, new Cursor(position));
    }

    DefaultFunction or(Element e, Object d) throws TemplateException {
        return new DefaultFunction(e, d, null);
    }

    Identifier identifier(String name, String position) throws TemplateException {
        return new Identifier(name, new Cursor(position));
    }

    Keyword keyword(String name, String position) throws TemplateException {
        return new Keyword(name, new Cursor(position));
    }

    Command command(Keyword keyword, String position, Element element) throws TemplateException {
        return new Command(keyword, new Cursor(position), element);
    }

    Function function(Identifier name, Object input) {
        return new Function(name, input);
    }

    Functionp functionp(Identifier name, List<Object> initParameters, Element input) {
        return new Functionp(name, initParameters, input);
    }

    Message message(Identifier name, List<Object> parameters) {
        return new Message(name, parameters);
    }

    ToApplyTok toapply(String position) {
        return new ToApplyTok(new Cursor(position));
    }

    CommandTok tocommand(String position) {
        return new CommandTok(new Cursor(position));
    }

    CommaTok comma(String position) {
        return new CommaTok(new Cursor(position));
    }

    Expression toparse(String expr, String position) {
        return new Expression(expr, new Cursor(position));
    }

    protected Text text(String name, String position) {
        return new Text(name, new Cursor(position));
    }

    Char character(char c, String position) {
        return new Char(c, new Cursor(position));
    }

    void displayRule(String s) {
        StringWriter swu = new StringWriter();
        StringWriter swd = new StringWriter();
        StringWriter swl = new StringWriter();
        for (int i = 0, u = 0, d = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                u = 0;
                d = 0;
                swu.append(' ');
                swd.append(' ');
                swl.append('*');
                continue;
            } else {
                u = (u + 1) % 10;
            }
            if (u != 0) {
                swu.append(String.valueOf(u).charAt(0));
                swd.append(' ');
                swl.append(' ');
            } else {
                swu.append(String.valueOf(u).charAt(0));
                d += 1;
                swd.append(String.valueOf(d).charAt(0));
                swl.append(' ');
            }
        }
    }

}
