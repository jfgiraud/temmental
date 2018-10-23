[logo]: http://temmental.sourceforge.net/logo.jpg "The ~$adjective~ template engine!"

Table of Contents:

* [temmental](#temmental)
    - [description](#description)
    - [example](#example)
    - [principles](#principles)
* [The template](#template)
    - [Example of file with 3 section](#example-of-file-with-3-sections)
    - [Example of file without section](#example-of-file-without-section)

<a name="temmental"/>

# temmental

<a name="description"/>

## description

Temmental is a *small* template engine *without dependency* written in *java*.

The template syntax does not depend of manipulated documents: 
You can use this template engine to generate text, html, xml... documents.

<a name="example"/>

## example

You can explore the unit tests of the project, but i wrote a complex example. Here are the links:
- [ExampleTest.java](./src/test/java/ExampleTest.java)
- [example.tpl](./src/test/resources/example.tpl)
- [example_en.properties](./src/test/resources/example_en.properties)
- [example_fr.properties](./src/test/resources/example_fr.properties)

<a name="principles"/>

## principles

To use the template engine, you need a template object. 
```java
    public Template(String filePath, Map<String, ? extends Object> transforms, Properties properties, Locale locale)
            throws IOException, TemplateException {

    public Template(String filePath, Map<String, ? extends Object> transforms, Properties properties)
            throws IOException, TemplateException {

    public Template(String filePath, Map<String, ? extends Object> transforms, Locale locale, Object... resourcesContainers)
            throws IOException, TemplateException {

    public Template(String filePath, Map<String, ? extends Object> transforms, ResourceBundle bundle)
            throws IOException, TemplateException {

    public Template(String filePath, Map<String, ? extends Object> transforms, String resourcePath)
            throws IOException, TemplateException {

    public Template(String filePath, Map<String, ? extends Object> transforms, String resourcePath, Locale locale)
            throws IOException, TemplateException {
```

Instanciation example:
```java
template = new Template("src/test/resources/example.tpl", transforms, "file:src/test/resources/example_fr.properties", locale);
```

In this example, the template object is created with:
- the path of the template file
```java
"src/test/resources/example.tpl"
```
- a map of named transform functions 
```java
transforms
```
- a resourcePath 
```java
"file:src/test/resources/example_fr.properties"
```
- a locale 
```java
Locale.FRENCH
```

After that, you call print-methods with or without models (map of key/value) to write data on the given stream.

```java
    public void printFile(Writer out) throws TemplateException, java.io.IOException {

    public void printFile(Writer out, Map<String, ? extends Object> model) throws TemplateException,
            java.io.IOException {

    public void printSection(Writer out, String sectionName, Map<String, ? extends Object> model)
            throws TemplateException, java.io.IOException {

    public void printSection(Writer out, String sectionName) throws TemplateException, java.io.IOException {
```


<a name="template"/>

# The template

The template is a file containing sections or not.

<a name="example-of-file-with-3-sections"/>

## Example of file with 3 sections

```
<!-- #section first -->
Hello ~$firstName~ ~$lastName:'upper~,
<!-- #section second -->
Do you want eat some fruits?
~$fruits#for<'fruit>~
  ~$fruit~
~#for~
<!-- #section third || last -->
Good bye.
```
The 3 sections are named: 'first', 'second' and 'third'. 'last' is an alias of 'third'.

When using sections, you should call __printSection__ method to render the section of the file.
You can call the __printSection__ on the same section as many times as you want.
```java
tpl.printSection(out, "first", createModel("firstName", "John", "lastname", "Doe"));
```

There is no order between sections. The final rendering is done by the order of your _printSection_ calls in the java code.

<a name="example-of-file-without-section"/>

## Example of file without section

```
Hello ~$firstName~ ~$lastName:'upper~,

Do you want eat some fruits?
~$fruits#for<'fruit>~
  ~$fruit~
~#for~

Good bye.
```
When using no section, you should use __printFile__ method to render the file.

```java
tpl.printFile(out, createModel("firstName", "John", "lastname", "Doe", "fruits", Arrays.asList("orange", "apple", "banana")));
```

# The message properties

The message properties is used to create the Template object. It contains key/value pairs to internationalize templates.

Example:



###HERE
###HERE
###HERE
###HERE
###HERE






















# The transform map

The transform map contains key/value pairs.

The values are functions to transform an input value to an output value.

For sample, __upper__, __lower__, __capitalize__ could populate the transform map to manipulate string cases. 

# The model

The model is a Map containing key/value pairs. The "holes" in template file we be replaced by these values after applying transform methods.

|syntax example|model|transforms|result|description|
|---|:---:|:---:|:---:|:---:|
|~$firstname~|{'firstname': 'John'}|x|John|Replace the tag firstname by the value associated to the key firstname contained in the model.|
|~$lastName:'upper~|{'lastname': 'Doe'}|{'upper':String.class.getDeclaredMethod("toUpperCase")|DOE||xxx

	

```
~$variable~     
~$variable?~
~$variable:'function~
~$variable?:'function~
~$variable:$function~
~$variable?:$function~
~'property[]~
~$property[]~

```


