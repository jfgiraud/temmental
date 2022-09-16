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

    public static TemplateMessages empty() {
        return createFrom(new Properties());
    }

    public static TemplateMessages createFrom(Properties ... properties) {
        return createFrom(Locale.getDefault(), properties);
    }

    public static TemplateMessages createFrom(Locale locale, Properties ... properties) {
        TemplateMessagesBuilder builder = new TemplateMessagesBuilder(locale);
        for (Properties p : properties) {
            builder.add(p);
        }
        return builder.build();
    }

    public static TemplateMessages createFrom(String ... resourcePaths) throws IOException {
        return createFrom(Locale.getDefault(), resourcePaths);
    }

    public static TemplateMessages createFrom(Locale locale, String ... resourcePaths) throws IOException {
        TemplateMessagesBuilder builder = new TemplateMessagesBuilder(locale);
        for (String p : resourcePaths) {
            builder.add(p);
        }
        return builder.build();
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
            messageContainer.getString(key);
            return true;
        } catch (MissingResourceException e) {
            return false;
        }
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
