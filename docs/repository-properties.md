Repository properties are stored in a properties file named
mdd.properties under the repository base project (project root, in
Eclipse).

This is an example:

    mdd.extendBaseObject=true
    mdd.aliases.base\:\:Real=mypackage\:\:MyReal


mdd.aliases
-----------

Not a property per se, a prefix for aliases. Example:

    # map references to package 'foo' to package 'bar' (foo::MyClass becomes bar::MyClass)
    mdd.aliases.foo=bar 

    # maps base::Object to mypackage::mysubpackage::MyObject
    map.aliases.base\:\:Object=mypackage\:\:mysubpackage\:\:MyObject

Note that this file uses the [Java Properties file
format](http://java.sun.com/javase/6/docs/api/java/util/Properties.html#load%28java.io.Reader%29 "http://java.sun.com/javase/6/docs/api/java/util/Properties.html#load%28java.io.Reader%29"),
so colon and equals must be escaped.

mdd.enableTypes
---------------

If defined and set to *true*, the built-in
[mdd\_types](https://github.com/abstratt/textuml/blob/master/plugins/com.abstratt.mdd.core/models/libraries/mdd_types.tuml)
library package becomes available. You can then import mdd\_types in
your models.

mdd.enableCollections
---------------------

If defined and set to *true*, the built-in
[mdd\_collections](https://github.com/abstratt/textuml/blob/master/plugins/com.abstratt.mdd.core/models/libraries/mdd_collections.tuml)
library package becomes available. In order to use the [TextUML Action
Language](behavior.md),
you need to enable this property.

mdd.enableLibraries
-------------------

A shortcut for enabling all built-in libraries (types and collections).

mdd.enableExtensions
--------------------

If defined and set to *true*, the built-in
[mdd\_extensions](https://github.com/abstratt/textuml/blob/master/plugins/com.abstratt.mdd.core/models/profiles/mdd_extensions.tuml)
profile becomes available (you don't need to apply it). In order to use
the [TextUML Action
Language](behavior.md),
you need to enable this property.

mdd.extendBaseObject
--------------------

If defined and set to *true*, all classes with no explicit ancestor will
extend from a class named base::Object. If you would like to extend from
a different class, you can use *mdd.aliases* to map from base::Object to
the base class of your choice. If you want to use the built-in Object
class, then you will need to enable mdd.enableTypes or
mdd.enableLibraries or else the compiler won't be able to find
base::Object.

mdd.defaultLanguage
-------------------

If defined, sets an implicit extension for extension-less files. Set to
'tuml' so you don't need to explicitly use .tuml in your TextUML source
files.
