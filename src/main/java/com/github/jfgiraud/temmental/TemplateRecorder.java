package com.github.jfgiraud.temmental;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Recorder for template calls (test case).
 */
public class TemplateRecorder {

    private static boolean recordStatus = false;
    private static Map<String, TemplateRecord> templateRecords = new HashMap<String, TemplateRecord>();

    TemplateRecorder() {
    }

    /**
     * Start or Stop recording
     *
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
    }

    /**
     * Indicates if the calls are recorded
     *
     * @return <code>true</code> if calls are recorded, <code>false</code> otherwise.
     */
    public static boolean isRecording() {
        return recordStatus;
    }

    static synchronized void log(Template template, String sectionName, Map<String, ? extends Object> model) {
        if (recordStatus) {
            if (!templateRecords.containsKey(template.getFilepath())) {
                templateRecords.put(template.getFilepath(), new TemplateRecord());
            }
            TemplateRecord templateCalls = templateRecords.get(template.getFilepath());
            templateCalls.log(sectionName, model, template.getMessages());
        }
    }

    /**
     * Returns the record corresponding to the given template
     *
     * @param filepath the path to the template file
     * @return the record or <code>null</code> if the template has not been created.
     */
    public static TemplateRecord getTemplateRecordFor(String filepath) {
        return templateRecords.get(filepath);
    }

    public static Set<String> getTemplateRecordFilepaths() {
        return templateRecords.keySet();
    }

}
