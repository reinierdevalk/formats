package tbp.symbols;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tbp.symbols.RhythmSymbol;
import tbp.symbols.Symbol;

public class RhythmSymbolTest {

	@Before
	public void setUp() throws Exception {
	}


	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testMakeVariant() {
		List<RhythmSymbol> expected = Arrays.asList(new RhythmSymbol[]{
			new RhythmSymbol("sb*", "H", 72), // sb dotted 
			new RhythmSymbol("mi**", "Q", 42), // mi double dotted
			new RhythmSymbol("sm-", "E", 12), // sm beamed
			new RhythmSymbol("fu*-", "S", 9), // fu dotted and beamed
			new RhythmSymbol("tr[sb", "H", 32), // sb tripletised
			new RhythmSymbol("trsb", "H", 32),
			new RhythmSymbol("tr]sb", "H", 32),
			new RhythmSymbol("tr[mi*", "Q", 24), // mi tripletised and dotted
			new RhythmSymbol("trmi*", "Q", 24),
			new RhythmSymbol("tr]mi*", "Q", 24),
			new RhythmSymbol("tr[sm-", "E", 8), // sm tripletised and beamed
			new RhythmSymbol("trsm-", "E", 8),
			new RhythmSymbol("tr]sm-", "E", 8),
			new RhythmSymbol("tr[fu*-", "S", 6), // fu tripletised, dotted, and beamed
			new RhythmSymbol("trfu*-", "S", 6),
			new RhythmSymbol("tr]fu*-", "S", 6),
			new RhythmSymbol("tr[sb*", "H", 48), // sb dotted tripletised
			new RhythmSymbol("trsb*", "H", 48),
			new RhythmSymbol("tr]sb*", "H", 48)
		});

		List<RhythmSymbol> actual = new ArrayList<>();
		actual.addAll(Symbol.SEMIBREVIS.makeVariant(1, false, false));
		actual.addAll(Symbol.MINIM.makeVariant(2, false,  false));
		actual.addAll(Symbol.SEMIMINIM.makeVariant(0, true, false));
		actual.addAll(Symbol.FUSA.makeVariant(1, true, false));
		actual.addAll(Symbol.SEMIBREVIS.makeVariant(0, false, true));
		actual.addAll(Symbol.MINIM.makeVariant(1, false, true));
		actual.addAll(Symbol.SEMIMINIM.makeVariant(0, true, true));
		actual.addAll(Symbol.FUSA.makeVariant(1, true, true));
		actual.addAll(new RhythmSymbol("sb*", "H", 72).makeVariant(0, false, true));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}


	@Test
	public void testIsTriplet() {
		List<String> expected = Arrays.asList(RhythmSymbol.TRIPLET_OPEN, "", RhythmSymbol.TRIPLET_CLOSE, null);

		List<RhythmSymbol> smtrs = Symbol.SEMIMINIM.makeVariant(1, true, true);
		List<String> actual = new ArrayList<>();
		for (RhythmSymbol rs : Arrays.asList(smtrs.get(0), smtrs.get(1), smtrs.get(2), Symbol.SEMIMINIM)) {
			actual.add(rs.isTriplet());
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
		assertEquals(expected, actual);
	}

}