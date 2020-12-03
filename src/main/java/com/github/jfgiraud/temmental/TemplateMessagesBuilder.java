package com.github.jfgiraud.temmental;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TemplateMessagesBuilder {

    private final Locale locale;
    private List<Object> messagesContainers;

    public TemplateMessagesBuilder(Locale locale) {
        this.locale = locale;
        this.messagesContainers = new ArrayList<Object>();
    }

    public TemplateMessagesBuilder add(String resourcePath) throws IOException {
        messagesContainers.add(readResource(resourcePath, locale));
        return this;
    }

    public TemplateMessagesBuilder add(Properties properties) {
        this.messagesContainers.add(properties);
        return this;
    }

    public TemplateMessagesBuilder add(ResourceBundle bundle) {
        this.messagesContainers.add(bundle);
        return this;
    }

    public TemplateMessages build() {
        return new TemplateMessages(locale, messagesContainers);
    }

    private Object readResource(String resourcePath, Locale locale) throws TemplateException, IOException {
        String protocol = resourcePath.substring(0, resourcePath.indexOf(":"));
        resourcePath = resourcePath.substring(resourcePath.indexOf(":") + 1);
        if (protocol.equals("classpath")) {
            try {
                return ResourceBundle.getBundle(resourcePath, locale);
            } catch (MissingResourceException e) {
                TemplateException te = new TemplateException("Can't find bundle for %s, locale %s", resourcePath, locale.toString());
                try {
                    return ResourceBundle.getBundle(resourcePath, Locale.getDefault());
                } catch (MissingResourceException e2) {
                    throw te;
                }
            }
        } else if (protocol.equals("file")) {
            List<String> tab = new ArrayList<String>();
            populateResourcesPossibilities(tab, resourcePath, locale);
//            System.out.println("locale="+locale);
            populateResourcesPossibilities(tab, resourcePath, Locale.getDefault());
//            System.out.println("locale="+Locale.getDefault());
            tab.add(resourcePath);
            String found = null;
            for (String path : tab) {
                File f = new File(path);
//                System.out.println(path);
                if (f.exists()) {
                    found = path;
                    break;
                }
            }
            if (found == null)
                throw new TemplateException("Can't find properties file for %s, locale %s", resourcePath, locale.toString());
            return TemplateUtils.readProperties(found);
        } else {
            throw new TemplateException("Protocol '%s' not supported! (%s)", protocol, resourcePath);
        }
    }

    private void populateResourcesPossibilities(List<String> tab, String resourcePath, Locale locale) {
        String suffix = resourcePath.substring(resourcePath.lastIndexOf("."));
        String prefix = resourcePath.substring(0, resourcePath.lastIndexOf("."));
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if (!language.equals("")) {
            if (!country.equals(""))
                tab.add(prefix + "_" + language + "_" + country + suffix);
            tab.add(prefix + "_" + language + suffix);
        }
    }

}
