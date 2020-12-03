package com.github.jfgiraud.temmental;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Wrapper to ResourceBundle or Properties. It is used for internationalization.
 */
public class TemplateMessages {

    private List<Object> messagesContainers;
    private Locale locale;

    TemplateMessages(Locale locale, List<Object> messagesContainers) {
        this.locale = locale;
        this.messagesContainers = messagesContainers;
    }

    public static TemplateMessages createFrom(Properties properties) {
        return new TemplateMessagesBuilder(Locale.getDefault()).add(properties).build();
    }

//    public static TemplateMessages createFrom(Locale locale, Properties properties) {
//        return new TemplateMessagesBuilder(locale).add(properties).build();
//    }

    public static TemplateMessages createFrom(String resourcePath) throws IOException {
        return new TemplateMessagesBuilder(Locale.getDefault()).add(resourcePath).build();
    }

    public static TemplateMessages createFrom(Locale locale, String resourcePath) throws IOException {
        return new TemplateMessagesBuilder(locale).add(resourcePath).build();
    }

    /**
     * Tests if the specified key is known in messages.
     *
     * @param key possible key
     * @return <code>true</code> if and only if the specified object is a key in this messages; <code>false</code> otherwise.
     */
    public boolean containsKey(String key) {
        for (int i = messagesContainers.size() - 1; i >= 0; i--) {
            Object messageContainer = messagesContainers.get(i);
            if (messageContainer instanceof Properties && ((Properties) messageContainer).containsKey(key))
                return true;
            if (messageContainer instanceof ResourceBundle && containsKey((ResourceBundle) messageContainer, key)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsKey(ResourceBundle messageContainer, String key) {
        try {
            if (messageContainer.getString(key) != null)
                return true;
        } catch (MissingResourceException e) {
        }
        return false;
    }

    /**
     * Searches the message with the specified key in messages. If the key is not found, returns <code>null</code>.
     *
     * @param key the message key
     * @return the value in the messages with the specified key value.
     */

    public String getString(String key) {
        for (int i = messagesContainers.size() - 1; i >= 0; i--) {
            Object messageContainer = messagesContainers.get(i);
            if (messageContainer instanceof Properties && ((Properties) messageContainer).containsKey(key))
                return ((Properties) messageContainer).getProperty(key);
            if (messageContainer instanceof ResourceBundle && containsKey((ResourceBundle) messageContainer, key))
                return ((ResourceBundle) messageContainer).getString(key);

        }
        return null;
    }

    /**
     * Format the message with the specified key in messages with the specified parameters
     *
     * @param key        the message key
     * @param parameters the parameters
     * @return the formatted message
     * @see java.lang.String#format(String, Object...)
     */
    public String format(String key, Object[] parameters) {
        return (new MessageFormat(getString(key), locale)).format(parameters, new StringBuffer(), null).toString();
    }


}
