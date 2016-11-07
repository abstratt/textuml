---
---
{% include header.md %}

Modeling structure
==================

*This document provides a brief description of the structural
elements of the TextUML notation and the UML concepts and elements they
represent. See [TextUML Action
Language](behavior.html "TextUML Action Language")
for the behavioral elements.*

This is a guide to the TextUML *notation*, not to the UML Language. It
is assumed the reader is familiar with UML.

Packages
--------

The top-level element of a compilation unit must always be a package
declaration:

    package <package-simple-name> ;

      // any number of packaged elements

    end.

Note the period following the 'end' keyword.

Packages can be nested into parent packages (i.e. packages are
packageable elements themselves). You can have the same package declared
in multiple compilation units, only the first one will actually cause
the package to be created.

### Syntactical nesting

One form of nesting a package into another, is by declaring the package
inside the parent package's declaration. For example:

    package top_level;

      package nested;

      end;

    end.

Note you don't use a period following the 'end' keyword for a nested
package that is declared inside its parent package.

### Logical nesting

Another form of nesting a package into another is by declaring the
package with a qualified name.

    package top_level::nested;
     
    end.

The 'top\_level' package must be declared elsewhere.

### Cross-package references

In order to refer to elements defined in a different package that is
part of the same repository, you can either:

1.  import the package defining the element and then refer to the named
    elements of interest using simple names
2.  use qualified names (qualified names are in the form
    namespace-1::namespace-2::...::namespace-n::named

***Note:** in the TextUML Toolkit, models are considered to be in the
same repository if a) they are at the root of the same TextUML (MDD)
project, or b) they are at the root of projects referenced by the
current TextUML (MDD) project. Also, note that even if your TextUML
source files are not stored at the root of your TextUML (MDD) project,
the generated UML files will be stored at the root of the project.*

#### Referencing external models

It is possible to reference models that are external to the current
repository. In order to do that, you first need to load the external
model using the following syntax:

    package referrent;

    load <<full-model-URI>>;

    end.

After loading the external model, the conventional rules for making
[cross-package
references](structure.html#Cross-package_references)
still apply.

Classifiers
-----------

### Classes

    [abstract | external] class <class-name> 

      [specializes <super-class-name> [, ...] ] 

      [implements <interface-name> [, ...] ]

     // class features (operations, attributes, etc)

    end;

Classes can also be declared as abstract (not instantiable) or external
(meant to be implemented elsewhere).

### Interfaces

Except for using a different keyword ("interface") and the lack of an
"implements" section, the syntax for declaring interfaces is much like
that one for classes.

    interface <interface-name> 
      [specializes <super-interface-name> [, ...] ]

     // interface features (operations, attributes, receptions etc)

    end;

### Data Types 
(1.2+)

Data types also use a similar syntax as class declarations, with a
different keyword ("datatype"). Also, data types cannot implement
interfaces (as they are not behaviored classifiers), so that section is
not supported either.

    datatype <data-type-name> 
      [specializes <super-data-type-name> [, ...] ]

     // data type's structural features (operations, attributes, etc)

    end;

### Primitive Types 
(1.2+)

Primitive types have the simplest syntax of all classifiers:

    primitive <primitive-type-name>;

Differently from all other classifiers, primitive types cannot declare
any features nor can specialize other types or realize interfaces.

### Enumerations

You can specify enumerations using the following syntax.

    enumeration <enumeration-name>
      <literal-1> [, ... <literal-n>]  
    end;

For example:

    enumeration UserStatus
      registered, active, inactive
    end;

There is no support at this time for specifying explicit values for
literals.

### Components 
(1.10+)

    component <component-name>
        <parts>
        <ports>
    end;

For example:

    component ExpensesApp
        composition expensePaymentService : EmployeePaymentService;
        composition expenses : Expense[*];
        provided port expensePayer : ExpensePayer connector expenses.expensePayer, expensePaymentService;
    end;

### Signals
(1.10+)

    signal <signal-name>
        <attributes>
    end;

For example:

    signal ExpenseApproved
        attribute employeeName : String;
        attribute amount : Double;
        attribute description : String;
        attribute expenseId : Integer;
    end;

### Special classifiers

See also sections on the following classifiers:
[stereotypes](structure.html#Stereotypes)
and
[associations](structure.html#Associations).

Features
--------

These can appear nested under any classifiers.

### Operations

      [<modifiers>] operation <operation-name>([ [<param-direction>] <param-name> : <param-type-name> [, ...] ]) [: <return-type>];

See also:
[modifiers](structure.html#Modifiers)
and
[multiplicity](structure.html#Multiplicity).

### Properties or Attributes

      [<modifiers>] attribute <attribute-name> : <attribute-type-name> [:= <initial-value>];

See also:
[references](structure.html#References),
[constants](structure.html#Constants),
[modifiers](structure.html#Modifiers)
and
[multiplicity](structure.html#Multiplicity).

#### Constants

Constants are basically read-only attributes that can have an expression
assigned to them.

      [<visibility>] constant <constant-name> : <attribute-type-name>; [:= <initial-value-expression>]

Whereas \<initial-value-expression\> must match the type of the
attribute.

For example:

    class User
      constant LEVEL_GUEST : Integer := 1; 
      constant LEVEL_NORMAL : Integer := 2;
      constant LEVEL_ADMIN : Integer := 3; 
      constant LEVEL_ANY : Integer := 0;
      ...
    end;

See also:
[modifiers](structure.html#Modifiers).

### Ports

    [required | provided] port <port-name> : <port-type> [connector <list-of-connection-ends>];

Port types must be interfaces. A port is required by default.

See examples in
[components](structure.html#Components).

### Receptions 
(1.10+)

Receptions must have one and only one parameter, which must be of a
signal type.

    reception [<reception-name>](<signal-param-name> : <signal-type-name>);

Example:

    reception expenseApproved(approval : ExpenseApproved);

\

### Modifiers

#### Visibility

Features can have one of the four different kinds of visibility:

-   public (1.7+: default for operations and attributes, 1.6 and
    earlier: default for operations)
-   private (1.6 and earlier: default for attributes)
-   protected
-   package

#### Static

The presence of the 'static' modifier causes the feature to be a
class-level one, instead of an instance level one.

#### Abstract

Classes and operations can be marked as 'abstract'.

#### Parameter direction

Operation parameters can optionally be marked as 'in' (the default),
'out' or 'inout'.

#### Read-only
(1.7+)

Attributes (a.k.a properties) can be declared as 'readonly'.

#### Derived
(1.6+)

Attributes (a.k.a properties) can be declared as 'derived'. In that
case, you need to provide the behavior that defines the property's value
via a [closure
literal](behavior.html#Closures "TextUML Action Language")
(except if the attribute is an id, see below).

#### Id
(1.10+)

Attributes can be declared as 'id'.

#### Port direction 
(1.10+)

Ports can be declared as 'required' (default) or 'provided'.

### Multiplicity

When declaring association ends, attributes, operation parameters and
return types, you can specify a multiplicity along with the type.

For example:

    attribute address : Address[*];
    operation op1 (x : Boolean) : Integer[1,*] {ordered};

You can omit the lower bound. If it the upper bound is unlimited ('\*'),
the default lower bound is zero; otherwise (it is a natural number), the
lower bound is equal to the upper bound.

You can also specify (between curly braces) one of each pair of
multiplicity modifiers: ordered or unordered, nonunique or unique. The
default according to the UML specification is {unordered, unique}.

Associations
------------

Associations don't have to be named. They can be ordinary associations,
or compositions, or aggregations. The last two types of association can
declare only two ends. Finally, association ends can be owned by the
association, or by the members of the association.

    association | composition | aggregation [<association-name>] 

      // association-owned ends
      [ [!]navigable] role <role-name> : <associated-class-name>; ]
      ...

      // member-owned ends
      [ [!]navigable] role <member-class-name>.<role-name>; ]
      ...   
     
    end;

Note that at least one of the ends must be navigable, which is the default. Navigability can be avoided by prepending the navigable modifier with '!'. 

**Note**: currently, the TextUML Toolkit supports binary associations
only.

### Shorthand notation

Simpler *reference*, *composite* and *aggregation* declarations (the
latter two, from 1.2 on only) can appear wherever an attribute can be
declared and provides a shorthand notation for declaring an anonymous
association/composition/aggregation between the containing class and the
reference type, where one end is owned by the declaring class.

      [ reference <reference-name> : <referenced-type-name>; ]

(1.2+)

      [ composition <reference-name> : <referenced-type-name>; ]
      [ aggregation <reference-name> : <referenced-type-name>; ]

State machines 
----------------------
(1.10+)


Classes can declare state machines. States can be regular, initial or
terminate. Transitions can be triggered by events (currently only
operation calls can trigger transitions).

For example:

    class MyClass
        attribute value : Integer;
        attribute status : SM1;
        operation action1();
        operation action2();
        operation action3();
        statemachine SM1
            initial state State0
                transition on call(action1) to State1;
            end;
            state State1
                transition on call(action1) to State1 when { self.value > 3 };
                transition on call(action1) to State2 when { self.value <= 3 };
                transition on call(action2) to State2;
            end;
            state State2
                transition on call(action1) to State1;
                transition on call(action3) to State3;
            end;
            terminate state State3 
            end;
        end;
    end;

### State specification

You can specify activities to be performed when transitions are
triggered or when states are entered/exited/assumed:

    statemachine <state-machine-name>
        [initial | terminate] state [<state-name>]
            [entry { <statements> }]
            [exit { <statements> }]
            [do { <statements> }]

            transition on <trigger> to <next-state> 
                [when { <guard-boolean-expression> }] 
                [do { <statements> }];
            [transition on ... ;]
         end;

         [state ... end;] 
    end;

Dependencies
-------------------
(1.3+)

Classifiers can declare dependencies on any types available.

For example:

    class <class-name>
      dependency <type-name1>;
      dependency <type-name2>;
      ...
      dependency <type-nameN>;
    end;

Profiles
========

Profiles are declared much like
[packages](structure.html#Packages),
just using the keyword 'profile' instead of 'package'. Profiles cannot
be nested. You use profiles for declaring
[stereotypes](structure.html#Stereotypes).
Profiles and stereotypes provide a lightweight mechanism for extending
UML.

Stereotypes
-----------

Stereotypes declare the metaclasses they extend and optionally some
properties. You must declare stereotypes in profiles, never in plain
packages or models.

    stereotype <stereotype-name> 

        [extends <meta-class-name> [, ...] ] 

        [ property <property-name> : <property-type-name>; ]
        ...
     
    end;

Example:

    profile business_apps;

    stereotype persistent extends uml::Class
    end;

    stereotype transactional extends uml::Operation
      property exclusive : Boolean;
    end;

    end.

Note that the UML metamodel package is "uml". **Warning**: in TextUML
1.8, when UML2 4.x is used, the metamodel library to import is "UML"
instead of "uml".

Also, see the section on
[annotations](structure.html#Annotations)
to learn how to use stereotypes in your models.

### Required extensions
(1.2+)

In the example below, the stereotype foo is required to be applied to
any operation element.

    profile my_profile; 

    import uml; 

    stereotype foo extends Class, Operation required
    end;

    end.

Annotations
-----------

Annotations are stereotype applications. According to the UML
specification, you can only apply a stereotype to an element if:

-   that stereotype declares it extends that element's meta-class and
-   the profile declaring the stereotype is *applied* to the package
    that declares the element to annotate.

If a stereotype declares properties, annotations can also provide values
to those properties (a.k.a. *tagged values*), using literals
corresponding to the property type.

For example:

    model bank;

    // apply profile so we can use stereotypes declared in it
    apply business_apps;

    [persistent]
    class Account
        attribute accountNumber : base::String;
        attribute balance : base::Real;
        attribute changes : AccountChange[0,*];
        [transactional(exclusive=true)] operation withdraw(amount : Real);
        [transactional] operation deposit(amount : Real);
        operation balance() : Real;
        [transactional] operation transfer(other : Account, amount : Real);
    end;

    end.

Since version 1.3: besides numbers, booleans and strings, you can also
use
[enumeration](structure.html#Enumerations)
literals when applying a stereotype.

Since 1.4, you can also annotate generalizations and interface
realizations.

Since 1.7, you can also annotate operation parameters.

Metamodel extensions
====================

Defining a meta-model (for implementing a UML [middleweight
extension](http://www.eclipse.org/modeling/mdt/uml2/docs/articles/Customizing_UML2_Which_Technique_is_Right_For_You/article.html#_Hlt174419544 "http://www.eclipse.org/modeling/mdt/uml2/docs/articles/Customizing_UML2_Which_Technique_is_Right_For_You/article.html#_Hlt174419544"),
for example) is as easy as applying the Standard profile to your model
and annotating the model and elements with the appropriate annotations:

    [Standard::Metamodel]
    model meta;

    apply Standard;

    [Standard::Metaclass]
    class MetaValueSpecification specializes uml::ValueSpecification
        attribute metaValue : uml::Element;    
    end;

    end.

In this example, a new kind of ValueSpecification that can refer to any
model element (thus the name, MetaValueSpecification) was created.

Subsetting
----------

You can also specify subsetting:

    [Standard::Metamodel]
    model meta;

    (* A signature is a classifier with parameters. *)
    [Standard::Metaclass]
    class Signature specializes uml::Classifier
      attribute ownedParameter : uml::Parameter[*] subsets uml::Namespace::member;
      attribute raisedException : uml::Classifier[*];
    end; 

    composition A_ownedParameter_signature
      role Signature.ownedParameter;
      role signature : Signature;
    end;

    end.

Reserved words
==============

This is a list of reserved words in the TextUML notation as implemented
by the TextUML Toolkit today. Given it is so early in the development of
the notation, many new keywords should be added in the future.

     abstract 
     access
     actor
     aggregation
     alias
     and
     any
     apply
     association
     as
     attribute
     begin
     broadcast
     by
     call
     catch
     class
     create
     component
     composition
     connector
     datatype
     delete
     derived
     destroy
     do
     else
     elseif
     end
     entry
     enumeration
     exit
     extends
     extent
     external
     false
     finally
     function
     id
     if
     implements
     import
     in
     initial
     inout
     interface
     invariant
     is
     link
     load
     model
     navigable
     new
     nonunique 
     not 
     null
     on
     operation 
     or
     ordered 
     out
     package
     port
     postcondition 
     precondition
     private
     primitive 
     profile 
     property 
     protected
     provided
     public
     raise
     raises
     read
     readonly
     reception
     reference
     repeat
     required 
     return
     role
     self
     send
     signal
     specializes
     state
     statemachine
     static
     status
     stereotype
     subsets
     terminate 
     then
     to
     transition 
     true
     try 
     type 
     unique
     unlink
     unordered 
     until
     update 
     var 
     when
     where
     while

Avoiding name clashing with keywords
------------------------------------

Escaping (using the '\\' backslash character) can be used to avoid name
clashing with keywords. For example:

    package \package;

    class \class
      operation \operation();
      attribute \attribute : \class;
    end;

    end.

"\\foo" will be translated into "foo" after parsing, no traces of the
escaped string will exist anywhere other than the source.

{% include footer.md %}    

