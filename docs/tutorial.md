---
---

{% include header.md %}

TextUML Toolkit Tutorial
===================

*This tutorial shows how to use the TextUML Toolkit for creating UML
models. It starts from a minimal model and then increasingly adds more
and more features to it.*

TextUML is a textual notation for the popular Unified Modeling Language
instead of a new language. It is a common misconception that UML is
about boxes and arrows. There is a graphical notation for UML, but the
semantics are defined completely independently of the graphical
notation, so alternative notations, including textual ones such as the
TextUML Toolkit, are possible. All the concepts and mechanisms exposed
in TextUML are defined by the UML specification. This is **not** an
introduction to UML (if that is what you are looking for, you might be
interested in the [UML
101](uml_101.html "UML 101")
article).


Preparation
===========

1.  if you haven't yet, [install the TextUML
    Toolkit](install.html "Install Instructions")
2.  start Eclipse
3.  create a new MDD project named "tutorial" by using the project
    creation wizard (File \> New \> Project... \> MDD \> MDD Project)
4.  inside the project you just created, create a new file named
    'inventory.tuml'

After the last step, since the file will be empty, there will be a
problem marker telling you that a package declaration is expected. That
takes us to the first step in this tutorial...

Creating an empty package
=========================

The first element to be created in a UML class diagram is a package. The
following snippet creates an empty package named 'inventory':

    package inventory;

    end.

Notice that when you save the source file, the builder kicks in and a
new 'inventory.uml' file appears in the project. That is the actual UML
model. It can always be derived from the TextUML source file, or in
other words, it is the object code. So if you use a team repository, you
do not have to share the compiled model files, only the source files
(which are easier to do comparisons, for instance).

Creating a new class
====================

Let's now edit the inventory.tuml file and declare a 'Product' class:

    package inventory;

    (* This is the Product class *)
    class Product

    end;

    end.

Diagram showing a class

To declare an interface instead of a class, you just replace the keyword
class with interface.

Element comments and source-level comments
==========================================

Also, note in the previous example how comments are declared. There are
actually two kinds of comments in TextUML. Comments that should appear
in the resulting UML model (element comments) use Pascal-like delimiters
'(\*' and '\*)'. Ordinary source level comments that are just ignored by
the compiler use C-like '/\*' and '\*/' as delimiters.

Basic types
===========

Previous versions of the TextUML Toolkit had built-in predefined
packages, such as the 'base' model and the 'base\_profile' profile.
Starting with M4, you have now to define any required basic types
yourself. Luckily, that is quite simple. Let's do that by declaring
three classes: dataType::Integer, dataType::String and dataType::Real.
Create a new file in your project and name it dataType.tuml. Then type
in the following:

    package dataType;

    primitive Integer; 

    primitive String; 

    primitive Real; 

    end.

We can now use these types in other packages in the same project, as
shown in the next section.

Adding attributes to a class
============================

Now that we defined the basic types we needed, let's see how we can
declare attributes on the Product class:

    package inventory;

    class Product
      attribute description : dataType::String;
      attribute available : dataType::Integer;
      attribute price : dataType::Real;
    end;

    end.

The syntax for declaring attributes should be quite obvious. Notice the
names for the attribute types are qualified. That is because we didn't
import the package defining them. If we import the 'dataType' package,
we don't need to qualify the type names:

    package inventory;

    import dataType;

    class Product
      attribute description : String;
      attribute available : Integer;
      attribute price : Real;
    end;

    end.

Diagram showing attributes

Note that if you don't mind using qualified names, there is no need to
import packages, you can always refer to elements in any other model in
the same project.

Creating associations
=====================

Associations are first-class citizens in UML, differently from what
occurs in most programming languages. In TextUML, associations are
declared as top-level elements.

In order to demonstrate the syntax for defining associations and
aggregations, let's first create two new classes named 'Cart' and
'CartItem' in a new 'shopping\_cart' package (in a file named
'shopping\_cart.tuml'):

    package shopping_cart;

    import dataType;
    import inventory;  

    class Cart
    end; 

    class CartItem
      attribute quantity : Integer;
    end;

    end.

Note you declared two classes in the same source file. That is alright.
It actually does not matter in what source file an element is declared.
The actual package it will be generated into is defined by the package
header.

Now, let's create an aggregation between Cart and CartItem and an
unidirectional association from CartItem to Product:

    package shopping_cart; 

    import dataType;
    import inventory;

    class Cart
    end;

    class CartItem
    end;

    association CartItemProduct
      navigable role item : CartItem[*];
      role product : Product[1]; 
    end;

    aggregation CartHasItems
      navigable role cart : Cart[1];
      navigable role item : CartItem[*];
    end;

    end.

For all types of associations, the syntax is basically the same - you
specify the kind of association by specifying the corresponding keyword
(among association, composition and aggregation).

Diagram showing associations

Adding operations
=================

The syntax for declaring operations is quite straightforward. See, for
example, how we'd go about adding operations to the classes in
'shopping\_cart':

    package shopping_cart;

    class Cart
      operation createItem(product : Product, quantity : Integer) : CartItem;
      operation removeItem(item : CartItem);
      operation checkout();
    end; 

    /* other elements here */

    end.

Diagram showing operations

Generalizations (a.k.a. subclassing)
====================================

To say a class subclasses another, you use the *specializes* keyword.
UML allows for multiple inheritance, so you can specify a
comma-separated list of superclasses (incidentally, for declaring
interfaces implemented by a class, you use the keyword implements
followed by a comma-separated list of interface names). See below for an
example:

    package payment;

    class PaymentMethod 
    end;

    class Cheque specializes PaymentMethod
    end; 

    class Paypal specializes PaymentMethod
    end;

    class CreditCard specializes PaymentMethod
    end;

    class Visa specializes CreditCard
    end; 

    class AmericanExpress specializes CreditCard
    end;

    class Diners specializes CreditCard
    end;

    end.



Diagram showing generalization

Note that the keyword *extends* is also supported in TextUML but is used
when declaring stereotypes.

Conclusion
==========

This ends our tour on using TextUML for creating a simple class model.
If you have any questions or suggestions about the article or the tool,
or found any bugs when trying it, just post a question to the [users
forum](https://groups.google.com/forum/#!forum/textuml-toolkit "https://groups.google.com/forum/#!forum/textuml-toolkit") or
[open an
issue](https://github.com/abstratt/textuml/issues "https://github.com/abstratt/textuml/issues") and it
will be addressed asap.

{% include footer.md %}