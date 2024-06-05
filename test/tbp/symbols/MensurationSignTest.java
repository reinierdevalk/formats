package tbp.symbols;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tbp.symbols.MensurationSign;
import tbp.symbols.Symbol;

public class MensurationSignTest {

	@Before
	public void setUp() throws Exception {
	}


	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testMakeStaffLine() {
		List<MensurationSign> mss = new ArrayList<>();
		// M<n> type; default beat unit, default staffline
		MensurationSign m1 = new MensurationSign();
		m1.setEncoding(Symbol.TWO.getEncoding());
		m1.setSymbol(Symbol.TWO.getSymbol());
		m1.setMeter(Symbol.TWO.getMeter());
		mss.add(m1);
		// M<n> type; default beat unit, non-default staffline
		MensurationSign m2 = new MensurationSign();
		m2.setEncoding(Symbol.TWO.makeVariant(-1, 4).getEncoding());
		m2.setSymbol(Symbol.TWO.makeVariant(-1, 4).getSymbol());
		m2.setMeter(Symbol.TWO.makeVariant(-1, 4).getMeter());
		mss.add(m2);
		// M<n> type; non-default beat unit, default staffline
		MensurationSign m3 = new MensurationSign();
		m3.setEncoding(Symbol.TWO.makeVariant(2, -1).getEncoding());
		m3.setSymbol(Symbol.TWO.makeVariant(2, -1).getSymbol());
		m3.setMeter(Symbol.TWO.makeVariant(2, -1).getMeter());
		mss.add(m3);
		// M<n> type; non-default beat unit, non-default staffline
		MensurationSign m4 = new MensurationSign();
		m4.setEncoding(Symbol.TWO.makeVariant(2, 4).getEncoding());
		m4.setSymbol(Symbol.TWO.makeVariant(2, 4).getSymbol());
		m4.setMeter(Symbol.TWO.makeVariant(2, 4).getMeter());
		mss.add(m4);
		// M<n>\ type; default beat unit, default staffline
		MensurationSign m5 = new MensurationSign();
		m5.setEncoding(Symbol.CUT_C.getEncoding());
		m5.setSymbol(Symbol.CUT_C.getSymbol());
		m5.setMeter(Symbol.CUT_C.getMeter());
		mss.add(m5);
		// M<n>\ type; default beat unit, non-default staffline
		MensurationSign m6 = new MensurationSign();
		m6.setEncoding(Symbol.CUT_C.makeVariant(-1, 4).getEncoding());
		m6.setSymbol(Symbol.CUT_C.makeVariant(-1, 4).getSymbol());
		m6.setMeter(Symbol.CUT_C.makeVariant(-1, 4).getMeter());
		mss.add(m6);
		// M<n>\ type; non-default beat unit, default staffline
		MensurationSign m7 = new MensurationSign();
		m7.setEncoding(Symbol.CUT_C.makeVariant(1, -1).getEncoding());
		m7.setSymbol(Symbol.CUT_C.makeVariant(1, -1).getSymbol());
		m7.setMeter(Symbol.CUT_C.makeVariant(1, -1).getMeter());
		mss.add(m7);
		// M<n>\ type; non-default beat unit, non-default staffline
		MensurationSign m8 = new MensurationSign();
		m8.setEncoding(Symbol.CUT_C.makeVariant(1, 4).getEncoding());
		m8.setSymbol(Symbol.CUT_C.makeVariant(1, 4).getSymbol());
		m8.setMeter(Symbol.CUT_C.makeVariant(1, 4).getMeter());
		mss.add(m8);

		List<Integer> expected = Arrays.asList(new Integer[]{3, 4, 3, 4, 3, 4, 3, 4});
		List<Integer> actual = new ArrayList<>();
		for (MensurationSign m : mss) {
			actual.add(m.makeStaffLine());
		}

		assertEquals(expected, actual);
	}


	@Test
	public void testMakeVariant() {
		List<MensurationSign> expected = Arrays.asList(new MensurationSign[]{
			new MensurationSign("M3", "3", new Integer[]{3, 4}), // M3, beatUnit same, staffLine same  (== Symbol.THREE)
			new MensurationSign("M34", "3", new Integer[]{3, 4}), // M3, beatUnit same, staffLine diff
			new MensurationSign("M3:1", "3", new Integer[]{3, 1}), // M3, beatUnit diff, staffLine same
			new MensurationSign("M3:14", "3", new Integer[]{3, 1}), // M3, beatUnit diff, staffLine diff
			new MensurationSign("MC\\", "\u00A2", new Integer[]{2, 2}), // MC\, beatUnit same, staffLine same  (== Symbol.CUT_C)
			new MensurationSign("MC\\4", "\u00A2", new Integer[]{2, 2}), // MC\, beatUnit same, staffLine diff
			new MensurationSign("MC\\:1", "\u00A2", new Integer[]{2, 1}), // MC\, beatUnit diff, staffLine same
			new MensurationSign("MC\\:14", "\u00A2", new Integer[]{2, 1}), // MC\, beatUnit diff, staffLine diff
		});

		List<MensurationSign> actual = new ArrayList<>();
		actual.add(Symbol.THREE.makeVariant(-1, -1));
		actual.add(Symbol.THREE.makeVariant(-1, 4));
		actual.add(Symbol.THREE.makeVariant(1, -1));
		actual.add(Symbol.THREE.makeVariant(1, 4));
		actual.add(Symbol.CUT_C.makeVariant(-1, -1));
		actual.add(Symbol.CUT_C.makeVariant(-1, 4));
		actual.add(Symbol.CUT_C.makeVariant(1, -1));
		actual.add(Symbol.CUT_C.makeVariant(1, 4));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}

}