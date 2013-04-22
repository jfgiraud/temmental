package temmentalr;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import static temmentalr.TemplateUtils.*;

public class TemplateTest {

    class Fruit {
        String name;
        public Fruit(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return "Fruit: " + name;
        }
    }

	private Template interpreter;
	private Map<String,Object> model;
	private Properties properties;
	
	@Before
	public void setUp() throws FileNotFoundException, TemplateException, IOException {
		properties = new Properties();
		interpreter = new Template(new TemplateMessages(Locale.ENGLISH, properties));
		model = new HashMap<String, Object>();
	}
	
	@Test
	public void testText() throws IOException, TemplateException {
		parse("Some text data...");
		assertParsingEquals(text("Some text data..."));
		assertWriteEquals("Some text data...");
	}
	
	@Test
	public void testNumber() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		parse("~123~");
		assertParsingEquals(number(123));
		assertWriteEquals("123");
		
		parse("~$text:'charat<2>~");
		populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
		populateModel("text", "lorem ipsum");
		assertWriteEquals("r");
	}

	@Test
	public void testTextContainingQuoteCharacter() throws IOException, TemplateException { 
	    parse("Some text data... with 'b");
	    assertParsingEquals(text("Some text data... with 'b"));
	    assertWriteEquals("Some text data... with 'b");
	}
	
	@Test
	public void testTextContainingDollarCharacter() throws IOException, TemplateException { 
	    parse("Some text data... with $b");
	    assertParsingEquals(text("Some text data... with $b"));
	    assertWriteEquals("Some text data... with $b");
	}
	
	@Test
	public void testTextContainingTildeCharacter() throws IOException, TemplateException {
	    parse("Some text data... with ~~");
	    assertParsingEquals(text("Some text data... with ~"));
	    assertWriteEquals("Some text data... with ~");
	}

	@Test
	public void testTextContainingAccents() throws IOException, TemplateException {
	    parse("Text with accents: ÍntèrnáTîönàlïzâÇïôn");
	    assertParsingEquals(text("Text with accents: ÍntèrnáTîönàlïzâÇïôn"));
	    assertWriteEquals("Text with accents: ÍntèrnáTîönàlïzâÇïôn");
	}

	@Test
	public void testVariableReplacement() throws IOException, TemplateException {
	    parse("~$text_to_replace~");
	    assertParsingEquals(eval("$text_to_replace"));
	    populateModel("text_to_replace", "Some text data...");
	    assertWriteEquals("Some text data...");
	}
	
	@Test
	public void testVariablesCanBeInTheMiddleOfTheText() throws IOException, TemplateException {
	    parse("The city of ~$city~, with a population of ~$population~ inhabitants, is the ~$rank~th largest city in ~$state~.");
	    assertParsingEquals(text("The city of "), eval("$city"), text(", with a population of "), eval("$population"), text(" inhabitants, is the "), eval("$rank"), text("th largest city in "), eval("$state"), text("."));
	    populateModel("city", "Bordeaux");
	    populateModel("population", 242945);
	    populateModel("rank", 9);
	    populateModel("state", "France");
	    assertWriteEquals("The city of Bordeaux, with a population of 242945 inhabitants, is the 9th largest city in France.");
	}
	
	@Test
	public void testWhenAVariableIsRequiredButNotFoundAnExceptionIsRaised() throws IOException, TemplateException {
	    parse("~$text_to_replace~");
	    assertParsingEquals(eval("$text_to_replace"));
	    assertWriteThrowsException("Key 'text_to_replace' is not present or has null value in the model map at position '-:l1:c2'.");
	}
	
	@Test
	public void testWhenAVariableIsOptionalAndNotFoundAnEmptyTextIsDisplayed() throws IOException, TemplateException {
	    parse("~$text_to_replace?~");
	    assertParsingEquals(eval("$text_to_replace?"));
	    assertWriteEquals("");
	}

	@Test
	public void testWhenAVariableIsOptionalAndFoundTheVariableReplacementIsWellDone() throws IOException, TemplateException {
	    parse("~$text_to_replace?~");
	    assertParsingEquals(eval("$text_to_replace?"));
	    populateModel("text_to_replace", "Some text data...");
	    assertWriteEquals("Some text data...");
	}

	@Test
	public void testAFunctionCanBeAppliedOnTheResultOfTheReplacement_TransformCase() throws IOException, TemplateException {
		parse("The uppercase of '~$text~' is '~$text:'upper~'");
		assertParsingEquals(text("The uppercase of '"), eval("$text"), text("' is '"), func("'upper", "$text"), text("'"));
		populateModel("text", "Eleanor of Aquitaine");
		populateTransform("upper", new Transform<String, String>() {
			@Override
			public String apply(String value) {
				return value.toUpperCase();
			}
		});
		assertWriteEquals("The uppercase of 'Eleanor of Aquitaine' is 'ELEANOR OF AQUITAINE'");
	}
	
	@Test
	public void testAFunctionCanBeAppliedOnTheResultOfTheReplacement_MethodCase() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		parse("The uppercase of '~$text~' is '~$text:'upper~'");
		assertParsingEquals(text("The uppercase of '"), eval("$text"), text("' is '"), func("'upper", "$text"), text("'"));
		populateModel("text", "Eleanor of Aquitaine");
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase", null));
		assertWriteEquals("The uppercase of 'Eleanor of Aquitaine' is 'ELEANOR OF AQUITAINE'");
	}
	
	@Test
	public void testWhenARequiredFunctionIsNotFoundAnExceptionIsRaised() throws IOException, TemplateException {
		parse("The required function 'upper is not known for rendering '~$text:'upper~'. An exception will be raised.");
		assertParsingEquals(text("The required function 'upper is not known for rendering '"), func("'upper", "$text"), text("'. An exception will be raised."));
		populateModel("text", "Eleanor of Aquitaine");
		assertWriteThrowsException("No transform function named ''upper' is associated with the template for rendering at position '-:l1:c65'.");
	}
	@Test
	public void testWhenARequiredFunctionIsFoundButArgumentTypeDiffersAnExceptionIsRaised() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		parse("~$text:'upper~");
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase", null));
		
//		populateTransform("upper", new Transform<String, String>() {
//			@Override
//			public String apply(String value) {
//				return value.toUpperCase();
//			}
//		});
		
		assertParsingEquals(func("'upper", "$text"));
		populateModel("text", 5);
		assertWriteThrowsException("Unable to apply function ''upper' at position '-:l1:c8'. This function expects java.lang.String. It receives java.lang.Integer.");
	}
	
	
	@Test
	public void testAChainOfFunctionCanBeApplied() throws IOException, TemplateException {
		parse("Apply a chain of functions ~$text:'bold:'italic~");
		assertParsingEquals(text("Apply a chain of functions "), 
				func("'italic", func("'bold", "$text")));
		populateModel("text", "Eleanor of Aquitaine");
		populateTransform("bold", new Transform<String, String>() {
			@Override
			public String apply(String value) {
				return "<b>" + value + "</b>";
			}
		});
		populateTransform("italic", new Transform<String, String>() {
			@Override
			public String apply(String value) {
				return "<i>" + value + "</i>";
			}
		});
		assertWriteEquals("Apply a chain of functions <i><b>Eleanor of Aquitaine</b></i>");
	}

	@Test
	public void testStringCanBeUsedBetweenTildeCharacters() throws IOException, TemplateException {
		parse("Something before...~\"A text with double quotes\\\", tilde ~, brackets >< ...\"~Something after...");
		assertParsingEquals(text("Something before..."), text("A text with double quotes\", tilde ~, brackets >< ..."), text("Something after..."));
		assertWriteEquals("Something before...A text with double quotes\", tilde ~, brackets >< ...Something after...");
	}
	
	@Test
	public void testStringCanBeUsedBetweenTildeCharactersBis() throws IOException, TemplateException {
		parse("Something before...~\"A text with antislash\\, tilde ~, brackets >< ...\"~Something after...");
		assertParsingEquals(text("Something before..."), text("A text with antislash\\, tilde ~, brackets >< ..."), text("Something after..."));
		assertWriteEquals("Something before...A text with antislash\\, tilde ~, brackets >< ...Something after...");
	}
	
	@Test
	public void testAFunctionWithInitializerCanBeCreatedForReusePurpose() throws IOException, TemplateException {
		populateModel("text", "Eleanor of Aquitaine");
		populateTransform("encapsulate", new Transform<String, Transform>() {
			@Override
			public Transform apply(final String tag) {
				return new Transform<String, String>() {
					@Override
					public String apply(String value) {
						return "<"+tag+">"+value+"</"+tag+">";
					}
				};
			}
		});
		
		parse("Apply a parameterized function ~$text:'encapsulate<\"b\">~");
		assertParsingEquals(text("Apply a parameterized function "), func(func("'encapsulate", "b"), "$text"));
		assertWriteEquals("Apply a parameterized function <b>Eleanor of Aquitaine</b>");
		
		parse("Apply a parameterized function ~$text:'encapsulate<\"i\">~");
		assertParsingEquals(text("Apply a parameterized function "), func(func("'encapsulate", "i"), "$text"));
		assertWriteEquals("Apply a parameterized function <i>Eleanor of Aquitaine</i>");
	}

	@Test
	public void testAFunctionCanHaveAnInitializerWithSeveralParameters() throws IOException, TemplateException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("Applying bold and italic functions gives ~$text:'encapsulate<\"b\",\"i\">~");
		assertParsingEquals(text("Applying bold and italic functions gives "), func(func("'encapsulate", "b", "i"), "$text"));
		populateModel("text", "Eleanor of Aquitaine");
		populateTransform("encapsulate", new Transform<String[], Transform>() {
			@Override
			public Transform apply(final String tags[]) { // b, i
				return new Transform<String, String>() {
					@Override
					public String apply(String value) { // $text
						String s = "";
						for (int i=0; i<tags.length; i++) {
							s += "<" + tags[i] + ">";
						}
						s += value;
						for (int i=tags.length-1; i>=0; i--) {
							s += "</" + tags[i] + ">";
						}
						return s;
					}
				};
			}
		});
		assertWriteEquals("Applying bold and italic functions gives <b><i>Eleanor of Aquitaine</i></b>");
	}
	
	// -- 
	
	@Test
	public void testParameterizedQuoteFunctionChain() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		parse("~$text:'concat<$suffix>~");
		assertParsingEquals(func(func("'concat", "$suffix"), "$text"));
		populateTransform("concat", String.class.getDeclaredMethod("concat", String.class));
		populateModel("text", "hello world");
		populateModel("suffix", "!");
		assertWriteEquals("hello world!");

	}
	
	@Test
	public void testParameterizedQuoteFunctionAcceptsFunctionApplicationOnItsInitializers() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'replace<$what,$with:'upper>~");
		assertParsingEquals(func(func("'replace", "$what", func("'upper", "$with")), "$text"));
		populateTransform("replace", String.class.getDeclaredMethod("replaceAll",String.class, String.class));
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
		populateModel("text", "this is string example....wow!!! this is really string");
		populateModel("what", "is");
		populateModel("with", "was");
		assertWriteEquals("thWAS WAS string example....wow!!! thWAS WAS really string");
	}
	
	@Test
	public void testExceptionMessageOnTransformWith1Param_CaseOk() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'charat<$index>~");
		assertParsingEquals(func(func("'charat", "$index"), "$text"));
		populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
		populateModel("text", "lorem ipsum");
		populateModel("index", 2);
		assertWriteEquals("r");
	}
	
	@Test
	public void testExceptionMessageOnTransformWith1Param_CaseKo1() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'charat<$index,$index>~");
		populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
		populateModel("text", "lorem ipsum");
		populateModel("index", 2);
		assertWriteThrowsException("Unable to apply function ''charat' at position '-:l1:c8'. This function expects only one parameter. It receives 2 parameters.");
	}
	
	@Test
	public void testExceptionMessageOnTransformWith1Param_CaseKo4() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'charat<>~");
		populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
		populateModel("text", "lorem ipsum");
		populateModel("index", 2);
		assertWriteThrowsException("Unable to apply function ''charat' at position '-:l1:c8'. This function expects one parameter. It receives no parameter.");
	}

	@Test
	public void testExceptionMessageOnTransformWithoutParam() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'charat~");
		populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
		populateModel("text", "lorem ipsum");
		populateModel("index", 2);
		assertWriteThrowsException("Unable to apply function ''charat' at position '-:l1:c8'. This function expects one or more parameters. It receives no parameter.");
	}
	
	@Test
	public void testExceptionMessageInsideNoParameter() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'replace<$what,$with:'upper:'charat>~");
		populateTransform("replace", String.class.getDeclaredMethod("replaceAll",String.class, String.class));
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
		populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
		populateModel("text", "lorem ipsum dolor sit amet");
		populateModel("what", "ipsum");
		populateModel("with", "elit");
		assertWriteThrowsException("Unable to apply function ''charat' at position '-:l1:c36'. This function expects one or more parameters. It receives no parameter.");
	}

	@Test
	public void testExceptionMessageInsideBadParameter() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'replace<$what,$with:'upper:'charat<$index>>~");
		populateTransform("replace", String.class.getDeclaredMethod("replaceAll",String.class, String.class));
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
		populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
		populateModel("text", "lorem ipsum dolor sit amet");
		populateModel("what", "ipsum");
		populateModel("with", "elit");
		populateModel("index", "2");
		assertWriteThrowsException("Unable to apply function ''charat' at position '-:l1:c36'. This function expects int for parameter #1. It receives java.lang.String.");
	}
	
	@Test
	public void testExceptionMessageInsideEmptyParameterList() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'replace<$what,$with:'upper:'charat<>>~");
		populateTransform("replace", String.class.getDeclaredMethod("replaceAll",String.class, String.class));
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
		populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
		populateModel("text", "lorem ipsum dolor sit amet");
		populateModel("what", "ipsum");
		populateModel("with", "elit");
		assertWriteThrowsException("Unable to apply function ''charat' at position '-:l1:c36'. This function expects one parameter. It receives no parameter.");
	}
	
	@Test
	public void testExceptionMessageInsideWrongNumberOfParameters() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'replace<$what,$with:'upper:'charat<$index,$index>>~");
		populateTransform("replace", String.class.getDeclaredMethod("replaceAll",String.class, String.class));
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
		populateTransform("charat", String.class.getDeclaredMethod("charAt", int.class));
		populateModel("text", "lorem ipsum dolor sit amet");
		populateModel("what", "ipsum");
		populateModel("with", "elit");
		populateModel("index", 2);
		assertWriteThrowsException("Unable to apply function ''charat' at position '-:l1:c36'. This function expects only one parameter. It receives 2 parameters.");
	}
	
	@Test
	public void testExceptionMessageOnTransformWith1Param_CaseKo2() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'charat<$index>~");
		assertParsingEquals(func(func("'charat", "$index"), "$text"));
		Method toto = String.class.getDeclaredMethod("charAt", int.class);
		populateTransform("charat", toto);
		populateModel("text", "lorem ipsum");
		populateModel("index", "2");
		assertWriteThrowsException("Unable to apply function ''charat' at position '-:l1:c8'. This function expects int for parameter #1. It receives java.lang.String.");
	}

	@Test
	public void testExceptionMessageOnTransformWith1Param_CaseKo3() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'charat<$index>~");
		assertParsingEquals(func(func("'charat", "$index"), "$text"));
		Method toto = String.class.getDeclaredMethod("charAt", int.class);
		populateTransform("charat", toto);
		populateModel("text", 12345);
		populateModel("index", 2);
		assertWriteThrowsException("Unable to apply function ''charat' at position '-:l1:c8'. This function expects java.lang.String. It receives java.lang.Integer.");
	}
	
	@Test
	public void testExceptionMessageOnTransformWith2Params_CaseOk() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'replace<$what,$with>~");
		assertParsingEquals(func(func("'replace", "$what", "$with"), "$text"));
		populateTransform("replace", String.class.getDeclaredMethod("replaceAll",String.class, String.class));
		populateModel("text", "lorem ipsum dolor sit amet");
		populateModel("what", "ipsum");
		populateModel("with", "elit");
		assertWriteEquals("lorem elit dolor sit amet");
	}
	
	@Test
	public void testExceptionMessageOnTransformWith2Params_CaseKo1() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'replace<$what,$with,$with>~");
		populateTransform("replace", String.class.getDeclaredMethod("replaceAll",String.class, String.class));
		populateModel("text", "lorem ipsum dolor sit amet");
		populateModel("what", "ipsum");
		populateModel("with", "elit");
		assertWriteThrowsException("Unable to apply function ''replace' at position '-:l1:c8'. This function expects 2 parameters. It receives 3 parameters.");
	}
	
	@Test
	public void testExceptionMessageOnTransformWith2Params_CaseKo2() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'replace<$what,$with>~");
		assertParsingEquals(func(func("'replace", "$what", "$with"), "$text"));
		populateTransform("replace", String.class.getDeclaredMethod("replaceAll",String.class, String.class));
		populateModel("text", "lorem ipsum dolor sit amet");
		populateModel("what", 5);
		populateModel("with", "elit");
		assertWriteThrowsException("Unable to apply function ''replace' at position '-:l1:c8'. This function expects java.lang.String for parameter #1. It receives java.lang.Integer.");
	}

	@Test
	public void testExceptionMessageOnTransformWith2Params_CaseKo3() throws IOException, TemplateException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		parse("~$text:'replace<$what,$with>~");
		assertParsingEquals(func(func("'replace", "$what", "$with"), "$text"));
		populateTransform("replace", String.class.getDeclaredMethod("replaceAll",String.class, String.class));
		populateModel("text", "lorem ipsum dolor sit amet");
		populateModel("what", "ipsum");
		populateModel("with", 5);
		assertWriteThrowsException("Unable to apply function ''replace' at position '-:l1:c8'. This function expects java.lang.String for parameter #2. It receives java.lang.Integer.");
	}

	
	@Test
	public void testParameterizedQuoteFunctionAcceptsFunctionApplicationOnTheResult() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		parse("~$text:'replace<$what:'lower,$with>:'upper~");
		assertParsingEquals(func("'upper", func(func("'replace", func("'lower", "$what"), "$with"), "$text")));
		populateTransform("replace", String.class.getDeclaredMethod("replaceAll",String.class, String.class));
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
		populateTransform("lower", String.class.getDeclaredMethod("toLowerCase"));
		populateModel("text", "this is string example....wow!!! this is really string");
		populateModel("what", "Is");
		populateModel("with", "was");
		assertWriteEquals("THWAS WAS STRING EXAMPLE....WOW!!! THWAS WAS REALLY STRING");
	}
	
	@Test
	public void testADynamicFunctionCanBeAppliedOnTheResultOfTheReplacement() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		parse("~$text:$f~");
		populateModel("text", "It is an example!");
		populateModel("f", "upper");
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
		assertParsingEquals(func("$f", "$text"));
		assertWriteEquals("IT IS AN EXAMPLE!");
	}
	
	@Test
	public void testADynamicFunctionCanBeOptional_CaseKnown() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		parse("~$text:$f?~");
		populateModel("text", "It is an example!");
		populateModel("f", "upper");
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
		assertParsingEquals(func("$f?", "$text"));
		assertWriteEquals("IT IS AN EXAMPLE!");
	}
	
	@Test
	public void testADynamicFunctionCanBeOptional_CaseUnknown() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		parse("~$text:$f?~");
		populateModel("text", "It is an example!");
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
		assertParsingEquals(func("$f?", "$text"));
		assertWriteEquals("");
	}
	
	@Test
	public void testWhenARequiredDynamicFunctionIsNotFoundAnExceptionIsRaised() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		parse("~$text:$f~");
		populateModel("text", "It is an example!");
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
		assertParsingEquals(func("$f", "$text"));
		assertWriteThrowsException("Key 'f' is not present or has null value in the model map at position '-:l1:c8'.");
	}

	
	@Test
	public void testAMessageCanBeDefinedStaticallyInProperties() throws IOException, TemplateException {
		parse("Text before...~'helloworld[]~Text after...");
		populateProperty("helloworld", "Bonjour le monde !");
		assertParsingEquals(text("Text before..."), message("'helloworld"), text("Text after...")); 
		assertWriteEquals("Text before...Bonjour le monde !Text after...");
	}
	
	@Test
	public void testIfMessageDefinedStaticallyIsNotFoundInPropertiesAnExceptionIsRaised() throws IOException, TemplateException {
		parse("Text before...~'helloworld[]~Text after...");
		assertParsingEquals(text("Text before..."), message("'helloworld"), text("Text after..."));
		assertWriteThrowsException("Key 'helloworld' is not present in the property map to render message (-:l1:c16)");
	}
	
	
	@Test
	public void testAMessageCanUseParametersToBeDisplayed_CaseOptionalParameterNotSet() throws IOException, TemplateException {
		parse("Text before...~'hello[$firstname?]~Text after...");
		populateProperty("hello", "Bonjour {0} !");
		assertParsingEquals(text("Text before..."), message("'hello", "$firstname?"), text("Text after...")); 
		assertWriteEquals("Text before...Text after...");
	}
	
	@Test
	public void testAMessageRaiseAnExceptionIfARequiredParameterIsNotSet() throws IOException, TemplateException {
		parse("Text before...~'hello[$firstname]~Text after...");
		populateProperty("hello", "Bonjour {0} !");
		assertParsingEquals(text("Text before..."), message("'hello", "$firstname"), text("Text after...")); 
		assertWriteThrowsException("Key 'firstname' is not present or has null value in the model map at position '-:l1:c23'.");
	}
	
	@Test
	public void testParseQuoteMessageWithRequiredParameterSet() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		parse("Text before...~'hello[$firstname:'upper]~Text after...");
		populateProperty("hello", "Bonjour {0} !");
		populateModel("firstname", "John");
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));
		assertParsingEquals(text("Text before..."), message("'hello", func("'upper", "$firstname")), text("Text after...")); 
		assertWriteEquals("Text before...Bonjour JOHN !Text after...");
	}
	
	
	@Test
	public void testParseAMessageCanBeDefinedDynamicallyInProperties() throws IOException, TemplateException {
		parse("Text before...~$msg[]~Text after...");
		populateProperty("helloworld", "Bonjour le monde !");
		populateModel("msg", "helloworld");
		assertParsingEquals(text("Text before..."), message("$msg"), text("Text after...")); 
		assertWriteEquals("Text before...Bonjour le monde !Text after...");
	}
	
	@Test
	public void testParseArrays() throws IOException, TemplateException {
		parse("~($p1,$p2):'add~");
		populateModel("p1", 5);
		populateModel("p2", 3);
		populateTransform("add", new Transform<Integer[], Integer>() {
			@Override
			public Integer apply(Integer[] values) {
				int sum = 0;
				for (Integer value : values) {
					sum += value.intValue();
				}
				return sum;
			}
		});
		assertParsingEquals(func("'add", array("$p1", "$p2"))); 
		assertWriteEquals("8");
	}
	
	@Test
	public void testParseArraysBadType() throws IOException, TemplateException {
		parse("~($p1,$p2):'add~");
		populateModel("p1", "5");
		populateModel("p2", "3");
		populateTransform("add", new Transform<Integer[], Integer>() {
			@Override
			public Integer apply(Integer[] values) {
				int sum = 0;
				for (Integer value : values) {
					sum += value.intValue();
				}
				return sum;
			}
		});
		assertParsingEquals(func("'add", array("$p1", "$p2"))); 
		assertWriteThrowsException("Unable to apply function ''add' at position '-:l1:c12'. This function expects java.lang.Integer[]. It receives java.lang.String[].");
	}

	@Test
	public void testParseExceptionOnInvalidIdentifier() throws IOException, TemplateException {
		assertParseThrowsException("Invalid identifier syntax for 'variable' at '-:l1:c2'.", "~variable~");
	}
	
	@Test
	public void testParseExceptionForTransformFunction() throws IOException, TemplateException {
		assertParseThrowsException("Invalid identifier syntax for 'function' at '-:l1:c13'.", "~$variable?:function~");
		assertParseThrowsException("Invalid identifier syntax for 'function2' at '-:l1:c24'.", "~$variable?:'function1:function2~");
		assertParseThrowsException("Invalid identifier syntax for 'function2' at '-:l1:c29'.", "~$variable?:'function1<$p1>:function2~");
		assertParseThrowsException("Invalid identifier syntax for 'function3' at '-:l1:c28'.", "~$variable?:'function1<$p1:function3>:'function2~");
	}
	
	@Test
	public void testABlockIsRenderedIfTheConditionIsTrue() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		populateModel("aBoolean", true);
		populateModel("firstname", "john");
		populateModel("lastname", "doe");
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));

		parse("~$aBoolean#if~Hello ~$firstname~ ~$lastname:'upper~~#/if~");
		assertWriteEquals("Hello john DOE");
	}

	@Test
	public void testABlockIsNotRenderedIfTheConditionIsFalse() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		populateModel("aBoolean", false);
		parse("~$aBoolean#if~Hello ~$firstname~ ~$lastname:'upper~~#/if~");
		assertWriteEquals("");
	}
	
	@Test
	public void testParsingThrowsAnExceptionIfCommandIsNotClosed() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		assertParseThrowsException("Command '/if' not closed to complete statement (reach 'if' at position '-:l1:c13').", "~$aBoolean?#if~Hello ~$firstname~ ~$lastname:'upper~");
	}
	
	@Test
	public void testParsingThrowsAnExceptionIfCommandIsNotOpened() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		assertParseThrowsException("Command 'if' not opened to complete statement (reach '/if' at position '-:l1:c51').", "~$aBoolean~Hello ~$firstname~ ~$lastname:'upper~~#/if~");
	}
	
	@Test
	public void testAConditionCanBeAnRpnOperation() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		populateModel("aBoolean", false);
		populateModel("firstname", "john");
		populateModel("lastname", "doe");
		populateTransform("upper", String.class.getDeclaredMethod("toUpperCase"));

		parse("~{$aBoolean not}#if~Hello ~$firstname~ ~$lastname:'upper~~#/if~");
		assertWriteEquals("Hello john DOE");
	}
	
	@Test
	public void testAConditionIsOptional_CaseNotPresent() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		parse("~$aBoolean?#if~lorem ipsum~#/if~");
		assertWriteEquals("");
	}
	
	@Test
	public void testAConditionIsOptional_CasePresent() throws IOException, TemplateException, NoSuchMethodException, SecurityException {
		populateModel("aBoolean", true);
		parse("~$aBoolean?#if~lorem ipsum~#/if~");
		assertWriteEquals("lorem ipsum");
	}
	
	@Test
	public void testIterateOnIterable() throws IOException, TemplateException {
		parse("~$fruits#for~~$fruit~~#/for~");
		populateModel("fruits", createList(createModel("fruit", "Orange"), createModel("fruit", "Lemon"), createModel("fruit", "Apple")));
		assertWriteEquals("OrangeLemonApple");
	}
	
	@Test
	public void testIterateDoesNotChangeModel() throws IOException, TemplateException {
		parse("~$fruit~~$fruits#for~~$fruit~~#/for~~$fruit~");
		populateModel("fruit", "Lemon");
		populateModel("fruits", createList(createModel("fruit", "Orange"), createModel("fruit", "Lemon"), createModel("fruit", "Apple")));
		assertWriteEquals("LemonOrangeLemonAppleLemon");
	}
	
	@Test
	public void test_010d() throws IOException, TemplateException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	    populateTransform("toModel", new Transform<List<Fruit>, List<Map<String,Object>>>() {
			@Override
			public List<Map<String,Object>> apply(List<Fruit> fruits) throws TemplateException {
                return convert(fruits, new ConvertFunction<Fruit>() {
                    public void populate(Map<String,Object> model, Fruit f, int index) {
                        model.put("fruit", f.name);
                        model.put("index", index);
                    }
                });
			}
		});
	    populateModel("fruit", "lemon");
	    populateModel("fruits", Arrays.asList(new Fruit("orange"), new Fruit("lemon"), new Fruit("apple")));
	    parse("~$fruit~/~$fruits:'toModel#for~~$index~.~$fruit~/~#/for~~$fruit~");
	    assertWriteEquals("lemon/0.orange/1.lemon/2.apple/lemon");
    }

	@Test
	public void testForAs() throws IOException, TemplateException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
	    populateModel("fruits", Arrays.asList(new Fruit("orange"), new Fruit("lemon"), new Fruit("apple")));
	    
	    populateTransform("as", new Transform<String, Transform<Iterable, Iterable>>() {
			@Override
			public Transform<Iterable, Iterable> apply(final String key) throws TemplateException {
				return new Transform<Iterable, Iterable>() {
					@Override
					public Iterable apply(Iterable values) throws TemplateException {
						List result = new ArrayList<>();
						Iterator it = values.iterator();
						while (it.hasNext()) {
							Object e = it.next();
							Map m = new HashMap();
							m.put(key, e);
							result.add(m);
						}
						return result;
					}
				};
			}
		});
	    parse("~$fruits:'as<\"fruit\">#for~~$fruit~~#/for~");

	    // [ lemon, orange ] 
	    // [ (1, lemon), (2, orange) ]
	    // [ { index:1, name: lemon }, { index:2, name: orange } ]
	    // ~$fruits@fruit#for
	    //    $fruit:'get<"name">
	    // ~#/for~
	    // ~$fruits:'toModel<"fruit">#
	    // ~$fruits:'enumerate#for~
	    //    ~name~
	    // ~#/for~
	    
	    assertWriteEquals("le mon/0.orange/1.lemon/2.apple/lemon");
    } 

	
	@Test
	public void testCalc() throws IOException, TemplateException {
		parse("~{$s 2 +}~");
		assertParsingEquals(calc(eval("$s"), number(2), text("+")));
		populateModel("s", 5);
		assertWriteEquals("7");
		parse("~{$s 2 <=}~"); 
		assertWriteEquals("false");
	}

	@Test
	public void testCalcInvalidSyntax() throws IOException, TemplateException {
		assertParseThrowsException("Invalid identifier syntax for 'bar' at '-:l1:c3'.", "~{bar}~");
		assertParseThrowsException("Invalid identifier syntax for 'bar' at '-:l1:c10'.", "~{$dummy bar}~");
		assertParseThrowsException("Invalid identifier syntax for 'bar' at '-:l1:c10'.", "~{$dummy bar $dummy}~");
		assertParseThrowsException("Invalid identifier syntax for 'bar' at '-:l1:c8'.", "~{$foo:bar}~");
		// TODO mettre le resultat de la pile dans la pile mere
		assertParseThrowsException("Invalid identifier syntax for 'foo' at '-:l1:c15'.", "~{$bar {1 4 + foo -}}~");
	}
	
	private void assertEvaluation(String expected, String expression) throws IOException, TemplateException {
		parse(expression);
		assertWriteEquals(expected);
	}
	
	@Test
	public void testCalcOperations() throws IOException, TemplateException {
		populateModel("i", 11);
		populateModel("l", 17L);
		populateModel("f", 17.5F);
		populateModel("d", 17.5);
		
		parse("~{$i -2 +}~");
		assertParsingEquals(calc(eval("$i"), number(-2), text("+")));

		assertEquals(3.5, Double.parseDouble("3.5d"), 0.001);
		
		assertEvaluation("34", "~{23L 11 +}~");
		assertEvaluation("9", "~{$i -2 +}~");
		assertEvaluation("-5", "~{$i 16 -}~");
		assertEvaluation("68", "~{$l 4 *}~");
		assertEvaluation("70.0", "~{$f 4 *}~");
		assertEvaluation("2", "~{$l 3 %}~");
		assertEvaluation("3", "~{7 2 /}~");
		assertEvaluation("3.5", "~{7 2.0 /}~");
		assertEvaluation("3.5", "~{7 2d /}~");
		assertEvaluation("200050.0", "~{.5e+2 2e5 +}~");
		
		assertEvaluation("2.5", "~{$f 3 %}~");
		assertEvaluation("-17.5", "~{$f neg}~");
		assertEvaluation("true", "~{$l odd}~");
		assertEvaluation("false", "~{$l even}~");
		assertEvaluation("true", "~{$i $l <}~");
		assertEvaluation("false", "~{$l $i <}~");
		assertEvaluation("-11", "~{$i neg}~");
		assertEvaluation("-17.5", "~{$f neg}~");
		assertEvaluation("false", "~{$i $l >}~");
		assertEvaluation("false", "~{$i $i >}~");
		assertEvaluation("true", "~{$l $i >}~");
		assertEvaluation("false", "~{$i $l >=}~");
		assertEvaluation("true", "~{$i $i >=}~");
		assertEvaluation("false", "~{$l $i <=}~");
		assertEvaluation("true", "~{$i $l <=}~");
		assertEvaluation("true", "~{$i $i <=}~");
		assertEvaluation("false", "~{$i 11 !=}~");
		assertEvaluation("true", "~{$i 17 !=}~");
		assertEvaluation("true", "~{$i 11 ==}~");
		assertEvaluation("false", "~{$i 17 ==}~");
		assertEvaluation("true", "~{$f $d ==}~");
		assertEvaluation("false", "~{$f $d !=}~");
		assertEvaluation("22", "~{{$i} {$i} +}~");

		assertEvaluation("144", "~{12 sq}~");
		assertEvaluation("8.0", "~{2 3 pow}~");
		
		assertEvaluation("3", "~{2 3 max}~");
		assertEvaluation("-2", "~{-2 3 min}~");
		
		assertEvaluation("2", "~{1.5 ceil}~");
		assertEvaluation("1", "~{1 ceil}~");
		assertEvaluation("-1", "~{-1.5 ceil}~");

		assertEvaluation("1", "~{1.5 floor}~");
		assertEvaluation("1", "~{1 floor}~");
		assertEvaluation("-2", "~{-1.5 floor}~");

		assertEvaluation("10.5", "~{10.5 abs}~");
		assertEvaluation("10.5", "~{-10.5 abs}~");
		assertEvaluation("15", "~{-15 abs}~");
		
		assertEvaluation("7.56", "~{6.55957 2 round 1 +}~");
		assertEvaluation("0.667", "~{2.0 3 / 3 round}~");
		assertEvaluation("2", "~{1.9 0 round}~");
		assertEvaluation("-4", "~{-3.9 0 round}~");
		assertEvaluation("-3.9", "~{-3.87 1 round}~");
		
		assertEvaluation("7.55", "~{6.55957 2 trunc 1 +}~");
		assertEvaluation("-6.55", "~{-6.55957 2 trunc}~");
		assertEvaluation("0.666", "~{2.0 3 / 3 trunc}~");
		assertEvaluation("0.6", "~{2.0 3 / 1 trunc}~");
		assertEvaluation("3", "~{3 0 trunc}~");
		assertEvaluation("3", "~{3.9 0 trunc}~");
		assertEvaluation("-3", "~{-3.9 0 trunc}~");
		
		populateModel("T", true);
		populateModel("F", false);
		assertEvaluation("false", "~{true $F and}~");
		assertEvaluation("true", "~{true {$T} and}~");
		assertEvaluation("false", "~{$F $F and}~");
		
		assertEvaluation("false", "~{$F $F or}~");
		assertEvaluation("true", "~{$F $T or}~");
		assertEvaluation("true", "~{$T $F or}~");

		assertEvaluation("false", "~{$F $F xor}~");
		assertEvaluation("true", "~{$F $T xor}~");
		assertEvaluation("true", "~{$T $F xor}~");
		assertEvaluation("false", "~{$T $T xor}~");

		assertEvaluation("true", "~{$F not}~");
		assertEvaluation("false", "~{$T not}~");

		unpopulateModel("F");
		parse("~{$F not}~");
		assertWriteThrowsException("Key 'F' is not present or has null value in the model map at position '-:l1:c3'.");
		assertEvaluation("", "~{$T true not xor $F? or}~");
		
		populateModel("F", false);
		assertEvaluation("true", "~{$T true not xor $F? or}~");
		
		
		assertEvaluation("15", "~{-15 abs}~");
	}
		
	private String calc(String ... stack) {
		return "calc(" + Arrays.asList(stack) + ")";
	}

	@Test
	public void testChainErrorDetectedWhileParsing() {
		fail("testChainErrorDetectedWhileParsing");
	}
	
	@Test
	public void testBracketMismatch() {
		assertParseThrowsException("Bracket mismatch ('<' at position '-:l1:c16' vs ']' at position '-:l1:c26').", "~$text:'replace<$old,$new]~");

		assertParseThrowsException("Invalid bracket type '[' at position '-:l1:c30'.", "~$text:'replace<$old:'replace[$a,$b>,$new>~");
	}
	
	@Test
	public void testInvalidBracket() {
		assertParseThrowsException("Invalid bracket type '[' at position '-:l1:c16'.", "~$text:'replace[$old,$new]~");
		assertParseThrowsException("Invalid bracket type '(' at position '-:l1:c16'.", "~$text:'replace($old,$new)~");
		assertParseThrowsException("Invalid bracket type '(' at position '-:l1:c30'.", "~$text:'replace<$old,'replace($old,$new)>~");
	}
	
	@Test
	public void testBracketNotClosed() {
		assertParseThrowsException("Bracket not closed ('<' at position '-:l1:c16').", "~$text:'replace<$old,$new~");
		assertParseThrowsException("Invalid bracket type '[' at position '-:l1:c16'.", "~$text:'replace[$old,$new~");
		assertParseThrowsException("Bracket not closed ('[' at position '-:l1:c2').", "~[$old~");
		assertParseThrowsException("Bracket not closed ('(' at position '-:l1:c2').", "~($old~");
	}
	
	@Test
	public void testBracketNotOpened() {
		assertParseThrowsException("Bracket '<' not opened for the closing bracket '>' '-:l1:c19').", "~$text:'replaceold>~");
		assertParseThrowsException("Bracket not closed ('<' at position '-:l1:c16').", "~$text:'replace<$new,'foo<$old>~");
	}
	
	@Test
	public void testModelOverride() {
		fail("testModelOverride");
	}
	
	// --------------------------------------------------------------------------------------------------------------------------------------------
	
	
	private String eval(String text) {
		return "eval(" + text + ")";
	}
	
	private String text(String text) {
		return text;
	}
	
	private String number(Number number) {
		return number.toString();
	}

	private String array(Object ... parameters) {
		List<Object> params = new ArrayList<Object>();
		for (Object o : parameters) {
			if (o instanceof String) {
				if (((String) o).startsWith("$")) 
					params.add(eval((String) o));
				else if (((String) o).startsWith("'")) 
					params.add(eval((String) o));	
				else  
					params.add(o);	

			} else { 
				params.add(o);
			}
		}
		return params.toString();
	}
	
	private String message(String name, Object ... parameters) {
		if (! name.startsWith("eval"))
			name = eval(name);
		List<Object> params = new ArrayList<Object>();
		for (Object o : parameters) {
			if (o instanceof String) {
				if (((String) o).startsWith("$")) 
					params.add(eval((String) o));
				else if (((String) o).startsWith("'")) 
					params.add(eval((String) o));	
				else  
					params.add(o);	

			} else { 
				params.add(o);
			}
		}
		return "msg("+ name + ","  + params.toString() + ")";
	}
	
	private String func(String name, Object ... parameters) {
		if (! name.startsWith("eval"))
			name = eval(name);
		List<Object> params = new ArrayList<Object>();
		for (Object o : parameters) {
			System.out.println(">>" + o + " " + o.getClass().getName());
			if (o instanceof String) {
				if (((String) o).startsWith("$")) 
					params.add(eval((String) o));
				else if (((String) o).startsWith("'")) 
					params.add(eval((String) o));	
				else  
					params.add(o);	

			} else { 
				params.add(o);
			}
		}
		return name + params.toString();
	}

	private void populateModel(String name, Object value) throws TemplateException {
		model.put(name, value);
	}

	private void unpopulateModel(String name) throws TemplateException {
		model.remove(name);
	}
	
	private void populateProperty(String name, Object value) throws TemplateException {
		properties.put(name, value);
	}
	
	private void populateTransform(String name, Method method) throws TemplateException {
		interpreter.addFunction(name, method);
	}

	private void populateTransform(String name, Transform transform) throws TemplateException {
		interpreter.addFunction(name, transform);
	}
	
	@Test
	public void testMacthes() {
		assertTrue("[-:l1:c12, #pos]".matches("[-:l\\d+:c\\d+, #pos]".replace("$", "\\$").replace("[", "\\[").replace("]", "\\]")));
	}

	private void assertParsingEquals(String ... expectedStack) throws IOException {
		String shouldBe = "[";
		for (String expected : expectedStack) {
			if (! shouldBe.equals("["))
				shouldBe += ", ";
			shouldBe += expected;
		}
		shouldBe += "]";
		assertEquals(shouldBe, interpreter.toString());
	}

	private void parse(String s) throws IOException, TemplateException {
		interpreter.clear();
		interpreter.parse(s, "-", 1, 0);
	}

	private void assertWriteEquals(String expected) throws IOException, TemplateException {
		StringWriter out = new StringWriter();
		interpreter.write(out, model);
		assertEquals(expected, out.toString());
	}

	private void assertWriteThrowsException(String expectedMessage) {
		StringWriter out = new StringWriter();
		try {
			interpreter.write(out, model);
			fail("An exception must be raised.");
		} catch (Exception e) {
			assertEquals(expectedMessage, e.getMessage());
		}
		
	}

	private void assertParseThrowsException(String expectedMessage, String pattern) {
		try {
			parse(pattern);
			interpreter.printStack(System.out);
			fail("An exception must be raised.");
		} catch (Exception e) {
			e.printStackTrace(System.err);
			assertEquals(expectedMessage, e.getMessage());
		}
	}

}
