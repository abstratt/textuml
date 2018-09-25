package com.abstratt.mdd.core.tests.frontend.textuml

import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests
import com.abstratt.mdd.core.tests.harness.AssertHelper
import com.abstratt.mdd.frontend.textuml.renderer.ActivityGenerator
import com.abstratt.mdd.frontend.textuml.renderer.ActivityRenderer
import junit.framework.Test
import junit.framework.TestSuite
import org.eclipse.uml2.uml.Operation

import static extension com.abstratt.mdd.core.util.ActivityUtils.*

class ActivityGeneratorTests extends AbstractRepositoryBuildingTests {
	
	String source
	val generator = new ActivityGenerator()
	
	new(String name) {
		super(name)
	}
	
    def public static Test suite() {
        return new TestSuite(ActivityGeneratorTests)
    }
    
	def compileModel(String header, String behavior) throws Exception {
		val actualHeader = if (header.nullOrEmpty) "operation templateOp();" else header
	    source = '''
            model someModel;
            import base;
            
            signal MySignal
                attribute value1 : String;
                attribute value2 : Integer;
            end;
            
            association MyAssociation
                role role1 : MyClass;
                role role2 : MyClass2;
            end;
            
            class MyClass2
            end;
            
            class MyClass
                attribute attr1 : String;
                attribute attr2 : Integer;
                operation op1();
                «actualHeader»
                begin
                    «behavior»
                end;
            end;
            end.
    	'''
        parseAndCheck(source)
	}
	
	def check(String header, String behavior) {
		compileModel(header, behavior)
		val generated = generate("templateOp")
        AssertHelper.assertStringsEqual(behavior, generated.toString)
	}
	
	def check(String behavior) {
		check("", behavior)
	}
	

	def testCallOperation() {
        check('''self.op1();''')
	}
	
	def testSendSignal() {
        check('''send MySignal(value1 := "foobar", value2 := 30) to new MyClass;''')
	}
	
	
	def testCreateObject() {
		check('''new MyClass;''')
	}
	
	def testCreateLink() {
		check('''link MyAssociation(role1 := new MyClass, role2 := new MyClass2);''')
	}
	

	def testWriteAttribute() {
		check('''self.attr1 := "foobar";''')
	}
	
	def testCallOperation_Operator() {
		check('''self.attr2 := self.attr2 * 2;''')
	}
	
	def testConditional() {
		check('''
		if (self.attr2 > 0) then
		begin
			self.attr2 := self.attr2 - 1;
		end;
		''')
	}
	
	def testConditional_else() {
		check('''
		if (self.attr2 > 0) then
		begin
			self.attr2 := self.attr2 - 1;
		end
		else
		begin
		    self.attr2 := self.attr2 * (-1);
		end;
		''')
	}
	
	def testConditional_else_if() {
		check('''
		if (self.attr2 > 0) then
		begin
			self.attr2 := self.attr2 - 1;
		end
		else
		begin 
		    if (self.attr2 < 0) then
		    begin
			    self.attr2 := self.attr2 + 1;
		    end
			else
			begin
			    self.attr2 := 1;
			end;
		end;
		''')
	}

	def testCallOperation_Collection_emptySet() {
		check('''
			query templateOp() : MyClass[*];
			''',
			'''
			return MyClass[];
			''')
	}

	def testCallOperation_Collection_extend() {
		check('''
			query templateOp() : MyClass[*];
			''',
			'''
			return MyClass[].extend(self);
			''')
	}
	
	def testReadAttribute() {
		check('''
			query templateOp() : String;
			''',
			'''
			return self.attr1;
			''')
	}
	
	def testTestIdentity() {
		check('''
			query templateOp() : Boolean;
			''',
			'''
			return self == self;
			''')
	}
	
	def testTransactionalBlocks() {
		check(
			'''
			begin
			    (* Transaction #1 *)
			    self.attr1 := "foo";
			end;
			begin
			    (* Transaction #2 *)
			    self.attr1 := "bar";
			end;
			''')
	}
	
	protected def CharSequence generate(String fixtureName) {
		val fixture = getOperation('''someModel::MyClass::«fixtureName»''')
		generator.generateActivity(fixture.activity)
	}
}