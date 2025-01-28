package conversion.exports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence; // package for all midi classes
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;

import conversion.imports.MIDIImport;
import de.uos.fmt.musitech.data.score.NotationChord;
import de.uos.fmt.musitech.data.score.NotationSystem;
import de.uos.fmt.musitech.data.score.NotationVoice;
import de.uos.fmt.musitech.data.score.ScoreNote;
import de.uos.fmt.musitech.data.structure.Note;
import de.uos.fmt.musitech.data.structure.Piece;
import external.Transcription;
import tools.ToolBox;


/**
 * Adapted from http://www.automatic-pilot.com/midifile.html. See also 
 * https://learn.sparkfun.com/tutorials/midi-tutorial/messages
 * http://www.rapidtables.com/convert/number/decimal-to-hex.htm
 * 
 * @author rdv
 *
 */
public class MIDIExport {

	// See https://en.wikipedia.org/wiki/General_MIDI#Program_change_events
	public static final int PIANO = 1-1;
	public static final int HARPSICHORD = 7-1;
	public static final int GUITAR = 25-1;
	public static final int TRUMPET = 57-1;
	public static int DEFAULT_INSTR = TRUMPET;
	private static int TICKS_PER_BEAT = 256; 
	
	
	public static void main(String argv[]) {
		
		String path = "C:/Users/Reinier/Desktop/MIDI-test/";
//		String piece = "phalese-1547_7-tant_que-a3";
		String piece = "newsidler-1544_2-nun_volget";
		
		Transcription t = new Transcription(
			new File(path + piece + MIDIImport.MID_EXT)
//			, new File("F:/research/data/encodings/intabulations/3vv/" + piece + Encoding.EXTENSION)
		);
		ToolBox.storeObjectBinary(t, new File(path + piece + ".ser"));
			
		Transcription stored =	
			ToolBox.getStoredObjectBinary(new Transcription(), new File(path + piece + ".ser"));
		Piece p = stored.getScorePiece();
		
		List<Integer> instruments = null; // Arrays.asList(new Integer[]{h, h, h, h});
		String s = "C:/Users/Reinier/Desktop/MIDI-test/midifile2.mid";
//		exportMidiFile(p, instruments, s);
	}
	
//	http://www.somascape.org/midi/tech/mfile.html
//	https://learn.sparkfun.com/tutorials/midi-tutorial/all
//
//	http://pages.uoregon.edu/emi/32.php
//	https://ccrma.stanford.edu/~craig/articles/linuxmidi/misc/essenmidi.html
//	https://www.cs.cmu.edu/~music/cmsip/readings/MIDI%20tutorial%20for%20programmers.html
//	https://www.nyu.edu/classes/bello/FMT_files/9_MIDI_code.pdf
//
//	http://www.deluge.co/?q=midi-tempo-bpm

	private void setTicksPerBeat(int tpb) {
		TICKS_PER_BEAT = tpb;
	}
	
	
//	public static void setDefaultInstrument(int arg) {
//		defaultInstr = arg;
//	}
	
	
	/**
	 * Calculates the tick onset and offset for all the given meters, given the ticks 
	 * per beat (quarter note).
	 * 
	 * @param meterInfo
	 * @param ticksPerBeat
	 * @return For each meter, tick onset of the first bar and the tick offset of the 
	 *         last bar in that meter.  
	 */
	// TESTED
	static List<Integer[]> getTimeSigTicks(List<Integer[]> meterInfo, int ticksPerBeat) {
		List<Integer[]> timeSigTicks = new ArrayList<Integer[]>();
		int first = 0;
		for (int i = 0; i < meterInfo.size(); i++) {
			Integer[] in = meterInfo.get(i);
			// Number of bars in current meter
			int numBars = (in[3] - in[2]) + 1;
			// Number of quarter notes in current meter (2/2 = 4; 3/4 0 3; 6/8 = 3; 3/8 = 1.5; ...)
			double numQuarters = (4.0/in[1]) * in[0]; 
			// Onset of first and offset of last tick in current meter
			if (first != 0) {
				first = timeSigTicks.get(i-1)[1];
			}
			int last = first + (int)(numBars*numQuarters*ticksPerBeat);
			timeSigTicks.add(new Integer[]{first, last});
			// Reset first
			first = last;
		}
		return timeSigTicks;
	}


	/**
	 * Returns a MIDI sequence of the given Piece.
	 * 
	 * @param p The Piece.
	 * @param instruments The instrument for each voice. If the list contains only one element, 
	 *        this instrument is used for all tracks.
	 * @param meterInfo The meterInfo for the Piece.       
	 */
	private static Sequence exportMidiFile(Piece p, List<Integer> instruments, List<Integer[]> meterInfo,
		List<Integer[]> keyInfo) { // 05.12 added meterInfo and keyInfo
		
		NotationSystem ns = p.getScore();
		int numVoices = ns.size();
		
//		System.out.println(ns.size());
//		for (NotationStaff nst : ns) {
//			System.out.println("SIZE = " + nst.get(0).size());
//			for (NotationVoice nv : nst) {
//				System.out.println(nv);
//				for (NotationChord nc: nv) {
//					System.out.println(nc);
//				}
//			}
//			System.out.println("-----------");
//		}
//		System.exit(0);

//		long lastOffTick = 0;
//		for (NotationStaff nst : ns) {
//			NotationVoice nv = nst.get(0);
//			Note lastNote = nv.get(nv.size() - 1).get(0);
//			ScoreNote sn = lastNote.getScoreNote();
//			long offTick = 
//				(long) (sn.getMetricTime().mul(4*TICKS_PER_BEAT)).toDouble() + 
//				(long) (sn.getMetricDuration().mul(4*TICKS_PER_BEAT)).toDouble();
//			if (offTick > lastOffTick) {
//				lastOffTick = offTick;
//			}
//		}
//		System.out.println(lastOffTick);
		
		Sequence seq = null;
		// Create the MIDI file
		try {
			// Create a MIDI sequence with TICKS_PER_BEAT ticks per beat
			seq = new Sequence(javax.sound.midi.Sequence.PPQ, TICKS_PER_BEAT);
//			Sequence seq = new Sequence(javax.sound.midi.Sequence.PPQ, TICKS_PER_BEAT);

			// Create tracks: one for meta messages + one for each voice
			List<Track> tracks = new ArrayList<Track>();
			for (int i = 0; i < numVoices+1; i++) {
				tracks.add(seq.createTrack());
			}
			
			// SYSTEM MESSAGES
			SysexMessage sm = new SysexMessage();
			// General MIDI SysEx to turn on General MIDI sound set (0xF0)
			byte[] b = {(byte)0xF0, 0x7E, 0x7F, 0x09, 0x01, (byte)0xF7};
			sm.setMessage(b, 6);
			tracks.get(0).add(new MidiEvent(sm,0));
			
			// META MESSAGES
			// See http://www.recordingblogs.com/sa/Wiki/topic/MIDI-meta-messages
			MetaMessage mt;
			// Tempo (0x51)
			// Tempo is in microsecs/beat (quarter note). The three bytes in bt form a hexadecimal 
			// value; since there are 60M microsec/min, the tempo is 60M/that value
			// See http://www.recordingblogs.com/sa/Wiki/topic/MIDI-Set-Tempo-meta-message
			mt = new MetaMessage();
			byte[] bt = {0x07, (byte)0xA1, 0x20}; // 0x07A120 = 500,000 --> tempo = 60/0.5=120 
			mt.setMessage(0x51, bt, 3);
			tracks.get(0).add(new MidiEvent(mt,0));
			
			// Time signature (0x58)
			// The four bytes in bts (nn, dd, cc, bb) indicate num, den (as 2^dd), per how many 
			// MIDI clock ticks a metronome click is given (the standard MIDI clock has 24 ticks
			// per quarter note), and the number of 32nd notes per quarter note
//			List<Integer[]> meterInfo = Transcription.createMeterInfo(p); // 05.12 commented out
			List<Integer[]> timeSigTicks = getTimeSigTicks(meterInfo, TICKS_PER_BEAT);
			for (int i = 0; i < meterInfo.size(); i++) {
				Integer[] mi = meterInfo.get(i);
				Integer[] tst = timeSigTicks.get(i);
				mt = new MetaMessage();
				// 2^x = den --> x = log_2(den) = log_e(den)/log_e(2)
				double dd = Math.log(mi[Transcription.MI_DEN])/Math.log(2);
				byte[] bts = {(byte)(int)mi[Transcription.MI_NUM], (byte)dd, 0x18, 0x08}; // nn=in[0], cc=24, bb=8
				mt.setMessage(0x58, bts, 4);			
				tracks.get(0).add(new MidiEvent(mt,tst[0]));
			}
			
			byte[] sigs = new byte[]{0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};
			byte[] majMin = new byte[]{0x00, 0x01};
			
			// Key signature (0x59)
//			List<Integer[]> keyInfo = Transcription.createKeyInfo(p, meterInfo); // 05.12 commented out
			for (int i = 0; i < keyInfo.size(); i++) {
				Integer[] ki = keyInfo.get(i);
				mt = new MetaMessage();
				byte[] bts = 
					{(byte)(int)ki[Transcription.KI_KEY], 
					 (byte)(int)ki[Transcription.KI_MODE]};
				mt.setMessage(0x59, bts, 2);
				tracks.get(0).add(new MidiEvent(mt,0));
			}

			// Track name (0x03)
			for (int i = 0; i < tracks.size(); i++) {
				mt = new MetaMessage();
				String TrackName = new String("track " + i);
				mt.setMessage(0x03, TrackName.getBytes(), TrackName.length()); 
				tracks.get(i).add(new MidiEvent(mt,0));
			}
		
			// End of track (0x2F)
			long lastOffTick = timeSigTicks.get(timeSigTicks.size() - 1)[1];
			mt = new MetaMessage();
			byte[] bet = {}; 
			mt.setMessage(0x2F, bet, 0);
			tracks.get(0).add(new MidiEvent(mt, lastOffTick));

			// STATUS MESSAGES
			ShortMessage mm = new ShortMessage();
			// Continuous controller (0xB_) 
			mm.setMessage(0xB0, 0x7D, 0x00); // 0x7D = Omni Mode on
			tracks.get(0).add(new MidiEvent(mm, 0)); 
			mm = new ShortMessage();
			mm.setMessage(0xB0, 0x7F, 0x00); // 0x7F = Polyphonic Mode on 
			tracks.get(0).add(new MidiEvent(mm, 0));

			// Instrument (0xC_)
			// For a list see https://en.wikipedia.org/wiki/General_MIDI
			int instr = instruments.get(0);
			for (int i = 1; i < tracks.size(); i++) {
				mm = new ShortMessage();
//				int instr = defaultInstr;
				if (instruments.size() > 1) {
//				if (instruments != null) {
					instr = instruments.get(i);
				}
				mm.setMessage(0xC0+(i-1), instr, 0x00); // -1 needed because of start at 1
				tracks.get(i).add(new MidiEvent(mm,0));
			}
		
			// Note on- and off (0x9_ and 0x8_)		
			for (int i = 0; i < ns.size(); i++) {
				Track currTrack = tracks.get(i+1);
				NotationVoice nv = ns.get(i).get(0);
				for (int j = 0; j < nv.size(); j++) {
//					Note n = nv.get(j).get(0); 
					NotationChord nc = nv.get(j);
					for (Note n : nc) {
						ScoreNote sn = n.getScoreNote();
						long onTick = (long) sn.getMetricTime().mul(4*TICKS_PER_BEAT).toDouble();
						long offTick = 
							onTick + ((long) sn.getMetricDuration().mul(4*TICKS_PER_BEAT).toDouble());
				
						// Note on
						mm = new ShortMessage();
						mm.setMessage(0x90+i, n.getMidiPitch(), 90);
						currTrack.add(new MidiEvent(mm, onTick));
					
						// Note off
						mm = new ShortMessage();
						mm.setMessage(0x80+i, n.getMidiPitch(), 0);
						currTrack.add(new MidiEvent(mm, offTick));
					}
				}
			}

//			// Write the MIDI sequence to a MIDI file
//			File f = new File(path);
//			f.getParentFile().mkdirs();
//			MidiSystem.write(seq, 1, f);
//				

		} catch(Exception e) {
			System.out.println("Exception caught " + e.toString());
		}
		return seq;
	}


	/**
	 * Stores the given piece as a MIDI file in the give location.
	 * 
	 * @param p The piece
	 * @param instruments The instrument for each voice. If the list contains only one element, 
	 *        this instrument is used for all tracks.
	 * @param meterInfo The meterInfo for the Piece.       
	 * @param path The path where the MIDI file is saved 
	 */
	public static void exportMidiFile(Piece p, List<Integer> instruments, List<Integer[]> meterInfo, 
		List<Integer[]> keyInfo, String path) { // 05.12 added meterInfo and keyInfo

		Sequence seq = exportMidiFile(p, instruments, meterInfo, keyInfo);

		File f = new File(path);
		f.getParentFile().mkdirs();
		try {
			MidiSystem.write(seq, 1, f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

} 
