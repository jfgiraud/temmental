package com.github.jfgiraud.temmental;

import org.junit.Before;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.github.jfgiraud.temmental.TemplateMessages.createFrom;

public abstract class AbstractTestElement extends AbstractTestTemplate {

    protected Map<String, Object> model;
    protected Map<String, Object> transforms;
    protected Properties properties;
    protected TemplateMessages messages;

    @Before
    public void setUp() throws Exception {
        model = new HashMap<String, Object>();
        transforms = new HashMap<String, Object>();
        properties = new Properties();
        messages = createFrom(properties);
    }

    public void populateModel(String key, Object value) {
        model.put(key, value);
    }

    public void unpopulateModel(String key) {
        model.remove(key);
    }

    public void populateTransform(String key, Transform value) {
        transforms.put(key, value);
    }

    public void populateTransform(String key, final Method method) {
        transforms.put(key, method);
    }

    public void populateProperty(String key, Object value) {
        properties.put(key, value);
    }

    public void unpopulateProperty(String key) {
        properties.remove(key);
    }

}
