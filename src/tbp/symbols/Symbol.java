package tbp.symbols;

/**
 * @author Reinier de Valk
 * @version 14.04.2023 (last well-formedness check)
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import tbp.symbols.TabSymbol.TabSymbolSet;

public class Symbol {

	// I. Punctuation symbols
	public static final String SYMBOL_SEPARATOR = ".";  
	public static final String SYSTEM_BREAK_INDICATOR = "/";
	public static final String END_BREAK_INDICATOR = "//";

	// II. Musical symbols
	// 1. Constant musical symbols
	public static final ConstantMusicalSymbol SPACE = new ConstantMusicalSymbol(">", "-");
	public static final ConstantMusicalSymbol BARLINE = 
		new ConstantMusicalSymbol(ConstantMusicalSymbol.PIPE, ConstantMusicalSymbol.PIPE);
	public static final Map<String, ConstantMusicalSymbol> CONSTANT_MUSICAL_SYMBOLS;
	static {
		List<ConstantMusicalSymbol> cmss = Arrays.asList(
			SPACE, 
			BARLINE,
			BARLINE.makeVariant(1, "left"),
			BARLINE.makeVariant(1, "right"),
			BARLINE.makeVariant(1, "both"),
			BARLINE.makeVariant(2, null),
			BARLINE.makeVariant(2, "left"),
			BARLINE.makeVariant(2, "right"),
			BARLINE.makeVariant(2, "both")
		);
		CONSTANT_MUSICAL_SYMBOLS = new LinkedHashMap<String, ConstantMusicalSymbol>();
		cmss.forEach(cms -> CONSTANT_MUSICAL_SYMBOLS.put(cms.getEncoding(), cms));
	}

	// 2. Mensuration signs
	public static final MensurationSign TWO	= new MensurationSign("M2", "2", new Integer[]{2, 4});
	public static final MensurationSign THREE = new MensurationSign("M3", "3", new Integer[]{3, 4});
	public static final MensurationSign FOUR = new MensurationSign("M4", "4", new Integer[]{4, 4});
	public static final MensurationSign FIVE = new MensurationSign("M5", "5", new Integer[]{5, 4});
	public static final MensurationSign SIX	= new MensurationSign("M6", "6", new Integer[]{6, 4});
	public static final MensurationSign SEVEN = new MensurationSign("M7", "7", new Integer[]{7, 4});
	public static final MensurationSign O = new MensurationSign("MO", "O", new Integer[]{3, 4});
	public static final MensurationSign C = new MensurationSign("MC", "C", new Integer[]{4, 4});
	public static final MensurationSign CUT_C = new MensurationSign("MC\\", "\u00A2", new Integer[]{2, 2});	
	public static final Map<String, MensurationSign> MENSURATION_SIGNS;
	static {
		List<MensurationSign> mss = Arrays.asList(
			TWO, THREE, FOUR, FIVE, SIX, SEVEN, O, C, CUT_C,
			THREE.makeVariant(-1, 4),
			THREE.makeVariant(2, -1),
			FOUR.makeVariant(2, -1),
			SIX.makeVariant(2, -1),
			TWO.makeVariant(1, -1),
			THREE.makeVariant(1, -1),
			TWO.makeVariant(4, -1),
			TWO.makeVariant(8, -1),
			THREE.makeVariant(8, -1),
			FIVE.makeVariant(16, -1)
		); 
		MENSURATION_SIGNS = new LinkedHashMap<String, MensurationSign>();
		mss.forEach(ms -> MENSURATION_SIGNS.put(ms.getEncoding(), ms));
	}

	// 3. Rhythm symbols
	public static final String CORONA_INDICATOR = "co";
	public static final RhythmSymbol LONGA = new RhythmSymbol("lo", "B", 192);
//	public static final RhythmSymbol LONGA = new RhythmSymbol("lo", "D", 192);
	public static final RhythmSymbol BREVIS = new RhythmSymbol("br", "W", 96); 
	public static final RhythmSymbol SEMIBREVIS = new RhythmSymbol("sb", "H", 48);
	public static final RhythmSymbol MINIM = new RhythmSymbol("mi", "Q", 24);
	public static final RhythmSymbol SEMIMINIM = new RhythmSymbol("sm", "E", 12);
	public static final RhythmSymbol FUSA = new RhythmSymbol("fu", "S", 6); 
	public static final RhythmSymbol SEMIFUSA = new RhythmSymbol("sf", "T", 3);
	public static final RhythmSymbol CORONA_LONGA = new RhythmSymbol("colo", "C", 192);
	public static final RhythmSymbol CORONA_BREVIS = new RhythmSymbol("cobr", "C", 96);
	public static final RhythmSymbol CORONA_SEMIBREVIS = new RhythmSymbol("cosb", "C", 48);
	public static final RhythmSymbol RHYTHM_DOT = new RhythmSymbol(RhythmSymbol.DOT_ENCODING, ".", -1);
	public static final Map<String, RhythmSymbol> RHYTHM_SYMBOLS;
	static {	
		List<RhythmSymbol> rss = new ArrayList<RhythmSymbol>();
		// Basic (lo-sf)
		Arrays.asList(LONGA, BREVIS, SEMIBREVIS, MINIM, SEMIMINIM, FUSA, SEMIFUSA, 
			CORONA_LONGA, CORONA_BREVIS, CORONA_SEMIBREVIS, RHYTHM_DOT)
			.forEach(rs -> rss.add(rs));
		// Dotted (lo-fu)
		Arrays.asList(LONGA, BREVIS, SEMIBREVIS, MINIM, SEMIMINIM, FUSA, CORONA_BREVIS, 
			CORONA_SEMIBREVIS)
			.forEach(rs -> rss.addAll(rs.makeVariant(1, false, false)));
		rss.addAll(MINIM.makeVariant(2, false, false));
		// Beamed (sm-sf)
		Arrays.asList(SEMIMINIM, FUSA, SEMIFUSA)
			.forEach(rs -> rss.addAll(rs.makeVariant(0, true, false)));
		// Dotted and beamed (sm-fu)
		Arrays.asList(SEMIMINIM, FUSA)
			.forEach(rs -> rss.addAll(rs.makeVariant(1, true, false)));
		// Triplets, basic (br-sf)
		Arrays.asList(BREVIS, SEMIBREVIS, MINIM, SEMIMINIM, FUSA, SEMIFUSA)
			.forEach(rs -> rss.addAll(rs.makeVariant(0, false, true)));
		// Triplets, dotted (br-fu)
		Arrays.asList(BREVIS, SEMIBREVIS, MINIM, SEMIMINIM, FUSA)
			.forEach(rs -> rss.addAll(rs.makeVariant(1, false, true)));
		// Triplets, beamed (sm-sf)
		Arrays.asList(SEMIMINIM, FUSA, SEMIFUSA)
			.forEach(rs -> rss.addAll(rs.makeVariant(0, true, true)));
		// Triplets, dotted and beamed (sm-fu)
		Arrays.asList(SEMIMINIM, FUSA)
			.forEach(rs -> rss.addAll(rs.makeVariant(1, true, true)));
		RHYTHM_SYMBOLS = new LinkedHashMap<String, RhythmSymbol>();
		rss.forEach(rs -> RHYTHM_SYMBOLS.put(rs.getEncoding(), rs));
	}
	
	// 4. Tab symbols
	public static final List<TabSymbol> FRENCH = getTabSymbols(TabSymbolSet.FRENCH);
	public static final List<TabSymbol> ITALIAN = getTabSymbols(TabSymbolSet.ITALIAN);
	public static final List<TabSymbol> SPANISH = getTabSymbols(TabSymbolSet.SPANISH);
	public static final List<TabSymbol> JUDENKUENIG_1523 = getTabSymbols(TabSymbolSet.JUDENKUENIG_1523);
	public static final List<TabSymbol> NEWSIDLER_1536 = getTabSymbols(TabSymbolSet.NEWSIDLER_1536);
	public static final List<TabSymbol> OCHSENKUN_1558 = getTabSymbols(TabSymbolSet.OCHSENKUN_1558);
	public static final List<TabSymbol> HECKEL_1562 = getTabSymbols(TabSymbolSet.HECKEL_1562);
	public static final Map<String, Map<String, TabSymbol>> TAB_SYMBOLS;
	static {
		// RH index finger 
		for (List<TabSymbol> l : Arrays.asList(FRENCH, ITALIAN, SPANISH, 
			JUDENKUENIG_1523, NEWSIDLER_1536, OCHSENKUN_1558, HECKEL_1562)) {
			new ArrayList<>(l).forEach(ts -> l.add(ts.makeVariant(1)));
		}		
		Map<String, List<TabSymbol>> tss = new LinkedHashMap<String, List<TabSymbol>>();
		tss.put(TabSymbolSet.FRENCH.getName(), FRENCH);
		tss.put(TabSymbolSet.ITALIAN.getName(), ITALIAN);
		tss.put(TabSymbolSet.SPANISH.getName(), SPANISH);
		tss.put(TabSymbolSet.JUDENKUENIG_1523.getName(), JUDENKUENIG_1523);
		tss.put(TabSymbolSet.NEWSIDLER_1536.getName(), NEWSIDLER_1536);
		tss.put(TabSymbolSet.OCHSENKUN_1558.getName(), OCHSENKUN_1558);
		tss.put(TabSymbolSet.HECKEL_1562.getName(), HECKEL_1562);
		TAB_SYMBOLS = new LinkedHashMap<String, Map<String, TabSymbol>>();
		for (Entry<String, List<TabSymbol>> e : tss.entrySet()) {
			Map<String, TabSymbol> curr = new LinkedHashMap<String, TabSymbol>();
			e.getValue().forEach(ts -> curr.put(ts.getEncoding(), ts));
			TAB_SYMBOLS.put(e.getKey(), curr);
		}
	}

	private String encoding;
	private String symbol;


	//////////////////////////////
	//
	//  S E T T E R S
	//  for instance variables
	//
	void setEncoding(String e) {
		encoding = e;
	}


	void setSymbol(String s) {
		symbol = s;
	}


	//////////////////////////////
	//
	//  G E T T E R S
	//  for instance variables
	//
	public String getEncoding() {
		return encoding;
	}


	public String getSymbol() {
		return symbol;
	}


	////////////////////////////////
	//
	//  C L A S S  M E T H O D S
	//  symbol retrieval
	//
	// TESTED
	public static ConstantMusicalSymbol getConstantMusicalSymbol(String e) {
		return !CONSTANT_MUSICAL_SYMBOLS.containsKey(e) ? null : CONSTANT_MUSICAL_SYMBOLS.get(e);
	}


	// TESTED
	public static MensurationSign getMensurationSign(String e) {
		// Check for double MS 
		return !MENSURATION_SIGNS.containsKey(e) ? null : MENSURATION_SIGNS.get(e);
	}


	// TESTED
	public static MensurationSign getMensurationSign(Integer[] m, int sl) {
		for (MensurationSign ms : MENSURATION_SIGNS.values()) {
		    if (ms.getMeter()[0] == m[0] && ms.getMeter()[1] == m[1]) { 
		    	if ((sl != -1 && ms.getStaffLine() == sl) || 
		    		(sl == -1 && ms.getStaffLine() == MensurationSign.DEFAULT_STAFFLINE)) {
		    		return ms;
		    	}
		    }
		}
		return null;
	}


	// TESTED
	public static RhythmSymbol getRhythmSymbol(String e) {
		return !RHYTHM_SYMBOLS.containsKey(e) ? null : RHYTHM_SYMBOLS.get(e);
	}


	// TESTED
	public static RhythmSymbol getRhythmSymbol(int d, boolean isCorona, boolean isBeamed, 
		String tripletType) {
		List<Integer> durations = new ArrayList<>();
		RHYTHM_SYMBOLS.values().forEach(rs -> durations.add(rs.getDuration()));

		return 
			!durations.contains(d) ? null : 
			RHYTHM_SYMBOLS.values().stream()
			.filter(
				rs -> rs.getDuration() == d &&
				rs.getEncoding().startsWith(CORONA_INDICATOR) == isCorona && // corona check
				rs.getEncoding().endsWith(RhythmSymbol.BEAM) == isBeamed && // beam check
				(rs.isTriplet() != null ? rs.isTriplet().equals(tripletType) : 
				rs.isTriplet() == tripletType) // triplet check
			)
			.findFirst().get();
	}


	// TESTED
	public static TabSymbol getTabSymbol(String e, TabSymbolSet tss) {
		String n = tss.getName(); 
		return !TAB_SYMBOLS.containsKey(n) ? null :
			(!TAB_SYMBOLS.get(n).containsKey(e) ? null : TAB_SYMBOLS.get(n).get(e));
	}


	// TESTED
	public static List<TabSymbol> getTabSymbols(TabSymbolSet tss) {
		List<TabSymbol> allTs = new ArrayList<TabSymbol>();

		boolean isGerman = tss.getType().equals("German");
		String[][] frets = 
			!isGerman ? TabSymbolSet.FRETS.get(TabSymbolSet.FRETS_FRENCH) : 
			TabSymbolSet.FRETS.get(TabSymbolSet.FRETS_GERMAN);
		// For each course
		for (int c = 0; c < tss.getMaxNumCourses(); c++) {
			String courseStr = String.valueOf(c + 1);
			// For each fret
			for (int f = 0; f < frets[0].length; f++) {
				if (!isGerman) {
					String fretStr = tss == TabSymbolSet.FRENCH ? frets[0][f] : String.valueOf(f);
					allTs.add(new TabSymbol(fretStr + courseStr, fretStr, f, c + 1));
				}
				else {
					if (c + 1 == 6) {
						// Only if the course has enough frets encoded
						if (f < tss.getFretsSixthCourse().length) {
							String e = tss.getFretsSixthCourse()[f];
							allTs.add(new TabSymbol(e, e, f, c + 1));
						}
					}
					else {
						String e = frets[c][f];
						allTs.add(new TabSymbol(e, e, f, c + 1));
					}
				}
			}
		}
		return allTs;
	}


	// TESTED
	public static TabSymbol getTabSymbolEquivalent(TabSymbol ts, TabSymbolSet tss) {
		int c = ts.getCourse();
		int f = ts.getFret();
		String e;
		if (tss == TabSymbolSet.FRENCH) {
			e = TabSymbolSet.FRETS.get(TabSymbolSet.FRETS_FRENCH)[0][f] + String.valueOf(c);
		}
		else if (tss == TabSymbolSet.ITALIAN || tss == TabSymbolSet.SPANISH) {
			e = String.valueOf(f) + String.valueOf(c);
		}
		else {
			e = c == 6 ? tss.getFretsSixthCourse()[f] :
				TabSymbolSet.FRETS.get(TabSymbolSet.FRETS_GERMAN)[c - 1][f];
		}
		return getTabSymbol(e + TabSymbol.FINGERING_DOT_ENCODING.repeat(ts.getFingeringDots()), tss);
	}
}