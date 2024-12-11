package conversion.exports;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.uos.fmt.musitech.utility.math.Rational;
import external.Tablature;
import external.Transcription;
import interfaces.CLInterface;
import internal.core.Encoding;
import internal.structure.Event;
import tbp.symbols.Symbol;
import tools.ToolBox;

public class MEIExportTest {

	private File encodingTestpiece;
	private File encodingNewsidler;
	private File midiTestpiece;
	private File midiNewsidler;

	private final Rational r128 = new Rational(1, 128);
	private final Rational r64 = new Rational(1, 64);
	private final Rational r32 = new Rational(1, 32);
	private final Rational r16 = new Rational(1, 16);
	private final Rational r8 = new Rational(1, 8);
	private final Rational r4 = new Rational(1, 4);
	private final Rational r2 = new Rational(1, 2);
	private final Rational r1 = new Rational(1, 1);

	@Before
	public void setUp() throws Exception {
		Map<String, String> paths = CLInterface.getPaths(true);
		String ep = paths.get("ENCODINGS_PATH");
		String td = "test/5vv/";
		String mp = paths.get("MIDI_PATH");
		encodingTestpiece = new File(
			CLInterface.getPathString(Arrays.asList(ep, td)) + "testpiece.tbp"
		);
		encodingNewsidler = new File(CLInterface.getPathString(
			Arrays.asList(ep, "thesis-int", "3vv")) + "newsidler-1544_2-nun_volget.tbp"
		);
		midiTestpiece = new File(CLInterface.getPathString(
			Arrays.asList(mp, td)) + "testpiece.mid"
		);
		midiNewsidler = new File(CLInterface.getPathString(
			Arrays.asList(mp, "thesis-int", "3vv")) + "newsidler-1544_2-nun_volget.mid"	
		);
	}


	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testRebarKeyInfo() {
		Tablature t1 = new Tablature(encodingTestpiece, false);
		Transcription tr1 = new Transcription(midiTestpiece, encodingTestpiece);
		Tablature t2 = new Tablature(encodingNewsidler, false);
		Transcription tr2 = new Transcription(midiNewsidler, encodingNewsidler);
		
//		for (Integer[] in : t2.getMeterInfoAgnostic()) {
//			System.out.println(Arrays.asList(in));
//		}
//		System.out.println("----");
//		for (Integer[] in : tr2.getMeterInfo()) {
//			System.out.println(Arrays.asList(in));
//		}
//		System.out.println("----");
//		for (Integer[] in : tr2.getKeyInfo()) {
//			System.out.println(Arrays.asList(in));
//		}
//		System.exit(0);

		List<Integer[]> ki1 = tr1.getKeyInfo(); // bars 1-3 (= tab bars 1-4)
		List<Integer[]> ki2 = tr2.getKeyInfo();
		// Add fake second key sig change at metric time 44/1, which is bar 44 
		// following the METER_INFO tag (i.e., with 6/4 starting at bar 42)
		ki2.get(0)[Transcription.KI_LAST_BAR] = 43; // bars 1-43 (= tab bars 1-45)
		ki2.add(new Integer[]{-1, 0, 44, 92, 44, 1}); // bars 44-92 (= tab bars 46-96)

		List<List<Integer[]>> expected = Arrays.asList(
			Arrays.asList(new Integer[][]{new Integer[]{0, 1, 1, 4, 0, 1}}),
			Arrays.asList(new Integer[][]{new Integer[]{-1, 0, 1, 45, 0, 1},
				new Integer[]{-1, 0, 46, 96, 44, 1}})
		);

		List<List<Integer[]>> actual = Arrays.asList(
			MEIExport.rebarKeyInfo(t1, ki1),
			MEIExport.rebarKeyInfo(t2, ki2)
		);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {			
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j).length, actual.get(i).get(j).length);
				for (int k = 0; k < expected.get(i).get(j).length; k++) {
					assertEquals(expected.get(i).get(j)[k], actual.get(i).get(j)[k]);
				}
			}
		}
	}


	@Test
	public void testAlignMeterAndKeyinfo() {
		Tablature t1 = new Tablature(encodingTestpiece, false);
		Transcription tr1 = new Transcription(midiTestpiece, encodingTestpiece);
		Tablature t2 = new Tablature(encodingNewsidler, false);
		Transcription tr2 = new Transcription(midiNewsidler, encodingNewsidler);
		// Add fake second key signature change to ki
		List<Integer[]> tr2kiExt = new ArrayList<>(tr2.getKeyInfo());
		tr2kiExt.add(new Integer[]{2, 0, 46, 96, 44, 1});

		List<List<List<Integer[]>>> expected = new ArrayList<>();
		List<List<Integer[]>> expected1 = Arrays.asList(
			Arrays.asList(
				new Integer[]{2, 2, 1, 2, 0, 1, 1, 1},
				new Integer[]{3, 16, 3, 3, 2, 1, 1, 1},
				new Integer[]{13, 16, 4, 4, 35, 16, 1, 1}
			),
			Arrays.asList(
				new Integer[]{0, 1, 1, 2, 0, 1, 1},
				new Integer[]{0, 1, 3, 3, 2, 1, 0},
				new Integer[]{0, 1, 4, 4, 35, 16, 0}
			)
		);
		expected.add(expected1);
		//
		List<List<Integer[]>> expected2a = Arrays.asList(
			Arrays.asList(
				new Integer[]{2, 2, 1, 41, 0, 1, 1, 1},
				new Integer[]{3, 4, 42, 49, 41, 1, 1, 1},
				new Integer[]{2, 2, 50, 96, 47, 1, 1, 1}
			),
			Arrays.asList(
				new Integer[]{-1, 0, 1, 41, 0, 1, 1},
				new Integer[]{-1, 0, 42, 49, 41, 1, 0},
				new Integer[]{-1, 0, 50, 96, 47, 1, 0}
			)
		);
		expected.add(expected2a);
		//
		List<List<Integer[]>> expected2b = Arrays.asList(
			Arrays.asList(
				new Integer[]{2, 2, 1,  41, 0,  1, 1, 1},
				new Integer[]{3, 4, 42, 45, 41, 1, 1, 1},
				new Integer[]{3, 4, 46, 49, 44, 1, 1, 0},
				new Integer[]{2, 2, 50, 96, 47, 1, 1, 1}
			),
			Arrays.asList(
				new Integer[]{-1, 0, 1, 41, 0, 1, 1},
				new Integer[]{-1, 0, 42, 45, 41, 1, 0},
				new Integer[]{2, 0, 46, 49, 44, 1, 1},
				new Integer[]{2, 0, 50, 96, 47, 1, 0}
			)
		);
		expected.add(expected2b);

		List<List<List<Integer[]>>> actual = Arrays.asList(
			MEIExport.alignMeterAndKeyInfo(t1.getMeterInfoAgnostic(), tr1.getKeyInfo()),
			MEIExport.alignMeterAndKeyInfo(t2.getMeterInfoAgnostic(), tr2.getKeyInfo()),
			MEIExport.alignMeterAndKeyInfo(t2.getMeterInfoAgnostic(), tr2kiExt)
		);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).size(), actual.get(i).size());
			for (int j = 0; j < expected.get(i).size(); j++) {
				assertEquals(expected.get(i).get(j).size(), actual.get(i).get(j).size());
				for (int k = 0; k < expected.get(i).get(j).size(); k++) {
					assertEquals(expected.get(i).get(j).get(k).length, actual.get(i).get(j).get(k).length);
					for (int l = 0; l < expected.get(i).get(j).get(k).length; l++) {
						assertEquals(expected.get(i).get(j).get(k)[l], actual.get(i).get(j).get(k)[l]);
					}
				}
			}
		}
	}


	@Test
	public void testGetStaffAndLayer() {
		List<Integer[]> expected = Arrays.asList(
			new Integer[]{1, 1},
			new Integer[]{1, 1},
			//
			new Integer[]{1, 1},
			new Integer[]{1, 1},
			new Integer[]{3, 1},
			new Integer[]{5, 1},
			//
			new Integer[]{2, 2},
			new Integer[]{4, 1},
			//
			new Integer[]{3, 2},
			new Integer[]{5, 1},
			new Integer[]{2, 2},
			new Integer[]{4, 1}
		);

		List<Boolean> tabOnly = Arrays.asList(true, false);
		List<Boolean> tabAndTrans = Arrays.asList(false, true);
		List<Boolean> transOnly = Arrays.asList(false, false);
		List<Integer[]> actual = Arrays.asList(
			// Tablature Staff
			// tabOnly (tabOnTop is always true)
			MEIExport.getStaffAndLayer(true, tabOnly, true, true, -1, -1),
			MEIExport.getStaffAndLayer(true, tabOnly, true, false, -1, -1),
			// tabAndTrans
			MEIExport.getStaffAndLayer(true, tabAndTrans, true, true, 4, -1),
			MEIExport.getStaffAndLayer(true, tabAndTrans, true, false, 4, -1),
			MEIExport.getStaffAndLayer(true, tabAndTrans, false, true, 4, -1),
			MEIExport.getStaffAndLayer(true, tabAndTrans, false, false, 4, -1),
			// Non-tablature staff
			// transOnly ((tabOnTop is always false)
			MEIExport.getStaffAndLayer(false, transOnly, false, true, 4, 3),
			MEIExport.getStaffAndLayer(false, transOnly, false, false, 4, 3),
			// tabAndTrans
			MEIExport.getStaffAndLayer(false, tabAndTrans, true, true, 4, 3),
			MEIExport.getStaffAndLayer(false, tabAndTrans, true, false, 4, 3),
			MEIExport.getStaffAndLayer(false, tabAndTrans, false, true, 4, 3),
			MEIExport.getStaffAndLayer(false, tabAndTrans, false, false, 4, 3)
		);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
	  		assertEquals(expected.get(i).length, actual.get(i).length);
	  		for (int j = 0; j < expected.get(i).length; j++) {
	  			assertEquals(expected.get(i)[j], actual.get(i)[j]);
	  		}
		}
	}


	@Test
	public void testGetCleffing() {
		String[] g = new String[]{"G", "2"};
		String[] f = new String[]{"F", "4"};

		List<String[]> expected = Arrays.asList(
			g, f, 
			//
			g, f, 
			g, f, f,
			g, g, f, f,
			g, g, f, f, f,
			g, g, g, f, f, f
		);

		List<String[]> actual = Arrays.asList(
			// Grand staff
			MEIExport.getCleffing(1, -1, true),
			MEIExport.getCleffing(2, -1, true),
			// Non-grand staff
			MEIExport.getCleffing(1, 2, false),
			MEIExport.getCleffing(2, 2, false),
			//
			MEIExport.getCleffing(1, 3, false),
			MEIExport.getCleffing(2, 3, false),
			MEIExport.getCleffing(3, 3, false),
			//
			MEIExport.getCleffing(1, 4, false),
			MEIExport.getCleffing(2, 4, false),
			MEIExport.getCleffing(3, 4, false),
			MEIExport.getCleffing(4, 4, false),
			//
			MEIExport.getCleffing(1, 5, false),
			MEIExport.getCleffing(2, 5, false),
			MEIExport.getCleffing(3, 5, false),
			MEIExport.getCleffing(4, 5, false),
			MEIExport.getCleffing(5, 5, false),
			//
			MEIExport.getCleffing(1, 6, false),
			MEIExport.getCleffing(2, 6, false),
			MEIExport.getCleffing(3, 6, false),
			MEIExport.getCleffing(4, 6, false),
			MEIExport.getCleffing(5, 6, false),
			MEIExport.getCleffing(6, 6, false)
		);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}


	@Test
	public void testMakeOpeningTag() {
		List<String> expected = Arrays.asList(
			"<note pname='c' oct='4' dur='4' xml:id='n1'/>", 
			"<rest dur='2' dots='1' xml:id='r1'>"
		);

		List<String> actual = Arrays.asList(
			MEIExport.makeOpeningTag("note", true, 
				new String[][]{
					{"pname", "c"}, {"oct", "4"}, {"dur", "4"}, null, {"xml:id", "n1"}
				}
			),
			MEIExport.makeOpeningTag("rest", false, 
				new String[][]{
					null, {"dur", "2"}, {"dots", "1"}, null, {"xml:id", "r1"}
				}
			)
		);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}


	@Test
	public void testGetXMLDur() {
		Encoding encoding = new Encoding(encodingTestpiece);
		
		List<Integer[]> expected = Arrays.asList(
			// Bar 1
			null,
			new Integer[]{2, 0},
			new Integer[]{4, 0},
			new Integer[]{4, 0},
			null,
			// Bar 2
			new Integer[]{8, 1},
			new Integer[]{16, 0},
			new Integer[]{8, 0},
			null,
			new Integer[]{4, 0},
			new Integer[]{8, 0},
			null, null,
			// Bar 3
			new Integer[]{16, 0},
			null,
			new Integer[]{32, 0},
			null, null,
			// Bar 4
			null, null,
			new Integer[]{4, 0},		
			new Integer[]{4, 0},
			new Integer[]{4, 0},
			null
		);

		List<Integer[]> actual = new ArrayList<>();
		List<String> events = new ArrayList<>();
		List<Event> ebl = Encoding.removeDecorativeBarlineEvents(encoding.getEvents());
		for (Event e : ebl) {
			events.add(e.getEncoding().substring(0, 
				e.getEncoding().lastIndexOf(Symbol.SYMBOL_SEPARATOR)));
		}
		for (String s : events) {
			if (!s.equals(Symbol.SYSTEM_BREAK_INDICATOR) && !s.equals(Symbol.END_BREAK_INDICATOR)) {
				actual.add(MEIExport.getXMLDur(s));
			}
		}

		for (int i = 0; i < expected.size(); i++) {
			if (expected.get(i) != null) {
				assertEquals(expected.get(i).length, actual.get(i).length);
				for (int j = 0; j < expected.get(i).length; j++) {
					assertEquals(expected.get(i)[j], actual.get(i)[j]);
				}
			}
			else {
				assertNull(expected.get(i));
				assertNull(actual.get(i));
//				assertEquals(expected.get(i), actual.get(i));
			}
		}
	}


	@Test
	public void testGetXMLDurAlt() {
		List<Rational> rs = Arrays.asList(
			Rational.ONE,
			new Rational(1, 2),
			new Rational(1, 4),
			new Rational(1, 8),
			new Rational(2, 1),
			new Rational(4, 1),
			new Rational(7, 2)
		);

		List<Integer> expected = Arrays.asList(
			1, 2, 4, 8, -1, -2, 0
		);

		List<Integer> actual = new ArrayList<>();
		rs.forEach(r -> actual.add(MEIExport.getXMLDur(r)));

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
		 	assertEquals(expected.get(i), actual.get(i));
		}
		assertEquals(expected, actual);
	}


	@Test
	public void testMakeNoteXMLID() {
		List<String> expected = Arrays.asList(
			"n0.0.1.0.c4.0:1",
			"n1.1.1.1.d4.1:4",
			"n2.2.1.2.e4.1:2"
		);

		List<String> actual = Arrays.asList(
			MEIExport.makeNoteXMLID(0, 0, 1, 0, "c", "4", Rational.ZERO),
			MEIExport.makeNoteXMLID(1, 1, 1, 1, "d", "4", new Rational(1, 4)),
			MEIExport.makeNoteXMLID(2, 2, 1, 2, "e", "4", new Rational(1, 2))
		);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}


	@Test
	public void testMakeRestXMLID() {
		List<String> expected = Arrays.asList(
			"r.0.1.0.0:1",
			"r.1.1.1.1:4",
			"r.2.1.2.1:2"
		);

		List<String> actual = Arrays.asList(
			MEIExport.makeRestXMLID(0, 1, 0, Rational.ZERO),
			MEIExport.makeRestXMLID(1, 1, 1, new Rational(1, 4)),
			MEIExport.makeRestXMLID(2, 1, 2, new Rational(1, 2))
		);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}


	@Test
	public void testGetDurFromXMLDur() {
		List<Integer> expected = Arrays.asList(
			96, 144, 168, // brevis; 0, 1, 2 dots
			48, 72, 84, // semibrevis; 0, 1, 2 dots 
			24, 36, 42, // minim; 0, 1, 2 dots
			12, 18, 21 // semiminim; 0, 1, 2 dots
		);

		List<Integer> XMLDurs = Arrays.asList(
			1, 1, 1, 2, 2, 2, 4, 4, 4, 8, 8, 8);

		List<Integer> dots = Arrays.asList(
			0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2
		);
		List<Integer> actual = new ArrayList<>();
		ToolBox.getRange(0, XMLDurs.size()).forEach(i -> 
			actual.add(MEIExport.getDurFromXMLDur(XMLDurs.get(i), dots.get(i)))
		);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
		 	assertEquals(expected.get(i), actual.get(i));
		}
		assertEquals(expected, actual);
	}


//	public void testGetDuration() {
//		List<Rational> all = new ArrayList<Rational>();
//		for (int i = 1; i <= 128; i++) {
//			all.add(new Rational(i, 128));
//		}
//
//		List<Integer[]> expected = new ArrayList<Integer[]>();
//		expected.add(new Integer[]{128, 0});
//		expected.add(new Integer[]{64, 0});
//		expected.add(new Integer[]{64, 1}); 
//		expected.add(new Integer[]{32, 0}); // 4 ---
//		expected.add(new Integer[]{}); // 1/32 + n 
//		expected.add(new Integer[]{32, 1});
//		expected.add(new Integer[]{32, 2}); 
//		expected.add(new Integer[]{16, 0}); // 8 ---
//		expected.add(new Integer[]{}); // 1/16 + n
//		expected.add(new Integer[]{}); // 1/16 + 1/64
//		expected.add(new Integer[]{}); // 1/16 + 1/64d
//		expected.add(new Integer[]{16, 1}); // 12
//		expected.add(new Integer[]{}); // 
//		expected.add(new Integer[]{16, 2});
//		expected.add(new Integer[]{16, 3});
//		expected.add(new Integer[]{8, 0}); // 16 ---
//		expected.add(new Integer[]{}); // 1/8 + n
//		expected.add(new Integer[]{}); // 1/8 + 1/64
//		expected.add(new Integer[]{}); // 1/8 + 1/64d
//		expected.add(new Integer[]{}); // 20
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{8, 1}); // 24
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{8, 2}); // 28
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{8, 3});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{4, 0}); // 32 ---
//		expected.add(new Integer[]{}); // 1/4 + n
//		expected.add(new Integer[]{}); // 1/4 + 1/64
//		expected.add(new Integer[]{}); // 1/4 + 1/64d
//		expected.add(new Integer[]{}); // 36
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 40
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 44
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{4, 1}); // 48
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 52
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{4, 2}); // 56
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{4, 3}); // 60
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{2, 0}); // 64 ---
//		expected.add(new Integer[]{}); // 1/2 + n
//		expected.add(new Integer[]{}); // 1/2 + 1/64
//		expected.add(new Integer[]{}); // 1/2 + 1/64d
//		expected.add(new Integer[]{}); // 68
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 72
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 76
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 80
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 84
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 88
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 92
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{2, 1}); // 96
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 100
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 104
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 108
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{2, 2}); // 112
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{}); // 116
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{2, 3}); // 120
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{2, 4}); // 124
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{});
//		expected.add(new Integer[]{1, 0}); // 128 --- 
//	}

}
