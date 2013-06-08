package temmental;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ExprValMessage extends ExprVal {

    protected List<ExprVal> parameters;
    
    ExprValMessage(Template template, String positionInformation, String allMatchStr) {
        super(template, positionInformation, allMatchStr);
    }
    
    public void initialize(Matcher m, int tagOrKeyGroup, int parameterGroup, int filterNamesGroup) throws TemplateException {
        tag_or_key = m.group(tagOrKeyGroup);
        parameters = createParams(positionInformation, allMatchStr, m.group(parameterGroup));
        filterNames = extractFilterNames(m, filterNamesGroup);
        assertFilterNamesExist();
        check();
    }
    
    @Override
    Object render(Writer out, Map<String, ? extends Object> model) throws IOException, TemplateException {
        String propertyKey;
        if (isQuoted()) {
            propertyKey = getTagOrKey();
        } else {
            String key = getTagOrKey();
            if (!isOptional() && model.get(key) == null) {
                throw new TemplateException("Key '%s' is not present or has null value in the model map to render '%s' at position '%s'.", key, allMatchStr, positionInformation);
            } else if (isOptional() && model.get(key) == null) {
                return null;
            }
            propertyKey = model.get(key).toString();
        } 
        if (! template.messages.containsKey(propertyKey)) {
            throw new TemplateException("Key '%s' is not present in the property map to render '%s' at position '%s'.", propertyKey, allMatchStr, positionInformation);
        }
        List<Object> parameters = new ArrayList<Object>();
        boolean rendered = computeEachParamWithTheirFilters(parameters, model);
        if (! rendered) {
            return null;
        }
        try {
            String result = template.messages.format(propertyKey, parameters);
            Object o = applyFilters(result, filterNames);
            if (out != null)
                out.write(o.toString());
            return o;
        } catch (MissingFormatArgumentException e) {
            throw new TemplateException(e, "Unable to format the string '" + template.messages.getString(propertyKey) + "' with parameters " + parameters + " to render '%s' at position '%s'.", allMatchStr, positionInformation);
        }
    }

    protected boolean computeEachParamWithTheirFilters(List<Object> parameters, Map<String, ? extends Object> model) throws TemplateException, IOException {
        for (ExprVal param : this.parameters) {
        	if (! param.isArobase()) {
        		Object o = param.render(null, model);
        		if (o == null)
        			return false;
        		parameters.add(o);
        	} else {
        		String param_tagOrKey = param.getTagOrKey();

        		Object result = model.get(param_tagOrKey);
            	if (! param.isOptional() && result == null) {
            		throw new TemplateException("Key '%s' is not present or has null value in the model map to render '%s' at position '%s'.", param_tagOrKey, allMatchStr, positionInformation);
            	} 
            	
            	if (result == null)
            		return false;

            	if (! result.getClass().isArray() && (! (result instanceof Iterable))) {
            		throw new TemplateException("Key '%s' is not an array or an Iterable object in the model map to render '%s' at position '%s'.", param_tagOrKey, allMatchStr, positionInformation);
            	}
            	
            	
            	if (result.getClass().isArray()) {
            		for (Object o : (Object[]) result) {
            			o = applyFilters(o, param.filterNames);
            			parameters.add(o);
            		}
            	} else {
            		for (Object o : (Iterable) result) {
            			o = applyFilters(o, param.filterNames);
            			parameters.add(o);
            		}
            	}
        	}
        }
        return true;
    }
    
    protected List<ExprVal> createParams(String positionInformation, String allMatchString, String tagsAndFilters) throws TemplateException {
        List<ExprVal> params = new ArrayList<ExprVal>();
        if (tagsAndFilters == null)
            return null;
        Pattern p = Pattern.compile(Template.SIMPLE_EXPR);
        Matcher m = p.matcher(tagsAndFilters);
        while (m.find()) {
            ExprVal valToAdd;            
            String parameters = m.group(Template.se_pg);
            if (parameters == null) {
            	valToAdd = new ExprValTag(template, positionInformation, allMatchString);
            	((ExprValTag) valToAdd).initialize(m, Template.se_tokg, Template.se_fg);
            } else {
            	valToAdd = new ExprValMessage(template, positionInformation, allMatchString);
            	((ExprValMessage) valToAdd).initialize(m, Template.se_tokg, Template.se_pg, Template.se_fg);
            }
            params.add(valToAdd);
        } 
        return params;
    }

    private void check() throws TemplateException {
        if (isQuoted() && isOptional()) {
            // on ne peut pas ecrire qque chose du genre ~'sentence?[]~
            throw new TemplateException("Invalid syntax in '%s' at position '%s' (flags ' and ? are mutually exclusive).", allMatchStr, positionInformation);
        }
        
        if (isQuoted()) {
            // sur ~'sentence[]~, vérifie que sentence est bien dans les properties
            String property = getTagOrKey();
            if (! template.messages.containsKey(property)) {
                throw new TemplateException("Key '%s' is not present in the property map to render '%s' at position '%s'.", property, allMatchStr, positionInformation);
            }
        }
    }
    
    @Override
    public String toString() {
    	return "ExprValMessage " + tag_or_key + " [[ " + parameters + " ]] " + " ## " + filterNames;
    }

}
