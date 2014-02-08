package temmental2;

import java.util.Map;

public class PrintCall {

    private Map<String, ? extends Object> model;
    private TemplateMessages messages;

    public PrintCall(Map<String, ? extends Object> model, TemplateMessages messages) {
        this.model = model;
        this.messages = messages;
    }

    public Map<String,? extends Object> getModel() {
        return model;
    }
}
