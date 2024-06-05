package conversion.exports;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import external.Transcription;
import junit.framework.TestCase;
import tools.path.PathTools;

public class MIDIExportTest extends TestCase {

//	private File midiTestGetMeterInfoDiminuted;
	private File midiTestGetMeterInfo;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
//		Runner.setPathsToCodeAndData(UI.getRootDir(), false);
//		midiTestGetMeterInfoDiminuted = new File(Runner.midiPathTest + "test_get_meter_key_info_diminuted.mid");
		Map<String, String> paths = PathTools.getPaths();
		String mp = paths.get("MIDI_PATH");
		String td = "test";
		midiTestGetMeterInfo = new File(PathTools.getPathString(
			Arrays.asList(mp, td)) + "test_get_meter_key_info.mid"
		);
//		midiTestGetMeterInfoDiminuted = new File(MEIExport.rootDir + "data/annotated/MIDI/test/" + "test_get_meter_key_info_diminuted.mid");
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}


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
