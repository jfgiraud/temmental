package temmental;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

abstract class ExprVal {
    
    protected Template template;
    protected String tag_or_key;
    protected List<String> filterNames;
    protected String positionInformation;
    protected String allMatchStr;
    protected boolean isArobase;
    
    protected ExprVal(Template template, String positionInformation, String allMatchStr) {
        this.positionInformation = positionInformation;
        this.allMatchStr = allMatchStr;
        this.template = template;
        this.isArobase = false;
    }
    
    abstract Object render(Writer out, Map<String, ? extends Object> model) throws IOException, TemplateException;
    
    public boolean isOptional() {
        return tag_or_key != null && tag_or_key.contains("?");
    }
    
    protected boolean isQuoted() {
        return tag_or_key != null && tag_or_key.contains("'");
    }
    
    protected boolean isArobase() {
        return tag_or_key != null && tag_or_key.contains("@");
    }
    
    String getTagOrKey() {
        return tag_or_key != null ? tag_or_key.replace("?", "").replace("'", "").replace("@", "") : null;
    }
    
    // -------------------------------------------- private visibility -----------------------------------------------
    
    protected List<String> getFilters() {
        return filterNames;
    }
    
    protected List<String> extractFilterNames(Matcher m, int numeroGroup) {
        List<String> filterNames = new ArrayList<String>();
        if (!"".equals(m.group(numeroGroup))) {
            String[] tab = m.group(numeroGroup).substring(1).split(":");
            filterNames = Arrays.asList(tab);
        }
        return filterNames;
    }

    protected Object applyFilters(Object s, List<String> filterNames) throws TemplateException {
        if (filterNames == null || filterNames.size() == 0)
            return s;
        Iterator<String> it = filterNames.iterator();
        if (it.hasNext()) {
            String first = it.next();
            //System.out.println(first);
            ObjectFilter firstFilter = template.filters.get(first);
            s = applyFilter(firstFilter, s, first);

            while (it.hasNext()) {
                String second = it.next();

                //System.out.println(second);
                // traitement
                ObjectFilter secondFilter = template.filters.get(second);

                s = applyFilter(secondFilter, s, second);

                first = second;
                firstFilter = secondFilter;
            };
        }
        return s;
    }
    
    private Object applyFilter(ObjectFilter filter, Object s, String filterName) throws TemplateException {
        Class typeIn = Object.class; 
        boolean isArray = false;
        
        try {
            Method firstMethod = getApply(filter);
            typeIn = firstMethod.getParameterTypes()[0]; 
            
            isArray = typeIn.isArray();
            if (isArray)
                typeIn = typeIn.getComponentType();
            boolean convertToString = typeIn == String.class;
            
            if (! isArray) {
                if (convertToString) {
                    if (s.getClass().isArray()) {
                        throw new TemplateException("Invalid filter chain. Filter '%s' expects '%s%s'. It receives '%s'. Unable to render '%s' at position '%s'.", filterName, typeIn.getCanonicalName(), isArray ? "[]" : "", 
                                s.getClass().getCanonicalName(), allMatchStr, positionInformation);
                    } else {
                        s = filter.apply(s.toString());
                    }
                } else {
                    s = filter.apply(s);
                }
            } else {
                //http://www.java2s.com/Tutorial/Java/0125__Reflection/CreatearraywithArraynewInstance.htm
                Object[] objs = (Object[]) s;
                Object o = Array.newInstance(typeIn, objs.length);
                for (int i = 0; i < objs.length; i++) {
                    Object val = objs[i];
                    if (convertToString) {
                        //System.out.println("hello");
                        Array.set(o, i, val.toString());
                    } else {
                        //System.out.println("bye");
                        Array.set(o, i, val);
                    }
                }
                s = filter.apply(o);
            }
            return s;
        } catch (ClassCastException e) {
            throw new TemplateException("Invalid filter chain. Filter '%s' expects '%s%s'. It receives '%s'. Unable to render '%s' at position '%s'.", filterName, typeIn.getCanonicalName(), isArray ? "[]" : "", s.getClass().getCanonicalName(), allMatchStr, positionInformation);
        } catch (TemplateException e) {
            throw e; 
        } catch (Exception e) {
            throw new TemplateException(e, "Unable to apply filter to render '%s' at position '%s'.", allMatchStr, positionInformation, e.getMessage());
        }
    }

    private Method getApply(ObjectFilter filter) {
        Method[] methods = filter.getClass().getMethods();
        for (Method method : methods) {
            if (method.getName().equals("apply"))
                return method;
        }
        return null;
    }

    protected void assertFilterNamesExist() throws TemplateException {
        boolean multiple = (this instanceof ExprValMultiple);
        
        for (String filterName : filterNames) {
            if (! template.filters.containsKey(filterName)) {
                throw new TemplateException("Unknown filter name '%s' at position '%s' to render '%s'.", filterName, positionInformation, allMatchStr);
            }
        }

        String where = "";
        Iterator<String> it = filterNames.iterator();
        if (it.hasNext()) {
            String first = it.next();
            ObjectFilter firstFilter = template.filters.get(first);

            Method firstMethod = getApply(firstFilter);
            Class firstOut = firstMethod.getReturnType();
            Class firstIn = firstMethod.getParameterTypes()[0]; 

            if (multiple ^ firstIn.isArray()) {
                String expect = firstIn.getCanonicalName();
                String but = multiple ? "array" : "object";
                throw new TemplateException("Invalid filter chain at position '%s'. Unable to render '%s'. Filter '%s' expects '%s' but will receive an %s.", positionInformation, allMatchStr, first, expect, but);
            }


            while (it.hasNext()) {
                String second = it.next();

                // traitement
                ObjectFilter secondFilter = template.filters.get(second);

                Class nextIn = getApply(secondFilter).getParameterTypes()[0];
                
                //System.out.println("" + firstOut + " -> " + nextIn);
                
                boolean firstOutArray = firstOut.isArray();
                boolean nextInArray = nextIn.isArray();

                //System.out.println("" + firstOutArray + " -> " + nextInArray);
                //System.out.flush();
                boolean ok = ! (firstOutArray ^ nextInArray) && ((Class<?>)nextIn).isAssignableFrom(((Class<?>)firstOut));

                if (! ok && nextIn != String.class) {
                    String outClass = ((Class<?>)firstOut).getCanonicalName();
                    String inClass = ((Class<?>)nextIn).getCanonicalName();
                    throw new TemplateException("Invalid filter chain at position '%s'. Unable to render '%s'. Filter '%s' produces '%s'. Filter '%s' expects '%s'.", positionInformation, allMatchStr, first, outClass, second, inClass);
                }

                firstFilter = secondFilter;
                first = second;
                
                /*firstOut = nextIn;
                firstOutArray = nextInArray;*/
                firstMethod = getApply(firstFilter);
                firstOut = firstMethod.getReturnType();
                firstOutArray = firstOut.isArray();
                
            };
        }
    }

    public Object compute(Map<String, ? extends Object> model) throws IOException, TemplateException {
        return render(null, model);
    }
    
}