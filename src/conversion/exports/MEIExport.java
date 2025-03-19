package conversion.exports;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import de.uos.fmt.musitech.data.score.NotationChord;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.utility.math.Rational;
import external.Tablature;
import external.Transcription;
import external.Tablature.Tuning;
import interfaces.CLInterface;
import interfaces.PythonInterface;
import internal.core.Encoding;
import internal.core.ScorePiece;
import internal.structure.Event;
import internal.structure.ScoreMetricalTimeLine;
import internal.structure.Timeline;
import tbp.symbols.MensurationSign;
import tbp.symbols.RhythmSymbol;
import tbp.symbols.Symbol;
import tbp.symbols.TabSymbol;
import tbp.symbols.TabSymbol.TabSymbolSet;
import tools.ToolBox;
import tools.labels.LabelTools;
import tools.music.PitchKeyTools;
import tools.music.TimeMeterTools;
import tools.text.StringTools;

public class MEIExport {

	public static final Rational TRIPLETISER = new Rational(3, 2);
	public static final Rational DETRIPLETISER = new Rational(2, 3);
	public static final int BREVE = -1;
	public static final int LONG = -2;

	public static final String TAB = "    ";

	public static final String MEI_EXT = ".mei";
	public static final String XML_EXT = ".xml";
	
	public static final List<String> MEI_HEAD = Arrays.asList("title");
	public static final List<String> STRINGS = Arrays.asList(
		"pname", "oct", "accid", "accid.ges", "tie", "dur", "dots", "xml:id"
	);	
	public static final List<String> INTS = Arrays.asList(
		"ind", "indTab", "bar", 
		"onsetNum", "onsetDen", "metPosNum", "metPosDen", 
		"dur", "dots", 
		"tripletOpen", "tripletMid", "tripletClose", 
		"beamOpen", "beamClose"
	);

	private static boolean ONLY_TAB, ONLY_TRANS, TAB_AND_TRANS;
	private static boolean TAB_ON_TOP, GRAND_STAFF;
	private static boolean verbose = false;
	private static boolean addAccidGes = true;
	
	private static List<String> xmlIDs;
	private static final int LEN_ID = 8;

//	private static String templatesPath;
//	private static String pythonPath;

	public static void main(String[] args) {
		
	}

	public static void main2(String[] args) {
		String testTabFile = "4471_40_cum_sancto_spiritu";
//		testTabFile = "5263_12_in_exitu_israel_de_egipto_desprez-3";
//		testTabFile = "4465_33-34_memor_esto-2";
//		testTabFile = "5255_04_stabat_mater_dolorosa_desprez-2";
//		testTabFile = "5254_03_benedicta_es_coelorum_desprez-1";
//		testTabFile = "5256_05_inviolata_integra_desprez-2";
//		testTabFile = "5256_05_inviolata_integra_desprez-3";
//		testTabFile = "4vv/BSB-mus.ms._272-mille_regres";

//		testTabFile = "3vv/newsidler-1536_7-disant_adiu";
//		testTabFile = "3vv/newsidler-1536_7-mess_pensees";
//		testTabFile = "3vv/pisador-1552_7-pleni_de"; // TODO remove every second barline
//		testTabFile = "3vv/judenkuenig-1523_2-elslein_liebes";
//		testTabFile = "3vv/newsidler-1544_2-nun_volget"; // TODO remove every second barline in ternary part
//		testTabFile = "3vv/phalese-1547_7-tant_que-3vv";
		
		testTabFile = "4vv/rotta-1546_15-bramo_morir";
		
		Tablature testTab = new Tablature(new File(
			"F:/research/data/annotated/encodings/thesis-int/" + testTabFile + 
			Encoding.TBP_EXT));
		
		testTab = new Tablature(new File(
			"C:/Users/Reinier/Desktop/test-capirola/tab/capirola-1520-et_in_terra_pax" + 
			Encoding.TBP_EXT));
		
//		exportTabMEIFile(testTab, "C:/Users/Reinier/Desktop/test-capirola/capirola-1520-et_in_terra_pax" + "-tab");	
//		System.exit(0);
		
		String notationtypeStr = "tab.lute.italian"; // give as param to method
		String tuningStr = "lute.renaissance.6"; // give as param to method
		
		String path = "C:/Users/Reinier/Desktop/MEI/";
		path = "C:/Users/Reinier/Desktop/IMS-tours/example/MIDI/";
		
		String tabFile = "thesis-int/3vv/newsidler-1544_2-nun_volget";
		tabFile = "1132_13_o_sio_potessi_donna_berchem_solo";

		String pieceName = "capirola-1520-et_in_terra_pax";
		pieceName = "1025_adieu_mes_amours";

		// This must be a created Transcription and the second argument must be null
		Transcription trans = 
//			null;
			new Transcription(
////			new File("F:/research/data/MIDI/thesis-int/4vv/rotta-1546_15-bramo_morir.mid")
////			new File("F:/research/data/annotated/MIDI/thesis-int/3vv/newsidler-1544_2-nun_volget.mid")
////			new File("F:/research/data/MIDI/" + tabFile + MIDIImport.EXTENSION)
////			new File("C:/Users/Reinier/Desktop/MEI/newsidler-1544_2-nun_volget-test.mid")
////			new File("C:/Users/Reinier/Desktop/2019-ISMIR/test/mapped/3610_033_inter_natos_mulierum_morales_T-rev-mapped.mid")
////			new File("C:/Users/Reinier/Desktop/IMS-tours/fold_06-1025_adieu_mes_amours.mid")
////			new File("C:/Users/Reinier/Desktop/IMS-tours/example/MIDI/Berchem_-_O_s'io_potessi_donna.mid")
////			new File("C:/Users/Reinier/Desktop/test-capirola/mapped/" + pieceName + MIDIImport.EXTENSION)
//			new File("C:/Users/Reinier/Desktop/beaming/mapped/" + pieceName + MIDIImport.EXTENSION)
			new File("F:/research/experiments/thesis/exp_3.1/thesis-int/3vv/N/bwd/out/fold_03-judenkuenig-1523_2-elslein_liebes.mid")		
		);


//		Tablature tab = 
//			new Tablature(new File("F:/research/data/encodings/" + tabFile + Encoding.EXTENSION), true);
//		Tablature tab = 
////			new Tablature(new File("C:/Users/Reinier/Desktop/test-capirola/tab/" +
//			new Tablature(new File("C:/Users/Reinier/Desktop/beaming/tab/" +
//			pieceName + Encoding.EXTENSION), true);
		Tablature tab = 
			new Tablature(new File("F:/research/data/annotated/encodings/thesis-int/" + 
			"3vv/judenkuenig-1523_2-elslein_liebes" + 
//			"4vv/rotta-1546_15-bramo_morir" +
			Encoding.TBP_EXT), true);
//		tab = null;
		
//		List<List<String[]>> data = getData(t);
//		List<Object> data = getData(t);
//		List<List<Integer>> mismatchInds = 
//			ToolBox.getStoredObjectBinary(new ArrayList<List<Integer>>(), 
//			new File("C:/Users/Reinier/Desktop/2019-ISMIR/test/mapped/" + 
//			"3610_033_inter_natos_mulierum_morales_T-rev" + "-mismatchInds" + ".ser"));
		List<List<Integer>> mismatchInds = new ArrayList<>();
		mismatchInds.add(new ArrayList<Integer>());
		mismatchInds.add(new ArrayList<Integer>());
		mismatchInds.add(new ArrayList<Integer>());
		mismatchInds.add(new ArrayList<Integer>());
		mismatchInds.add(new ArrayList<Integer>());
		
		boolean grandStaff = true;
		boolean tabOnTop = true;
		String s = path + "newsidler-1544_2-nun_volget-test";
		s = path + "fold_06-1025_adieu_mes_amours";
		s = path + "Berchem_-_O_s'io_potessi_donna";
		s = "C:/Users/Reinier/Desktop/test-capirola/" + pieceName;
		s = "C:/Users/Reinier/Desktop/beaming/" + pieceName;
		s = "C:/Users/Reinier/Desktop/" + "judenkuenig-1523_2-elslein_liebes";
		
//		List<Integer[]> mi = (tab == null) ? trans.getMeterInfo() : tab.getMeterInfo();
		
		Map<String, String> paths = null;
		exportMEIFile(trans, tab, /*tab.getBasicTabSymbolProperties(), trans.getKeyInfo(), 
			tab.getTripletOnsetPairs(),*/ mismatchInds, paths, 
			null, new String[]{s, ""});
//		System.out.println(ToolBox.readTextFile(new File(s)));

//		String scoreType = grandStaff ? "grand_staff" : "score";
//		ToolBox.storeTextFile(mei, 
//			new File(path + t.getNumberOfVoices() + Runner.voices + "/" + t.getPieceName() + "-" + 
//			scoreType + ".xml"));
//		System.out.println(Arrays.asList(data.get(0).get(0)));
	}


	//////////////////////////////
	//
	//  S E T T E R S  
	//  for instance variables
	//
//	public static void setTemplatesPath(String arg) {
//		templatesPath = arg;
//	}


//	public static void setPythonPath(String arg) {
//		pythonPath = arg;
//	}


	//////////////////////////////
	//
	//  G E T T E R S  
	//  for instance variables
	//
//	public static String getTemplatesPath() {
//		return templatesPath;
//	}


//	public static String getPythonPath() {
//		return pythonPath;
//	}


	////////////////////////////////
	//
	//  C L A S S  M E T H O D S
	//
	/**
	 * Exports the given Transcription or Tablature as an MEI file. If a save path is provided,
	 * the file is saved at the given path; else, its contents are returned as a String.
	 * 
	 * @param trans Must be a Transcription created setting the encodingFile argument to null
	 *              (i.e., one that has basicNoteProperties).
	 * @param tab
	 * @param mismatchInds
	 * @param grandStaff
	 * @param tabOnTop
	 * @param paths
	 * @param transParams
	 * @param dict
	 * 
	 * @return
	 */
	public static String exportMEIFile(Transcription trans, Tablature tab, List<List<Integer>> mismatchInds, 
		Map<String, String> transParams, Map<String, String> paths, String[] dict) {
//		System.out.println("\r\n>>> MEIExport.exportMEIFile() called");

//		if (transParams != null) {
//			System.out.println("hier!");
//			for (Map.Entry<String, String> entry : transParams.entrySet()) {
//				System.out.println(entry.getKey() + " -- " + entry.getValue());
//			}
//		}

		String INDENT_SCORE = TAB.repeat(4); // for the <score>
		String INDENT_ONE = INDENT_SCORE + TAB; // for the main <scoreDef> and all <section>s
		String INDENT_TWO = INDENT_SCORE + TAB.repeat(2); // for the first child of each <section>

		String tp = StringTools.getPathString(Arrays.asList(paths.get("TEMPLATES_PATH")));
		String mei = ToolBox.readTextFile(new File(tp + paths.get("MEI_TEMPLATE")));
		mei = StringTools.crlf2lf(mei);
		String path = dict[0];

		boolean includeTab = transParams.get(CLInterface.TABLATURE).equals("y");

		ONLY_TAB = tab != null && trans == null;
		TAB_AND_TRANS = tab != null && trans != null;
		ONLY_TRANS = 
			(tab == null && trans != null) // there is no tab
			||
			((tab != null && !includeTab) && trans != null); // there is a tab but it is not included
	
		if (TAB_AND_TRANS) {
			TAB_ON_TOP = transParams.get(CLInterface.PLACEMENT).equals("t");
		}
		if (TAB_AND_TRANS || ONLY_TRANS) {
			GRAND_STAFF = transParams.get(CLInterface.STAFF).equals("d"); // TODO change GRAND_STAFF or !GRAND_STAFF into d, v, s
		}
			
		List<Integer[]> mi = 
			ONLY_TAB || TAB_AND_TRANS ? tab.getMeterInfoAgnostic() : trans.getMeterInfo();

		List<Integer[]> ki = null;
		int numVoices = -1;
		if (TAB_AND_TRANS || ONLY_TRANS) {
			ki = trans.getKeyInfo();
			if (ki != null && ki.size() == 0) {
				ki.add(new Integer[]{-2, 0, 0, 0, 0, 0});
			}
//			ki.add(new Integer[]{2, 0, -1, -1, 44, 1}); // for testing
			// Adapt bars in ki to tablature bars (the barring in ki is equal to that 
			// in trans.getMeterInfo()
			if (TAB_AND_TRANS) {
				ki = rebarKeyInfo(tab, ki);
			}

			// Align mi and ki
			List<List<Integer[]>> miKiAligned = alignMeterAndKeyInfo(mi, ki);
			mi = miKiAligned.get(0);
			ki = miKiAligned.get(1);

			numVoices = trans.getNumberOfVoices();
		}

		// 1. Make the <meiHead> and replace in template
		// See https://music-encoding.org/guidelines/v5/content/metadata.html#headerstructure
		String[] meiHead = new String[MEI_HEAD.size()];
		meiHead[MEI_HEAD.indexOf("title")] = ONLY_TAB || TAB_AND_TRANS ? tab.getName() : trans.getName();
		mei = mei.replace("title_placeholder", meiHead[MEI_HEAD.indexOf("title")]);
		mei = mei.replace("date_placeholder", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		String version = "1.0.0"; // TODO
		mei = mei.replace("version_placeholder", version);
		mei = mei.replace("input_file_placeholder", "Input file: " + dict[1]);
		mei = mei.replace("tool_placeholder", dict[2]);
		

		// 2. Make the <music> and replace in template. The <music> consists of the <score>, 
		// containing one or more <section>s. Each <section> consists of a <scoreDef> followed 
		// by a set of <measure>s (containing tab bars, tab and trans bars, or only trans bars)
		xmlIDs = new ArrayList<>();
		int numBars = mi.get(mi.size()-1)[Transcription.MI_LAST_BAR];
		List<Integer> sectionBars = ToolBox.getItemsAtIndex(mi, Transcription.MI_FIRST_BAR);
		// a. Get all <scoreDef>s
		List<List<String>> scoreDefs = makeScoreDefs(tab, mi, ki, transParams, sectionBars, numVoices);
		// b. Get all tab bars
		List<List<String>> tabBars = null;
		if (ONLY_TAB || TAB_AND_TRANS) {
			tabBars = getTabBars(tab, numVoices);
		}
		// c. Get all trans bars
		List<List<String>> transBars = null;
		if (TAB_AND_TRANS || ONLY_TRANS) {
			List<Rational[]> tripletOnsetPairs = TAB_AND_TRANS ? tab.getTripletOnsetPairs() : null;
			List<Object> data = getData(tab, trans, mi, ki, tripletOnsetPairs);				
			data = beam(data, mi, tripletOnsetPairs, mismatchInds, paths, numVoices);
			transBars = getTransBars(data, tripletOnsetPairs, mismatchInds, numVoices);
		}
		
		// d. Add the main scoreDef, which is placed before the first section. All
		// <section>s after the main one have their <scoreDef> as the first child
		StringBuilder scorePlaceholder = new StringBuilder();
		scoreDefs.get(0).forEach(s -> scorePlaceholder.append(INDENT_ONE + s + "\n")); // WOENS
		// e. Add the <section>s
		for (int i = 0; i < sectionBars.size(); i++) {
			int sectionFirstBar = sectionBars.get(i);
			Integer[] currMi = mi.get(
				ToolBox.getItemsAtIndex(mi, Transcription.MI_FIRST_BAR).indexOf(sectionFirstBar)
			);
			List<String> currScoreDef = scoreDefs.get(i);

			// Add <section>
			String[][] atts = new String[][]{
				{"xml:id", addUniqueID("s", xmlIDs).get(xmlIDs.size() - 1)},	
				{"n", String.valueOf((i+1))}
			};
			if (i > 0) {
				atts = Arrays.copyOf(atts, atts.length + 1);
				String label;
				// If there is an unspecified meter change (i.e., bar length change), currScoreDef is null
				if (currScoreDef == null) {
					label = "meter change (unspecified)";
				}
				else {
					if (ONLY_TAB) {
						label = "meter change";
					}
					else {
						Integer[] currKi = ki.get(
							ToolBox.getItemsAtIndex(ki, Transcription.KI_FIRST_BAR).indexOf(sectionFirstBar)
						);
						label = 
							currMi[currMi.length - 1] == 1 && currKi[currKi.length - 1] == 0 ? "meter change" :
							(currMi[currMi.length - 1] == 0 && currKi[currKi.length - 1] == 1 ? "key change" : 
							 "meter and key change"
							);
					}
				}
				atts[atts.length -1] = new String[]{"label", label};
			}
			scorePlaceholder.append(INDENT_ONE + makeOpeningTag("section", false, atts) + "\n"); // WOENS

			// Add <scoreDef>
			if (i > 0 && currScoreDef != null) {
				scoreDefs.get(i).forEach(s -> scorePlaceholder.append(INDENT_TWO + s + "\n")); // WOENS
			}

			// Add <measure>s
			int currFirstBar = currMi[Tablature.MI_FIRST_BAR];
			int currLastBar = currMi[Tablature.MI_LAST_BAR];
			for (int b = currFirstBar; b <= currLastBar; b++) {
				scorePlaceholder.append(INDENT_TWO + makeOpeningTag("measure", false, 
					new String[][]{
						{"xml:id", addUniqueID("m", xmlIDs).get(xmlIDs.size() - 1)},
						{"n", String.valueOf(b)},
						(b == numBars ? new String[]{"right", "end"} : null)

					}
				) + "\n"); // WOENS

				if (ONLY_TAB || (TAB_AND_TRANS && TAB_ON_TOP)) {
					tabBars.get(b-1).forEach(s -> scorePlaceholder.append(INDENT_TWO + TAB + s + "\n")); // WOENS
					if (!ONLY_TAB) {
						transBars.get(b-1).forEach(s -> scorePlaceholder.append(INDENT_TWO + TAB + s + "\n")); // WOENS
					}
				}
				if ((TAB_AND_TRANS && !TAB_ON_TOP) || ONLY_TRANS) {
					transBars.get(b-1).forEach(s -> scorePlaceholder.append(INDENT_TWO + TAB + s + "\n")); // WOENS
					if (!ONLY_TRANS) {
						tabBars.get(b-1).forEach(s -> scorePlaceholder.append(INDENT_TWO + TAB + s + "\n")); // WOENS
					}
				}
				scorePlaceholder.append(INDENT_TWO + makeClosingTag("measure")+ "\n"); // WOENS
				
			}
			scorePlaceholder.append(INDENT_ONE + makeClosingTag("section") + "\n"); // WOENS
		}
		mei = mei.replace(INDENT_ONE + "score_placeholder" + "\n", scorePlaceholder.toString()); // WOENS

		// 3. Save
		if (path != null) { 
			ToolBox.storeTextFile(
				mei, new File(path /*+ "-" + (GRAND_STAFF ? "grand_staff" : "score") + MEI_EXT*/)
			);
			return null;
		}
		else {
			return mei;
		}
	}


	private static List<List<String>> makeScoreDefs(Tablature tab, List<Integer[]> mi, 
		List<Integer[]> ki, Map<String, String> transParams, List<Integer> sectionBars, int numVoices) {
//		System.out.println("\r\n>>> makeScoreDefs() called");

		// The <scoreDef> contains a <staffGrp>, which contains one (TAB_ONLY case) or more 
		// (other cases) <staffDef>s. In the TAB_AND_TRANS case, the <staffDef>s for 
		// the CMN are wrapped in another <staffGrp> to enable across-staff barlines
		//
		// TAB_ONLY case
		// <scoreDef>
		//     <staffGrp>
		//         <staffDef> ... </staffDef>					| = staffDefTab
		//     </staffGrp>
		// <scoreDef>/
		//
		// TRANS_ONLY case
		// <scoreDef>
		//     <staffGrp symbol='bracket' bar.thru='true'>		|
		//         <staffDef> ... </staffDef>					|
		//         ...											| = staffGrpTrans
		//         <staffDef> ... </staffDef>					|
		//     </staffGrp>										|
		// <scoreDef>/
		//
		// TAB_AND_TRANS case (with tab on bottom)
		// <scoreDef>
		//     <staffGrp>
		//         <staffGrp symbol='bracket' bar.thru='true'>	|
		//             <staffDef> ... </staffDef> 				|
		//             ...										| = staffGrpTrans
		//             <staffDef> ... </staffDef>				|
		//         </staffGrp>									|
		//         <staffDef> ... </staffDef>					| = staffDefTab
		//     </staffGrp>
		// <scoreDef>/
		List<List<String>> scoreDefs = new ArrayList<>();

		boolean useBasicMEI = true; // TODO

		Tuning tuning = null;
		TabSymbolSet tss = null;
		List<String[]> tabMensSigns = null;
		Integer[] slsTab = null;
		int tabLines = 6;
		if (ONLY_TAB || TAB_AND_TRANS) {
			// tuning
			tuning = Tuning.getTuning(transParams.get(CLInterface.TUNING));

			// tss (type)
			tss = TabSymbolSet.getTabSymbolSet(null, transParams.get(CLInterface.TYPE));				
//				tss = Arrays.stream(TabSymbolSet.values())
//					.filter(t -> t.getShortType().equals(argsType))
//					.findFirst()
//					.orElse(null);
//			}

			// NB: tabMensSigns aligns with mi, i.e., each of its elements corresponds to
			// an element with the same bar and metric time in mi (but not vice versa)
			tabMensSigns = tab.getMensurationSigns();
			slsTab = getStaffAndLayer(
				true, Arrays.asList(ONLY_TAB, TAB_AND_TRANS), TAB_ON_TOP, 
				GRAND_STAFF, numVoices, -1
			);
			
			if (tss.getShortType().equals("GLT")) {
				tabLines = tab.getLargestTablatureChord();
			}
		}

		List<Integer[]> slsTrans = null;
		List<String[]> clefs = null;
		int numStaffs = GRAND_STAFF ? 2 : numVoices;
		if (TAB_AND_TRANS || ONLY_TRANS) {
			slsTrans = new ArrayList<>();
			for (int i = 0; i < numVoices; i++) {
				slsTrans.add(getStaffAndLayer(
					false, Arrays.asList(ONLY_TAB, TAB_AND_TRANS), 
					TAB_ON_TOP, GRAND_STAFF, numVoices, i
				));
			}
			clefs = new ArrayList<>();
			for (int i = 0; i < numStaffs; i++) {
				clefs.add(getCleffing(i+1, numStaffs, GRAND_STAFF));
			}
		}

		for (int bar : sectionBars) {
			List<String> currScoreDef = new ArrayList<>();
			int indInTabMs = 
				(ONLY_TRANS && tab == null) ? -1 :
				ToolBox.getItemsAtIndex(tabMensSigns, 1).indexOf(String.valueOf(bar));
			String tabMs = 
				(ONLY_TRANS && tab == null) ? null :
				(indInTabMs == -1 ? null : tabMensSigns.get(indInTabMs)[0]);

			Integer[] currMi = mi.get(
				ToolBox.getItemsAtIndex(mi, Transcription.MI_FIRST_BAR).indexOf(bar)
			);
			Integer[] currKi = 
				ONLY_TAB ? null : 
				ki.get(ToolBox.getItemsAtIndex(ki, Transcription.KI_FIRST_BAR).indexOf(bar));

			boolean includeTuning = bar == 1;
			boolean includeClef = bar == 1;
			boolean includeKey = (TAB_AND_TRANS || ONLY_TRANS) && currKi[currKi.length - 1] == 1;
			boolean includeMeter = 
				(ONLY_TAB || TAB_AND_TRANS || (ONLY_TRANS && tab != null)) ? 
					currMi[currMi.length - 1] == 1 && tabMs != null : 
					currMi[currMi.length - 1] == 1;

			// Always add a scoreDef at the beginning of the piece; at the remaining meter
			// or key change bars, only add the scoreDef if there is a *specified* meter 
			// change or a key change, else add null
			if (!(includeMeter || includeKey) && scoreDefs.size() > 0) {
				scoreDefs.add(null);
			}
			else {
				// Determine count, unit, and sym
				int count = -1;
				int unit = -1;
				String sym = null;
				if (includeMeter) {
					count = currMi[Transcription.MI_NUM];
					unit = currMi[Transcription.MI_DEN];
					// Overrule if tabMs gives a different meter (occurs only in the tablature
					// case, when the first bar is an anacrusis)
					if (tabMs != null) {
						// Take into account double MS (such as MO.M3), where the individual parts
						// represent the same MS
						String tMs = 
							tabMs.contains(Symbol.SYMBOL_SEPARATOR) ? 
							tabMs.substring(0, tabMs.indexOf(Symbol.SYMBOL_SEPARATOR)) :
							tabMs;
						Integer[] meter = MensurationSign.getMensurationSign(tMs).getMeter();
						if (!(new Rational(meter[0], meter[1]).equals(new Rational(count, unit)))) {
							count = meter[0];
							unit = meter[1];
						}
					}
					if (count == 4 && unit == 4) {
						sym = "common";
					}
					else if (count == 2 && unit == 2) {
						sym = "cut";
					}
				}
				// Determine sig 
				String sig = null;
				if (includeKey) {
					int ks = currKi[Transcription.KI_KEY];
					sig = String.valueOf(Math.abs(ks)) + (ks == 0 ? "" : ((ks < 0) ? "f" : "s"));
				}

				// 1. Make staffDefTab, containing <tuning> and <meterSig> (both optional)
				List<String> staffDefTab = new ArrayList<>();
				if (ONLY_TAB || TAB_AND_TRANS) {
					staffDefTab.add(makeOpeningTag("staffDef", false,
						new String[][]{
							{"xml:id", addUniqueID("sd", xmlIDs).get(xmlIDs.size() - 1)},
							{"n", String.valueOf(slsTab[0])},
							{"lines", String.valueOf(tabLines)},
							{"notationtype", "tab.lute." + tss.getType().toLowerCase()},
							{"tab.dur.sym.ratio", "1"} // TODO remove?
						}
					));
					// Add tuning
					if (includeTuning) {
						boolean isSimpleTuning = false;
						if (isSimpleTuning) {
							staffDefTab.add(TAB + makeOpeningTag("tuning", true,
								new String[][]{
									{"xml:id", addUniqueID("t", xmlIDs).get(xmlIDs.size() - 1)},
									{"tuning.standard", String.join(
										".", "lute", tuning.getEra().toLowerCase(), 
										String.valueOf(tuning.getCourses().size()))}
								}
							));
						}
						else {
							staffDefTab.add(TAB + makeOpeningTag("tuning", false, 
								new String[][]{
									{"xml:id", addUniqueID("t", xmlIDs).get(xmlIDs.size() - 1)}
								}
							));
							List<String> courses = new ArrayList<String>(tuning.getCourses());
							Collections.reverse(courses);
							List<Integer> intervals = new ArrayList<Integer>(tuning.getIntervals());
							Collections.reverse(intervals);
							int pitch = tuning.getPitchLowestCourse() + ToolBox.sumListInteger(intervals) ;
							for (int i = 0; i < courses.size(); i++) {
								staffDefTab.add(TAB + TAB + makeOpeningTag("course", true, 
									new String[][]{
										{"xml:id", addUniqueID("c", xmlIDs).get(xmlIDs.size() - 1)},
										{"n", String.valueOf((i+1))},
										{"pname", courses.get(i).toLowerCase()},
										{"oct", String.valueOf(PitchKeyTools.getOctave(pitch))}
									}
								));
								if (i < courses.size() - 1) {
									pitch -= intervals.get(i);
								}
							}
							staffDefTab.add(TAB + makeClosingTag("tuning"));
						}
					}
					// Add meterSig
					if (includeMeter) {
						// In case of a triple meter, show only the count (always 3)
						// NB: applies only to tablature case
						if (count == 6) {
							count /= 2;
							unit /= 2;
						}
						staffDefTab.add(TAB + makeOpeningTag("meterSig", true,
							new String[][]{
								{"xml:id", addUniqueID("ms", xmlIDs).get(xmlIDs.size() - 1)},
								{"count", String.valueOf(count)},
								{"unit", String.valueOf(unit)},
								(count != 3 ? new String[]{"sym", sym} : new String[]{"form", "num"})
							}
						));
					}
					staffDefTab.add(makeClosingTag("staffDef"));
				}

				// 2. Make staffGrpTrans, containing <staffDef>s containing <clef>, <keySig>, and 
				// <metersig> (all optional)    
				List<String> staffGrpTrans = new ArrayList<>(); 
				if (ONLY_TRANS || TAB_AND_TRANS) {
					int firstStaff = slsTrans.get(0)[0];
					staffGrpTrans.add(makeOpeningTag("staffGrp", false,
						new String[][]{
							{"xml:id", addUniqueID("sg", xmlIDs).get(xmlIDs.size() - 1)},
							{"symbol", "bracket"},
							{"bar.thru", "true"}
						}
					));
					// For each <staffDef> in the <staffGrp>
					for (int i = 0; i < numStaffs; i++) {
						// Add staffDef
						String[][] atts = new String[][]{
							{"xml:id", addUniqueID("sd", xmlIDs).get(xmlIDs.size() - 1)},
							{"n", String.valueOf(firstStaff + i)},
							{"lines", "5"},
						};
						if (useBasicMEI && includeKey) {
							String[][] keysig = new String[][]{{"keysig", sig}};
							atts = Stream.concat(Arrays.stream(atts), Arrays.stream(keysig)).toArray(String[][]::new);
						}
						if (useBasicMEI && includeMeter) {
							String[][] meter = new String[][]{
								{"meter.count", String.valueOf(count)},
								{"meter.unit", String.valueOf(unit)}
							};
							atts = Stream.concat(Arrays.stream(atts), Arrays.stream(meter)).toArray(String[][]::new);
						}
						staffGrpTrans.add(TAB + makeOpeningTag("staffDef", false, atts));
						// Add clef
						if (includeClef) {
							String[] clef = clefs.get(i);
							staffGrpTrans.add(TAB + TAB + makeOpeningTag("clef", true,
								new String[][]{
									{"xml:id", addUniqueID("c", xmlIDs).get(xmlIDs.size() - 1)},
									{"shape", clef[0]},
									{"line", clef[1]}
								} 
							));
						}
						// Add keySig
						if (includeKey) {
							staffGrpTrans.add(TAB + TAB + makeOpeningTag("keySig", true,
								new String[][]{
									{"xml:id", addUniqueID("ks", xmlIDs).get(xmlIDs.size() - 1)},
									{"sig", sig},
									{"mode", currKi[Transcription.KI_MODE] == 0 ? "major" : "minor"}
								}
							));
						}
						// Add meterSig
						if (includeMeter) {
							staffGrpTrans.add(TAB + TAB + makeOpeningTag("meterSig", true,
								new String[][]{
									{"xml:id", addUniqueID("ms", xmlIDs).get(xmlIDs.size() - 1)},
									{"count", String.valueOf(count)},
									{"unit", String.valueOf(unit)},
									(count != 3 ? new String[]{"sym", sym} : 
										(TAB_AND_TRANS ? new String[]{"form", "num"} : null))
								}
							));		
						}
						staffGrpTrans.add(TAB + makeClosingTag("staffDef"));
					}
					staffGrpTrans.add(makeClosingTag("staffGrp"));
				}

				// 3. Construct scoreDef (see schema above)
				currScoreDef.add(makeOpeningTag("scoreDef", false, 
					new String[][]{
						{"xml:id", addUniqueID("sd", xmlIDs).get(xmlIDs.size() - 1)}
					}
				));
				List<String> tabPart = new ArrayList<>();
				staffDefTab.forEach(s -> tabPart.add(TAB.repeat(2) + s));
				List<String> transPart = new ArrayList<>();
				staffGrpTrans.forEach(s -> transPart.add((ONLY_TRANS ? TAB : TAB.repeat(2)) + s));
				if (ONLY_TAB) {
					currScoreDef.add(TAB + makeOpeningTag("staffGrp", false, 
						new String[][]{
							{"xml:id", addUniqueID("sg", xmlIDs).get(xmlIDs.size() - 1)}
						}
					));
					currScoreDef.addAll(tabPart);
					currScoreDef.add(TAB + makeClosingTag("staffGrp"));
				}
				else if (ONLY_TRANS) {
					currScoreDef.addAll(transPart);
				}
				else {
					currScoreDef.add(TAB + makeOpeningTag("staffGrp", false, 
						new String[][]{
							{"xml:id", addUniqueID("sg", xmlIDs).get(xmlIDs.size() - 1)}
						}
					));
					if (TAB_ON_TOP) {
						currScoreDef.addAll(tabPart);
						currScoreDef.addAll(transPart);
					}
					else {
						currScoreDef.addAll(transPart);
						currScoreDef.addAll(tabPart);
					}
					currScoreDef.add(TAB + makeClosingTag("staffGrp"));
				}
				currScoreDef.add(makeClosingTag("scoreDef"));
				scoreDefs.add(currScoreDef);
			}
		}

		return scoreDefs;
	}


	private static List<List<String>> getTabBars(Tablature tab, int numVoices) {
//l		System.out.println("\r\n>>> getTabBars() called");
		List<List<String>> tabBars = new ArrayList<>();

		Integer[] sl = getStaffAndLayer(
			true, Arrays.asList(ONLY_TAB, TAB_AND_TRANS), TAB_ON_TOP, 
			GRAND_STAFF, numVoices, -1
		);
		int staff = sl[0];
		int layer = sl[1];

		// Organise events per bar. Per getEvents(), each bar ends with a barline; any
		// decorative opening barlines must be removed prior to the organisation per bar
		List<List<Event>> eventsPerBar = new ArrayList<>();
		Encoding enc = tab.getEncoding();
		List<Event> events = Encoding.removeDecorativeBarlineEvents(enc.getEvents());
		TabSymbolSet tss = enc.getTabSymbolSet();
		List<Event> eventsCurrBar = new ArrayList<>();
		for (int i = 0; i < events.size(); i++) {
			Event e = events.get(i);
			if (!Encoding.assertEventType(e.getEncoding(), tss, "MensurationSign")) {
				eventsCurrBar.add(e);
				if (Encoding.assertEventType(e.getEncoding(), tss, "barline")) {
					eventsPerBar.add(eventsCurrBar);
					eventsCurrBar = new ArrayList<>();
				}
			}
		}

		// For each bar
		Integer[] prevDurXML = new Integer[]{0, 0};
		for (int i = 0; i < eventsPerBar.size(); i++) {
			List<String> currBarAsXML = new ArrayList<>();

			// Add opening <staff> and <layer>
			currBarAsXML.add(makeOpeningTag("staff", false, 
				new String[][]{
					{"xml:id", addUniqueID("s", xmlIDs).get(xmlIDs.size() - 1)},
					{"n", String.valueOf(staff)}
				}
			));
			currBarAsXML.add(TAB + makeOpeningTag("layer", false, 
				new String[][]{
					{"xml:id", addUniqueID("l", xmlIDs).get(xmlIDs.size() - 1)},
					{"n", String.valueOf(layer)}
				}
			));

			// Add tab bar <note>s
			List<Event> currBarEvents = eventsPerBar.get(i);
			// Remove barline event
			currBarEvents.remove(currBarEvents.size() - 1);
			List<Object> tabBar = getTabBar(currBarEvents, tss, prevDurXML);
			currBarAsXML.addAll((List<String>) tabBar.get(0));

			// Add closing <layer> and <staff>
			currBarAsXML.add(TAB + makeClosingTag("layer"));
			currBarAsXML.add(makeClosingTag("staff"));

			tabBars.add(currBarAsXML);
			prevDurXML = (Integer[]) tabBar.get(1);
		}

		return tabBars;
	}


	private static List<Object> getTabBar(List<Event> currBarEvents, TabSymbolSet tss, 
		Integer[] prevDurXML) {
		List<String> barList = new ArrayList<>();

		String ss = Symbol.SYMBOL_SEPARATOR;

		// For each event in the bar
		boolean beamActive = false;
		for (int i = 0; i < currBarEvents.size(); i++) {
			Event currEventFull = currBarEvents.get(i);
			String currEvent = currEventFull.getEncoding();

			// If there is a footnote: extract any original event
			String currFootnote = currEventFull.getFootnote();
			boolean eventIsCorrected = currFootnote != null && currFootnote.contains("'");
			String currEventOrig = null;
			if (currFootnote != null) {
				currEventOrig =
					eventIsCorrected ? currFootnote.substring(
						currFootnote.indexOf("'")+1, currFootnote.lastIndexOf("'")
					) :
					currFootnote.substring(
						currFootnote.indexOf(Encoding.FOOTNOTE_INDICATOR) + 1
					);
			}

			// Make event, sicList (containing all the <sic> events), and corrList
			// (containing all the <corr> events)
			String event = eventIsCorrected ? null : currEvent;
			String sicEvent = eventIsCorrected ? currEventOrig : null;
			String corrEvent = eventIsCorrected ? currEvent : null;
			List<String> defaultList = Arrays.asList(event);
			List<String> sicList = new ArrayList<>();
			if (sicEvent != null) {
				// Add all events in footnote
				for (String s : sicEvent.split(Symbol.SPACE.getEncoding() + ss)) {
					sicList.add(StringTools.removeTrailingSymbolSeparator(s));
				}
			}
			List<String> corrList = new ArrayList<>();
			if (corrEvent != null) {
				// Add corrEvent and all events with an empty footnote ({@}) that follow
				corrList.add(corrEvent);
				for (int j = i+1; j < currBarEvents.size(); j++) {
					Event nextEventFull = currBarEvents.get(j);
					String nextEvent = nextEventFull.getEncoding();
					String nextFootnote = nextEventFull.getFootnote();
					if (nextFootnote == null) {
						break;
					}
					else if (nextFootnote.equals(Encoding.FOOTNOTE_INDICATOR)) {
						corrList.add(nextEvent);
					}
					else {
						break;
					}
				}
			}

			// Not a barline event: add event to currBarAsXML
			List<String> currEventAsXML = new ArrayList<>();

			List<List<String>> allLists = Arrays.asList(
				!eventIsCorrected ? defaultList : 
				corrList, sicList
			);
			boolean beamActiveDefOrCorrOrSic = beamActive;
			Integer[] prevDurXMLDefOrCorrOrSic = prevDurXML;
			if (eventIsCorrected) {
				currEventAsXML.add(TAB.repeat(2) + makeOpeningTag("choice", false, 
					new String[][]{
						{"xml:id", addUniqueID("c", xmlIDs).get(xmlIDs.size() - 1)}
					}
				));
			}
			for (int j = 0; j < allLists.size(); j++) {
				// currList is
				// - defaultList if allLists.size() == 1
				// - corrList if allLists.size() == 2 and j == 0
				// - sicList if allLists.size() == 2 and j == 1
				List<String> currList = allLists.get(j);
				if (eventIsCorrected) {
					currEventAsXML.add(TAB.repeat(3) + 
						(j == 0 ? makeOpeningTag("corr", false, 
							new String[][]{
								{"xml:id", addUniqueID("c", xmlIDs).get(xmlIDs.size() - 1)}
							}	
						) : 
						makeOpeningTag("sic", false, 
							new String[][]{
								{"xml:id", addUniqueID("s", xmlIDs).get(xmlIDs.size() - 1)}
							}	
						))
					);
				}

				for (int k = 0 ; k < currList.size(); k++) {
					String defOrCorrOrSic = currList.get(k);
					if (!defOrCorrOrSic.endsWith(ss)) {
						defOrCorrOrSic += ss;
					}
					boolean isRSEvent = Encoding.assertEventType(
						defOrCorrOrSic, tss, "RhythmSymbol"
					);
					boolean isBeamed = 
						!isRSEvent ? false : 
						Symbol.getRhythmSymbol(
							defOrCorrOrSic.substring(0, defOrCorrOrSic.indexOf(ss))
						).getBeam();

					boolean openBeam = false;
					boolean closeBeam = false;
					if (!beamActiveDefOrCorrOrSic && isRSEvent && isBeamed) {
						openBeam = true;
						beamActiveDefOrCorrOrSic = true;
					}
					if (beamActiveDefOrCorrOrSic && isRSEvent && !isBeamed) {
						closeBeam = true;
						beamActiveDefOrCorrOrSic = false;
					}

					if (openBeam) {
						currEventAsXML.add(
							TAB.repeat(!eventIsCorrected ? 2 : 4) + makeOpeningTag("beam", false, 
								new String[][]{
									{"xml:id", addUniqueID("b", xmlIDs).get(xmlIDs.size() - 1)}
							}	
						));
					}
					currEventAsXML.addAll(
						getTabGrps(Arrays.asList(new String[]{defOrCorrOrSic}), 
						prevDurXMLDefOrCorrOrSic, eventIsCorrected, 
						beamActiveDefOrCorrOrSic || closeBeam, tss)
					);
					if (closeBeam) {
						currEventAsXML.add(
							TAB.repeat(!eventIsCorrected ? 2 : 4) + makeClosingTag("beam")
						);
					}

					// Update prevDurXMLCorr
					Integer[] currDurXML = getXMLDur(defOrCorrOrSic);
					if (currDurXML != null) {
						prevDurXMLDefOrCorrOrSic = currDurXML;
					}

					// Last <corr>/<sic> event
					if (eventIsCorrected && k == currList.size() - 1) {
						currEventAsXML.add(
							TAB.repeat(3) + (j == 0 ? makeClosingTag("corr") : makeClosingTag("sic"))
						);
						// Reset to before-loop values for second loop iteration
						if (j == 0) {
							beamActiveDefOrCorrOrSic = beamActive;
							prevDurXMLDefOrCorrOrSic = prevDurXML;
						}
					}

					// Update prevDurXML and beamActive
					if (k == currList.size() - 1) {
						// 1. In case of a change of RS, both the <sic> and the <corr> case will still  
						// end with the same RS, so prevDurXML can be set to either prevDurXMLCorr  
						// or prevDurXMLSic. Examples ('n' = notes in event; * = new prevDurXML value):
						//
						//            E   *                           Q   E*
						//     <sic>  n n n (n ...)            <sic>  n   n (n ...)
						// (1)                             (2)
						//            Q   E*                          E   *
						//     <corr> n   n (n ...)            <corr> n n n (n ...) 
						//
						// NB: prevDurXML is only used (in getTabGrps()) if NOT every event has a RS
						prevDurXML = prevDurXMLDefOrCorrOrSic;
						// 2. The same logic applies to beamActive. Examples ('n' = notes in event; * = new
						// beamActive value):
						//
						//            E-E-E-*E                        Q   E-*E
						//     <sic>  n n n (n ...)            <sic>  n   n (n ...)
						// (1)                             (2)
						//            Q   E-*E                        E-E-E-*E
						//     <corr> n   n (n ...)            <corr> n n n (n ...) 
						//
						// NB: beamActive is only used if every event has a RS
						beamActive = beamActiveDefOrCorrOrSic;
					}
				}
			}
			if (eventIsCorrected) {
				currEventAsXML.add(TAB.repeat(2) + makeClosingTag("choice"));
			}
			// Increment i to skip the events in corrList 
			if (eventIsCorrected) {
				i += corrList.size() - 1;
			}
			barList.addAll(currEventAsXML);
		}

		return Arrays.asList(barList, prevDurXML); 
	}


	/**
	 * Converts each event in the given list of events into an XML tabGrp, and concatenates
	 * this tabGrp as a list of <code>String</code>s to the List that is returned.
	 * 
	 * @param events The list of events.
	 * @param prevXMLDur An <code>Integer[]</code> containing the last XML duration (element 0)
	 *                   and number of dots (element 1) encountered before the events in the 
	 *                   given list. 
	 * @param isCorrected Whether or not the tabGrp is part of a corrected tabGrp (i.e., is
	 *        embedded in a {@code<sic>} or {@code <corr>} tag).
	 * @param isBeamed Whether or not the tabGrp has a beamed flag.       
	 * @param tss The TabSymbolSet.
	 * @return A list of tabGrps, each of them formatted as a list of <code>String</code>s.
	 */
	private static List<String> getTabGrps(List<String> events, Integer[] prevXMLDur, 
		boolean isCorrected, boolean isBeamed, TabSymbolSet tss) {
		List<String> tabGrpList = new ArrayList<>();

		String ss = Symbol.SYMBOL_SEPARATOR;
		// If the tabGrp is part of a <sic>/<corr> pair, two extra tabs must be added: 
		// one for the <choice> tag, and one for the <sic>/<corr> tag
		int addedTabs = isCorrected ? 2 : 0;
		// If the tabGrp is beamed, another extra tab must be added for the <beam> tag 
		if (isBeamed ) {
			addedTabs += 1;
		}

		for (int i = 0; i < events.size(); i++) {
			String e = events.get(i);
			String[] currEventSplit = 
				(!e.contains(ss)) ? new String[]{e} : e.split("\\" + ss);

			// If the event is not an MS event (which consist of one or multiple MS)
			if (!Encoding.assertEventType(e, tss, "mensuration")) {
				// In the case of GLT, reverse the event so that the symbol on top 
				// appears on top in the MEI
				if (tss.getType().equals("German") && Encoding.assertEventType(e, tss, "TabSymbol")) {
					boolean isRSEvent = Encoding.assertEventType(e, tss, "RhythmSymbol");
					String[] rev = Arrays.copyOfRange(
						currEventSplit, (isRSEvent ? 1 : 0), currEventSplit.length
					);
					ArrayUtils.reverse(rev);
					if (isRSEvent) {
						rev = ArrayUtils.add(rev, 0, currEventSplit[0]);
					}
					currEventSplit = rev;
				}

				// Determine current duration; update prevDurXML
				Integer[] currXMLDur = getXMLDur(String.join(ss, currEventSplit));
				int dur = currXMLDur != null ? currXMLDur[0] : prevXMLDur[0];
				int dots = currXMLDur != null ? currXMLDur[1] : prevXMLDur[1];
				if (currXMLDur != null) {
					prevXMLDur = currXMLDur;
				}

				// Make tabGrp
				// 1. <tabGrp>
				tabGrpList.add(TAB.repeat(2 + addedTabs) + makeOpeningTag("tabGrp", false, 
					new String[][]{
						{"xml:id", addUniqueID("tg", xmlIDs).get(xmlIDs.size() - 1)},
						{"dur", String.valueOf(dur)},
						(dots > 0 ? new String[]{"dots", String.valueOf(dots)} : null)
					}
				));
				// 2. <tabDurSym> (also covers rests)
				if (currXMLDur != null) {
					tabGrpList.add(TAB.repeat(3 + addedTabs) + makeOpeningTag("tabDurSym", true, 
						new String[][]{{"xml:id", addUniqueID("tds", xmlIDs).get(xmlIDs.size() - 1)}}
					));
				}
				// 3. <note>s (rests are covered by the tabDurSym)
				for (int j = ((currXMLDur != null) ? 1 : 0); j < currEventSplit.length; j++) {
					TabSymbol ts = Symbol.getTabSymbol(currEventSplit[j], tss);
					tabGrpList.add(TAB.repeat(3 + addedTabs) + makeOpeningTag("note", true, 
						new String[][]{
							{"xml:id", addUniqueID("n", xmlIDs).get(xmlIDs.size() - 1)},
							{"tab.course", String.valueOf(ts.getCourse())},
							{"tab.fret", String.valueOf(ts.getFret())},
						}
					));
				}
				tabGrpList.add(TAB.repeat(2 + addedTabs) + makeClosingTag("tabGrp"));
			}
		}

		return tabGrpList;
	}


	private static List<String> addUniqueID(String prefix, List<String> argXmlIDs) {
		String xmlID;

		// Generate ID; repeat until it is not in the list (f necessary); add to list
		do {
			xmlID = prefix + StringTools.randomID(LEN_ID - prefix.length());
		} while (argXmlIDs.contains(xmlID));
		argXmlIDs.add(xmlID); // Add to the list once it's unique

		return argXmlIDs;
    }


	/**
	 * Extracts from the given Transcription the data (note attributes) needed to create the MEI file.
	 * 
	 * @param tab
	 * @param trans
	 * @param mi
	 * @param ki
	 * @param tripletOnsetPairs
	 *
	 * @returns A {@code List<Object>} containing a {@code List<List<List<Integer[]>>>} ({@code lin})  
	 *          and a {@code List<List<List<String[]>>>} ({@code lst}) representing note attributes 
	 *          as described in {@code INTS} and {@code STRINGS}. For both {@code lin} and {@code lst},
	 *          <ul>
	 *          <li>Each element (a {@code List<List<Integer[]>>} or {@code List<List<String[]>>})
	 *              represents a bar.</li> 
	 *          <li>Each bar element (a {@code List<Integer[]>} or {@code List<String[]>}) represents 
	 *              a voice.</li>
	 *          <li>Each bar-voice element (a {@code Integer[]} or {@code String[]}) represents a note.</li>
	 *          </ul>
	 *          
	 *          Thus, {@code lin.size()} (or {@code lst.size()}) gives the number of bars in the piece, and
	 *          <ul>
	 *          <li> {@code lin.get(b)} gives bar {@code b}, and 
	 *               {@code lin.get(b).size()} gives the number of voices in bar {@code b} 
	 *               (and thus in the whole piece, as the number of voices is fixed).</li>
	 *          <li> {@code lin.get(b).get(v)} gives bar {@code b}, voice {@code v}, and 
	 *               {@code lin.get(b).get(v).size()} gives the number of notes in that voice.</li>
	 *          <li>{@code lin.get(b).get(v).get(n)} gives bar {@code b}, voice {@code v}, note {@code n}, and 
	 *               {@code lin.get(b).get(v).get(n).length} gives the number of attributes of that note.</li>  
	 *          </ul>
	 */
	private static List<Object> getData(Tablature tab, Transcription trans, List<Integer[]> mi, 
		List<Integer[]> ki, List<Rational[]> tripletOnsetPairs) {
//l		System.out.println("\r\n>>> getData() called");

		Integer[][] bnp = trans.getBasicNoteProperties();
		Integer[][] btp = null;
		Timeline tla = null;
		List<List<Integer>> transToTabInd = null;
		if (ONLY_TAB || TAB_AND_TRANS) {
			btp = tab.getBasicTabSymbolProperties();
			tla = tab.getEncoding().getTimelineAgnostic();
			transToTabInd = Transcription.alignTabAndTransIndices(btp, bnp).get(1);
		}
		ScorePiece p = trans.getScorePiece();
		ScoreMetricalTimeLine smtl = p.getScoreMetricalTimeLine();
		int numVoices = p.getScore().size();
		int numBars = mi.get(mi.size()-1)[Transcription.MI_LAST_BAR];
		List<Integer> sectionBars = ToolBox.getItemsAtIndex(mi, Transcription.MI_FIRST_BAR);
		Rational endOffset = TimeMeterTools.getFinalOffset(mi);
		Rational gridVal = 
			ONLY_TAB || TAB_AND_TRANS ? Tablature.SMALLEST_RHYTHMIC_VALUE : 
			Transcription.SMALLEST_RHYTHMIC_VALUE;
		List<Integer> gridNums = IntStream.rangeClosed(0, gridVal.getDenom())
			.boxed()
			.collect(Collectors.toList());

		List<List<String[]>> voicesStrs = new ArrayList<List<String[]>>();
		List<List<Integer[]>> voicesInts = new ArrayList<List<Integer[]>>();
		for (int i = 0; i < numVoices; i++) {
			voicesStrs.add(new ArrayList<String[]>());
			voicesInts.add(new ArrayList<Integer[]>());
		}
		List<Integer> naturalsAlreadyAdded = new ArrayList<Integer>();
		List<Integer> accidentalsAlreadyAdded = new ArrayList<Integer>();
		List<Integer> doubleFlatsInEffect = new ArrayList<>();
		List<Integer> flatsInEffect = new ArrayList<>();
		List<Integer> naturalsInEffect = new ArrayList<>();
		List<Integer> sharpsInEffect = new ArrayList<>();
		List<Integer> doubleSharpsInEffect = new ArrayList<>();
		String pitchesNotInKey = "";
		List<String> uniquePitchesNotInKey = new ArrayList<>();
		Integer[] currKi = ki.get(0); 
		List<Object> grids = PitchKeyTools.createGrids(
			currKi[Transcription.KI_KEY], currKi[Transcription.KI_MODE]
		);
		Rational barEnd = new Rational(
			mi.get(0)[Transcription.MI_NUM], mi.get(0)[Transcription.MI_DEN]
		);
		for (int i = 0; i < bnp.length; i++) {
			int iTab = ONLY_TAB || TAB_AND_TRANS ? transToTabInd.get(i).get(0) : -1; // each element (list) contains only one element (int)		
			int pitch = bnp[i][Transcription.PITCH];
			Rational dur = 
				ONLY_TAB ? new Rational(btp[iTab][Tablature.MIN_DURATION], Tablature.SRV_DEN) :
				new Rational(bnp[i][Transcription.DUR_NUMER], bnp[i][Transcription.DUR_DENOM]);
			dur = TimeMeterTools.round(dur, gridNums);
			Rational onset = 
				ONLY_TAB ? new Rational(btp[iTab][Tablature.ONSET_TIME], Tablature.SRV_DEN) :
				new Rational(bnp[i][Transcription.ONSET_TIME_NUMER], bnp[i][Transcription.ONSET_TIME_DENOM]);
			onset = TimeMeterTools.round(onset, gridNums);
			Rational[] barMetPos = 
				ONLY_TAB || TAB_AND_TRANS ?
				tla.getMetricPosition((int) onset.mul(Tablature.SRV_DEN).toDouble()) : // multiplication necessary because of division when making onset above
				smtl.getMetricPosition(onset);
			Rational metPos = barMetPos[1];
			Rational offset = onset.add(dur);
			int bar = barMetPos[0].getNumer();
			int voice = LabelTools.convertIntoListOfVoices(trans.getVoiceLabels().get(i)).get(0); // TODO why .get(0)? 

			// currVoiceStrs and currVoiceInts start out as empty lists at i == 0,
			// and are populated with currStrs and currInts for the current note 
			// (+ any preceding rests) while iterating over bnp
			List<String[]> currVoiceStrs = voicesStrs.get(voice);
			List<Integer[]> currVoiceInts = voicesInts.get(voice);

			// When new bar is reached
			if (onset.isGreaterOrEqual(barEnd)) {
				// Increment barEnd and clear lists
				barEnd = (onset.sub(metPos)).add(Transcription.getMeter(bar, mi));
				naturalsAlreadyAdded.clear();
				accidentalsAlreadyAdded.clear();
				doubleFlatsInEffect.clear();
				flatsInEffect.clear();
				naturalsInEffect.clear();
				sharpsInEffect.clear();
				doubleSharpsInEffect.clear();

				// Update currKi and, if key signature changes, grids
				if (sectionBars.contains(bar)) {
					currKi = ki.get(sectionBars.indexOf(bar));
					if (currKi[currKi.length-1] == 1) {
						grids = PitchKeyTools.createGrids(
							currKi[Transcription.KI_KEY], currKi[Transcription.KI_MODE]
						);
					}
				}
			}

			if (verbose) {
				System.out.println("pitch                    " + pitch);
				System.out.println("dur                      " + dur);
				System.out.println("onset                    " + onset);
				System.out.println("metPos                   " + metPos);
				System.out.println("offset                   " + offset);	
				System.out.println("voice                    " + voice);
				System.out.println("bar                      " + bar);
				System.out.println("barEnd                   " + barEnd);
				System.out.println("midiPitchClass           " + (pitch % 12));
				System.out.println("currMpcGrid              " + Arrays.asList(grids.get(0)));
				System.out.println("currAltgrid              " + Arrays.asList(grids.get(1)));
				System.out.println("currPcGrid               " + Arrays.asList(grids.get(2)));
				System.out.println("------------------");	
			
				if (!Arrays.asList(grids.get(0)).contains(pitch % 12)) {
					List<String> pitches = Arrays.asList(
						"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "Bb", "B"
					);
					Rational mp = metPos;
					mp.reduce();
					pitchesNotInKey += pitches.get(pitch % 12) + " (MIDI " + pitch + "), bar " + 
						bar + ", voice " + voice + ", onset " + metPos + " not in key\r\n"; 
					if (!uniquePitchesNotInKey.contains(pitches.get(pitch % 12))) {
						uniquePitchesNotInKey.add(pitches.get(pitch % 12));
					}
				}
			}

			List<String[]> currStrs = new ArrayList<String[]>();
			List<Integer[]> currInts = new ArrayList<Integer[]>();
			String[] curr = new String[STRINGS.size()];
			// 1. Update data lists for rest(s) preceding the note at i (if applicable)
			Rational durPrev, metPosPrev, offsetPrev;
			// First note in voice?
			if (currVoiceStrs.size() == 0) {
				durPrev = Rational.ZERO;
				metPosPrev = Rational.ZERO;
				offsetPrev = Rational.ZERO;
			}
			else {
				// prevNote is the last item in currVoiceInts 
				// (1) whose onset is less than onset
				// (2) whose onset equals onset (if prevNote is a lower note in an in-voice chord)
				Integer[] prevNote = null;
				Rational onsetPrev = null;
				// NB currVoiceInts grows with every iteration through the i-loop, so starting at 
				// its end just means starting at the element added in the last i-loop iteration
				for (int j = currVoiceInts.size()-1; j >= 0; j--) {
					prevNote = currVoiceInts.get(j);
					onsetPrev = new Rational(
						prevNote[INTS.indexOf("onsetNum")], prevNote[INTS.indexOf("onsetDen")]
					);
					// If previous onset is less or equal than current (but is not a rest (onset = -1/-1))
					if (onsetPrev.getNumer() != -1 && onsetPrev.isLessOrEqual(onset)) {
						break;
					}
				}
				int durPrevInt = prevNote[INTS.indexOf("dur")]; 
				durPrev = 
					(durPrevInt > 0) ? new Rational(1, durPrevInt) :
					((durPrevInt == BREVE ? new Rational(2, 1) : new Rational(4, 1)));
				int dotsPrev = prevNote[INTS.indexOf("dots")];
				Rational dlf = TimeMeterTools.getDotLengtheningFactor(dotsPrev);
				durPrev = durPrev.add(durPrev.mul(dlf));
				metPosPrev = 
					ONLY_TAB || TAB_AND_TRANS ? 
						tla.getMetricPosition((int) onsetPrev.mul(Tablature.SRV_DEN).toDouble())[1] :
					smtl.getMetricPosition(onsetPrev)[1];
				offsetPrev = onsetPrev.add(durPrev);

				// If onsetPrev is within a triplet, durPrev and offsetPrev (needed to calculate 
				// rests) must be reverted from their tripletised value to their untripletised value
				//
				// To tripletise is to give a note its nominal (shown) value instead of its 
				// actual value by multiplying it with TRIPLETISER (3/2). 
				// Example for a half note:
				// untripletised = 1/3, 1/3, 1/3
				// tripletised = 1/2, 1/2, 1/2
				// E.g., 1/3 * 3/2 = 1/2; 1/6 * 3/2 = 1/4; etc.
				// Untripletised (real time value) variables calculated from prevNote: onsetPrev, metPosPrev
				// Tripletised (nominal time value) variables calculated from prevNote: durPrev, offsetPrev
				if (prevNote[INTS.indexOf("tripletOpen")] == 1 || prevNote[INTS.indexOf("tripletMid")] == 1 ||
					prevNote[INTS.indexOf("tripletClose")] == 1) {
					// Recalculate durPrev by multiplying it by DETRIPLETISER (2/3)
					durPrev = durPrev.mul(DETRIPLETISER);
					offsetPrev = onsetPrev.add(durPrev);
				}
			}
			Rational durRest = onset.sub(offsetPrev);
			if (durRest.isGreater(Rational.ZERO)) {
				Rational precedingInBar = metPos;
				boolean singleBarRestInSameBar = durRest.isLessOrEqual(precedingInBar);
				boolean singleBarRestInPrevBar = 
					precedingInBar.equals(Rational.ZERO) && 
					durRest.isLessOrEqual(Transcription.getMeter(bar-1, mi));
				// Single-bar rest
				if (singleBarRestInSameBar || singleBarRestInPrevBar) {
					Rational onsetRest = offsetPrev;
					Rational metPosRest;
					// Single-bar rest in the same bar
					if (singleBarRestInSameBar) {
						if (verbose) System.out.println("CASE: single-bar rest");
						metPosRest = 
							durRest.equals(precedingInBar) ? Rational.ZERO : // the bar starts with a rest
							(currVoiceStrs.size() == 0 ? Rational.ZERO : metPosPrev.add(durPrev));
					}
					// Single-bar rest in the previous bar (onset is 0/x)
					else {
						if (verbose) System.out.println("CASE: single-bar rest in previous bar");
						metPosRest = 
							currVoiceStrs.size() == 0 ? Rational.ZERO : metPosPrev.add(durPrev);	
//						metPosRest = // more complicated but also possible
//							currVoiceStrs.size() == 0 ? Rational.ZERO :
//							(ONLY_TAB || TAB_AND_TRANS ? 
//								tl.getMetricPosition((int) onsetRest.mul(Tablature.SRV_DEN).toDouble())[1] :
//							smtl.getMetricPosition(onsetRest)[1]);
					}					
					updateDataLists(
						currInts, currStrs, true, onsetRest, 
						i, iTab, curr, durRest, onsetRest, metPosRest, 
						mi, tripletOnsetPairs, gridVal
					);
				}
				// Multi-bar rest
				else {
					if (verbose) System.out.println("CASE: multi-bar rest");
					// Check how many bars the note spans and make subNoteDurs and 
					// subNoteDursOnsets (containing the onsets of the subnotes)
					List<Rational> subNoteDurs = new ArrayList<>();
					List<Rational> subNoteDursOnsets = new ArrayList<>(); 
					if (!precedingInBar.equals(Rational.ZERO)) {
						subNoteDurs.add(precedingInBar);
						subNoteDursOnsets.add(onset.sub(precedingInBar));
					}
					Rational remainder = durRest.sub(precedingInBar);
					int beginBar = 
						ONLY_TAB || TAB_AND_TRANS ? 
							tla.getMetricPosition((int) offsetPrev.mul(Tablature.SRV_DEN).toDouble())[0].getNumer() : 
						smtl.getMetricPosition(offsetPrev)[0].getNumer();
					for (int j = bar - 1; j >= beginBar; j--) {
						Rational currBarLen = Transcription.getMeter(j, mi);
						if (remainder.isGreaterOrEqual(currBarLen)) {
							subNoteDurs.add(currBarLen);
							// The onset of this subnote is onset of the previous subnote 
							// (i.e., the one added last to the list) minus the bar length
							subNoteDursOnsets.add(
								subNoteDursOnsets.size() == 0 ? onset.sub(currBarLen) : // no remainderInBar, so onset is at the beginning of the bar 
								subNoteDursOnsets.get(subNoteDursOnsets.size()-1).sub(currBarLen)
							);
							remainder = remainder.sub(currBarLen);
						}
						else {
							if (!remainder.equals(Rational.ZERO)) {
								subNoteDurs.add(remainder);
								// The onset of this subnote is onset of the previous subnote 
								// (i.e.,  the one added last to the list) minus the remainder
								// NB subNoteDursOnsets.size()-1 always exists: in the case of 
								// a multi-bar rest, either a rest with length remainingInBar 
								// or one with length barLength has already been added
								subNoteDursOnsets.add(
									subNoteDursOnsets.get(subNoteDursOnsets.size()-1).sub(remainder)
								);
							}
						}
					}
					Collections.reverse(subNoteDurs);
					Collections.reverse(subNoteDursOnsets);
					// For each subnote
					Rational currOnset = offsetPrev;
					Rational currMetPosRest = 
						(currVoiceStrs.size() == 0) ? Rational.ZERO : metPosPrev.add(durPrev);
					// If currMetPosRest equals the length of the bar before beginBar, this means 
					// that the rest starts at the beginning of beginBar, and that currMetPosRest
					// must be set to Rational.ZERO
					if (currMetPosRest.equals(Transcription.getMeter(beginBar-1, mi))) {
						currMetPosRest = Rational.ZERO;
					}
					for (int j = 0; j < subNoteDurs.size(); j++) {
						Rational currSubNoteDur = subNoteDurs.get(j);
						Rational currSubNoteDurOnset = subNoteDursOnsets.get(j);
						updateDataLists(
							currInts, currStrs, false, currSubNoteDurOnset, 
							i, iTab, curr, currSubNoteDur, currOnset, currMetPosRest, 
							mi, tripletOnsetPairs, gridVal);
						currOnset = currOnset.add(currSubNoteDur);
						currMetPosRest = Rational.ZERO;
					}
				}
			}

			// 2. Update data lists for the note at i
			// Determine pitch spelling and update accidentals lists
			List<List<Integer>> accidsInEffect = new ArrayList<>();
			accidsInEffect.add(doubleFlatsInEffect);
			accidsInEffect.add(flatsInEffect);
			accidsInEffect.add(naturalsInEffect);
			accidsInEffect.add(sharpsInEffect);
			accidsInEffect.add(doubleSharpsInEffect);
			List<Object> pitchSpell = PitchKeyTools.spellPitch(
				pitch, currKi[Transcription.KI_KEY], grids, accidsInEffect
			);
			String[] pa = (String[]) pitchSpell.get(0);

			List<List<Integer>> aie = (List<List<Integer>>) pitchSpell.get(1);
			doubleFlatsInEffect = aie.get(0);
			flatsInEffect = aie.get(1);
			naturalsInEffect = aie.get(2);
			sharpsInEffect = aie.get(3);
			doubleSharpsInEffect = aie.get(4);
			// a. Set pname, accid, oct
			String pname = pa[0];
			String accid = pa[1];
			String accidGes = pa[2];
			String oct = String.valueOf(PitchKeyTools.getOctave(pitch));
			curr[STRINGS.indexOf("pname")] = pname; 
			if (!accid.equals("")) {
				curr[STRINGS.indexOf("accid")] = accid;
			}
			if (addAccidGes) {
				if (!accidGes.equals("")) {
					// accid.ges overrules accid
					curr[STRINGS.indexOf("accid")] = null;
					curr[STRINGS.indexOf("accid.ges")] = accidGes;
				}
			}
			curr[STRINGS.indexOf("oct")] = oct;
			if (verbose) {
				System.out.println("pname                    " + pname);
				System.out.println("accid                    " + accid);
				System.out.println("oct                      " + oct);
			}			
			// b. Set tie, dur, dots
			Rational remainingInBar = barEnd.sub(onset);
			// Single-bar note
			if (dur.isLessOrEqual(remainingInBar)) {				
				if (verbose) System.out.println("CASE: single-bar note");				
				updateDataLists(
					currInts, currStrs, false, onset, 
					i, iTab, curr, dur, onset, metPos, 
					mi, tripletOnsetPairs, gridVal);
			}
			// Multi-bar note
			else {				
				if (verbose) System.out.println("CASE: multi-bar note");
				// Check how many bars the note spans and make subNoteDurs and 
				// subNoteDursOnsets (containing the onsets of the subnotes)
				List<Rational> subNoteDurs = new ArrayList<>();
				List<Rational> subNoteDursOnsets = new ArrayList<>();
				subNoteDurs.add(remainingInBar);
				subNoteDursOnsets.add(onset);
				subNoteDursOnsets.add(onset.add(remainingInBar));
				Rational remainder = dur.sub(remainingInBar);				
				// In the case of a tablature with predicted durations, those of the final chord
				// can be incorrectly predicted too long, thus extending beyond endOffset 
				if (offset.isGreater(endOffset)) {
					offset = endOffset;
				}
				int endBar = 
					(offset.equals(endOffset)) ? numBars : 
					(ONLY_TAB || TAB_AND_TRANS ?
						tla.getMetricPosition((int) offset.mul(Tablature.SRV_DEN).toDouble())[0].getNumer() :
					smtl.getMetricPosition(offset)[0].getNumer());
				for (int j = bar + 1; j <= endBar; j++) {
					Rational currBarLen = Transcription.getMeter(j, mi);
					if (remainder.isGreaterOrEqual(currBarLen)) {
						subNoteDurs.add(currBarLen);
						subNoteDursOnsets.add(
							subNoteDursOnsets.get(subNoteDursOnsets.size()-1).add(currBarLen)
						); 
						remainder = remainder.sub(currBarLen);
					}
					else {
						if (!remainder.equals(Rational.ZERO)) {
							subNoteDurs.add(remainder);
						}
					}
				}				
				// For each subnote
				Rational currOnset = onset;
				Rational currMetPos = metPos;
				for (int j = 0; j < subNoteDurs.size(); j++) {
					Rational currSubNoteDur = subNoteDurs.get(j);
					Rational currSubNoteDurOnset = subNoteDursOnsets.get(j);
					int lenBefore = currStrs.size();					
					updateDataLists(
						currInts, currStrs, false, currSubNoteDurOnset, 
						i, iTab, curr, currSubNoteDur, currOnset, currMetPos, 
						mi, tripletOnsetPairs, gridVal);					
					int subNoteSize = currStrs.size() - lenBefore;
					// Set subNoteInd to that of the last sub-subnote
					int subNoteInd = j == 0 ? currStrs.size() - 1 : // last element of updated list
						currStrs.size() - subNoteSize; // = first of updated part of the list
					String tie =
						j == 0 ? subNoteSize == 1 ? "i" : "m" :
						(j == subNoteDurs.size() - 1 ? (subNoteSize == 1 ? "t" : "m") : 
						"m");
					currStrs.get(subNoteInd)[STRINGS.indexOf("tie")] = tie;	// zondig
//					currStrs.get(subNoteInd)[STRINGS.indexOf("tie")] = "tie='" + tie + "'";
					currOnset = currOnset.add(currSubNoteDur);
					currMetPos = Rational.ZERO;
				}
			}

			// 3. Final note in the voice: complete data lists with rests if offset does not equal piece end 
			NotationVoice nv = p.getScore().get(voice).get(0);
			NotationChord lastNc = nv.get(nv.size()-1);
			if ((lastNc.getMetricTime().equals(onset)) && !offset.equals(endOffset)) {
				// Take into account any chords: add rest only after the highest-pitched note in lastNc 
				int pitchHighestChordNote = lastNc.get(lastNc.size() - 1).getMidiPitch();
				if (lastNc.size() == 1 || (lastNc.size() > 1 && pitchHighestChordNote == pitch)) {
					// Add rest to fill up current bar (if applicable) 
					Rational restCurrBar = barEnd.sub(offset);
					if (restCurrBar.isGreater(Rational.ZERO)) {
						Rational metPosRestCurrentBar = metPos.add(dur);
						updateDataLists(
							currInts, currStrs, false, offset, 
							-1, -1, new String[STRINGS.size()], restCurrBar, offset, metPosRestCurrentBar, 
							mi, tripletOnsetPairs, gridVal);
					}
					// Add bar rests for all remaining bars
					updateDataListsWithBarRests(currInts, currStrs, bar + 1, numBars);
				}
			}

			// 4. Add data lists to lists
			currVoiceStrs.addAll(currStrs);
			currVoiceInts.addAll(currInts);
		}
		if (verbose) {
			System.out.println(uniquePitchesNotInKey);
			System.out.println(pitchesNotInKey);
		}

//		// Add <xml:id>s
//		for (int v = 0; v < numVoices; v++) {
//			List<String[]> currVoiceStrs = voicesStrs.get(v);
//			List<Integer[]> currVoiceInts = voicesInts.get(v);
//			updateDataListsWithXMLIDs(currVoiceInts, currVoiceStrs, v);
//		}

		if (verbose) {
			int v = 0;
			List<String[]> currVoiceStrs = voicesStrs.get(v);
			List<Integer[]> currVoiceInts = voicesInts.get(v);
			for (int i = 0; i < currVoiceStrs.size(); i++) {
				System.out.println(
					Arrays.toString(currVoiceInts.get(i)) 
					+ "\n" + Arrays.toString(currVoiceStrs.get(i))
				);
			}
		}

		// Restructure voicesInts/voicesStrs into barsInts/barsStrs (i.e., per bar, voice, note)
		// List<List<List<T>>>			superlist: each element is a bar
		// List<List<T>>				sublist, bar-level: each element is a voice
		// List<T>						sub-sublist, voice-level: each element is a note
		List<List<List<Integer[]>>> barsInts = new ArrayList<>();
		List<List<List<String[]>>> barsStrs = new ArrayList<>();
		List<Integer> indsOfNewBar = new ArrayList<>(Collections.nCopies(numVoices, 0));
		for (int b = 1; b <= numBars; b++) {
			// Sublists, bar-level
			List<List<Integer[]>> barInts = new ArrayList<>();
			List<List<String[]>> barStrs = new ArrayList<>();
			for (int v = 0; v < numVoices; v++) {
				List<Integer[]> currVoiceInts = voicesInts.get(v);
				List<String[]> currVoiceStrs = voicesStrs.get(v);
				// Sublists, voice-level
				List<Integer[]> voiceInts = new ArrayList<>();
				List<String[]> voiceStrs = new ArrayList<>();
				for (int i = indsOfNewBar.get(v); i < currVoiceInts.size(); i++) {
					voiceInts.add(currVoiceInts.get(i));
					voiceStrs.add(currVoiceStrs.get(i));
					// If there is a next element and it is in the next bar
					if (i < currVoiceInts.size() - 1 && (currVoiceInts.get(i + 1)[INTS.indexOf("bar")] == b + 1)) {
						indsOfNewBar.set(v, i + 1);
						break;
					}
				}
				barInts.add(voiceInts);
				barStrs.add(voiceStrs);
			}
			barsInts.add(barInts);
			barsStrs.add(barStrs);
		}

		return Arrays.asList(barsInts, barsStrs);
	}


	/**
	 * Updates (adds to) the given {@code List<Integer[]>} and {@code List<String[]>} 
	 * with the data for the note(s)/rest(s) represented by the given arguments.
	 * 
	 * @param ints
	 * @param strs
	 * @param prepend
	 * @param onsetITO
	 * @param iGND
	 * @param iTabGND
	 * @param currGND
	 * @param argDurGND
	 * @param argOnsetGND
	 * @param argMetPosGND
	 * @param mi
	 * @param tripletOnsetPairs
	 * @param gridVal
	 */
	private static void updateDataLists(List<Integer[]> ints, List<String[]> strs, boolean prepend,
		Rational onsetITO, int iGND, int iTabGND, String[] currGND, Rational argDurGND, Rational argOnsetGND, 
		Rational argMetPosGND, List<Integer[]> mi, List<Rational[]> tripletOnsetPairs, Rational gridVal) {

		List<Boolean> tripletInfo = 
			tripletOnsetPairs == null ? null :					
			TimeMeterTools.isTripletOnset(tripletOnsetPairs, onsetITO);
		List<Object> noteData = getNoteData(
			currGND, iGND, iTabGND, argDurGND, argOnsetGND, argMetPosGND, 
			mi, tripletInfo, tripletOnsetPairs, gridVal
		);

		List<Integer[]> intsNote = (List<Integer[]>) noteData.get(0);
		List<String[]> strsNote = (List<String[]>) noteData.get(1);
		if (prepend) {
			ints.addAll(0, intsNote);
			strs.addAll(0, strsNote);
		}
		else {
			ints.addAll(intsNote);
			strs.addAll(strsNote);
		}		
	}


	/**
	 * Gets the note information for the note (or rest) at index i, given as input a String[] 
	 * containing only the attributes pname, oct, and accid (or only <code>null</code> in case
	 * of a rest). 
	 * 
	 * @param curr
	 * @param ind
	 * @param indTab
	 * @param argDur Is untripletised (has actual value).
	 * @param argOnset Is untripletised (has actual value).
	 * @param argMetPos Is untripletised (has actual value).
	 * @param mi
	 * @param tripletInfo
	 * @param tripletOnsetPairs
	 * @param gridVal
	 * @return <ul>
	 * 	       <li>As element 0: a {@code List<Integer[]>} containing, for each unit fraction the 
	 *             note (rest) can be divided into, an {@code Integer[]} containing the attributes 
	 *             as listed in {@code INTS}.</li>
	 *         <li>As element 1: a {@code List<String[]>} containing, for each unit fraction the 
	 *             note (rest) can be divided into, a {@code String[]} containing the attributes as 
	 *             listed in {@code STRINGS}.</li>
	 *         </ul>      
	 *         In case of a simple (non-dotted, non-compound) or dotted note (rest), the lists 
	 *         returned have only one element; in case of a non-dotted compound note (rest) (e.g.,  
	 *         a half tied to an eighth), the lists have more than one element.
	 */
	private static List<Object> getNoteData(String[] curr, int ind, int indTab, Rational argDur, 
		Rational argOnset, Rational argMetPos, List<Integer[]> mi, List<Boolean> tripletInfo, 
		List<Rational[]> tripletOnsetPairs, Rational gridVal) {

		List<Integer[]> intsNote = new ArrayList<>(); // for current note/rest
		List<String[]> strsNote = new ArrayList<>(); // for current note/rest

		// If the note or rest has a triplet onset time, argDur must be tripletised (i.e., 
		// given its nominal (shown) value instead of its actual value). As a consequence, 
		// uf and durAsRat are also tripletised. argOnset and argMetPos remain untripletised 
		// (i.e., keep their actual value), so currOnset and currMetPos are too
		if (tripletInfo != null && tripletInfo.contains(Boolean.TRUE)) {
			argDur = argDur.mul(TRIPLETISER);
		}
		List<Rational> uf = TimeMeterTools.getUnitFractions(argDur, gridVal);
		int numDots = TimeMeterTools.getNumDots(uf);
		boolean isSimple = (uf.size() == 1 && numDots == 0);
		boolean isDotted = numDots > 0;
		boolean isNonDottedCompound = (uf.size() > 1 && numDots == 0);
		boolean isRest = curr[STRINGS.indexOf("pname")] == null;

		Rational currOnset = argOnset;
		Rational currMetPos = argMetPos;
		// Iterate through the unit fractions and add to intsNote and strsNote. In the
		// case of a simple or dotted note, the for-loop breaks at the end of i = 0  
		for (int i = 0; i < uf.size(); i++) {
			Rational durAsRatUndotted = uf.get(i);
			Rational durAsRat = durAsRatUndotted;
			if (isDotted) {
				durAsRat = durAsRat.add(uf.get(1));
			}

			// Determine dur; allow for breve (2/1) and long (4/1)
			int dur = -1;
			Rational r = isSimple || isDotted ? durAsRatUndotted : durAsRat;
			if (r.isLessOrEqual(Rational.ONE)) {
				dur = r.getDenom();
			}
			else if (r.equals(new Rational(2, 1))) {
				dur = BREVE;
			}
			else if (r.equals(new Rational(4, 1))) {
				dur = LONG;
			}

			// Complete uf String[] with tie, dur, dots; add to strs
			String[] str = Arrays.copyOf(curr, curr.length);
			String[] strRemainder = null;
			if (isNonDottedCompound && !isRest) {
				str[STRINGS.indexOf("tie")] = 
					(i == 0 ? "i" : ((i > 0 && i < uf.size() - 1) ? "m" : "t"));
			}
			str[STRINGS.indexOf("dur")] = 
				(dur > 0 ? Integer.toString(dur) : (dur == BREVE ? "breve" : "long"));
			if (isDotted) {
				str[STRINGS.indexOf("dots")] = String.valueOf(numDots);
			}

			// Create uf Integer[] and initialise with zeros; fill with all but tripletOpen, 
			// tripletMid, tripletClose, and beamOpen and beamClose
			Integer[] in = new Integer[INTS.size()];
			Arrays.fill(in, 0);
			Integer[] inRemainder = null;
			in[INTS.indexOf("ind")] = !isRest ? ind : -1;
			in[INTS.indexOf("indTab")] = !isRest ? indTab : -1;
			in[INTS.indexOf("bar")] = TimeMeterTools.getMetricPosition(currOnset, mi)[0].getNumer();
			in[INTS.indexOf("onsetNum")] = currOnset.getNumer();
			in[INTS.indexOf("onsetDen")] = currOnset.getDenom();
			in[INTS.indexOf("metPosNum")] = currMetPos.getNumer();
			in[INTS.indexOf("metPosDen")] = currMetPos.getDenom();
			in[INTS.indexOf("dur")] = dur;
			in[INTS.indexOf("dots")] = numDots;

			// Add any triplet information
			List<Boolean> openMidClose = (tripletOnsetPairs == null) ? null : 
				TimeMeterTools.isTripletOnset(tripletOnsetPairs, currOnset);
			if (tripletOnsetPairs != null) {
				Rational[] top = TimeMeterTools.getExtendedTripletOnsetPair(
					currOnset, tripletOnsetPairs
				);
				if (top != null) {
					Rational currTripletOpenOnset = top[0];
					Rational metPosTripletOpen = TimeMeterTools.getMetricPosition(currTripletOpenOnset, mi)[1];
					Rational currTripletLen = top[3];
					Rational tripletBorder = metPosTripletOpen.add(currTripletLen);
					Rational onsetTripletBorder = currTripletOpenOnset.add(currTripletLen);
					Rational offset = currMetPos.add(durAsRat.mul(DETRIPLETISER));

					// If currOnset is the second part of a non-dotted compound note or rest that 
					// falls after tripletCloseOnset: set tripletCloseOnset to true 
					// NB: This does not apply to isSimple or isDotted, where there is no second part
					// E.g.: last H in voice 0 = tripletCloseOnset; last Q in voice 1, which
					// is tied to the W before, falls after it 
					// voice 0: W       H   H   H   H     |
					// voice 1: W       H.    W       (Q) |
					// (This happens with TabMapper in 5263_12_in_exitu_israel_de_egipto_desprez-3, bar 77, voice 3)
					if (isNonDottedCompound) {
						Rational[] topOnset = TimeMeterTools.getExtendedTripletOnsetPair(
							argOnset, tripletOnsetPairs
						);
						Rational tripletOpenOnset = (topOnset != null) ? topOnset[0] : null;			
						Rational tripletCloseOnset = (topOnset != null) ? topOnset[1] : null;
						if (topOnset != null && currOnset.isGreater(tripletCloseOnset) && 
							currOnset.isLess(tripletOpenOnset.add(currTripletLen))) {
							if (verbose) System.out.println("within triplet but after tripletCloseOnset --> tripletClose");
							openMidClose.set(2, true);
						}
					}

					// Handle triplet
					List<Object> triplet = handleTriplet(
						in, str, openMidClose, offset, tripletBorder, currMetPos,
						currTripletLen, onsetTripletBorder, gridVal, isRest
					);
					List<Integer[]> intsNoteTriplet = (List<Integer[]>) triplet.get(0);
					List<String[]> strsNoteTriplet = (List<String[]>) triplet.get(1);
					in = intsNoteTriplet.get(0);
					inRemainder = intsNoteTriplet.get(1);
					str = strsNoteTriplet.get(0);
					strRemainder = strsNoteTriplet.get(1);
				}
			}
			intsNote.add(in);
			strsNote.add(str);
			if (inRemainder != null) { // strRemainder is also non-null
				intsNote.add(inRemainder);
				strsNote.add(strRemainder);
			}
			if (isSimple || isDotted) {
				if (verbose) System.out.println("break: isSimple || isDotted");
				break;
			}
			else {
				if (openMidClose != null && openMidClose.contains(true)) {
					durAsRat = durAsRat.mul(DETRIPLETISER);
				}
				currOnset = currOnset.add(durAsRat);
				currMetPos = currMetPos.add(durAsRat);
			}
		}
		// If the last element in intsNote is a tripletClose: check if the elements up to 
		// tripletOpen are a single note occupying the whole tripletLen, all rests, or all 
		// tied notes. If so, replace the triplet value by its non-triplet value with tripletLen
		int indLast = intsNote.size()-1;
		Integer[] last = intsNote.get(indLast);
		if (last[INTS.indexOf("tripletClose")] == 1) {
			List<Boolean> rests = new ArrayList<>();
			List<Boolean> ties = new ArrayList<>();
			// Find accompanying tripletOpen
			int indOpen = -1;
			for (int j = indLast; j >= 0; j--) {
				boolean isTripletOpen = intsNote.get(j)[INTS.indexOf("tripletOpen")] == 1;
				// If the element at index j is a rest: add
				rests.add(strsNote.get(j)[STRINGS.indexOf("pname")] == null ? true : false);
				// If the element at index j is a tie: add
				String tieStr = strsNote.get(j)[STRINGS.indexOf("tie")]; 
				if (tieStr != null) {
					// In order for the triplet to be fully tied, the last element cannot be i;
					// the middle element cannot be i or t; and the first element cannot be t 
					boolean isTied = // zondig
						j == indLast && (tieStr.equals("m") || tieStr.equals("t")) ||
						(j < indLast && !isTripletOpen) && tieStr.equals("m") ||
						isTripletOpen && (tieStr.equals("i") || tieStr.equals("m"));
//					boolean isTied = 
//						j == indLast && (tieStr.equals("tie='m'") || tieStr.equals("tie='t'")) ||
//						(j < indLast && !isTripletOpen) && tieStr.equals("tie='m'") ||
//						isTripletOpen && (tieStr.equals("tie='i'") || tieStr.equals("tie='m'"));
					ties.add(isTied ? true : false);
				}
				if (isTripletOpen) {
					indOpen = j;
					break;
				}
			}
			// If indOpen is reached and the triplet consists of a single note occupying the 
			// whole tripletLen, of only rests, or of only tied notes: undo triplet
			if (indOpen != -1 && !rests.contains(false) || indOpen != -1 && !ties.contains(false)) {
				List<Object> undoneTriplet = undoTriplet(
					intsNote, strsNote, (ties.size() > 0 && !ties.contains(false)), indOpen, indLast
				);
				intsNote = (List<Integer[]>) undoneTriplet.get(0);
				strsNote = (List<String[]>) undoneTriplet.get(1);
			}
		}

		return Arrays.asList(new Object[]{intsNote, strsNote});
	}


	private static List<Object> handleTriplet(Integer[] in, String[] str, List<Boolean> openMidClose, 
		Rational offset, Rational tripletBorder, Rational currMetPos, Rational currTripletLen, 
		Rational onsetTripletBorder, Rational gridVal, boolean isRest) {

		boolean isTripletOpen = openMidClose.get(0);
		boolean isTripletMid = openMidClose.get(1);
		boolean isTripletClose = openMidClose.get(2);

		Integer[] inRemainder = null;
		String[] strRemainder = null;
		
		in[INTS.indexOf("tripletOpen")] = isTripletOpen ? 1 : 0;
		in[INTS.indexOf("tripletMid")] = isTripletMid ? 1 : 0;
		in[INTS.indexOf("tripletClose")] = isTripletClose ? 1 : 0;
		if (verbose) System.out.println(
			"is " + (isTripletOpen ? "tripletOpen" : (isTripletMid ? "tripletMid" : "tripletClose"))
		);

		// If note/rest ends on triplet border: set to tripletClose
		if (isTripletOpen || isTripletMid) {
			if (offset.equals(tripletBorder)) {
				if (verbose) System.out.println("ends on tripletBorder --> tripletClose");
				in[INTS.indexOf("tripletClose")] = 1;
			}
		}
		// If note/rest ends after triplet border: split at triplet border and 
		// set the first part to tripletClose and the second to tripletOpen
		if (offset.isGreater(tripletBorder)) {
			if (verbose) System.out.println("across triplet border --> split");
			Rational firstPart = tripletBorder.sub(currMetPos);
			Rational remainder = offset.sub(tripletBorder);
			List<Object> splitTriplet = splitAcrossTripletBorder(
				in, str, firstPart, remainder, currTripletLen, 
				tripletBorder, onsetTripletBorder, gridVal, isRest
			);
			List<Integer[]> intsNoteSplitTriplet = (List<Integer[]>) splitTriplet.get(0);
			List<String[]> strsNoteSplitTriplet = (List<String[]>) splitTriplet.get(1);
			in = intsNoteSplitTriplet.get(0);
			inRemainder = intsNoteSplitTriplet.get(1);
			str = strsNoteSplitTriplet.get(0);
			strRemainder = strsNoteSplitTriplet.get(1);
		}

		return Arrays.asList(new Object[]{
			Arrays.asList(new Integer[][]{in, inRemainder}), 
			Arrays.asList(new String[][]{str, strRemainder})}
		);
	}


	/**
	 * Splits a note (or rest) that crosses the triplet border in the part before the triplet 
	 * border (which is set to tripletClose) and the part after the triplet border (which is 
	 * set to tripletOpen).
	 * 
	 * NB: It is assumed that the splitting leads to no more complex notes than dotted notes.
	 * 
	 * @param in
	 * @param str
	 * @param firstPart
	 * @param remainder
	 * @param tripletLen
	 * @param tripletBorder
	 * @param onsetTripletBorder
	 * @param gridVal
	 * @param isRest
	 * 
	 * @return <ul>
	 * 		   <li>A List of two {@code Integer[]}, the first element of which represents the note (or rest)
	 *             before the triplet border, and the second the one after.</li>
	 *         <li>A List of two {@code String[]}, the first element of which represents the note (or rest) 
	 *             before the triplet border, and the second the one after.</li>
	 *         </ul>     
	 */
	private static List<Object> splitAcrossTripletBorder(Integer[] in, String[] str,
		Rational firstPart, Rational remainder, Rational tripletLen, Rational tripletBorder, 
		Rational onsetTripletBorder,  Rational gridVal, boolean isRest) {

		String[] afterStr = Arrays.copyOf(str, str.length);
		Integer[] afterIn = Arrays.copyOf(in, in.length);
		String strTie = str[STRINGS.indexOf("tie")];

		List<Integer[]> intsNote = new ArrayList<>();
		List<String[]> strsNote = new ArrayList<>();
		for (int i = 0; i < 2; i++) {
			String[] currStr = i == 0 ? str : afterStr;
			Integer[] currIn = i == 0 ? in : afterIn;
			Rational curr = i == 0 ? firstPart : remainder;

			int durMEICurr, numDotsCurr;
			if (!curr.equals(tripletLen)) {
				List<Rational> ufCurr = TimeMeterTools.getUnitFractions(curr.mul(TRIPLETISER), gridVal);
				durMEICurr = ufCurr.get(0).getDenom();
				numDotsCurr = TimeMeterTools.getNumDots(ufCurr);
			}
			else {
				curr.reduce();
				durMEICurr = curr.getDenom();
				numDotsCurr = 0;
			}

			// 1. Handle currIn
			// Adapt dur, dots, tripletOpen, tripletMid, tripletClose
			currIn[INTS.indexOf("dur")] = durMEICurr;
			if (numDotsCurr > 0 ) {
				currIn[INTS.indexOf("dots")] = numDotsCurr;
			}
			currIn[INTS.indexOf("tripletOpen")] = 0;
			currIn[INTS.indexOf("tripletMid")] = 0;
			currIn[INTS.indexOf("tripletClose")] = 0;
			if (!curr.equals(tripletLen)) {
				if (i == 0) {
					currIn[INTS.indexOf("tripletClose")] = 1;
				}
				else {
					currIn[INTS.indexOf("tripletOpen")] = 1;
				}
			}
			// Adapt onset, metPos
			if (i == 1) {
				Rational o = onsetTripletBorder;
				o.reduce();
				Rational m = tripletBorder;
				m.reduce();
				currIn[INTS.indexOf("onsetNum")] = o.getNumer();
				currIn[INTS.indexOf("onsetDen")] = o.getDenom();
				currIn[INTS.indexOf("metPosNum")] = m.getNumer();
				currIn[INTS.indexOf("metPosDen")] = m.getDenom();
			}

			// 2. Handle currStr
			// Adapt dur, dots, tie
			currStr[STRINGS.indexOf("dur")] = "dur='" + durMEICurr + "'";
			if (numDotsCurr > 0 ) {
				currStr[STRINGS.indexOf("dots")] = "dots='" + numDotsCurr + "'";
			}
			if (!isRest) {
				if (i == 0) {
					// If str has 
					// (a) tie='i' or 'm': OK (remainder set to 'm')
					// (b) tie='t': NOK, set to 'm' (remainder set to 't')
					if (strTie.equals("t")) { // zondig
//					if (strTie.equals("tie='t'")) {
						currStr[STRINGS.indexOf("tie")] = "m"; // zondig
//						currStr[STRINGS.indexOf("tie")] = "tie='m'";
					}
				}
				else {
					// If str has 
					// (a) tie='i' or 'm': NOK, set to 'm'
					// (b) tie='t': OK
					if (strTie.equals("i")) { // zondig
//					if (strTie.equals("tie='i'")) {
						currStr[STRINGS.indexOf("tie")] = "m"; // zondig
//						currStr[STRINGS.indexOf("tie")] = "tie='m'";
					}
				}
			}

			intsNote.add(currIn);
			strsNote.add(currStr);
		}

		return Arrays.asList(new Object[]{intsNote, strsNote});
	}


	private static List<Object> undoTriplet(List<Integer[]> ints, List<String[]> strs,
		boolean allTied, int indOpen, int indLast) {

		if (verbose) System.out.println("rest/note of tripletLen replaced by untripletised version");
		boolean isTripletLen = indOpen == indLast;

		Rational tripletisedDur = Rational.ZERO;
		for (int j = indOpen; j <= indLast; j++) {
			int durAsInt = ints.get(j)[INTS.indexOf("dur")];
			tripletisedDur = tripletisedDur.add(
				(durAsInt > 0) ? new Rational(1, durAsInt) : // whole or shorter
				new Rational(durAsInt*-2, 1) // breve or long		
			);
		}
		// If the rest/note is not of tripletLen, it must be detripletised (given 
		// its actual value)
		if (!isTripletLen) {
			tripletisedDur = tripletisedDur.mul(DETRIPLETISER);
		}

		// 1. Change items at indOpen
		ints.get(indOpen)[INTS.indexOf("dur")] = tripletisedDur.getDenom();
		ints.get(indOpen)[INTS.indexOf("dots")] = 0;
		ints.get(indOpen)[INTS.indexOf("tripletOpen")] = 0;
		if (isTripletLen) {
			ints.get(indOpen)[INTS.indexOf("tripletClose")] = 0;
		}
		strs.get(indOpen)[STRINGS.indexOf("dur")] = "dur='" + tripletisedDur.getDenom() + "'";
		strs.get(indOpen)[STRINGS.indexOf("dots")] = null;
		if (allTied) {
			// Tie for the first element (which can only be i or m) must be retained 
			// only if tie for the last element (which can only be m or t) is m
			if (strs.get(indLast)[STRINGS.indexOf("tie")].equals("t")) { // zondig
//			if (strs.get(indLast)[STRINGS.indexOf("tie")].equals("tie='t'")) {	
				strs.get(indOpen)[STRINGS.indexOf("tie")] = null;
			}
		}

		// 2. Remove items at indices after indOpen
		ints = ints.subList(0, indOpen+1);
		strs = strs.subList(0, indOpen+1);

		return Arrays.asList(new Object[]{ints, strs});
	}


	/**
	 * Updates (adds to) the given {@code List<Integer[]>} and {@code List<String[]>} 
	 * with the data for bar rests for the given start and end bar.
	 * 
	 * @param ints
	 * @param strs
	 * @param startBar
	 * @param endBar
	 */
	private static void updateDataListsWithBarRests(List<Integer[]> ints, List<String[]> strs, 
		int startBar, int endBar) {
		for (int b = startBar; b <= endBar; b++) {
			String[] restStr = new String[STRINGS.size()];
			restStr[STRINGS.indexOf("dur")] = "dur='bar'";
			strs.add(restStr);
			Integer[] restInt = new Integer[INTS.size()];
			Arrays.fill(restInt, -1);
			restInt[INTS.indexOf("bar")] = b;
			restInt[INTS.indexOf("metPosNum")] = 0;
			restInt[INTS.indexOf("metPosDen")] = 1;
			restInt[INTS.indexOf("dur")] = -1; // n/a (bar rest)
			restInt[INTS.indexOf("dots")] = -1; // n/a (bar rest)
			ints.add(restInt);
		}
	}


	/**
	 * Updates (adapts) the given {@code List<String[]>} with the {@code xml:id}s for each 
	 * list element (note/rest). The {@code xml:id} is a String that consists of
	 * <ul>
	 * <li>In the case of a rest: {@code 'r<v>.<b>.<s>.<mp>'}</li>
	 * <li>In the case of a note: {@code 'n<i>.<v>.<b>.<s>.<po>.<mp>'}</li>
	 * </ul>
	 * where {@code <i>} indicates the note index (in the piece), {@code <v>} the voice number, 
	 * {@code <b>} the bar number, {@code <s>} the sequence number in the bar, {@code <po>} the 
	 * pitch + octave, and {@code <mp>} the metric position in the bar.
	 * 
	 * @param ints
	 * @param strs
	 * @param v
	 */
	private static void updateDataListsWithXMLIDs(List<Integer[]> ints, List<String[]> strs, int v) {		
		int initBar = ints.get(0)[INTS.indexOf("bar")];
		int seq = 0;
		for (int i = 0; i < strs.size(); i++) {
			String[] currStr = strs.get(i);
			Integer[] currIn = ints.get(i);
			String pitch = currStr[STRINGS.indexOf("pname")];
			Rational mp = new Rational(
				currIn[INTS.indexOf("metPosNum")], currIn[INTS.indexOf("metPosDen")]);
			mp.reduce();
			int currBar = currIn[INTS.indexOf("bar")]; 
			if (currBar > initBar) {
				seq = 0;
				initBar = currBar;
			}

			currStr[STRINGS.indexOf("xml:id")] = 
				pitch != null ? 
					makeNoteXMLID(
						currIn[INTS.indexOf("ind")], v, currBar, seq, pitch, 
						currStr[STRINGS.indexOf("oct")], mp 
					) : 
					makeRestXMLID(v, currBar, seq, mp);
			seq++;
		}
	}


	private static List<Object> beam(/*Tablature tab,*/ List<Object> data, List<Integer[]> mi, // dimmid
		List<Rational[]> tripletOnsetPairs, List<List<Integer>> mismatchInds, Map<String, String> paths, 
		int numVoices) {
//		System.out.println(">>> beam() called");

		// ints and strs are organised per bar, voice, note
		List<List<List<Integer[]>>> ints = (List<List<List<Integer[]>>>) data.get(0);
		List<List<List<String[]>>> strs = (List<List<List<String[]>>>) data.get(1);
		int numBars = ints.size();

		// 1. Make unbeamed, organised per voice, bar (for the python beaming script)
		List<List<String>> unbeamed = new ArrayList<>();
		for (int i = 0; i < numVoices; i++) {
			unbeamed.add(new ArrayList<String>(Arrays.asList(new String[]{"voice=" + i + "\n"}))); // WOENS
		}
		// Populate unbeamed
//		Timeline tla = TAB_AND_TRANS ? tab.getEncoding().getTimelineAgnostic() : null; // dimmid
		for (int i = 0; i < numBars; i++) {
			int bar = i + 1;
			Rational currMeter = Transcription.getMeter(bar, mi);
			if (verbose) System.out.println("bar = " + (i+1));
			List<List<Integer[]>> currBarInts = ints.get(i);
			List<List<String[]>> currBarStrs = strs.get(i);
			for (int j = 0; j < numVoices; j++) {
				if (verbose) System.out.println("voice = " + j);
				StringBuilder barListSb = new StringBuilder();
				barListSb.append("meter='" + currMeter.getNumer() + "/" + currMeter.getDenom() + "'" + "\n"); // WOENS
				List<String> barList = getBar( // dimmid
					currBarInts.get(j), currBarStrs.get(j), tripletOnsetPairs, 
					mismatchInds, j
					/*, (TAB_AND_TRANS ? tla.getDiminution(bar) : 1)*/
				);
				barList.forEach(s -> barListSb.append(s + "\n")); // WOENS
				unbeamed.get(j).add(barListSb.toString());
			}
		}
		// Store unbeamed as text file // TODO find cleaner solution
		// NB: the stored file ends with a line break
		String psp = StringTools.getPathString(Arrays.asList(paths.get("UTILS_PYTHON_PATH")));
//		String psp = getPythonPath();
		String fName = psp + "unbeamed.txt";
		File f = new File(fName);
		StringBuilder sb = new StringBuilder();
		unbeamed.forEach(l -> l.forEach(sb::append));
		ToolBox.storeTextFile(sb.toString(), f);

		// 2. Run beaming script; delete stored file
		// NB: the output of the beaming script does not end with a line break, but 
		// PythonInterface.runPythonFileAsScript() adds one to the end of it
		String python = PythonInterface.python2Installed() ? "python3" : "python";
		String beamedStr = PythonInterface.runPythonFileAsScript(
			new String[]{python, psp + paths.get("BEAM_SCRIPT"), fName}
		);
//		String beamedStr = "";
//		try {
//			beamed = PythonInterface.runPythonFileAsScript(
//				new String[]{"python", pythonScriptPath + "beam.py", notesFileName}
//			);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		f.delete();

		// 3. Make beamed, organised per bar, voice (for completion of ints )
		List<List<List<String>>> beamed = new ArrayList<>();
		for (int i = 0; i < numBars; i++) {
			beamed.add(new ArrayList<>());
			for (int j = 0; j < numVoices; j++) {
				beamed.get(i).add(new ArrayList<>());
			}
		}
		// Populate beamed
		for (int i = 0; i < numVoices; i++) {
			List<String> barsCurrVoice = Arrays.asList(
				(Arrays.asList(beamedStr.split("end of voice" + "\n"))) // WOENS
				.get(i).split("end of bar" + "\n")); // WOENS
			for (int j = 0; j < numBars; j++) {
				beamed.get(j).get(i).addAll(Arrays.asList(barsCurrVoice.get(j).split("\n"))); // WOENS
			}
		}

		// 3. Complete ints with beamOpen and beamClose
		for (int bar = 0; bar < beamed.size(); bar++) {
			List<List<String>> voicesCurrBar = beamed.get(bar);
			for (int voice = 0; voice < voicesCurrBar.size(); voice++) {
				List<String> notesCurrBarCurrVoice = voicesCurrBar.get(voice);
				for (int note = 0; note < notesCurrBarCurrVoice.size(); note++) {
					String noteCurrBarCurrVoice = notesCurrBarCurrVoice.get(note);
					if (noteCurrBarCurrVoice.startsWith("<beam>")) {
						ints.get(bar).get(voice).get(note)[INTS.indexOf("beamOpen")] = 1;
					}
					if (noteCurrBarCurrVoice.endsWith("</beam>")) {
						ints.get(bar).get(voice).get(note)[INTS.indexOf("beamClose")] = 1;
					}
				}
			}
		}

		return Arrays.asList(ints, strs);
	}


	/**
	 * Returns the XML for the bar represented by the given lists <code>String[]</code> and 
	 * <ode>Integer[]</code>.
	 * 
	 * @param ints
	 * @param strs
	 * @param tripletOnsetPairs
	 * @param mismatchInds
	 * @param voice
	 * @param diminution
	 * @return
	 */
	private static List<String> getBar(List<Integer[]> ints, List<String[]> strs, List<Rational[]> 
		tripletOnsetPairs, List<List<Integer>> mismatchInds, int voice/*, int diminution*/) { // dimmid
		List<String> barList = new ArrayList<>();

		boolean highlightNotes = mismatchInds.size() != 0;
		boolean isMappingCase = highlightNotes && mismatchInds.get(0) == null;

		String barRestStr = TAB.repeat(2) + makeOpeningTag("mRest", true, 
			new String[][]{
				{"xml:id", addUniqueID("mr", xmlIDs).get(xmlIDs.size() - 1)}
			}
		);

		// If the bar does not contain any notes/rests (this happens when a voice
		// ends with one or more bars of rests)
		if (ints.size() == 0) {
			barList.add(barRestStr);
		}
		// If the bar contains notes/rests
		else {
			boolean isFullBarRest = !(
				ToolBox.getItemsAtIndex(strs, STRINGS.indexOf("pname"))
				.stream().anyMatch(s -> s != null)
			);

			if (isFullBarRest) {
				barList.add(barRestStr);
			}
			else {
				String noteStr;
				boolean chordActive = false;
				// For each note/rest
				int indentsAdded = 0;
				for (int i = 0; i < strs.size(); i++) {
					String[] currStr = strs.get(i);
					Integer[] currIn = ints.get(i);
					int currDur = currIn[INTS.indexOf("dur")];
					int currDots = currIn[INTS.indexOf("dots")];
					Rational currOnset = new Rational(
						currIn[INTS.indexOf("onsetNum")], currIn[INTS.indexOf("onsetDen")]
					);
					Rational nextOnset = 
						(i == strs.size()-1) ? null : 
						new Rational(
							ints.get(i+1)[INTS.indexOf("onsetNum")], ints.get(i+1)[INTS.indexOf("onsetDen")]
						);

					// Check for any <beam> to be added before <note>
					if (currIn[INTS.indexOf("beamOpen")] == 1) {
						barList.add(TAB.repeat(2) + makeOpeningTag("beam", false, 
							new String[][]{
								{"xml:id", addUniqueID("b", xmlIDs).get(xmlIDs.size() - 1)},
							}
						)); 
						indentsAdded++;
					}
					// Check for any <chord> to be added before <note>
					if ((i < strs.size()-1) && (currOnset.equals(nextOnset) && !chordActive)) {
						barList.add(TAB.repeat(2 + indentsAdded) + makeOpeningTag("chord", false, 
							new String[][]{
								{"xml:id", addUniqueID("c", xmlIDs).get(xmlIDs.size() - 1)},	
								{"dur", String.valueOf(currDur)}
							}
						));
						chordActive = true;
						indentsAdded++;
					}
					// Check for any <tuplet> to be added before <note>
					if (currIn[INTS.indexOf("tripletOpen")] == 1) {
						int tupletDur = -1;
						for (Rational[] r : tripletOnsetPairs) {
							if (currOnset.equals(r[0])) {
								// r[2] gives the RhythmSymbol duration and must be converted to CMN 
								// equivalent (e.g., minim = 24; 24 * 1/96 = 1/4) 
								Rational tupletDurRat = r[2].mul(Tablature.SMALLEST_RHYTHMIC_VALUE);
//								// Apply diminution and get XML dur value 
//								tupletDurRat = // dimmid
//									(diminution > 0) ? tupletDurRat.mul(diminution) :
//									tupletDurRat.div(diminution);
								tupletDur = getXMLDur(tupletDurRat);
								break;
							}
						}
						barList.add(TAB.repeat(2 + indentsAdded) + makeOpeningTag("tuplet", false, 	
							new String[][]{
								{"xml:id", addUniqueID("t", xmlIDs).get(xmlIDs.size() - 1)},	
								{"dur", String.valueOf(tupletDur)},
								{"num", "3"},
								{"numbase", "2"},
							}
						));
						indentsAdded++;
					}
					// Make rest
					if (currStr[STRINGS.indexOf("pname")] == null) {
						noteStr = TAB.repeat(2 + indentsAdded) + makeOpeningTag("rest", true, 
							new String[][]{
								{"xml:id", addUniqueID("r", xmlIDs).get(xmlIDs.size() - 1)},
								{"dur", String.valueOf(currDur)},
								currDots != 0 ? new String[]{"dots", String.valueOf(currDots)} : null,
//								{"xml:id", currStr[STRINGS.indexOf("xml:id")]},
							}
						);
					}
					// Make note
					else {
						List<String[]> attsList = new ArrayList<>();
						attsList.add(new String[]{"xml:id", addUniqueID("n", xmlIDs).get(xmlIDs.size() - 1)});
						for (int j = 0; j < currStr.length; j++) {
							String s = currStr[j];
							if (s != null) {
								attsList.add(new String[]{STRINGS.get(j), s});
							}
						}
						int tabInd = currIn[INTS.indexOf("indTab")];
						List<String> clrs;
						if (isMappingCase) {
							clrs = Arrays.asList(new String[]{null, "blue", "lime", "orange", "red"});
							for (int j : Arrays.asList(Transcription.ORNAMENTATION_IND, 
								Transcription.REPETITION_IND, Transcription.FICTA_IND,Transcription.ADAPTATION_IND)) {
								if (mismatchInds.get(j).contains(tabInd)) {
									attsList.add(new String[]{"color", clrs.get(j)});
									break;
								}
							}
						}
						else {
							if (highlightNotes) {
								clrs = Arrays.asList(new String[]{null, "red", "orange", "lime", "blue"});
								int ind = (tabInd != -1) ? tabInd : currIn[INTS.indexOf("ind")];
								// Loops over ErrorCalculator.INCORRECT_VOICE, ErrorCalculator.OVERLOOKED_VOICE, 
								// ErrorCalculator.SUPERFLUOUS_VOICE, ErrorCalculator.HALF_VOICE
								for (int j : Arrays.asList(1, 2, 3, 4)) {
									if (mismatchInds.get(j).contains(ind)) {
										attsList.add(new String[]{"color", clrs.get(j)});
										break;
									}
								}
							}
						}
						noteStr = TAB.repeat(2 + indentsAdded) + makeOpeningTag("note", true, 
							attsList.stream().toArray(String[][]::new)
						);
					}
					barList.add(noteStr);

					// Close any active <tuplet>, <chord>, or <beam>
					if (currIn[INTS.indexOf("tripletClose")] == 1) {
						indentsAdded--;
						barList.add(TAB.repeat(2 + indentsAdded) + makeClosingTag("tuplet"));
					}
					if (chordActive && ((i < strs.size()-1 && nextOnset.isGreater(currOnset)) || i == strs.size()-1)) {
						chordActive = false;
						indentsAdded--;
						barList.add(TAB.repeat(2 + indentsAdded) + makeClosingTag("chord"));
					}
					if (currIn[INTS.indexOf("beamClose")] == 1) {
						indentsAdded--;
						barList.add(TAB.repeat(2 + indentsAdded) + makeClosingTag("beam"));
					}
				}
			}
		}

		return barList;
	}


	private static List<List<String>> getTransBars(/*Tablature tab,*/ List<Object> data, // dimmid
		List<Rational[]> tripletOnsetPairs, List<List<Integer>> mismatchInds, int numVoices) {
//l		System.out.println("\r\n>>> getTransBars() called");
		List<List<String>> transBars = new ArrayList<>();

		List<List<List<Integer[]>>> ints = (List<List<List<Integer[]>>>) data.get(0);
		List<List<List<String[]>>> strs = (List<List<List<String[]>>>) data.get(1);
		int numBars = ints.size();

		List<Integer[]> sls = new ArrayList<>();
		ToolBox.getRange(0, numVoices).forEach(i -> 
			sls.add(getStaffAndLayer(
				false, Arrays.asList(ONLY_TAB, TAB_AND_TRANS), TAB_ON_TOP, GRAND_STAFF, numVoices, i)
			)
		);

		// For each bar
//		Timeline tla = tab.getEncoding().getTimelineAgnostic(); // dimmid
		for (int i = 0; i < numBars; i++) {
			List<String> currBarAsXML = new ArrayList<>();
			if (verbose) System.out.println("bar = " + i + 1);
			List<List<Integer[]>> currBarInts = ints.get(i);
			List<List<String[]>> currBarStrs = strs.get(i);
			// For each voice
			for (int j = 0; j < numVoices; j++) {
				if (verbose) System.out.println("voice = " + j);
				int staff = sls.get(j)[0];
				int layer = sls.get(j)[1];

				// Add opening <staff> and <layer>
				// Grand staff case: only add if j is first voice (upper staff) or if  
				// previous voice has staff-1 (lower staff) 
				if (!GRAND_STAFF || (GRAND_STAFF && (j == 0 || (j > 0 && sls.get(j-1)[0] == staff-1)))) {
					currBarAsXML.add(makeOpeningTag("staff", false, 
						new String[][]{
							{"xml:id", addUniqueID("s", xmlIDs).get(xmlIDs.size() - 1)},
							{"n", String.valueOf(staff)}
						}
					));
				}
				currBarAsXML.add(TAB + makeOpeningTag("layer", false, 
					new String[][]{
						{"xml:id", addUniqueID("l", xmlIDs).get(xmlIDs.size() - 1)},
						{"n", String.valueOf(layer)}
					}
				));

				// Add bar <note>s and <rest>s
				currBarAsXML.addAll(getBar( // dimmid
					currBarInts.get(j), currBarStrs.get(j), tripletOnsetPairs, mismatchInds, j
					/*, diminution*/
				));

				// Add closing <layer> and <staff>
				currBarAsXML.add(TAB + makeClosingTag("layer"));
				// Grand staff case: only add if next voice has staff+1 (upper staff) or if 
				// j is last voice (lower staff)
				if (!GRAND_STAFF || (GRAND_STAFF && ((j < numVoices-1 && sls.get(j+1)[0] == staff+1) || j == numVoices-1))) {
					currBarAsXML.add(makeClosingTag("staff"));
				}
			}
			transBars.add(currBarAsXML);
		}

		return transBars;
	}


	/**
	 * Adapts the barring of the given keyInfo to that of the given <code>Tablature</code>.
	 * 
	 * @param tab
	 * @param ki
	 * @return
	 */
	// TESTED
	static List<Integer[]> rebarKeyInfo(Tablature tab, List<Integer[]> ki) {
		Timeline tla = tab.getEncoding().getTimelineAgnostic();
		List<Integer[]> miAgn = tab.getMeterInfoAgnostic();
		for (int i = 0; i < ki.size(); i++) {
			Integer[] in = ki.get(i);
			int mtFirstBarInKi = new Rational(
				in[Transcription.KI_NUM_MT_FIRST_BAR],
				in[Transcription.KI_DEN_MT_FIRST_BAR]
			).mul(Tablature.SRV_DEN).getNumer();
			int firstBar = tla.getMetricPosition(mtFirstBarInKi)[0].getNumer();
			int lastBar;
			if (i < ki.size() - 1) {
				Integer[] inNext = ki.get(i + 1);
				int mtFirstBarInKiNext = new Rational(
					inNext[Transcription.KI_NUM_MT_FIRST_BAR],
					inNext[Transcription.KI_DEN_MT_FIRST_BAR]
				).mul(Tablature.SRV_DEN).getNumer();
				lastBar = tla.getMetricPosition(mtFirstBarInKiNext)[0].getNumer() - 1;
			}
			else {
				lastBar = miAgn.get(miAgn.size() - 1)[Transcription.MI_LAST_BAR];
			}
			in[Transcription.KI_FIRST_BAR] = firstBar;
			in[Transcription.KI_LAST_BAR] = lastBar;
		}

		return ki;
	}


	/**
	 * Aligns the given meterInfo and keyInfo so that each section is represented in both.
	 *  
	 * @param mi
	 * @param ki
	 * @return The aligned meterInfo and KeyInfo, with one value added to each element, 
	 *         indicating whether (1) or not (0) at the metric time for that element a 
	 *         meter change (meterInfo case) or key change (keyInfo case) occurs. If not,
	 *         the appropriate values from the previous element are copied (i.e., all 
	 *         values but those relating to bar and metric time).
	 */
	// TESTED
	static List<List<Integer[]>> alignMeterAndKeyInfo(List<Integer[]> mi, List<Integer[]> ki) {		
		List<Integer[]> miAligned = new ArrayList<>();
		List<Integer[]> kiAligned = new ArrayList<>();

		List<Integer> miBars = ToolBox.getItemsAtIndex(mi, Transcription.MI_FIRST_BAR);
		List<Integer> kiBars = ToolBox.getItemsAtIndex(ki, Transcription.KI_FIRST_BAR);
		List<Integer> sectionBars = new ArrayList<>();
		sectionBars.addAll(ToolBox.getItemsAtIndex(mi, Tablature.MI_FIRST_BAR));
		sectionBars.addAll(ToolBox.getItemsAtIndex(ki, Transcription.KI_FIRST_BAR));
		sectionBars = sectionBars.stream().distinct().collect(Collectors.toList());
		Collections.sort(sectionBars);
		int lastBar = mi.get(mi.size()-1)[Tablature.MI_LAST_BAR];
		for (int i = 0; i < sectionBars.size(); i++) {
			int currFirstBar = sectionBars.get(i);
			int currLastBar = i < sectionBars.size() - 1 ? (sectionBars.get(i+1) - 1) : lastBar;

			Integer[] miIn;
			if (miBars.contains(currFirstBar)) {
				miIn = mi.get(miBars.indexOf(currFirstBar));
				// Extend with meter change
				List<Integer> miInAsList = new ArrayList<Integer>(Arrays.asList(miIn));
				miInAsList.add(1);
				miIn = miInAsList.toArray(new Integer[0]);
			}
			else {
				miIn = ki.get(kiBars.indexOf(currFirstBar));
				Integer[] miInPrev = miAligned.get(i-1);
				// Extend with diminution and meter change
				List<Integer> miInAsList = new ArrayList<Integer>(Arrays.asList(miIn));
				miInAsList.add(miInPrev[Tablature.MI_DIM]);
				miInAsList.add(0);
				miIn = miInAsList.toArray(new Integer[0]);
				// Adapt meter to values in miInPrev
				miIn[Tablature.MI_NUM] = miInPrev[Tablature.MI_NUM];
				miIn[Tablature.MI_DEN] = miInPrev[Tablature.MI_DEN];
			}
			miIn[Tablature.MI_LAST_BAR] = currLastBar;
			miAligned.add(miIn);

			Integer[] kiIn;
			if (kiBars.contains(currFirstBar)) {
				kiIn = ki.get(kiBars.indexOf(currFirstBar));
				// Extend with key change
				List<Integer> kiInAsList = new ArrayList<Integer>(Arrays.asList(kiIn));
				kiInAsList.add(1);
				kiIn = kiInAsList.toArray(new Integer[0]);
			}
			else {
				kiIn = mi.get(miBars.indexOf(currFirstBar));
				Integer[] kiInPrev = kiAligned.get(i-1);
				// Remove diminution and extend with key change
				List<Integer> kiInAsList = new ArrayList<Integer>(Arrays.asList(kiIn));
				if (ONLY_TAB || TAB_AND_TRANS) {
					kiInAsList.remove(Tablature.MI_DIM); // olja 10.12
				}
				kiInAsList.add(0);
				kiIn = kiInAsList.toArray(new Integer[0]);
				// Adapt key to values in kiInPrev
				kiIn[Transcription.KI_KEY] = kiInPrev[Transcription.KI_KEY];
				kiIn[Transcription.KI_MODE] = kiInPrev[Transcription.KI_MODE];
			}
			kiIn[Transcription.KI_LAST_BAR] = currLastBar;
			kiAligned.add(kiIn);
		}

		return Arrays.asList(miAligned, kiAligned);
	}


	/**
	 * Gets the staff and layer in the given scenario. Both staff and layer are 1-based and 
	 * are counted from the top (as is practice in MEI).
	 * 
	 * @param tablatureStaff
	 * @param cases onlyTab, tabAndTrans, or onlyTrans
	 * @param tabOnTop
	 * @param grandStaff
	 * @param numVoices
	 * @param voice
	 * @return An {@code Integer[]} containing
	 *         <ul>
	 *         <li>As element 0: the staff.</li>
	 *         <li>As element 1: the layer.</li>
	 *         </ul>
	 */
	// TESTED
	static Integer[] getStaffAndLayer(boolean tablatureStaff, List<Boolean> cases, 
		boolean tabOnTop, boolean grandStaff, int numVoices, int voice) {

		boolean onlyTab = cases.get(0);
		boolean tabAndTrans = cases.get(1);
		boolean onlyTrans = onlyTab == false && tabAndTrans == false;

		// Options: onlyTab; tabAndTrans && tabOnTop; tabAndTrans && !tabOnTop.
		// Value of grandStaff is only relevant if tabAndTrans && !tabOnTop
		if (tablatureStaff) {
			int staff;
			int layer = 1;
			// If tablature staff is upper staff
			if (onlyTab || (tabAndTrans && tabOnTop)) {
				staff = 1;
			}
			// If tablature staff is lower staff
			else {
				staff = grandStaff ? 2 + 1 : numVoices + 1;
			}
			return new Integer[]{staff, layer};
		}
		// Options: onlyTrans; tabAndTrans && tabOnTop; tabAndTrans && !tabOnTop
		// Value of grandStaff is always relevant
		else {
			int staff;
			int layer;
			// If transcription staves are upper staves
			if (onlyTrans || tabAndTrans && !tabOnTop) {
				if (grandStaff) {
					Integer[] sl = getStaffAndLayerGrandStaff(numVoices)[voice];
					staff = sl[0];
					layer = sl[1];
				}
				else {
					staff = voice + 1;
					layer = 1;
				}
			}
			// If transcription staves are lower staves
			else {
				if (grandStaff) {
					Integer[] sl = getStaffAndLayerGrandStaff(numVoices)[voice];
					staff = sl[0] + 1;
					layer = sl[1];
				}
				else {
					staff = (voice + 1) + 1;
					layer = 1;
				}
			}
			return new Integer[]{staff, layer};
		}
	}


	static Integer[][] getStaffAndLayerGrandStaff(int numVoices) {
		Integer[][] staffAndLayer = new Integer[numVoices][2];
		// Each element represents a voice and contains the staff and the layer for it
		if (numVoices == 2) {
			staffAndLayer[0] = new Integer[]{1, 1};
			staffAndLayer[1] = new Integer[]{2, 1};
		}
		else if (numVoices == 3) {
			staffAndLayer[0] = new Integer[]{1, 1};
			staffAndLayer[1] = new Integer[]{2, 1};
			staffAndLayer[2] = new Integer[]{2, 2};
		}
		else if (numVoices == 4) {
			staffAndLayer[0] = new Integer[]{1, 1};
			staffAndLayer[1] = new Integer[]{1, 2};
			staffAndLayer[2] = new Integer[]{2, 1};
			staffAndLayer[3] = new Integer[]{2, 2};
		}
		else if (numVoices == 5) {
			staffAndLayer[0] = new Integer[]{1, 1};
			staffAndLayer[1] = new Integer[]{1, 2};
			staffAndLayer[2] = new Integer[]{2, 1};
			staffAndLayer[3] = new Integer[]{2, 2};
			staffAndLayer[4] = new Integer[]{2, 3};
		}
		else if (numVoices == 6) {
			staffAndLayer[0] = new Integer[]{1, 1};
			staffAndLayer[1] = new Integer[]{1, 2};
			staffAndLayer[2] = new Integer[]{1, 3};
			staffAndLayer[3] = new Integer[]{2, 1};
			staffAndLayer[4] = new Integer[]{2, 2};
			staffAndLayer[5] = new Integer[]{2, 3};
		}
		else {
			staffAndLayer = null;
		}

		return staffAndLayer;
	}


	/**
	 * Gets the clef for the given staff (1-based) with the given number of voices. 
	 * Non-grand-staff cleffing is fixed as follows
	 * <ul>
	 * <li>2vv: G F</li>
	 * <li>3vv: G F F</li>
	 * <li>4vv: G G F F</li>
	 * <li>5vv: G G F F F</li>
	 * <li>6vv: G G G F F F</li>
	 * </ul>
	 * 
	 * @param staff 1-based
	 * @param numVoices
	 * @param grandStaff
	 * @return
	 */
	// TESTED
	static String[] getCleffing(int staff, int numVoices, boolean grandStaff) {
		String[] g = new String[]{"G", "2"};
		String[] f = new String[]{"F", "4"};
		if (!grandStaff) {
			if ((staff == 2 && (numVoices == 2 || numVoices == 3)) ||
				(staff == 3 && (numVoices == 3 || numVoices == 4 || numVoices == 5)) ||
				(staff >= 4)) {
				return f;
			}
			else {
				return g;
			}
		}
		else {
			return staff == 1 ? g : f;
		}
	}


	// TESTED
	static String makeOpeningTag(String name, boolean isSelfClosing, String[][] atts) {
		String element = "<" + name;
		if (atts != null) {
			for (String[] att : atts) {
				if (att != null) {
					element += " " + att[0] + "='" + att[1] + "'";
				}
			}		
		}
		element += isSelfClosing ? "/>" : ">";
		return element;
	}


	static String makeClosingTag(String name) {
		return "</" + name + ">";
	}


	/**
	 * Gets the XML duration (1 for a whole note, 2 for a half note, 4 for a quarter note, etc.)
	 * of the given event.
	 * 
	 * @param event
	 * @return An <code>Integer[]</code> containing
	 *         <ul> 
	 *         <li>as element 0: the XML duration</li>
	 *         <li>as element 1: the XML dots</li>
	 *         </ul> 
	 *         or <code>null</code> if the given event does not start with a RhythmSymbol.
	 */
	// TESTED
	static Integer[] getXMLDur(String event) {
		Integer[] XMLDur = null;

		String ss = Symbol.SYMBOL_SEPARATOR;
		RhythmSymbol rs = 
			(!event.contains(ss)) ?	Symbol.getRhythmSymbol(event) : 
			Symbol.getRhythmSymbol(event.substring(0, event.indexOf(ss)));
		if (rs != null) {
			int dots = rs.getNumberOfDots();
			int dottedDur = rs.getDuration();
			int undottedDur = 
				dots != 0 ? TimeMeterTools.getUndottedNoteLength(dottedDur, dots) : 
				dottedDur;	
			XMLDur = new Integer[]{
				getXMLDur(Tablature.SMALLEST_RHYTHMIC_VALUE.mul(undottedDur)), dots
			};
		}
		return XMLDur;
	}


	/**
	 * Gets the XML duration of the given <code>Rational</code>.
	 * 
	 * @param r
	 * @return When <code>r</code> is a valid value (<= 1; 2; or 4)
	 * 		   <ul>
	 * 	       <li>When <code>r</code> <= 1: its denominator.</li>
	 *         <li>When <code>r</code> == 2: <code>BREVE</code>, i.e., -1.</li>
	 *         <li>When <code>r</code> == 4: <code>LONG</code>, i.e., -2.</li>
	 *         </ul>
	 *         or 0 when <code>r</code> is not a valid value.
	 *         
	 */
	// TESTED
	static int getXMLDur(Rational r) {
		r.reduce();
		if (r.isLessOrEqual(Rational.ONE)) {
			return r.getDenom();
		}
		else {
			if (r.equals(new Rational(2, 1))) { 
				return BREVE;
			}
			else if (r.equals(new Rational(4, 1))) {
				return LONG;
			}
			else {
				return 0;
			}
		}
	}


	// TESTED
	static String makeNoteXMLID(int ind, int v, int b, int s, String p, String o, Rational mp) {
		return String.join(".", 
			"n" + ind, 
			String.valueOf(v), 
			String.valueOf(b), 
			String.valueOf(s), 
			p + o, 
			mp.equals(Rational.ZERO) ? "0:1" : mp.getNumer() + ":" + mp.getDenom()
		);
	}


	// TESTED
	static String makeRestXMLID(int v, int b, int s, Rational mp) {
		return String.join(".", 
			"r",
			String.valueOf(v), 
			String.valueOf(b), 
			String.valueOf(s), 
			mp.equals(Rational.ZERO) ? "0:1" : mp.getNumer() + ":" + mp.getDenom()
		);
	}


	// TESTED BUT NOT IN USE -->
	/**
	 * Gets the duration (in Tablature.SMALLEST_RHYTHMIC_VALUE) of the given XML duration with 
	 * the given number of dots.
	 * 
	 * @param XMLDur
	 * @param dots
	 * @return
	 */
	// TESTED
	static int getDurFromXMLDur(int XMLDur, int dots) {
		int dur;
		
		// Get undotted duration
		// Breve (-1)
		if (XMLDur == -1) {
			dur = Symbol.LONGA.getDuration();
		}
		// W (1), H (2), Q (4), E (8), S (16), T (32)
		else {
			dur = Symbol.BREVIS.getDuration() / XMLDur;
		}
		
		// Get dotted duration
		if (dots > 0) {
			dur = TimeMeterTools.getDottedNoteLength(dur, dots);
		}	
		return dur;
	}


	// DEPRECATED -->
	private static int getStaffNum(boolean tablatureStaff, List<Boolean> cases, boolean tabOnTop, 
		boolean grandStaff, int numVoices, int voice) {
		boolean onlyTab = cases.get(0);
		boolean tabAndTrans = cases.get(1);

		// Options: ONLY_TAB, TAB_AND_TRANS && tabOnTop, TAB_AND_TRANS && !tabOnTop
		if (tablatureStaff) {
			if (onlyTab || (tabAndTrans && tabOnTop)) {
				return 1;
			}
			else {
				return grandStaff ? 3 : numVoices + 1;
			}
		}
		// Options: ONLY_TRANS, TAB_AND_TRANS && tabOnTop, TAB_AND_TRANS && !tabOnTop
		else {
			int staffNum = grandStaff ? getStaffAndLayerGrandStaff(numVoices)[voice][0] : voice + 1;
			if (tabAndTrans && tabOnTop) {
				staffNum++;
			}
			return staffNum;
		}
	}


	private static String exportMEIFileOLD(Transcription trans, Tablature tab, 
		List<List<Integer>> mismatchInds, boolean grandStaff, boolean tabOnTop, String[] dict) {
		System.out.println("\r\n>>> MEIExport.exportMEIFile() called");

		String INDENT_ONE = TAB.repeat(5);
		String INDENT_TWO = TAB.repeat(6);

//		if (getTemplatesPath() == null) {
//			setTemplatesPath("F:/research/data/" + "templates/");
//			setTemplatePath(Path.DEPLOYMENT_DEV_PATH + Path.TEEMPLATES_DIR);
//		}

		String res = null;
		String path = dict[0];
		String app = dict[1];

		if (tab != null) {
			if (trans == null) ONLY_TAB = true; else TAB_AND_TRANS = true;
		}
		ONLY_TRANS = tab == null ? true : false;

		String pieceName = tab != null ? tab.getName() : trans.getName();
		// mi from tab is the same as mi from trans in TAB_AND_TRANS case
		List<Integer[]> mi = (tab != null) ? tab.getMeterInfoAgnostic() : trans.getMeterInfo();
		int numBars = mi.get(mi.size()-1)[Transcription.MI_LAST_BAR];
//		int numMetricBars = mi.get(mi.size()-1)[Transcription.MI_LAST_BAR];
//		int numBars = numMetricBars;
		List<Rational[]> tripletOnsetPairs = tab != null ? tab.getTripletOnsetPairs() : null;
		// NB: tabMensSigns aligns with mi, i.e., each of its elements corresponds to
		// an element with the same bar and metric time in mi (but not vice versa)
		List<String[]> tabMensSigns = tab != null ? tab.getMensurationSigns() : null;
		Tuning tuning = ONLY_TAB || TAB_AND_TRANS ? tab.getTunings()[0] : null;

		List<Integer[]> ki = trans != null ? trans.getKeyInfo() : null;
		if (ki != null && ki.size() == 0) {
			ki.add(new Integer[]{-2, 0, 0, 0, 0, 0});
		}
//		ki.add(new Integer[]{2, 0, -1, -1, 44, 1}); // HAAL WEG!		
		// TAB_AND_TRANS CASE: adapt bars in ki to tablature bars (barring in
		// ki is equal to barring in mi, which follows the content of METER_INFO)
		if (TAB_AND_TRANS) {
			Timeline tla = tab.getEncoding().getTimelineAgnostic(); 
			for (int i = 0; i < ki.size(); i++) {
				Integer[] in = ki.get(i);
				int mtFirstBarInKi = new Rational(
					in[Transcription.KI_NUM_MT_FIRST_BAR],
					in[Transcription.KI_DEN_MT_FIRST_BAR]
				).mul(Tablature.SRV_DEN).getNumer();
				int firstBar = tla.getMetricPosition(mtFirstBarInKi)[0].getNumer();
				int lastBar;
				if (i < ki.size() - 1) {
					Integer[] inNext = ki.get(i + 1);
					int mtFirstBarInKiNext = new Rational(
						inNext[Transcription.KI_NUM_MT_FIRST_BAR],
						inNext[Transcription.KI_DEN_MT_FIRST_BAR]
					).mul(Tablature.SRV_DEN).getNumer();
					lastBar = tla.getMetricPosition(mtFirstBarInKiNext)[0].getNumer() - 1;
				}
				else {
					lastBar = mi.get(mi.size() - 1)[Transcription.MI_LAST_BAR];
				}
				in[Transcription.KI_FIRST_BAR] = firstBar;
				in[Transcription.KI_LAST_BAR] = lastBar;
			}
		}
		int numVoices = trans != null ? trans.getNumberOfVoices() : 0;
		System.out.println("meterInfo");
		for (Integer[] in : mi) {
			System.out.println(Arrays.asList(in));
		}
		if (ki != null) {
			System.out.println("keyInfo");
			for (Integer[] in : ki) {
				System.out.println(Arrays.asList(in));
			}
		}
		System.out.println("tabMensSigns");
		tabMensSigns.forEach(s -> System.out.println(Arrays.asList(s)));
		
		// Get the meter change and key change bars
//		List<Integer> meterChangeBars = ToolBox.getItemsAtIndex(mi, Tablature.MI_FIRST_BAR);

//		List<Integer> meterChangeBars = new ArrayList<>();
//		// Transcription case: all meter changes are shown, meterChangeBars deducted from mi
//		if (ONLY_TRANS) {
//			meterChangeBars = ToolBox.getItemsAtIndex(mi, Transcription.MI_FIRST_BAR);
//		}
//		// Tablature case: only those meter changes for which there is an MS in the
//		// tab are shown; meterChangeBars has to be built using tabMensSigns
//		if (ONLY_TAB || TAB_AND_TRANS) {
//			for (String[] s : tabMensSigns) {
//				meterChangeBars.add(Integer.parseInt(s[1]));
//			}
//			// First bar should always be there
//			if (!meterChangeBars.contains(1)) {
//				meterChangeBars.add(0, 1);
//			}
//		}
			
//		List<Integer> keyChangeBars = 
//			trans != null ? ToolBox.getItemsAtIndex(ki, Transcription.KI_FIRST_BAR) : null;
				
//		// Get section bars			
//		List<Integer> sectionBars = new ArrayList<>();
//		sectionBars.addAll(meterChangeBars);
//		sectionBars.addAll(trans != null ? new ArrayList<>(keyChangeBars) : new ArrayList<>());
//		sectionBars = sectionBars.stream().distinct().collect(Collectors.toList());
//		Collections.sort(sectionBars);

		// Align mi and ki
		if (TAB_AND_TRANS || ONLY_TRANS) {
			List<List<Integer[]>> miKiAligned = alignMeterAndKeyInfo(mi, ki);
			mi = miKiAligned.get(0);
			ki = miKiAligned.get(1);
		}

		// Get section bars
		List<Integer> sectionBars = 
//			ONLY_TAB ? meterChangeBars : 
			ToolBox.getItemsAtIndex(mi, Transcription.MI_FIRST_BAR); 

//		System.out.println("meterChangeBars");
//		System.out.println(meterChangeBars);
//		System.out.println("keyChangeBars");
//		System.out.println(keyChangeBars);
		System.out.println("sectionBars");
		System.out.println(sectionBars);

		// Make the <meiHead> 
		String[] meiHead = new String[MEI_HEAD.size()];
		meiHead[MEI_HEAD.indexOf("title")] = pieceName;
		res = res.replace("title_placeholder", meiHead[MEI_HEAD.indexOf("title")]);
		res = res.replace("date_placeholder", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		String version = "1.0.0";
		res = res.replace("version_placeholder", version);
		res = res.replace("tool_placeholder", app);

		// Make the <music> 
		// 1. Make the <scoreDef>s as strings (one for each section) 
		List<String> scoreDefsAsStr = new ArrayList<>();
		for (int i = 0; i < sectionBars.size(); i++) {
			int bar = sectionBars.get(i);
			System.out.println("BARRRRRRR = " + bar);

//			// currMi (currKi) is null if there is a key (meter) change at bar, 
//			// but no meter (key) change
//			Integer[] currMi = 
//				meterChangeBars.contains(bar) ? 
//				mi.get(ToolBox.getItemsAtIndex(mi, Transcription.MI_FIRST_BAR).indexOf(bar)) : null;
//			Integer[] currKi = 
//				keyChangeBars.contains(bar) ? 
//				ki.get(ToolBox.getItemsAtIndex(ki, Transcription.KI_FIRST_BAR).indexOf(bar)) : null;

			Integer[] currMi = mi.get(
				ToolBox.getItemsAtIndex(mi, Transcription.MI_FIRST_BAR).indexOf(bar)
			);
			Integer[] currKi = 
				ONLY_TAB ? null : 
				ki.get(
					ToolBox.getItemsAtIndex(ki, Transcription.KI_FIRST_BAR).indexOf(bar)
				);

			int indInTabMs = 
				tab == null ? - 1 : 
				ToolBox.getItemsAtIndex(tabMensSigns, 1).indexOf(String.valueOf(bar));
			String tabMs = 
				tab == null ? null :
				indInTabMs == -1 ? null : tabMensSigns.get(indInTabMs)[0];
				
			System.out.println("currMi");
			System.out.println(Arrays.asList(currMi));
			if (currKi != null) {
				System.out.println("currKi");
				System.out.println(Arrays.asList(currKi));
			}
			System.out.println(tabMs);
				
//			String tabMs;
//			if (tab == null) {
//				tabMs = null;
//			}
//			else {
//				int indInTabMs = ToolBox.getItemsAtIndex(
//					tabMensSigns, 1).indexOf(String.valueOf(bar)
//				);
//				tabMs = indInTabMs == -1 ? null : tabMensSigns.get(indInTabMs)[0];
//			}

//			if (tab != null && currMi != null) {
//				for (String[] s : tabMensSigns) {
//					int tabMSBar =
//						// tab bar
//						ONLY_TAB ? Integer.parseInt(s[1]) :	
//						// bar in mi that goes with the MS's metric time
//						TimeMeterTools.getMetricPosition(
//							new Rational(Integer.parseInt(s[2]), Tablature.SRV_DEN), mi
//						)[0].getNumer();
//
//					if (tabMSBar == bar) {
//						tabMs = s[0];
//						break;
//					}
//				}
//			}

			List<String> currScoreDef = null; makeScoreDefs(
				tab, mi, ki, null, numVoices
			);
			currScoreDef.forEach(s -> System.out.println(s));
			StringBuilder currScoreDefStr = new StringBuilder();
			for (String s : currScoreDef) {
				// Any <scoreDef> after the first is placed inside its <section> instead of
				// before it, and therefore must be shifted by INDENT_ONE + TAB 
				currScoreDefStr.append((i == 0 ? INDENT_ONE : INDENT_ONE + TAB) + s + "\r\n");
			}
			scoreDefsAsStr.add(currScoreDefStr.toString());
		}

		// 2. Make the bars as strings
		// a. Tab bars
		List<String> tabBarsAsStr = new ArrayList<>();
		if (ONLY_TAB || TAB_AND_TRANS) {
			List<List<String>> tabBars = getTabBars(tab, numVoices);
				for (int i = 0; i < numBars; i++) {
				StringBuilder currTabBarAsStr = new StringBuilder();
				if (i < numBars) {
					for (String s : tabBars.get(i)) {
						currTabBarAsStr.append(INDENT_TWO + TAB + s + "\r\n");
					}
				}
				tabBarsAsStr.add(currTabBarAsStr.toString());
			}
		}
		// b. Trans bars
		List<String> transBarsAsStr = new ArrayList<>();
		if (ONLY_TRANS || TAB_AND_TRANS) {	
			List<Object> data = getData(tab, trans, mi, ki, tripletOnsetPairs/*, -1, -1*/);
			List<List<String>> transBars = getTransBars(
				tab, data, mi, tripletOnsetPairs, mismatchInds, numVoices, dict[1]
			);
			for (int i = 0; i < numBars; i++) {
				StringBuilder currTransBarAsStr = new StringBuilder();
				for (String s : transBars.get(i)) {
					currTransBarAsStr.append(INDENT_TWO + TAB + s + "\r\n");
				}
				transBarsAsStr.add(currTransBarAsStr.toString());
			}
		}

		// 3. Make the <section>s as strings. Each <section> consists of a <scoreDef> 
		// followed by its bars (as made above) wrapped in <measure>s. The first <section> 
		// has the (main) <scoreDef> preceding it; any subsequent <section>s have their 
		// <scoreDef> as their first child		
		List<String> sectionsAsStr = new ArrayList<>();
		StringBuilder currSection = new StringBuilder();
		for (int i = 0; i < numBars; i++) {
			int currBar = i + 1;
			int nextBar = currBar + 1;
			boolean lastBarInSection = sectionBars.contains(nextBar) || currBar == numBars;
			if (sectionBars.contains(currBar)) {
				int sectionInd = sectionBars.indexOf(currBar);
				// First <scoreDef> and <section>: <scoreDef> precedes <section>
				if (sectionInd == 0) {
					currSection.append(scoreDefsAsStr.get(sectionInd));
					currSection.append(INDENT_ONE + "<section n='" + (sectionInd+1) + "'>" + "\r\n");
				}
				// Subsequent <scoreDef>s and <section>s: <scoreDef> first child of <section>
				else {
					currSection.append(INDENT_ONE + "<section n='" + (sectionInd+1) + "'>" + "\r\n");
					currSection.append(scoreDefsAsStr.get(sectionInd));
				}
			}
			String barline = 
				lastBarInSection ? (currBar == numBars ? " right='end'" : " right='dbl'") : "";
			currSection.append(INDENT_TWO + "<measure n='" + (i+1) + "'" + barline + ">" + "\r\n");
			String tabBar = tab != null ? tabBarsAsStr.get(i) : null;
			String transBar = ONLY_TAB ? null : transBarsAsStr.get(i);
			if (ONLY_TAB) {
				currSection.append(tabBar);
			}
			else if (ONLY_TRANS) {
				currSection.append(transBar);
			}
			else if (TAB_AND_TRANS) {
				if (tabOnTop) {
					currSection.append(tabBar);
					currSection.append(transBar);
				}
				else {
					currSection.append(transBar);
					currSection.append(tabBar);
				}
			}
			currSection.append(INDENT_TWO + "</measure>" + "\r\n");
			// Last bar in section or last bar in piece? Add to sectionsAsStr
			if (lastBarInSection) {
				currSection.append(INDENT_ONE + "</section>" + "\r\n");
				sectionsAsStr.add(currSection.toString());
				currSection = new StringBuilder();
			}
		}

		// 4. Combine the <section>s into the <score> content
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sectionBars.size(); i++) {
			sb.append(sectionsAsStr.get(i));
		}

		res = res.replace(INDENT_ONE + "score_placeholder" + "\r\n", sb.toString());

		if (path != null) { 
			ToolBox.storeTextFile(
				res, new File(path + "-" + (grandStaff ? "grand_staff" : "score") + MEI_EXT)
			);
			return null;
		}
		else {
			return res;
		}
	}


	/**
	 * Extracts from the given Transcription the data (note attributes) needed to create the MEI file.
	 * 
	 * @param tab
	 * @param trans
	 * @param mi
	 * @param ki
	 * @param tripletOnsetPairs
	 *
	 * @returns A {@code List<Object>} containing a {@code List<List<List<Integer[]>>>} ({@code lin})  
	 *          and a {@code List<List<List<String[]>>>} ({@code lst}) representing note attributes 
	 *          as described in {@code INTS} and {@code STRINGS}. For both {@code lin} and {@code lst},
	 *          <ul>
	 *          <li>Each element (a {@code List<List<Integer[]>>} or {@code List<List<String[]>>})
	 *              represents a bar.</li> 
	 *          <li>Each bar element (a {@code List<Integer[]>} or {@code List<String[]>}) represents 
	 *              a voice.</li>
	 *          <li>Each bar-voice element (a {@code Integer[]} or {@code String[]}) represents a note.</li>
	 *          </ul>
	 *          
	 *          Thus, {@code lin.size()} (or {@code lst.size()}) gives the number of bars in the piece, and
	 *          <ul>
	 *          <li> {@code lin.get(b)} gives bar {@code b}, and 
	 *               {@code lin.get(b).size()} gives the number of voices in bar {@code b} 
	 *               (and thus in the whole piece, as the number of voices is fixed).</li>
	 *          <li> {@code lin.get(b).get(v)} gives bar {@code b}, voice {@code v}, and 
	 *               {@code lin.get(b).get(v).size()} gives the number of notes in that voice.</li>
	 *          <li>{@code lin.get(b).get(v).get(n)} gives bar {@code b}, voice {@code v}, note {@code n}, and 
	 *               {@code lin.get(b).get(v).get(n).length} gives the number of attributes of that note.</li>  
	 *          </ul>
	 */
	@SuppressWarnings("unchecked")
	private static List<Object> getDataOLD(Tablature tab, Transcription trans, List<Integer[]> mi, 
		List<Integer[]> ki, List<Rational[]> tripletOnsetPairs) {
		System.out.println("\r\n>>> getData() called");

		Integer[][] bnp = trans.getBasicNoteProperties();
		Integer[][] btp = null;
		Timeline tl = null;
		List<List<Integer>> transToTabInd = null;
		if (ONLY_TAB || TAB_AND_TRANS) {
			btp = tab.getBasicTabSymbolProperties();
			tl = tab.getEncoding().getTimelineAgnostic();
			transToTabInd = Transcription.alignTabAndTransIndices(btp, bnp).get(1);
		}
		ScorePiece p = trans.getScorePiece();
		ScoreMetricalTimeLine smtl = p.getScoreMetricalTimeLine();
		int numVoices = p.getScore().size();
		int numBars = mi.get(mi.size()-1)[Transcription.MI_LAST_BAR];
		List<Integer> sectionBars = ToolBox.getItemsAtIndex(mi, Transcription.MI_FIRST_BAR);
		Rational endOffset = TimeMeterTools.getFinalOffset(mi);

		Rational gridVal = 
			ONLY_TAB || TAB_AND_TRANS ? Tablature.SMALLEST_RHYTHMIC_VALUE : 
			new Rational(1, 128);
		List<Integer> gridNums = IntStream.rangeClosed(0, gridVal.getDenom())
			.boxed()
			.collect(Collectors.toList());

		List<List<String[]>> noteAttribPerVoiceStrs = new ArrayList<List<String[]>>();
		List<List<Integer[]>> noteAttribPerVoiceInts = new ArrayList<List<Integer[]>>();
		for (int i = 0; i < numVoices; i++) {
			noteAttribPerVoiceStrs.add(new ArrayList<String[]>());
			noteAttribPerVoiceInts.add(new ArrayList<Integer[]>());
		}
		List<Integer> naturalsAlreadyAdded = new ArrayList<Integer>();
		List<Integer> accidentalsAlreadyAdded = new ArrayList<Integer>();
		List<Integer> doubleFlatsInEffect = new ArrayList<>();
		List<Integer> flatsInEffect = new ArrayList<>();
		List<Integer> naturalsInEffect = new ArrayList<>();
		List<Integer> sharpsInEffect = new ArrayList<>();
		List<Integer> doubleSharpsInEffect = new ArrayList<>();
		String pitchesNotInKey = "";
		List<String> uniquePitchesNotInKey = new ArrayList<>();

//		int numAlt = 0;
		Integer[] currKi = ki.get(0); 
		List<Object> grids = PitchKeyTools.createGrids(
			currKi[Transcription.KI_KEY], currKi[Transcription.KI_MODE]
		);
//		Integer[] mpcGrid = (Integer[]) grids.get(0);
//		String[] altGrid = (String[]) grids.get(1);
//		String[] pcGrid = (String[]) grids.get(2);
//		int prevBar = 0;
		Rational barEnd = new Rational(
			mi.get(0)[Transcription.MI_NUM], mi.get(0)[Transcription.MI_DEN]
		);
		for (int i = 0; i < bnp.length; i++) {
			int iTab = ONLY_TAB || TAB_AND_TRANS ? transToTabInd.get(i).get(0) : -1; // each element (list) contains only one element (int)		
//			int iTab = -1;
//			if (ONLY_TAB || TAB_AND_TRANS) {
//				iTab = transToTabInd.get(i).get(0); // each element (list) contains only one element (int)
//			}

			int pitch = bnp[i][Transcription.PITCH];

			Rational dur = 
				ONLY_TAB ? new Rational(btp[iTab][Tablature.MIN_DURATION], Tablature.SRV_DEN) :
				new Rational(bnp[i][Transcription.DUR_NUMER], bnp[i][Transcription.DUR_DENOM]);
			dur = TimeMeterTools.round(dur, gridNums);
			
			Rational onset = 
				ONLY_TAB ? new Rational(btp[iTab][Tablature.ONSET_TIME], Tablature.SRV_DEN) :
				new Rational(bnp[i][Transcription.ONSET_TIME_NUMER], bnp[i][Transcription.ONSET_TIME_DENOM]);
			onset = TimeMeterTools.round(onset, gridNums);

//			Rational onset = new Rational(
//				bnp[i][Transcription.ONSET_TIME_NUMER], bnp[i][Transcription.ONSET_TIME_DENOM]
//			);
//			if (ONLY_TAB) {
//				onset = new Rational(btp[iTab][Tablature.ONSET_TIME], Tablature.SRV_DEN);
//			}
//			onset = TimeMeterTools.round(onset, gridNums);

			Rational[] barMetPos = 
				ONLY_TAB || TAB_AND_TRANS ?
				tl.getMetricPosition((int) onset.mul(Tablature.SRV_DEN).toDouble()) : // multiplication necessary because of division when making onset above
				smtl.getMetricPosition(onset);
			
			Rational metPos = barMetPos[1];

			Rational offset = onset.add(dur);
			
//			Integer[] currKi = ki.get(sectionBars.indexOf(bar));
			int voice = LabelTools.convertIntoListOfVoices(trans.getVoiceLabels().get(i)).get(0);
			// currVoiceStrings and currVoiceInts start out as empty lists at i == 0, and 
			// are populated with pitchOctAccTie and indBarOnsMpDurDots for the current 
			// note (+ any preceding rests) while iterating over bnp
			List<String[]> currVoiceStrs = noteAttribPerVoiceStrs.get(voice);
			List<Integer[]> currVoiceInts = noteAttribPerVoiceInts.get(voice);

//			// New bar (this includes the first)? Update prevBar and check if grids must be updated
//			if (bar == prevBar + 1) {
//				if (sectionBars.contains(bar)) {
//					Integer[] currKi = ki.get(sectionBars.indexOf(bar));
//					// Only if key signature changes
//					if (currKi[currKi.length-1] == 1) {
//						numAlt = currKi[Transcription.KI_KEY];
//						grids = PitchKeyTools.createGrids(numAlt, currKi[Transcription.KI_MODE]);
//						mpcGrid = (Integer[]) grids.get(0);
//						altGrid = (String[]) grids.get(1);
//						pcGrid = (String[]) grids.get(2);
//					}
//				}
//				prevBar = bar;
//			}

			int bar = barMetPos[0].getNumer();
			
			// When new bar is reached
			if (onset.isGreaterOrEqual(barEnd)) {
				// Increment barEnd and clear lists
				barEnd = (onset.sub(metPos)).add(Transcription.getMeter(bar, mi));
				naturalsAlreadyAdded.clear();
				accidentalsAlreadyAdded.clear();
				doubleFlatsInEffect.clear();
				flatsInEffect.clear();
				naturalsInEffect.clear();
				sharpsInEffect.clear();
				doubleSharpsInEffect.clear();

				// Update currKi and, if key signature changes, grids
				if (sectionBars.contains(bar)) {
					currKi = ki.get(sectionBars.indexOf(bar));
					if (currKi[currKi.length-1] == 1) {
//						numAlt = currKi[Transcription.KI_KEY];
						grids = PitchKeyTools.createGrids(
							currKi[Transcription.KI_KEY], currKi[Transcription.KI_MODE]
						);
//						mpcGrid = (Integer[]) grids.get(0);
//						altGrid = (String[]) grids.get(1);
//						pcGrid = (String[]) grids.get(2);
					}
				}
			}
	
//			Rational dur = new Rational(
//				bnp[i][Transcription.DUR_NUMER], bnp[i][Transcription.DUR_DENOM]
//			);
//			dur = TimeMeterTools.round(dur, gridNums);
//			if (ONLY_TAB) {
//				dur = new Rational(btp[iTab][Tablature.MIN_DURATION], Tablature.SRV_DEN);
//			}

			if (verbose) {
				System.out.println("pitch                    " + pitch);
				System.out.println("dur                      " + dur);
				System.out.println("onset                    " + onset);
				System.out.println("metPos                   " + metPos);
				System.out.println("offset                   " + offset);	
				System.out.println("voice                    " + voice);
				System.out.println("bar                      " + bar);
				System.out.println("barEnd                   " + barEnd);
				System.out.println("midiPitchClass           " + (pitch % 12));
				System.out.println("currMpcGrid              " + Arrays.asList(grids.get(0)));
				System.out.println("currAltgrid              " + Arrays.asList(grids.get(1)));
				System.out.println("currPcGrid               " + Arrays.asList(grids.get(2)));
				System.out.println("------------------");	
			
				if (!Arrays.asList(grids.get(0)).contains(pitch % 12)) {
					List<String> pitches = Arrays.asList(
						"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "Bb", "B"
					);
					Rational mp = metPos;
					mp.reduce();
					pitchesNotInKey += pitches.get(pitch % 12) + " (MIDI " + pitch + "), bar " + 
						bar + ", voice " + voice + ", onset " + metPos + " not in key\r\n"; 
					if (!uniquePitchesNotInKey.contains(pitches.get(pitch % 12))) {
						uniquePitchesNotInKey.add(pitches.get(pitch % 12));
					}
				}
			}

			// Add data to pitchOctAccTie and indBarOnsMpDurDots for 
			// - any rests preceding the note at i
			// - the note at i
			// At end of loop over bnp, add to currVoiceStrings and currVoiceInts 
			List<String[]> pitchOctAccTie = new ArrayList<String[]>();
			List<Integer[]> indBarOnsMpDurDots = new ArrayList<Integer[]>();
			String[] curr = new String[STRINGS.size()];

			// 1. Rest(s) preceding the note at i
			// a. Determine the previous note's duration, metric position, and offset
			Rational durPrev, metPosPrev, offsetPrev;
			// First note in voice?
			if (currVoiceStrs.size() == 0) {
				durPrev = Rational.ZERO;
				metPosPrev = Rational.ZERO;
				offsetPrev = Rational.ZERO;
			}
			else {
				// prevNote is the last item in currVoiceInts 
				// (1) whose onset is less than onset
				// (2) whose onset equals onset (if prevNote is a lower note in an in-voice chord)
				Integer[] prevNote = null;
				Rational onsetPrev = null;
				// NB currVoiceInts grows with every iteration through the i-loop, so starting at 
				// its end just means starting at the element added in the last i-loop iteration
				for (int j = currVoiceInts.size()-1; j >= 0; j--) {
					prevNote = currVoiceInts.get(j);
					onsetPrev = new Rational(
						prevNote[INTS.indexOf("onsetNum")], prevNote[INTS.indexOf("onsetDen")]
					);
					// If previous onset is less or equal than current (but is not a rest (onset = -1/-1))
					if (onsetPrev.getNumer() != -1 && onsetPrev.isLessOrEqual(onset)) {
//					if (onsetPrev.getNumer() != -1 && onsetPrev.isLess(onset)) {
						break;
					}
//					// If previous onset equals current (but is not a rest (onset = -1/-1))
//					if (onsetPrev.getNumer() != -1 && onsetPrev.equals(onset)) {
//						break;
//					}
				}
				int durPrevInt = prevNote[INTS.indexOf("dur")]; 
				durPrev = 
					(durPrevInt > 0) ? new Rational(1, durPrevInt) :
					((durPrevInt == BREVE ? new Rational(2, 1) : new Rational(4, 1)));
				int dotsPrev = prevNote[INTS.indexOf("dots")];
				Rational dlf = TimeMeterTools.getDotLengtheningFactor(dotsPrev);
//				Rational dlf = new Rational((int) Math.pow(2, dotsPrev) - 1, (int) Math.pow(2, dotsPrev));
				durPrev = durPrev.add(durPrev.mul(dlf));
				metPosPrev = 
					ONLY_TAB || TAB_AND_TRANS ? 
						tl.getMetricPosition((int) onsetPrev.mul(Tablature.SRV_DEN).toDouble())[1] :
					smtl.getMetricPosition(onsetPrev)[1];
				offsetPrev = onsetPrev.add(durPrev);

				// If onsetPrev is within a triplet, durPrev and offsetPrev (needed to calculate 
				// rests) must be reverted from their tripletised value to their untripletised value
				//
				// To tripletise is to give a note its nominal (shown) value instead of its 
				// actual value by multiplying it with TRIPLETISER (3/2). 
				// Example for a half note:
				// untripletised = 1/3, 1/3, 1/3
				// tripletised = 1/2, 1/2, 1/2
				// E.g., 1/3 * 3/2 = 1/2; 1/6 * 3/2 = 1/4; etc.
				// Untripletised variables calculated from prevNote: onsetPrev, metPosPrev
				// Tripletised variables calculated from prevNote: durPrev, offsetPrev
				if (prevNote[INTS.indexOf("tripletOpen")] == 1 || prevNote[INTS.indexOf("tripletMid")] == 1 ||
					prevNote[INTS.indexOf("tripletClose")] == 1) {
					// Recalculate durPrev by multiplying it by DETRIPLETISER (2/3)
//					durPrevInt = prevNote[INTS.indexOf("dur")];
//					durPrev = 
//						(durPrevInt > 0) ? new Rational(1, durPrevInt) :
//						((durPrevInt == BREVE ? new Rational(2, 1) : new Rational(4, 1)));
					durPrev = durPrev.mul(DETRIPLETISER);
//					dotsPrev = prevNote[INTS.indexOf("dots")];
//					dlf = TimeMeterTools.getDotLengtheningFactor(dotsPrev);
//					dlf = new Rational((int) Math.pow(2, dotsPrev) - 1, (int) Math.pow(2, dotsPrev));
					durPrev = durPrev.add(durPrev.mul(dlf));
					offsetPrev = onsetPrev.add(durPrev);
				}
			}
			// b. Add rest data (if applicable)
			Rational durRest = onset.sub(offsetPrev);
			if (durRest.isGreater(Rational.ZERO)) {
				Rational precedingInBar = metPos;
				boolean singleBarRestInSameBar = durRest.isLessOrEqual(precedingInBar);
				boolean singleBarRestInPrevBar = 
					precedingInBar.equals(Rational.ZERO) && 
					durRest.isLessOrEqual(Transcription.getMeter(bar-1, mi));
				// Single-bar rest
				if (singleBarRestInSameBar || singleBarRestInPrevBar) {
					Rational onsetRest = offsetPrev;
					Rational metPosRest;
					// Single-bar rest in the same bar
					if (singleBarRestInSameBar) {
//						System.out.println("CASE: single-bar rest");
						metPosRest = 
							durRest.equals(precedingInBar) ? Rational.ZERO : // the bar starts with a rest
							(currVoiceStrs.size() == 0 ? Rational.ZERO : metPosPrev.add(durPrev));
					}
					// Single-bar rest in the previous bar (onset is 0/x)
					else {
//						System.out.println("CASE: single-bar rest in previous bar");
						metPosRest = 
							currVoiceStrs.size() == 0 ? Rational.ZERO :
							(ONLY_TAB || TAB_AND_TRANS ? 
								tl.getMetricPosition((int) onsetRest.mul(Tablature.SRV_DEN).toDouble())[1] :
							smtl.getMetricPosition(onsetRest)[1]);
					}

//					List<Boolean> tripletInfo = 
//						tripletOnsetPairs == null ? null :
//						TimeMeterTools.isTripletOnset(tripletOnsetPairs, onsetRest);
//					List<Object> noteData = getNoteData(
//						i, iTab, curr, durRest, onsetRest, metPosRest, mi, 
//						tripletInfo, tripletOnsetPairs, gridVal 
//					);
//					pitchOctAccTie.addAll(0, (List<String[]>) noteData.get(0));
//					indBarOnsMpDurDots.addAll(0, (List<Integer[]>) noteData.get(1));
					
					updateDataLists(
						indBarOnsMpDurDots, pitchOctAccTie, true, onsetRest, 
						i, iTab, curr, durRest, onsetRest, metPosRest, 
						mi, tripletOnsetPairs, gridVal
					);
				}
//				// Single-bar rest in the same bar
//				if (singleBarRestInSameBar) {
////					System.out.println("CASE: single-bar rest");
//					Rational onsetRest = offsetPrev;
//					Rational metPosRest = 
//						durRest.equals(precedingInBar) ? Rational.ZERO : // the bar starts with a rest
//						(currVoiceStrings.size() == 0 ? Rational.ZERO : metPosPrev.add(durPrev));					
////					Rational metPosRest = null;
////					// If the bar starts with a rest
////					if (durRest.equals(precedingInBar)) {
////						metPosRest = Rational.ZERO;
////					}
////					else {
////						metPosRest = 
////							currVoiceStrings.size() == 0 ? Rational.ZERO : metPosPrev.add(durPrev);
////					}
//
//					// ZELFDE 1-2 -->
//					List<Boolean> tripletInfo = 
//						tripletOnsetPairs == null ? null :
//						TimeMeterTools.isTripletOnset(tripletOnsetPairs, onsetRest);
//					List<Object> noteData = getNoteData(
//						i, iTab, curr, durRest, gridVal, onsetRest, metPosRest, mi, 
//						tripletInfo, tripletOnsetPairs
//					);
//					pitchOctAccTie.addAll(0, (List<String[]>) noteData.get(0));
//					indBarOnsMpDurDots.addAll(0, (List<Integer[]>) noteData.get(1));
//					// <-- ZELFDE
//				}
//				// Single-bar rest in the previous bar (onset is 0/x)
//				else if (singleBarRestInPrevBar) {
////					System.out.println("CASE: single-bar rest in previous bar");
//					Rational onsetRest = offsetPrev;
//					Rational metPosRest = 
//						currVoiceStrings.size() == 0 ? Rational.ZERO :
//						(ONLY_TAB || TAB_AND_TRANS ? 
//							tl.getMetricPosition((int) onsetRest.mul(Tablature.SRV_DEN).toDouble())[1] :
//						smtl.getMetricPosition(onsetRest)[1]);
//
//					// ZELFDE 1-2 -->
//					List<Boolean> tripletInfo = 
//						tripletOnsetPairs == null ? null :
//						TimeMeterTools.isTripletOnset(tripletOnsetPairs, onsetRest);
//					List<Object> noteData = getNoteData(
//						i, iTab, curr, durRest, gridVal, onsetRest, metPosRest, mi, 
//						tripletInfo, tripletOnsetPairs
//					);
//					pitchOctAccTie.addAll(0, (List<String[]>) noteData.get(0));
//					indBarOnsMpDurDots.addAll(0, (List<Integer[]>) noteData.get(1));
//					// <-- ZELFDE
//				}
				// Multi-bar rest
				else {
//					System.out.println("CASE: multi-bar rest");
					// Check how many bars the note spans and make subNoteDurs and 
					// subNoteDursOnsets (containing the onsets of the subnotes)
					List<Rational> subNoteDurs = new ArrayList<>();
					List<Rational> subNoteDursOnsets = new ArrayList<>(); 
					if (!precedingInBar.equals(Rational.ZERO)) {
						subNoteDurs.add(precedingInBar);
						subNoteDursOnsets.add(onset.sub(precedingInBar));
					}
					Rational remainder = durRest.sub(precedingInBar);
					int beginBar = 
						ONLY_TAB || TAB_AND_TRANS ? 
							tl.getMetricPosition((int) offsetPrev.mul(Tablature.SRV_DEN).toDouble())[0].getNumer() : 
						smtl.getMetricPosition(offsetPrev)[0].getNumer();
					for (int j = bar - 1; j >= beginBar; j--) {
						Rational currBarLen = Transcription.getMeter(j, mi);
						if (remainder.isGreaterOrEqual(currBarLen)) {
							subNoteDurs.add(currBarLen);
							// The onset of this subnote is onset of the previous subnote 
							// (i.e., the one added last to the list) minus the bar length
							subNoteDursOnsets.add(
								subNoteDursOnsets.size() == 0 ? onset.sub(currBarLen) : // no remainderInBar, so onset is at the beginning of the bar 
								subNoteDursOnsets.get(subNoteDursOnsets.size()-1).sub(currBarLen)
							);
//							if (subNoteDursOnsets.size() == 0) {
//								// No remainderInBar, so onset is at the beginning of the bar
//								subNoteDursOnsets.add(
//									onset.sub(currBarLen)
//								);
//							}
//							else {
//								subNoteDursOnsets.add(
//									subNoteDursOnsets.get(subNoteDursOnsets.size()-1).sub(currBarLen)
//								);
//							}
							remainder = remainder.sub(currBarLen);
						}
						else {
							if (!remainder.equals(Rational.ZERO)) {
								subNoteDurs.add(remainder);
								// The onset of this subnote is onset of the previous subnote 
								// (i.e.,  the one added last to the list) minus the remainder
								// NB subNoteDursOnsets.size()-1 always exists: in the case of 
								// a multi-bar rest, either a rest with length remainingInBar 
								// or one with length barLength has already been added
								subNoteDursOnsets.add(
									subNoteDursOnsets.get(subNoteDursOnsets.size()-1).sub(remainder)
								);
							}
						}
					}
					Collections.reverse(subNoteDurs);
					Collections.reverse(subNoteDursOnsets);
					// For each subnote
					Rational currOnset = offsetPrev;
					Rational currMetPosRest = 
						(currVoiceStrs.size() == 0) ? Rational.ZERO : metPosPrev.add(durPrev);
					// If currMetPosRest equals the length of the bar before beginBar, this means 
					// that the rest starts at the beginning of beginBar, and that currMetPosRest
					// must be set to Rational.ZERO
					if (currMetPosRest.equals(Transcription.getMeter(beginBar-1, mi))) {
						currMetPosRest = Rational.ZERO;
					}
					for (int j = 0; j < subNoteDurs.size(); j++) {
						Rational currSubNoteDur = subNoteDurs.get(j);
						Rational currSubNoteDurOnset = subNoteDursOnsets.get(j);
//						List<Boolean> tripletInfo = 
//							(tripletOnsetPairs == null) ? null :
//							TimeMeterTools.isTripletOnset(tripletOnsetPairs, currSubNoteDurOnset);
//						List<Object> noteData = getNoteData(
//							i, iTab, curr, currSubNoteDur, currOnset, currMetPosRest, 
//							mi, tripletInfo, tripletOnsetPairs, gridVal 
//						);
////						List<String[]> subNote = (List<String[]>) noteData.get(0);
//						pitchOctAccTie.addAll((List<String[]>) noteData.get(0));
//						indBarOnsMpDurDots.addAll((List<Integer[]>) noteData.get(1));

						updateDataLists(
							indBarOnsMpDurDots, pitchOctAccTie, false, currSubNoteDurOnset, 
							i, iTab, curr, currSubNoteDur, currOnset, currMetPosRest, 
							mi, tripletOnsetPairs, gridVal);

						currOnset = currOnset.add(currSubNoteDur);
						currMetPosRest = Rational.ZERO;
					}
				}
			}

			// 2. Note at i
			// Determine pitch spelling and update accidentals lists
			List<List<Integer>> accidsInEffect = new ArrayList<>();
			accidsInEffect.add(doubleFlatsInEffect);
			accidsInEffect.add(flatsInEffect);
			accidsInEffect.add(naturalsInEffect);
			accidsInEffect.add(sharpsInEffect);
			accidsInEffect.add(doubleSharpsInEffect);
			List<Object> pitchSpell = PitchKeyTools.spellPitch(
				pitch, currKi[Transcription.KI_KEY], grids, accidsInEffect
			);
			String[] pa = (String[]) pitchSpell.get(0);
			List<List<Integer>> aie = (List<List<Integer>>) pitchSpell.get(1);
			doubleFlatsInEffect = aie.get(0);
			flatsInEffect = aie.get(1);
			naturalsInEffect = aie.get(2);
			sharpsInEffect = aie.get(3);
			doubleSharpsInEffect = aie.get(4);

			// a. Set pname, accid, oct
			String pname = pa[0];
			String accid = pa[1];
			String oct = String.valueOf(PitchKeyTools.getOctave(pitch));
			curr[STRINGS.indexOf("pname")] = "pname='" + pname + "'"; 
			if (!accid.equals("")) {
				curr[STRINGS.indexOf("accid")] = "accid='" + accid + "'";
			}
			curr[STRINGS.indexOf("oct")] = "oct='" + oct + "'";
			if (verbose) {
				System.out.println("pname                    " + pname);
				System.out.println("accid                    " + accid);
				System.out.println("oct                      " + oct);
			}

			// b. Set tie, dur, dots
			Rational remainingInBar = barEnd.sub(onset);
			// Single-bar note
			if (dur.isLessOrEqual(remainingInBar)) {
//				System.out.println("CASE: single-bar note");
//				List<Boolean> tripletInfo = 
//					(tripletOnsetPairs == null) ? null : 
//					TimeMeterTools.isTripletOnset(tripletOnsetPairs, onset);
//				List<Object> noteData = getNoteData(
//					i, iTab, curr, dur, onset, metPos, mi, tripletInfo, 
//					tripletOnsetPairs, gridVal 
//				);
//				pitchOctAccTie.addAll((List<String[]>) noteData.get(0));
//				indBarOnsMpDurDots.addAll((List<Integer[]>) noteData.get(1));
				
				updateDataLists(indBarOnsMpDurDots, pitchOctAccTie, false,
					onset, 
					i, iTab, curr, dur, onset, metPos, 
					mi, tripletOnsetPairs, gridVal);
			}
			// Multi-bar note
			else {
//				System.out.println("CASE: multi-bar note");
				// Check how many bars the note spans and make subNoteDurs and 
				// subNoteDursOnsets (containing the onsets of the subnotes)
				List<Rational> subNoteDurs = new ArrayList<>();
				List<Rational> subNoteDursOnsets = new ArrayList<>();
				subNoteDurs.add(remainingInBar);
				subNoteDursOnsets.add(onset);
				subNoteDursOnsets.add(onset.add(remainingInBar));
				Rational remainder = dur.sub(remainingInBar);
				// In the case of a tablature with predicted durations, those of the final chord
				// can be incorrectly predicted too long, thus extending beyond endOffset 
				if (offset.isGreater(endOffset)) {
					offset = endOffset;
				}
				int endBar = 
					(offset.equals(endOffset)) ? numBars :	
//					(offset.equals(endOffset)) ? lastBar : // HIEJE
//					(offset.equals(endOffset)) ? mi.get(mi.size()-1)[Transcription.MI_LAST_BAR] : 
					(ONLY_TAB || TAB_AND_TRANS ?
						tl.getMetricPosition((int) offset.mul(Tablature.SRV_DEN).toDouble())[0].getNumer() :
					smtl.getMetricPosition(offset)[0].getNumer());
				for (int j = bar + 1; j <= endBar; j++) {
					Rational currBarLen = Transcription.getMeter(j, mi);
					if (remainder.isGreaterOrEqual(currBarLen)) {
						subNoteDurs.add(currBarLen);
						subNoteDursOnsets.add(
							subNoteDursOnsets.get(subNoteDursOnsets.size()-1).add(currBarLen)
						); 
						remainder = remainder.sub(currBarLen);
					}
					else {
						if (!remainder.equals(Rational.ZERO)) {
							subNoteDurs.add(remainder);
						}
					}
				}
				// For each subnote
				Rational currOnset = onset;
				Rational currMetPos = metPos;
				for (int j = 0; j < subNoteDurs.size(); j++) {
					Rational currSubNoteDur = subNoteDurs.get(j);
					Rational currSubNoteDurOnset = subNoteDursOnsets.get(j);
//					List<Boolean> tripletInfo = (tripletOnsetPairs == null) ? null : 
//						TimeMeterTools.isTripletOnset(tripletOnsetPairs, currSubNoteDurOnset);
//					List<Object> noteData = getNoteData(
//						i, iTab, curr, currSubNoteDur, currOnset, currMetPos, 
//						mi, tripletInfo, tripletOnsetPairs, gridVal
//					);				
//					List<String[]> subNote = (List<String[]>) noteData.get(0);

					int lenBefore = pitchOctAccTie.size();
					updateDataLists(indBarOnsMpDurDots, pitchOctAccTie, false,
						currSubNoteDurOnset, 
						i, iTab, curr, currSubNoteDur, currOnset, currMetPos, 
						mi, tripletOnsetPairs, gridVal);
					int subNoteSize = pitchOctAccTie.size() - lenBefore;
					// Set subNoteInd to that of the last sub-subnote
					int subNoteInd = j == 0 ? pitchOctAccTie.size() - 1 : // last element of updated list
						pitchOctAccTie.size() - subNoteSize; // = first of updated part of the list
					String tie =
						j == 0 ? subNoteSize == 1 ? "i" : "m" :
						(j == subNoteDurs.size() - 1 ? (subNoteSize == 1 ? "t" : "m") : 
						"m");
					pitchOctAccTie.get(subNoteInd)[STRINGS.indexOf("tie")] = "tie='" + tie + "'";

//					// Set subNoteInd to that of the last sub-subnote
//					int subNoteInd = j == 0 ? subNote.size() - 1 : 0; 
//					String tie =
//						j == 0 ? subNote.size() == 1 ? "i" : "m" :
//						(j == subNoteDurs.size() - 1 ? (subNote.size() == 1 ? "t" : "m") : 
//						"m");
//					String tie = "";
//					if (j == 0) {
//						tie = (subNote.size() == 1) ? "i" : "m";
//						subNoteInd = subNote.size()-1;
//					}
//					else if (j == subNoteDurs.size() - 1) {
//						tie = (subNote.size() == 1) ? "t" : "m";
//					}
//					else {
//						tie = "m";
//					}
//					subNote.get(subNoteInd)[STRINGS.indexOf("tie")] = "tie='" + tie + "'";
//					pitchOctAccTie.addAll(subNote);
//					indBarOnsMpDurDots.addAll((List<Integer[]>) noteData.get(1));
					
					currOnset = currOnset.add(currSubNoteDur);
					currMetPos = Rational.ZERO;
				}
			}

			// 3. Final note in the voice: complete with rests if offset does not equal the piece end 
			NotationVoice nv = p.getScore().get(voice).get(0);
			NotationChord lastNc = nv.get(nv.size()-1);
			if ((lastNc.getMetricTime().equals(onset)) && !offset.equals(endOffset)) {
				// Take into account any chords: add rest only after the highest-pitched note in lastNc 
				int pitchHighestChordNote = lastNc.get(lastNc.size() - 1).getMidiPitch();
				if (lastNc.size() == 1 || (lastNc.size() > 1 && pitchHighestChordNote == pitch)) {
					// Add rest to fill up current bar (if applicable) 
					Rational restCurrBar = barEnd.sub(offset);
					if (restCurrBar.isGreater(Rational.ZERO)) {
						Rational metPosRestCurrentBar = metPos.add(dur);
//						List<Boolean> tripletInfo = 
//							(tripletOnsetPairs == null) ? null : 
//							TimeMeterTools.isTripletOnset(tripletOnsetPairs, offset);
//						List<Object> noteData = getNoteData(
//							-1, -1, new String[STRINGS.size()], restCurrBar, offset, 
//							metPosRestCurrentBar, mi, tripletInfo, tripletOnsetPairs, gridVal
//						);
//						pitchOctAccTie.addAll((List<String[]>) noteData.get(0));
//						indBarOnsMpDurDots.addAll((List<Integer[]>) noteData.get(1));
						
						updateDataLists(indBarOnsMpDurDots, pitchOctAccTie, false,
							offset, 
							-1, -1, new String[STRINGS.size()], restCurrBar, offset, metPosRestCurrentBar, 
							mi, tripletOnsetPairs, gridVal);
					}
					// Add bar rests for all remaining bars
					updateDataListsWithBarRests(indBarOnsMpDurDots, pitchOctAccTie, bar + 1, numBars);
//					for (int b = bar + 1; b <= numBars; b++) {
//						String[] restStr = new String[STRINGS.size()];
//						restStr[STRINGS.indexOf("dur")] = "dur='bar'";
//						pitchOctAccTie.add(restStr);
//						Integer[] restInt = new Integer[INTS.size()];
//						Arrays.fill(restInt, -1);
//						restInt[INTS.indexOf("bar")] = b;
//						restInt[INTS.indexOf("metPosNum")] = 0;
//						restInt[INTS.indexOf("metPosDen")] = 1;
//						restInt[INTS.indexOf("dur")] = -1; // n/a (bar rest)
//						restInt[INTS.indexOf("dots")] = -1; // n/a (bar rest)
//						indBarOnsMpDurDots.add(restInt);
//					}
				}
			}

			// 4. Add data to lists
			currVoiceStrs.addAll(pitchOctAccTie);
			currVoiceInts.addAll(indBarOnsMpDurDots);
//			noteAttribPerVoiceStrings.get(voice).addAll(pitchOctAccTie);
//			noteAttribPerVoiceInts.get(voice).addAll(indBarOnsMpDurDots);
		} // END OF LOOP OVER BNP
		if (verbose) {
			System.out.println(uniquePitchesNotInKey);
			System.out.println(pitchesNotInKey);
		}
		
		// Add <xml:id>s
//		// Note: voice, bar, seq number in bar, pitch+oct, metPos, index in trans, index in tab)
//		// Rest: voice, bar, seq number in bar, r, metPos
		for (int v = 0; v < numVoices; v++) {
			List<String[]> currVoiceStrs = noteAttribPerVoiceStrs.get(v);
			List<Integer[]> currVoiceInts = noteAttribPerVoiceInts.get(v);
			updateDataListsWithXMLIDs(currVoiceInts, currVoiceStrs, v);

//			int initBar = ints.get(0)[INTS.indexOf("bar")];
//			int seq = 0;
//			for (int j = 0; j < strings.size(); j++) {
//				String[] currStr = strings.get(j);
//				Integer[] currInt = ints.get(j);
//				int currBar = currInt[INTS.indexOf("bar")]; 
//				if (currBar > initBar) {
//					seq = 0;
//					initBar = currBar;
//				}
//				String ID = v + "." + currBar + "." + seq + ".";
//				String pi = currStr[STRINGS.indexOf("pname")];
//				String metPos = 
//					currInt[INTS.indexOf("metPosNum")] + "/" + currInt[INTS.indexOf("metPosDen")];
//				currStr[STRINGS.indexOf("metPos")] = metPos;
//				// In case of a note
//				if (pi != null) {
//					pi = String.valueOf(pi.charAt(pi.indexOf("'") + 1));
//					String oct = currStr[STRINGS.indexOf("oct")];
//					oct = String.valueOf(oct.charAt(oct.indexOf("'") + 1));
//					ID += pi + oct + "." + metPos + "." + currInt[INTS.indexOf("ind")] + "." + 
//						currInt[INTS.indexOf("indTab")];
//				}
//				// In case of a rest
//				else {
//					ID += "r" + "." + metPos;
//				}
//				currStr[STRINGS.indexOf("ID")] = "xml:id='" + ID + "'";
//				seq++;				
//			}
		}

		if (verbose) {
			int v = 0;
			List<String[]> currVoiceStrs = noteAttribPerVoiceStrs.get(v);
			List<Integer[]> currVoiceInts = noteAttribPerVoiceInts.get(v);
			for (int i = 0; i < currVoiceStrs.size(); i++) {
				System.out.println(
					Arrays.toString(currVoiceInts.get(i)) 
					+ "\n" + Arrays.toString(currVoiceStrs.get(i))
				);
			}
		}

		// Structure per bar, per voice, per note
		// List<List<List<T>>>			superlist: each element is a bar
		// List<List<T>>				sublist, bar-level: each element is a voice
		// List<T>						sub-sublist, voice-level: each element is a note
		List<List<List<Integer[]>>> attsInts = new ArrayList<>();
		List<List<List<String[]>>> attsStrs = new ArrayList<>();
		List<Integer> indsOfNewBar = new ArrayList<>(Collections.nCopies(numVoices, 0));
		for (int b = 1; b <= numBars; b++) {
			// Sublists, bar-level
			List<List<Integer[]>> barInts = new ArrayList<>();
			List<List<String[]>> barStrs = new ArrayList<>();
			for (int v = 0; v < numVoices; v++) {
				List<Integer[]> currVoiceInts = noteAttribPerVoiceInts.get(v);
				List<String[]> currVoiceStrs = noteAttribPerVoiceStrs.get(v);
				// Sublists, voice-level
				List<Integer[]> voiceInts = new ArrayList<>();
				List<String[]> voiceStrs = new ArrayList<>();
				for (int i = indsOfNewBar.get(v); i < currVoiceInts.size(); i++) {
					voiceInts.add(currVoiceInts.get(i));
					voiceStrs.add(currVoiceStrs.get(i));
					// If there is a next element and it is in the next bar
					if (i < currVoiceInts.size() - 1 && (currVoiceInts.get(i + 1)[INTS.indexOf("bar")] == b + 1)) {
						indsOfNewBar.set(v, i + 1);
						break;
					}
				}
				barInts.add(voiceInts);
				barStrs.add(voiceStrs);
			}
			attsInts.add(barInts);
			attsStrs.add(barStrs);
		}

		boolean useOld = false;
		if (useOld) {
			List<List<List<Integer[]>>> attsIntsAlt = new ArrayList<>();
			List<List<List<String[]>>> attsStrsAlt = new ArrayList<>();
			
			for (int b = 0; b < numBars; b++) {
				List<List<Integer[]>> voicesForThisBarInt = new ArrayList<>();
				List<List<String[]>> voicesForThisBarStr = new ArrayList<>();
				for (int v = 0; v < numVoices; v++) {
					voicesForThisBarInt.add(new ArrayList<Integer[]>());
					voicesForThisBarStr.add(new ArrayList<String[]>());
				}
				attsIntsAlt.add(voicesForThisBarInt);
				attsStrsAlt.add(voicesForThisBarStr);
			}
			// Populate lists
			for (int i = 0; i < numVoices; i++) {
				for (int j = 0; j < noteAttribPerVoiceInts.get(i).size(); j++) {
					Integer[] currInt = noteAttribPerVoiceInts.get(i).get(j);
					String[] currStr = noteAttribPerVoiceStrs.get(i).get(j);
					int currBar = currInt[INTS.indexOf("bar")];
					attsIntsAlt.get(currBar-1).get(i).add(currInt);
					attsStrsAlt.get(currBar-1).get(i).add(currStr);
				}
			}

			// Sort the content for each voice per bar by onset (for correct rendering of 
			// in-voice chords)
			// For each bar
			for (int i = 0; i < attsIntsAlt.size(); i++) {
				List<List<Integer[]>> currVoicesPerBarInt = attsIntsAlt.get(i);
				List<List<String[]>> currVoicesPerBarStr = attsStrsAlt.get(i);
				// For each voice
				for (int j = 0; j < currVoicesPerBarInt.size(); j++) {
					List<Integer[]> notesForCurrVoiceInt = currVoicesPerBarInt.get(j);
					List<String[]> notesForCurrVoiceStr = currVoicesPerBarStr.get(j);

					// Sort
					List<Integer[]> sortedInt = new ArrayList<Integer[]>(notesForCurrVoiceInt);
					ToolBox.sortBy(sortedInt, INTS.indexOf("metPosNum"), INTS.indexOf("metPosDen"), "division");
					List<Integer> newInds = new ArrayList<>();
					sortedInt.forEach(in -> newInds.add(notesForCurrVoiceInt.indexOf(in)));
					List<String[]> sortedStr = new ArrayList<>();
					newInds.forEach(ind -> sortedStr.add(notesForCurrVoiceStr.get(ind)));
//					List<String[]> sortedStr = new ArrayList<String[]>(notesForCurrVoiceStr);
//					ToolBox.sortByString(sortedStr, STRINGS.indexOf("ID"), "Rational", ".", "last");
////					ToolBox.sortByString(sortedStr, STRINGS.indexOf("metPos"), "Rational", null, null); // 26 maart
//					List<Integer> newInds = new ArrayList<>();
//					sortedStr.forEach(s -> newInds.add(notesForCurrVoiceStr.indexOf(s)));
//					List<Integer[]> sortedInt = new ArrayList<>();
//					newInds.forEach(ind -> sortedInt.add(notesForCurrVoiceInt.get(ind)));
//					// set metPos to null
////					sortedStr.forEach(s -> s[STRINGS.indexOf("metPos")] = null); // 26 maart
					// Replace with sorted lists 
					currVoicesPerBarInt.set(j, sortedInt);
					currVoicesPerBarStr.set(j, sortedStr);
				}
			}
			attsInts = attsIntsAlt;
			attsStrs = attsStrsAlt;
		}

//		List<Object> res = new ArrayList<>();
//		res.add(attsInts);
//		res.add(attsStrs);		
//		return res;
		return Arrays.asList(attsInts, attsStrs);
	}


	/**
	 * Splits a note or rest that crosses the triplet border in the part before the triplet 
	 * border (which is set to tripletClose) and the part after the triplet border (which is 
	 * set to tripletOpen).
	 * 
	 * NB: It is assumed that the splitting leads to no more complex notes than dotted notes.
	 *   
	 * @param firstPart
	 * @param remainder
	 * @param tripletLen
	 * @param tripletBorder
	 * @param onsetTripletBorder
	 * @param lastStr
	 * @param curr
	 * @param lastIn
	 * @param gridVal
	 * @param isRest
	 * 
	 * @return <ul>
	 *         <li>A List of two String[], the first element of which replaces the last of 
	 *             strs, and the second is to be added to strs</li>
	 *         <li>A List of two Integer[], whose elements are are to be added to ints</li>
	 *         </ul>     
	 */
	private static List<Object> handleNoteAcrossTripletBorderOLD(Rational firstPart, 
		Rational remainder, Rational tripletLen, Rational tripletBorder, 
		Rational onsetTripletBorder, String[] lastStr, /*String[] curr,*/ Integer[] lastIn, 
		Rational gridVal, boolean isRest) {

		// 1. Handle firstPart
		// a. In lastStr
		int durMEIFirstPart, numDotsFirstPart;
		if (!firstPart.equals(tripletLen)) {
			List<Rational> ufFirstPart = TimeMeterTools.getUnitFractions(firstPart.mul(TRIPLETISER), gridVal);
			durMEIFirstPart = ufFirstPart.get(0).getDenom();
			numDotsFirstPart = TimeMeterTools.getNumDots(ufFirstPart);
		}
		else {
			firstPart.reduce();
			durMEIFirstPart = firstPart.getDenom();
			numDotsFirstPart = 0;
		}
		// Adapt dur, dots, tie
		lastStr[STRINGS.indexOf("dur")] = "dur='" + durMEIFirstPart + "'";
		if (numDotsFirstPart > 0 ) {
			lastStr[STRINGS.indexOf("dots")] = "dots='" + numDotsFirstPart + "'";
		}
		if (!isRest) {
			// If lastStr has 
			// (a) tie='i' or 'm': OK (remainder set to 'm')
			// (b) tie='t': NOK, set to 'm' (remainder set to 't')
			if (lastStr[STRINGS.indexOf("tie")].equals("tie='t'")) {
				lastStr[STRINGS.indexOf("tie")] = "tie='m'";
			}
		}
		// b. In lastInt
		// Adapt dur, dots, tripletOpen, tripletMid, tripletClose
		lastIn[INTS.indexOf("dur")] = durMEIFirstPart;
		if (numDotsFirstPart > 0 ) {
			lastIn[INTS.indexOf("dots")] = numDotsFirstPart;
		}
		if (!firstPart.equals(tripletLen)) {
			lastIn[INTS.indexOf("tripletOpen")] = 0;
			lastIn[INTS.indexOf("tripletMid")] = 0;
			lastIn[INTS.indexOf("tripletClose")] = 1;
		}
		else {
			lastIn[INTS.indexOf("tripletOpen")] = 0;
			lastIn[INTS.indexOf("tripletMid")] = 0;
			lastIn[INTS.indexOf("tripletClose")] = 0;
		}

		// 2. Handle remainder
		// a. In afterLastStr
		String[] afterLastStr = Arrays.copyOf(lastStr, lastStr.length); // lastStr was curr 08.04
//		String[] afterLastStr = Arrays.copyOf(curr, curr.length);
		int durMEIRemainder, numDotsRemainder;
		if (!remainder.equals(tripletLen)) {
			List<Rational> ufRemainder = TimeMeterTools.getUnitFractions(remainder.mul(TRIPLETISER), gridVal);
			durMEIRemainder = ufRemainder.get(0).getDenom();
			numDotsRemainder = TimeMeterTools.getNumDots(ufRemainder);
		}
		else {
			remainder.reduce();
			durMEIRemainder = remainder.getDenom();
			numDotsRemainder = 0;
		}
		// Adapt dur, dots, tie
		afterLastStr[STRINGS.indexOf("dur")] = "dur='" + durMEIRemainder + "'";
		if (numDotsRemainder > 0 ) {
			afterLastStr[STRINGS.indexOf("dots")] = "dots='" + numDotsRemainder + "'";
		}
		if (!isRest) {
			// If lastStr has 
			// (a) tie='i' or 'm': NOK, set to 'm'
			// (b) tie='t': OK
			if (lastStr[STRINGS.indexOf("tie")].equals("tie='i'")) {
				afterLastStr[STRINGS.indexOf("tie")] = "tie='m'";
			}
		}

		// b. In afterLastIn
		Integer[] afterLastIn = Arrays.copyOf(lastIn, lastIn.length);
		// Adapt dur, dots, tripletOpen, tripletMid, tripletClose
		afterLastIn[INTS.indexOf("dur")] = durMEIRemainder;
		if (numDotsRemainder > 0 ) {
//		if (numDotsFirstPart > 0 ) {	
			afterLastIn[INTS.indexOf("dots")] = numDotsRemainder;
		}
		if (!remainder.equals(tripletLen)) {
			afterLastIn[INTS.indexOf("tripletOpen")] = 1;
			afterLastIn[INTS.indexOf("tripletMid")] = 0;
			afterLastIn[INTS.indexOf("tripletClose")] = 0;
		}
		else {
			afterLastIn[INTS.indexOf("tripletOpen")] = 0;
			afterLastIn[INTS.indexOf("tripletMid")] = 0;
			afterLastIn[INTS.indexOf("tripletClose")] = 0;
		}
		// Adapt onset, metPos
		Rational o = onsetTripletBorder;
		o.reduce();
		Rational m = tripletBorder;
		m.reduce();
		afterLastIn[INTS.indexOf("onsetNum")] = o.getNumer();
		afterLastIn[INTS.indexOf("onsetDen")] = o.getDenom();
		afterLastIn[INTS.indexOf("metPosNum")] = m.getNumer();
		afterLastIn[INTS.indexOf("metPosDen")] = m.getDenom();
		
		return Arrays.asList(new Object[]{
			Arrays.asList(new String[][]{lastStr, afterLastStr}),
			Arrays.asList(new Integer[][]{lastIn, afterLastIn})}
		);
	}


	/**
	 * Gets the note information for the note (or rest) at index i, given as input a String[] 
	 * containing only the attributes pname, oct, and accid (or only <code>null</code> in case
	 * of a rest). 
	 *               
	 * @param ind
	 * @param indTab
	 * @param curr
	 * @param argDur Is untripletised (has actual value).
	 * @param argOnset Is untripletised (has actual value).
	 * @param argMetPos Is untripletised (has actual value).
	 * @param mi
	 * @param tripletInfo
	 * @param tripletOnsetPairs
	 * @param gridVal
	 * @return <ul>
	 * 	       <li>As element 0: a List<Integer[]> containing, for each unit fraction the note (rest) 
	 *             can be divided into, an Integer[] containing the attributes as listed in INTS.</li>
	 *         <li>As element 1: a List<String[]> containing, for each unit fraction the note (rest) 
	 *             can be divided into, a String[] containing the attributes as listed in STRINGS.</li>
	 *         </ul>      
	 *         In case of a simple (non-dotted, non-compound) or dotted note (rest), the lists returned 
	 *         have only one element; in case of a non-dotted compound note (e.g., a half tied to an 
	 *         eighth), the lists have more than one element.
	 */
	private static List<Object> getNoteDataOLD(int ind, int indTab, String[] curr, Rational argDur, 
		Rational argOnset, Rational argMetPos, List<Integer[]> mi, List<Boolean> tripletInfo, 
		List<Rational[]> tripletOnsetPairs, Rational gridVal) {

		List<Integer[]> ints = new ArrayList<>(); // was indBarOnsMpDurDots
		List<String[]> strs = new ArrayList<>(); // was pitchOctAccTie

		// If the note or rest has a triplet onset time, argDur is tripletised (i.e., 
		// given its nominal (shown) value instead of its actual value). As a consequence, 
		// uf and durAsRat are also tripletised. argOnset and argMetPos remain untripletised 
		// (i.e., keep their actual value), so currOnset and currMetPos are too
		if (tripletInfo != null && tripletInfo.contains(Boolean.TRUE)) {
			argDur = argDur.mul(TRIPLETISER);
		}
		List<Rational> uf = TimeMeterTools.getUnitFractions(argDur, gridVal);
		int numDots = TimeMeterTools.getNumDots(uf);
		boolean isSimple = (uf.size() == 1 && numDots == 0);
		boolean isDotted = numDots > 0;
		boolean isNonDottedCompound = (uf.size() > 1 && numDots == 0);
		boolean isRest = curr[STRINGS.indexOf("pname")] == null;

		Rational currOnset = argOnset;
		Rational currMetPos = argMetPos;
		// Iterate through the unit fractions. In the case of a simple or dotted note, 
		// the for-loop breaks at the end of i = 0  
		for (int i = 0; i < uf.size(); i++) {
			Rational durAsRatUndotted = uf.get(i);
			Rational durAsRat = durAsRatUndotted;
//			Rational durAsRat = uf.get(i);
			if (isDotted) {
				durAsRat = durAsRat.add(uf.get(1));
			}

			// Determine dur; allow for breve (2/1) and long (4/1)
			int dur = -1;
			Rational r = isSimple || isDotted ? durAsRatUndotted : durAsRat;
			if (r.isLessOrEqual(Rational.ONE)) {
				dur = r.getDenom();
			}
			else if (r.equals(new Rational(2, 1))) {
				dur = BREVE;
			}
			else if (r.equals(new Rational(4, 1))) {
				dur = LONG;
			}
//			if (isSimple || isDotted) {
//				if (uf.get(i).isLessOrEqual(Rational.ONE)) {
//					dur = uf.get(i).getDenom();
//				}
//				else if (uf.get(i).equals(new Rational(2, 1))) {
//					dur = BREVE;
//				}
//				else if (uf.get(i).equals(new Rational(4, 1))) {
//					dur = LONG;
//				}
//			}
//			else {
//				if (durAsRat.isLessOrEqual(Rational.ONE)) {
//					dur = durAsRat.getDenom();
//				}
//				else if (durAsRat.equals(new Rational(2, 1))) {
//					dur = BREVE;
//				}
//				else if (durAsRat.equals(new Rational(4, 1))) {
//					dur = LONG;
//				}
//			}

			// Complete uf String[] with tie, dur, dots; add to strs
			String[] copyOfCurr = Arrays.copyOf(curr, curr.length);
			if (isNonDottedCompound && !isRest) { 
//			if (isNonDottedCompound) { 
//				if (!isRest) {
//					String tie = (i == 0 ? "i" : ((i > 0 && i < uf.size() - 1) ? "m" : "t");
				copyOfCurr[STRINGS.indexOf("tie")] = 
					"tie='" + (i == 0 ? "i" : ((i > 0 && i < uf.size() - 1) ? "m" : "t")) + "'";
//				}
			}
//			String durStr = 
//				(dur > 0) ? Integer.toString(dur) : ((dur == BREVE ? "breve" : "long"));
			copyOfCurr[STRINGS.indexOf("dur")] = 
//				"dur='" + durStr + "'";
				"dur='" + (dur > 0 ? Integer.toString(dur) : (dur == BREVE ? "breve" : "long")) + "'";
			if (isDotted) {
//			if (isSimple || isDotted) {
//				if (numDots != 0) {
				copyOfCurr[STRINGS.indexOf("dots")] = "dots='" + numDots + "'";
//				}
			}
			strs.add(copyOfCurr);

			// Create uf Integer[] and initialise with zeros; fill with all but tripletOpen, 
			// tripletMid, tripletClose, and beamOpen and beamClose
			Integer[] in = new Integer[INTS.size()];
			Arrays.fill(in, 0); //check if all are filled and if not if remaining values are correctly 0
//			if (!isRest) {
			in[INTS.indexOf("ind")] = !isRest ? ind : -1;
			in[INTS.indexOf("indTab")] = !isRest ? indTab : -1;
//			}
//			int currBar = TimeMeterTools.getMetricPosition(currOnset, mi)[0].getNumer();
			in[INTS.indexOf("bar")] = TimeMeterTools.getMetricPosition(currOnset, mi)[0].getNumer();
//			in[INTS.indexOf("bar")] = currBar;
			in[INTS.indexOf("onsetNum")] = currOnset.getNumer();
			in[INTS.indexOf("onsetDen")] = currOnset.getDenom();
			in[INTS.indexOf("metPosNum")] = currMetPos.getNumer();
			in[INTS.indexOf("metPosDen")] = currMetPos.getDenom();
			in[INTS.indexOf("dur")] = dur;
			in[INTS.indexOf("dots")] = numDots;
//			// Set tripletOpen, tripletMid, and tripletClose
//			in[INTS.indexOf("tripletOpen")] = 0;
//			in[INTS.indexOf("tripletMid")] = 0;
//			in[INTS.indexOf("tripletClose")] = 0;
			
			ints.add(in);

			List<Boolean> openMidClose = (tripletOnsetPairs == null) ? null : 
				TimeMeterTools.isTripletOnset(tripletOnsetPairs, currOnset);
			if (tripletOnsetPairs != null) {
				// Get any triplet information
				String[] last = strs.get(strs.size()-1);
				Rational[] top = TimeMeterTools.getExtendedTripletOnsetPair(
					currOnset, tripletOnsetPairs/*, diminution*/
				);
				Rational currTripletLen = null;
//				Rational currTripletOpenOnset = null;
//				Rational metPosTripletOpen = null;
				Rational tripletBorder = null;
				Rational onsetTripletBorder = null;
				Rational offset = null;
				Integer[] secondIn = null;
				if (top != null) {
					Rational currTripletOpenOnset = top[0];
					Rational metPosTripletOpen = TimeMeterTools.getMetricPosition(currTripletOpenOnset, mi)[1];
					currTripletLen = top[3];
					tripletBorder = metPosTripletOpen.add(currTripletLen);
					onsetTripletBorder = currTripletOpenOnset.add(currTripletLen);
					offset = currMetPos.add(durAsRat.mul(DETRIPLETISER));
				}
				// If currOnset is the second part of a non-dotted compound note or rest that 
				// falls after tripletCloseOnset: set tripletCloseOnset to true 
				// NB: This does not apply to isSimple or isDotted, where there is no second part
				// E.g.: last H in voice 0 = tripletCloseOnset; last Q in voice 1, which
				// is tied to the W before, falls after it 
				// voice 0: W       H   H   H   H     |
				// voice 1: W       H.    W       (Q) |
				// (This happens in 5263_12_in_exitu_israel_de_egipto_desprez-3, bar 77, voice 2)
				if (isNonDottedCompound) {
					Rational[] topOnset = TimeMeterTools.getExtendedTripletOnsetPair(
						argOnset, tripletOnsetPairs
					);
					Rational tripletOpenOnset = (topOnset != null) ? topOnset[0] : null;			
					Rational tripletCloseOnset = (topOnset != null) ? topOnset[1] : null;
					if (topOnset != null && currOnset.isGreater(tripletCloseOnset) && 
						currOnset.isLess(tripletOpenOnset.add(currTripletLen))) {
						if (verbose) System.out.println("within triplet but after tripletCloseOnset --> tripletClose");
						openMidClose.set(2, true);
					}
				}

				// If the note is tripletOpen
				if (openMidClose.get(0) == true) {
					if (verbose) System.out.println("is tripletOpen");
					in[INTS.indexOf("tripletOpen")] = 1;					
					// a. If note ends on triplet border: set to tripletClose
					if (offset.equals(tripletBorder)) {
						if (verbose) System.out.println("ends on tripletBorder --> tripletClose");
						in[INTS.indexOf("tripletClose")] = 1;
					}
					// b. If note ends after triplet border: split at border and set the first
					// part to tripletClose and the second to tripletOpen
					if (offset.isGreater(tripletBorder)) {
						System.out.println("YES FIRST");
						if (verbose) System.out.println("across triplet border --> split");
						Rational firstPart = tripletBorder.sub(currMetPos);
						Rational remainder = offset.sub(tripletBorder);
						List<Object> res = 
							splitAcrossTripletBorder(firstPart, remainder, currTripletLen, 
							tripletBorder, onsetTripletBorder, last, curr, in, gridVal, isRest);
						List<String[]> lastTwoStr = (List<String[]>) res.get(0);
						List<Integer[]> lastTwoInt = (List<Integer[]>) res.get(1);
						strs.set(strs.size()-1, lastTwoStr.get(0));
						strs.add(lastTwoStr.get(1));

//						in = lastTwoInt.get(0);
						secondIn = lastTwoInt.get(1);

						ints.set(ints.size()-1, lastTwoInt.get(0));
						if (secondIn != null) {
							ints.add(secondIn);
						}
					}
				}
				// If the note is tripletMid
				if (openMidClose.get(1) == true) {
					if (verbose) System.out.println("is tripletMid");
					in[INTS.indexOf("tripletMid")] = 1;	
					// a. If note ends on triplet border: set to tripletClose
					if (offset.equals(tripletBorder)) {
						if (verbose) System.out.println("ends on tripletBorder --> tripletClose");
						in[INTS.indexOf("tripletClose")] = 1;
					}
					// b. If note ends after triplet border: split at border and set the first
					// part to tripletClose and the second to tripletOpen
					if (offset.isGreater(tripletBorder)) {
						System.out.println("YES SECOND");
						if (verbose) System.out.println("across triplet border --> split");
						Rational firstPart = tripletBorder.sub(currMetPos);
						Rational remainder = offset.sub(tripletBorder);
						List<Object> res = 
							splitAcrossTripletBorder(firstPart, remainder, currTripletLen,
							tripletBorder, onsetTripletBorder, last, curr, in, gridVal, isRest);
						List<String[]> lastTwoStr = (List<String[]>) res.get(0);
						List<Integer[]> lastTwoInt = (List<Integer[]>) res.get(1);
						strs.set(strs.size()-1, lastTwoStr.get(0));
						strs.add(lastTwoStr.get(1));
//						in = lastTwoInt.get(0);
						secondIn = lastTwoInt.get(1);
						
						ints.set(ints.size()-1, lastTwoInt.get(0));
						if (secondIn != null) {
							ints.add(secondIn);
						}
					}
				}
				// If the note is tripletClose
				if (openMidClose.get(2) == true) {
					if (verbose) System.out.println("is tripletClose");
					in[INTS.indexOf("tripletClose")] = 1;
					// a. If note ends on triplet border: no action required
					// b. If note ends after triplet border: split at border and set the first
					// part to tripletClose and the second to tripletOpen
					if (offset.isGreater(tripletBorder)) {
						System.out.println("YES THIRD");
						if (verbose) System.out.println("across triplet border --> split");
						Rational firstPart = tripletBorder.sub(currMetPos);
						Rational remainder = offset.sub(tripletBorder);
						List<Object> res = 
							splitAcrossTripletBorder(firstPart, remainder, currTripletLen,
							tripletBorder, onsetTripletBorder, last, curr, in, gridVal, isRest);
						List<String[]> lastTwoStr = (List<String[]>) res.get(0);
						List<Integer[]> lastTwoInt = (List<Integer[]>) res.get(1);
						strs.set(strs.size()-1, lastTwoStr.get(0));
						strs.add(lastTwoStr.get(1));
						
//						in = lastTwoInt.get(0);
						secondIn = lastTwoInt.get(1);
						
						ints.set(ints.size()-1, lastTwoInt.get(0));
						if (secondIn != null) {
							ints.add(secondIn);
						}
					}
				}
			}
//			ints.add(in);
//			if (secondIn != null) {
//				ints.add(secondIn);
//			}
			if (isSimple || isDotted) {
				if (verbose) System.out.println("break: isSimple || isDotted");
				break;
			}
			else {
				if (openMidClose != null && openMidClose.contains(true)) {
					durAsRat = durAsRat.mul(DETRIPLETISER);
				}
				currOnset = currOnset.add(durAsRat);
				currMetPos = currMetPos.add(durAsRat);
			}
		}
		// If the last element in ints is a tripletClose: check if the elements up to tripletOpen 
		// are a single note occupying the whole tripletLen, all rests, or all tied notes. If so,
		// replace the triplet value by its non-triplet value with tripletLen
		int indLast = ints.size()-1;
		Integer[] last = ints.get(indLast);
		if (last[INTS.indexOf("tripletClose")] == 1) { // && currIndBarOnsMpDurDots.size() > 1) {
			List<Boolean> rests = new ArrayList<>();
			List<Boolean> ties = new ArrayList<>();
			// Find accompanying tripletOpen
			int indOpen = -1;
			for (int j = indLast; j >= 0; j--) { // was indLast-1
				boolean isTripletOpen = ints.get(j)[INTS.indexOf("tripletOpen")] == 1;
				// If the element at index j is a rest: add
				if (strs.get(j)[STRINGS.indexOf("pname")] == null) {
					rests.add(true);
				}
				else {
					rests.add(false);
				}
				// If the element at index j is a tie: add
				String tieStr = strs.get(j)[STRINGS.indexOf("tie")]; 
				if (tieStr != null) {
					// In order for the triplet to be fully tied, the last element cannot be i;
					// the middle element cannot be i or t; and the first element cannot be t 
					if (j == indLast && (tieStr.equals("tie='m'") || tieStr.equals("tie='t'")) ||
						(j < indLast && !isTripletOpen) && tieStr.equals("tie='m'") ||
						isTripletOpen && (tieStr.equals("tie='i'") || tieStr.equals("tie='m'"))) {
						ties.add(true);
					}
					else {
						ties.add(false);
					}
				}
				// Break when tripleOpen is reached
				if (isTripletOpen) {
					indOpen = j;
					break;
				}
			}
			// If indOpen is reached and the triplet consists of a single note occupying the 
			// whole tripletLen, only of rests, or only of tied notes
			if (indOpen != -1 && !rests.contains(false) || indOpen != -1 && !ties.contains(false)) {
				if (verbose) System.out.println("rest/note of tripletLen replaced by untripletised version");
				boolean isTripletLen = indOpen == indLast; 
				Rational tripletisedDur = Rational.ZERO;
				for (int j = indOpen; j <= indLast; j++) {
					int durAsInt = ints.get(j)[INTS.indexOf("dur")];
					Rational toAdd = 
						(durAsInt > 0) ? new Rational(1, durAsInt) : // whole or shorter
						new Rational(durAsInt*-2, 1); // breve or long	
					tripletisedDur = tripletisedDur.add(toAdd);
				}
				// If the rest/note is not of tripletLen, it must be detripletised
				if (!isTripletLen) {
					tripletisedDur = tripletisedDur.mul(DETRIPLETISER);
				}
				// Change items at indOpen
				ints.get(indOpen)[INTS.indexOf("dur")] = tripletisedDur.getDenom();
				ints.get(indOpen)[INTS.indexOf("dots")] = -1;
				ints.get(indOpen)[INTS.indexOf("tripletOpen")] = 0;
				// If the rest/note is of tripletLen, tripletClose, which will be set to 1,
				// must be reset too
				if (isTripletLen) {
					ints.get(indOpen)[INTS.indexOf("tripletClose")] = 0;
				}
				//
				strs.get(indOpen)[STRINGS.indexOf("dur")] = 
					"dur='" + tripletisedDur.getDenom() + "'";
				strs.get(indOpen)[STRINGS.indexOf("dots")] = null;
				if (ties.size() > 0 && !ties.contains(false)) {
					// Tie for the first element (which can only be i or m) must be retained 
					// only if tie for the last element (which can only be m or t) is m
					if (strs.get(indLast)[STRINGS.indexOf("tie")].equals("tie='t'")) {
						strs.get(indOpen)[STRINGS.indexOf("tie")] = null;
					}
				}
				// Remove items at indices after indOpen
				ints = ints.subList(0, indOpen+1);
				strs = strs.subList(0, indOpen+1);
			}
		}
		return Arrays.asList(new Object[]{ints, strs});
	}


	private static List<Object> beamOLD(Tablature tab, List<Object> data, List<Integer[]> mi, 
		List<Rational[]> tripletOnsetPairs, List<List<Integer>> mismatchInds, int numVoices) {
		System.out.println(">>> beam() called");

		// Organise the information (i) per voice, (ii) per bar for the python beaming script
		List<List<String>> unbeamedBarsPerVoice = new ArrayList<>();
		for (int i = 0; i < numVoices; i++) {
			List<String> empty = new ArrayList<>();
			empty.add("voice=" + i + "\r\n");
			unbeamedBarsPerVoice.add(empty);
		}
			
		List<List<List<Integer[]>>> dataInt = (List<List<List<Integer[]>>>) data.get(0);
//		dataInt = dataInt.subList(firstBar-1, lastBar);
		List<List<List<String[]>>> dataStr = (List<List<List<String[]>>>) data.get(1);
//		dataStr = dataStr.subList(firstBar-1, lastBar);
			
		// For each bar
		Timeline tla = tab.getEncoding().getTimelineAgnostic();
		for (int i = 0; i < dataStr.size(); i++) {
			int bar = i+1;
			if (verbose) System.out.println("bar = " + (i+1));
			List<List<Integer[]>> currBarInt = dataInt.get(i);
			List<List<String[]>> currBarStr = dataStr.get(i);
			
//			System.out.println("X X X X X X X X X X X X");
//			System.out.println("bar = " + bar);
				
			// For each voice
			for (int j = 0; j < currBarInt.size(); j++) {
				if (verbose) System.out.println("voice = " + j);
				List<Integer[]> currBarCurrVoiceInt = currBarInt.get(j);
				List<String[]> currBarCurrVoiceStr = currBarStr.get(j);
					
//				System.out.println("voice = " + j);
//				System.out.println("ints");
//				currBarCurrVoiceInt.forEach(in -> System.out.println(Arrays.asList(in)));
//				System.out.println("strs");
//				currBarCurrVoiceStr.forEach(in -> System.out.println(Arrays.asList(in)));
				
				// Add current bar to list corresponding to current voice
				int diminution = 1;
				if (TAB_AND_TRANS) {
//				if (mi.get(0).length == Tablature.MI_SIZE) {
					diminution = tla.getDiminution(bar);
//					diminution = Tablature.getDiminution(bar, mi);
				}
//				System.out.println("-->");
//				System.out.println("bar i = " + i);
//				System.out.println("voice j = " + j);
//				for (Integer[] in : currBarCurrVoiceInt) {
//					System.out.println(Arrays.asList(in));
//				}
//				for (String[] in : currBarCurrVoiceStr) {
//					System.out.println(Arrays.asList(in));
//				}
				List<String> barList = 
					getBar(currBarCurrVoiceInt, currBarCurrVoiceStr, tripletOnsetPairs,
					mismatchInds, j, diminution);

				String barListAsStr = "";
				Rational currMeter = Transcription.getMeter(bar, mi); 
				barListAsStr += 
					"meter='" + currMeter.getNumer() + "/" + currMeter.getDenom() + "'" + "\r\n";
				for (int k = 0; k < barList.size(); k++) {
					barListAsStr += barList.get(k) + "\r\n";
//					// Add line break after all but final note
//					if (!(i == dataStr.size()-1 && j == currBarInt.size()-1 && 
//						k == barList.size()-1)) {
//						barListAsStr += "\r\n";
//					}
				}
				unbeamedBarsPerVoice.get(j).add(barListAsStr);
			}
		}
		// Store unbeamedBarsPerVoice
		// NB: the stored file ends with a line break
		StringBuilder strb = new StringBuilder();
		for (List<String> l : unbeamedBarsPerVoice) {
			for (String s : l) {
				strb.append(s);
			}
		}
		String filePath = getPythonScriptPath();
//		String filePath = MEITemplatePath; //path; // scriptPathPythonMEI;
		String notesFileName = filePath + "notes.txt";
		File notesFile = new File(notesFileName);
		ToolBox.storeTextFile(strb.toString(), notesFile);
//		System.out.println(strb.toString());

		// Call the beaming script and get output; delete
		// NB: the output of the beaming script does not end with a line break, but 
		// PythonInterface.getScriptOutput() adds one to the end of it
		String beamed = "";
//		try {
			beamed = 
				PythonInterface.runPythonFileAsScript(new String[]{
				"python", getPythonScriptPath() + "beam.py", notesFileName});
//			beamed = PythonInterface.getScriptOutput(new String[]{
//					"python", scriptPathPythonMEI + "beam.py", notesFileName});
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

//		System.out.println(pythonScriptPath);
//		System.out.println("* * * * * *");
//		System.out.println(beamed);
//		System.out.println("* * * * * *");
//		System.exit(0);
				
//		System.out.println(notesFile);

		notesFile.delete();

		// Re-organise the information (i) per bar, (ii) per voice so that it is the same
		// again as dataStr and dataInt
		List<List<List<String>>> beamedReorganised = new ArrayList<>();
		// Get each voice as a single string (a list)
		List<String> voices = Arrays.asList(beamed.split("end of voice" + "\r\n"));
		// Get, for each voice, each bar as a single string (a list)
		List<List<String>> barsPerVoice = new ArrayList<>();
		for (String currVoice : voices) {
			barsPerVoice.add(Arrays.asList(currVoice.split("end of bar" + "\r\n")));
		}
//		for (List<String> l : barsPerVoice) {
//			System.out.println(l);
//		}
//		System.exit(0);
		// Make, per bar, the empty voices
		int numBars = barsPerVoice.get(0).size();
		for (int i = 0; i < numBars; i++) {
			beamedReorganised.add(new ArrayList<>());
		}
		// Fill, per bar, the voices
		for (int bar = 0; bar < numBars; bar++) {
			for (int voice = 0; voice < numVoices; voice++) {
//				System.out.println(voice);
//				System.out.println(bar);
//				System.out.println("joe");
//				System.out.println(barsPerVoice.size());
				String notesCurrBarCurrVoice = barsPerVoice.get(voice).get(bar);
//				// Remove final line break
//				notesCurrBarCurrVoice = 
//					notesCurrBarCurrVoice.substring(0, notesCurrBarCurrVoice.lastIndexOf("\r\n"));
				List<String> notesAsOneString = 
					Arrays.asList(notesCurrBarCurrVoice.split("\r\n"));
				beamedReorganised.get(bar).add(notesAsOneString);
			}
		}
			
		// Align beamedReorganised and dataInt and set beamOpen and beamClose
		for (int bar = 0; bar < beamedReorganised.size(); bar++) {
			List<List<String>> voicesCurrBar = beamedReorganised.get(bar);
			for (int voice = 0; voice < voicesCurrBar.size(); voice++) {
				List<String> notesCurrBarCurrVoice = voicesCurrBar.get(voice);
				for (int note = 0; note < notesCurrBarCurrVoice.size(); note++) {
					String noteCurrBarCurrVoice = notesCurrBarCurrVoice.get(note);
					if (noteCurrBarCurrVoice.startsWith("<beam>")) {
						dataInt.get(bar).get(voice).get(note)[INTS.indexOf("beamOpen")] = 1;
					}
					if (noteCurrBarCurrVoice.endsWith("</beam>")) {
						dataInt.get(bar).get(voice).get(note)[INTS.indexOf("beamClose")] = 1;
					}
				}
			}
		}
//		System.out.println("TERING");
		return Arrays.asList(dataInt, dataStr);
//		return dataInt;
	}


	private static List<List<String>> getTransBarsOLD(Tablature tab, List<Object> data, List<Integer[]> mi,
			List<Rational[]> tripletOnsetPairs, List<List<Integer>> mismatchInds, 
			int numVoices, String app) {			
			System.out.println("\r\n>>> getTransBars() called");

			List<List<String>> transBars = new ArrayList<>();
//			for (List<Integer> l : mismatchInds) {
//				System.out.println(l);
//			}
			Encoding enc = tab.getEncoding();
			List<Event> events = enc.getEvents();
			
//			System.out.println("incorrect:");
//			System.out.println(mismatchInds.get(1));
//			System.out.println("overlooked:");
//			System.out.println(mismatchInds.get(2));
//			System.out.println("superfluous:");
//			System.out.println(mismatchInds.get(3));
//			System.out.println("half:");
//			System.out.println(mismatchInds.get(4));

			List<List<List<Integer[]>>> dataInt = (List<List<List<Integer[]>>>) data.get(0);
//			dataInt = dataInt.subList(firstBar-1, lastBar);
			List<List<List<String[]>>> dataStr = (List<List<List<String[]>>>) data.get(1);
//			dataStr = dataStr.subList(firstBar-1, lastBar);
			
			// TODO münchen: move glossary extraction to own method
			boolean extractOrnaments = app.equals("halcyon") ? false : true;
			if (extractOrnaments) {
				// Reorganise: list, per voice, the notes in that voice
				List<List<Integer[]>> dataIntPerVoice = new ArrayList<>();
				List<List<String[]>> dataStrPerVoice = new ArrayList<>();
				int numBars = dataInt.size();
				for (int voice = 0; voice < numVoices; voice++) {
					List<Integer[]> notesCurrVoiceInt = new ArrayList<>();
					List<String[]> notesCurrVoiceStr = new ArrayList<>();
					for (int bar = 0; bar < numBars; bar++) {
						notesCurrVoiceInt.addAll(dataInt.get(bar).get(voice));
						notesCurrVoiceStr.addAll(dataStr.get(bar).get(voice));
					}
					dataIntPerVoice.add(notesCurrVoiceInt);
					dataStrPerVoice.add(notesCurrVoiceStr);
				}
			
				Integer[][] btp = tab.getBasicTabSymbolProperties();
				List<Integer> ornInds = mismatchInds.get(Transcription.ORNAMENTATION_IND);
				List<String[]> ornaments = new ArrayList<>();
				ornaments.add(new String[]{"piece="+tab.getName()});
				for (int i = 0 ; i < numVoices; i++) {
//					System.out.println("voice " + i);
					List<Integer[]> currVoiceDataInt = dataIntPerVoice.get(i);
					int numNotes = currVoiceDataInt.size();
					for (int j = 0; j < numNotes - 1; j++) {
//						System.out.println("note " + j);
						Integer[] currNoteDataInt = currVoiceDataInt.get(j);
						Integer[] nextNoteDataInt = currVoiceDataInt.get(j+1);

						// If the next note is ornamental: build ornament. ornament consists of 
						// (1) the opening non-ornamental note (left border note)
						// (2) the ornamental note(s)
						// (3) the closing non-ornamental note (right border note)
						if (ornInds.contains(nextNoteDataInt[INTS.indexOf("indTab")])) {
							List<String> ornament = new ArrayList<>();
							List<String> loc = new ArrayList<>();
							// Add left border note
							int currIndTab = currNoteDataInt[INTS.indexOf("indTab")];
							int currPitch;
							int startPitch;
							String currRs;
							// If currIndTab is -1, currNoteDataInt represents a rest, and
							// the ornament starts with a rest (+ interval 0)
							if (currIndTab == -1) {
								currPitch = -1;
								startPitch = -1;
								currRs = "R";
							}
							else {
								currPitch = btp[currIndTab][Tablature.PITCH];
								startPitch = currPitch;
								int dur = btp[currIndTab][Tablature.MIN_DURATION];
								int dots = currNoteDataInt[INTS.indexOf("dots")];
								// getRhythmSymbol() needs the undotted dur
								dur = TimeMeterTools.getUndottedNoteLength(dur, dots);
								
								String e = events.get(btp[currIndTab][Tablature.TAB_EVENT_SEQ_NUM]).getEncoding();
								RhythmSymbol rs = Symbol.getRhythmSymbol(
									e.substring(0, e.indexOf(Symbol.SYMBOL_SEPARATOR))
								);
//								System.out.println(e);
//								System.out.println(rs);
								
								currRs = Symbol.getRhythmSymbol(
									dur, 
									e.startsWith(Symbol.CORONA_INDICATOR), 
									rs.getBeam(), 
									rs.isTriplet()
								).getSymbol() + ".".repeat(dots);
							}
							// Add current RS
							ornament.add(currRs);
							// Add ornamental note(s) and right border note
							for (int k = j + 1; k < numNotes; k++) {
								nextNoteDataInt = currVoiceDataInt.get(k);
								int nextIndTab = nextNoteDataInt[INTS.indexOf("indTab")];
								if (nextIndTab == -1) {
									System.out.println(Arrays.asList(nextNoteDataInt));
									System.out.println("k " + k);
									System.out.println(ornament);
								}
								// If nextIndTab is -1, nextNoteDataInt represents a rest, and
								// the ornament ends with a rest (+ interval 0)
								int nextPitch = nextIndTab == -1 ? -1 : btp[nextIndTab][Tablature.PITCH];
								if (startPitch == -1) {
									startPitch = nextPitch;
								}
								String nextRs;
								if (nextIndTab != -1) {
									int nextDur = btp[nextIndTab][Tablature.MIN_DURATION];
									int nextDots = nextNoteDataInt[INTS.indexOf("dots")];
									// getRhythmSymbol() needs the undotted dur
									nextDur = TimeMeterTools.getUndottedNoteLength(nextDur, nextDots);
//									System.out.println(nextDur);
//									if (j == 223 || j == 224 || j == 225 || j == 226 || j == 227) {
//										System.out.println(Arrays.asList(nextNoteDataInt));
//										System.out.println(Arrays.asList(dataStrPerVoice.get(i).get(k)));
//										String s = enc.getListsOfSymbols().get(1).get(nextIndTab);
//										int eInd = btp[nextIndTab][Tablature.TAB_EVENT_SEQ_NUM];
//										System.out.println(enc.getListsOfSymbols().get(0));
//										String e = events.get(eInd).getEncoding();
//										System.out.println(s);
//										System.out.println(e);
//									}

									String e = events.get(btp[nextIndTab][Tablature.TAB_EVENT_SEQ_NUM]).getEncoding();
									RhythmSymbol rs = Symbol.getRhythmSymbol(
										e.substring(0, e.indexOf(Symbol.SYMBOL_SEPARATOR))
									);

									nextRs = Symbol.getRhythmSymbol(
										nextDur, 
										e.startsWith(Symbol.CORONA_INDICATOR), 
										rs.getBeam(), 
										rs.isTriplet()
									).getSymbol() + ".".repeat(nextDots);
								}
								else {
									nextRs = "R";
								}
								// Add interval from curr to next (or 0 if left/right border is a rest)
								// and next RS
								ornament.add(
									(currIndTab == -1 || nextIndTab == -1) ? String.valueOf(0) :
									String.valueOf(nextPitch - currPitch));
								ornament.add(nextRs);
								
								currIndTab = nextIndTab;
								currPitch = nextPitch;
								// Break if last note added is right border note
								if (!ornInds.contains(nextIndTab)) {
									// Make sure that the next iteration of the j for-loop starts at the right 
									// border note, which could be a new left border note
									loc.add("voice=" + i);
									loc.add("bar=" + currNoteDataInt[INTS.indexOf("bar")]);
									Rational mp = new Rational(
										currNoteDataInt[INTS.indexOf("metPosNum")], 
										currNoteDataInt[INTS.indexOf("metPosDen")]
									);
									mp.reduce();
									String mpStr = 
										mp.equals(Rational.ZERO) ? "0" :
										(mp.equals(Rational.ONE) ? "1" : mp.toString());									
									loc.add("metPos=" + mpStr);
									loc.add("startPitch=" + startPitch);
									j = k-1; 
									break;
								}
							}
							ornaments.add(new String[]{
								ornament.toString(), loc.toString()	
//								"[" + String.join(", ", ornament) + "]", 
//								"[" +  String.join(", ", loc) + "]"  
							});
						}
					}
				
				
//				for (int j = 0; j < numNotes; j++) {
//					Integer[] currNoteDataInt = currVoiceDataInt.get(j);
//					// If there is a next note
//					if (j != numNotes - 1) {
//						// If the next note is ornamental: build ornament. ornament consists of 
//						// (1) the opening non-ornamental note (left border note)
//						// (2) the ornamental note(s)
//						// (3) the closing non-ornamental note (right border note)
//						if (ornInds.contains(currVoiceDataInt.get(j + 1)[INTS.indexOf("indTab")])) {
//							List<String> ornament = new ArrayList<>();
//							List<String> ornamentAbs = new ArrayList<>();
//							// Add left border note
//							int currIndTab = currNoteDataInt[INTS.indexOf("indTab")];
////							String o = "";
//							String rs = "R";
//							int pitch = -1;
//							// If currIndTab is -1, currNoteDataInt represents a rest
//							if (currIndTab != -1) {
//								pitch = btp[currIndTab][Tablature.PITCH];
//								int dur = btp[currIndTab][Tablature.MIN_DURATION];
//								int dots = currNoteDataInt[INTS.indexOf("dots")];
//								// getRhythmSymbol() needs the undotted dur
//								dur = getUndottedNoteLength(dur, dots);
//								rs = Symbol.getRhythmSymbol(dur, false, false, null).getSymbol() + 
//									".".repeat(dots);
////								o = rs + pitch;
//							}
//							ornament.add(rs + pitch);
//							ornamentAbs.add(rs);
//							// Add ornamental note(s) and right border note
//							for (int k = j + 1; k < numNotes; k++) {
//								Integer[] nextNoteDataInt = currVoiceDataInt.get(k);
//								int nextIndTab = nextNoteDataInt[INTS.indexOf("indTab")];
////								String nextO = "";
//								String nextRs = "";
//								int nextPitch = -1;
//								// If nextIndTab is -1, nextNoteDataInt represents a rest
//								if (nextIndTab != -1) {
//									nextPitch = btp[nextIndTab][Tablature.PITCH];
//									int nextDur = btp[nextIndTab][Tablature.MIN_DURATION];
//									int nextDots = nextNoteDataInt[INTS.indexOf("dots")];
//									// getRhythmSymbol() needs the undotted dur
//									nextDur = getUndottedNoteLength(nextDur, nextDots);
//									nextRs = Symbol.getRhythmSymbol(nextDur, false, false, null).getSymbol() + 
//										".".repeat(nextDots);
////									nextO = nextRs + nextPitch;
//								}
//								ornament.add(nextRs + nextPitch);
//								ornamentAbs.add(currIndTab != -1 ? String.valueOf(0) : 
//									String.valueOf(nextPitch - pitch));
//								ornamentAbs.add(nextRs);
	//	
//								// Break after right border note
//								if (!ornInds.contains(nextIndTab)) {
//									// Make sure that the next iteration of the j for-loop starts at the right 
//									// border note, which could a new left border note
//									ornament.add("voice=" + i);
//									ornament.add("bar=" + currNoteDataInt[INTS.indexOf("bar")]);
//									Rational mp = new Rational(
//										currNoteDataInt[INTS.indexOf("metPosNum")], 
//										currNoteDataInt[INTS.indexOf("metPosDen")]
//									);
//									mp.reduce();
//									String mpStr = 
//										mp.equals(Rational.ZERO) ? "0" :
//										(mp.equals(Rational.ONE) ? "1" : mp.toString());									
//									ornament.add("metPos=" + mpStr);
//									j = k-1; 
//									break;
//								}
//							}
//							ornaments.add(ornament);
//						}
//					}
//				}
				}
				ornFull.addAll(ornaments);
//			System.out.println("xxxxxxxxxxxxxxxxxxxx");
			}
		
//			// dataStr for voice 0
//			List<List<String>> ornaments = new ArrayList<>();
////			int numBars = dataStr.size();
//			for (int voice = 0; voice < numVoices; voice++) {
//				
//				List<String> ornament = new ArrayList<>();
//				List<String> orn = new ArrayList<>();
//				List<Integer> ornInds = mismatchInds.get(Transcription.ORNAMENTATION_IND);
//				for (int bar = 0; bar < numBars; bar++) {
//					List<String[]> currBarStr = dataStr.get(bar).get(voice);
//					List<Integer[]> currBarInt = dataInt.get(bar).get(voice);
//					int numNotes = currBarStr.size();
//					for (int note = 0; note < numNotes; note++) {
//						String[] currNoteStr = currBarStr.get(note);
//						Integer[] currNoteInt = currBarInt.get(note);
//						int currIndTab = currNoteInt[INTS.indexOf("indTab")];
//						if (note < numNotes - 1) {
	//
//							Integer[] nextNoteInt = currBarInt.get(note + 1);
//							int nextIndTab = currNoteInt[INTS.indexOf("indTab")];
//							if (ornInds.contains(nextNoteInt[INTS.indexOf("indTab")])) {
////								ornament.add(e);
//							}
//						}
	//
//					}
//					System.out.println("|");		
//				}
//			}
//			System.exit(0);
			
//			for (List<String[]> voice : dataStr.get(29)) {
//				for (String[] note : voice) {
//					System.out.println(Arrays.asList(note));
//				}
//				System.out.println("- - - - - - ");
//			}
//			System.exit(0);
			
//			System.out.println("------------");
//			for (List<Integer[]> l : dataInt.get(2)) {
//				for (Integer[] in : l) {
//					System.out.println(Arrays.toString(in));
//				}
//			}
//			System.out.println("------------");
//			for (List<String[]> l : dataStr.get(2)) {
//				for (String[] in : l) {
//					System.out.println(Arrays.toString(in));
//				}
//			}
			
//			List<String[]> b61v2 = dataStr.get(61).get(2); 
//			System.out.println("XXXXXX");
//			for (String[] s : b61v2) {
//				System.out.println(Arrays.toString(s));
//			}
//			System.exit(0);
			
			// Apply beaming: set beamOpen and beamClose in dataInt
//			System.out.println(Arrays.asList(dataInt));
//			System.out.println(Arrays.asList(dataStr));

//			dataInt = beam(dataInt, dataStr, tab, mi, tripletOnsetPairs, mismatchInds, numVoices);
			
//			for (List<Integer[]> l : dataInt.get(2)) {
//				for (Integer[] in : l) {
//					System.out.println(Arrays.toString(in));
//				}
//			}
//			System.out.println("------------");
//			for (List<String[]> l : dataStr.get(2)) {
//				for (String[] in : l) {
//					System.out.println(Arrays.toString(in));
//				}
//			}
			
			List<Integer[]> sls = new ArrayList<>();
			for (int i = 0; i < numVoices; i++) {
				sls.add(getStaffAndLayer(
					false, Arrays.asList(ONLY_TAB, TAB_AND_TRANS), 
					TAB_ON_TOP, GRAND_STAFF, numVoices, i
				));
			}
			// For each bar
			Timeline tla = tab.getEncoding().getTimelineAgnostic();
			for (int i = 0; i < dataStr.size(); i++) {
				List<String> currBarAsXML = new ArrayList<>();
				int bar = i + 1;
				if (verbose) System.out.println("bar = " + bar);
				List<List<Integer[]>> currBarInt = dataInt.get(i);
				List<List<String[]>> currBarStr = dataStr.get(i);

				// For each voice
				for (int j = 0; j < currBarInt.size(); j++) {
					if (verbose) System.out.println("voice = " + j);
					List<Integer[]> currBarCurrVoiceInt = currBarInt.get(j);
					List<String[]> currBarCurrVoiceStr = currBarStr.get(j);
					if (verbose) {
						System.out.println("contents of currBarCurrVoiceInt");
						for (Integer[] in : currBarCurrVoiceInt) {
							System.out.println(Arrays.toString(in));
						}
						System.out.println("contents of currBarCurrVoiceStr");
						for (String[] in : currBarCurrVoiceStr) {
							System.out.println(Arrays.toString(in));
						}
					}
//					Integer[][] vsl = getStaffAndLayer(numVoices);
//					int staff, layer;
//					if (!grandStaff) {
//						staff = j+1;
//						layer = 1;
//					}
//					else {
//						staff = vsl[j][1];
//						layer = vsl[j][2];
//					}
//					if (TAB_AND_TRANS && tabOnTop) {
//						staff += 1;
//					}
					
//					Integer[] sl = getStaffAndLayer(
//						false, Arrays.asList(ONLY_TAB, TAB_AND_TRANS), TAB_ON_TOP, 
//						GRAND_STAFF, numVoices, j
//					);
					int staff = sls.get(j)[0];
					int layer = sls.get(j)[1];

//					Integer[][] sl = getStaffAndLayer(numVoices);
//					int staff = getStaffNum(
//						false, Arrays.asList(ONLY_TAB, TAB_AND_TRANS), 
//						TAB_ON_TOP, GRAND_STAFF, numVoices, j
//					);
//					int layer = !GRAND_STAFF ? 1 : sl[j][1];
					// Non-grand staff case: add staff
					if (!GRAND_STAFF) {
						currBarAsXML.add("<staff n='" + staff + "'>");
					}
//					// Grand staff case: only add staff if layer = 1
//					else {
//						if (layer == 1) {
//							currBarAsXML.add("<staff n='" + staff + "'>");
//						}
//					}
					// Grand staff case: only add staff if first voice or if previous voice has staff-1
					else {		
						if (j == 0 || (j > 0 && staff - 1 == sls.get(j-1)[0])) {
//						if (j == 0 || (j > 0 && sl[j][0] == (sl[j-1][0] + 1))) {
							currBarAsXML.add("<staff n='" + staff + "'>");
						}
					}
					currBarAsXML.add(TAB + "<layer n='" + layer + "'>");

					// For each note
					int diminution = 1;
					if (TAB_AND_TRANS) {
//					if (mi.get(0).length == Tablature.MI_SIZE) {
						diminution = tla.getDiminution(bar);
//						diminution = Tablature.getDiminution(bar, mi);
					}
//					for (int z = 0; z < currBarCurrVoiceInt.size(); z++) {
//						System.out.println(Arrays.asList(currBarCurrVoiceStr.get(z)));
//						System.out.println(Arrays.asList(currBarCurrVoiceInt.get(z)));
//					}
//					System.exit(0);
					List<String> currNotesAsXML = getBar(
						currBarCurrVoiceInt, currBarCurrVoiceStr, tripletOnsetPairs, mismatchInds, 
						j, diminution);

					currBarAsXML.addAll(currNotesAsXML);
					currBarAsXML.add(TAB + "</layer>");

					// Non-grand staff case: add staff
					if (!GRAND_STAFF) {
						currBarAsXML.add("</staff>");
					}

					// Grand staff case: only add staff if last voice or if next voice has staff+1
					else {
						if (j == numVoices - 1 || (j < numVoices-1 && staff + 1 == sls.get(j+1)[0])) {
//						if (j == numVoices - 1 || (j < numVoices-1 && sl[j][0] == (sl[j+1][0] - 1))) {
							currBarAsXML.add("</staff>");
						}
					}
				}
				transBars.add(currBarAsXML);
			}
			return transBars;
		}


	/**
	 * Returns the XML for the bar represented by the given lists <code>String[]</code> and 
	 * <ode>Integer[]</code>.
	 * 
	 * @param currBarCurrVoiceInt
	 * @param currBarCurrVoiceStr
	 * @param tripletOnsetPairs
	 * @param mismatchInds
	 * @param argVoice
	 * @param diminution
	 * @return
	 */
	private static List<String> getBarOLD(List<Integer[]> currBarCurrVoiceInt, List<String[]> 
		currBarCurrVoiceStr, List<Rational[]> tripletOnsetPairs, 
		List<List<Integer>> mismatchInds, int argVoice, int diminution) {
		List<String> barList = new ArrayList<>();
//		int bar = currBarCurrVoiceInt.get(0)[INTS.indexOf("bar")];
//		System.out.println(INTS.indexOf("bar"));
//		System.out.println(currBarCurrVoiceInt.size());
//		System.out.println(Arrays.toString(currBarCurrVoiceInt.get(0)));
		
		String barRestStr = 
			TAB.repeat(2) + "<mRest " + "xml:id='" + argVoice + "." + 
			currBarCurrVoiceInt.get(0)[INTS.indexOf("bar")] + "." + "0.r.0'" + "/>";

//		int diminution = 1;
//		if (mi.get(0).length == Tablature.MI_SIZE) {
//			diminution = Tablature.getDiminution(bar, mi);
//		}

		// If applied to new data (mismatchInds is an empty list), no notes need to be colored
		boolean highlightNotes = mismatchInds.size() != 0;
		boolean isMappingCase = highlightNotes && mismatchInds.get(0) == null; // zondag
//		boolean isMappingCase = mismatchInds.get(0) == null;
		List<Integer> orn = null;
		List<Integer> rep = null;
		List<Integer> ficta = null;
		List<Integer> other = null;
		List<Integer> inc = null;
		List<Integer> over = null;
		List<Integer> sup = null;
		List<Integer> half = null;
		// Mapping case
		if (isMappingCase) {
			orn = mismatchInds.get(Transcription.ORNAMENTATION_IND);
			rep = mismatchInds.get(Transcription.REPETITION_IND);
			ficta = mismatchInds.get(Transcription.FICTA_IND);
			other = mismatchInds.get(Transcription.ADAPTATION_IND);
		}
		// Modelling case
		else {
			if (highlightNotes) { // zondag
				inc = mismatchInds.get(1); // TODO was ErrorCalculator.INCORRECT_VOICE
				over = mismatchInds.get(2); // TODO was ErrorCalculator.OVERLOOKED_VOICE
				sup = mismatchInds.get(3); // TODO was ErrorCalculator.SUPERFLUOUS_VOICE
				half = mismatchInds.get(4); // TODO was ErrorCalculator.HALF_VOICE
			}
		}

		// If the bar does not contain any notes or rests (this happens when a voice ends
		// with one or more bars of rests)
		if (currBarCurrVoiceInt.size() == 0) {
			barList.add(barRestStr);
		}
		// If the bar contains notes/rests
		else {
			boolean isFullBarRest = true;
			for (int i = 0; i < currBarCurrVoiceStr.size(); i++) {
				String[] note = currBarCurrVoiceStr.get(i);
				if (note[STRINGS.indexOf("pname")] != null) {
					isFullBarRest = false;
					break;
				}
			}
			if (isFullBarRest) {
				barList.add(barRestStr);
			}
			else {
//				int bar = currBarCurrVoiceInt.get(0)[INTS.indexOf("bar")];
//				Rational meter = Transcription.getMeter(bar, mi);
				
//				List<String> barListTupletWrapped = new ArrayList<>();
//				for (int k = 0; k < currBarCurrVoiceInt.size(); k++) {
//					Integer[] in = currBarCurrVoiceInt.get(k);
//					if (in[INTS.indexOf("tripletOpen")] == 1) {
//						barListTupletWrapped.add
//					}
//				}
	
//				List<List<String>> perBeat = new ArrayList<>();
				boolean chordActive = false;
				boolean tupletActive = false;
				boolean beamActive = false; 
				// For each note
				int indentsAdded = 0;
				for (int i = 0; i < currBarCurrVoiceStr.size(); i++) {
					String[] note = currBarCurrVoiceStr.get(i);
					Integer[] noteInt = currBarCurrVoiceInt.get(i);
					
//					System.out.println(Arrays.asList(noteInt));

//					Rational mp = new Rational(noteInt[INTS.indexOf("metPosNum")], 
//						noteInt[)INTS.indexOf("metPosDen")]);
//					Rational durRat = new Rational(Tablature.SMALLEST_RHYTHMIC_VALUE.getDenom(),
//						noteInt[INTS.indexOf("dur")]);
//					int dur = (int) durRat.toDouble();
					int currOnsNum = currBarCurrVoiceInt.get(i)[INTS.indexOf("onsetNum")];
					int currOnsDen = currBarCurrVoiceInt.get(i)[INTS.indexOf("onsetDen")];

//					// Close previous/open next beaming group if metPos is on the quarter note grid
//					if (mp.toDouble() == 0.25 || mp.toDouble() == 0.5 || mp.toDouble() == 0.75) { 
//						// Only if the note at index k is not the last note in the bar
//						sb.append(indent + tab.repeat(3) + "</beam>" + "\r\n");
//						sb.append(indent + tab.repeat(3) + "<beam>" + "\r\n");
//					}

					// WAS: check in sequence triplet - chord - beam
					// Check for any beam to be added before noteStr
					if (noteInt[INTS.indexOf("beamOpen")] == 1) {
						barList.add(TAB.repeat(2) + "<beam>");
						beamActive = true;
						indentsAdded++;
					}

					// Check for any chord to be added before noteStr
					if (i < currBarCurrVoiceStr.size()-1) {
						Integer[] nextNoteInt = currBarCurrVoiceInt.get(i+1);
						if (new Rational(currOnsNum, currOnsDen).equals(new Rational(
							nextNoteInt[INTS.indexOf("onsetNum")], nextNoteInt[INTS.indexOf("onsetDen")]))
							&& !chordActive) {
							barList.add(
								TAB.repeat(2 + indentsAdded) + "<chord dur='" + noteInt[INTS.indexOf("dur")] + "'>"
							);
							chordActive = true;
							indentsAdded++;
						}
					}
					
					// Check for any tripletOpen to be added before noteStr
					if (noteInt[INTS.indexOf("tripletOpen")] == 1) {
						int tupletDur = -1;
//						Rational tupletDur = null;
						Rational onset = new Rational(currOnsNum, currOnsDen);
//							currBarCurrVoiceInt.get(i)[INTS.indexOf("onsetDen")]);
						for (Rational[] r : tripletOnsetPairs) {
							if (onset.equals(r[0])) {
//								tupletDur = r[2].getNumer();
								Rational tupletDurRat = r[2];
								// tupletDurRat is expressed in RhythmSymbol duration (e.g., 
								// minim = 24): convert to CMN equivalent (e.g., 24 * 1/96 = 1/4) 
//								if (noteInt[INTS.indexOf("indTab")] != -1) {
								tupletDurRat = tupletDurRat.mul(Tablature.SMALLEST_RHYTHMIC_VALUE);
//								}
								// Apply diminution and get MEI dur value (e.g., 1/4 * 2 = 1/2;
								// MEI dur value is denom of 1/2)
								tupletDurRat = 
									(diminution > 0) ? tupletDurRat.mul(diminution) :
									tupletDurRat.div(diminution);
								tupletDur = tupletDurRat.getDenom();
								break;
							}
						}
						barList.add(
							TAB.repeat(2 + indentsAdded) + "<tuplet dur='" + tupletDur + "' num='3' numbase='2'>"
						);
						tupletActive = true;
						indentsAdded++;
					}

					String noteStr = TAB.repeat(2 + indentsAdded);
//					// Add indent for any tuplet and/or chord
//					if (tupletActive) {
//						noteStr += TAB;
//					}
//					if (chordActive) {
//						noteStr += TAB;
//					}
//					if (beamActive) {
//						noteStr += TAB;
//					}
					noteStr = 
						(note[STRINGS.indexOf("pname")] == null) ? noteStr + "<rest " : 
						noteStr + "<note ";
					
//					if (note[STRINGS.indexOf("pname")] == null) {
//						noteStr += "<rest "; 
//					}
//					else {
//						// Check for in-voice chords
//						if (k < currBarCurrVoiceStr.size()-1) {
//							if (currOnsNum == currBarCurrVoiceInt.get(k+1)[INTS.indexOf("onsetNum")]) {
//								barList.add("<chord>");
//								chordActive = true;
//							}
//						}
//						noteStr += "<note ";
//					}

//					Arrays.asList(note).forEach(a -> {if (a != null) {fromNote += (a + " ");}});
					for (String s : note) {
						if (s != null) { 
							noteStr += (s + " ");
						}
					}
//					String ID = note[STRINGS.indexOf("ID")];
//					ID = ID.substring(ID.indexOf("'") + 1, ID.lastIndexOf("'"));
					int tabInd = -1;
					if (note[STRINGS.indexOf("pname")] != null) {
						tabInd = noteInt[INTS.indexOf("indTab")];
					}
					if (isMappingCase) {
						if (orn.contains(tabInd)) {
							noteStr += "color='blue'" + " ";
						}
						else if (rep.contains(tabInd)) {
							noteStr += "color='lime'" + " ";
						}
						else if (ficta.contains(tabInd)) {
							noteStr += "color='orange'" + " ";
						}
						else if (other.contains(tabInd)) {
							noteStr += "color='red'" + " ";
						}
					}
					else {
						if (highlightNotes) {
							int index = (tabInd != -1) ? tabInd : noteInt[INTS.indexOf("ind")];
							if (inc.contains(index)) {
								noteStr += "color='red'" + " ";
							}
							else if (over.contains(index)) {
								noteStr += "color='orange'" + " ";
							}
							else if (sup.contains(index)) {
								noteStr += "color='lime'" + " ";
							}
							else if (half.contains(index)) {
								noteStr += "color='blue'" + " ";
							}
						}
					}
					// Trim trailing spaces (not leading spaces, as there may be TABs)
					// (see https://stackoverflow.com/questions/12106757/removing-spaces-at-the-end-of-a-string-in-java) 
					noteStr = noteStr.replaceAll("\\s+$", "");
					noteStr = noteStr + "/>";
					barList.add(noteStr);
					
					// Close any active triplet, chord, beam
					// Triplet
					if (noteInt[INTS.indexOf("tripletClose")] == 1) {
						indentsAdded--;
						barList.add(TAB.repeat(2 + indentsAdded) + "</tuplet>");
						tupletActive = false;
					}
					// Chord
					if (chordActive) {
						Rational currOnset = new Rational(currOnsNum, currOnsDen);
						Rational nextOnset = 
							(i == currBarCurrVoiceStr.size()-1) ? null :
							new Rational(currBarCurrVoiceInt.get(i+1)[INTS.indexOf("onsetNum")],
							currBarCurrVoiceInt.get(i+1)[INTS.indexOf("onsetDen")]);
						if ((i < currBarCurrVoiceStr.size()-1 && nextOnset.isGreater(currOnset)) ||
							i == currBarCurrVoiceStr.size()-1) {
							indentsAdded--;
							barList.add(TAB.repeat(2 + indentsAdded) + "</chord>");
							chordActive = false;
						}
					}
					// Beam
					if (noteInt[INTS.indexOf("beamClose")] == 1) {
						indentsAdded--;
						barList.add(TAB.repeat(2 + indentsAdded) + "</beam>");
						beamActive = false;
					}
				}
			}
		}
		return barList;
	}

	public static List<String[]> ornFull = new ArrayList<>();
	private static void extractOrnaments(Tablature tab, List<Object> data, List<List<Integer>> mismatchInds, 
		List<Event> events,	int numVoices) {
		List<List<List<Integer[]>>> dataInt = (List<List<List<Integer[]>>>) data.get(0);
		List<List<List<String[]>>> dataStr = (List<List<List<String[]>>>) data.get(1);

		// Reorganise: list, per voice, the notes in that voice
		List<List<Integer[]>> dataIntPerVoice = new ArrayList<>();
		List<List<String[]>> dataStrPerVoice = new ArrayList<>();
		int numBars = dataInt.size();
		for (int voice = 0; voice < numVoices; voice++) {
			List<Integer[]> notesCurrVoiceInt = new ArrayList<>();
			List<String[]> notesCurrVoiceStr = new ArrayList<>();
			for (int bar = 0; bar < numBars; bar++) {
				notesCurrVoiceInt.addAll(dataInt.get(bar).get(voice));
				notesCurrVoiceStr.addAll(dataStr.get(bar).get(voice));
			}
			dataIntPerVoice.add(notesCurrVoiceInt);
			dataStrPerVoice.add(notesCurrVoiceStr);
		}
		
		Integer[][] btp = tab.getBasicTabSymbolProperties();
		List<Integer> ornInds = mismatchInds.get(Transcription.ORNAMENTATION_IND);
		List<String[]> ornaments = new ArrayList<>();
		ornaments.add(new String[]{"piece="+tab.getName()});
		for (int i = 0 ; i < numVoices; i++) {
//			System.out.println("voice " + i);
			List<Integer[]> currVoiceDataInt = dataIntPerVoice.get(i);
			int numNotes = currVoiceDataInt.size();
			for (int j = 0; j < numNotes - 1; j++) {
//				System.out.println("note " + j);
				Integer[] currNoteDataInt = currVoiceDataInt.get(j);
				Integer[] nextNoteDataInt = currVoiceDataInt.get(j+1);
				// If the next note is ornamental: build ornament. ornament consists of 
				// (1) the opening non-ornamental note (left border note)
				// (2) the ornamental note(s)
				// (3) the closing non-ornamental note (right border note)
				if (ornInds.contains(nextNoteDataInt[INTS.indexOf("indTab")])) {
					List<String> ornament = new ArrayList<>();
					List<String> loc = new ArrayList<>();
					// Add left border note
					int currIndTab = currNoteDataInt[INTS.indexOf("indTab")];
					int currPitch;
					int startPitch;
					String currRs;
					// If currIndTab is -1, currNoteDataInt represents a rest, and
					// the ornament starts with a rest (+ interval 0)
					if (currIndTab == -1) {
						currPitch = -1;
						startPitch = -1;
						currRs = "R";
					}
					else {
						currPitch = btp[currIndTab][Tablature.PITCH];
						startPitch = currPitch;
						int dur = btp[currIndTab][Tablature.MIN_DURATION];
						int dots = currNoteDataInt[INTS.indexOf("dots")];
						// getRhythmSymbol() needs the undotted dur
						dur = TimeMeterTools.getUndottedNoteLength(dur, dots);
							
						String e = events.get(btp[currIndTab][Tablature.TAB_EVENT_SEQ_NUM]).getEncoding();
						RhythmSymbol rs = Symbol.getRhythmSymbol(
							e.substring(0, e.indexOf(Symbol.SYMBOL_SEPARATOR))
						);
//						System.out.println(e);
//						System.out.println(rs);
									
						currRs = Symbol.getRhythmSymbol(
							dur, 
							e.startsWith(Symbol.CORONA_INDICATOR), 
							rs.getBeam(), 
							rs.isTriplet()
						).getSymbol() + ".".repeat(dots);
					}
					// Add current RS
					ornament.add(currRs);
					// Add ornamental note(s) and right border note
					for (int k = j + 1; k < numNotes; k++) {
						nextNoteDataInt = currVoiceDataInt.get(k);
						int nextIndTab = nextNoteDataInt[INTS.indexOf("indTab")];
						if (nextIndTab == -1) {
							System.out.println(Arrays.asList(nextNoteDataInt));
							System.out.println("k " + k);
							System.out.println(ornament);
						}
						// If nextIndTab is -1, nextNoteDataInt represents a rest, and
						// the ornament ends with a rest (+ interval 0)
						int nextPitch = nextIndTab == -1 ? -1 : btp[nextIndTab][Tablature.PITCH];
						if (startPitch == -1) {
							startPitch = nextPitch;
						}
						String nextRs;
						if (nextIndTab != -1) {
							int nextDur = btp[nextIndTab][Tablature.MIN_DURATION];
							int nextDots = nextNoteDataInt[INTS.indexOf("dots")];
							// getRhythmSymbol() needs the undotted dur
							nextDur = TimeMeterTools.getUndottedNoteLength(nextDur, nextDots);
//							System.out.println(nextDur);
//							if (j == 223 || j == 224 || j == 225 || j == 226 || j == 227) {
//								System.out.println(Arrays.asList(nextNoteDataInt));
//								System.out.println(Arrays.asList(dataStrPerVoice.get(i).get(k)));
//								String s = enc.getListsOfSymbols().get(1).get(nextIndTab);
//								int eInd = btp[nextIndTab][Tablature.TAB_EVENT_SEQ_NUM];
//								System.out.println(enc.getListsOfSymbols().get(0));
//								String e = events.get(eInd).getEncoding();
//								System.out.println(s);
//								System.out.println(e);
//							}
								
							String e = events.get(btp[nextIndTab][Tablature.TAB_EVENT_SEQ_NUM]).getEncoding();
							RhythmSymbol rs = Symbol.getRhythmSymbol(
								e.substring(0, e.indexOf(Symbol.SYMBOL_SEPARATOR))
							);
							nextRs = Symbol.getRhythmSymbol(
								nextDur, 
								e.startsWith(Symbol.CORONA_INDICATOR), 
								rs.getBeam(), 
								rs.isTriplet()
							).getSymbol() + ".".repeat(nextDots);
						}
						else {
							nextRs = "R";
						}
						// Add interval from curr to next (or 0 if left/right border is a rest)
						// and next RS
						ornament.add(
							(currIndTab == -1 || nextIndTab == -1) ? String.valueOf(0) :
						String.valueOf(nextPitch - currPitch));
						ornament.add(nextRs);
						
						currIndTab = nextIndTab;
						currPitch = nextPitch;
						// Break if last note added is right border note
						if (!ornInds.contains(nextIndTab)) {
							// Make sure that the next iteration of the j for-loop starts at the right 
							// border note, which could be a new left border note
							loc.add("voice=" + i);
							loc.add("bar=" + currNoteDataInt[INTS.indexOf("bar")]);
							Rational mp = new Rational(
								currNoteDataInt[INTS.indexOf("metPosNum")], 
								currNoteDataInt[INTS.indexOf("metPosDen")]
							);
							mp.reduce();
							String mpStr = 
								mp.equals(Rational.ZERO) ? "0" :
								(mp.equals(Rational.ONE) ? "1" : mp.toString());									
							loc.add("metPos=" + mpStr);
							loc.add("startPitch=" + startPitch);
							j = k-1; 
							break;
						}
					}
					ornaments.add(new String[]{
						ornament.toString(), loc.toString()	
//						"[" + String.join(", ", ornament) + "]", 
//						"[" +  String.join(", ", loc) + "]"  
					});
				}
			}
							
//				for (int j = 0; j < numNotes; j++) {
//					Integer[] currNoteDataInt = currVoiceDataInt.get(j);
//					// If there is a next note
//					if (j != numNotes - 1) {
//						// If the next note is ornamental: build ornament. ornament consists of 
//						// (1) the opening non-ornamental note (left border note)
//						// (2) the ornamental note(s)
//						// (3) the closing non-ornamental note (right border note)
//						if (ornInds.contains(currVoiceDataInt.get(j + 1)[INTS.indexOf("indTab")])) {
//							List<String> ornament = new ArrayList<>();
//							List<String> ornamentAbs = new ArrayList<>();
//							// Add left border note
//							int currIndTab = currNoteDataInt[INTS.indexOf("indTab")];
////							String o = "";
//							String rs = "R";
//							int pitch = -1;
//							// If currIndTab is -1, currNoteDataInt represents a rest
//							if (currIndTab != -1) {
//								pitch = btp[currIndTab][Tablature.PITCH];
//								int dur = btp[currIndTab][Tablature.MIN_DURATION];
//								int dots = currNoteDataInt[INTS.indexOf("dots")];
//								// getRhythmSymbol() needs the undotted dur
//								dur = getUndottedNoteLength(dur, dots);
//								rs = Symbol.getRhythmSymbol(dur, false, false, null).getSymbol() + 
//									".".repeat(dots);
////								o = rs + pitch;
//							}
//							ornament.add(rs + pitch);
//							ornamentAbs.add(rs);
//							// Add ornamental note(s) and right border note
//							for (int k = j + 1; k < numNotes; k++) {
//								Integer[] nextNoteDataInt = currVoiceDataInt.get(k);
//								int nextIndTab = nextNoteDataInt[INTS.indexOf("indTab")];
////								String nextO = "";
//								String nextRs = "";
//								int nextPitch = -1;
//								// If nextIndTab is -1, nextNoteDataInt represents a rest
//								if (nextIndTab != -1) {
//									nextPitch = btp[nextIndTab][Tablature.PITCH];
//									int nextDur = btp[nextIndTab][Tablature.MIN_DURATION];
//									int nextDots = nextNoteDataInt[INTS.indexOf("dots")];
//									// getRhythmSymbol() needs the undotted dur
//									nextDur = getUndottedNoteLength(nextDur, nextDots);
//									nextRs = Symbol.getRhythmSymbol(nextDur, false, false, null).getSymbol() + 
//										".".repeat(nextDots);
////									nextO = nextRs + nextPitch;
//								}
//								ornament.add(nextRs + nextPitch);
//								ornamentAbs.add(currIndTab != -1 ? String.valueOf(0) : 
//									String.valueOf(nextPitch - pitch));
//								ornamentAbs.add(nextRs);
//	
//								// Break after right border note
//								if (!ornInds.contains(nextIndTab)) {
//									// Make sure that the next iteration of the j for-loop starts at the right 
//									// border note, which could a new left border note
//									ornament.add("voice=" + i);
//									ornament.add("bar=" + currNoteDataInt[INTS.indexOf("bar")]);
//									Rational mp = new Rational(
//										currNoteDataInt[INTS.indexOf("metPosNum")], 
//										currNoteDataInt[INTS.indexOf("metPosDen")]
//									);
//									mp.reduce();
//									String mpStr = 
//										mp.equals(Rational.ZERO) ? "0" :
//										(mp.equals(Rational.ONE) ? "1" : mp.toString());									
//									ornament.add("metPos=" + mpStr);
//									j = k-1; 
//									break;
//								}
//							}
//							ornaments.add(ornament);
//						}
//					}
//				}
		}
		ornFull.addAll(ornaments);
	}


	/**
	 * Represent the given tablature as a list of bars, each of which itself is a list of events
	 * in that bar.
	 *  
	 * @param tab
	 * @return
	 */
//	private static List<List<String>> getTabData(Tablature tab) {
//		List<List<String>> bars = new ArrayList<>();
//
//		String ss = Symbol.SYMBOL_SEPARATOR;
//		// Split into bars
//		String cleanEncoding = tab.getEncoding().getCleanEncoding();
//		// Remove EBI and split into systems
//		cleanEncoding = cleanEncoding.replace(Symbol.END_BREAK_INDICATOR, "");
//		String[] cleanEncodingSystems = cleanEncoding.split(Symbol.SYSTEM_BREAK_INDICATOR);
//		// Remove leading barline (of any kind) for each system
//		for (int i = 0; i < cleanEncodingSystems.length; i++) {
//			String system = cleanEncodingSystems[i];
//			String first = system.substring(0, system.indexOf(ss));
//			// If barline
//			if (Symbol.getConstantMusicalSymbol(first) != null && Symbol.getConstantMusicalSymbol(first).isBarline()) {
////			if (ConstantMusicalSymbol.isBarline(first)) {
//				cleanEncodingSystems[i] = system.substring(system.indexOf(ss) + 1, system.length());
//			}
//		}
//
//		for (String system : cleanEncodingSystems) {
//			List<String> bar = new ArrayList<>();
//			int start = 0;
//			String event = "";
//			// Split into bars, which themselves are split into event // make method in Tablature
//			for (int i = 0; i < system.length(); i++) {
//				if (system.substring(i, i+1).equals(ss)) {
//					String curr = system.substring(start, i);
//					boolean isSpace = curr.equals(Symbol.SPACE.getEncoding());
//					boolean isBarline = 
//						Symbol.getConstantMusicalSymbol(curr) != null && Symbol.getConstantMusicalSymbol(curr).isBarline();
////					boolean isBarline = ConstantMusicalSymbol.isBarline(curr);
//					if (!isSpace) {
//						event = (event.length() == 0) ? event + curr : event + ss + curr;
//					}
//					if (isSpace || isBarline) {
//						bar.add(event);
//						event = "";
//					}
//					start = i+1;
//
//					// If barline: add bar to bars
//					if (isBarline) {
//						bars.add(bar);
//						bar = new ArrayList<>();
//					}
//				}
//			}
//		}
//		return bars;
//	}


//	private static List<List<String>> getTabBarsBIJNA(Tablature tab, int staff) {
//		System.out.println("\r\n>>> getTabBars() called");
//		List<List<String>> tabBars = new ArrayList<>();
//		
//		String ss = Symbol.SYMBOL_SEPARATOR;
//		String sp = Symbol.SPACE.getEncoding();
//		TabSymbolSet tss = tab.getEncoding().getTabSymbolSet();
//
//		// Organise events per bar. Per getEvents(), each bar ends with a barline. Any
//		// decorative opening barlines must be removed prior to the organisation per bar
//		List<List<Event>> eventsPerBar = new ArrayList<>();
//		List<Event> events = 
//			Encoding.removeDecorativeBarlineEvents(tab.getEncoding().getEvents());
//		List<Event> eventsCurrBar = new ArrayList<>();
//		for (int i = 0; i < events.size(); i++) {
//			Event e = events.get(i);
//			eventsCurrBar.add(e);
//			if (Encoding.assertEventType(e.getEncoding(), tss, "barline")) {
//				eventsPerBar.add(eventsCurrBar);
//				eventsCurrBar = new ArrayList<>();
//			}
//		}
//
//		// For each bar
//		Integer[] prevDurXML = new Integer[]{0, 0};
//		for (int i = 0; i < eventsPerBar.size(); i++) {
//			List<String> currBarAsXML = new ArrayList<>();
//			currBarAsXML.add("<staff n='" + staff + "'>");
//			currBarAsXML.add(TAB + "<layer n='1'>");
//			List<Event> currBarEvents = eventsPerBar.get(i);
//
//			// For each event in the bar
//			boolean beamActive = false;
//			for (int j = 0; j < currBarEvents.size(); j++) {
//				Event currEventFull = currBarEvents.get(j);
//
//				// Get current event
//				String currEvent = currEventFull.getEncoding();
//
//				// If there is a footnote: extract any original event
//				String currFootnote = currEventFull.getFootnote();
//				boolean eventIsCorrected = currFootnote != null && currFootnote.contains("'");
//				String currEventOrig = null;
//				if (currFootnote != null) {
//					currEventOrig =
//						eventIsCorrected ? currFootnote.substring(
//							currFootnote.indexOf("'")+1, currFootnote.lastIndexOf("'")
//						) :
//						currFootnote.substring(
//							currFootnote.indexOf(Encoding.FOOTNOTE_INDICATOR) + 1
//						);
//				}
//
//				// Make event, sicList (containing all the <sic> events), and corrList
//				// (containing all the <corr> events)
//				String event = eventIsCorrected ? null : currEvent;
//				String sicEvent = eventIsCorrected ? currEventOrig : null;
//				String corrEvent = eventIsCorrected ? currEvent : null;
//				List<String> sicList = new ArrayList<>();
//				if (sicEvent != null) {
//					// Add all events in footnote
//					for (String s : sicEvent.split(sp + ss)) {
//						sicList.add(StringTools.removeTrailingSymbolSeparator(s));
//					}
//				}
//				List<String> corrList = new ArrayList<>();
//				if (corrEvent != null) {
//					// Add corrEvent and all events with an empty footnote ({@}) that follow
//					corrList.add(corrEvent);
//					for (int k = j+1; k < currBarEvents.size(); k++) {
//						Event nextEventFull = currBarEvents.get(k);
//						String nextEvent = nextEventFull.getEncoding();
//						String nextFootnote = nextEventFull.getFootnote();
//						if (nextFootnote == null) {
//							break;
//						}
//						else if (nextFootnote.equals(Encoding.FOOTNOTE_INDICATOR)) {
//							corrList.add(nextEvent);
//						}
//						else {
//							break;
//						}
//					}
//				}
//
////				// Remove trailing SS
////				currEvent = removeTrailingSymbolSeparator(currEvent);
////				if (currEventOrig != null) {
////					currEventOrig = removeTrailingSymbolSeparator(currEventOrig);
////				}
//
//				// Not a barline event: add event to currBarAsXML 
//				if (!Encoding.assertEventType(currEvent, tss, "barline")) {
//					List<String> currEventAsXML = new ArrayList<>();
//					if (!eventIsCorrected) {
//						boolean isRSEvent = Encoding.assertEventType(event, tss, "RhythmSymbol");
//						boolean isBeamed = 
//							!isRSEvent ? false : 
//							Symbol.getRhythmSymbol(event.substring(0, event.indexOf(ss))).getBeam();
//
//						boolean openBeam = false;
//						boolean closeBeam = false;
//						if (!beamActive && isRSEvent && isBeamed) {
//							openBeam = true;
//							beamActive = true;
//						}
//						if (beamActive && isRSEvent && !isBeamed) {
//							closeBeam = true;
//							beamActive = false;
//						}
//						
//						if (openBeam) {
//							currEventAsXML.add(TAB.repeat(2) + "<beam>");
//						}
//						currEventAsXML.addAll(
//							getTabGrps(Arrays.asList(new String[]{event}), prevDurXML, 
//							eventIsCorrected, beamActive || closeBeam, tss)
//						);
//						if (closeBeam) {
//							currEventAsXML.add(TAB.repeat(2) + "</beam>");
//						}
//
//						// Update prevDurXML
//						Integer[] currDurXML = getXMLDur(event);
//						if (currDurXML != null) {
//							prevDurXML = currDurXML;
//						}
//					}
//					else {
//						currEventAsXML.add(TAB.repeat(2) + "<choice>");
//						// <corr> part
////						currEventAsXML.add(TAB.repeat(3) + "<corr>");
////						boolean beamActiveBeforeChoice = beamActive;
////						boolean beamActiveCorr = beamActive; // beamActiveBeforeChoice;
//						boolean beamActiveCorrOrSic = beamActive;
////						Integer[] prevDurXMLBeforeChoice = prevDurXML;
////						Integer[] prevDurXMLCorr = prevDurXML; // prevDurXMLBeforeChoice;
//						Integer[] prevDurXMLCorrOrSic = prevDurXML;
//						// k == 0: corrList; k == 1: sicList
//						for (int k = 0; k < 2; k++) {
//							List<String> currList = k == 0 ? corrList : sicList;
//
//							currEventAsXML.add(TAB.repeat(3) + (k == 0 ? "<corr>" : "<sic>"));
//
//							for (int l = 0 ; l < currList.size(); l++) {
////							for (String corrOrSic : currList) {	
////							for (String corr : corrList) {
//								String corrOrSic = currList.get(l);
//								if (!corrOrSic.endsWith(ss)) {
//									corrOrSic += ss;
//								}
//								boolean isRSEvent = Encoding.assertEventType(corrOrSic, tss, "RhythmSymbol");
//								boolean isBeamed = 
//									!isRSEvent ? false : 
//									Symbol.getRhythmSymbol(corrOrSic.substring(0, corrOrSic.indexOf(ss))).getBeam();
//	
//								boolean openBeam = false;
//								boolean closeBeam = false;
//								if (!beamActiveCorrOrSic && isRSEvent && isBeamed) {
//									openBeam = true;
//									beamActiveCorrOrSic = true;
//								}
//								if (beamActiveCorrOrSic && isRSEvent && !isBeamed) {
//									closeBeam = true;
//									beamActiveCorrOrSic = false;
//								}
//								
//								if (openBeam) {
//									currEventAsXML.add(TAB.repeat(4) + "<beam>");
//								}
//								currEventAsXML.addAll(
//									getTabGrps(Arrays.asList(new String[]{corrOrSic}), prevDurXMLCorrOrSic, 
//									eventIsCorrected, beamActiveCorrOrSic || closeBeam, tss)
//								);
//								if (closeBeam) {
//									currEventAsXML.add(TAB.repeat(4) + "</beam>");
//								}
//
//								// Update prevDurXMLCorr
//								Integer[] currDurXML = getXMLDur(corrOrSic);
//								if (currDurXML != null) {
//									prevDurXMLCorrOrSic = currDurXML;
//								}
//
//								if (l == currList.size() - 1) {
//									currEventAsXML.add(TAB.repeat(3) + (k == 0 ? "</corr>" : "</sic>"));
//								}
//
//								// Reset to before-loop values for second loop iteration
//								if (l == currList.size() - 1) {
//									beamActiveCorrOrSic = beamActive;
//									prevDurXMLCorrOrSic = prevDurXML;
//								}
//
//								// Update prevDurXML and beamActive
//								if (l == currList.size() - 1) {
//									// 1. In case of a change of RS, both the <sic> and the <corr> case will still  
//									// end with the same RS, so prevDurXML can be set to either prevDurXMLCorr  
//									// or prevDurXMLSic. Examples ('n' = notes in event; * = new prevDurXML value):
//									//
//									//            E   *                           Q   E*
//									//     <sic>  n n n (n ...)            <sic>  n   n (n ...)
//									// (1)                             (2)
//									//            Q   E*                          E   *
//									//     <corr> n   n (n ...)            <corr> n n n (n ...) 
//									//
//									// NB: prevDurXML is only used (in getTabGrps()) if NOT every event has a RS
//									prevDurXML = prevDurXMLCorrOrSic;
//									// 2. The same logic applies to beamActive. Examples ('n' = notes in event; * = new
//									// beamActive value):
//									//
//									//            E-E-E-*E                        Q   E-*E
//									//     <sic>  n n n (n ...)            <sic>  n   n (n ...)
//									// (1)                             (2)
//									//            Q   E-*E                        E-E-E-*E
//									//     <corr> n   n (n ...)            <corr> n n n (n ...) 
//									//
//									// NB: beamActive is only used if every event has a RS
//									beamActive = beamActiveCorrOrSic;
//								}
//							}
////							currEventAsXML.add(TAB.repeat(3) + "</corr>");
////							currEventAsXML.add(TAB.repeat(3) + "<sic>");
////							boolean beamActiveSic = beamActive; //beamActiveBeforeChoice;
////							Integer[] prevDurXMLSic = prevDurXML; //prevDurXMLBeforeChoice;
////							for (String sic : sicList) {
////								if (!sic.endsWith(ss)) {
////									sic += ss;
////								}
////								boolean isRSEvent = Encoding.assertEventType(sic, tss, "RhythmSymbol");
////								boolean isBeamed = 
////									!isRSEvent ? false : 
////									Symbol.getRhythmSymbol(sic.substring(0, sic.indexOf(ss))).getBeam();
////
////								boolean openBeam = false;
////								boolean closeBeam = false;
////								if (!beamActiveSic && isRSEvent && isBeamed) {
////									openBeam = true;
////									beamActiveSic = true;
////								}
////								if (beamActiveSic && isRSEvent && !isBeamed) {
////									closeBeam = true;
////									beamActiveSic = false;
////								}
////
////								if (openBeam) {
////									currEventAsXML.add(TAB.repeat(4) + "<beam>");
////								}
////								currEventAsXML.addAll(
////									getTabGrps(Arrays.asList(new String[]{sic}), prevDurXMLSic, 
////									eventIsCorrected, beamActiveSic || closeBeam, tss)
////								);
////								if (closeBeam) {
////									currEventAsXML.add(TAB.repeat(4) + "</beam>");
////								}
////
////								Integer[] currDurXML = getXMLDur(sic);
////								if (currDurXML != null) {
////									prevDurXMLSic = currDurXML;
////								}
////							}
//						
////						currEventAsXML.add(TAB.repeat(3) + "</sic>");
////						currEventAsXML.add(TAB.repeat(2) + "</choice>");
//						}
//						currEventAsXML.add(TAB.repeat(2) + "</choice>");
//						// Increment j as to skip the events in corrList 
//						j += corrList.size() - 1;
//					}	
//					currBarAsXML.addAll(currEventAsXML);
//				}
//				// Barline: close currBarAsXML and add it to tabBars
//				else {
//					currBarAsXML.add(TAB + "</layer>");
//					currBarAsXML.add("</staff>");
//					tabBars.add(currBarAsXML);
//				}
//			}
//		}
//		return tabBars;
//	}


//	private static List<List<String>> getTabBarsOLD(Tablature tab, int staff) {
//		System.out.println("\r\n>>> getTabBars() called");
//		List<List<String>> tabBars = new ArrayList<>();
//		
//		String ss = Symbol.SYMBOL_SEPARATOR;
//		String sp = Symbol.SPACE.getEncoding();
//		TabSymbolSet tss = tab.getEncoding().getTabSymbolSet();
//		List<Integer[]> mi = tab.getMeterInfo();
//		List<Integer[]> tabBarsToMetricBars = null; // tab.mapBarsToMetricBars();
//
//		List<Integer[]> tbi = tab.getEncoding().getTimeline().getBarInfo();
////		List<Integer[]> tbi = tab.getEncoding().getBarInfo();
//		List<Integer[]> mbi = null; // tab.getMetricBarInfo();
//		List<Integer> metricBarOnsets = ToolBox.getItemsAtIndex(mbi, 0);
//		// Remove onset 0, and add onset of the fictional bar after the last
//		metricBarOnsets.remove(0);
//		metricBarOnsets.add(metricBarOnsets.get(metricBarOnsets.size()-1) + mbi.get(mbi.size()-1)[1]);
//
//		Timeline tl = tab.getEncoding().getTimeline();
////		for (int i = 0; i < tbi.size(); i++) {
////			Integer[] in = tbi.get(i);
////			int ons = in[0];
////			int dur = in[1];
////			Rational[] mp = tl.getMetricPosition(ons);
////			System.out.println("tab bar " + (i+1) + " starts at metric bar " + mp[0].getNumer() + ", mp " + mp[1]);
////		}
//
//		// For each bar
//		List<String> currBarAsXML = new ArrayList<>();
//		int prevDur = -1;
//		int currOffsetInTab = 0;
//		Integer[] prevDurXML = new Integer[]{0, 0};
//		boolean startNewBar = true;
//		// Organise events per tab bar; the closing barline is included
//		List<List<Event>> eventsPerBar = new ArrayList<>();
//		// Get events; remove any decorative opening barlines (affecting XML bar numbering)
//		List<Event> events = 
//			Encoding.removeDecorativeBarlineEvents(tab.getEncoding().getEvents());
////		System.out.println(events.get(events.size() - 2).getEncoding());
////		System.out.println(events.get(events.size() - 1).getEncoding());
//
//		int currBar = events.get(0).getBar();
//		// Make eventsPerBar. Per getEvents(), each bar ends with a barline, which should
//		// not be retained in eventsPerBar
//		List<Event> eventsBar = new ArrayList<>();
//		for (int i = 0; i < events.size(); i++) {
//			Event e = events.get(i);
//			//////////
//			if (!Encoding.assertEventType(e.getEncoding(), tss, "MensurationSign")) {
//				if (!Encoding.assertEventType(e.getEncoding(), tss, "barline")) {
//					eventsBar.add(e);
//				}
//				else {
//					eventsPerBar.add(eventsBar);
//					eventsBar.forEach(ev -> System.out.println(ev.getEncoding()));
//					eventsBar = new ArrayList<>();
//				}
//			}
//			//////////
//			
////			// Next bar or final event: remove barline (last event in eventsBar) and add
////			if (events.get(i).getBar() == currBar + 1 || i == events.size() - 1) {
////				eventsBar.remove(eventsBar.size()-1);
////				eventsPerBar.add(eventsBar);
////				eventsBar = new ArrayList<>();
////				currBar++;
////			}
////			eventsBar.add(events.get(i));
//		}
//
////		for (List<Event> l : eventsPerBar) {
////			l.forEach(e -> System.out.println(e.getEncoding()));
////			System.out.println("end of bar");
////		}
////		System.exit(0);
//
//		for (int i = 0; i < eventsPerBar.size(); i++) {
//			System.out.println("bar = " + (i+1));
////			Integer[] tbtmb = tabBarsToMetricBars.get(i);
//			Integer[] currTbi = tbi.get(i);
//			
////			System.out.println("tbtmb = " + Arrays.asList(tbtmb));
////			System.out.println("events in this bar");
////			eventsPerBar.get(i).forEach(e -> System.out.println(e.getEncoding()));
////			System.out.println("***");
//			if (startNewBar) {
//				currBarAsXML = new ArrayList<>();
//				currBarAsXML.add("<staff n='" + staff + "'>");
//				currBarAsXML.add(TAB + "<layer n='1'>");
//			}
//			List<Event> currBarEvents = eventsPerBar.get(i);
//			List<Integer> currBarDurs = new ArrayList<>();
////			for (Event e : currBarEvents) {
////				String en = e.getEncoding();
////				if (Encoding.assertEventType(en, tss, "RhythmSymbol") ||
////					Encoding.assertEventType(en, tss, "rest")) {
////					RhythmSymbol rs = Symbol.getRhythmSymbol(
////						en.substring(0, en.indexOf(ss))
////					); 
////					currBarDurs.add(rs.getDuration());
////					prevDur = rs.getDuration();
////				}
////				else if (Encoding.assertEventType(en, tss, "TabSymbol") &&
////					!(Encoding.assertEventType(en, tss, "RhythmSymbol"))) {
////					currBarDurs.add(prevDur);
////				}
////				else {
////					currBarDurs.add(-1);
////				}
////			}
//			
////			System.out.println("CBD = " + currBarDurs);
////			currBarEvents.forEach(e -> System.out.println(e.getEncoding()));
////			if (i == 4) {
////				System.exit(0);
////			}
//			
//			int currTabBar = currBarEvents.get(0).getBar();
////			int currMetricBar = tabBarsToMetricBars.get(currTabBar - 1)[Tablature.METRIC_BAR_IND];
////			int currDim = tab.getEncoding().getTimeline().getDiminution(currMetricBar);
//
//			// For each event in the bar
//			Rational barLen = Rational.ZERO;
////			int currOffsetInSRV = 0;
//			for (int j = 0; j < currBarEvents.size(); j++) {
//				Event currEventFull = currBarEvents.get(j);
//				System.out.println("j = " + j);
//				System.out.println("e = " + currEventFull.getEncoding());
//				String en = currEventFull.getEncoding();
//				boolean isTSEvent = Encoding.assertEventType(en, tss, "TabSymbol"); 
//				boolean isRSEvent = Encoding.assertEventType(en, tss, "RhythmSymbol");
//				boolean isRestEvent = Encoding.assertEventType(en, tss, "rest");
//
//				int currEventDur = 0;
////				int currEventDur = currBarDurs.get(j);
//				if (isRSEvent || isRestEvent) {
//					RhythmSymbol rs = Symbol.getRhythmSymbol(
//						en.substring(0, en.indexOf(ss))
//					); 
//					currEventDur = rs.getDuration();
////					currBarDurs.add(rs.getDuration());
//					prevDur = rs.getDuration();
//				}
//				else if (isTSEvent && !isRSEvent) {
//					currEventDur = prevDur;
////					currBarDurs.add(prevDur);
//				}
////				else {
////					currEventDur = -1;
//////					currBarDurs.add(-1);
////				}
//				
////				int currEventDur = currBarDurs.get(j);
////				if (currEventDur == -1) { // MS or barline
////					currEventDur = 0;
////				}
//				
//				currOffsetInTab += currEventDur;
//
////				System.out.println("- - - - -> " + currEventDur);
////				System.out.println("---------> " + currOffsetInTab);
//				String currEvent = 
//					StringTools.removeTrailingSymbolSeparator(currEventFull.getEncoding());
//				String currEventOrig = currEventFull.getFootnote();
//				// Extract correction
//				boolean isCorrected = false;
//				if (currEventOrig != null) {
//					if (currEventOrig.contains("'")) {
//						currEventOrig = currEventOrig.substring(
//							currEventOrig.indexOf("'")+1, currEventOrig.lastIndexOf("'")
//						);
//						isCorrected = true;
//					}
//					else {
//						currEventOrig = currEventOrig.substring(
//							currEventOrig.indexOf(Encoding.FOOTNOTE_INDICATOR) + 1
//						);
//					}
//					currEventOrig = StringTools.removeTrailingSymbolSeparator(currEventOrig);
//				}
//
////				boolean isBarline = 
////					Symbol.getConstantMusicalSymbol(currEvent) != null && 
////					Symbol.getConstantMusicalSymbol(currEvent).isBarline();
//				if (!(Encoding.assertEventType(currEvent, tss, "barline"))) {
////				if (!isBarline) {
//					// Get XML durations of currEvent, and, if applicable, currEventOrig
//					Integer[] currDurXML = getXMLDur(currEvent);
//					
////					if (currDurXML != null) {
////						currOffsetInSRV += getDur(currDurXML[0], currDurXML[1]);
////					}
////					else {
////						currOffsetInSRV += getDur(prevDurXML[0], prevDurXML[1]);
////					}
////					System.out.println(currOffsetInSRV);
//					
////					if (currDurXML == null) {
//////						Rational currDurRat = new Rational(1, prevDurXML[0]);
////						if (prevDurXML[1] != 0) {
//////							currDurRat = currDurRat.mul(prevDurXML[1]);
////						}
//////						barLen = barLen.add(prevDurXML);
////					}
////					else {
//////						barLen = barLen.add();
////					}
//
//					String sicEvent = !isCorrected ? currEvent : currEventOrig;
//					String corrEvent = !isCorrected ? null : currEvent;
//
//					boolean defaultCase = !isCorrected; 
//					boolean oneReplacedByMultiple = isCorrected && sicEvent.endsWith(sp);
//					boolean multipleReplacedByOne = 
//						isCorrected && sicEvent.contains(sp) && !sicEvent.endsWith(sp);
//					boolean defaultCorrectedCase = 
//						isCorrected && !oneReplacedByMultiple && !multipleReplacedByOne;
//
//					List<String> sicList = new ArrayList<>();
//					List<String> corrList = new ArrayList<>();
//					// No <sic> and <corr>
//					if (defaultCase) {
//						sicList.add(sicEvent);
//					}
//					// Both <sic> and <corr> contain one <tabGrp>
//					if (defaultCorrectedCase) {
//						sicList.add(sicEvent);
//						corrList.add(corrEvent);
//					}
//					// <sic> contains multiple <tabGrp>s, <corr> one 
//					if (multipleReplacedByOne) {
//						for (String s : sicEvent.split(sp + ss)) {
//							sicList.add(StringTools.removeTrailingSymbolSeparator(s));
//						}
//						corrList.add(corrEvent);
//					}
//					// <sic> contains one <tabGrp>, <corr> multiple: corrList contains corrEvent 
//					// plus all events following that together have the same duration as durSic
//					// NB It is assumed that the replacement is within the bar
//					if (oneReplacedByMultiple) {
//						sicEvent = sicEvent.substring(0, sicEvent.indexOf(sp));
//						sicList.add(StringTools.removeTrailingSymbolSeparator(sicEvent));
//						corrList.add(corrEvent);
//						RhythmSymbol rsSic = 
//							Symbol.getRhythmSymbol(sicEvent.substring(0, sicEvent.indexOf(ss)));
//						int durSic; 
//						if (rsSic != null) {
//							durSic = rsSic.getDuration();
//						}
//						else {
//							durSic = -1; // get last specified duration before currEvent
//						}
//						RhythmSymbol rsCorr = 
//							Symbol.getRhythmSymbol(corrEvent.substring(0, corrEvent.indexOf(ss)));
//						int durCorr;
//						if (rsCorr != null) {
//							durCorr = rsCorr.getDuration();
//						}
//						else {
//							durCorr = -1; // get last specified duration before currEvent
//						}
//						int durCorrToTrack = durCorr;
//						// Iterate through next events until durCorr equals durSic
//						for (int l = j+1; l < currBarEvents.size(); l++) {
//							Event nextEventFull = currBarEvents.get(l);
//							String nextEvent = 
//								StringTools.removeTrailingSymbolSeparator(nextEventFull.getEncoding());
//							String nextEventOrig = nextEventFull.getFootnote();
//							// If the next element has a footnote
//							if (nextEventOrig != null) {
//								nextEventOrig = StringTools.removeTrailingSymbolSeparator(nextEventOrig);
//								corrList.add(nextEvent);
//								// Determine duration of corrected event, increment durCorr,
//								// and update durrCorrToTrack
//								RhythmSymbol nextEventRS = Symbol.getRhythmSymbol(
//									nextEvent.substring(0, nextEvent.indexOf(ss))
//								);
//								int durCorrNext;
//								if (nextEventRS != null) {
//									durCorrNext = nextEventRS.getDuration();
//									durCorrToTrack = durCorrNext;
//								}
//								else {
//									durCorrNext = durCorrToTrack;
//								}
//								durCorr += durCorrNext;		
//							}
//							if (durCorr == durSic) {
//								int eventsToSkip = corrList.size() - 1;
//								j += eventsToSkip;
//								break;
//							}
//						}
//					}
//
//					List<String> currEventAsXML = new ArrayList<>();
//					if (isCorrected) {
//						currEventAsXML.add(TAB.repeat(2) + "<choice>");
//						currEventAsXML.add(TAB.repeat(3) + "<sic>");
//					}
//					currEventAsXML.addAll(getTabGrps(sicList, prevDurXML, isCorrected, false, tss));
//					if (isCorrected) {
//						currEventAsXML.add(TAB.repeat(3) + "</sic>");
//					}
//					if (isCorrected) {
//						currEventAsXML.add(TAB.repeat(3) + "<corr>");
//						currEventAsXML.addAll(getTabGrps(corrList, prevDurXML, isCorrected, false, tss));
//						currEventAsXML.add(TAB.repeat(3) + "</corr>");
//					}
//					if (isCorrected) {
//						currEventAsXML.add(TAB.repeat(2) + "</choice>");
//					}
//					currBarAsXML.addAll(currEventAsXML);
//
//					// Update prevDurXML
//					// a. Set prevDurXML to currDurXML (or, if it is null, to the last 
//					// XML duration)
//					if (defaultCase || defaultCorrectedCase || multipleReplacedByOne) {
//						if (currDurXML != null) {
//							prevDurXML = currDurXML;
//						}
//					}
//					// b. Set prevDurXML to the last specified duration in corrList  
//					if (oneReplacedByMultiple) {
//						List<String> corrListRev = new ArrayList<>(corrList);
//						Collections.reverse(corrListRev);
//						for (String event : corrListRev) {
//							Integer[] lastDurXML = getXMLDur(event);
//							if (lastDurXML != null) {
//								prevDurXML = lastDurXML;
//								break;
//							}
//						}
//					}
//				} // End of if (!isBarline)
//
//				boolean endOfTabBar = (j == currBarEvents.size() - 1);
//				boolean endOfMetricBar = metricBarOnsets.contains(currOffsetInTab);
////				if (j == currBarEvents.size() - 1) {
////					currBarAsXML.forEach(s -> System.out.println(s));
////				}
////				System.out.println(currBarEvents.get(currBarEvents.size()-1).getEncoding());
//
//				if (endOfTabBar) {
//					System.out.println("endOfTabBar " + endOfTabBar);
//					System.out.println(currOffsetInTab);
//				}
//				if (endOfMetricBar) {
//					System.out.println("endOfMetricBar " + endOfMetricBar);
//					System.out.println(currOffsetInTab);
//				}
//
////				if (endOfTabBar) {
////					if (endOfMetricBar) {
//////						System.out.println("metric and tab");/
////						currBarAsXML.add(TAB + "</layer>");
////						currBarAsXML.add("</staff>");
////						tabBars.add(currBarAsXML);
////						startNewBar = true;
////					}
////					else {
//////						System.out.println("only tab");
////						currBarAsXML.add(TAB.repeat(2) + "<barLine/>");
////						startNewBar = false;
////					}
////				}
////				else {
////					if (endOfMetricBar) {
//////						System.out.println("metric and tab");
////						currBarAsXML.add(TAB + "</layer>");
////						currBarAsXML.add("</staff>");
////						tabBars.add(currBarAsXML);
////						startNewBar = true;
////					}
////				}
//				
//
//				// Place metric barline and tab barline
//				if (endOfTabBar && endOfMetricBar) {
//					System.out.println("metric and tab");
//					currBarAsXML.add(TAB + "</layer>");
//					currBarAsXML.add("</staff>");
//					tabBars.add(currBarAsXML);
//					startNewBar = true;
//				}
//				// Place only tab barline
//				else if (endOfTabBar && !endOfMetricBar) {
//					System.out.println("only tab");
//					currBarAsXML.add(TAB.repeat(2) + "<barLine/>");
//					startNewBar = false;
//				}
//				// Place only metric barline
//				else if (endOfMetricBar && !endOfTabBar) {
//					System.out.println("only metric");
//					currBarAsXML.add(TAB + "</layer>");
//					currBarAsXML.add("</staff>");
//					tabBars.add(currBarAsXML);
//					currBarAsXML = new ArrayList<>();
////					startNewBar = true;
//					currBarAsXML.add("<staff n='" + staff + "'>");
//					currBarAsXML.add(TAB + "<layer n='1'>");
////					currBarAsXML.add(TAB.repeat(2) + "<barLine/>");
//					startNewBar = false;
//				}
//			} // loop over currBarEvents
//			System.out.println("---------> currOffsetInTab at end of bar = " + currOffsetInTab);
//			
////			System.out.println("* * * * * * * * * * *");
////			currBarAsXML.forEach(s -> System.out.println(s));
////			System.out.println("* * * * * * * * * * *");
//
//			boolean oldWay = false;
//			if (oldWay) {
//				Integer[] tbtmb = tabBarsToMetricBars.get(i);
//				// Tab and metric bar overlap fully (tab bar:metric bar = 1:1 case)
//				boolean equalToMetric = 
//					tbtmb[Tablature.LAST_METRIC_BAR_IND] == -1 &&	
//					tbtmb[Tablature.TAB_BAR_REL_ONSET_IND] == 0 && 
//					tbtmb[Tablature.METRIC_BAR_REMAINDER_IND] == 0;
//				// Tab bar overlaps start of metric bar (tab bar:metric bar n:1 and 3:2 case)
//				boolean overlapsStartMetric = 
//					tbtmb[Tablature.LAST_METRIC_BAR_IND] == -1 &&	
//					tbtmb[Tablature.TAB_BAR_REL_ONSET_IND] == 0 && 
//					tbtmb[Tablature.METRIC_BAR_REMAINDER_IND] > 0;
//					// Tab bar overlaps middle of metric bar (tab bar:metric bar n:1 case)
//				boolean overlapsMiddleMetric = 
//					tbtmb[Tablature.LAST_METRIC_BAR_IND] == -1 &&
//					tbtmb[Tablature.TAB_BAR_REL_ONSET_IND] > 0 && 
//					tbtmb[Tablature.METRIC_BAR_REMAINDER_IND] > 0;	
//					// Tab bar overlaps end of current and start of next metric bar (tab bar:metric bar 3:2 case)
//				boolean overlapsEndAndStartMetric = 
//					tbtmb[Tablature.LAST_METRIC_BAR_IND] != -1;
//				// Tab bar overlaps end of metric bar (tab bar:metric bar n:1 and 3:2 case)
//				boolean overlapsEndMetric = 
//					tbtmb[Tablature.LAST_METRIC_BAR_IND] == -1 &&
//					tbtmb[Tablature.TAB_BAR_REL_ONSET_IND] > 0 && 
//					tbtmb[Tablature.METRIC_BAR_REMAINDER_IND] == 0;
//
//				int tabBar = i+1;	
//				int tabBarOnset = currTbi[0];
//				int tabBarDur = currTbi[1];
//				int tabBarOffset = currTbi[2];
//				Rational[] mp = tl.getMetricPosition(tabBarOnset);
//				int metricBar = mp[0].getNumer();
//				Rational mpInMetricBar = mp[1];
//				mpInMetricBar.reduce();
//			
//				Integer[] currMbi = mbi.get(metricBar - 1);
//				int metricBarOnset = currMbi[0];
//				int metricBarDur = currMbi[1];
//				int metricBarOffset = currMbi[2];
//						
//				boolean tabBarEndsBeforeMetricBar = tabBarOffset < metricBarOffset;
//				boolean tabBarEndsWithMetricBar = tabBarOffset == metricBarOffset;
//				boolean tabBarEndsAfterMetricBar = tabBarOffset > metricBarOffset;
//			
//				boolean tabBarInterruptsMetricBar = tabBarOffset < metricBarOffset;
//				boolean metricBarInterruptsTabBar = tabBarOffset > metricBarOffset;
////				boolean tabBarEndsWithMetricBar = tabBarOffset == metricBarOffset;
//			
////				System.out.println("tab bar onset, dur, offset    = " + tabBarOnset + ", " + tabBarDur + ", " + tabBarOffset);
////				System.out.println("metric bar                    = " + metricBar);
////				System.out.println("pos in metric bar             = " + mpInMetricBar);
////				System.out.println("metric bar onset, dur, offset = " + metricBarOnset + ", " + metricBarDur + ", " + metricBarOffset);
////				if (tabBarEndsBeforeMetricBar) {
////					System.out.println("tab ends before metric");
////				}
////				if (tabBarEndsWithMetricBar) {
////					System.out.println("tab ends with metric");
////				}
////				if (tabBarEndsAfterMetricBar) {
////					System.out.println("tab ends after metric");
////				}
////				System.out.println("-------------------------------");
//			
//			
//				// Place metric barline and tab barline
////				if (metricBarOnsets.contains(o))
//				if (equalToMetric || overlapsEndMetric) {
////					System.out.println("equalToMetric || overlapsEndMetric");
//					currBarAsXML.add(TAB + "</layer>");
//					currBarAsXML.add("</staff>");
//					tabBars.add(currBarAsXML);
//					startNewBar = true;
//				}
//				// Place only tab barline
//				else if (overlapsStartMetric || overlapsMiddleMetric) {
////					System.out.println("overlapsStartMetric || overlapsMiddleMetric");
//					currBarAsXML.add(TAB.repeat(2) + "<barLine/>");
//					startNewBar = false;
//				}
////				else if (overlapsMiddleMetric) {
//////				System.out.println("i = " + i + ", overlapsMiddleMetric");
////					currBarAsXML.add(TAB.repeat(2) + "<barLine/>");
////					startNewBar = false;
////				}
//				// Place only metric barline
//				else if (overlapsEndAndStartMetric) {
////					System.out.println("overlapsEndAndStartMetric");
//				
//					// Get the duration of the end part of the tab bar (belonging to the next metric bar) 
//					int toNextMetricBarDur = tabBarsToMetricBars.get(i+1)[Tablature.TAB_BAR_REL_ONSET_IND];
//
//					// Get the durations of the tabGrps that fall into the end part
//					List<Integer> tabGrpDurs = new ArrayList<>();
//					for (int k = currBarDurs.size() - 1; k > 0; k--) {
//						tabGrpDurs.add(currBarDurs.get(k));
//						if (ToolBox.sumListInteger(tabGrpDurs) == toNextMetricBarDur) {
//							break;
//						}
//					}
//
//					// Move the tabGrps that fall into the end part from currBarAsXML to nextCurrBarAsXML
//					List<String> nextCurrBarAsXML = new ArrayList<>();
//					for (int k = currBarAsXML.size() - 1; k > 0; k--) {
//						nextCurrBarAsXML.add(0, currBarAsXML.get(k));
//						if (StringUtils.countMatches(String.join(",", nextCurrBarAsXML), 
//							"<tabGrp") == tabGrpDurs.size()) {
//							break;
//						}
//					}
//					currBarAsXML = currBarAsXML.subList(0, currBarAsXML.size() - nextCurrBarAsXML.size());
//					// Complete currBarAsXML with staff and layer elements and add
//					currBarAsXML.add(TAB + "</layer>");
//					currBarAsXML.add("</staff>");
//					tabBars.add(currBarAsXML);
//
//					// Complete nextCurrBarAsXML with staff and layer elements and set currBarAsXML
//					nextCurrBarAsXML.add(0, "<staff n='" + staff + "'>");
//					nextCurrBarAsXML.add(1, TAB + "<layer n='1'>");
//					nextCurrBarAsXML.add(TAB.repeat(2) + "<barLine/>");
//					currBarAsXML = nextCurrBarAsXML;
//					startNewBar = false;
//
////						for (int k = currBarAsXML.size() - 1; k > 0; k--) {
////							String s = currBarAsXML.get(k);
////							toNextMetricBar.add(0, s);
////							if (s.contains("<tabGrp")) {
////								int start = s.indexOf("dur='") + "dur='".length();
////								int XMLDurInt = Integer.parseInt(s.substring(start, s.indexOf("'", start)));
////								int dotsInt = 0;
////								if (s.contains("dots='")) {
////									start = s.indexOf("dots='") + "dots='".length();
////									dotsInt = Integer.parseInt(s.substring(start, s.indexOf("'", start)));
////								}
////								cumulativeDur += getDur(XMLDurInt, dotsInt);
////								if (cumulativeDur == toNextMetricBarDur) {
////									break;
////								}
////							}
////						}
//				}
//			}
//				
////			// Place metric barline and tab barline
////			else if (overlapsEndMetric) {
//////				System.out.println("i = " + i + ", overlapsEndMetric");
////				currBarAsXML.add(TAB + "</layer>");
////				currBarAsXML.add("</staff>");
////				tabBars.add(currBarAsXML);
////				startNewBar = true;
////			}
//
////			boolean combineTabBars = 			
//////			tabBarsToMetricBars.get(i + 1)[Tablature.METRIC_BAR_IND] == currMetricBar;
////			tabBarsToMetricBars.get(i)[Tablature.METRIC_BAR_REMAINDER_IND] != 0;
//
////			}
//				
////			else {
////				currBarAsXML.add(TAB + "</layer>");
////				currBarAsXML.add("</staff>");
////				tabBars.add(currBarAsXML);
////				startNewBar = true;
////			}
//				
//			boolean old = false;
//			if (old) {
//				boolean combineTabBars = 
//					i < eventsPerBar.size() - 1 && 
////					tabBarsToMetricBars.get(i + 1)[Tablature.METRIC_BAR_IND] == currMetricBar;
//					tabBarsToMetricBars.get(i)[Tablature.METRIC_BAR_REMAINDER_IND] != 0;
//				// tabBars follows tablature 'barring'; combine any tab bars to follow metric barring
//				// In case of (i) last tab bar or (ii) where there is a next tab bar that does not 
//				// have currMetricBar
//				if (i == eventsPerBar.size() - 1 || !combineTabBars) {
//					currBarAsXML.add(TAB + "</layer>");
//					currBarAsXML.add("</staff>");
//					tabBars.add(currBarAsXML);
//					startNewBar = true;
//				}
//				// In case where there is a next tab bar that has currMetricBar
//				else if (combineTabBars) {				
//					currBarAsXML.add(TAB.repeat(2) + "<barLine/>");
//					startNewBar = false;
//				}
//			}
//
////			else {
////				currBarAsXML.add(TAB + "</layer>");
////				currBarAsXML.add("</staff>");
////				tabBars.add(currBarAsXML);
////				startNewBar = true;
////			}
//
////			// Append meter change (if applicable)
////			boolean doThis = false;
////			if (doThis) {
////				if (i < ebf.size()-1) {
////					// Check for meter change in first event of next bar
////					List<String[]> nextBar = ebf.get(i+1);
////					String[] firstEventNextFull = nextBar.get(0);
////					String firstEventNext = firstEventNextFull[Encoding.EVENT_IND];
////					// Remove final ss
////					firstEventNext = firstEventNext.substring(0, firstEventNext.lastIndexOf(ss));
////					String[] firstEventNextSplit = 
////						(!firstEventNext.contains(ss)) ? new String[]{firstEventNext} : 
////						firstEventNext.split("\\" + ss);
////					// Meter change found? Add scoreDef after bar
////					if (MensurationSign.getMensurationSign(firstEventNextSplit[0]) != null) {
////						String meterStr = "";
////						for (String s : meters.get(meterIndex+1)) {
////							if (!s.equals("")) {
////								meterStr += s + " ";
////							}
////						}
////						meterStr = meterStr.trim();
////						String scoreDef = INDENT_TWO + "<scoreDef" + " " + meterStr + "/>" + "\r\n";
//////						sb.append(scoreDef);
//////						measuresTab.addAll(scoreDef);
////						meterIndex++;
////					}
////				}
////			}
//
//		} // loop over eventsPerBar
//
////		// tabBars follows tablature 'barring'; combine any tab bars to follow metric barring
////		if (alignWithMetricBarring) {
////			List<List<String>> tabBarsCombined = new ArrayList<>();
////			int tabBarInd = 0;
////			int numMetricBars = mi.get(mi.size()-1)[Transcription.MI_LAST_BAR];
////			for (int metricBarInd = 0; metricBarInd < numMetricBars; metricBarInd++) {
////				List<String> currTabBar = new ArrayList<>(tabBars.get(tabBarInd));
////				int currMetricBar = metricBarInd + 1;
//////				String currTabBarAsStr = "";
//////				int metricBarInd = j;
//////				currMeasureTabAsStr += INDENT_TWO + "<measure n='" + currMeasure + "'" + 
//////					((j == numBars-1) ? " right='end'" : "") + ">" + "\r\n";
//////				// The elements in tabBars start at <tabGrp> level
//////				currTabBarAsStr += INDENT_TWO + TAB + "<staff n='1'>" + "\r\n";
//////				currTabBarAsStr += INDENT_TWO + TAB.repeat(2) + "<layer n='1'>" + "\r\n";
//////				for (String line : tabBars.get(tabBarInd)) {
//////					currTabBarAsStr += INDENT_TWO + TAB.repeat(3) + line + "\r\n";
//////				}
////			
////				// Add any next tab bars with the same metric bar
////				int startInd = tabBarInd + 1;
//////				if (startInd != tabBarsToMetricBars.size()) {
////				for (int j = startInd; j < tabBarsToMetricBars.size(); j++) {
////					if (tabBarsToMetricBars.get(j)[1] == currMetricBar) {
////						currTabBar.add(TAB.repeat(2) + "<barline/>");
//////						currTabBarAsStr += INDENT_TWO + TAB.repeat(3) + "<barline/>" + "\r\n";
////						for (String line : tabBars.get(j)) {
////							currTabBar.add(line);
//////							currTabBarAsStr += INDENT_TWO + TAB.repeat(3) + line + "\r\n";
////						}
////						// Additional increment of tabBarInd
////						tabBarInd++;
////					}
////					else {
////						break;
////					}
////				}
////				tabBarsCombined.add(currTabBar);
//////				}
////				// Normal increment of tabBarInd (together with metricBarInd)
////				tabBarInd++;
//////				currTabBarAsStr += INDENT_TWO + TAB.repeat(2) + "</layer>" + "\r\n";
//////				currTabBarAsStr += INDENT_TWO + TAB + "</staff>";
//////				tabBarsAsStr.add(currTabBarAsStr);
////			}
////			tabBars = tabBarsCombined;
////		}
//
//		return tabBars;
//	}


//	private static List<List<String>> getTabBarsOLDEST(Tablature tab, boolean alignWithMetricBarring,
//			int staff) {
//			System.out.println("\r\n>>> getTabBars() called");
//			List<List<String>> tabBars = new ArrayList<>();
//			
//			String ss = Symbol.SYMBOL_SEPARATOR;
//			String sp = Symbol.SPACE.getEncoding();
//			TabSymbolSet tss = tab.getEncoding().getTabSymbolSet();
//			List<Integer[]> mi = tab.getMeterInfo();
////			List<Integer[]> mi = tab.getTimeline().getMeterInfoOBS();
////			List<String[]> meters = new ArrayList<>();
////			int meterIndex = 0;
//			List<Integer[]> tabBarsToMetricBars = null; // tab.mapBarsToMetricBars();
//			System.out.println("xxx tabBarsToMetricBars xxx");
//			for (Integer[] in : tabBarsToMetricBars) {
//				System.out.println(Arrays.toString(in));
//			}
//
////			List<Integer> xmlDurPerBar = new ArrayList<>();
////			Rational prevDur = null;
////			for (List<String[]> bar : ebf) {
////				Rational currXmlDur = Rational.ZERO;
////				int barNum = Integer.parseInt(bar.get(0)[1]);
////				for (String[] eventInfo : bar) {
////					String event = eventInfo[0];
////					System.out.println(event);
////					Integer[] xmlDurDots = getXMLDur(event);
////					String first = event.substring(0, event.indexOf(ss));
////					// If the event is not a barline event or a MS event
////					if (!ConstantMusicalSymbol.isBarline(first)
////						&& MensurationSign.getMensurationSign(first) == null) {
////						// If the event starts with a RS
////						Rational xmlDur = null;
////						if (RhythmSymbol.getRhythmSymbol(first) != null) {
////							xmlDur = new Rational(1, xmlDurDots[0]);
////							// Add the value for any dots
////							for (int i = 0; i < xmlDurDots[1]; i++) {
////								xmlDur = xmlDur.add(xmlDur.mul(new Rational(1, 2)));
////							}
////							prevDur = xmlDur;
////						}
////						// If the event does not start with a RS
////						else {
////							xmlDur = prevDur; 
////						}
////						currXmlDur = currXmlDur.add(xmlDur);
////					}
////				}
////				// Apply diminution
////				int finalXmlDur = (int) (currXmlDur.div(new Rational(1, 8)).toDouble());
////				List<Integer> diminutions = ToolBox.getItemsAtIndex(mi, Tablature.MI_DIM);
////				List<Rational[]> meterChangesByMetricPos = new ArrayList<>();
////				Rational firstMetPos = Rational.ZERO;
////				for (Integer[] in : mi) {
////					int numBarsInMeter = 
////						in[Transcription.MI_LAST_BAR] - in[Transcription.MI_FIRST_BAR] + 1;
////					Rational meter = new Rational(in[Transcription.MI_NUM], in[Transcription.MI_DEN]);
////					System.out.println(meter);
////					System.out.println(numBarsInMeter);
////					Rational lastMetPos = firstMetPos.add(meter.mul(numBarsInMeter));
////					meterChangesByMetricPos.add(new Rational[]{firstMetPos, lastMetPos});
////					firstMetPos = lastMetPos;
////				}
////				for (Rational[] r : meterChangesByMetricPos) {
////					System.out.println(Arrays.toString(r));
////				}
////				int dim = 1;
////				
////				xmlDurPerBar.add(finalXmlDur);
////			}
////			System.out.println(xmlDurPerBar);
//
//			// For each bar
//			List<String> currBarAsXML = new ArrayList<>();
//			Integer[] prevDurXML = new Integer[]{0, 0};
//			boolean startNewBar = true;
//			// Get events; remove any decorative opening barlines (affecting XML bar numbering)
//			List<Event> events = 
//				Encoding.removeDecorativeBarlineEvents(tab.getEncoding().getEvents());
//
////			events.forEach(e -> System.out.println("bar " + e.getBar() + " : " + e.getEncoding()));
////			System.exit(0);
////			System.out.println(events.size());
////			for (Event e : events) {
////				System.out.println(e.getBar());
////				System.out.println(e.getEncoding());
////			}
////			System.exit(0);
//			
//			// Organise events per bar
//			List<List<Event>> eventsPerBar = new ArrayList<>();
//			int currBar = events.get(0).getBar();
//			List<Event> eventsBar = new ArrayList<>();
//			for (int i = 0; i < events.size(); i++) {
//				// Next bar or final event: add
//				if (events.get(i).getBar() == currBar + 1 || i == events.size() - 1) {
//					eventsPerBar.add(eventsBar);
//					eventsBar = new ArrayList<>();
//					currBar++;
//				}
//				eventsBar.add(events.get(i));		
//			}
////			System.out.println(eventsPerBar.size());
////			for (Event l : eventsPerBar.get(1)) {
////				System.out.println(l.getEncoding());
////			}
////			System.exit(0);
//			int prevDur = -1;
//			for (int i = 0; i < eventsPerBar.size(); i++) {
//				System.out.println("i = " + i);
//				if (startNewBar) {
//					currBarAsXML = new ArrayList<>();
//					currBarAsXML.add("<staff n='" + staff + "'>");
//					currBarAsXML.add(TAB + "<layer n='1'>");
//				}
//				List<Event> currBarEvents = eventsPerBar.get(i);
//				List<Integer> currBarDurs = new ArrayList<>();
////				System.out.println(("i = " + i));
//				for (Event e : currBarEvents) {
////					System.out.println(e.getEncoding());
//					if (Encoding.assertEventType(e.getEncoding(), tss, "RhythmSymbol") ||
//						Encoding.assertEventType(e.getEncoding(), tss, "rest")) {
//						RhythmSymbol rs = 
//							Symbol.getRhythmSymbol(e.getEncoding().substring(0, 
//							e.getEncoding().indexOf(Symbol.SYMBOL_SEPARATOR))); 
//						currBarDurs.add(rs.getDuration());
//						prevDur = rs.getDuration();
//					}
//					else if (Encoding.assertEventType(e.getEncoding(), tss, "TabSymbol") &&
//						!(Encoding.assertEventType(e.getEncoding(), tss, "RhythmSymbol"))) {
//						currBarDurs.add(prevDur);
//					}
//				}
////				System.out.println("currBarDurs:");
////				System.out.println(currBarDurs);
//
////				int currTabBar = events.get(i).getBar();
//				int currTabBar = currBarEvents.get(0).getBar();
////				List<Event> currBarEvents = new ArrayList<>();
////				for (int j = i; j < events.size(); j++) {
////					if (events.get(j).getBar() == currTabBar + 1) {
////						i = j - 1;
////						break;
////					}
////					currBarEvents.add(events.get(j));
////				}
//				int currMetricBar = tabBarsToMetricBars.get(currTabBar - 1)[Tablature.METRIC_BAR_IND];
////				System.out.println(currMetricBar);
//				int currDim = tab.getEncoding().getTimeline().getDiminution(currMetricBar);
//
//				// For each event
//				Rational barLen = Rational.ZERO;
//				for (int j = 0; j < currBarEvents.size(); j++) {
//					Event currEventFull = currBarEvents.get(j);
////					String[] currEventFull = currBarEvents.get(j);
//					String currEvent = 
//						StringTools.removeTrailingSymbolSeparator(currEventFull.getEncoding());
////					String currEvent = 
////						removeTrailingSymbolSeparator(currEventFull[Encoding.EVENT_IND]);
//					String currEventOrig = currEventFull.getFootnote();
////					String currEventOrig = currEventFull[Encoding.FOOTNOTE_IND];
//					// Extract correction
//					boolean isCorrected = false;
//					if (currEventOrig != null) {
//						if (currEventOrig.contains("'")) {
//							currEventOrig = currEventOrig.substring(currEventOrig.indexOf("'")+1,
//								currEventOrig.lastIndexOf("'"));
//							isCorrected = true;
//						}
//						else {
//							currEventOrig = 
//								currEventOrig.substring(currEventOrig.indexOf(Encoding.FOOTNOTE_INDICATOR) + 1);
//						}
//						currEventOrig = StringTools.removeTrailingSymbolSeparator(currEventOrig);
//					}
//
////					// Barline? End of bar reached; set barline if not single
////					if (ConstantMusicalSymbol.isBarline(currEvent)) {
////						if (currEvent.equals(ConstantMusicalSymbol.DOUBLE_BARLINE.getEncoding())) {
////							barline = " right='dbl'";
////						}
////						if (i == ebf.size()-1) {
////							barline = " right='end'";
////						}
////					}
//					boolean isBarline = 
//						Symbol.getConstantMusicalSymbol(currEvent) != null && Symbol.getConstantMusicalSymbol(currEvent).isBarline();
//					if (!isBarline) {
////					if (Symbol.getConstantMusicalSymbol(currEvent) != null && !Symbol.getConstantMusicalSymbol(currEvent).isBarline()) {
////					if (!ConstantMusicalSymbol.isBarline(currEvent)) {
//						// Get XML durations of currEvent, and, if applicable, currEventOrig
//						Integer[] currDurXML = getXMLDur(currEvent);
//						if (currDurXML == null) {
////							Rational currDurRat = new Rational(1, prevDurXML[0]);
//							if (prevDurXML[1] != 0) {
////								currDurRat = currDurRat.mul(prevDurXML[1]);
//							}
////							barLen = barLen.add(prevDurXML);
//						}
//						else {
////							barLen = barLen.add();
//						}
//
//						String sicEvent = !isCorrected ? currEvent : currEventOrig;
//						String corrEvent = !isCorrected ? null : currEvent;
//
//						boolean defaultCase = !isCorrected; 
//						boolean oneReplacedByMultiple = isCorrected && sicEvent.endsWith(sp);
//						boolean multipleReplacedByOne = 
//							isCorrected && sicEvent.contains(sp) && !sicEvent.endsWith(sp);
//						boolean defaultCorrectedCase = 
//							isCorrected && !oneReplacedByMultiple && !multipleReplacedByOne;
//
//						List<String> sicList = new ArrayList<>();
//						List<String> corrList = new ArrayList<>();
//						// No <sic> and <corr>
//						if (defaultCase) {
//							sicList.add(sicEvent);
//						}
//						// Both <sic> and <corr> contain one <tabGrp>
//						if (defaultCorrectedCase) {
//							sicList.add(sicEvent);
//							corrList.add(corrEvent);
//						}
//						// <sic> contains multiple <tabGrp>s, <corr> one 
//						if (multipleReplacedByOne) {
//							for (String s : sicEvent.split(sp + ss)) {
//								sicList.add(StringTools.removeTrailingSymbolSeparator(s));
//							}
//							corrList.add(corrEvent);
//						}
//						// <sic> contains one <tabGrp>, <corr> multiple: corrList contains corrEvent 
//						// plus all events following that together have the same duration as durSic
//						// NB It is assumed that the replacement is within the bar
//						if (oneReplacedByMultiple) {
//							sicEvent = sicEvent.substring(0, sicEvent.indexOf(sp));
//							sicList.add(StringTools.removeTrailingSymbolSeparator(sicEvent));
//							corrList.add(corrEvent);
//							RhythmSymbol rsSic = 
//								Symbol.getRhythmSymbol(sicEvent.substring(0, sicEvent.indexOf(ss)));
//							int durSic; 
//							if (rsSic != null) {
//								durSic = rsSic.getDuration();
//							}
//							else {
//								durSic = -1; // get last specified duration before currEvent
//							}
//							RhythmSymbol rsCorr = 
//								Symbol.getRhythmSymbol(corrEvent.substring(0, corrEvent.indexOf(ss)));
//							int durCorr;
//							if (rsCorr != null) {
//								durCorr = rsCorr.getDuration();
//							}
//							else {
//								durCorr = -1; // get last specified duration before currEvent
//							}
//							int durCorrToTrack = durCorr;
//							// Iterate through next events until durCorr equals durSic
//							for (int l = j+1; l < currBarEvents.size(); l++) {
//								Event nextEventFull = currBarEvents.get(l);
////								String[] nextEventFull = currBarEvents.get(l);
//								String nextEvent = 
//									StringTools.removeTrailingSymbolSeparator(nextEventFull.getEncoding());
////								String nextEvent = 
////									removeTrailingSymbolSeparator(nextEventFull[Encoding.EVENT_IND]);
//								String nextEventOrig = nextEventFull.getFootnote();
////								String nextEventOrig = nextEventFull[Encoding.FOOTNOTE_IND];
//								// If the next element has a footnote
//								if (nextEventOrig != null) {
//									nextEventOrig = StringTools.removeTrailingSymbolSeparator(nextEventOrig);
//									corrList.add(nextEvent);
//									// Determine duration of corrected event, increment durCorr,
//									// and update durrCorrToTrack
//									RhythmSymbol nextEventRS = 
//										Symbol.getRhythmSymbol(nextEvent.substring(0, 
//										nextEvent.indexOf(ss)));
//									int durCorrNext;
//									if (nextEventRS != null) {
//										durCorrNext = nextEventRS.getDuration();
//										durCorrToTrack = durCorrNext;
//									}
//									else {
//										durCorrNext = durCorrToTrack;
//									}
//									durCorr += durCorrNext;		
//								}
//								if (durCorr == durSic) {
//									int eventsToSkip = corrList.size() - 1;
//									j += eventsToSkip;
//									break;
//								}
//							}
//						}
//
//						List<String> currEventAsXML = new ArrayList<>();
//						if (isCorrected) {
//							currEventAsXML.add(TAB.repeat(2) + "<choice>");
//							currEventAsXML.add(TAB.repeat(3) + "<sic>");
//						}
//						currEventAsXML.addAll(getTabGrps(sicList, prevDurXML, isCorrected, false, tss));
//						if (isCorrected) {
//							currEventAsXML.add(TAB.repeat(3) + "</sic>");
//						}
//						if (isCorrected) {
//							currEventAsXML.add(TAB.repeat(3) + "<corr>");
//							currEventAsXML.addAll(getTabGrps(corrList, prevDurXML, isCorrected, false, tss));
//							currEventAsXML.add(TAB.repeat(3) + "</corr>");
//						}
//						if (isCorrected) {
//							currEventAsXML.add(TAB.repeat(2) + "</choice>");
//						}
//						currBarAsXML.addAll(currEventAsXML);
//
////						// Add duration
////						if (eventAsXML.contains("dur='")) {
////							// Remove any <sic> part 
////							if (eventAsXML.contains("<sic>")) {
////								int firstInd = eventAsXML.indexOf("<sic>");
////								int secondInd = eventAsXML.indexOf("</sic>" + "\r\n");
////								eventAsXML = null;
////							}
////							int firstInd = eventAsXML.indexOf("dur='") + "dur='".length();
////							int secondInd = eventAsXML.indexOf("'", firstInd); 
////							// As whole note
////							double dur = 1.0 / Integer.parseInt(eventAsXML.substring(firstInd, secondInd));
////							double durFinal = dur;
////							if (eventAsXML.contains("dots='")) {
////								firstInd = eventAsXML.indexOf("dots='") + "dots='".length();
////								secondInd = eventAsXML.indexOf("'", firstInd);
////								int dots = Integer.parseInt(eventAsXML.substring(firstInd, secondInd));
////								for (int k = 0; k < dots; k++) {
////									durFinal += durFinal * 0.5;
////								}
////							}
////							System.out.println("-->" + durFinal + "<--");
////							if (eventAsXML.contains("<corr>")) {
////		//						get corr part and then all durs
////		//						System.exit(0);
////							}
////							// As eighth note
//////							durFinal = durFinal / 0.125;
////							barLenInEighths += (int) (durFinal / 0.125);
//////							System.out.println(durFinal);
//////							System.exit(0);
////						}
//
//						// Update prevDurXML
//						// a. Set prevDurXML to currDurXML (or, if it is null, to the last 
//						// XML duration)
//						if (defaultCase || defaultCorrectedCase || multipleReplacedByOne) {
//							if (currDurXML != null) {
//								prevDurXML = currDurXML;
//							}
//						}
//						// b. Set prevDurXML to the last specified duration in corrList  
//						if (oneReplacedByMultiple) {
//							List<String> corrListRev = new ArrayList<>(corrList);
//							Collections.reverse(corrListRev);
//							for (String event : corrListRev) {
//								Integer[] lastDurXML = getXMLDur(event);
//								if (lastDurXML != null) {
//									prevDurXML = lastDurXML;
//									break;
//								}
//							}
//						}
//					} // if (!isBarline)
//				} // loop over currBarEvents
//
//				if (alignWithMetricBarring) {
////					if (i < eventsPerBar.size() - 1) {
//					Integer[] tbtmb = tabBarsToMetricBars.get(i);
//					// Tab and metric bar overlap fully (tab bar:metric bar = 1:1 case)
//					System.out.println("tbtmb");
//					System.out.println(Arrays.asList(tbtmb));
//					boolean equalToMetric = 
//						tbtmb[Tablature.LAST_METRIC_BAR_IND] == -1 &&	
//						tbtmb[Tablature.TAB_BAR_REL_ONSET_IND] == 0 && 
//						tbtmb[Tablature.METRIC_BAR_REMAINDER_IND] == 0;
//					// Tab bar overlaps start of metric bar (tab bar:metric bar n:1 and 3:2 case)
//					boolean overlapsStartMetric = 
//						tbtmb[Tablature.LAST_METRIC_BAR_IND] == -1 &&	
//						tbtmb[Tablature.TAB_BAR_REL_ONSET_IND] == 0 && 
//						tbtmb[Tablature.METRIC_BAR_REMAINDER_IND] > 0;
//					// Tab bar overlaps middle of metric bar (tab bar:metric bar n:1 case)
//					boolean overlapsMiddleMetric = 
//						tbtmb[Tablature.LAST_METRIC_BAR_IND] == -1 &&
//						tbtmb[Tablature.TAB_BAR_REL_ONSET_IND] > 0 && 
//						tbtmb[Tablature.METRIC_BAR_REMAINDER_IND] > 0;	
//					// Tab bar overlaps end of current and start of next metric bar (tab bar:metric bar 3:2 case)
//					boolean overlapsEndAndStartMetric = 
//						tbtmb[Tablature.LAST_METRIC_BAR_IND] != -1;
//					// Tab bar overlaps end of metric bar (tab bar:metric bar n:1 and 3:2 case)
//					boolean overlapsEndMetric = 
//						tbtmb[Tablature.LAST_METRIC_BAR_IND] == -1 &&
//						tbtmb[Tablature.TAB_BAR_REL_ONSET_IND] > 0 && 
//						tbtmb[Tablature.METRIC_BAR_REMAINDER_IND] == 0;
//						
//					// Tab bar and last metric bar end align
//					boolean tabAndMetricBarEnd = tbtmb[Tablature.BAR_OFFSETS_ALIGN_IND] == 1;
//					// Tab bar spans multiple metric bars; last metric bar end and tab bar end do not align
//					boolean tabBarEnd = 
//						(tbtmb[Tablature.METRIC_BAR_IND] != tbtmb[Tablature.LAST_METRIC_BAR_IND]) && 
//						tbtmb[Tablature.BAR_OFFSETS_ALIGN_IND] == 0;
//					// 
//
//					// Place metric barline and tab barline
//					if (equalToMetric || overlapsEndMetric) {
//						System.out.println("equalToMetric || overlapsEndMetric");
//						currBarAsXML.add(TAB + "</layer>");
//						currBarAsXML.add("</staff>");
//						tabBars.add(currBarAsXML);
//						startNewBar = true;
//					}
//					// Place only tab barline 					
//					else if (overlapsStartMetric || overlapsMiddleMetric) {
//						System.out.println("overlapsStartMetric || overlapsMiddleMetric");
//						currBarAsXML.add(TAB.repeat(2) + "<barLine/>");
//						startNewBar = false;
//					}
////					else if (overlapsMiddleMetric) {
//////					System.out.println("i = " + i + ", overlapsMiddleMetric");
////						currBarAsXML.add(TAB.repeat(2) + "<barLine/>");
////						startNewBar = false;
////					}
//					// Place only metric barline
//					else if (overlapsEndAndStartMetric) {
//						System.out.println("overlapsEndAndStartMetric");
//						// Get the duration of the end part of the tab bar (belonging to the next metric bar) 
//						int toNextMetricBarDur = tabBarsToMetricBars.get(i+1)[Tablature.TAB_BAR_REL_ONSET_IND];
////						System.out.println(toNextMetricBarDur);
//						// Get the durations of the tabGrps that fall into the end part
//						List<Integer> tabGrpDurs = new ArrayList<>();
//						for (int k = currBarDurs.size() - 1; k > 0; k--) {
//							tabGrpDurs.add(currBarDurs.get(k));
//							if (ToolBox.sumListInteger(tabGrpDurs) == toNextMetricBarDur) {
//								break;
//							}
//						}
////						System.out.println("numTabGroupsToNextMetricBar:");
////						System.out.println(tabGrpDurs.size());
//						// Move the tabGrps that fall into the end part from currBarAsXML to nextCurrBarAsXML
//						List<String> nextCurrBarAsXML = new ArrayList<>();
//						for (int k = currBarAsXML.size() - 1; k > 0; k--) {
//							nextCurrBarAsXML.add(0, currBarAsXML.get(k));
//							if (StringUtils.countMatches(String.join(",", nextCurrBarAsXML), 
//								"<tabGrp") == tabGrpDurs.size()) {
//								break;
//							}
//						}
//						currBarAsXML = currBarAsXML.subList(0, currBarAsXML.size() - nextCurrBarAsXML.size());
//						// Complete currBarAsXML with staff and layer elements and add
//						currBarAsXML.add(TAB + "</layer>");
//						currBarAsXML.add("</staff>");
//						tabBars.add(currBarAsXML);
//							
////						for (String s : currBarAsXML) {
////							System.out.println(s);
////						}
////						System.out.println("-------");
//							
//						// Complete nextCurrBarAsXML with staff and layer elements and set currBarAsXML
//						nextCurrBarAsXML.add(0, "<staff n='" + staff + "'>");
//						nextCurrBarAsXML.add(1, TAB + "<layer n='1'>");
//						nextCurrBarAsXML.add(TAB.repeat(2) + "<barLine/>");
//						currBarAsXML = nextCurrBarAsXML;
//						startNewBar = false;
//							
////						for (String s : currBarAsXML) {
////							System.out.println(s);
////						}
//							
////						for (int k = currBarAsXML.size() - 1; k > 0; k--) {
////							String s = currBarAsXML.get(k);
////							toNextMetricBar.add(0, s);
////							if (s.contains("<tabGrp")) {
////								int start = s.indexOf("dur='") + "dur='".length();
////								int XMLDurInt = Integer.parseInt(s.substring(start, s.indexOf("'", start)));
////								int dotsInt = 0;
////								if (s.contains("dots='")) {
////									start = s.indexOf("dots='") + "dots='".length();
////									dotsInt = Integer.parseInt(s.substring(start, s.indexOf("'", start)));
////								}
////								cumulativeDur += getDur(XMLDurInt, dotsInt);
////								if (cumulativeDur == toNextMetricBarDur) {
////									break;
////								}
////							}
////						}
//					}
//					
////					// Place metric barline and tab barline
////					else if (overlapsEndMetric) {
//////						System.out.println("i = " + i + ", overlapsEndMetric");
////						currBarAsXML.add(TAB + "</layer>");
////						currBarAsXML.add("</staff>");
////						tabBars.add(currBarAsXML);
////						startNewBar = true;
////					}
//
////					boolean combineTabBars = 			
//////					tabBarsToMetricBars.get(i + 1)[Tablature.METRIC_BAR_IND] == currMetricBar;
////					tabBarsToMetricBars.get(i)[Tablature.METRIC_BAR_REMAINDER_IND] != 0;
//
////					}
//					
////					else {
////						currBarAsXML.add(TAB + "</layer>");
////						currBarAsXML.add("</staff>");
////						tabBars.add(currBarAsXML);
////						startNewBar = true;
////					}
//					
//					boolean old = false;
//					if (old) {
//						boolean combineTabBars = 
//							i < eventsPerBar.size() - 1 && 
////							tabBarsToMetricBars.get(i + 1)[Tablature.METRIC_BAR_IND] == currMetricBar;
//							tabBarsToMetricBars.get(i)[Tablature.METRIC_BAR_REMAINDER_IND] != 0;
//						// tabBars follows tablature 'barring'; combine any tab bars to follow metric barring
//						// In case of (i) last tab bar or (ii) where there is a next tab bar that does not 
//						// have currMetricBar
//						if (i == eventsPerBar.size() - 1 || !combineTabBars) {
//							currBarAsXML.add(TAB + "</layer>");
//							currBarAsXML.add("</staff>");
//							tabBars.add(currBarAsXML);
//							startNewBar = true;
//						}
//						// In case where there is a next tab bar that has currMetricBar
//						else if (combineTabBars) {				
//							currBarAsXML.add(TAB.repeat(2) + "<barLine/>");
//							startNewBar = false;
//						}
//					}
//				}
//				else {
//					currBarAsXML.add(TAB + "</layer>");
//					currBarAsXML.add("</staff>");
//					tabBars.add(currBarAsXML);
//					startNewBar = true;
//				}
//
////				// Append meter change (if applicable)
////				boolean doThis = false;
////				if (doThis) {
////					if (i < ebf.size()-1) {
////						// Check for meter change in first event of next bar
////						List<String[]> nextBar = ebf.get(i+1);
////						String[] firstEventNextFull = nextBar.get(0);
////						String firstEventNext = firstEventNextFull[Encoding.EVENT_IND];
////						// Remove final ss
////						firstEventNext = firstEventNext.substring(0, firstEventNext.lastIndexOf(ss));
////						String[] firstEventNextSplit = 
////							(!firstEventNext.contains(ss)) ? new String[]{firstEventNext} : 
////							firstEventNext.split("\\" + ss);
////						// Meter change found? Add scoreDef after bar
////						if (MensurationSign.getMensurationSign(firstEventNextSplit[0]) != null) {
////							String meterStr = "";
////							for (String s : meters.get(meterIndex+1)) {
////								if (!s.equals("")) {
////									meterStr += s + " ";
////								}
////							}
////							meterStr = meterStr.trim();
////							String scoreDef = INDENT_TWO + "<scoreDef" + " " + meterStr + "/>" + "\r\n";
//////							sb.append(scoreDef);
//////							measuresTab.addAll(scoreDef);
////							meterIndex++;
////						}
////					}
////				}
//
//			} // loop over eventsPerBar
//
////			// tabBars follows tablature 'barring'; combine any tab bars to follow metric barring
////			if (alignWithMetricBarring) {
////				List<List<String>> tabBarsCombined = new ArrayList<>();
////				int tabBarInd = 0;
////				int numMetricBars = mi.get(mi.size()-1)[Transcription.MI_LAST_BAR];
////				for (int metricBarInd = 0; metricBarInd < numMetricBars; metricBarInd++) {
////					List<String> currTabBar = new ArrayList<>(tabBars.get(tabBarInd));
////					int currMetricBar = metricBarInd + 1;
//////					String currTabBarAsStr = "";
//////					int metricBarInd = j;
//////					currMeasureTabAsStr += INDENT_TWO + "<measure n='" + currMeasure + "'" + 
//////						((j == numBars-1) ? " right='end'" : "") + ">" + "\r\n";
//////					// The elements in tabBars start at <tabGrp> level
//////					currTabBarAsStr += INDENT_TWO + TAB + "<staff n='1'>" + "\r\n";
//////					currTabBarAsStr += INDENT_TWO + TAB.repeat(2) + "<layer n='1'>" + "\r\n";
//////					for (String line : tabBars.get(tabBarInd)) {
//////						currTabBarAsStr += INDENT_TWO + TAB.repeat(3) + line + "\r\n";
//////					}
////				
////					// Add any next tab bars with the same metric bar
////					int startInd = tabBarInd + 1;
//////					if (startInd != tabBarsToMetricBars.size()) {
////					for (int j = startInd; j < tabBarsToMetricBars.size(); j++) {
////						if (tabBarsToMetricBars.get(j)[1] == currMetricBar) {
////							currTabBar.add(TAB.repeat(2) + "<barline/>");
//////							currTabBarAsStr += INDENT_TWO + TAB.repeat(3) + "<barline/>" + "\r\n";
////							for (String line : tabBars.get(j)) {
////								currTabBar.add(line);
//////								currTabBarAsStr += INDENT_TWO + TAB.repeat(3) + line + "\r\n";
////							}
////							// Additional increment of tabBarInd
////							tabBarInd++;
////						}
////						else {
////							break;
////						}
////					}
////					tabBarsCombined.add(currTabBar);
//////					}
////					// Normal increment of tabBarInd (together with metricBarInd)
////					tabBarInd++;
//////					currTabBarAsStr += INDENT_TWO + TAB.repeat(2) + "</layer>" + "\r\n";
//////					currTabBarAsStr += INDENT_TWO + TAB + "</staff>";
//////					tabBarsAsStr.add(currTabBarAsStr);
////				}
////				tabBars = tabBarsCombined;
////			}
//
//			return tabBars;
//		}	


	/**
	 * Gets the note information for the note at index i. As input is given a String[] containing
	 * only the attributes pname, oct, and accid (or only <code>null</code> in case of a rest). 
	 * Returns
	 * as element 0: a List<String[]> containing, for each unit fraction the note can be divided 
	 *               into, the original String[] completed with the dur and, if applicable, tie
	 *               and dots attributes (or only dur and, if applicable, dots in case of a rest)
	 * as element 1: a List<Integer[]> containing, for each unit fraction the note can be divided 
	 *               into, an Integer[] containing the note's index, bar, onset (numerator), onset 
	 *               (denominator), metric position (numerator), metric position (denominator),
	 *               duration, dots, tripletOpen, tripletMid, and tripletClose attributes 
	 *               (or only bar, dur, dots, tripletOpen, -Mid, and -Close in case of a rest)
	 *               
	 * In case of a simple (non-dotted, non-compound) or dotted note, the lists returned have
	 * only one element; in case of a non-dotted compound note (e.g., a half tied to an eighth)
	 * the lists have more than one element.
	 *               
	 * @param i
	 * @param iTab
	 * @param argDur Is tripletised (has nominal value).
	 * @param gridVal
	 * @param onset Is untripletised (has actual value).
	 * @param metPos Is untripletised (has actual value).
	 * @param mi
	 * @param tripletInfo
	 * @param tripletOnsetPairs
	 * @return
	 */
//	private static List<Object> getNoteDataOLD(int i, int iTab, String[] curr, Rational argDur, 
//		Rational gridVal, Rational onset, Rational metPos, List<Integer[]> mi, 
//		List<Boolean> tripletInfo, List<Rational[]> tripletOnsetPairs) {
//
//		int diminution = 0;
//		List<String[]> currPitchOctAccTie = new ArrayList<>();
//		List<Integer[]> currIndBarOnsMpDurDots = new ArrayList<>();
//
//		// Determine the unit fractions (which, if the note or rest has a triplet onset
//		// time, are tripletised) 
//		// NB: argDur is tripletised, meaning that uf and durAsRat below are as well
//		if (tripletInfo != null && tripletInfo.contains(Boolean.TRUE)) {
//			argDur = argDur.mul(TRIPLETISER);
//		}
//		List<Rational> uf = TimeMeterTools.getUnitFractions(argDur, gridVal);
//
//		int numDots = TimeMeterTools.getNumDots(uf);
//		boolean isRest = curr[STRINGS.indexOf("pname")] == null;
//		boolean isSimple = (uf.size() == 1 && numDots == 0);
//		boolean isDotted = numDots > 0;
//		boolean isNonDottedCompound = (uf.size() > 1 && numDots == 0);
//
//		Rational currOnset = onset;
//		Rational currMetPos = metPos;
//		// Iterate through the unit fractions. In the case of a simple or dotted note, the 
//		// for loop breaks at the end of k = 0  
//		for (int k = 0; k < uf.size(); k++) {
//			System.out.println("k = " + k);
//			int currBar = TimeMeterTools.getMetricPosition(currOnset, mi)[0].getNumer();
//			String[] copyOfCurr = Arrays.copyOf(curr, curr.length);
//			Rational durAsRat = uf.get(k);
//			if (isDotted) {
//				durAsRat = durAsRat.add(uf.get(1));
//			}
//			// Allow for breve (2/1) and long (4/1)
//			int dur = -1;
//			if (isSimple || isDotted) {
//				if (uf.get(k).isLessOrEqual(Rational.ONE)) {
//					dur = uf.get(k).getDenom();
//				}
//				else if (uf.get(k).equals(new Rational(2, 1))) {
//					dur = BREVE;
//				}
//				else if (uf.get(k).equals(new Rational(4, 1))) {
//					dur = LONG;
//				}
//			}
//			else {
//				if (durAsRat.isLessOrEqual(Rational.ONE)) {
//					dur = durAsRat.getDenom();
//				}
//				else if (durAsRat.equals(new Rational(2, 1))) {
//					dur = BREVE;
//				}
//				else if (durAsRat.equals(new Rational(4, 1))) {
//					dur = LONG;
//				}
//			}
//			String durStr = 
//				(dur > 0) ? Integer.toString(dur) : ((dur == BREVE ? "breve" : "long"));
//			copyOfCurr[STRINGS.indexOf("dur")] = "dur='" + durStr + "'";
//			if (isSimple || isDotted) {
//				if (numDots != 0) {
//					copyOfCurr[STRINGS.indexOf("dots")] = "dots='" + numDots + "'";
//				}
//			}
//			if (isNonDottedCompound) { // in nonDottedCompound case, numDots is always 0 
//				if (!isRest) {
//					String tie = (k == 0) ? "i" : ((k > 0 && k < uf.size()-1) ? "m" : "t");
//					copyOfCurr[STRINGS.indexOf("tie")] = "tie='" + tie + "'";
//				}
//			}
//			currPitchOctAccTie.add(copyOfCurr);
//
//			Integer[] in = new Integer[INTS.size()];
//			Arrays.fill(in, -1);
//			Integer[] secondIn = null;
//			in[INTS.indexOf("bar")] = currBar;
//			in[INTS.indexOf("metPosNum")] = currMetPos.getNumer();
//			in[INTS.indexOf("metPosDen")] = currMetPos.getDenom();
//			in[INTS.indexOf("dur")] = dur;
//			in[INTS.indexOf("dots")] = numDots;
//			if (!isRest) {
//				in[INTS.indexOf("ind")] = i;
//				in[INTS.indexOf("indTab")] = iTab;				
//			}
//			in[INTS.indexOf("onsetNum")] = currOnset.getNumer();
//			in[INTS.indexOf("onsetDen")] = currOnset.getDenom();
//			
//			// Set tripletOpen, tripletMid, and tripletClose
//			in[INTS.indexOf("tripletOpen")] = 0;
//			in[INTS.indexOf("tripletMid")] = 0;
//			in[INTS.indexOf("tripletClose")] = 0;
//			
//			List<Boolean> openMidClose = (tripletOnsetPairs == null) ? null : 
//				isTripletOnset(tripletOnsetPairs, currOnset);
//			if (tripletOnsetPairs != null) {
//				// Get any triplet information
//				String[] last = currPitchOctAccTie.get(currPitchOctAccTie.size()-1);
//				Rational[] top = 
//					getExtendedTripletOnsetPair(currOnset, tripletOnsetPairs, mi, diminution);
//				Rational currTripletLen = null;
//				Rational currTripletOpenOnset = null;
//				Rational metPosTripletOpen = null;
//				Rational tripletBorder = null;
//				Rational onsetTripletBorder = null;
//				Rational offset = null;
//				if (top != null) {
//					currTripletOpenOnset = 
//						getExtendedTripletOnsetPair(currOnset, tripletOnsetPairs, mi, diminution)[0];
//					metPosTripletOpen = TimeMeterTools.getMetricPosition(currTripletOpenOnset, mi)[1];
//					currTripletLen = top[3];
//					tripletBorder = metPosTripletOpen.add(currTripletLen);
//					onsetTripletBorder = currTripletOpenOnset.add(currTripletLen);
//					offset = currMetPos.add(durAsRat.mul(DETRIPLETISER));
//				}
//				// If currOnset is the second part of a non-dotted compound note or rest that 
//				// falls after tripletCloseOnset: set tripletCloseOnset to true 
//				// NB: This does not apply to isSimple or isDotted, where there is no second part
//				// E.g.: last H in voice 0 = tripletCloseOnset; last Q in voice 1, which
//				// is tied to the W before, falls after it 
//				// voice 0: W       H   H   H   H     |
//				// voice 1: W       H.    W       (Q) |
//				// (This happens in 5263_12_in_exitu_israel_de_egipto_desprez-3, bar 77, voice 2)
//				if (isNonDottedCompound) {
//					Rational[] topOnset = 
//						getExtendedTripletOnsetPair(onset, tripletOnsetPairs, mi, diminution);
//					Rational tripletOpenOnset = (topOnset != null) ? topOnset[0] : null;			
//					Rational tripletCloseOnset = (topOnset != null) ? topOnset[1] : null;
//					if (topOnset != null && currOnset.isGreater(tripletCloseOnset) && 
//						currOnset.isLess(tripletOpenOnset.add(currTripletLen))) {
//						System.out.println("within triplet but after tripletCloseOnset --> tripletClose");
//						openMidClose.set(2, true);
//					}
//				}
//
//				// If the note is tripletOpen
//				if (openMidClose.get(0) == true) {
//					System.out.println("is tripletOpen");
//					in[INTS.indexOf("tripletOpen")] = 1;
////nu				// If the note or rest equals tripletLen: remove dot in last and in
////					if (ToolBox.sumListRational(uf).mul(DETRIPLETISER).isEqual(currTripletLen)) {
////						last[STRINGS.indexOf("dots")] = null;
////						in[INTS.indexOf("dots")] = -1;
////					}
////					// If the note or rest does not equal tripletLen: set tripletOpen
////					else {
////						in[INTS.indexOf("tripletOpen")] = 1;
////					}
////nu				// Determine tripletOffsets
////					Rational currBarLen = Rational.ZERO;
////					for (Integer[] m : mi) {
////						if (currBar >= m[2] && currBar <= m[3]) {
////							currBarLen = new Rational(m[0], m[1]);
////							break;
////						}
////					}
////					List<Rational> tripletOffsets = new ArrayList<>();
////					int times = (int) currBarLen.div(currTripletLen).toDouble();
////					for (int j = 1; j <= times; j++) {
////						tripletOffsets.add(currTripletLen.mul(j));
////					}
////nu				// Check offset of remainder to check if it is in tripletOffsets.
////					// Example from 5256_05_inviolata_integra_desprez-2, bar 19, voice 0 (n = note; r = rest)
////					// --3-- --3--   
////					// H W   W   H | H
////					// n r   r   r   n
////					// uf for the rest will be [1/1, 1/1, 1/2]; but at k = 1, the remainder of
////					// uf does not need to be a triplet as it equals tripletLen, but can be a W
////					Rational offsetRemainder = null;
////					if (k != uf.size() - 1) {
////						Rational remainder = ToolBox.sumListRational(uf.subList(k+1, uf.size()));
////						offsetRemainder = offset.add(remainder.mul(DETRIPLETISER));
////					}
//					
//					// a. If note ends on triplet border: set to tripletClose
//					if (offset.equals(tripletBorder)) {
//						System.out.println("ends on tripletBorder --> tripletClose");
//						in[INTS.indexOf("tripletClose")] = 1;
//					}
//					// b. If note ends after triplet border: split at border and set the first
//					// part to tripletClose and the second to tripletOpen
//					if (offset.isGreater(tripletBorder)) {
////nu					// a. If note ends on (one of the) triplet border(s): no triplet necessary
////						if (tripletOffsets.contains(offset) || tripletOffsets.contains(offsetRemainder)) {
////							in[INTS.indexOf("tripletOpen")] = 0;
////							if (isDotted || isSimple) {
////								// Reset dot (which tripletises the note) in last element of 
////								// currPitchOctAccTie and in
////								last[STRINGS.indexOf("dots")] = null;
////								in[INTS.indexOf("dots")] = -1;
////							}
////							if (isNonDottedCompound) {
////								// Reset dur and tie in last element of currPitchOctAccTie and in
////								// NB: there will be no dots, as the elements of uf are all non-dotted
////								last[STRINGS.indexOf("dur")] = "dur='" + currTripletLen.getDenom() + "'";
////								if (!isRest) {
////									// Adapt if there is a tie that is not 't'
////									if (last[STRINGS.indexOf("tie")].equals("tie='i'")) {
////										last[STRINGS.indexOf("tie")] = null;
////									}
////									if (last[STRINGS.indexOf("tie")].equals("tie='m'")) {
////										last[STRINGS.indexOf("tie")] = "tie='t'";
////									}
////								}
////								in[INTS.indexOf("dur")] = currTripletLen.getDenom();
////							}
////							// Add in to currIndBarOnsMpDurDots and break from for loop
////							currIndBarOnsMpDurDots.add(in);
////							break;
////						}
//
//						System.out.println("across triplet border --> split");
//						Rational firstPart = tripletBorder.sub(currMetPos);
//						Rational remainder = offset.sub(tripletBorder);
//						List<Object> res = 
//							handleNoteAcrossTripletBorder(firstPart, remainder, currTripletLen, 
//							tripletBorder, onsetTripletBorder, last, curr, in, gridVal, isRest);
//						List<String[]> lastTwoStr = (List<String[]>) res.get(0);
//						List<Integer[]> lastTwoInt = (List<Integer[]>) res.get(1);
//						currPitchOctAccTie.set(currPitchOctAccTie.size()-1, lastTwoStr.get(0));
//						currPitchOctAccTie.add(lastTwoStr.get(1));
//						in = lastTwoInt.get(0);
//						secondIn = lastTwoInt.get(1);
//					}					
//				}
//				// If the note is tripletMid
//				if (openMidClose.get(1) == true) {
//					System.out.println("is tripletMid");
//					in[INTS.indexOf("tripletMid")] = 1;
//					
//					// a. If note ends on triplet border: set to tripletClose
//					if (offset.equals(tripletBorder)) {
//						System.out.println("ends on tripletBorder --> tripletClose");
//						in[INTS.indexOf("tripletClose")] = 1;
//					}
//					// b. If note ends after triplet border: split at border and set the first
//					// part to tripletClose and the second to tripletOpen
//					if (offset.isGreater(tripletBorder)) {
////					if (offset.isGreaterOrEqual(tripletBorder)) {					
////nu					// a. If note ends on triplet border: set to tripletClose
////						if (offset.equals(tripletBorder)) {
////							System.out.println("ends on tripletBorder --> tripletClose");
////							in[INTS.indexOf("tripletClose")] = 1;
////						}
//
////nu						// b. If note ends after triplet border: split at border and set the first
////						// part to tripletClose and the second to tripletOpen
////						else {
//						System.out.println("across triplet border --> split");
//						Rational firstPart = tripletBorder.sub(currMetPos);
//						Rational remainder = offset.sub(tripletBorder);
//						List<Object> res = 
//							handleNoteAcrossTripletBorder(firstPart, remainder, currTripletLen,
//							tripletBorder, onsetTripletBorder, last, curr, in, gridVal, isRest);
//						List<String[]> lastTwoStr = (List<String[]>) res.get(0);
//						List<Integer[]> lastTwoInt = (List<Integer[]>) res.get(1);
//						currPitchOctAccTie.set(currPitchOctAccTie.size()-1, lastTwoStr.get(0));
//						currPitchOctAccTie.add(lastTwoStr.get(1));
//						in = lastTwoInt.get(0);
//						secondIn = lastTwoInt.get(1);
////						}
//					}
////nu				else {
////						in[INTS.indexOf("tripletMid")] = 1;
////					}
//				}
//				// If the note is tripletClose
//				if (openMidClose.get(2) == true) {
//					System.out.println("is tripletClose");
//					in[INTS.indexOf("tripletClose")] = 1;
//					// a. If note ends on triplet border: no action required
//					// b. If note ends after triplet border: split at border and set the first
//					// part to tripletClose and the second to tripletOpen
//					if(offset.isGreater(tripletBorder)) {
//						System.out.println("across triplet border --> split");
//						Rational firstPart = tripletBorder.sub(currMetPos);
//						Rational remainder = offset.sub(tripletBorder);
//						List<Object> res = 
//							handleNoteAcrossTripletBorder(firstPart, remainder, currTripletLen,
//							tripletBorder, onsetTripletBorder, last, curr, in, gridVal, isRest);
//						List<String[]> lastTwoStr = (List<String[]>) res.get(0);
//						List<Integer[]> lastTwoInt = (List<Integer[]>) res.get(1);
//						currPitchOctAccTie.set(currPitchOctAccTie.size()-1, lastTwoStr.get(0));
//						currPitchOctAccTie.add(lastTwoStr.get(1));
//						in = lastTwoInt.get(0);
//						secondIn = lastTwoInt.get(1);
//					}
//				}
//			}
//			currIndBarOnsMpDurDots.add(in);
//			if (secondIn != null) {
//				currIndBarOnsMpDurDots.add(secondIn);
//			}
//			if (isSimple || isDotted) {
//				System.out.println("break: isSimple || isDotted");
//				break;
//			}
//			else {
//				if (openMidClose != null && openMidClose.contains(true)) {
//					durAsRat = durAsRat.mul(DETRIPLETISER);
//				}
//				currOnset = currOnset.add(durAsRat);
//				currMetPos = currMetPos.add(durAsRat);
//			}
//		}
//		// If the last element in currIndBarOnsMpDurDots is a tripletClose: check if the elements
//		// up to tripletOpen are a single note occupying the whole tripletLen, all rests, or all
//		// tied notes. If so, replace the triplet value by its non-triplet value with tripletLen
//		int indLast = currIndBarOnsMpDurDots.size()-1;
//		Integer[] last = currIndBarOnsMpDurDots.get(indLast);
//		if (last[INTS.indexOf("tripletClose")] == 1) { // && currIndBarOnsMpDurDots.size() > 1) {
//			List<Boolean> rests = new ArrayList<>();
//			List<Boolean> ties = new ArrayList<>();
//			// Find accompanying tripletOpen
//			int indOpen = -1;
//			for (int j = indLast; j >= 0; j--) { // was indLast-1
//				boolean isTripletOpen = 
//					currIndBarOnsMpDurDots.get(j)[INTS.indexOf("tripletOpen")] == 1;
//				// If the element at index j is a rest: add
//				if (currPitchOctAccTie.get(j)[STRINGS.indexOf("pname")] == null) {
//					rests.add(true);
//				}
//				else {
//					rests.add(false);
//				}
//				// If the element at index j is a tie: add
//				String tieStr = currPitchOctAccTie.get(j)[STRINGS.indexOf("tie")]; 
//				if (tieStr != null) {
//					// In order for the triplet to be fully tied, the last element cannot be i;
//					// the middle element cannot be i or t; and the first element cannot be t 
//					if (j == indLast && (tieStr.equals("tie='m'") || tieStr.equals("tie='t'")) ||
//						(j < indLast && !isTripletOpen) && tieStr.equals("tie='m'") ||
//						isTripletOpen && (tieStr.equals("tie='i'") || tieStr.equals("tie='m'"))) {
//						ties.add(true);
//					}
//					else {
//						ties.add(false);
//					}
//				}
//				// Break when tripleOpen is reached
//				if (isTripletOpen) {
//					indOpen = j;
//					break;
//				}
//			}
//			// If indOpen is reached and the triplet consists of a single note occupying the 
//			// whole tripletLen, only of rests, or only of tied notes
//			if (indOpen != -1 && !rests.contains(false) || indOpen != -1 && !ties.contains(false)) {
//				System.out.println("triplet rest/note replaced by untripletised rest/note");
//				boolean isTripletLen = indOpen == indLast; 
//				Rational tripletisedDur = Rational.ZERO;
//				for (int j = indOpen; j <= indLast; j++) {
//					int durAsInt = currIndBarOnsMpDurDots.get(j)[INTS.indexOf("dur")];
//					Rational toAdd = 
//						(durAsInt > 0) ? new Rational(1, durAsInt) : // whole or shorter
//						new Rational(durAsInt*-2, 1); // breve or long	
//					tripletisedDur = tripletisedDur.add(toAdd);
//				}
//				// If the rest/note is not of tripletLen, it must be detripletised
//				if (!isTripletLen) {
//					tripletisedDur = tripletisedDur.mul(DETRIPLETISER);
//				}
//				// Change items at indOpen
//				currIndBarOnsMpDurDots.get(indOpen)[INTS.indexOf("dur")] = tripletisedDur.getDenom();
//				currIndBarOnsMpDurDots.get(indOpen)[INTS.indexOf("dots")] = -1;
//				currIndBarOnsMpDurDots.get(indOpen)[INTS.indexOf("tripletOpen")] = 0;
//				// If the rest/note is of tripletLen, tripletClose, which will be set to 1,
//				// must be reset too
//				if (isTripletLen) {
//					currIndBarOnsMpDurDots.get(indOpen)[INTS.indexOf("tripletClose")] = 0;
//				}
//				//
//				currPitchOctAccTie.get(indOpen)[STRINGS.indexOf("dur")] = 
//					"dur='" + tripletisedDur.getDenom() + "'";
//				currPitchOctAccTie.get(indOpen)[STRINGS.indexOf("dots")] = null;
//				if (ties.size() > 0 && !ties.contains(false)) {
//					// Tie for the first element (which can only be i or m) must be retained 
//					// only if tie for the last element (which can only be m or t) is m
//					if (currPitchOctAccTie.get(indLast)[STRINGS.indexOf("tie")].equals("tie='t'")) {
//						currPitchOctAccTie.get(indOpen)[STRINGS.indexOf("tie")] = null;
//					}
//				}
//				// Remove items at indices after indOpen
//				currIndBarOnsMpDurDots = currIndBarOnsMpDurDots.subList(0, indOpen+1);
//				currPitchOctAccTie = currPitchOctAccTie.subList(0, indOpen+1);
//			}
//		}
//		return Arrays.asList(new Object[]{currPitchOctAccTie, currIndBarOnsMpDurDots});
//	}


//	/**
//	 * Returns the duration of the given Rational. Returns an Integer[] with as element 0 the 
//	 * duration as the smallest denominator, and as element the number of dots.  
//	 *  
//	 * @param dur
//	 * @return
//	 */
//	private Integer[] getDuration(Rational durRat) {
//		Integer[] dur = null;
//		durRat.reduce();
//		List<Rational> all = new ArrayList<Rational>();
//		for (int i = 1; i <= 128; i++) {
//			all.add(new Rational(i, 128));
//		}
//		
//		// Quantize
//		if (!all.contains(durRat)) {
//			dur = null;
//		}
//		else {	
//			// Quadruple dotted = 1.9375
//			Rational basicNoteValQdrD = durRat.div(new Rational(31, 16));
//			basicNoteValQdrD.reduce();
//			if (all.contains(basicNoteValQdrD)) {
//				dur = new Integer[]{basicNoteValQdrD.getDenom(), 4};
//			}
//			// Triple dotted = 1.875 * basic note value
//			Rational basicNoteValTrpD = durRat.div(new Rational(15, 8));
//			basicNoteValTrpD.reduce();
//			if (all.contains(basicNoteValTrpD)) {
//				dur = new Integer[]{basicNoteValTrpD.getDenom(), 3};
//			}
//			// Double dotted = 1.75 * basic note value
//			Rational basicNoteValDblD = durRat.div(new Rational(7, 4));
//			basicNoteValDblD.reduce();
//			if (all.contains(basicNoteValDblD)) {
//				dur = new Integer[]{basicNoteValDblD.getDenom(), 2};
//			}
//			// Dotted = 1.5 * basic note value 
//			Rational basicNoteValD = durRat.div(new Rational(3, 2));
//			basicNoteValD.reduce();
//			if (all.contains(basicNoteValD)) {
//				dur = new Integer[]{basicNoteValD.getDenom(), 1};
//			}
//			// Undotted
//			Rational basicNoteVal = durRat.div(new Rational(1, 1));
//			basicNoteVal.reduce();
//			if (dur != null && all.contains(basicNoteVal)) {
//				dur = new Integer[]{basicNoteVal.getDenom(), 0};
//			}
//		}
//		return dur;
//	}


	/**
	 * Exports the given tablature as a TabMEI file, saved at the given path.
	 * 
	 * @param tab
	 * @param path
	 */
//	private static void exportTabMEIFile(Tablature tab, String path) {
//
//		String INDENT_TWO = TAB.repeat(6);
//		
//		String res = ToolBox.readTextFile(new File(templatePath + "template-MEI.xml"));
////		String res = ToolBox.readTextFile(new File(MEITemplatePath + "template-MEI.xml"));
//		String tuningStr = "lute.renaissance.6";
//		TabSymbolSet tss = tab.getEncoding().getTabSymbolSet();
//		String notationtypeStr = getNotationTypeStr(tss);
//
//		String ss = Symbol.SYMBOL_SEPARATOR;
//		String sp = Symbol.SPACE.getEncoding();
//
//		// 1. Make meiHead
//		String[] meiHead = new String[MEI_HEAD.size()];
//		meiHead[MEI_HEAD.indexOf("title")] = tab.getName();
//		res = res.replace("title_placeholder", meiHead[MEI_HEAD.indexOf("title")]);
//
//		List<Integer[]> mi = tab.getMeterInfo();
////		List<Integer[]> mi = tab.getTimeline().getMeterInfoOBS();
//		List<String[]> meters = new ArrayList<>();
//		for (Integer[] in : mi) {
//			String sym = "";
//			if (in[Transcription.MI_NUM] == 4 && in[Transcription.MI_DEN] == 4) {
//				sym = " meter.sym='common'";
//			}
//			else if (in[Transcription.MI_NUM] == 2 && in[Transcription.MI_DEN] == 2) {
//				sym = " meter.sym='cut'";
//			}
//			meters.add(new String[]{
//				"meter.count='" + in[Transcription.MI_NUM] + "'", 
//				"meter.unit='" + in[Transcription.MI_DEN] + "'",
//				sym}
//			);
//		}
//
//		// 2. Make music
//		// a. Make scoreDef. The scoreDef contains the initial meter (if any); any additional 
//		// ones are stored in nonInitMeters
//		String[] initMeter = meters.get(0); 
//		String scoreDefStr = initMeter[0] + " " + initMeter[1] + initMeter[2];
//		res = res.replace("scoreDef_placeholder", scoreDefStr.trim());
//		int meterIndex = 0;
//
//		// b. Make staffGrp (goes inside scoreDef)
//		String staffGrpAtt = "";
//		res = res.replace(" staffGrp_placeholder", staffGrpAtt);
//		String staffGrpStr = 
//			"<staffDef n='1'" + " xml:id='s1'" + " lines='6'" + " " + "notationtype='" + 
//			notationtypeStr + "'" + ">" + "\r\n"; 
//		staffGrpStr += INDENT_TWO + TAB + TAB + "<tuning tuning.standard='" + tuningStr + "'" + "/>" + "\r\n";		
//		staffGrpStr += INDENT_TWO + TAB + "</staffDef>";
//
//		res = res.replace("staffGrp_content_placeholder", staffGrpStr);
//		
//		// Get events; remove any decorative opening barlines (which affect the XML bar numbering)
//		List<Event> events = 
//			Encoding.removeDecorativeBarlineEvents(tab.getEncoding().getEvents());
////		List<List<String[]>> ebf = tab.getEncoding().getExtendedEventsPerBar(true);
//
////		List<List<String[]>> ebfPruned = new ArrayList<>();
////		for (int i = 0; i < ebf.size(); i++) {
////			List<String[]> bar = ebf.get(i);
////			// Add bar only if it is not one containing only one element that is a barline
////			String f = removeTrailingSymbolSeparator(bar.get(0)[Encoding.EVENT_IND]);
////			if (!(bar.size() == 1 && ConstantMusicalSymbol.isBarline(f))) {
////				ebfPruned.add(bar);
////			}
////		}
////		ebf = ebfPruned;
//
//		// 3. Make bars
//		// Organise the information per bar
//		StringBuilder sb = new StringBuilder();
//		List<String> measuresTab = new ArrayList<>();
//		Integer[] prevDurXML = new Integer[]{0, 0};
//		for (int i = 0; i < events.size(); i++) {
////			List<String[]> currBar = events.get(i);
//			
//			int currTabBar = events.get(i).getBar();
////			int currTabBar = currBarEvents.get(0).getBar();
//			List<Event> currBar = new ArrayList<>();
//			for (int j = i; j < events.size(); j++) {
//				if (events.get(j).getBar() == currTabBar + 1) {
//					i = j - 1;
//					break;
//				}
//				currBar.add(events.get(j));
//			}
//			System.out.println("bar  : " + (i+1));
//
//			StringBuilder sbBar = new StringBuilder();
//			
//			// Make XML content for currBar
//			String currBarXMLAsString = "";
//			String barline = "";
//			// For each event
//			for (int j = 0; j < currBar.size(); j++) {
//				Event currEventFull = currBar.get(j);
////				String[] currEventFull = currBar.get(j);
//				String currEvent = 
//					StringTools.removeTrailingSymbolSeparator(currEventFull.getEncoding());
////				String currEvent = 
////					removeTrailingSymbolSeparator(currEventFull[Encoding.EVENT_IND]);
//				String currEventOrig = currEventFull.getFootnote();
////				String currEventOrig = currEventFull[Encoding.FOOTNOTE_IND];
//				System.out.println(currEventFull);
//				System.out.println("event: " + currEvent);
//				// Extract correction
//				boolean isCorrected = false;
//				if (currEventOrig != null) {
//					if (currEventOrig.contains("'")) {
//						currEventOrig = currEventOrig.substring(currEventOrig.indexOf("'")+1,
//							currEventOrig.lastIndexOf("'"));
//						isCorrected = true;
//					}
//					else {
//						currEventOrig = 
//							currEventOrig.substring(currEventOrig.indexOf(Encoding.FOOTNOTE_INDICATOR) + 1);
//					}
//					currEventOrig = StringTools.removeTrailingSymbolSeparator(currEventOrig);
//				}
//
//				// Barline? End of bar reached; set barline if not single
//				if (Symbol.getConstantMusicalSymbol(currEvent) != null && Symbol.getConstantMusicalSymbol(currEvent).isBarline()) {
////				if (ConstantMusicalSymbol.isBarline(currEvent)) {
//					if (currEvent.equals(Symbol.BARLINE.makeVariant(2, null).getEncoding())) {
//						barline = " right='dbl'";
//					}
//					if (i == events.size()-1) {
//						barline = " right='end'";
//					}
//				}
//				// Not a barline?
//				else {
//					// Get XML durations of currEvent, and, if applicable, currEventOrig
//					Integer[] currDurXML = getXMLDur(currEvent);
//
//					String sicEvent = !isCorrected ? currEvent : currEventOrig;
//					String corrEvent = !isCorrected ? null : currEvent;
//
//					boolean defaultCase = !isCorrected; 
//					boolean oneReplacedByMultiple = isCorrected && sicEvent.endsWith(sp);
//					boolean multipleReplacedByOne = 
//						isCorrected && sicEvent.contains(sp) && !sicEvent.endsWith(sp);
//					boolean defaultCorrectedCase = 
//						isCorrected && !oneReplacedByMultiple && !multipleReplacedByOne;
//
//					List<String> sicList = new ArrayList<>();
//					List<String> corrList = new ArrayList<>();
//					// No <sic> and <corr>
//					if (defaultCase) {
//						sicList.add(sicEvent);
//					}
//					// Both <sic> and <corr> contain one <tabGrp>
//					if (defaultCorrectedCase) {
//						sicList.add(sicEvent);
//						corrList.add(corrEvent);
//					}
//					// <sic> contains multiple <tabGrp>s, <corr> one 
//					if (multipleReplacedByOne) {
//						for (String s : sicEvent.split(sp + ss)) {
//							sicList.add(StringTools.removeTrailingSymbolSeparator(s));
//						}
//						corrList.add(corrEvent);
//					}
//					// <sic> contains one <tabGrp>, <corr> multiple 
//					// --> corrList contains corrEvent plus all events following that 
//					// together have the same duration as durSic
//					// NB It is assumed that the replacement is within the bar
//					if (oneReplacedByMultiple) {
//						sicEvent = sicEvent.substring(0, sicEvent.indexOf(sp));
//						sicList.add(StringTools.removeTrailingSymbolSeparator(sicEvent));
//						corrList.add(corrEvent);
//						RhythmSymbol rsSic = 
//							Symbol.getRhythmSymbol(sicEvent.substring(0, sicEvent.indexOf(ss)));
//						int durSic; 
//						if (rsSic != null) {
//							durSic = rsSic.getDuration();
//						}
//						else {
//							durSic = -1; // get last specified duration before currEvent
//						}
//						RhythmSymbol rsCorr = 
//							Symbol.getRhythmSymbol(corrEvent.substring(0, corrEvent.indexOf(ss)));
//						int durCorr;
//						if (rsCorr != null) {
//							durCorr = rsCorr.getDuration();
//						}
//						else {
//							durCorr = -1; // get last specified duration before currEvent
//						}
//						int durCorrToTrack = durCorr;
//						// Iterate through next events until durCorr equals durSic
//						for (int l = j+1; l < currBar.size(); l++) {
//							Event nextEventFull = currBar.get(l);
////							String[] nextEventFull = currBar.get(l);
//							String nextEvent = 
//								StringTools.removeTrailingSymbolSeparator(nextEventFull.getEncoding());
////							String nextEvent = 
////								removeTrailingSymbolSeparator(nextEventFull[Encoding.EVENT_IND]);
//							String nextEventOrig = nextEventFull.getFootnote();
////							String nextEventOrig = nextEventFull[Encoding.FOOTNOTE_IND];
//							// If the next element has a footnote
//							if (nextEventOrig != null) {
//								nextEventOrig = StringTools.removeTrailingSymbolSeparator(nextEventOrig);
//								corrList.add(nextEvent);
//								// Determine duration of corrected event, increment durCorr,
//								// and update durrCorrToTrack
//								RhythmSymbol nextEventRS = 
//									Symbol.getRhythmSymbol(nextEvent.substring(0, 
//									nextEvent.indexOf(ss)));
//								int durCorrNext;
//								if (nextEventRS != null) {
//									durCorrNext = nextEventRS.getDuration();
//									durCorrToTrack = durCorrNext;
//								}
//								else {
//									durCorrNext = durCorrToTrack;
//								}
//								durCorr += durCorrNext;		
//							}
//							if (durCorr == durSic) {
//								int eventsToSkip = corrList.size() - 1;
//								j += eventsToSkip;
//								break;
//							}
//						}
//					}
//
//					String eventAsXML = "";
//					if (isCorrected) {
//						eventAsXML += INDENT_TWO + TAB.repeat(3) + "<choice>" + "\r\n";
//						eventAsXML += INDENT_TWO + TAB.repeat(4) + "<sic>" + "\r\n";
//					}
//					eventAsXML += getTabGrps(sicList, prevDurXML, isCorrected, false, tss);
//					if (isCorrected) {
//						eventAsXML += INDENT_TWO + TAB.repeat(4) + "</sic>" + "\r\n";
//					}
//					if (isCorrected) {
//						eventAsXML += INDENT_TWO + TAB.repeat(4) + "<corr>" + "\r\n";
//						eventAsXML += getTabGrps(corrList, prevDurXML, isCorrected, false, tss);
//						eventAsXML += INDENT_TWO + TAB.repeat(4) + "</corr>" + "\r\n";
//					}
//					if (isCorrected) {
//						eventAsXML += INDENT_TWO + TAB.repeat(3) + "</choice>" + "\r\n";
//					}
//					currBarXMLAsString += eventAsXML;
//					
//					// Update prevDurXML
//					// a. Set prevDurXML to currDurXML (or, if it is null, to the last 
//					// XML duration)
//					if (defaultCase || defaultCorrectedCase || multipleReplacedByOne) {
//						if (currDurXML != null) {
//							prevDurXML = currDurXML;
//						}
//					}
//					// b. Set prevDurXML to the last specified duration in corrList  
//					if (oneReplacedByMultiple) {
//						List<String> corrListRev = new ArrayList<>(corrList);
//						Collections.reverse(corrListRev);
//						for (String event : corrListRev) {
//							Integer[] lastDurXML = getXMLDur(event);
//							if (lastDurXML != null) {
//								prevDurXML = lastDurXML;
//								break;
//							}
//						}
//					}
//				}
//			}
//			// Wrap currBar in measure-staff-layer elements
////			// First bar: no indentation required because of section_content_placeholder placement
////			if (i > 0) {
//			sbBar.append(INDENT_TWO);
////			}
//			sbBar.append("<measure n='" + (i+1) + "'" + barline + ">" + "\r\n");
//			sbBar.append(INDENT_TWO + TAB + "<staff n='1'" + ">" + "\r\n");
//			sbBar.append(INDENT_TWO + TAB.repeat(2) + "<layer n='1'" + ">" + "\r\n");
//			sbBar.append(currBarXMLAsString);
//			sbBar.append(INDENT_TWO + TAB.repeat(2) + "</layer>" + "\r\n");
//			sbBar.append(INDENT_TWO + TAB + "</staff>" + "\r\n");
//			sbBar.append(INDENT_TWO + "</measure>");
////			System.out.println(sb.toString());
//			if (i < events.size()-1) {
//				sbBar.append("\r\n");
//			}
//			
//			sb.append(sbBar);
//			measuresTab.add(sbBar.toString());
//
//			// Append meter change (if applicable)
//			if (i < events.size()-1) {
//				// Check for meter change in first event of next bar
//				List<Event> nextBar = null; //events.get(i+1);
////				List<String[]> nextBar = events.get(i+1);
//				Event firstEventNextFull = nextBar.get(0);
////				String[] firstEventNextFull = nextBar.get(0);
//				String firstEventNext = firstEventNextFull.getEncoding();
////				String firstEventNext = firstEventNextFull[Encoding.EVENT_IND];
//				// Remove final ss
//				firstEventNext = firstEventNext.substring(0, firstEventNext.lastIndexOf(ss));
//				String[] firstEventNextSplit = 
//					(!firstEventNext.contains(ss)) ? new String[]{firstEventNext} : 
//					firstEventNext.split("\\" + ss);
//				// Meter change found? Add scoreDef after bar
//				if (Symbol.getMensurationSign(firstEventNextSplit[0]) != null) {
//					String meterStr = "";
//					for (String s : meters.get(meterIndex+1)) {
//						if (!s.equals("")) {
//							meterStr += s + " ";
//						}
//					}
//					meterStr = meterStr.trim();
//					String scoreDef = INDENT_TWO + "<scoreDef" + " " + meterStr + "/>" + "\r\n";
//					sb.append(scoreDef);
//					measuresTab.add(scoreDef);
//					meterIndex++;
//				}
//			}
//		}
//
////		System.out.println("= = = = = = = = = = = = = =");
////		for (String s : measures) {
////			System.out.println(s);
////		}
////		System.out.println("= = = = = = = = = = = = = =");
////		System.exit(0);
//		res = res.replace(INDENT_TWO + "section_content_placeholder", sb.toString());
//		ToolBox.storeTextFile(res, new File(path + ".xml"));
//	}

}
