[logo]: http://temmental.sourceforge.net/logo.jpg "The ~$adjective~ template engine!"

# Description

Temmental is a small template engine without dependency written in java.

The template syntax does not depend of manipulated documents.

You can use this template engine to generate text, html, xml... documents.

# Principles

To use the template engine, you need :
- a template object
- a messages properties (to facilitate internationalization)
- a map with declared transform functions. 

After that, you call print-methods with models (map of key/value) to write data on the given stream.

# The template

The template is created from a file (Template) or a String (StringTemplate).

It can contain sections or not. 

Example:
[example.tpl](./src/test/resources/example.tpl)

If a template contains no section, you should use __printFile__ to render the content, otherwise you should use a succession of __printSection__ to render the content.    

Sample :

``

```java
template = new Template("src/test/resources/example.tpl", filters, "file:src/test/resources/example_fr.properties", locale);
```







