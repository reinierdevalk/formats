package tbp.symbols;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uos.fmt.musitech.utility.math.Rational;
import external.Tablature.Tuning;
import tbp.symbols.Symbol;
import tbp.symbols.TabSymbol;
import tbp.symbols.TabSymbol.TabSymbolSet;

public class TabSymbolTest {

	private static final Rational HALF = new Rational(1, 2);
	private static final Rational DOTTED_HALF = new Rational(3, 4);


	@Before
	public void setUp() throws Exception {
	}


	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testGetTabSymbolDur() {
		List<Rational> rs = Arrays.asList(new Rational[]{				
			HALF, DOTTED_HALF, new Rational(15, 16), new Rational(5, 4)
		});
		List<Integer> expected = Arrays.asList(new Integer[]{48, 72, 90, 120});

		List<Integer> actual = new ArrayList<>();
		for (Rational r : rs) {
			actual.add(TabSymbol.getTabSymbolDur(r));
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		} 
		assertEquals(expected, actual);
	}


	@Test
	public void testMakeVariant() {
		List<TabSymbol> expected = Arrays.asList(new TabSymbol[]{
			new TabSymbol("f1" + TabSymbol.FINGERING_DOT_ENCODING, "f", 5, 1), // French
			new TabSymbol("+" + TabSymbol.FINGERING_DOT_ENCODING.repeat(1), "+", 0, 6), // Ochsenkun1558
			new TabSymbol("27" + TabSymbol.FINGERING_DOT_ENCODING.repeat(2), "2", 2, 7), // Spanish
			new TabSymbol("08" + TabSymbol.FINGERING_DOT_ENCODING.repeat(3), "0", 0, 8) // Italian 
		});
		
		List<TabSymbol> actual = new ArrayList<>();
		actual.add(Symbol.getTabSymbol("f1", TabSymbolSet.FRENCH).makeVariant(1));
		actual.add(Symbol.getTabSymbol("+", TabSymbolSet.OCHSENKUN_1558).makeVariant(1));
		actual.add(Symbol.getTabSymbol("27", TabSymbolSet.SPANISH).makeVariant(2));
		actual.add(Symbol.getTabSymbol("08", TabSymbolSet.ITALIAN).makeVariant(3));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}


	@Test
	public void testGetPitch() {
		TabSymbol ts1 = new TabSymbol("f1", "f", 5, 1); // French, G
		TabSymbol ts2 = new TabSymbol("+", "+", 0, 6); // Ochsenkun1558, A6G
		TabSymbol ts3 = new TabSymbol("27", "2", 2, 7); // Spanish, G7D
		TabSymbol ts4 = new TabSymbol("08", "0", 0, 8); // Italian, A8

		List<Integer> expected = Arrays.asList(new Integer[]{72, 43, 40, 40});
		List<Integer> actual = new ArrayList<>();
		actual.add(ts1.getPitch(Tuning.G));
		actual.add(ts2.getPitch(Tuning.A6G));
		actual.add(ts3.getPitch(Tuning.G7D));
		actual.add(ts4.getPitch(Tuning.A8));

		assertEquals(expected, actual);
	}

}