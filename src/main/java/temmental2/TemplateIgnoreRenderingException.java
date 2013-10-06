package temmental2;

class TemplateIgnoreRenderingException extends TemplateException {

    public TemplateIgnoreRenderingException(String format, Object... params) {
        super(format, params);
    }

}
