package temmental2;

import static temmental2.TemplateUtils.createModel;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

public class NewExampleTest extends TestCase {

    /*
    reste a parser #true #false #list @X
    ~$map:'keys:'sort#iterate<$item,$index>~
  Nom: ~$item@name~
~#iterate~

~foncteurs#list<valeur>~~valeur~<br/>~#list~

    <!-- #section -->
    
    $k?[] si k present mais pas dans les messages : exception
    $k?[]? si k present mais pas dans les messages : pas de rendu
    $k?[]! si k present mais pas dans les messages : affiche k a la place (peut etre utile pour des roles qui seront crees)
    
    */
	private NewTemplate template;
	private Transform<String, ? extends Object> upper, firstLower, length, color, bool;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		template = new NewTemplate();
		upper = new Transform<String, String>() {
			public String apply(String value) {
				return value.toUpperCase();
			}
		};
		firstLower = new Transform<String, String>() {
			public String apply(String value) {
				return "" + value.substring(0, 1).toLowerCase() + value.substring(1);
			}
		};
		length = new Transform<String, Integer>() {
			public Integer apply(String value) {
				return value.length();
			}
		};
		bool = new Transform<String, Boolean>() {
			@Override
			public Boolean apply(String value) {
				return Boolean.valueOf(value);
			}
		};
		color = new Transform<String, String>() {
			public String apply(String value) {
				return "<font color=\"red\">" + value + "</font>";
			}
		};
	}
	
	// -----------------------------------------------------------------------------------------------------------------
	// Parsing variables
	// -----------------------------------------------------------------------------------------------------------------
	
	public void testParseText() throws IOException, TemplateException {
		Node node = template.parse("Some text data...");
		assertEquals("text=Some text data...", node.representation());
		assertEquals("Some text data...", getContent());
	
	}
	
	private String getContent(Object ... map) throws TemplateException, IOException {
		StringWriter out = new StringWriter();
		template.printFile(out, createModel(map));
		return out.toString();
	}
	
	public void testParseTextWithQuote() throws IOException, TemplateException { 
	    Node node = template.parse("Some text data... with 'b");
        assertEquals("text=Some text data... with 'b", node.representation());
        assertEquals("Some text data... with 'b", getContent());
	}
	
	public void testParseTextWithNode() throws IOException, TemplateException { 
	    Node node = template.parse("Some text data... with $b");
        assertEquals("text=Some text data... with $b", node.representation());
        assertEquals("Some text data... with $b", getContent());
	}
	
	public void testParseTextWithTilde() throws IOException, TemplateException {
	    Node node = template.parse("Some text data... with ~~");
        assertEquals("text=Some text data... with ~", node.representation());
        assertEquals("Some text data... with ~", getContent());
	}
	
	public void testParseVariableRequired() throws IOException, TemplateException {
	    Node node = template.parse("~$myvar~");
        assertEquals("text=|variable=myvar|text=", template.representation(node));
        assertEquals("The text", getContent("myvar", "The text"));
        try {
        	getContent();
        	fail("An exception must be thrown.");
        } catch (TemplateException e) {
        	assertEquals("Key 'myvar' is not present or has null value in the model map (needed for '$myvar' at position '-:l1:c1').", e.getMessage());
        }
	}

	public void testParseVariableOptionalNothingRenderedIfNotPresent() throws IOException, TemplateException {
	    Node node = template.parse("~$myvar?~");
	    assertEquals("text=|variable=myvar,norenderifnotpresent|text=", template.representation(node));
	    assertEquals("", getContent());
	    assertEquals("The text", getContent("myvar", "The text"));
	    
	    node = template.parse("~$my.var_XX.99?~");
        assertEquals("text=|variable=my.var_XX.99,norenderifnotpresent|text=", template.representation(node));
	}
	
    public void testParseVariableOptionalRenderNameIfNotPresent() throws IOException, TemplateException {
        Node node = template.parse("~$myvar!~");
        assertEquals("text=|variable=myvar,rendernameifnotpresent|text=", template.representation(node));
        assertEquals("myvar", getContent());
        assertEquals("The text", getContent("myvar", "The text"));
    }

    public void testParseVariableCanNotChangeOptionalFlag() throws IOException, TemplateException {
        try {
            template.parse("~$myvar!?~");
            fail("An exception must be thrown.");
        } catch (Exception e) {
            assertEquals("Invalid syntax at position '-:l1:c9' - reach character '?'", e.getMessage());
        }
        try {
            template.parse("~$myvar??~");
            fail("An exception must be thrown.");
        } catch (Exception e) {
            assertEquals("Invalid syntax at position '-:l1:c9' - reach character '?'", e.getMessage());
        }
    }

    
	public void testParseVariableWithTextBeforeAndTextAfter() throws IOException, TemplateException {
		Node node = template.parse("cb\u00e9a~$myvar~ab\u00e9c");
		assertEquals("text=cb\u00e9a|variable=myvar|text=ab\u00e9c", template.representation(node));
		assertEquals("cbéaThe textabéc", getContent("myvar", "The text"));
	}

	public void testParseVariableWithOptionalVariableFilter() throws IOException, TemplateException {
	    Node node = template.parse("~$myvar:$filter?~");
	    assertEquals("text=|variable=myvar#transform,variable=filter,norenderifnotpresent|text=", template.representation(node));
	    assertEquals("THE TEXT", getContent("myvar", "The text", "filter", upper));
	    assertEquals("", getContent("myvar", "The text"));
	}

	public void testParseVariableWithStaticFilter() throws IOException, TemplateException {
        Node node = template.parse("~$myvar:'filter~");
        template.addTransform("filter", upper);
        
        assertEquals("text=|variable=myvar#transform,quote=filter|text=", template.representation(node));
        assertEquals("THE TEXT", getContent("myvar", "The text"));
	}
	
	public void testParseVariableWithTwoDifferentFilters() throws IOException, TemplateException {
        Node node = template.parse("~$myvar:$filter?:'filter2~");
        
        assertEquals("text=|variable=myvar#transform,variable=filter,norenderifnotpresent#transform,quote=filter2|text=", template.representation(node));
        Transform addOne = new Transform<Integer, Integer>() {
			public Integer apply(Integer value) {
				return value + 1;
			}
		};
		template.addTransform("filter2", color);
        assertEquals("", getContent("myvar", "The text"));
        assertEquals("<font color=\"red\">9</font>", getContent("myvar", 8, "filter", addOne));
    }
	
	public void testParseVariableWithThreeDifferentFilters() throws IOException, TemplateException {
		Node node = template.parse("~$myvar:$filter?:'filter2:$filter3~");
		assertEquals("text=|variable=myvar#transform,variable=filter,norenderifnotpresent#transform,quote=filter2#transform,variable=filter3|text=", template.representation(node));
		template.addTransform("filter2", firstLower);
        		
//		"Key '%s' is not present or has null value in the model map to render '%s' at position '%s'."
		assertFormatException("Key 'filter3' is not present or has null value in the model map (needed for '$filter3' at position '-:l1:c26').", "myvar", "The text");
        assertEquals("8", getContent("myvar", "The text", "filter", upper, "filter3", length));
	}

	private void assertFormatException(String exceptionMsg, Object ... map) {
		try {
			getContent(map);
			fail("An exception must be thrown.");
		} catch (Exception e) {
			assertEquals(exceptionMsg, e.getMessage());
		}
	}

	public void testAVariableNameAcceptsPointAndUnderscore() throws IOException, TemplateException {
		Node node = template.parse("~$my.var_XX.99~");
		assertEquals("text=|variable=my.var_XX.99|text=", template.representation(node));
		assertEquals("The text", getContent("my.var_XX.99", "The text"));
	}
	
	public void testAnOptionalVariableAcceptsFilters() throws IOException, TemplateException {
		Node node = template.parse("~$myvar?:$filter?~");
		assertEquals("text=|variable=myvar,norenderifnotpresent#transform,variable=filter,norenderifnotpresent|text=", template.representation(node));
		assertEquals("THE TEXT", getContent("myvar", "The text", "filter", upper));
		assertEquals("", getContent("myvar", "The text"));
		assertEquals("", getContent("filter", upper));
		
		node = template.parse("~$myvar?:$filter?:'filter2~");
		template.addTransform("filter2", firstLower);
		assertEquals("text=|variable=myvar,norenderifnotpresent#transform,variable=filter,norenderifnotpresent#transform,quote=filter2|text=", template.representation(node));
		assertEquals("tHE TEXT", getContent("myvar", "The text", "filter", upper));
		assertEquals("", getContent("myvar", "The text"));
		assertEquals("", getContent("filter", upper));
		
		node = template.parse("~$myvar?:$filter?:'filter2:$filter3~");
		template.addTransform("filter2", firstLower);
		assertEquals("text=|variable=myvar,norenderifnotpresent#transform,variable=filter,norenderifnotpresent#transform,quote=filter2#transform,variable=filter3|text=", template.representation(node));
		assertEquals("<font color=\"red\">tHE TEXT</font>", getContent("myvar", "The text", "filter", upper, "filter3", color));
		assertEquals("", getContent("myvar", "The text", "filter3", upper));
		assertEquals("", getContent("filter", upper, "filter3", upper));
	}

	public void testVariableMessageWithOptionalKey() throws IOException, TemplateException {
		Node node = template.parse("before~$myvar?[]~after");
		setMessages("hello", "Hello Mister");
		assertEquals("text=before|message,variable=myvar,norenderifnotpresent,noparam|text=after", template.representation(node));
		assertEquals("beforeHello Misterafter", getContent("myvar", "hello"));
		assertEquals("beforeafter", getContent());
	}
	
	private void setMessages(String ... map) throws TemplateException {
		Properties properties = new Properties();
		if (map.length %2 != 0)
            throw new TemplateException("Invalid number of elements (key/value list implies an even size).");
        Map<String, Object> model = new HashMap<String, Object>();
        for (int i=0; i<map.length/2; i++) {
            properties.put((String) map[2*i], map[2*i+1]);
        }
        TemplateMessages messages = new TemplateMessages(properties, Locale.FRENCH);
		template.setMessages(messages);
	}
	
	
	public void testMessageWithOptionalKeyAcceptsFilters() throws IOException, TemplateException {
		Node node = template.parse("before~$myvar?[]:$filter?:'filter2:$filter3~after");
		assertEquals("text=before|message,variable=myvar,norenderifnotpresent,noparam#transform,variable=filter,norenderifnotpresent#transform,quote=filter2#transform,variable=filter3|text=after", template.representation(node));

		setMessages("hello", "Hello Mister");
		template.addTransform("filter2", firstLower);
		assertEquals("before<font color=\"red\">hELLO MISTER</font>after", getContent("myvar", "hello", "filter", upper, "filter3", color));
		
	}
	
	public void testQuoteMessage() throws IOException, TemplateException {
		Node node = template.parse("before~'myvar[]~after");
		setMessages("myvar", "Hello Mister");
		
		assertEquals("text=before|message,quote=myvar,noparam|text=after", template.representation(node));
		assertEquals("beforeHello Misterafter", getContent("myvar", "hello"));
	} 
	
	public void testTransform() throws IOException, TemplateException {
		Node node = template.parse("The result of the addition is: ~'add<$p1,$p2>~");
		Transform<Iterable<Integer>, Integer> add = new Transform<Iterable<Integer>, Integer>() {
			@Override
			public Integer apply(Iterable<Integer> values) {
				Integer sum = 0;
				for (Integer value : values) {
					sum = sum + value;
				}
				return sum;
			}
		};
		template.addTransform("add", add);
		
		
		assertEquals("text=The result of the addition is: |message,quote=add,parameters=[variable=p1,,variable=p2]|text=", template.representation(node));
		
		
		assertEquals("The result of the addition is: 8", getContent("p1", 3, "p2", 5));
		assertFormatException("Unable to apply parametrized function 'add' to render ''add' at position '-:l1:c32'.", "p1", 3, "p2", "7");
		
		node = template.parse("The result of the addition is: ~'add<@items>~");
		template.addTransform("add", add);
		
		assertEquals("The result of the addition is: 8", getContent("items", Arrays.asList(2, 5, 6)));
	}
	
	public void testQuoteMessageAcceptsFilters() throws IOException, TemplateException {
		Node node = template.parse("~'myvar[]:$filter?:'filter2:$filter3~hello");
		assertEquals("text=|message,quote=myvar,noparam#transform,variable=filter,norenderifnotpresent#transform,quote=filter2#transform,variable=filter3|text=hello", template.representation(node));

		setMessages("myvar", "Hello Mister");
		template.addTransform("filter2", firstLower);
		assertEquals("<font color=\"red\">hELLO MISTER</font>hello", getContent("filter", upper, "filter3", color));
	}
	
	
/*
	$k?[] si k present mais pas dans les messages : exception
	        $k?[]? si k present mais pas dans les messages : pas de rendu
	        $k?[]! si k present mais pas dans les messages : affiche k a la place (peut etre utile pour des roles qui seront crees)
	*/
	
	public void testSomethingIsMissingBeforeFilterUse() throws IOException, TemplateException {
		try {
			template.parse("~:$filter~");
			fail("An exception must be thrown.");
		} catch (Exception e) {
			assertEquals("Invalid syntax at position '-:l1:c2' - reach character ':'", e.getMessage());
		}
		try {
			template.parse("~'myvar[:$filter?:'filter2:$filter3]~");
			fail("An exception must be thrown.");
		} catch (Exception e) {
			assertEquals("Invalid syntax at position '-:l1:c9' - reach character ':'", e.getMessage());
		}
	}

	public void testVariableMessageParameterOrFilterFlag() throws IOException, TemplateException {
		Node node = template.parse("before~'myvar[$name?]~after");
		assertEquals("text=before|message,quote=myvar,parameters=[variable=name,norenderifnotpresent]|text=after", template.representation(node));
		setMessages("myvar", "Hello {0}");
		assertEquals("beforeHello Jeffafter", getContent("name", "Jeff"));
		assertEquals("beforeafter", getContent());

		node = template.parse("before~'myvar[$name?:$filter?]~after");
		assertEquals("text=before|message,quote=myvar,parameters=[variable=name,norenderifnotpresent#transform,variable=filter,norenderifnotpresent]|text=after", template.representation(node));
		assertEquals("beforeHello JEFFafter", getContent("name", "Jeff", "filter", upper));
		assertEquals("beforeafter", getContent("name", "Jeff"));
		assertEquals("beforeafter", getContent("filter", upper));
	}
	
    public void testParseVariableMessagePropertyRequired() throws IOException, TemplateException {
        Node node = template.parse("~$myvar[]~");
        assertEquals("text=|message,variable=myvar,noparam|text=", template.representation(node));
        assertFormatException("Key 'myvar' is not present or has null value in the model map (needed for '$myvar' at position '-:l1:c1').");
        
        setMessages("hello", "Hello Mister");
        assertEquals("Hello Mister", getContent("myvar", "hello"));
        assertFormatException("Key 'myvar' is not present or has null value in the model map (needed for '$myvar' at position '-:l1:c1').");
    }

    public void testParseVariableMessagePropertyOptional() throws IOException, TemplateException {
        Node node = template.parse("~$myvar[]?~");
        setMessages();
        assertEquals("text=|message,variable=myvar,norenderifpropertynamenotpresent,noparam|text=", template.representation(node));
        assertEquals("", getContent("myvar", "hello"));
        setMessages("hello", "Hello Mister");
        assertEquals("Hello Mister", getContent("myvar", "hello"));
    }
    
    public void testQuoteMessagePropertyCanNotBeOptional() throws IOException, TemplateException {
        try {
            template.parse("~'myvar[]?~");
            fail("An exception must be thrown.");
        } catch (Exception e) {
            assertEquals("Invalid syntax at position '-:l1:c10' - reach character '?'", e.getMessage());
        }
        try {
            template.parse("~'myvar[]!~");
            fail("An exception must be thrown.");
        } catch (Exception e) {
            assertEquals("Invalid syntax at position '-:l1:c10' - reach character '!'", e.getMessage());
        }
    }

    public void testParseVariableMessagePropertyOptionalButKeyDisplayed() throws IOException, TemplateException {
        Node node = template.parse("~$myvar[]!~");
        assertEquals("text=|message,variable=myvar,renderpropertynameifnotpresent,noparam|text=", template.representation(node));
        setMessages();
        assertEquals("hello", getContent("myvar", "hello"));
    }
    
    public void testParseVariableMessageCanNotChangeOptionalFlag() throws IOException, TemplateException {
    	assertParseException("~$myvar[]!?~", "Invalid syntax at position '-:l1:c11' - reach character '?'");
    	assertParseException("~$myvar[]??~", "Invalid syntax at position '-:l1:c11' - reach character '?'");
    	assertParseException("~$myvar[]?!~", "Invalid syntax at position '-:l1:c11' - reach character '!'");
    }
    
    public void testParseVariableMessagePropertyInvalidCharAfterCloseBracket() throws IOException, TemplateException {
    	assertParseException("~$myvar[]a~", "Invalid syntax at position '-:l1:c10' - reach character 'a'");
        assertParseException("~$myvar[].~hello", "Invalid syntax at position '-:l1:c10' - reach character '.'");
        assertParseException("~$myvar[]_~hello", "Invalid syntax at position '-:l1:c10' - reach character '_'");
        assertParseException("~$myvar[]$~hello", "Invalid syntax at position '-:l1:c10' - reach character '$'");
	}
    
	public void testOnePropertyIncludedInAProperty() throws IOException, TemplateException {
		Node node = template.parse("~'hello['mister[$lastname]]~");
		setMessages("hello", "Bonjour {0}", "mister", "Monsieur {0}!");
		assertEquals("text=|message,quote=hello,parameters=[message,quote=mister,parameters=[variable=lastname]]|text=", template.representation(node));
		assertEquals("Bonjour Monsieur Jeff!", getContent("lastname", "Jeff"));
	}
	
	public void testPropertiesIncludedMustHaveBracket() throws IOException, TemplateException {
		assertParseException("~'hello['mister]~", "Invalid syntax at position '-:l1:c16' - reach character ']'");
    }
	
	public void testVariableMessageWithDifferentTypeOfParametersAndFilters() throws IOException, TemplateException {
        Node node = template.parse("before~'myvar[$a?:'f1,$b:'f2]:'f3~after");
        assertEquals("text=before|message,quote=myvar,parameters=[variable=a,norenderifnotpresent#transform,quote=f1,,variable=b#transform,quote=f2]#transform,quote=f3|text=after", template.representation(node));
        template.addTransform("f1", firstLower);
        template.addTransform("f2", upper);
        template.addTransform("f3", color);
        setMessages("myvar", "Hello {0} and {1}");
        
        assertEquals("before<font color=\"red\">Hello jeff and PEG</font>after", getContent("a", "Jeff", "b", "Peg"));
        
        node = template.parse("~'myvar[$a?:$f1,$b.c:'f2,'msg[]:'f5]:'f3~hello");
        assertEquals("text=|message,quote=myvar,parameters=[variable=a,norenderifnotpresent#transform,variable=f1,,variable=b.c#transform,quote=f2,,message,quote=msg,noparam#transform,quote=f5]#transform,quote=f3|text=hello", template.representation(node));
        
        node = template.parse("~'myvar[$a?:$f1,$b.c:'f2,'msg[$cc:$f6]:'f5]:'f3~hello");
        assertEquals("text=|message,quote=myvar,parameters=[variable=a,norenderifnotpresent#transform,variable=f1,,variable=b.c#transform,quote=f2,,message,quote=msg,parameters=[variable=cc#transform,variable=f6]#transform,quote=f5]#transform,quote=f3|text=hello", template.representation(node));
        
        setMessages("myvar", "Hello {0}, {1} and {2}", "msg", "Mister {0}");
        
        template.addTransform("f2", upper);
        template.addTransform("f3", color);
        template.addTransform("f5", firstLower);
        assertEquals("<font color=\"red\">Hello JEFF, PEG and mister MIKE</font>hello", getContent("a", "Jeff", "b.c", "Peg", "cc", "Mike", "f1", upper, "f6", upper));
        
	}
	
	public void testArray() throws IOException, TemplateException {
        Node node = template.parse("aaa~($a?,$b:'f2):'f3~bbb");
        assertEquals("text=aaa|array,parameters=[variable=a,norenderifnotpresent,,variable=b#transform,quote=f2]#transform,quote=f3|text=bbb", template.representation(node));
        template.addTransform("f2", upper);
        template.addTransform("f3", new Transform<String[], String>() {
			@Override
			public String apply(String[] value) {
				String s = "";
				for (int i=0; i<value.length; i++) {
					s += value[value.length-1-i];
				}
				return s;
			}
		});
        assertEquals("aaaDOEJohnbbb", getContent("a", "John", "b", "Doe"));
        assertEquals("aaabbb", getContent("b", "Doe"));
        
        node = template.parse("~'myvar[$disk,($a?:'f1,$b:'f2):'f3]:'f4~");
        Transform toInt = new Transform<String, Integer>() {
			@Override
			public Integer apply(String value) {
				return Integer.valueOf(value);
			}
		};
		template.addTransform("f1", toInt);
        template.addTransform("f2", toInt);
        Transform multiplicate = new Transform<Integer[], Integer>() {
			@Override
			public Integer apply(Integer[] value) {
				int r = 1;
				for (int i : value) {
					r *= i;
				}
				return r;
			}
		};
		template.addTransform("f3", multiplicate);
		template.addTransform("f4", upper);
		setMessages("myvar", "The disk {0} contains {1,choice,0#no file|1#one file|1<{1,number,integer} files}.");
        assertEquals("text=|message,quote=myvar,parameters=[variable=disk,,array,parameters=[variable=a,norenderifnotpresent#transform,quote=f1,,variable=b#transform,quote=f2]#transform,quote=f3]#transform,quote=f4|text=", template.representation(node));
        assertEquals("THE DISK C: CONTAINS 10 FILES.", getContent("a", "5", "b", "2", "disk", "C:"));
	}

	public void testMultipleFilters() throws IOException, TemplateException {
	    Node node = template.parse("aaa~$a:'f2:'f3~bbb");
	    template.addTransform("f2", upper);
        template.addTransform("f3", firstLower);
	    assertEquals("text=aaa|variable=a#transform,quote=f2#transform,quote=f3|text=bbb", template.representation(node));
	    assertEquals("aaadOEbbb", getContent("a", "Doe"));
	}

	public void testCommandIf() throws IOException, TemplateException {
	    Node node = template.parse("aaa~#if $a:'f~before ~$b~ after~#/if~bbb");
	    template.addTransform("f", bool);
        assertEquals("text=aaa|command[open]=if,variable=a#transform,quote=f,childs=[text=before ,,variable=b,,text= after,,command[close]=if]|text=bbb", template.representation(node));
        assertEquals("aaabbb", getContent("a", "false"));
        assertEquals("aaabefore Doe afterbbb", getContent("a", "true", "b", "Doe"));
	}
	
	public void testCommandNotReferenced() throws IOException, TemplateException {
		assertParseException("aaa~#command_not_referenced $a:'f~before ~$b~ after~#/command_not_referenced~bbb",
				"Invalid syntax at position '-:l1:c77' - invalid command name 'command_not_referenced'!");
    }
	
	public void testCommandOpenCloseMismatch() throws IOException, TemplateException {
		assertParseException("aaa~#if $a:'f~before ~$b~ after~#/iter~bbb",
				"Invalid syntax at position '-:l1:c39' - bad close tag (expected='if', actual='iter')");
		assertParseException("aaabefore ~$b~ after~#/if~bbb", 
				"Invalid syntax at position '-:l1:c23' - reach close tag without opened tag!");
    }

	public void testErrorWithStrangeCharacterInCommandNames() throws IOException, TemplateException {
		assertParseException("~#i$f $b~text~~#/if~", "Invalid syntax at position '-:l1:c4' - reach character '$'");
		assertParseException("~#i?f $b~text~~#/if~", "Invalid syntax at position '-:l1:c4' - reach character '?'");
        assertParseException("~#i'f $b~text~~#/if~", "Invalid syntax at position '-:l1:c4' - reach character '\''");
        assertParseException("~#i!f $b~text~~#/if~", "Invalid syntax at position '-:l1:c4' - reach character '!'");
	}
	
	public void testInvalidSyntaxAboutArrays() throws IOException, TemplateException {
        assertParseException("aaa~($a?,$b:'f2]:'f3~bbb", "Invalid syntax at position '-:l1:c16' - reach character ']', invalid bracket type!");
        assertParseException("~'myvar[($a?:'f1,$b:'f2)]:'f3~", "Invalid syntax at position '-:l1:c25' - reach character ']', a parameter can not be an array!");
        assertParseException("~'myvar[]:($a,$b)~", "Invalid syntax at position '-:l1:c11' - reach character '('");
    }
	
	public void testInvalidSyntaxBadNameAndCarriageReturnChangesLineNumber() throws IOException, TemplateException {
		assertParseException("Some text data\nwith a carriage return~$my\u00e9var~", "Invalid syntax at position '-:l2:c27' - reach character '\u00e9'");
	}
	
	public void testInvalidSyntaxCarriageReturnChangesLineNumber() throws IOException, TemplateException {
        assertParseException("Some text data\nwith a carriage return~$my$var~", "Invalid syntax at position '-:l2:c27' - reach character '$'");
    }
	
	public void testErrorOnInvalidNames() throws IOException, TemplateException {
		assertParseException("~$.my.var_XX~", "Invalid syntax at position '-:l1:c3' - invalid name '.my.var_XX'");
		assertParseException("~$~", "Invalid syntax at position '-:l1:c3' - reach character '~', empty name!");
	}

	public void testInvalidSyntaxFlagsSomewhereInNames() throws IOException, TemplateException {
		assertParseException("~$$myvar~", "Invalid syntax at position '-:l1:c3' - reach character '$'");
		assertParseException("~$myvar:$$toto~", "Invalid syntax at position '-:l1:c10' - reach character '$'");
		assertParseException("~$myvar:$toto$~", "Invalid syntax at position '-:l1:c14' - reach character '$'");
		assertParseException("~$myvar:$to$to~", "Invalid syntax at position '-:l1:c12' - reach character '$'");
		assertParseException("~$myvar:$to'to~", "Invalid syntax at position '-:l1:c12' - reach character '\''");
		assertParseException("~$my$var:$toto~", "Invalid syntax at position '-:l1:c5' - reach character '$'");
		assertParseException("~$myvar:$to?to~", "Invalid syntax at position '-:l1:c13' - reach character 't'");
		assertParseException("~$myvar:$to!to~", "Invalid syntax at position '-:l1:c13' - reach character 't'");
		assertParseException("~$my!var:$toto~", "Invalid syntax at position '-:l1:c6' - reach character 'v'");
		assertParseException("~$my?var:$toto~", "Invalid syntax at position '-:l1:c6' - reach character 'v'");
		assertParseException("~$my?var[$toto]~", "Invalid syntax at position '-:l1:c6' - reach character 'v'");
		assertParseException("~$myvar[$to?to]~", "Invalid syntax at position '-:l1:c13' - reach character 't'");
	}
	
	public void testQuoteMustHaveSquareBrackets() throws IOException, TemplateException {
		assertParseException("~'myvar~", "Invalid syntax at position '-:l1:c8' - reach character '~'");
	}
	
	public void testSomeErrorsWithSquareBrackets() throws IOException, TemplateException {
		assertParseException("~'myvar?~", "Invalid syntax at position '-:l1:c8' - reach character '?'");
		assertParseException("~'myvar?[]~", "Invalid syntax at position '-:l1:c8' - reach character '?'");
		assertParseException("~'myvar[~", "Invalid syntax at position '-:l1:c9' - reach character '~', bracket not closed!");
		assertParseException("~'myvar[[~", "Invalid syntax at position '-:l1:c9' - reach character '['");
		assertParseException("~'myvar][~", "Invalid syntax at position '-:l1:c8' - reach character ']'");
		assertParseException("~'myvar[]]~", "Invalid syntax at position '-:l1:c10' - reach character ']'");
		assertParseException("~'myvar[]$]~", "Invalid syntax at position '-:l1:c10' - reach character '$'");
	}
	
	public void testErrorOnEmptyName() throws IOException, TemplateException {
		assertParseException("~$?a~", "Invalid syntax at position '-:l1:c3' - reach character '?', empty name!");
	}

	private void assertParseException(String expr, String exceptionMsg) {
		try {
			Node node = template.parse(expr);
			fail("An exception must be thrown. " + node.representation());
		} catch (Exception e) {
			e.printStackTrace();
			assertEquals(exceptionMsg, e.getMessage());
		}
	}
	
	public void testParseVariableKO_07() throws IOException, TemplateException {
		assertParseException("~:a~", "Invalid syntax at position '-:l1:c2' - reach character ':'");
		assertParseException("~:$a~", "Invalid syntax at position '-:l1:c2' - reach character ':'");
	}

	public void testParseVariableKO_10() throws IOException, TemplateException {
		assertParseException("~$myvar:filter~", "Invalid syntax at position '-:l1:c9' - reach character 'f'");
	}

	public void testParseVariableKO_09() throws IOException, TemplateException {
		assertParseException("~$myvar:$filter?:'filter2?~", "Invalid syntax at position '-:l1:c26' - reach character '?'");
	}

	public void testTilde() throws IOException, TemplateException {
		Node node = template.parse("Some text~~Another text");
		assertEquals("text=Some text~Another text", template.representation(node));
		assertEquals("Some text~Another text", getContent());
		
		node = template.parse("Some text~~~~Another text");
		assertEquals("text=Some text~~Another text", template.representation(node));
		assertEquals("Some text~~Another text", getContent());
	}

	// -----------------------------------------------------------------------------------------------------------------
	// Parsing something...
	// -----------------------------------------------------------------------------------------------------------------
	
	public void testParseString() throws IOException, TemplateException {
		Node node = template.parse("Some text~\"Another text\"~And the end");
		assertEquals("text=Some text|string=Another text|text=And the end", template.representation(node));
		assertEquals("Some textAnother textAnd the end", getContent());
		
		node = template.parse("Some text~$msg[\"Another text\"]~And the end");
		setMessages("text", "the text is ''{0}''");
        assertEquals("text=Some text|message,variable=msg,parameters=[string=Another text]|text=And the end", template.representation(node));
        assertEquals("Some textthe text is 'Another text'And the end", getContent("msg", "text"));
        
        node = template.parse("Some text~$msg[\"Another text\":'g,$b:'f]~And the end");
        assertEquals("text=Some text|message,variable=msg,parameters=[string=Another text#transform,quote=g,,variable=b#transform,quote=f]|text=And the end", template.representation(node));
        template.addTransform("g", upper);
        template.addTransform("f", firstLower);
        setMessages("text", "the text is ''{0}''+{1}");
        assertEquals("Some textthe text is 'ANOTHER TEXT'+bYEAnd the end", getContent("msg", "text", "b", "BYE"));
        
        assertParseException("Some text~\"Another text~And the end", "Invalid syntax at position '-:l1:c24' - reach character '~', string not closed!");
        
        node = template.parse("Some text~$msg[\"Another text\":'g,$b:'f,\"\"]~And the end");
        assertEquals("text=Some text|message,variable=msg,parameters=[string=Another text#transform,quote=g,,variable=b#transform,quote=f,,string=]|text=And the end", template.representation(node));
        assertParseException("~$\"Another text\"~", "Invalid syntax at position '-:l1:c3' - reach character '\"'");
        assertParseException("~$b\"Another text\"~", "Invalid syntax at position '-:l1:c4' - reach character '\"'");
        assertParseException("~$b[$c~", "Invalid syntax at position '-:l1:c7' - reach character '~'");
        assertParseException("~$b[$c[]~", "Invalid syntax at position '-:l1:c9' - reach character '~'");
        assertParseException("~$b[$c[],]~", "Invalid syntax at position '-:l1:c10' - reach character ']'");
        assertParseException("~$b[,$c[]]~", "Invalid syntax at position '-:l1:c5' - reach character ','");
	}
	
	public void testParseKO_01() throws IOException, TemplateException {
		assertParseException("~myvar~", "Invalid syntax at position '-:l1:c2' - reach character 'm'");
	}

	
	public void testIterate() throws IOException, TemplateException {
		Node node = template.parse("~#iter $names~hello ~$name~~#/iter~");
		assertEquals("text=|command[open]=iter,variable=names,childs=[text=hello ,,variable=name,,text=,,command[close]=iter]|text=", template.representation(node));
	}

//	public void testSections() throws IOException, TemplateException {
//		Node node = template.parseFile("~#iter $names~hello ~$name~~#/iter~");
//		assertEquals("text=|command[open]=iter,variable=names,childs=[text=hello ,,variable=name,,text=,,command[close]=iter]|text=", template.representation(node));
//	}
	

}
