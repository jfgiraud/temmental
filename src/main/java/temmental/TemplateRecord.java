package temmental;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Record of all calls concerning a template (test case).
 * <p/>
 * The calls to the following functions are recorded :
 * <ul>
 * <li><code>template.printSection(out, sectionName, model)</code></li>
 * <li><code>template.printSection(out, sectionName)</code></li>
 * <li><code>template.printFile(out, sectionName)</code></li>
 * <li><code>template.printFile(out, sectionName, model)</code></li>
 * </ul>
 * <p/>
 * These class permits to retrieve models used to display parts of the template.
 *
 * @see TemplateRecorder#getTemplateRecordFor
 */
public class TemplateRecord {

    TemplateRecord() {
    }

    private Map<String, List<PrintCall>> sections = new HashMap<String, List<PrintCall>>();

    private List<String> printedSections = new ArrayList<String>();

    void log(String sectionName, Map<String, ? extends Object> model, TemplateMessages messages) {
        List<PrintCall> section = sections.get(sectionName);
        if (section == null) {
            section = new ArrayList<PrintCall>();
            sections.put(sectionName, section);
        }
        section.add(new PrintCall(model, messages));
        printedSections.add(sectionName);
    }

    /**
     * Returns the first model used on <code>printSection(out, section, model)</code> or <code>printSection(out, section)</code> calls.
     *
     * @param section the section name
     * @return the first model or <code>null</code> if the section has not been displayed
     */
    public PrintCall getFirstPrintCallForSection(String section) {
        List<PrintCall> modelsForSection = getPrintCallsForSection(section);
        return modelsForSection == null ? null : modelsForSection.get(0);
    }

    /**
     * Returns the first model used on <code>printFile(out, section, model)</code> or <code>printFile(out, section)</code> calls.
     *
     * @return the first model or <code>null</code> if the file has not been displayed
     */
    public PrintCall getPrintCallForFile() {
        List<PrintCall> modelsForFile = getPrintCallsForFile();
        return modelsForFile == null ? null : modelsForFile.get(0);
    }

    /**
     * Returns the list of models used on <code>printSection(out, section, model)</code> or <code>printSection(out, section)</code> calls.
     *
     * @param section the section name
     * @return the first model or <code>null</code> if the section has not been displayed
     */
    public List<PrintCall> getPrintCallsForSection(String section) {
        return sections.get(section);
    }

    /**
     * Returns the list of models used on <code>printFile(out, section, model)</code> or <code>printFile(out, section)</code> calls.
     *
     * @return the first model or <code>null</code> if the file has not been displayed
     */
    public List<PrintCall> getPrintCallsForFile() {
        return sections.get("__default_section");
    }

    /**
     * Returns the list of printed section.
     *
     * @return The ordered list of printed section. A section name can be present more than once.
     */
    public List<String> getPrintedSections() {
        return printedSections;
    }

}
