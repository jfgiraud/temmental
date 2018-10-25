[logo]: http://temmental.sourceforge.net/logo.jpg "The ~$adjective~ template engine!"

## description

Temmental is a **_small_** template engine **_without dependency_** written in **_java_**.

The template syntax does not depend of manipulated documents.
 
You can use this template engine to generate _text_, _html_, _xml_... documents.

The template engine is made to raise exceptions as soon as possible when something wrong is detected.

## explore

You can explore the unit tests of the project, but i wrote a complex example: 

- [ExampleTest.java](./src/test/java/ExampleTest.java)
- [example.tpl](./src/test/resources/example.tpl)
- [example_en.properties](./src/test/resources/example_en.properties)
- [example_fr.properties](./src/test/resources/example_fr.properties)

## principles

To use the template engine, you need to create a template object. 

After that, you call print-methods with models to write on the expected output.

# the template

## constructors
  
```java
public Template(String filePath, Map<String, ? extends Object> transforms, Properties properties, Locale locale)
public Template(String filePath, Map<String, ? extends Object> transforms, Properties properties)
public Template(String filePath, Map<String, ? extends Object> transforms, Locale locale, Object... resourcesContainers)
public Template(String filePath, Map<String, ? extends Object> transforms, ResourceBundle bundle)
public Template(String filePath, Map<String, ? extends Object> transforms, String resourcePath)
public Template(String filePath, Map<String, ? extends Object> transforms, String resourcePath, Locale locale)
    
// all constructors throw IOException or TemplateException
```

## instantiation example

```java
template = new Template("src/test/resources/example.tpl", transforms, "file:src/test/resources/example.properties", locale);
```

In this example, the template object is created with:
- the path of the template file `"src/test/resources/example.tpl"`
- a map of named transform functions `transforms` 
- a locale `Locale.FRENCH` 
- a resourcePath `"file:src/test/resources/example.properties"` 

1. As you can see, the specified resource path doesn't exist. Only the localized resource pathes exist.
 The given locale will be used to compute the best resource file to use. 
2. If a transform function is used in the template file but not declared in the transforms map, an exception will be thrown.
3. If a property key is used in the template file but not declared in the properties, an exception will be thrown.

## using template object

You call print-methods with or without models (map of key/value) to write data on the given stream.

```java
    public void printFile(Writer out) throws TemplateException, java.io.IOException {

    public void printFile(Writer out, Map<String, ? extends Object> model) throws TemplateException,
            java.io.IOException {

    public void printSection(Writer out, String sectionName, Map<String, ? extends Object> model)
            throws TemplateException, java.io.IOException {

    public void printSection(Writer out, String sectionName) throws TemplateException, java.io.IOException {
```

1. If the model doesn't declare a variable used in the template or if the model has null value for that variable, an 
exception will be thrown.
2. Variables can also be used in the template for "property messages" or "transform functions" (indirection).

# The template

The template is a file containing sections or not.

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
tpl.printSection(out, "first", createModel("firstName", "John", "lastName", "Doe"));
```

There is no order between sections. The final rendering is done by the order of your _printSection_ calls in the java code.

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
tpl.printFile(out, createModel("firstName", "John", "lastName", "Doe", "fruits", Arrays.asList("orange", "apple", "banana")));
```

As the templates with sections, you can call `printFile` more than once.

# The message properties

The message properties is used to create the Template object. 

It contains key/value pairs to internationalize templates.

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


