package temmental2;

/**
 * Specific exception used by the template engine.
 */
@SuppressWarnings("serial")
public class TemplateException extends RuntimeException {

    public TemplateException(String format, Object... params) {
        super(String.format(format, params).replace(" at position ''", ""));
    }

    public TemplateException(Exception e, String format, Object... params) {
        super(String.format(format, params).replace(" at position ''", ""), e);
    }

}