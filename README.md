![Logo](/temmental.jpg)

## description

Temmental is a **_small_** template engine **_without dependency_** written in **_java_**.

The template syntax does not depend of manipulated documents. You can use this template engine to generate _text_, _html_, _xml_... documents.

The template engine is made to raise exceptions as soon as possible when something wrong is detected.

## explore

You can explore the unit tests of the project, but a complex example is available: 

- [ExampleTest.java](./src/test/java/ExampleTest.java)
- [example.tpl](./src/test/resources/example.tpl)
- [example_en.properties](./src/test/resources/example_en.properties)
- [example_fr.properties](./src/test/resources/example_fr.properties)

## principles

To use the template engine, you need to create a template object. 

After that, you call print-methods with models to write on the expected output.

# Template class

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

# The template file `.tpl`

The template is a file containing sections or not.

## example of file with 3 sections

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

## example of file without section

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
```text
hello_dear=Bonjour {0} {1}
client_number=Num\u00e9ro client : {0}
account_number=Num\u00e9ro de compte : {0}
line_number=Num\u00e9ro ligne : {0}
unknown=inconnu
eeddmmyyyy=EEEEEEEEEEEEEE dd MMMMMMMMMMMMMM yyyy
ddmmyyyy=dd/MM/yyyy
client_since=Anciennet\u00e9 : {0}
email=Email: {0}
# properties are iso-8859-1, so i use unicode sequence for euro sign
price={0}\u20ac
duration={0} secondes
sms={0} SMS
you_have=Vous avez {0,choice,0#aucune option|1#une option|1<{0,number,integer} options} :
```

The message properties contains localized messages. You can write your template without defining properties but its better to define a such file.

A property message can:
- require multiple parameters to be used
- define a format to display a date and/or an hour
- be use to manipulate plurials
- ... 

You can refer to the [MessageFormat](https://docs.oracle.com/javase/1.5.0/docs/api/java/text/MessageFormat.html) class of the java library.

Example of use in `.tpl` file:

```text
~'hello_dear[$firstName,$lastName]~
```

Example of use in `.java` file:
```text
Map<String, Object> model = new HashMap<String, Object>();
model.put("firstName", "John");
model.put("lastName", "Doe");
StringWriter out = new StringWriter();
template.printFile(out, model);
```

# The transform map

The transform map contains key/value pairs.

The values are functions to transform an input value to an output value.

For sample, __upper__, __lower__, __capitalize__ could populate the transform map to manipulate string cases.

A sequence of transform functions can be used in the template to render data. 

Example of declarations:
```java
transforms = new HashMap<>();
transforms.put("upper", String.class.getDeclaredMethod("toUpperCase"));
transforms.put("size", Transforms.SIZE);
transforms.put("gender", new ParamTransform<String[], Character, String>() {
    @Override
    public String apply(String[] values, Character c) {
	if (c == 'f')
	    return values[0];
	if (c == 'm')
	    return values[1];
	return StringUtils.join(", ", Arrays.asList(values[0], values[1]));
    }
});
transforms.put("titleize", StringUtils.class.getDeclaredMethod("titleize", String.class));
transforms.put("date_formatter", new Transform<String[], Transform<Date, String>>() {
    public Transform<Date, String> apply(final String[] objects) {
	return new Transform<Date, String>() {
	    public String apply(Date value) {
		return new SimpleDateFormat(objects[0], locale).format(value);
	    }
	};
    }
});
transforms.put("toModel", TemplateUtils.getDeclaredMethod(ConvertToModel.class, "toModel", null));
transforms.put("add", Transforms.ADD);
```

As you can see, you have multiple ways to declare transform functions.

# The model

The model is a Map containing key/value pairs. 

These key/value pairs will be used to compute the code between tildes (variables, messages, commands...)

The result of the computing is used to render your data in the template.

The best way to understand the code of a template is to read the given example: [example.tpl](./src/test/resources/example.tpl) 

```text
<!-- #section header || other -->
~$firstName~ ~$lastName:'upper~                                        Bordeaux, ~$date:'date_formatter<'eeddmmyyyy[]>:'titleize~
~|t|$streetLines#for<'streetLine>~
    ~|lt|$streetLine:'titleize~
~|t|#for~
~$zip~ ~$city:'upper~
~$country!"FRANCE"¡~
~'email[$email?]~

~'client_number[$clientNumber]~
~'account_number[$accountNumber!'unknown[]¡]~
~'line_number[$lineNumber!'unknown[]¡]~
~'client_since[$inscription?:'date_formatter<'ddmmyyyy[]>]~

<!-- #section body -->

~$genre:'gender<"Madame","Monsieur">~,

Veuillez trouver la facture relative à votre ligne.

~'you_have[$options:'size]~
~|rt|$options#for<'option>~
    ~|lt|$option:'toModel#override~  - ~|rt|~
        ~|lt|$label~: ~'price[$price]~ (~$unit[$quantity]~)
    ~|lt,rt|#override~
~|lt|#for~
~$totaux:'toModel#override~~$label~: ~'price[$price]~~#override~
```

Small explanations,
- `$variable` is a variable (so defined in the model). 
- `:'function` is a transform function (so defined in the transform map)
- `:$function` is a dynamic transform function (function is defined in the model, the associated value must be defined in the transform map)
- `:'function<$parameter1,...,$parameterN>` is a parametrized transform function (so defined in the transform map). 
- `:$function<$parameter1,...,$parameterN>` is a parametrized dynamic transform function (function is defined in the model, the associated value must be defined in the transform map)
- `'property[$parameter1,...,$parameterN]` is a message. The property named 'property' is defined in the resource file.
- `$property[$parameter1,...,$parameterN]` is a dynamic message. The value associated with the 'property' variable must be defined in the model and the value property must be defined in the resource file.
- `$xxx!'unknown[]¡` is a default replacement. If xxx is not defined in the model or has null value, the exception will be catched and $xxx will be replaced with the message `'unknown[]` 


|syntax example|model|
|---|:---:|
|`<!-- #section header \|\| other -->`|define the section named header. other is an alias to header|
|`~$firstName~`|the value associated with the firsName key in the model will be written|
|`~$lastName:'upper~`|the lastName will be written after being upperized (upper is defined in the transform map)|
|`Bordeaux, `|static text|
|`~$date:'date_formatter<'eeddmmyyyy[]>:'titleize~`|a date instance is transformed by the date formatter to the format defined in the properties. The result is titleized|
	
