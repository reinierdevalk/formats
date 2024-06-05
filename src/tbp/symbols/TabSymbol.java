package tbp.symbols;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.uos.fmt.musitech.utility.math.Rational;
import external.Tablature;
import external.Tablature.Tuning;

/**
 * @author Reinier de Valk
 * @version 14.04.2023 (last well-formedness check)
 */
public class TabSymbol extends Symbol implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String FINGERING_DOT_ENCODING = "'";

	private int fret;
	private int course;
	private int fingeringDots;

	public static enum TabSymbolSet  {
		FRENCH("French", "French", 8, null),
		ITALIAN("Italian", "Italian", 8, null),
		SPANISH("Spanish", "Spanish", 8, null),
		JUDENKUENIG_1523("Judenkuenig1523", "German", 6, new String[]{"A", "B", "C", "D", "E", "F", "G", "H"}),
		NEWSIDLER_1536("Newsidler1536", "German", 6, new String[]{"+", "A", "B", "C", "D", "E", "F", "G", "H"}),
		OCHSENKUN_1558("Ochsenkun1558", "German", 6, new String[]{"+", "2-", "3-", "4-", "5-", "6-", "7-", "8-", "9-", "10-", "11-"}),
		HECKEL_1562("Heckel1562", "German", 6, new String[]{"+", "A-", "F-", "L-", "Q-", "X-"});

		public static final int FRETS_FRENCH = 0;
		public static final int FRETS_GERMAN = 1;
		public static final List<String[][]> FRETS;
		static {
			FRETS = Arrays.asList(
				new String[][] {
					{"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l"} // 11 frets per course
				},
				new String[][] {
					{"5", "e", "k", "p", "v", "9", "e-", "k-", "p-", "v-", "9-"}, // 11 frets per course
					{"4", "d", "i", "o", "t", "7", "d-", "i-", "o-", "t-", "7-"},
					{"3", "c", "h", "n", "s", "z", "c-", "h-", "n-", "s-", "z-"},
					{"2", "b", "g", "m", "r", "y", "b-", "g-", "m-", "r-", "y-"},
					{"1", "a", "f", "l", "q", "x", "a-", "f-", "l-", "q-", "x-"}
				}
			);
//			FRETS = new ArrayList<>();
//			FRETS.add(new String[][] {
//				{"a", "b", "c", "d", "e", "f", "g", "h", "i", "k", "l"} // 11 frets per course
//			});
//			FRETS.add(new String[][] {
//				{"5", "e", "k", "p", "v", "9", "e-", "k-", "p-", "v-", "9-"}, // 11 frets per course
//				{"4", "d", "i", "o", "t", "7", "d-", "i-", "o-", "t-", "7-"},
//				{"3", "c", "h", "n", "s", "z", "c-", "h-", "n-", "s-", "z-"},
//				{"2", "b", "g", "m", "r", "y", "b-", "g-", "m-", "r-", "y-"},
//				{"1", "a", "f", "l", "q", "x", "a-", "f-", "l-", "q-", "x-"}
//			});
		}

		private String name;
		private String type;
		private int maxNumCourses;
		private String[] fretsSixthCourse;

		TabSymbolSet(String n, String t, int m, String[] f) {
			name = n;
			type = t;
			maxNumCourses = m;
			fretsSixthCourse = f;
		}

		public String getName() {
			return name;
		}

		public String getType() {
			return type;
		}

		public int getMaxNumCourses() {
			return maxNumCourses;
		}

		public String[] getFretsSixthCourse() {
			return fretsSixthCourse; 
		}

		public static TabSymbolSet getTabSymbolSet(String n, String t) {
			if (n != null) {
				for (TabSymbolSet tss : TabSymbolSet.values()) { 
					if (tss.getName().equals(n)) {
						return tss;
					}
				}
			}
			if (t != null) {
				return t.equals("German") ? TabSymbolSet.NEWSIDLER_1536 : getTabSymbolSet(t, null);
			}
			return null;
		}
	}


	///////////////////////////////
	//
	//  C O N S T R U C T O R S
	//
	public TabSymbol(String e, String s, int f, int c) {
		init(e, s, f, c);
	}


	private void init(String e, String s, int f, int c) {
		setEncoding(e);
		setSymbol(s);
		setFret(f);
		setCourse(c);
		setFingeringDots();
	}


	//////////////////////////////
	//
	//  S E T T E R S  
	//  for instance variables
	//
	void setFret(int f) {
		fret = f;
	}


	void setCourse(int c) {
		course = c;
	}


	void setFingeringDots() {
		fingeringDots = 
			(int) getEncoding().chars()
			.filter(c -> c == FINGERING_DOT_ENCODING.charAt(0)).count();
	}


	//////////////////////////////
	//
	//  G E T T E R S  
	//  for instance variables
	//
	public int getFret() {
		return fret;
	}


	public int getCourse() {
		return course;
	}


	public int getFingeringDots() {
		return fingeringDots;
	}


	////////////////////////////////
	//
	//  C L A S S  M E T H O D S
	//
	/**
	 * Returns the TabSymbol duration of the given CMN duration.
	 *  
	 * @param dur
	 * @return
	 */
	// TESTED
	public static int getTabSymbolDur(Rational dur) { // TODO to TabSymbol
		return dur.mul(Tablature.SRV_DEN).getNumer();
	}


	//////////////////////////////////////
	//
	//  I N S T A N C E  M E T H O D S
	//
	/**
	 * Makes a variant (RH-dotted) of the TS.
	 * 
	 * @param fingeringDots
	 * @return
	 */
	// TESTED
	public TabSymbol makeVariant(int fingeringDots) {
		String e = getEncoding() + FINGERING_DOT_ENCODING.repeat(fingeringDots); 
		return new TabSymbol(e, getSymbol(), getFret(), getCourse());
	}


	/**
	 * Returns the TabSymbol's pitch, as a MIDI number, in the given tuning.
	 * 
	 * @param t
	 * @return 
	 */
	// TESTED
	public int getPitch(Tuning t) {
		List<Integer> openCourses = t.getPitches();
		Collections.reverse(openCourses);
		return openCourses.get(getCourse() - 1) + getFret();
	}


	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof TabSymbol)) {
			return false;
		}
		TabSymbol t = (TabSymbol) o;
		return 
			getEncoding().equals(t.getEncoding()) &&
			getSymbol().equals(t.getSymbol()) &&
			getFret() == t.getFret() &&
			getCourse() == t.getCourse() &&
			getFingeringDots() == t.getFingeringDots();
	}
}