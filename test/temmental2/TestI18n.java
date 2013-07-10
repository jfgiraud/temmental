package temmental2;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import junit.framework.TestCase;

public class TestI18n extends TestCase {

    private static final Locale locale_en = new Locale("en");
    private static final Locale locale_en_AU = new Locale("en", "AU");
    private static final Locale locale_en_CA = new Locale("en", "CA");
    private static final Locale locale_en_GB = new Locale("en", "GB");
    private static final Locale locale_en_US = new Locale("en", "US");
    private static final Locale locale_es_AR = new Locale("es", "AR");
    private static final Locale locale_es_BO = new Locale("es", "BO");
    private static final Locale locale_fr_CA = new Locale("fr", "CA");
    
    private Template template;
    private HashMap<String, Transform> filters;
    private HashMap<String, Object> model;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        filters = new HashMap<String, Transform>();
        model = new HashMap<String, Object>();
        TemplateRecorder.setRecording(true);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TemplateRecorder.setRecording(false);
    }

    private void assertFoundAndEquals(String expected, Locale locale, String file) throws IOException, TemplateException {
        template = new Template("test/temmental2/test-file.tpl", filters, "classpath:temmental2/" + file, locale);
        assertEquals(expected, template.formatForTest("~'hello[]~", model));
        template = new Template("test/temmental2/test-file.tpl", filters, "file:test/temmental2/" + file + ".properties", locale);
        assertEquals(expected, template.formatForTest("~'hello[]~", model));
    }
    
    private void assertNotFound(Locale locale, String file) throws IOException, TemplateException {
        try {
            template = new Template("test/temmental2/test-file.tpl", filters, "classpath:temmental2/" + file, locale);
            fail("An exception must be thrown!");
        } catch (TemplateException e) {
            assertTrue(e.getMessage().startsWith("Can't find bundle for "));
        }
        try {
            template = new Template("test/temmental2/test-file.tpl", filters, "file:test/temmental2/" + file + ".properties", locale);
            fail("An exception must be thrown!");
        } catch (TemplateException e) {
            assertTrue(e.getMessage().startsWith("Can't find properties file"));
        }
    }

    public void test_en_ca() throws IOException, TemplateException {
        assertFoundAndEquals("hello en_CA", locale_en_CA, "test");
        assertNotFound(locale_en_CA, "test2");
        assertFoundAndEquals("hello (default)", locale_en_CA, "test3");
    }

    public void test_en_gb() throws IOException, TemplateException {
        assertFoundAndEquals("hello en_GB", locale_en_GB, "test");
        assertFoundAndEquals("hello en_GB", locale_en_GB, "test2");
        assertFoundAndEquals("hello (default)", locale_en_GB, "test3");
    }
    
    public void test_en() throws IOException, TemplateException {
        assertFoundAndEquals("hello fr_FR", locale_en, "test");
        assertNotFound(locale_en, "test2");
        assertFoundAndEquals("hello (default)", locale_en, "test3");
    }

    public void test_es_ar() throws IOException, TemplateException {
        assertFoundAndEquals("hello es_AR", locale_es_AR, "test");
        assertNotFound(locale_es_AR, "test2");
        assertFoundAndEquals("hello (default)", locale_es_AR, "test3");
    }
    
    public void test_es_bo() throws IOException, TemplateException {
        assertFoundAndEquals("hello es", locale_es_BO, "test");
        assertNotFound(locale_es_AR, "test2");
        assertFoundAndEquals("hello (default)", locale_es_BO, "test3");
    }
    
    public void test_fr_CA() throws IOException, TemplateException {
        assertFoundAndEquals("hello fr_FR", locale_fr_CA, "test");
        assertNotFound(locale_fr_CA, "test2");
        assertFoundAndEquals("hello (default)", locale_fr_CA, "test3");
    }

}
