package com.abstratt.mdd.core.tests.frontend.textuml;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;

import com.abstratt.mdd.core.tests.harness.AbstractRepositoryBuildingTests;

public class CollectionTests extends AbstractRepositoryBuildingTests {

	public static Test suite() {
		return new TestSuite(CollectionTests.class);
	}

	public CollectionTests(String name) {
		super(name);
	}

	private String getSimpleModelSource() {
		String source = "";
		source += "model simple;\n";
		source += "import base;\n";
		source += "  class Account\n";
		source += "    attribute balance : Integer;\n";
		source += "    operation deposit(valor : Integer);\n";
		source += "    begin\n";
		source += "      self.balance := self.balance + valor;\n";
		source += "    end;\n";
		source += "  end;\n";
		source += "  class Customer\n";
		source += "    attribute name : String;\n";
		source += "  end;\n";
		source += "  association AccountCustomer\n";
		source += "    navigable role account : Account[*];\n";
		source += "    navigable role owner : Customer;\n";		
		source += "  end;\n";
		source += "end.";
		return source;
	}

	public void testAccessLocalVar() throws CoreException {
		String model = "";
		model += "model tests;\n";
		model += "  import simple;\n";
		model += "  class Foo\n";
		model += "    static operation countInstances(values: Integer[*]) : Integer;\n";
		model += "    begin\n";
		model += "      var count : Integer;\n";
		model += "      count := 0;\n";
		model += "      values.forEach((value : Integer)  { count := count + value;});\n";
		model += "      return count;\n";
		model += "    end;\n";
		model += "  end;\n";
		model += "end.\n";
		parseAndCheck(getSimpleModelSource(), model);
	}
	
	public void testEmptyLiteral() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "  import base;\n";
		source += "  class CollectionTests\n";
		source += "    operation op1( ) : Integer[*];\n";
		source += "    begin\n";
		source += "      return Integer[];\n";
		source += "    end;\n";
		source += "  end;\n";
		source += "end.";
		parseAndCheck(source);
	}
	
	//TODO temporarily disabled during refactor to remove metamodel extensions
//	public void testLiteral() throws CoreException {
//		String source = "";
//		source += "model simple;\n";
//		source += "  import base;\n";
//		source += "  class CollectionTests\n";
//		source += "    operation op1( ) : Integer[*];\n";
//		source += "    begin\n";
//		source += "      return Integer{1, 4, 6, 9, 12};\n";
//		source += "    end;\n";
//		source += "  end;\n";
//		source += "end.";
//		parseAndCheck(source);
//	}


	public void testExtent() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "  class CollectionTests\n";
		source += "    operation buildSet() : Account[*];\n";
		source += "    begin\n";
		source += "      return Account extent;\n";
		source += "    end;\n";
		source += "  end;\n";
		source += "end.";
		parseAndCheck(getSimpleModelSource(), source);
	}

	public void testIterate() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "class TestDriver\n";
		source += "  static operation iteration(accounts : Account[*]);\n";
		source += "  begin\n";
		source += "    accounts.forEach((account : Account) {account.deposit(10);});\n";
		source += "  end;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(getSimpleModelSource(), source);
	}
	
	public void _testIterateShorthand() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "class TestDriver\n";
		source += "  static operation iteration(accounts : Account[*]);\n";
		source += "  begin\n";
		source += "    accounts.forEach({a | a.deposit(10)});\n";
		source += "  end;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(getSimpleModelSource(), source);
	}

	public void testSelect() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "class TestDriver\n";
		source += "  static operation selection() : Account[*];\n";
		source += "  begin\n";
		source += "    return Account extent .select((account : Account) : Boolean {\n";
		source += "        return account.balance > 10;\n";
		source += "    }).asSet();\n";
		source += "  end;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(getSimpleModelSource(), source);
	}
	
	public void _testSelectShorthand() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "class TestDriver\n";
		source += "  static operation selection() : Account[*]; {\n";
		source += "    ^ Account extent .select({a | ^ a.balance > 10}).asSet();\n";
		source += "  };\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(getSimpleModelSource(), source);
	}

	public void testCollect() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "class TestDriver\n";
		source += "  static operation selection() : Customer[*];\n";
		source += "  begin\n";
		source += "    return Account extent.collect((account : Account) : Customer {\n";
		source += "        account<-AccountCustomer->owner\n";
		source += "    });\n";
		source += "  end;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(getSimpleModelSource(), source);
	}
	
	public void testReduce() throws CoreException {
        String source = "";
        source += "model simple;\n";
        source += "class TestDriver\n";
        source += "  static operation reduction() : Integer;\n";
        source += "  begin\n";
        source += "    return Account extent.reduce(\n";
        source += "        (account : Account, total : Integer) : Integer {\n";
        source += "            account.balance + total\n";
        source += "        },\n";
        source += "        0\n";
        source += "    );\n";
        source += "  end;\n";
        source += "end;\n";
        source += "end.";
        parseAndCheck(getSimpleModelSource(), source);
    }
	
	public void testGroupBy() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "class TestDriver\n";
		source += "  static operation grouping() : Grouping<Account>;\n";
		source += "  begin\n";
		source += "    return Account extent.groupBy((account : Account) : Customer {\n";
		source += "        account<-AccountCustomer->owner\n";
		source += "    });\n";
		source += "  end;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(getSimpleModelSource(), source);
	}

	
	public void testCollectDataType() throws CoreException {
		String source = "";
		source += "model simple;\n";
		source += "datatype Name attribute name : String; end;\n";
		source += "class TestDriver\n";
		source += "  static operation selection() : {: String}[*];\n";
		source += "  begin\n";
		source += "    return Account extent.collect((account : Account) : {:String} {\n";
		source += "        {name := account<-AccountCustomer->owner.name}\n";
		source += "    });\n";
		source += "  end;\n";
		source += "end;\n";
		source += "end.";
		parseAndCheck(getSimpleModelSource(), source);
	}
}
