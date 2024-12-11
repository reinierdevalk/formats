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

import external.Transcription;
import interfaces.CLInterface;

public class MIDIExportTest {

//	private File midiTestGetMeterInfoDiminuted;
	private File midiTestGetMeterInfo;

	@Before
	public void setUp() throws Exception {
//		Runner.setPathsToCodeAndData(UI.getRootDir(), false);
//		midiTestGetMeterInfoDiminuted = new File(Runner.midiPathTest + "test_get_meter_key_info_diminuted.mid");
		Map<String, String> paths = CLInterface.getPaths(true);
		String mp = paths.get("MIDI_PATH");
		String td = "test/5vv/";
		midiTestGetMeterInfo = new File(CLInterface.getPathString(
			Arrays.asList(mp, td)) + "test_get_meter_key_info.mid"
		);
//		midiTestGetMeterInfoDiminuted = new File(MEIExport.rootDir + "data/annotated/MIDI/test/" + "test_get_meter_key_info_diminuted.mid");
	}

	@After
	public void tearDown() throws Exception {
	}


	@Test
	public void testGetTimeSigTicks() {
		Transcription t = new Transcription(midiTestGetMeterInfo);

		List<Integer[]> expected = new ArrayList<Integer[]>();
		expected.add(new Integer[]{0, 0 + 3*256}); // 768
		expected.add(new Integer[]{768, 768 + 16*256 }); // 4864
		expected.add(new Integer[]{4864, 4864 + 24*256}); // 11008
		expected.add(new Integer[]{11008, 11008 + 8*256}); // 13056
		expected.add(new Integer[]{13056, 13056 + (256 + 64)}); // 13376
		expected.add(new Integer[]{13376, 13376 + 2*256});

		List<Integer[]> actual = MIDIExport.getTimeSigTicks(t.getMeterInfo(), 256);

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i).length, actual.get(i).length);
			for (int j = 0; j < expected.get(i).length; j++) {
				assertEquals(expected.get(i)[j], actual.get(i)[j]);
			}
		}
	}

}
