---
---
It is a little known fact that TextUML also comprises a full-blown
action language for UML. The TextUML Toolkit supports the TextUML action
language since release 1.2.

You will find some examples of the TextUML action language if you follow
our blog's [action language
category](http://abstratt.com/blog/category/uml/action-language/ "http://abstratt.com/blog/category/uml/action-language/").

You can find the EBNF grammar for the TextUML notation, which includes
both the structural and behavioural aspects of the
notation, [here](../plugins/com.abstratt.mdd.frontend.textuml.core/textuml.scc).

This complements the documentation for [structural
aspects](structure.html)
of the TextUML notation.


Requirements
------------

In order to use the TextUML Action Language, the following properties
**must** be enabled in the
[mdd.properties](repository_properties.html)
file:

-   mdd.enableExtensions=true
-   mdd.enableLibraries=true

Expressions
-----------

### Current object

    self

### Object instantiation

     new <class-name>

Example:

     newCustomer := new Customer;

### Attribute read

    <object-expression>.<attribute-name>

Example:

    aName := customer.name;

Note that *object-expression* is not optional, so for accessing an
object's own attributes, always use *self*.



### Instance operation invocation

    <object-expression>.<operation-name>(<argument-list)

Example:

    account.withdraw(200)

Again, *object-expression* is not optional, so for invoking an object's
own operation, always use *self*.

### Class operation invocation

    <class-name>#<operation-name>(<argument-list)

Example:

    Client#create("John Doe")

### Link traversal

    <source-object-expression> -> <association-name> -> <target-role-name>

or (if the target role is owned by the source class):

    <source-object-expression> -> <target-role-name>

Examples:

    anEmployee := anExpense->ExployeeExpense->employee

or

    anEmployee := anExpense->employee

### Class extent

    <class-name> extent

Example:

    Customer extent

Statements
----------

Statements are ended by a semicolon (optional for single-statement
blocks). Note that expressions can also be used as statements.

### Writing to a local variable

    <variable-name> := <expression>;

Example:

     aName := "John Doe";

### Writing to an attribute

    <object-expression>.<attribute-name> := <expression>;

Example:

     employee.name := "John Doe";

### Linking two objects

    link <association-identifier> (
        <role-identifier-1> := <object-expression-1>, 
        <role-identifier-2> := <object-expression-2>
    );

Example:

    link EmployeeExpenses (employee := anEmployee, expense := anExpense);

### Unlinking two objects

    unlink <association-identifier> (
        <role-identifier-1> := <object-expression-1>, 
        <role-identifier-2> := <object-expression-2>
    );

Example:

    /* anEmployee and anExpense are currently linked, disconnect them */
    unlink EmployeeExpenses (employee := anEmployee, expense := anExpense);

### Destroying an object

     destroy <object-reference>;

Example:

     destroy itemOrder;

### Returning a value

    return [<expression>];

Example:

    return self.balance > 0;

### Raising an exception

    raise <expression>;

Example:

    raise new InsufficientFunds;

### Handling an exception

    try
      <protected body>
    catch (<exception-declaration)
      <handler body>
    end;

Example:

    try
      account.withdraw(100);
    catch (e : InsufficientFunds)
      /* handle exception */
    end;

### Sending a signal

    send <signal-name>(
        [<attribute-1> := <value-expression-1> 
        [, ... <attribute-n> := <value-expression-n>]]
    ) to <object-expression>;

Example:

    send ExpenseApproved(
        employeeName := "John Nader",
        amount := 205.05,
        description := "Trip to LA"
    ) to paymentApprover;

Built-in operators
------------------

### Comparison operators

-   Identity (for objects): ==
-   Equality (for values): =
-   Greater than: \>
-   Greater or equal to: \>=
-   Lower than: \<
-   Lower or equal to: \<=

### Arithmetic operators

-   Binary: +, -, \*, /
-   Unary: -

### Boolean operators

-   Binary: and, or, xor
-   Unary: not

### String operators and methods

-   Concatenation: + (example: ("foo" + "bar") = "foobar")
-   Size: size() (example: "foo".size() = 3)
-   Case change: toLower()/toUpper() (example: "Foo".toLower() = "foo",
    "bar".toUpper() = "BAR")

### Type conversion (cast)

    <object-expression> as <target-classifier>

Example:

    (c as Customer)

### Collection operations

See:
[mdd_collections.tuml](../plugins/com.abstratt.mdd.core/models/libraries/mdd_collections.tuml)

Literals
--------

### Primitive literals

-   String: "foo"
-   Integer: 10
-   Double: 10.0
-   Boolean: true/false

### Closures

    (<parameter-list>) [: <return-type>] { <statement-list> }

Example:

    begin
      var comparator : {(:Integer,:Integer) : Boolean}
      comparator := (p1 :  Integer, p2 : Integer) : Boolean { return p1 > p2 };
      ...
    end;
    
    
{% include footer.md %}
