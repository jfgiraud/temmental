package temmental;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

class ExprValTag extends ExprVal {
    
	ExprValTag(Template template, String positionInformation, String allMatchStr) {
        super(template, positionInformation, allMatchStr);
    }
    
    public void initialize(Matcher m, int tagOrKeyGroup, int filterNamesGroup) throws TemplateException {
        tag_or_key = m.group(tagOrKeyGroup);
        filterNames = extractFilterNames(m, filterNamesGroup);
        assertFilterNamesExist();
        check();
    }
    
    @Override
    Object render(Writer out, Map<String, ? extends Object> model) throws IOException, TemplateException {
    	Object result = model.get(getTagOrKey());
    	if (! isOptional() && result == null) {
    		throw new TemplateException("Key '%s' is not present or has null value in the model map to render '%s' at position '%s'.", getTagOrKey(), allMatchStr, positionInformation);
    	} else if (result != null) {
    		result = applyFilters(result, filterNames);
    		if (out != null)
    			out.write(result.toString());
    		return result;
    	} else {
    		return null;
    	}
    }
    
    private void check() throws TemplateException {
        if (isQuoted()) {
            // on ne peut pas ecrire qque chose du genre ~'sentence~
            throw new TemplateException("Invalid syntax in '%s' at position '%s' (flag ' requires []).", allMatchStr, positionInformation);
        }
    }

    @Override
    public String toString() {
    	return "ExprValTag " + tag_or_key;
    }
    
}