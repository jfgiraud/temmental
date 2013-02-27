package temmental;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

class ExprValMultiple extends ExprValMessage {

    ExprValMultiple(Template template, String positionInformation, String allMatchStr) {
        super(template, positionInformation, allMatchStr);
    }
    
    public void initialize(Matcher m, int parameterGroup, int filterNamesGroup) throws TemplateException {
        tag_or_key = null;
        parameters = createParams(positionInformation, allMatchStr, m.group(parameterGroup));
        filterNames = extractFilterNames(m, filterNamesGroup);
        assertFilterNamesExist();
        check();
    }
    
    @Override
    Object render(Writer out, Map<String, ? extends Object> model) throws IOException, TemplateException {
        List<Object> parameters = new ArrayList<Object>();
        boolean rendered = computeEachParamWithTheirFilters(parameters, model);
        if (! rendered) {
            return null;
        }
        
        Object[] result = parameters.toArray(new Object[1]);
        Object o = applyFilters(result, filterNames);
        if (out != null)
            out.write(o.toString());
        return o;
    }
    
    private void check() throws TemplateException {
    }
    
    @Override
    public String toString() {
    	return "ExprValMultiple [[ " + parameters + " ]] " + " ## " + filterNames;
    }
}
