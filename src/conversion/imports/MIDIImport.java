package conversion.imports;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import de.uos.fmt.musitech.data.score.NotationChord;
import de.uos.fmt.musitech.data.score.NotationStaff;
import de.uos.fmt.musitech.data.score.NotationSystem;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.data.score.ScoreNote;
import de.uos.fmt.musitech.data.score.ScorePitch;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.data.structure.Piece;
import de.uos.fmt.musitech.data.structure.container.Containable;
import de.uos.fmt.musitech.data.time.MetricalTimeLine;
import de.uos.fmt.musitech.data.time.TimeSignatureMarker;
import de.uos.fmt.musitech.performance.midi.MidiReader;
import de.uos.fmt.musitech.utility.math.Rational;
import external.Tablature;
import internal.core.ScorePiece;
import tools.ToolBox;

public class MIDIImport {
	
	public static final String EXTENSION = ".mid";

	public static void main(String[] args) {
		
	}
	
	public static void main2(String[] args) {
		List<String> pieces = Arrays.asList(new String[]{
				"bach-WTC1-fuga_4-BWV_849",
				"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_1", 
				"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_2",
				"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_3",
				"bach-WTC1-fuga_4-BWV_849-split_at_44-65-86_4",
				"bach-WTC1-fuga_22-BWV_867",
				"bach-WTC1-fuga_22-BWV_867-split_at_37_1",
				"bach-WTC1-fuga_22-BWV_867-split_at_37_2",
			});
		
		for (String s : pieces) {
			String piece = s + EXTENSION;
			File curr = new File("F:/research/data/annotated/MIDI/bach-WTC/thesis/5vv/" + piece);
			File gith = new File("I:/removed_from_research-software-github/data-old/ISMIR-2018/" + piece);
		
//			MidiImport midiImport = new MidiImport();
			String currStr = "";
			int count = 1;
			currStr += Arrays.asList(importMidiFile(curr).getHarmonyTrack().getContentsRecursive()) + "\r\n";
			currStr += importMidiFile(curr).getMetricalTimeLine() + "\r\n";
			for (NotationStaff ns : importMidiFile(curr).getScore()) {
				currStr += "staff " + count + "\r\n";
				count++;
				for (NotationVoice nss : ns) {
					for (NotationChord nc : nss) {
						for (Note n : nc) {
							currStr += n + "\r\n";
						}
					}
				}
			}
//			System.out.println(currStr);
			String githStr = "";
			count = 1;
			githStr += Arrays.asList(importMidiFile(gith).getHarmonyTrack().getContentsRecursive()) + "\r\n";
			githStr += importMidiFile(gith).getMetricalTimeLine() + "\r\n";
			for (NotationStaff ns : importMidiFile(gith).getScore()) {
				githStr += "staff " + count + "\r\n";
				count++;
				for (NotationVoice nss : ns) {
					for (NotationChord nc : nss) {
						for (Note n : nc) {
							githStr += n + "\r\n";
						}
					}
				}
			}
//			System.out.println(githStr);
			System.out.println(currStr.equals(githStr));
		}
		System.exit(0);
//		ScoreEditor scoreEditor = new ScoreEditor(fullScore.getScore());
//		JFrame fullScoreFrame = new JFrame(dir.toString());
//		fullScoreFrame.add(new JScrollPane(scoreEditor));
//		fullScoreFrame.setSize(800, 600);
//		fullScoreFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		fullScoreFrame.setVisible(true);    
	}


	/**
	 * Rounds the given fraction by incrementally decreasing and increasing its numerator
	 * (-1, +1, -2, +2, -3, +3, ...) until the denominator of the resulting reduced fraction 
	 * equals 1.
	 * 
	 * @param r
	 * @return
	 */
	// TESTED
	static Rational roundFraction(Rational r) {
		int numer = r.getNumer();
		int denom = r.getDenom(); 
		int diff = 1;
		while (r.getDenom() != 1) {
			// Try subtraction 
			r = new Rational((numer-diff), denom);
			r.reduce();
			if (r.getDenom() == 1) {
				break;
			}
			// Reset and try addition
			r = new Rational(numer, denom);
			r = new Rational((numer+diff), denom);
			r.reduce();
			if (r.getDenom() == 1) {
				break;
			}
			diff++;
		}
		return r; 
	}


	/**
	 * Imports a MIDI file and converts it into a Piece, where each MIDI track forms a 
	 * NotationStaff in the Piece. 
	 * 
	 * @param f
	 * @return 
	 */
	public static Piece importMidiFile(File f) {
		String fileName = f.getName();
		URL url;
		if (!fileName.endsWith(EXTENSION)) {
			throw new RuntimeException("ERROR: the file is not a MIDI file.");
		}
		try {
			url = f.toURI().toURL();
		}
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		
		Sequence sequence = null;
		try {
			sequence = MidiSystem.getSequence(url);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		boolean useThis = false;
		if (useThis) {
			Track[] tracks = sequence.getTracks();
			byte key = -1;
			byte mode = -1;
			for (int i = 0; i < tracks.length; i++) {
				Track t = tracks[i];
				for (int j = 0; j < t.size(); j++) {
					MidiEvent me = t.get(j);
					byte[] b = me.getMessage().getMessage(); 
					if (b[1] == 0x59) { // key signature
						System.out.println("found in track " + i);
						key = b[b.length-2];
						mode = b[b.length-1];
						break;
					}
				}
			}
			System.out.println("key = " + key);
			System.out.println("mM  = " + mode);
		}
//-*-		System.out.println(url);
		
		Piece p = new MidiReader().getPiece(url);
		boolean quantiseTriplets = false;
		if (quantiseTriplets) {
			int srv = Tablature.SRV_DEN;
			NotationSystem ns = p.getScore(); 
			for (int i = 0; i < ns.size(); i++) {
				NotationVoice voice = ns.get(i).get(0);  
				for (int j = 0; j < voice.size(); j++) {
					NotationChord notationChord = voice.get(j);
					for (int k = 0; k < notationChord.size(); k++) {
						Note originalNote = notationChord.get(k);
						// Onset
						Rational onset = originalNote.getMetricTime();
						Rational onsetQuantised = new Rational(onset.getNumer(), onset.getDenom());
						if (onset.mul(srv).getDenom() != 1) {
							onset = onset.mul(srv);
							onsetQuantised = roundFraction(onset);
							// onsetQuantised is now a Rational with denom 1
							onsetQuantised = onsetQuantised.mul(new Rational(1, srv));
//							// If onset was moved back, increase duration with difference 
//							// (diff > 0 --> add diff to dur);
//							// If onset was moved forward, decrease duration with difference
//							// (diff < 0 --> add diff to dur)
//							Rational dur = originalNote.getMetricDuration().mul(srv);
//							Rational diff = onset.sub(onsetQuantised);
//							Rational durQuantised = dur.add(diff);
						}
						// Duration
						Rational dur = originalNote.getMetricDuration();
						Rational durQuantised = new Rational(dur.getNumer(), dur.getDenom());
						if (dur.mul(srv).getDenom() != 1) {
							dur = dur.mul(srv);
							durQuantised = roundFraction(dur);
							// durQuantised is now a Rational with denom 1
							durQuantised = durQuantised.mul(new Rational(1, srv));
						}
						if (!onsetQuantised.equals(onset) || !durQuantised.equals(dur)) {
							Note quantisedNote = ScorePiece.createNote(
								originalNote.getMidiPitch(), onsetQuantised, durQuantised, -1, null);
							notationChord.remove(originalNote);
							notationChord.add(quantisedNote);
						}
					}
				}
			}
		}
		
		p.setName(f.getName());
//		KeyMarker keyMarker = new KeyMarker(Rational.ZERO, 0); 
//		if (mode == 0) {
//			keyMarker.setAlterationNumAndMode(key, KeyMarker.Mode.MODE_MAJOR);
//		}
//		else {
//			keyMarker.setAlterationNumAndMode(key, KeyMarker.Mode.MODE_MINOR);
//		}

		// Remove any empty NotationStaffs
		NotationSystem ns = p.getScore();
		List<Integer> toRemove = new ArrayList<Integer>();
		for (int i = 0; i < ns.size(); i++) {
			NotationStaff nst = ns.get(i); 
			// If the first (and thus any other) NotationVoice in nst is empty
//			System.out.println("i = " + i);
			if (nst.get(0).size() == 0) {
				toRemove.add(i);
			}
			// Special case for 2vv inventions that have one note in tr2, ch9 and tr3, ch9  
			else if (f.getName().contains("bach-inv-inventio") && nst.get(0).size() == 1) {
				toRemove.add(i);
			}
		}
		int removed = 0;
		for (int i : toRemove) {
			ns.remove(i - removed);
			removed++;
		}
//		System.out.println(toRemove);
//		System.out.println(ns.size());
//		System.exit(0);

//		for (int i = 0; i < ns.size(); i++) {
//			NotationStaff nst = ns.get(i); 
//			// If the first (and thus any other) NotationVoice in nst is empty
//			if (nst.get(0).size() == 0) {
//				System.out.println(i + "  is leeg" );
//			}
//		}

		Rational mltpl = null;
//		Rational mltpl = new Rational(27, 32);
		if (mltpl != null) {
			// Adapt note durations
			Collection<Containable> notesInPart = p.getScore().getContentsRecursiveList(null);
			for (Containable c : notesInPart) {
				if (c instanceof Note) {	
					ScorePitch pitch = new ScorePitch(((Note) c).getMidiPitch());
					Rational ons = ((Note) c).getMetricTime();
					ons = ons.mul(mltpl);
					Rational dur = ((Note) c).getMetricDuration();
					dur = dur.mul(mltpl);
					((Note) c).setScoreNote(new ScoreNote(pitch, ons, dur));
				}	
			}
	
//			for (NotationStaff nos : p.getScore()) {
//				NotationVoice nv = nos.get(0);
//				for (NotationChord nc : nv) {
//					System.out.println(nc);
//				}
//			}
			
			// Adapt time signature positions
			MetricalTimeLine mtl = p.getMetricalTimeLine();
			for (long[] ts : mtl.getTimeSignature()) {
				Rational timeOld = new Rational(ts[3], ts[4]);
				Rational timeNew = timeOld.mul(mltpl);
				timeNew.reduce();
				TimeSignatureMarker tsm = mtl.getTimeSignatureMarker(timeOld);
				tsm.setMetricTime(timeNew);
			}
		}
		return p;
	}


	/**
	 * Combines the MIDIfiles of the separate voice parts in the given directory into a single Piece (score). 
	 * 
	 * @param f Is a directory when called in cross-validation; is a file when called on new data.
	 * @return 
	 */
	private static Piece importMidiFilesOLD(File f) {
		// NEW -->
		Rational mltpl = new Rational(27, 32);
		long[][] timeSigs = null;
		MetricalTimeLine mtl = new MetricalTimeLine();
		// <-- NEW
		
		// 1. Create a new Piece and make a NotationSystem for it 
		Piece piece = new Piece();    
		NotationSystem notationSystem = piece.createNotationSystem();

		// 2. Add each MIDIfile in dir to piece
		List<String> midiFileNames = new ArrayList<String>();
		// If f is a directory
		if (f.isDirectory()) {
			String[] fileNames = f.list();
			for (String s : fileNames) {
				if (s.endsWith(EXTENSION)) {
					midiFileNames.add(s);  
				}
			}
			// Sort midiFileNames numerically (if it is already sorted, nothing changes)
			midiFileNames = ToolBox.bubbleSortString(midiFileNames);
			// If midiFileNames contains Strings not starting with a number between and including 0-9: throw Exception 
			if (midiFileNames == null) {
				throw new RuntimeException("ERROR: One of the file names does not start with a number.");
			}
		}
		// If f is not a directory, i.e., a MIDI file 
		else {
			String s = f.getName();
			if (s.endsWith(EXTENSION)) {
				midiFileNames.add(s);  
			}
		}

		URL url;
		for (String midiFileName : midiFileNames) {
			// a. Make a Staff and add it to notationSystem
			NotationStaff staff = new NotationStaff(notationSystem);
			notationSystem.add(staff);
			// Ensure correct cleffing for each staff
			if (f.isDirectory()) {
				if (midiFileName.endsWith("S.mid") || midiFileName.endsWith("A.mid")) {
					staff.setClefType('g', -1, 0);
				}
				else {
					staff.setClefType('f', 1, 0);
				}
			}
			// b. Make a NotationVoice and add it to staff
			NotationVoice notationVoice = new NotationVoice(staff);
			staff.add(notationVoice);
			
			// c. Create a Piece from the MIDI file. The Piece is a single part
			if (!midiFileName.endsWith(EXTENSION)) {
				continue;
			}
			try {
				if (f.isDirectory()) {
					url = new File(f + "/" + midiFileName).toURI().toURL();
				}
				else {
					url = f.toURI().toURL();
				}
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
//			MidiReader midiReader = new MidiReader();
			Piece part = new MidiReader().getPiece(url);

			// d. Add the Piece's notes to notationVoice
			Collection<Containable> notesInPart = part.getScore().getContentsRecursiveList(null);
			for (Containable c : notesInPart) {
				if (c instanceof Note) {	
					// NEW -->
					ScorePitch pitch = new ScorePitch(((Note) c).getMidiPitch());
					Rational ons = ((Note) c).getMetricTime();
					ons = ons.mul(mltpl);
					Rational dur = ((Note) c).getMetricDuration();
					dur = dur.mul(mltpl);
					((Note) c).setScoreNote(new ScoreNote(pitch, ons, dur));
//					System.out.println(c);				
					// <-- NEW
					notationVoice.add((Note) c); 	
				}	
			}
			
//			// NEW -->
			// Get time signature information 
//			long[][] ts = part.getMetricalTimeLine().getTimeSignature();
//			for (long[] l : ts) {
//				mtl.add(new TimeSignatureMarker((int) l[0], (int) l[1],	
//					new Rational((int) l[3]*mltpl.getNumer(), (int) l[4]*mltpl.getDenom())));
//				System.out.println(l.length);
//				System.out.println(Arrays.toString(l));
//			}
//			// <-- NEW
		}
		
		
		piece.setMetricalTimeLine(mtl);
		
		long[][] ts = piece.getMetricalTimeLine().getTimeSignature();
//		System.out.println("HIERRRRRRRRRRRRRRRRR");
		for (long[] l : ts) {
//			System.out.println(Arrays.toString(l));
		}
		
//		System.exit(0);
		return piece;
	}


	/**
	 * Combines the MIDIfiles of the separate voice parts in the given directory into a single Piece (score). 
	 * 
	 * @param dir
	 * @return 
	 */
	private static Piece importMidiFilesOLDEST(File dir) {
		// 1. Create a new Piece and make a NotationSystem for it 
		Piece piece = new Piece();    
		NotationSystem notationSystem = piece.createNotationSystem();

		// 2. Add each MIDIfile in dir to piece
		String[] fileNames = dir.list();
		// Make a list that contains only the MIDI file names in fileNames
		List<String> midiFileNames = new ArrayList<String>();
		for (String s : fileNames) {
			if (s.endsWith(EXTENSION)) {
				midiFileNames.add(s);  
			}
		}
		// Sort midiFileNames numerically (if it is already sorted, nothing changes)
		midiFileNames = ToolBox.bubbleSortString(midiFileNames);
		// If midiFileNames contains Strings not starting with a number between and including 0-9: throw Exception 
		if (midiFileNames == null) {
			throw new RuntimeException("ERROR: One of the file names does not start with a number.");
		}

		URL url;
		for (String midiFileName : midiFileNames) {
			if (!midiFileName.endsWith(EXTENSION)) {
				continue;
			}
			try {
				url = new File(dir + "/" + midiFileName).toURI().toURL();
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}
			// b. Turn the MIDIfile into a Piece (a single part)
			MidiReader midiReader = new MidiReader();
			Piece singlePartPiece = midiReader.getPiece(url);
			// c. Make a Staff and add it to notationSystem
			NotationStaff staff = new NotationStaff(notationSystem);
			notationSystem.add(staff);
			// Ensure correct cleffing for each staff
			if (midiFileName.endsWith("S.mid") || midiFileName.endsWith("A.mid")) {
				staff.setClefType('g', -1, 0);
			}
			else {
				staff.setClefType('f', 1, 0);
			}
			// d. Make a NotationVoice and add it to staff
			NotationVoice notationVoice = new NotationVoice(staff);
			staff.add(notationVoice);
			// e. Get the notes from singlePartPiece and add them to notationVoice
			Collection<Containable> singlePartPieceNotes = singlePartPiece.getScore().getContentsRecursiveList(null);
			for (Containable c : singlePartPieceNotes) {
				if (c instanceof Note) {
					notationVoice.add((Note) c); 
				}
			}
		}
		return piece;
	}

}
  
    // ObjectPlayer player = ObjectPlayer.getInstance();
    // player.setContainer(MidiNoteSequence.convert(voice));
    // TransportButtons trans = new
    // TransportButtons(player.getPlayTimer());
    // trans.setPlayOnly(true);
    //
    // try {
    // Display display = EditorFactory.createDisplay(system);
    // player.getPlayTimer().registerMetricForPush((NotationDisplay)display);
    // JFrame frame = new JFrame(fileName);
    // frame.getContentPane().setLayout(new BorderLayout());
    // frame.getContentPane().add((JComponent)display);
    // frame.getContentPane().add(trans,BorderLayout.NORTH);
    // frame.pack();
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // frame.setVisible(true);
    // } catch (EditorConstructionException e) {
    // e.printStackTrace();
    // }
  

