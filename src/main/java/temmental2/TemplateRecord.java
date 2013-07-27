package temmental2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Record of all calls concerning a template (test case). 
 * 
 * The calls to the following functions are recorded :
 * <ul>
 * <li><code>template.printSection(out, sectionName, model)</code></li>
 * <li><code>template.printSection(out, sectionName)</code></li>
 * <li><code>template.printFile(out, sectionName)</code></li>
 * <li><code>template.printFile(out, sectionName, model)</code></li>
 * </ul>
 * 
 * These class permits to retrieve models used to display parts of the template.
 * 
 * @see TemplateRecorder#getTemplateRecordFor
 */
public class TemplateRecord {

    TemplateRecord() {
    }
    
    private Map<String, List<Map<String, ? extends Object>>> sections = new HashMap<String, List<Map<String,? extends Object>>>();
    private List<String> printedSections = new ArrayList<String>();

    void log(String sectionName, Map<String, ? extends Object> model) {
        List<Map<String, ? extends Object>> section = sections.get(sectionName);
        if (section == null) {
            section = new ArrayList<Map<String, ? extends Object>>();
            sections.put(sectionName, section);         
        }
        section.add(new HashMap<String, Object>(model));
        printedSections.add(sectionName);
    }

    /**
     * Returns the first model used on <code>printSection(out, section, model)</code> or <code>printSection(out, section)</code> calls.
     * @param section the section name
     * @return the first model or <code>null</code> if the section has not been displayed
     */
    public Map<String, ? extends Object> getModelForSection(String section) {
        List<Map<String, ? extends Object>> modelsForSection = getModelsForSection(section);
        return modelsForSection == null ? null : modelsForSection.get(0);
    }

    /**
     * Returns the first model used on <code>printFile(out, section, model)</code> or <code>printFile(out, section)</code> calls.
     * @return the first model or <code>null</code> if the file has not been displayed
     */
    public Map<String, ? extends Object> getModelForFile() {
        List<Map<String, ? extends Object>> modelsForFile = getModelsForFile();
        return modelsForFile == null ? null : modelsForFile.get(0);
    }

    /**
     * Returns the list of models used on <code>printSection(out, section, model)</code> or <code>printSection(out, section)</code> calls.    
     * @param section the section name
     * @return the first model or <code>null</code> if the section has not been displayed
     */
    public List<Map<String, ? extends Object>> getModelsForSection(String section) {
        return sections.get(section);
    }
    
    /**
     * Returns the list of models used on <code>printFile(out, section, model)</code> or <code>printFile(out, section)</code> calls.
     * @return the first model or <code>null</code> if the file has not been displayed
     */
    public List<Map<String, ? extends Object>> getModelsForFile() {
        return sections.get("__default_section");
    }

    /**
     * Returns the list of printed section. 
     * @return The ordered list of printed section. A section name can be present more than once. 
     */
    public List<String> getPrintedSections() {
        return printedSections;
    }

}
