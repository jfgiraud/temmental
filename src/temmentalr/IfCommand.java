package temmentalr;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class IfCommand extends Element {

	private Calc expr;
	private List blocks;
	private String position;

	public IfCommand(String position, Calc expr, List blocks) {
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
			throw new TemplateException("The evaluation of 'if' condition is not a boolean at position '%s'", position);
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
				throw new TemplateException(e, "Unable to render block for 'if' command at position '%s'", position);
			}
		}
		return result.toString();
	}

}
