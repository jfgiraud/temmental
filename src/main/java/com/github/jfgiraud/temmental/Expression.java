package com.github.jfgiraud.temmental;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Expression {

    private String expr;
    private Cursor cursor;
    private boolean betweenTildes;
    private Object currentToken = null;

    Expression(String expr, Cursor cursor, boolean betweenTildes) {
        this.expr = expr;
        this.cursor = cursor.clone();
        this.betweenTildes = betweenTildes;
    }

    Expression(String expr, Cursor cursor) {
        this(expr, cursor, true);
    }

    public Object parse() throws IOException, TemplateException {
        Stack tokens = parseToTokens();
        tokens.reverse();
        try {
            return interpretTokens(tokens);
        } catch (ClassCastException e) {
            if (currentToken instanceof Token)
                throw new TemplateException(e, "Parsing exception at position %s.", ((Token)currentToken).getCursor().getPosition());
            else
                throw e;
        }
    }

    @Override
    public String toString() {
        return "@" + cursor.getPosition() + "\tExpression(" + expr + ")";
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof Expression))
            return false;
        Expression oc = (Expression) o;
        return oc.expr.equals(expr) && oc.cursor.equals(cursor);
    }

    // ====== static methods ======================================================================================

    Object interpretTokens(Stack tokens) throws TemplateException {
        Stack oldOut = new Stack();
        Stack oldCommas = new Stack();
        int commas = 0;
        Stack out = new Stack();
        //tokens.printStack(System.out);
        Map<Integer, Integer> brackets = new HashMap<Integer, Integer>();
        brackets.put((int)'(', 0);
        brackets.put((int)'[', 0);
        brackets.put((int)'<', 0);
        brackets.put((int)'!', 0);
        while (tokens.depth() >= 1) {
            Object token = tokens.pop();
            currentToken = token;
            if (isLeafToken(token)) {
                out.push(token);
            } else if (token instanceof BracketTok) {
                BracketTok b = (BracketTok) token;
                if (b.isOpening()) {
                    brackets.put(b.getBracket(), brackets.get(b.getBracket())+1);
                    oldOut.push(out);
                    out = new Stack();
                    out.push(token);
                    oldCommas.push(commas);
                    commas = 0;
                } else {
                    brackets.put(b.neg(), brackets.get(b.neg())-1);
                    BracketTok other = (BracketTok) out.value(out.depth());
                    if (other.getBracket() != b.neg()) {
                        throw new TemplateException("Corresponding bracket for '%c' at position '%s' is invalid (found '%c' at position '%s').", b.getBracket(), b.getCursor().getPosition(),
                                other.getBracket(), other.getCursor().getPosition());
                    }
                    out.remove(out.depth());
                    if (b.getBracket() != '¡' && commas == 0 && commas != out.depth() - 1) {
                        if (b.getBracket() == '>') {
                            throw new TemplateException("Empty init list parameter before '%c' at position '%s'.", b.getBracket(), b.getCursor().getPosition());
                        } else if (b.getBracket() != ']') {
                            throw new TemplateException("Empty list parameter before '%c' at position '%s'.", b.getBracket(), b.getCursor().getPosition());
                        }
                    }

                    if (b.getBracket() == '>') {
                        if (commas != out.depth() - 1) {
                            throw new TemplateException("No parameter before '%c' at position '%s'.", b.getBracket(), b.getCursor().getPosition());
                        }
                        out.tolist(out.depth());
                        List initParameters = (List) out.pop();
                        out = (Stack) oldOut.pop();
                        if (out.value() instanceof  Command) {
                            Command command = (Command) out.pop();
                            if (! command.allowParameters(initParameters.size())) {
                                throw new TemplateException("Invalid syntax on closing bracket '%c' at position '%s'. " +
                                        "Bad number of parameter for command '%s'.",
                                        b.getBracket(), b.getCursor().getPosition(),
                                        command.getKeyword().getKeyword());

                            }
                            command.setVarname((Element) initParameters.get(0));
                            out.push(command);
                            commas = (Integer) oldCommas.pop();
                        } else {
                            if (!(out.value() instanceof Function)) {
                                throw new TemplateException("Invalid syntax on closing bracket '%c' at position '%s'. " +
                                        "Expects '%s' token but receives '%s' token.",
                                        b.getBracket(), b.getCursor().getPosition(),
                                        Function.class.getCanonicalName(),
                                        out.value().getClass().getCanonicalName());
                            }
                            Function func = (Function) out.pop();
                            out.push(new Functionp(func, initParameters));
                            commas = (Integer) oldCommas.pop();
                        }
                    } else if (b.getBracket() == ']') {
                        if ((out.depth() != 0) && (commas != out.depth() - 1)) {
                            throw new TemplateException("No parameter before '%c' at position '%s'.", b.getBracket(), b.getCursor().getPosition());
                        }
                        out.tolist(out.depth());
                        List msgParameters = (List) out.pop();
                        out = (Stack) oldOut.pop();
                        if (!(out.value() instanceof Identifier)) {
                            throw new TemplateException("Invalid syntax on closing bracket '%c' at position '%s'. " +
                                    "Expects '%s' token but receives '%s' token.",
                                    b.getBracket(), b.getCursor().getPosition(),
                                    Identifier.class.getCanonicalName(),
                                    out.value().getClass().getCanonicalName());
                        }
                        Identifier messageIdentifier = (Identifier) out.pop();
                        out.push(new Message(messageIdentifier, msgParameters));
                        commas = (Integer) oldCommas.pop();
                    } else if (b.getBracket() == ')') {
                        if (commas != out.depth() - 1) {
                            throw new TemplateException("No parameter before '%c' at position '%s'.", b.getBracket(), b.getCursor().getPosition());
                        }
                        out.tolist(out.depth());
                        out.push(new Array((List<Object>) out.pop(), other.getCursor()));
                        commas = (Integer) oldCommas.pop();
                    } else if (b.getBracket() == '¡') {
                        Object def = ! out.empty() ? out.pop() : null;
                        out = (Stack) oldOut.pop();
                        Element input = (Element) out.pop();
                        out.push(new DefaultFunction(input, def, other.getCursor()));
                        commas = (Integer) oldCommas.pop();
                    } else {
                        throw new TemplateException("BracketTok %c not supported at position '%s'!", b.getBracket(), b.getCursor().getPosition());
                    }
                }
            } else if (token instanceof ToApplyTok) {
                Identifier filter = (Identifier) tokens.pop();
                Object input = out.pop();
                out.push(new Function(filter, input));
            } else if (token instanceof CommaTok) {
                commas += 1;
            } else if (token instanceof CommandTok) {
                Keyword keyword = (Keyword) tokens.pop();
                if (out.empty()) {
                    out.push(new Command(keyword, ((CommandTok) token).getCursor()));
                } else {
                    Element input = (Element) out.pop();
                    out.push(new Command(keyword, ((CommandTok) token).getCursor(), input));
                }
            } else {
                throw new TemplateException("Case " + token.getClass().getCanonicalName() + " not supported");
            }
        }
        if (out.depth() > 1) {
            String r = "";
            for (int c : brackets.keySet()) {
                if (brackets.get(c) != 0) {
                    r += (r.equals("") ? "" : ", ") + String.format("'%c'=%d", c, brackets.get(c));
                }
            }
            if (r.length() == 0) {
                throw new TemplateException("Too much objects in the stack for expression '%s' at position '%s'.", expr, cursor.getPosition());
            } else {
                throw new TemplateException("Too much objects in the stack for expression '%s' at position '%s'. One or more tags are not closed (%s).", expr, cursor.getPosition(), r);
            }
        } else if (out.empty()) {
            throw new TemplateException("Not enough object in the stack for expression '%s' at position '%s'.", expr, cursor.getPosition());
        }
        return out.pop();
    }

    private boolean isLeafToken(Object token) {
        return token instanceof Char || token instanceof Text || token instanceof Number || token instanceof Boolean || token instanceof Identifier;
    }

    Stack parseToTokens() throws IOException, TemplateException {
        Stack stack = new Stack();
        String expression = expr;
        Cursor cursor = this.cursor.clone();
        if (betweenTildes) {
            if (!expression.startsWith("~")) {
                throw new TemplateException("Expression '%s' doesn't start with '~' character at position '%s'", expression, cursor.getPosition());
            }
            if (!expression.endsWith("~")) {
                throw new TemplateException("Expression '%s' doesn't end with '~' character at position '%s'", expression, cursor.getPosition());
            }
            expression = expression.substring(1);
            expression = expression.substring(0, expression.length() - 1);
            cursor.move1r();
        }
        StringReader sr = new StringReader(expression);
        StringWriter word = new StringWriter();
        boolean inDQ = false;
        boolean inSQ = false;
        boolean isProg = false;
        boolean escape = false;
        boolean afterHash = false;
        try {
            int currentChar = sr.read();
            while (currentChar != -1) {
                cursor.next(currentChar);
                if (!inSQ && !inDQ && currentChar == '\\') {
                    throw new TemplateException("Invalid escape char '%c' at position '%s'.", currentChar, cursor.getPosition(-1));
                } else if ((inSQ || inDQ) && currentChar == '\\') {
                    escape = true;
                    cursor.move1l();
                } else if (escape) {
                    word.write(currentChar);
                    escape = false;
                } else if (!inSQ && !inDQ && currentChar == '#') {
                    String expr = word.toString();
                    if (!expr.equals("")) {
                        stack.push(evalToken(expr, cursor.clone().move1l(), afterHash));
                    } else {
                        behaviourOnEmptyToken(currentChar, stack, cursor);
                    }
                    word = new StringWriter();
                    stack.push(new CommandTok(cursor.clone().move1l()));
                    afterHash = true;
                } else if (!inSQ && !inDQ && currentChar == ':') {
                    String expr = word.toString();
                    if (!expr.equals("")) {
                        stack.push(evalToken(expr, cursor.clone().move1l(), afterHash));
                    } else {
                        behaviourOnEmptyToken(currentChar, stack, cursor);
                    }
                    word = new StringWriter();
                    stack.push(new ToApplyTok(cursor.clone().move1l()));
                    afterHash = false;
                } else if (!inSQ && !inDQ && BracketTok.isBracket(currentChar)) {
                    String expr = word.toString();
                    if (!expr.equals("")) {
                        stack.push(evalToken(expr, cursor.clone().move1l(), afterHash));
                    } else {
                        behaviourOnEmptyToken(currentChar, stack, cursor);
                    }
                    word = new StringWriter();
                    stack.push(new BracketTok(currentChar, cursor.clone().move1l()));
                    afterHash = false;
                } else if (!inSQ && !inDQ && currentChar == ',') {
                    String expr = word.toString();
                    if (!expr.equals("")) {
                        stack.push(evalToken(expr, cursor.clone().move1l(), afterHash));
                    } else {
                        behaviourOnEmptyToken(currentChar, stack, cursor);
                    }
                    word = new StringWriter();
                    stack.push(new CommaTok(cursor.clone().move1l()));
                    afterHash = false;
                } else if (!inSQ && !inDQ && currentChar == '"') {
                    inDQ = true;
                    word.write(currentChar);
                } else if (!inSQ && !inDQ && currentChar == '\'') {
                    inSQ = true;
                    word.write(currentChar);
                } else if (inSQ && currentChar == '\'') {
                    inSQ = false;
                    word.write(currentChar);
                } else if (inDQ && currentChar == '"') {
                    inDQ = false;
                    word.write(currentChar);
                } else if (inDQ || inSQ) {
                    word.write(currentChar);
                } else {
                    word.write(currentChar);
                }
                if (inSQ && word.toString().length() == 3) {
                    inSQ = false;
                }
                currentChar = sr.read();
            }
        } finally {
            sr.close();
        }
        String expr = word.toString();
        if (!expr.equals("")) {
            stack.push(evalToken(expr, cursor.clone(), afterHash));
        } else {
            behaviourOnEmptyToken(0, stack, cursor);
        }

        return stack;
    }

    private static void behaviourOnEmptyToken(int currentChar, Stack stack, Cursor cursor) throws TemplateException {
        if (currentChar == ',') {
            throw new TemplateException("No parameter before ',' at position '%s'.", cursor.getPosition(-1));
        } else if (currentChar == ':') {
            if (stack.empty())
                throw new TemplateException("No identifier before ':' at position '%s'.", cursor.getPosition(-1));
            if (stack.value() instanceof BracketTok) {
                BracketTok b = (BracketTok) stack.value();
                if (b.isOpening()) {
                    throw new TemplateException("No parameter before ':' at position '%s'.", cursor.getPosition(-1));
                } else if (! b.isClosing()) {
                    throw new TemplateException("No token at position '%s'.", cursor.getPosition(-1));
                }
            } else if (stack.value() instanceof CommaTok) {
                throw new TemplateException("No parameter before ':' at position '%s'.", cursor.getPosition(-1));
            }
        } else if (currentChar == '[' || currentChar == '<') {
            if (stack.empty() || ! (stack.value() instanceof BracketTok && ((BracketTok) stack.value()).getBracket() == '¡'))
                throw new TemplateException("No identifier before '%c' at position '%s'.", currentChar, cursor.getPosition(-1));

        }
    }

    public static Object evalToken(String expr, Cursor cursor, boolean allowKeyword) throws TemplateException {
        if (expr.startsWith("\"")) {
            if (!expr.endsWith("\"")) {
                throw new TemplateException("Sentence not closed at position '%s').", cursor.getPosition());
            }
            Cursor c = cursor.clone().movel(expr, 0);
            String t = expr.substring(1); //c.move1r();
            t = t.substring(0, t.length() - 1);
            return new Text(t, c);
        } else if (expr.startsWith("'") && expr.endsWith("'")) {
            Cursor c = cursor.clone().movel(expr, 0);
            String t = expr.substring(1); //c.move1r();
            t = t.substring(0, t.length() - 1);
            if (t.length() == 0) {
                throw new TemplateException("Empty char at position '%s').", cursor.getPosition());
            } else if (t.length() > 1) {
                throw new TemplateException("Invalid length for char at position '%s').", cursor.getPosition());
            }
            return new Char(t.charAt(0), c);
        } else if (expr.matches("(-)?\\d+[lL]")) {
            return Long.parseLong(expr.substring(0, expr.length() - 1));
        } else if (expr.matches("(-)?\\d+")) {
            return Integer.parseInt(expr);
        } else if (expr.matches("(-)?(\\d*.)?\\d+?([eE][+-]?\\d+)?[dD]?")) {
            return Double.parseDouble(expr);
        } else if (expr.matches("(-)?(\\d*.)?\\d+?([eE][+-]?\\d+)?[fF]")) {
            return Float.parseFloat(expr);
        } else if (allowKeyword) {
            return new Keyword(expr, cursor.clone().movel(expr, 0));
        } else if (expr.matches("true|false")) {
            return Boolean.parseBoolean(expr);
        }
        return new Identifier(expr, cursor.clone().movel(expr, 0));
    }

}
