package temmentalr;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class Command extends Element {

	private Element expr;
	private List blocks;
	private String position;
	private String command;

	public Command(String position, String command, Element expr, List blocks) {
		this.position = position;
		this.command = command;
		this.expr = expr;
		this.blocks = blocks;
	}

	@Override
	String getIdentifier() {
		return "if";
	}

	@Override
	String getPosition() {
		return position;
	}

	@Override
	Object writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		if (command.equals("if")) {
			return if_writeObject(functions, model, messages);
		} else if (command.equals("for")) {
			return for_writeObject(functions, model, messages);
		} else {
			throw new TemplateException("Unsupported '%s' command", command);
		}
	}
	
	private Object if_writeObject(Map<String, Transform> functions, Map<String, Object> model, TemplateMessages messages) throws TemplateException {
		Object exprEval = expr.writeObject(functions, model, messages);
		if (exprEval == null) {
			return null;
		}
		if (! (exprEval instanceof Boolean)) {
			throw new TemplateException("The 'if' statement requires a boolean value at position '%s' (receives '%s')", position, exprEval.getClass().getCanonicalName());
		}
		if (((Boolean)exprEval).booleanValue()) {
			StringWriter result = new StringWriter();
			for (Object block : blocks) {
				Object o = Template.writeObject(functions, model, messages, block);
				if (o != null) {
					result.append(o.toString());
				}
			}
			return result.toString();
		} else {
			return null;
		}
	}
	
	private Object for_writeObject(Map<String, Transform> functions, Map<String, Object> parentModel, TemplateMessages messages) throws TemplateException {
		Object exprEval = expr.writeObject(functions, parentModel, messages);
		if (exprEval == null) {
			return null;
		}
		if (! (exprEval instanceof Iterable)) {
			throw new TemplateException("The 'for' statement requires an iterable value at position '%s' (receives '%s')", position, exprEval.getClass().getCanonicalName());
		}
		Iterator iterator = ((Iterable) exprEval).iterator();
		StringWriter result = new StringWriter();
		while (iterator.hasNext()) {
			Map model = new HashMap(parentModel);
			model.putAll((Map) iterator.next());
			for (Object block : blocks) {
				Object o = Template.writeObject(functions, model, messages, block);
				if (o != null) {
					result.append(o.toString());
				}
			}
		}
		return result.toString();
	}

	@Override
	public String toString() {
		return "if{" + expr + "}" + blocks;
	}
	
}
