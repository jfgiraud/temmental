package temmental2;

import temmental2.rpl.*;
import temmental2.rpl.Function;

import java.lang.reflect.Method;
import java.util.*;

public class Reader {

    protected List<Object> operations = new ArrayList<Object>();

    protected Reader(List<Object> operations) {
        this.operations = new ArrayList<Object>(operations);
    }

    public List<Object> getOperations() {
        return operations;
    }

    public String read_until(List<String> tokens, List until) {
        if (tokens.size() == 0) {
            if (until != null) {
                throw new StackException(String.format("Unable to reach token \"%s\".", StringUtils.join("\" or \"", until)));
            }
        }
        String token = tokens.remove(0);
        while (token != null) {
            if (until != null && until.contains(token)) {
                return token;
            }
            if (token.equals("if")) {
                IfCmd ifCmd = new IfCmd();
                ifCmd.read_until(tokens, Arrays.asList("then"));
                ifCmd.tocond();
                String tok = ifCmd.read_until(tokens, Arrays.asList("else", "end"));
                if (tok.equals("else")) {
                    ifCmd.totrue();
                    ifCmd.read_until(tokens, Arrays.asList("end"));
                    ifCmd.tofalse();
                } else {
                    ifCmd.totrue();
                }
                operations.add(ifCmd);
            } else if (token.equals("case")) {
                CaseCmd caseCmd = new CaseCmd();
                String tok = caseCmd.read_until(tokens, Arrays.asList("then", "end"));
                while (tok != null) {
                    if (tok.equals("then")) {
                        caseCmd.tocond();
                        caseCmd.read_until(tokens, Arrays.asList("end"));
                        caseCmd.totrue();
                        caseCmd.append();
                        tok = caseCmd.read_until(tokens, Arrays.asList("then", "end"));
                    } else if (tok.equals("end")) {
                        caseCmd.todefault();
                        break;
                    } else {
                        break;
                    }
                }
                operations.add(caseCmd);
            } else if (token.equals("start")) {
                StartCmd startCmd = new StartCmd();
                String tok = startCmd.read_until(tokens, Arrays.asList("next", "step"));
                if (tok.equals("next")) {
                    startCmd.tonext();
                } else {
                    startCmd.tostep();
                }
                operations.add(startCmd);
            } else if (token.equals("for")) {
                ForCmd forCmd = new ForCmd();
                String tok = forCmd.read_until(tokens, Arrays.asList("next", "step"));
                if (tok.equals("next")) {
                    forCmd.tonext();
                } else {
                    forCmd.tostep();
                }
                operations.add(forCmd);
            } else if (token.equals("while")) {
                WhileCmd whileCmd = new WhileCmd();
                whileCmd.read_until(tokens, Arrays.asList("repeat"));
                whileCmd.tocond();
                whileCmd.read_until(tokens, Arrays.asList("end"));
                whileCmd.toloopst();
                operations.add(whileCmd);
            } else if (token.equals("do")) {
                DoCmd doCmd = new DoCmd();
                doCmd.read_until(tokens, Arrays.asList("until"));
                doCmd.toloopst();
                doCmd.read_until(tokens, Arrays.asList("end"));
                doCmd.tocond();
                operations.add(doCmd);
            } else if (token.equals("->")) {
                LocalProgCmd localProg = new LocalProgCmd();
                localProg.read_until(tokens, Arrays.asList("{"));
                localProg.tovars();
                localProg.read_until(tokens, Arrays.asList("}"));
                localProg.toprog();
                localProg.read_until(tokens, Arrays.asList("}"));
                localProg.toafter();
                tokens.add(0, "}");
                operations.add(localProg);
            } else if (token.equals("{")) {
                Prog pCmd = new Prog();
                pCmd.read_until(tokens, Arrays.asList("}"));
                operations.add(pCmd);
            } else if (Arrays.asList("then", "else", "end", "}").contains(token)) {
                throw new StackException(String.format("Unable to reach token '%s'.", StringUtils.join("' or '", until)));
            } else {
                if (token.matches("^(true|false)$")) {
                    operations.add(new Boolean(token));
                } else if (token.matches("^(-)?\\d+[lL]$")) {
                    operations.add(new Long(token));
                } else if (token.matches("^(-)?\\d+$")) {
                    operations.add(new Integer(token));
                } else if (token.matches("^(-)?((\\d*.)?\\d+?([eE][+-]?\\d+)?|nan|inf)$")) {
                    operations.add(new Float(token));
                } else if (Arrays.asList("depth", "drop", "drop2", "dropn", "dup", "dup2", "dupdup", "dupn", "ndupn", "nip",
                        "over", "pick", "pick3", "roll", "rolld", "rot", "unrot", "keep", "pop", "push", "remove", "swap",
                        "value", "insert", "empty", "clear", "unpick", "tolist", "get",
                        "upper", "lower", "capitalize", "title",
                        "length", "startswith", "endswith", "reverse", "replace",
                        "strip", "lstrip", "rstrip",
                        "split", "rsplit",
                        "add", "sub", "mul", "div",
                        "eq", "ne", "lt", "le", "gt", "ge",
                        "ift", "ifte",
                        "+", "-", "*", "/", "+", "==", "!=", "<", ">", "<=", ">=",
                        "eval",
                        "and", "or", "not", "xor").contains(token)) {
                    Map<String, String> associations = new HashMap<String, String>();
                    associations.put("+", "add");
                    associations.put("-", "sub");
                    associations.put("*", "mul");
                    associations.put("/", "div");
                    associations.put("==", "eq");
                    associations.put("!=", "ne");
                    associations.put("<", "lt");
                    associations.put("<=", "le");
                    associations.put(">", "gt");
                    associations.put(">=", "ge");

                    String original = token;

                    token = associations.containsKey(token) ? associations.get(token) : original;

                    boolean found = false;
                    for (Method m : new RplStack(new ArrayList<Object>()).getClass().getMethods()) {
                        if (m.getName().equals(token))
                            found = true;
                    }
                    if (!found) {
                        throw new StackException("Method '" + token + "' not present");
                    }


                    operations.add(new Function(token, original));
                } else if (token.startsWith("\"") && token.endsWith("\"")) {
                    operations.add(token.substring(1, token.length() - 1));
                } else if (token.startsWith("'") && token.endsWith("'")) {
                    operations.add(new Variable(token.substring(1, token.length() - 1)));
                } else {
                    operations.add(new Variable(token));
                }
            }
            if (tokens.size() > 0) {
                token = tokens.remove(0);
            } else {
                if (until != null) {
                    throw new StackException(String.format("Unable to reach token \"%s\".", StringUtils.join("\" or \"", until)));
                }
                return null;
            }
        }
        return null;
    }


}
