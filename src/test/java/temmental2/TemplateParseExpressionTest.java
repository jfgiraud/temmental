package temmental2;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class TemplateParseExpressionTest extends AbstractTestTemplate {

	private Expression expression;

	private Stack tokens;
	private Element element;

	private boolean displayRule = true;

	@Test
	public void testVariable() throws IOException, TemplateException {
		parseExpression("~$variable~");

		assertTokensEquals(identifier("$variable", p(1, 2)));
		
		assertElementEquals(identifier("$variable", p(1, 2)));
	}
	
	@Test
	public void testFilter() throws IOException, TemplateException {
		parseExpression("~$variable:'filter~");
		
		assertTokensEquals(identifier("$variable", p(1, 2)),
				toapply(p(1, 11)),
				identifier("'filter", p(1, 12))
				);
		
		assertElementEquals(
				function(identifier("'filter", p(1, 12)),
						 identifier("$variable", p(1, 2))));
		
	}
	
	@Test
	public void testTwoFilters() throws IOException, TemplateException {
		parseExpression("~$variable:'filter:$filter2~");
		
		assertTokensEquals(identifier("$variable", p(1, 2)),
				toapply(p(1, 11)),
				identifier("'filter", p(1, 12)),
				toapply(p(1, 19)),
				identifier("$filter2", p(1, 20))
				);
		
		assertElementEquals(
				function(identifier("$filter2", p(1, 20)),
						 function(identifier("'filter", p(1, 12)),
								  identifier("$variable", p(1, 2)))));

	}
	
	@Test
	public void testFilterWithNumInit() throws IOException, TemplateException {
		parseExpression("~$variable:'substr<0,5>~");
		
		assertTokensEquals(identifier("$variable", p(1, 2)),
				toapply(p(1, 11)),
				identifier("'substr", p(1, 12)),
				bracket('<', p(1, 19)),
				0,
				comma(p(1, 21)),
				5,
				bracket('>', p(1, 23))
				);
		
		assertElementEquals(
				functionp(identifier("'substr", p(1, 12)),
						  list(0, 5),
						  identifier("$variable", p(1, 2))));
		
	}
	
	@Test
	public void testFilterWithInit() throws IOException, TemplateException {
		parseExpression("~$variable:'replaceAll<\"a\",\"A\">~");
		
		assertTokensEquals(identifier("$variable", p(1, 2)),
				toapply(p(1, 11)),
				identifier("'replaceAll", p(1, 12)),
				bracket('<', p(1, 23)),
				text("a", p(1, 24)),
				comma(p(1, 27)),
				text("A", p(1, 28)),
				bracket('>', p(1, 31))
				);
		
		assertElementEquals(
				functionp(identifier("'replaceAll", p(1, 12)),
						  list(text("a", p(1, 24)), text("A", p(1, 28))),
						  identifier("$variable", p(1, 2))));
		
	}
	
	@Test
	public void testFilterWithInitChar() throws IOException, TemplateException {
		parseExpression("~$variable:'indexOf<'a'>~");
		//               1234567890123456789012345               
		assertTokensEquals(identifier("$variable", p(1, 2)),
				toapply(p(1, 11)),
				identifier("'indexOf", p(1, 12)),
				bracket('<', p(1, 20)),
				character('a', p(1, 21)),
				bracket('>', p(1, 24))
				);
		
		assertElementEquals(
				functionp(identifier("'indexOf", p(1, 12)),
						  list(character('a', p(1, 21))),
						  identifier("$variable", p(1, 2))));
		
	}
	
	@Test
	public void testMessageWithoutParameter() throws IOException, TemplateException {
		parseExpression("~$message[]~");

		assertTokensEquals(identifier("$message", p(1, 2)),
				bracket('[', p(1, 10)),
				bracket(']', p(1, 11)));
		
		assertElementEquals(
				message(identifier("$message", p(1, 2)),
						list()));
	}
	
	@Test
	public void testMessage() throws IOException, TemplateException {
		parseExpression("~$message[$firstname,$lastname]~");

		assertTokensEquals(identifier("$message", p(1, 2)),
				bracket('[', p(1, 10)),
				identifier("$firstname", p(1, 11)),
				comma(p(1, 21)),
				identifier("$lastname", p(1, 22)),
				bracket(']', p(1, 31)));
		
		assertElementEquals(
				message(identifier("$message", p(1, 2)),
						list(identifier("$firstname", p(1, 11)), identifier("$lastname", p(1, 22)))));
		
	}
	
	@Test
	public void testMessageWithTextDoubleQuote() throws IOException, TemplateException {
		parseExpression("~$message[$firstname,\"$last,\\\"$na:'me\"]~");

		assertTokensEquals(identifier("$message", p(1, 2)),
				bracket('[', p(1, 10)),
				identifier("$firstname", p(1, 11)),
				comma(p(1, 21)),
				text("$last,\"$na:'me", p(1, 22)),
				bracket(']', p(1, 38)));
	
		assertElementEquals(
				message(identifier("$message", p(1, 2)),
						list(identifier("$firstname", p(1, 11)), text("$last,\"$na:'me", p(1, 22)))));
	
	}

	@Test
	public void testMessageWithTextComma() throws IOException, TemplateException {
		parseExpression("~$message[$firstname,\"$last,$na:'me\"]~");

		assertTokensEquals(identifier("$message", p(1, 2)),
				bracket('[', p(1, 10)),
				identifier("$firstname", p(1, 11)),
				comma(p(1, 21)),
				text("$last,$na:'me", p(1, 22)),
				bracket(']', p(1, 37)));
		
		assertElementEquals(
				message(identifier("$message", p(1, 2)),
						list(identifier("$firstname", p(1, 11)), text("$last,$na:'me", p(1, 22)))));
		
	}

    @Test
    public void testOpeningCommand() throws IOException, TemplateException {
        parseExpression("~$models:'filter#for~");

        assertTokensEquals(identifier("$models", p(1, 2)),
                toapply(p(1, 9)),
                identifier("'filter", p(1, 10)),
                tocommand(p(1, 17)),
                keyword("for", p(1, 18))
                );

        assertElementEquals(command(keyword("for", p(1, 18)), p(1, 2), function(
                identifier("'filter", p(1, 10)),
                identifier("$models", p(1, 2)))
                ));
    }

    @Test
    public void testClosingCommand() throws IOException, TemplateException {
        parseExpression("~#for~");

        assertTokensEquals(tocommand(p(1, 2)),
                keyword("for", p(1, 3))
        );

        assertElementEquals(command(keyword("for", p(1, 3)), p(1, 2), null));
    }

	@Test
	public void testComplexMessage() throws IOException, TemplateException {
		parseExpression("~$message[$firstname:'upper,$lastname:'replace<\"a\",\"A\">]:'quote~");

		assertTokensEquals(identifier("$message", p(1, 2)),
				bracket('[', p(1, 10)),
				identifier("$firstname", p(1, 11)),
				toapply(p(1, 21)),
				identifier("'upper", p(1, 22)),
				comma(p(1, 28)),
				identifier("$lastname", p(1, 29)),
				toapply(p(1, 38)),
				identifier("'replace", p(1, 39)),
				bracket('<', p(1, 47)),
				text("a", p(1, 48)),
				comma(p(1, 51)),
				text("A", p(1, 52)),
				bracket('>', p(1, 55)),
				bracket(']', p(1, 56)),
				toapply(p(1, 57)),
				identifier("'quote", p(1, 58))
				);
		
		assertElementEquals(
				function(identifier("'quote", p(1, 58)),
						message(identifier("$message", p(1, 2)),
								list(
										function(identifier("'upper", p(1, 22)), identifier("$firstname", p(1, 11))),
										functionp(identifier("'replace", p(1, 39)),
												list(text("a", p(1, 48)), text("A", p(1, 52))),
												identifier("$lastname", p(1, 29)))))));

	}
		
	@Test
	public void testComplexMessage2() throws IOException, TemplateException {
		parseExpression("~$message[$firstname:'upper,$lastname:'replace<'\"',','>]:'quote~");

		assertTokensEquals(identifier("$message", p(1, 2)),
				bracket('[', p(1, 10)),
				identifier("$firstname", p(1, 11)),
				toapply(p(1, 21)),
				identifier("'upper", p(1, 22)),
				comma(p(1, 28)),
				identifier("$lastname", p(1, 29)),
				toapply(p(1, 38)),
				identifier("'replace", p(1, 39)),
				bracket('<', p(1, 47)),
				character('"', p(1, 48)),
				comma(p(1, 51)),
				character(',', p(1, 52)),
				bracket('>', p(1, 55)),
				bracket(']', p(1, 56)),
				toapply(p(1, 57)),
				identifier("'quote", p(1, 58))
				);
		
		assertElementEquals(
				function(identifier("'quote", p(1, 58)),
						message(identifier("$message", p(1, 2)),
								list(
										function(identifier("'upper", p(1, 22)), identifier("$firstname", p(1, 11))),
										functionp(identifier("'replace", p(1, 39)),
												list(character('"', p(1, 48)), character(',', p(1, 52))),
												identifier("$lastname", p(1, 29)))))));

	}
	
	@Test
	public void testArray() throws IOException, TemplateException {
		parseExpression("~($b1,$b2):$f~");
		
		assertTokensEquals(
				bracket('(', p(1, 2)),
				identifier("$b1", p(1, 3)),
				comma(p(1, 6)),
				identifier("$b2", p(1, 7)),
				bracket(')', p(1, 10)),
				toapply(p(1, 11)),
				identifier("$f", p(1, 12))
				);
		
		assertElementEquals(
			function(identifier("$f", p(1, 12)),
			array(p(1,2), identifier("$b1", p(1, 3)),identifier("$b2", p(1, 7)))));
				
	}
	
	protected void parseExpression(String s) throws IOException, TemplateException {
		if (displayRule) {
			displayRule(s);
		}
		
		expression = new Expression(s, c(1, 1)) {
            @Override
			Stack parseToTokens() throws IOException, TemplateException {
				tokens = super.parseToTokens();
				return tokens.clone();
			}
		};
		
		element = (Element) expression.parse();
	}

	protected void assertTokensEquals(Object ... expectedTokens) throws IOException {
		assertEquals(expectedTokens.length, tokens.depth());
		for (int i=0; i<expectedTokens.length; i++) {
			assertEquals("Invalid element #" + (i+1), expectedTokens[expectedTokens.length-i-1], tokens.value(i+1));
		}
	}

	protected void assertElementEquals(Object expected) throws IOException, TemplateException {
		assertEquals(expected, element);
	}
	
}
