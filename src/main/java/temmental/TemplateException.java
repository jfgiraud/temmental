package temmental;

/**
 * Specific exception used by the template engine.
 */
@SuppressWarnings("serial")
public class TemplateException extends Exception
{
   
    TemplateException(String format, String ... params) {
        super(String.format(format, (Object[]) params).replace(" at position ''", ""));
    }
    
    TemplateException(Exception e, String format, String ... params) {
        super(String.format(format, (Object[]) params).replace(" at position ''", ""), e);
    }

}





