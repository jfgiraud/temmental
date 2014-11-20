package temmental;

class TemplateIgnoreRenderingException extends TemplateException {

    public TemplateIgnoreRenderingException(String format, Object... params) {
        super(format, params);
    }

}
