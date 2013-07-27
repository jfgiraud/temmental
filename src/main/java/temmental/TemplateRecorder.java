package temmental;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Recorder for template calls (test case).   
 */
public class TemplateRecorder {

    private static boolean recordStatus = false;
    private static Map<String, TemplateRecord> templateRecords = new HashMap<String, TemplateRecord>();
    private static Map<String, TemplateMessages> templateDatas = new HashMap<String, TemplateMessages>();
    
    TemplateRecorder() {
    }
    
    /**
     * Start or Stop recording
     * @param status recording status. <code>true</code> to start recording, <code>false</code> to stop recording.
     */
    public static void setRecording(boolean status) {
        recordStatus = status;
        if (status) {
            clear();
        }
    }

    /**
     * Clear the cache of all calls.
     */
    public static void clear() {
        templateRecords.clear();
        templateDatas.clear();
    }
    
    /**
     * Indicates if the calls are recorded
     * @return <code>true</code> if calls are recorded, <code>false</code> otherwise.
     */
    public static boolean isRecording() {
        return recordStatus;
    }

    static synchronized void log(Template template, String sectionName, Map<String, ? extends Object> model) {
        if (recordStatus) {
            if (! templateRecords.containsKey(template.filepath)) {
                templateRecords.put(template.filepath, new TemplateRecord());
                templateDatas.put(template.filepath, template.messages);
            }
            TemplateRecord templateCalls = templateRecords.get(template.filepath);
            templateCalls.log(sectionName, model);
        }
    }

    /**
     * Returns the record corresponding to the given template
     * @param filepath the path to the template file
     * @return the record or <code>null</code> if the template has not been created. 
     */
    public static TemplateRecord getTemplateRecordFor(String filepath) {
        return templateRecords.get(filepath);
    }

    /**
     * Returns the messages corresponding to the given template
     * @return the messages or <code>null</code> if the template has not been created. 
     */
    public static Set<String> getTemplateMessagesFilepaths() {
        return templateDatas.keySet();
    }
    
    public static Set<String> getTemplateRecordFilepaths() {
    	return templateRecords.keySet();
    }

}
