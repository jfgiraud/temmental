package temmentalr;

/**
 * Specific exception used by the template engine.
 */
@SuppressWarnings("serial")
public class TemplateException extends Exception
{
   
    TemplateException(String format, Object ... params) {
        super(String.format(format, params).replace(" at position ''", ""));
    }
    
    TemplateException(Exception e, String format, Object ... params) {
        super(String.format(format, params).replace(" at position ''", ""), e);
    }

}