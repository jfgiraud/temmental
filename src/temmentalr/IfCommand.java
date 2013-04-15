package temmentalr;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class IfCommand extends Element {

	private Element expr;
	private List blocks;
	private String position;

	public IfCommand(String position, Element expr, List blocks) {
		this.position = position;
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
		Object exprEval = expr.writeObject(functions, model, messages);
		if (exprEval == null) {
			return null;
		}
		if (! (exprEval instanceof Boolean)) {
			throw new TemplateException("The 'if' statement requires a boolean value at position '%s' (receives '%s')", position, exprEval.getClass().getCanonicalName());
		}
		StringWriter result = new StringWriter();
		for (Object block : blocks) {
			try {
				StringWriter sout = new StringWriter();
				Object o = Template.writeObject(sout, functions, model, messages, block);
				if (o != null) {
					result.append(o.toString());
				}
			} catch (IOException e) {
				throw new TemplateException(e, "Unable to render block for IF command at position '%s'", position);
			}
		}
		return result.toString();
	}

}
