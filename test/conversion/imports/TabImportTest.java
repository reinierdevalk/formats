package conversion.imports;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import conversion.imports.TabImport;
import interfaces.CLInterface;
import internal.core.Encoding;

public class TabImportTest {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCreateMeterInfoString() {
		Map<String, String> paths = CLInterface.getPaths(true);
		String ep = paths.get("ENCODINGS_PATH");
		
		List<String> testPieces = Arrays.asList(new String[]{
			"test/5vv/testpiece.tbp",
			//
			"thesis-int/3vv/judenkuenig-1523_2-elslein_liebes.tbp",
			"thesis-int/3vv/newsidler-1536_7-disant_adiu.tbp",
			"thesis-int/3vv/newsidler-1536_7-mess_pensees.tbp",
			"thesis-int/3vv/newsidler-1544_2-nun_volget.tbp",
			"thesis-int/3vv/phalese-1547_7-tant_que-3vv.tbp",
			"thesis-int/3vv/pisador-1552_7-pleni_de.tbp",
			//
			"thesis-int/4vv/abondante-1548_1-mais_mamignone.tbp",
			"thesis-int/4vv/barbetta-1582_1-il_nest.tbp",
			"thesis-int/4vv/ochsenkun-1558_5-absolon_fili.tbp",
			"thesis-int/4vv/ochsenkun-1558_5-herr_gott.tbp",
			"thesis-int/4vv/ochsenkun-1558_5-in_exitu.tbp",
			"thesis-int/4vv/ochsenkun-1558_5-qui_habitat.tbp",
			"thesis-int/4vv/phalese-1547_7-tant_que-4vv.tbp",
			"thesis-int/4vv/phalese-1563_12-il_estoit.tbp",
			"thesis-int/4vv/phalese-1563_12-las_on.tbp",
			"thesis-int/4vv/rotta-1546_15-bramo_morir.tbp"
		});

		List<String> expected = Arrays.asList(new String[]{
			"2/2 (1-3)", // testpiece
			//
			"3/4 (1-24)", // elslein
			"2/2 (1-33)", // disant
			"2/2 (1-86)", // mess
			"2/2 (1-41); 3/4 (42-49); 2/2 (50-96)", // nun NB: not as in file! 
			"2/2 (1-22)", // tant
			"2/2 (1-43)", // pleni
			//
			"2/2 (1-50)", // mais
			"2/2 (1-30)", // il nest
			"2/2 (1-85)", // absolon
			"2/2 (1-23)", // herr 
			"2/2 (1-143)", // in
			"2/2 (1-155)", // qui
			"2/2 (1-26)", // tant
			"2/2 (1-20)", // il
			"2/2 (1-45)", // las
			"2/2 (1-59)" // bramo
		});

		List<String> actual = new ArrayList<>();
		for (String s : testPieces) {
			Encoding enc = new Encoding(new File(ep + s));
			String clean = enc.getCleanEncoding();
			String tss = enc.getMetadata().get(Encoding.METADATA_TAGS[Encoding.TABSYMBOLSET_IND]);
			actual.add(TabImport.createMeterInfoString(clean, tss));
		}

		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); i++) {
			assertEquals(expected.get(i), actual.get(i));
		}
	}

}
