package tbp.symbols;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tbp.symbols.ConstantMusicalSymbol;
import tbp.symbols.Symbol;

public class ConstantMusicalSymbolTest {

	@Before
	public void setUp() throws Exception {
	}


	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testMakeVariant() {
		List<ConstantMusicalSymbol> expected = Arrays.asList(new ConstantMusicalSymbol[]{
			new ConstantMusicalSymbol("||", "||"),
			new ConstantMusicalSymbol("||:", "||:"),
			new ConstantMusicalSymbol(":||", ":||"),
			new ConstantMusicalSymbol(":|:", ":|:")
		});

		List<ConstantMusicalSymbol> actual = new ArrayList<>();
		actual.add(Symbol.BARLINE.makeVariant(2, null));
		actual.add(Symbol.BARLINE.makeVariant(2, "left"));
		actual.add(Symbol.BARLINE.makeVariant(2, "right"));
		actual.add(Symbol.BARLINE.makeVariant(1, "both"));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}

}