
package tbp.editor;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import org.apache.commons.io.FilenameUtils;

import conversion.exports.MEIExport;
import external.Tablature;
import interfaces.CLInterface;
import conversion.imports.TabImport;
import internal.core.Encoding;
import internal.core.Encoding.Stage;
import tbp.symbols.Symbol;
import tbp.symbols.TabSymbol.TabSymbolSet;
import tools.ToolBox;

/**
 * @author Reinier de Valk
 * @version 11.06.2023 (last well-formedness check)
 */
public class Editor extends JFrame{
	private static final long serialVersionUID = 1L;

	private static final Font FONT = new Font("Courier New", Font.PLAIN, 12);
	private static final String ASCII = "ASCII tab";
	private static final String TC = "TabCode";
	private static final String MEI = "MEI";
//	private static final String MEI_EXTENSION_ALT = ".mei";
	private static final String TBP = "tab+";
	private static final String[] TITLE = new String[]{
		"untitled", Encoding.TBP_EXT, " - " + "tab+Editor"
	};

	private static final Map<String, String> EXTENSIONS;
	static { EXTENSIONS = new LinkedHashMap<String, String>();
		EXTENSIONS.put(TabImport.TAB_EXT, ASCII);
		EXTENSIONS.put(TabImport.TC_EXT, TC);
		EXTENSIONS.put(MEIExport.MEI_EXT, MEI);
		EXTENSIONS.put(MEIExport.XML_EXT, MEI);
		EXTENSIONS.put(Encoding.TBP_EXT, TBP);
	}

	private static final int HM = 15; // horizontal margin
	private static final int VM = 15; // vertical margin
	private static final int PANEL_W = 900;
	private static final int PANEL_PART_W = PANEL_W/3;
	private static final int ENC_PANEL_H = 200;
	private static final int TAB_PANEL_H = 300;
	private static final int LABEL_W = 90;
	private static final int LABEL_H = VM;
	private static final int BUTTON_W = LABEL_W;
	private static final int BUTTON_H = 2*VM;
	private static final int V_CORRECTION = 157;
	private static final Integer[] PANEL_DIMS = new Integer[]{HM + PANEL_W + HM, -1};
	private static final Integer[] FRAME_DIMS = new Integer[]{
		HM/2 + PANEL_DIMS[0] + HM/2, VM + ENC_PANEL_H + VM + TAB_PANEL_H + VM + V_CORRECTION
	};

	private Highlighter highlighter;
	private JTextArea encodingTextArea;
	private JTextArea tabTextArea;
	private ButtonGroup tabStyleButtonGroup;
	private JCheckBox rhythmFlagsCheckBox;
	private JFileChooser fileChooser;
	private Map<String, String> paths;
	private File file; // .tbp
	private File importFile; // .tab or .tc


	// https://www.youtube.com/watch?v=Z8p_BtqPk78
	// https://www.guru99.com/buffered-reader-in-java.html
	// https://stackoverflow.com/questions/56151113/use-try-with-resources-or-close-this-bufferedreader-in-a-finally-clause
	// https://stackoverflow.com/questions/17010647/set-default-saving-extension-with-jfilechooser
	// https://stackoverflow.com/questions/8402889/working-with-jfilechooser-getting-access-to-the-selected-file
	// https://stackoverflow.com/questions/8852560/how-to-make-popup-window-in-java
	// try-catch block is only needed when reading from a File using a BufferedReader
	public static void main(String[] args) {
		boolean dev = args.length == 0 ? true : args[0].equals(String.valueOf(true));
		String source = args[1];
		String destination = args[2];
		Map<String, String> argPaths = CLInterface.getPaths(dev);

		// No source and destination provided: convert through editor
		if (source.equals("") && destination.equals("")) {
			new Editor(argPaths);
		}
		// Convert directly
		else {
			String cp = argPaths.get("CONVERTER_PATH");
			String inputName = ToolBox.splitExt(source)[0];
			String inputFormat = ToolBox.splitExt(source)[1];
			String outputName = ToolBox.splitExt(destination)[0];
			String outputFormat = ToolBox.splitExt(destination)[1];
			
			System.out.println(inputFormat.equals(TabImport.TC_EXT));
			System.out.println(outputFormat.equals(MEIExport.MEI_EXT));
			
			String fileContent = ToolBox.readTextFile(new File(cp + source));
			
			// MEI
			if (inputFormat.equals(MEIExport.MEI_EXT) || inputFormat.equals(MEIExport.XML_EXT)) {
				
			}
			// ASCII
			else if (inputFormat.equals(TabImport.TAB_EXT)) {

			}
			// tab+
			else if (inputFormat.equals(Encoding.TBP_EXT)) {

			}
			// TabCode
			if (inputFormat.equals(TabImport.TC_EXT)) {
				String tbp = TabImport.tc2tbp(fileContent);
				if (outputFormat.equals(MEIExport.MEI_EXT)) {
					Encoding e = new Encoding(
						tbp, inputName, Stage.RULES_CHECKED
					);
					// Make transParams, containing only those params that MEIExport needs
					// TODO make global and remove also from saveAsLike()
					Map<String, String> transParams = new LinkedHashMap<String, String>();
					transParams.put(CLInterface.TUNING, CLInterface.INPUT);
					transParams.put(CLInterface.TABLATURE, "y");
					transParams.put(CLInterface.TYPE, CLInterface.INPUT);
					String mei = MEIExport.exportMEIFile(
						null, new Tablature(e, false), null, false, false, argPaths,
						transParams, new String[]{null, "abtab -- converter"}
					);
					System.out.println(cp + destination);
					ToolBox.storeTextFile(mei, new File(cp + destination));
				};
			}
		}
	}


	///////////////////////////////
	//
	//  C O N S T R U C T O R S
	//
	/**
	 * Creates a Viewer (JFrame) containing a JMenuBar and a Container with graphical elements.
	 */
	public Editor(Map<String, String> argPaths) {
		super();
		init(argPaths);
	}


	private void init(Map<String, String> argPaths) {
		// a. Viewer instance variables
		setHighlighter();
		setEncodingTextArea();
		setTabTextArea();
		setTabStyleButtonGroup();
		setRhythmFlagsCheckBox();
		setFileChooser(new File(CLInterface.getPathString(Arrays.asList(argPaths.get("CONVERTER_PATH")))));
//		setFileChooser(new File("F:/research/computation/tool_data/converter/"));
//		setFileChooser(new File("F:/research/data/user/in/"));
		setPaths(argPaths);
		setFile(null);
		setImportFile(null);
		// b. JFrame instance variables
		setLayout(null);
		setJMenuBar(makeJMenuBar());
		setContentPane(makeContentPane());
		setTitle(TITLE[0] + TITLE[1] + TITLE[2]);
		setBounds(0, 0, FRAME_DIMS[0], FRAME_DIMS[1]);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setVisible(true);
	}


	//////////////////////////////
	//
	//  S E T T E R S  
	//  for instance variables
	//  (with general makers (e.g., 
	//  make<Type>(), not make<MyVar>())
	//  and makers for elements of
	//  instance variables (e.g., 
	//  makeJPanel()))
	//
	private void setHighlighter() {
		highlighter = new DefaultHighlighter();
	}


	private void setEncodingTextArea() {
		encodingTextArea = makeJTextArea("", getHighlighter(), null);
	}


	private JTextArea makeJTextArea(String content, Highlighter hl, Rectangle bounds) {
		JTextArea ta = new JTextArea();
		// If there is a JScrollPane, the JTextArea's bounds are overridden by the JScrollPane's 
		if (bounds != null) {
			ta.setBounds(bounds);
		}
		ta.setLineWrap(true); // necessary because of JScrollPane
		ta.setEditable(true);
		ta.setFont(FONT);
		ta.setText(content);
		ta.setHighlighter(hl);
		return ta;
	}


	private void setTabTextArea() {
		tabTextArea = makeJTextArea("", null, null);
	}


	private void setTabStyleButtonGroup() {
		tabStyleButtonGroup = makeButtonGroup(
			new String[]{
				TabSymbolSet.FRENCH.getType(), 
				TabSymbolSet.ITALIAN.getType(),
				TabSymbolSet.SPANISH.getType(), 
				TabSymbolSet.NEWSIDLER_1536.getType()}, 
			new Rectangle[]{
				new Rectangle(HM, 2*VM, LABEL_W, LABEL_H),
				new Rectangle(HM, 2*VM + LABEL_H, LABEL_W, LABEL_H),
				new Rectangle(HM + LABEL_W, 2*VM, LABEL_W, LABEL_H),
				new Rectangle(HM + LABEL_W, 2*VM + LABEL_H, LABEL_W, LABEL_H)
			}, 
			new Boolean[]{true, false, false, false}, 
			new Boolean[]{true, true, true, false}
		);
	}


	private ButtonGroup makeButtonGroup(String[] t, Rectangle[] bounds, Boolean[] selected, 
		Boolean[] enabled) {
		ButtonGroup bg = new ButtonGroup();
		for (int i = 0; i < bounds.length; i++) {
			JRadioButton rb = new JRadioButton(t[i]);
			rb.setBounds(bounds[i]);
			rb.setSelected(selected[i]);
			rb.setEnabled(enabled[i]);
			bg.add(rb);
		}
		return bg;
	}


	private void setRhythmFlagsCheckBox() {
		rhythmFlagsCheckBox = makeJCheckBox(
			"Hide repeated rhythm flags",
			new Rectangle(HM, 2*VM, LABEL_W*2 + (LABEL_W / 2), LABEL_H)
		);
	}


	private JCheckBox makeJCheckBox(String t, Rectangle bounds) {
		JCheckBox cb = new JCheckBox();
		cb.setBounds(bounds);
		cb.setText(t);
		return cb;
	}


	private void setFileChooser(File f) {
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(f);
		fileChooser = fc;
	}


	private void setPaths(Map<String, String> p) {
		paths = p;
	}


	private void setFile(File f) {
		file = f;
	}


	private void setImportFile(File f) {
		importFile = f;
	}


	private JMenuBar makeJMenuBar() {
		JMenuBar mb = new JMenuBar();

		// File
		JMenu m = new JMenu("File");
		for (String s : Arrays.asList("New", "Open", "Save", "Save as", "Import", "Export")) {
			// Add JMenuItem
			if (!Arrays.asList("Import", "Export").contains(s)) {
				JMenuItem mi = new JMenuItem(s);
				mi.addActionListener(defineActionListener(s, null));
				m.add(mi);
			}
			// Add JMenu with JMenuItems
			else {
				JMenu mm = new JMenu(s);
				for (String ss : (s.equals("Import") ? Arrays.asList(TabImport.TAB_EXT, TabImport.TC_EXT) : 
					Arrays.asList(TabImport.TAB_EXT, MEIExport.MEI_EXT))) {
					JMenuItem mi = new JMenuItem(EXTENSIONS.get(ss));
					mi.addActionListener(defineActionListener(s, ss));
					mm.add(mi);
				}
				m.add(mm);
			}
		}
		mb.add(m);

		// Edit
		m = new JMenu("Edit");
		for (String s : Arrays.asList("Select all")) {
			JMenuItem mi = new JMenuItem(s);
			mi.addActionListener(defineActionListener(s, null));
			m.add(mi);
		}
		mb.add(m);

		return mb;
	}


	private Container makeContentPane() {
		Container cp = getContentPane(); 
		// 1. Encoding panel, containing scroll pane with text area
		JPanel encPanel = makeJPanel(
			null, "Encoding", new Rectangle(HM, VM, PANEL_W, ENC_PANEL_H) 
		);
		encPanel.add(makeJScrollPane(
			getEncodingTextArea(), new Rectangle(HM, 2*VM, PANEL_W - 2*HM, ENC_PANEL_H - 3*VM)), null);
		cp.add(encPanel);

		// 2. Tablature panel, containing scroll pane with text area; panel with radio buttons;
		// panel with checkbox; button
		JPanel tabPanel = makeJPanel(
			null, "Tablature", new Rectangle(HM, 2*VM + ENC_PANEL_H, PANEL_W, TAB_PANEL_H + 6*VM) 
		);
		tabPanel.add(makeJScrollPane(
			getTabTextArea(), new Rectangle(HM, 2*VM, PANEL_W - 2*HM, TAB_PANEL_H - 3*VM)), null);
		JPanel stylePanel = makeJPanel(
			null, "Tablature style", new Rectangle(HM, TAB_PANEL_H, PANEL_PART_W - HM, 5*VM)
		);
		for (AbstractButton b : Collections.list(getTabStyleButtonGroup().getElements())) {
			stylePanel.add(b, null);
		}
		tabPanel.add(stylePanel, null);
		JPanel rfPanel = makeJPanel(
			null, "Rhythm flags", new Rectangle(HM + PANEL_PART_W, TAB_PANEL_H, PANEL_PART_W - HM, 5*VM)
		);
		rfPanel.add(getRhythmFlagsCheckBox(), null);
		tabPanel.add(rfPanel, null);
		JButton viewButton = makeJButton(
			"View", new Rectangle(PANEL_W - BUTTON_W - HM, TAB_PANEL_H, BUTTON_W, BUTTON_H) 
		);
		tabPanel.add(viewButton);
		cp.add(tabPanel);

		return cp;
	}


	private JPanel makeJPanel(LayoutManager lm, String borderText, Rectangle bounds) {
		JPanel p = new JPanel();
		p.setLayout(lm);
		p.setBounds(bounds);
		p.setBorder(BorderFactory.createTitledBorder(borderText));
		return p;
	}


	private JScrollPane makeJScrollPane(JTextArea ta, Rectangle bounds) {
		JScrollPane sp = 
			new JScrollPane(ta, 
			ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sp.setBounds(bounds);
		return sp;
	}


	private JButton makeJButton(String t, Rectangle bounds) {
		JButton b = new JButton();
		b.setBounds(bounds);
		b.setText(t);
		b.addActionListener(defineActionListener(t, null));
		return b;
	}


	//////////////////////////////
	//
	//  G E T T E R S
	//  for instance variables 
	//
	private Highlighter getHighlighter() {
		return highlighter;
	}


	private JTextArea getEncodingTextArea() {
		return encodingTextArea; 
	}


	private JTextArea getTabTextArea() {
		return tabTextArea;
	}


	private ButtonGroup getTabStyleButtonGroup() {
		return tabStyleButtonGroup;
	}


	private JCheckBox getRhythmFlagsCheckBox() {
		return rhythmFlagsCheckBox; 
	}


	private JFileChooser getFileChooser() {
		return fileChooser;
	}


	private Map<String, String> getPaths() {
		return paths;
	}


	private File getFile() {
		return file;
	}


	private File getImportFile() {
		return importFile;
	}


	////////////////////////////////
	//
	//  C L A S S  M E T H O D S
	//
	/**
	 * Replaces, in the given String, all "\r\n"  with "\n".
	 * 
	 * @param s
	 * @return
	 */
	private static String handleReturns(String s) {
		s = s.replace("\r\n", "\n");
		return s;
	}


	/**
	 * Replaces, in the given String, all "\n" that are not preceded by "\r" with "\r\n".
	 * 
	 * @param s
	 * @return
	 */
	private static String handleReturnsAlt(String s) {
		// List all indices of the \n not preceded by \r
		List<Integer> indsOfLineBreaks = new ArrayList<Integer>(); 
		for (int i = 0; i < s.length(); i++) {
			String currChar = s.substring(i, i + 1);
			if (currChar.equals("\n")) {
				// NB: prevChar always exists as the char at index 0 in encoding will
				// never be a \n
				String prevChar = s.substring(i - 1, i);	
				if (!prevChar.equals("\r")) {
					indsOfLineBreaks.add(i);
				}
			}
		}
		// Replace all \n not preceded by \r in the encoding by \r\n and store the file
		for (int i = indsOfLineBreaks.size() - 1; i >= 0; i--) {
			int currInd = indsOfLineBreaks.get(i);
			s = s.substring(0, currInd) + "\r" + s.substring(currInd);
		}
		return s;
	}


	//////////////////////////////////////
	//
	//  I N S T A N C E  M E T H O D S
	//  actions 
	//
	private ActionListener defineActionListener(String actionStr, String ext) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (actionStr.equals("New")) {
					newFileAction();
				}
				else if (actionStr.equals("Open")) {
					openFileAction();
				}
				else if (actionStr.equals("Save")) {
					saveFileAction();
				}
				else if (actionStr.equals("Save as")) {
					saveAsFileAction();
				}
				else if (actionStr.equals("Import")) {
					importFileAction(ext);
				}
				else if (actionStr.equals("Export")) {
					exportFileAction(ext);
				}
				else if (actionStr.equals("Select all")) {
					selectAllEditAction();
				}
				else if (actionStr.equals("View")) {
					viewButtonAction();
				}
			}
		};
	}


	/**
	 * <code>file</code> = <code>null</code><br>
	 * <code>importFile</code> = <code>null</code><br> 
	 * title = "untitled.tbp"<br>
	 * <ul>
	 * <li>Save: redirects to Save as.</li> 
	 * <li>Save as: suggests to save in FileChooser dir (default dir) as untitled.tbp.</li>
	 * <li>Export:  suggests to save in FileChooser dir (default dir) as untitled.{@code<}ext{@code>}.</li>
	 *</ul>
	 */
	private void newFileAction() {
		StringBuilder sb = new StringBuilder();
		Arrays.stream(Encoding.METADATA_TAGS).forEach(t -> 
			sb.append(Encoding.OPEN_METADATA_BRACKET + t + ":" + Encoding.CLOSE_METADATA_BRACKET + "\n"));
		// Set JTextAreas content
		populateTextArea(sb.toString() + Symbol.END_BREAK_INDICATOR, getEncodingTextArea());
		populateTextArea("", getTabTextArea());
		// Set file, importFile, and title
		setFile(null);
		setImportFile(null);
		setTitle(TITLE[0] + TITLE[1] + TITLE[2]);
	}


	/**
	 * <code>file</code> = .../{@code<}opened_file{@code>}.tbp<br>
	 * <code>importFile</code> = <code>null</code><br> 
	 * title = "{@code<}opened_file{@code>}.tbp"<br>
	 * <ul>
	 * <li>Save: saves opened file.</li>
	 * <li>Save as: suggests to save in dir of opened file as {@code<}opened_file{@code>}.tbp.</li>
	 * <li>Export:  suggests to save in dir of opened file as {@code<}opened_file{@code>}.{@code<}ext{@code>}.</li>
	 * </ul>
	 */
	private void openFileAction() {
		openLike("Open", TBP + " (" + Encoding.TBP_EXT + ")", Encoding.TBP_EXT, null);
	}


	private void openLike(String dialogTitle, String fileDescr, String fileExt, String importType) {
		JFileChooser fc = getFileChooser();
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.setDialogTitle(dialogTitle);
		// Set file filter and suggested file name (empty string also removes any previous selection)
		fc.setFileFilter(new FileNameExtensionFilter(fileDescr, fileExt.substring(1)));
		fc.setSelectedFile(new File(""));
		// If dialog is confirmed
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			// Get content of f
			StringBuilder sb = new StringBuilder();
			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line).append("\r\n");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			String fileContent = sb.toString();
			// Make content to set in encodingTextArea
			String content = null; 
			// Open case
			if (importType == null) {
				content = fileContent;
			}
			// Import case
			else {
				if (importType.equals(ASCII)) {
					content = TabImport.ascii2tbp(fileContent);
				}
				else if (importType.equals(TC)) {
					content = TabImport.tc2tbp(fileContent);
				}
			}
			// Set JTextAreas content
			populateTextArea(handleReturns(content), getEncodingTextArea());
			populateTextArea("", getTabTextArea());
			// Set file, importFile, and title
			setFile(importType == null ? f : null);
			setImportFile(importType == null ? null : f);
			setTitle(FilenameUtils.removeExtension(f.getName()) + Encoding.TBP_EXT + TITLE[2]);
		}
	}


	private void saveFileAction() {
		if (checkEncoding() == null) {
			// Newly created or imported file
			if (getFile() == null) {
				saveAsFileAction();
			}
			// Opened file
			else {
				ToolBox.storeTextFile(handleReturns(getEncodingTextArea().getText()), getFile());
			}
		}
	}


	private void saveAsFileAction() {
		String extStr = EXTENSIONS.get(Encoding.TBP_EXT);
		saveAsLike("Save as", extStr + " (" + Encoding.TBP_EXT + ")", Encoding.TBP_EXT, null);
	}


	private void saveAsLike(String dialogTitle, String fileDescr, String fileExt, String exportType) {
		if (checkEncoding() == null) {
			JFileChooser fc = getFileChooser();
			fc.setDialogType(JFileChooser.SAVE_DIALOG);
			fc.setDialogTitle(dialogTitle);
			// Set file filter and suggested file name 
			fc.setFileFilter(new FileNameExtensionFilter(fileDescr, fileExt.substring(1)));
			File opf = getFile();
			File imf = getImportFile();
			File sf;
			// Called from file created with New
			if (opf == null && imf == null) {
				sf = new File(
					fc.getCurrentDirectory() + File.separator + "untitled" + fileExt
				);
			}
			// Called from file created with Open
			else if (opf != null && imf == null) {
				sf = 
					exportType == null ? opf :
					new File(FilenameUtils.removeExtension(opf.getAbsolutePath()) + fileExt);
			}
			// Called from file created with Import
			else {
				sf = new File(
					FilenameUtils.removeExtension(imf.getAbsolutePath()) + fileExt
				);
			}
			fc.setSelectedFile(sf);
			// If dialog is confirmed
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				File f = fc.getSelectedFile();
				// Get content of encodingTextArea
				// NB: returns entered directly in encodingTextArea are \n, but are saved as \r\n (applies to 
				// Save as-case only); returns in file content opened into encodingTextArea are always \r\n
				String etaContent = handleReturns(getEncodingTextArea().getText()); 
				// Make content to save as/export
				String content = null;
				// Save as-case
				if (exportType == null) {
					if (opf == null) {
						setFile(f);
						setTitle(f.getName() + TITLE[2]);
					}
					content = etaContent;
				}
				// Export case
				else {
					String fname = ToolBox.splitExt(f.getName())[0];
					Encoding e = new Encoding(
						etaContent, fname, Stage.RULES_CHECKED
					);
//					Encoding e = new Encoding(
//						etaContent, FilenameUtils.getBaseName(getFile().getName()), Stage.RULES_CHECKED
//					);
					if (exportType.equals(ASCII)) {
						content = makeASCIITab(e);
					}
					else if (exportType.equals(MEI)) {
						// Make transParams, containing only those params that MEIExport needs
						Map<String, String> transParams = new LinkedHashMap<String, String>();
						transParams.put(CLInterface.TUNING, CLInterface.INPUT);
						transParams.put(CLInterface.TABLATURE, "y");
						transParams.put(CLInterface.TYPE, CLInterface.INPUT);
						content = MEIExport.exportMEIFile(
							null, new Tablature(e, false), null, false, false, getPaths(),
							transParams, new String[]{null, "abtab -- converter"}
						);
					}
				}
				// Save as/export
				ToolBox.storeTextFile(content, f);
			}
		}		
	}


	/**
	 * <code>file</code> = <code>null</code><br>
	 * <code>importFile</code> = .../{@code<}imported_file{@code>}.{@code<}ext{@code>}<br>
	 * title = "{@code<}imported_file{@code>}.tbp"<br>
	 * <ul>
	 * <li>Save: redirects to Save as.</li>
	 * <li>Save as: suggests to save in dir of imported file as {@code<}imported_file{@code>}.tbp.</li>
	 * <li>Export:  suggests to save in dir of imported file as {@code<}imported_file{@code>}.{@code<}ext{@code>}.</li>
	 * </ul>
	 * 
	 * @param extension
	 */
	private void importFileAction(String extension) {
		String extStr = EXTENSIONS.get(extension);
		openLike("Import", extStr + " (" + extension + ")", extension, extStr);
	}


	private void exportFileAction(String extension) {
		String extStr = EXTENSIONS.get(extension);
		saveAsLike("Export", extStr + " (" + extension + ")", extension, extStr);
	}


	private void selectAllEditAction() {
		getEncodingTextArea().requestFocus();
		getEncodingTextArea().selectAll();
	}


	private void viewButtonAction() {
		getHighlighter().removeAllHighlights();
		if (checkEncoding() == null) {
			populateTextArea(makeASCIITab(new Encoding(
				handleReturns(getEncodingTextArea().getText()), "", Stage.RULES_CHECKED)), 
				getTabTextArea()
			);
		}
	}


	//////////////////////////////////////
	//
	//  I N S T A N C E  M E T H O D S
	//  other
	//
	private void populateTextArea(String content, JTextArea ta) { 
		ta.setText(content);
		ta.setCaretPosition(0);
	}


	private String[] checkEncoding() {
		String rawEnc = handleReturns(getEncodingTextArea().getText());
		String[] check = Encoding.checkEncoding(rawEnc);
		if (check != null) {
			createHighlight(check);
			createErrorDialog(check[2] + "\n" + check[3]);
		}
		return check; 
	}


	private void createHighlight(String[] errs) {
		int hilitStartInd = Integer.parseInt(errs[0]);
		int hilitEndInd = Integer.parseInt(errs[1]);
		HighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
		try {
			getHighlighter().addHighlight(hilitStartInd, hilitEndInd, p);
		} catch (BadLocationException e) {  
			System.err.println("BadLocationException: " + e.getMessage());
		}
	}


	private void createErrorDialog(String t) {
		JDialog dialog = 
			new JOptionPane(t, JOptionPane.ERROR_MESSAGE).createDialog("Error");
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
	}


	private String makeASCIITab(Encoding e) {
		TabSymbolSet tss = null;
		for (AbstractButton b : Collections.list(getTabStyleButtonGroup().getElements())) {
			if (b.isSelected()) {
				tss = TabSymbolSet.getTabSymbolSet(null, b.getText());
				break;
			}
		}
		return e.visualise(tss, getRhythmFlagsCheckBox().isSelected(), true, true);		
	}


//	private void initFromWWW() {
//		// https://www.codespeedy.com/how-to-add-multiple-panels-in-jframe-in-java/
//        setTitle("JPANEL CREATION");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setLayout(null);
//        //setting the bounds for the JFrame
//        setBounds(100, 100, 645, 470);
//        Border br = BorderFactory.createLineBorder(Color.black);
//        Container c=getContentPane();
//        //Creating a JPanel for the JFrame
//        JPanel panel=new JPanel();
//        JPanel panel2=new JPanel();
//        JPanel panel3=new JPanel();
//        JPanel panel4=new JPanel();
//        //setting the panel layout as null
//        panel.setLayout(null);
//        panel2.setLayout(null);
//        panel3.setLayout(null);
//        panel4.setLayout(null);
//        //adding a label element to the panel
//        JLabel label=new JLabel("Panel 1");
//        JLabel label2=new JLabel("Panel 2");
//        JLabel label3=new JLabel("Panel 3");
//        JLabel label4=new JLabel("Panel 4");
//        label.setBounds(120,50,200,50);
//        label2.setBounds(120,50,200,50);
//        label3.setBounds(120,50,200,50);
//        label4.setBounds(120,50,200,50);
//        panel.add(label);
//        panel2.add(label2);
//        panel3.add(label3);
//        panel4.add(label4);
//        // changing the background color of the panel to yellow
//        //Panel 1
//        panel.setBackground(Color.yellow);
//        panel.setBounds(10,10,300,200);
//        //Panel 2
//        panel2.setBackground(Color.red);
//        panel2.setBounds(320,10,300,200);
//        //Panel 3
//        panel3.setBackground(Color.green);
//        panel3.setBounds(10,220,300,200);
//        //Panel 4
//        panel4.setBackground(Color.cyan);
//        panel4.setBounds(320,220,300,200);
//        
//        // Panel border
//        panel.setBorder(br);
//        panel2.setBorder(br);
//        panel3.setBorder(br);
//        panel4.setBorder(br);
//        
//        //adding the panel to the Container of the JFrame
//        c.add(panel);
//        c.add(panel2);
//        c.add(panel3);
//        c.add(panel4);
//       
//        setVisible(true);
//	}
//
//
//	private void initOLD() {
////		setHighlighter();
//////		setPieceLabel();
////		setTabTypeButtonGroup();
////		setUpperErrorLabel();
////		setLowerErrorLabel();
////		setEncodingTextArea();
////		setTabTextArea("");
////		setRhythmSymbolsCheckBox();
////		setViewButton();
////		setMenuBar();
////		setEditorPanel();
////		//
////		setJMenuBar(getEditorMenuBar());
////		setContentPane(getEditorPanel());
//////		getContentPane().add(getEditorPanel());
////		setEditorFileChooser();
////		setEditorFile(null);
////		setSize(FRAME_DIMS[0], FRAME_DIMS[1]);
////		setVisible(true);
////		setTitle(TITLE[0] + TITLE[1] + TITLE[2]);
////		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//	}
//
//
//	private void setMenuBar() {
////		menuBar = makeMenuBar();
//	}
//
//
//	private JMenuBar getMenuBarThis() {
//		return null; //menuBar;
//	}
//
//
//	private void setViewButton() {		
////		viewButton = makeJButton(
////			new Rectangle(PANEL_W/*TEXT_AREA_W*/ - BUTTON_W - MARGIN, PANEL_H, BUTTON_W, BUTTON_H), 
////			"View"
////		);
////		viewButton.addActionListener(new ActionListener() {
////			@Override
////			public void actionPerformed(ActionEvent e) {
////				viewButtonAction();
////			}
////		});
//	}
//
//
//	private JButton getViewButton() {
//		return null; //viewButton;
//	}
//
//
//	private void setUpperErrorLabel() {
//		JLabel l = new JLabel();
//		l.setBounds(new Rectangle(99, 69, 592, 16));
//		l.setForeground(Color.RED);
////		upperErrorLabel = l;
//	}
//
//
//	private void setLowerErrorLabel() {
//		JLabel l = new JLabel();
//		l.setBounds(new Rectangle(99, 88, 592, 16));
//		l.setForeground(Color.RED);
////		lowerErrorLabel = l;
//	}
//
//
//	private void setTabTextArea(String content) {
////		tabTextArea = makeTextArea(false, content);
//	}
//
//
//	private JTextArea makeTextArea(boolean encoding, String content) {
//		JTextArea ta = new JTextArea();
//		// These bounds are overridden by those of the scrollpanes in the JPanels
////		ta.setBounds(encoding ? 
////			new Rectangle(MARGIN, 105, 571, 76) : 
////			new Rectangle(MARGIN + 571 + MARGIN, 105, 571, 76));
////		ta.setBounds(encodingFrame ? new Rectangle(15, 105, 571, 76) : new Rectangle(15, 240, 571, 136));
//		ta.setLineWrap(true); // necessary because of JScrollPane
//		ta.setEditable(true);
//		ta.setFont(FONT);
//		ta.setText(content);
////		ta.setHighlighter(getHighlighter());
//		ta.setHighlighter(encoding ? getHighlighter() : null); // highlighter also makes it copyable
//		return ta;
//	}
//
//
//	private void setEditorPanel() {
////		editorPanel = makeEditorPanel();
//	}
//
//
//	private JPanel makeEditorPanel() {
//		int PANEL_H = 200;
//		JPanel p = new JPanel();
//		p.setLayout(null);
//		p.setBorder(BorderFactory.createTitledBorder("Encoding"));
//		p.setSize(new Dimension(PANEL_DIMS[0],
////			PANEL_DIMS[1]));
//			FRAME_DIMS[1]/2));
//
////		JLabel l = new JLabel("Piece:");
////		l.setBounds(new Rectangle(15, 15, 81, 16));
////		p.add(l, null);
////		p.add(getPieceLabel());
//		//
//		JLabel l = new JLabel("Style:");
//		l.setBounds(new Rectangle(
//			HM, 
//			VM + PANEL_H + VM, 
////			MARGIN + ENCODING_TEXT_AREA_H + MARGIN, 
//			LABEL_W, 
//			LABEL_H)
//		);
//		p.add(l, null);
////		l.setBounds(new Rectangle(PANEL_MARGIN, 559, 81, 16));
////		l.setBounds(new Rectangle(15, 15, 81, 16));
////		l.setBounds(new Rectangle(15, 42, 81, 16));
//		for (AbstractButton b : Collections.list(getTabStyleButtonGroup().getElements())) {
//			p.add(b, null);
//		}
//		//
//		l = new JLabel("Error:");
//		l.setBounds(new Rectangle(
//			HM, 
//			69, 
//			LABEL_W, 
//			LABEL_H)
//		);
//		p.add(l, null);
////		p.add(getUpperErrorLabel(), null);
////		p.add(getLowerErrorLabel(), null);
//		//
//		p.add(getRhythmFlagsCheckBox(), null);
//		//
////		p.add(getViewButton(), null);
//
//		JScrollPane sp = 
//			new JScrollPane(getEncodingTextArea(), 
//			ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
//			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		sp.setBounds(new Rectangle(
//			HM, 
//			VM, 
//			PANEL_W/*TEXT_AREA_W*/, 
//			PANEL_H
////			ENCODING_TEXT_AREA_H
//		));
////		sp.setBounds(new Rectangle(15, 115, 850, 430));
//		p.add(sp, null);
//		sp = 
//			new JScrollPane(getTabTextArea(), 
//			ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
//			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//		sp.setBounds(new Rectangle(
//			HM, 
//			VM + PANEL_H /*ENCODING_TEXT_AREA_H*/ + VM + BUTTON_H + VM, 
//			PANEL_W/*TEXT_AREA_W*/,
//			PANEL_H
////			TAB_TEXT_AREA_H
//		));
////		sp.setBounds(new Rectangle(15 + 15 + 650, 115, 650, 430));
//		p.add(sp, null);
//
//		return p;
//	}
//
//
//	private void openFileActionOLD() {
//		populateTextArea("", getTabTextArea());
//		getFileChooser().setDialogType(JFileChooser.OPEN_DIALOG);
//		getFileChooser().setDialogTitle("Open");
//		// Set file type filter
//		getFileChooser().setFileFilter(new FileNameExtensionFilter(TBP + " (.tbp)", 
//			Encoding.EXTENSION.substring(1)));
//		// Remove any previous selection
//		getFileChooser().setSelectedFile(new File(""));
//		if (getFileChooser().showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
//			File f = null;
//			try {
//				f = getFileChooser().getSelectedFile();
//				BufferedReader br = new BufferedReader(new FileReader(getFileChooser().getSelectedFile()));						
//			} catch (IOException e) {
//				// 11:11
//				// https://www.youtube.com/watch?v=Z8p_BtqPk78
//				e.printStackTrace();
//			}
//			setFile(f);
//			setTitle(f.getName() + " - " + "tab+Editor");
////			fileName = f.getName();
////			String rawEnc = ToolBox.readTextFile(encFile);
//			String rawEncoding = "";
//			try {
//				rawEncoding = ToolBox.readTextFile(f);
//				rawEncoding = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//			populateTextArea(rawEncoding, getEncodingTextArea());
////			getEncodingTextArea().setText(rawEncoding);
////			getEncodingTextArea().setCaretPosition(0);
//		}
//
//		boolean doThis = false;
//		if (doThis) {
//			String encPath = null; // was method arg
//			if (encPath == null) {
//				String prefix = "F:/research/data/annotated/encodings/";
//				encPath = prefix;
//			
//				// Test
////				encPath += "test/testpiece.tbp";
////				encPath += "test/test_get_meter_info.tbp";
////				encPath =  "F:/research/publications/conferences-workshops/2019-ISMIR/paper/tst/tab/3610_033_inter_natos_mulierum_morales_T-rev.tbp";
////				encPath =  "F:/research/publications/conferences-workshops/2019-ISMIR/paper/tst/tab/3618_041_benedictus_from_missa_de_l_homme_arme_morales_T.tbp";
////				encPath =  "F:/research/projects/byrd/tst/il_me_souffit-short.tbp";
////				encPath =  "F:/research/projects/byrd/tst/pleni.tbp";
////				encPath =  "C:/Users/Reinier/Desktop/test-capirola/tab/capirola-1520-et_in_terra_pax.tbp";
//			
//				// Need to be double-checked
////				encPath += "newsidler-1536_6-mein_einigs_a.tbp";
////				encPath += "Newsidler 1536 - Mein hertz alzeyt hat gross verlangen.tbp";
////				encPath += "Ochsenkun 1558 - Cum Sancto spiritu.tbp";  	
////				encPath += "Barbetta 1582 - Martin menoit.tbp";
////				encPath += "Da Crema 1546 - Il nest plaisir.tbp";
////				encPath += "De Narbaez 1538 - MIlle regres.tbp";
////				encPath += "Heckel 1562 - Il est vne Fillete. [Tenor].tbp";
////				encPath += "Heckel 1562 - Il estoit vne fillete. Discant.tbp";
////				encPath += "Morlaye 1552 - LAs on peut iuger.tbp";
////				encPath += "Newsidler 1544 - Der hupff auf.tbp";
////				encPath += "Newsidler 1544 - Hie volget die Schlacht vor Bafia. Der Erst Teyl.tbp";
////				encPath += "Newsidler 1544 - Hie volget die Schlacht vor Bafia. Der ander Teyl der schlacht.tbp";
////				encPath += "Newsidler 1544 - Sula Bataglia.tbp";
////				encPath += "Ochsenkun 1558 - Benedicta es coelorum, Prima pars.tbp";
////				encPath += "Ochsenkun 1558 - Gott alls in allem wesentlich.tbp";
////				encPath += "Ochsenkun 1558 - Pater Noster, Prima pars.tbp"; 
////				encPath += "Ochsenkun 1558 - Praeter rerum seriem, Prima pars.tbp";
////				encPath += "Ochsenkun 1558 - Stabat mater dolorosa, Prima pars.tbp";
////				encPath += "Phalese 1546 - Martin menuyt de Iennequin.tbp";
////				encPath += "Spinacino 1507 - LA Bernardina de Iosquin.tbp";
//			
//				// Checked and ready for processing
//				// 3vv
////				encPath += "thesis-int/3vv/" + "newsidler-1536_7-disant_adiu.tbp";
////				encPath += "thesis-int/3vv/" + "newsidler-1536_7-mess_pensees.tbp";
////				encPath += "thesis-int/3vv/" + "pisador-1552_7-pleni_de.tbp";
////				encPath += "thesis-int/3vv/" + "judenkuenig-1523_2-elslein_liebes.tbp";
////				encPath += "thesis-int/3vv/" + "newsidler-1544_2-nun_volget.tbp";
//				encPath += "thesis-int/3vv/" + "phalese-1547_7-tant_que-3vv.tbp";
//
//				// 4vv
////				encPath += "thesis-int/4vv/" + "ochsenkun-1558_5-absolon_fili.tbp";
////				encPath += "thesis-int/4vv/" + "ochsenkun-1558_5-in_exitu.tbp";
////				encPath += "thesis-int/4vv/" + "ochsenkun-1558_5-qui_habitat.tbp";
////				encPath += "thesis-int/4vv/" + "rotta-1546_15-bramo_morir.tbp";
////				encPath += "thesis-int/4vv/" + "phalese-1547_7-tant_que-4vv.tbp";
////				encPath += "thesis-int/4vv/" + "ochsenkun-1558_5-herr_gott.tbp";
////				encPath += "thesis-int/4vv/" + "abondante-1548_1-mais_mamignone.tbp";
////				encPath += "thesis-int/4vv/" + "phalese-1563_12-las_on.tbp";
////				encPath += "thesis-int/4vv/" + "barbetta-1582_1-il_nest.tbp";
////				encPath += "thesis-int/4vv/" + "barbetta-1582_1-il_nest-corrected.tbp";
////				encPath += "thesis-int/4vv/" + "phalese-1563_12-il_estoit.tbp";
////				encPath += "thesis-int/4vv/" + "BSB-mus.ms._272-mille_regres.tbp";
//
//				// 5vv
////				encPath += "thesis-int/5vv/" + "adriansen-1584_6-d_vn_si.tbp";
////				encPath += "thesis-int/5vv/" + "ochsenkun-1558_5-inuiolata_integra.tbp";
//				
//				// Byrd
////				encPath += "byrd-int/4vv/ah_golden_hairs-NEW.tbp";
//
//				// JosquIntab
////				encPath = "F:/research/data/annotated/josquintab/tab/" + "5256_05_inviolata_integra_desprez-2.tbp";
////				encPath = "F:/research/data/annotated/josquintab/tab/" + "5263_12_in_exitu_israel_de_egipto_desprez-3.tbp";
////				encPath = "F:/research/data/annotated/josquintab/tab/" + "4465_33-34_memor_esto-2XXX.tbp";
//			}
//		
//			File encFile = new File(encPath);
////			setFile(encFile);
//		
////			getPieceLabel().setText(encFile.getName());
//			String rawEncoding = "";
//			try {
//				rawEncoding = new String(Files.readAllBytes(Paths.get(encFile.getAbsolutePath())));
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
//			getEncodingTextArea().setText(rawEncoding);
//		}
//	}
//	
//	
//	private void openFileActionOLDD() {
//		JFileChooser fc = getFileChooser();
//		fc.setDialogType(JFileChooser.OPEN_DIALOG);
//		fc.setDialogTitle("Open");
//		// Set file filter and suggested file name (empty string = remove any previous selection)
//		fc.setFileFilter(new FileNameExtensionFilter(TBP + " (" + Encoding.EXTENSION + ")", 
//			Encoding.EXTENSION.substring(1)));
//		fc.setSelectedFile(new File(""));
//		// If dialog is confirmed
//		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
//			File f = fc.getSelectedFile();
//			StringBuilder sb = new StringBuilder();
//			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
//				String line;
//				while ((line = br.readLine()) != null) {
//					sb.append(line).append("\r\n");
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			//
//			populateTextArea(sb.toString(), getEncodingTextArea());
//			populateTextArea("", getTabTextArea());
//			//
//			setFile(f);
//			setTitle(f.getName() + " - " + "tab+Editor");
//		}
//	}
//
//
//	private void saveAsFileActionOLD() {
//		JFileChooser fc = getFileChooser();
//		fc.setDialogType(JFileChooser.SAVE_DIALOG);
//		fc.setDialogTitle("Save as");
//		// Set file filter and suggested file name
//		fc.setFileFilter(new FileNameExtensionFilter(TBP + " (" + Encoding.EXTENSION + ")", 
//			Encoding.EXTENSION.substring(1)));
//		fc.setSelectedFile(getFile() == null ? new File("untitled" + Encoding.EXTENSION) : getFile());
//		// If dialog is confirmed
//		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
//			File f = fc.getSelectedFile();			
//			if (getFile() == null) {
//				setFile(f);
//				setTitle(f.getName());
//			}
//			// Returns entered directly in encodingTextArea will be \n, but will be saved as \r\n;
//			// returns in a file opened into encodingTextArea will always be \r\n -- so strictly
//			// speaking, handleReturns() is not needed
//			ToolBox.storeTextFile(handleReturns(getEncodingTextArea().getText()), f);
//		}
//	}
//
//
//	private void importFileActionOLDD(String extension) {
//		Map<String, String> extensions = new LinkedHashMap<String, String>();
//		extensions.put(ASCII_EXTENSION, ASCII);
//		extensions.put(TC_EXTENSION, TC);
//		extensions.put(MEI_EXTENSION, MEI);
//
//		JFileChooser fc = getFileChooser();
//		fc.setDialogType(JFileChooser.OPEN_DIALOG);
//		fc.setDialogTitle("Import");
//		// Set file filter and suggested file name (empty string = remove any previous selection)
//		fc.setFileFilter(new FileNameExtensionFilter(extensions.get(extension) + 
//			" (" + extension + ")", extension.substring(1)));
//		fc.setSelectedFile(new File(""));
//		// If dialog is confirmed
//		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
//			File f = fc.getSelectedFile();
//			StringBuilder sb = new StringBuilder();
//			try (BufferedReader br = new BufferedReader(new FileReader(f))) {
//				String line;
//				while ((line = br.readLine()) != null) {
//					sb.append(line).append("\r\n");
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			//
//			populateTextArea(TabImport.ascii2tbp(sb.toString()), getEncodingTextArea());
//			populateTextArea("", getTabTextArea());
//			//
//			File fTbp = new File(FilenameUtils.getBaseName(f.getName()) + Encoding.EXTENSION);
//			setFile(fTbp);
//			setTitle(fTbp.getName() + " - " + "tab+Editor");
//		}
//	}
//
//
//	private void exportFileActionOLD(String extension) {
//		Map<String, String> extensions = new LinkedHashMap<String, String>();
//		extensions.put(ASCII_EXTENSION, ASCII);
//		extensions.put(TC_EXTENSION, TC);
//		extensions.put(MEI_EXTENSION, MEI);
//		extensions.put(".mei", MEI);
//		
//		JFileChooser fc = getFileChooser();
//		fc.setDialogType(JFileChooser.SAVE_DIALOG);
//		fc.setDialogTitle("Export");
//		// Set file filter and suggested file name
//		fc.setFileFilter(new FileNameExtensionFilter(extensions.get(extension) + " (" + extension + ")", extension.substring(1)));
////		if (extension.equals(".mei") || extension.equals(MEI_EXTENSION)) {
////			fc.setFileFilter(new FileNameExtensionFilter("MEI (.mei, " + MEI_EXTENSION + ")", extension.substring(1)));
////		}
////		else if (extension.equals(ASCII_EXTENSION)) {
////			fc.setFileFilter(new FileNameExtensionFilter("ASCII (" + ASCII_EXTENSION + ")", extension.substring(1)));
////		}
//		fc.setSelectedFile(new File(FilenameUtils.getBaseName(getFile().getName()) + extension));
////		fc.setSelectedFile(new File(getFile().getAbsolutePath().replace(Encoding.EXTENSION, extension)));
//		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
//			File f = fc.getSelectedFile();			
//
//		}
//	}
//
//
//	private void viewButtonActionOLD() {
//		final int firstErrorCharIndex = 0;
//		final int lastErrorCharIndex = 1;
//		final int errorStringIndex = 2;
//		final int ruleStringIndex = 3;
//		
//		String rawEnc = getEncodingTextArea().getText();
//
//		// 1. Create an unchecked encoding
//		// Every time the viewbutton is clicked, a new rawEnc is made. The first time the 
//		// viewbutton is clicked, encodingArea.getText() will always be exactly as in the 
//		// file that is loaded because it is set as such in openFileAction(). Any next time,
//		// it will be exactly what is in the encodingArea (which now may have corrections 
//		// compared to what is in the loaded file)
////		Encoding enc = new Encoding(rawEnc, "", Stage.MINIMAL);
//		// a. If the encoding contains metadata errors: place error message
//		if (Encoding.checkMetadata(rawEnc) != null) {
////			getUpperErrorLabel().setText(METADATA_ERROR);
////			getLowerErrorLabel().setText("");
////			getErrorMessageLabel("upper").setText(Encoding.METADATA_ERROR);
////			getErrorMessageLabel("lower").setText("");
//			JOptionPane optionPane = 
//				new JOptionPane("METADATA ERROR -- Check for missing or misplaced curly brackets." + "\n" + "", JOptionPane.ERROR_MESSAGE);
////				JOptionPane optionPane = new JOptionPane("ErrorMsg", JOptionPane.ERROR_MESSAGE);
//			JDialog dialog = optionPane.createDialog("ERROR");
//			dialog.setAlwaysOnTop(true);
//			dialog.setVisible(true);
//		}
//		// b. If the encoding contains no metadata errors: continue
//		else {
//			Encoding enc = new Encoding(rawEnc, "", Stage.COMMENTS_AND_METADATA_CHECKED);
//			String cleanEnc = enc.getCleanEncoding();
//			// 2. Check the encoding
//			// Remove any remaining highlights and error messages
//			getHighlighter().removeAllHighlights();
////			getUpperErrorLabel().setText("(none)");
////			getLowerErrorLabel().setText(null);
////			getErrorMessageLabel("upper").setText("(none)");
////			getErrorMessageLabel("lower").setText(null);
//			// a. If the encoding contains encoding errors: place error messages and highlight
//			String[] encErrs = Encoding.checkEncodingOLD(rawEnc, cleanEnc, enc.getTabSymbolSet());
//			if (encErrs != null) {
////				getUpperErrorLabel().setText(encErrs[errorStringIndex]);
////				getLowerErrorLabel().setText(encErrs[ruleStringIndex]);
////				getErrorMessageLabel("upper").setText(encErrs[errorStringIndex]);
////				getErrorMessageLabel("lower").setText(encErrs[ruleStringIndex]);
//				int hilitStartIndex = Integer.parseInt(encErrs[firstErrorCharIndex]);
//				int hilitEndIndex = Integer.parseInt(encErrs[lastErrorCharIndex]);
//				Highlighter.HighlightPainter painter = 
//					new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
//				try {
//					getHighlighter().addHighlight(hilitStartIndex, hilitEndIndex, painter);
//				} catch (BadLocationException e) {  
//					System.err.println("BadLocationException: " + e.getMessage());
//				}
//				// https://stackoverflow.com/questions/8852560/how-to-make-popup-window-in-java
//				JOptionPane optionPane = 
//					new JOptionPane(encErrs[errorStringIndex] + "\n" + encErrs[ruleStringIndex], 
//					JOptionPane.ERROR_MESSAGE);
////				JOptionPane optionPane = new JOptionPane("ErrorMsg", JOptionPane.ERROR_MESSAGE);
//				JDialog dialog = optionPane.createDialog("ERROR");
//				dialog.setAlwaysOnTop(true);
//				dialog.setVisible(true);
//			}
//			// b. If the encoding contains no encoding errors: show the tablature in a new window 
//			else {
//				enc = new Encoding(rawEnc, "", Stage.RULES_CHECKED);
////				List<String> types = new ArrayList<>();
////				Arrays.asList(TabSymbolSet.values()).forEach(tss -> {
////					if (!types.contains(tss.getType())) {
////						types.add(tss.getType());
////					}
////				});
//
//				// Determine TabSymbolSet
//				TabSymbolSet tss = null;
//				for (AbstractButton b : Collections.list(getTabStyleButtonGroup().getElements())) {
//					if (b.isSelected()) {
//						tss = TabSymbolSet.getTabSymbolSet(null, b.getText());
//						break;
//					}
//				}
////				outerLoop: for (String type : types) {
////					if (getTabRadioButton(type).isSelected()) {
////						for (TabSymbolSet t : TabSymbolSet.values()) {
////							if (t.getType().equals(type)) {
//////							if (t.getName().toLowerCase().startsWith(type)) {
////								tss = t;
////								break outerLoop;
////							}
////						}
////					}
////				}
//
////				getTabFrameTextArea().setText(enc.visualise(tss, 
////					getRhythmSymbolsCheckBox().isSelected(), true, true));
////				initializeTabViewer(encPath);
//				
//				populateTextArea(enc.visualise(tss, getRhythmFlagsCheckBox().isSelected(), true, true), getTabTextArea());
////				new Viewer(/*getFileName(Encoding.EXTENSION)*/getFile(), enc.visualise(tss, getRhythmSymbolsCheckBox().isSelected(), true, true), false);
//			} 
//		}
//	}
//
//
//	private void viewButtonActionOLDD() {
//		final int firstErrorCharIndex = 0;
//		final int lastErrorCharIndex = 1;
//		final int errorStringIndex = 2;
//		final int ruleStringIndex = 3;
//		
//		String rawEnc = getEncodingTextArea().getText();
//
//		// 1. Create an unchecked encoding
//		// Every time the viewbutton is clicked, a new rawEnc is made. The first time the 
//		// viewbutton is clicked, encodingArea.getText() will always be exactly as in the 
//		// file that is loaded because it is set as such in openFileAction(). Any next time,
//		// it will be exactly what is in the encodingArea (which now may have corrections 
//		// compared to what is in the loaded file)
////		Encoding enc = new Encoding(rawEnc, "", Stage.MINIMAL);
//		// a. If the encoding contains metadata errors: place error message
//		if (Encoding.checkMetadata(rawEnc) != null) {
//			JOptionPane optionPane = 
//				new JOptionPane("METADATA ERROR -- Check for missing or misplaced curly brackets." + "\n" + "", JOptionPane.ERROR_MESSAGE);
//			JDialog dialog = optionPane.createDialog("Error");
//			dialog.setAlwaysOnTop(true);
//			dialog.setVisible(true);
//		}
//		// b. If the encoding contains no metadata errors: continue
//		else {
//			Encoding enc = new Encoding(rawEnc, "", Stage.COMMENTS_AND_METADATA_CHECKED);
//			String cleanEnc = enc.getCleanEncoding();
//			// 2. Check the encoding
//			// Remove any remaining highlights and error messages
//			getHighlighter().removeAllHighlights();
//			// a. If the encoding contains encoding errors: place error messages and highlight
//			String[] encErrs = Encoding.checkEncodingOLD(rawEnc, cleanEnc, enc.getTabSymbolSet());
//			if (encErrs != null) {
//				int hilitStartIndex = Integer.parseInt(encErrs[firstErrorCharIndex]);
//				int hilitEndIndex = Integer.parseInt(encErrs[lastErrorCharIndex]);
//				HighlightPainter painter = 
//					new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
////				Highlighter.HighlightPainter painter = 
////					new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
//				try {
//					getHighlighter().addHighlight(hilitStartIndex, hilitEndIndex, painter);
//				} catch (BadLocationException e) {  
//					System.err.println("BadLocationException: " + e.getMessage());
//				}
//				JOptionPane optionPane = 
//					new JOptionPane(encErrs[errorStringIndex] + "\n" + encErrs[ruleStringIndex], 
//					JOptionPane.ERROR_MESSAGE);
//				JDialog dialog = optionPane.createDialog("Error");
//				dialog.setAlwaysOnTop(true);
//				dialog.setVisible(true);
//			}
//			// b. If the encoding contains no encoding errors: show the tablature in a new window 
//			else {
//				
//				enc = new Encoding(rawEnc, "", Stage.RULES_CHECKED);
//
//				// Determine TabSymbolSet
//				TabSymbolSet tss = null;
//				for (AbstractButton b : Collections.list(getTabStyleButtonGroup().getElements())) {
//					if (b.isSelected()) {
//						tss = TabSymbolSet.getTabSymbolSet(null, b.getText());
//						break;
//					}
//				}
//				
//				populateTextArea(enc.visualise(
//					tss, getRhythmFlagsCheckBox().isSelected(), true, true), getTabTextArea());
//			} 
//		}
//	}
//
//
//	private boolean checkEncodingOLD() {
//		final int firstErrCharInd = 0;
//		final int lastErrCharInd = 1;
//		final int errStrInd = 2;
//		final int ruleStrInd = 3;
//
//		String rawEnc = handleReturns(getEncodingTextArea().getText());
//		// 0. Check for content
//		if (rawEnc.equals("")) {
//			createErrorDialog("No encoding.");
//			return false;
//		}
//		// 1. Create an unchecked encoding
////		Encoding enc = new Encoding(rawEnc, "", Stage.MINIMAL);
//		// a. If the encoding contains metadata errors: give error message
//		if (Encoding.checkMetadata(rawEnc) != null) {
//			createErrorDialog("METADATA ERROR -- Check for missing or misplaced curly brackets." + "\n" + "");
//			return false;
//		}
//		// b. If the encoding contains no metadata errors
//		else {
//			Encoding enc = new Encoding(rawEnc, "", Stage.COMMENTS_AND_METADATA_CHECKED);
//			String cleanEnc = enc.getCleanEncoding();
//			// 2. Check the encoding
//			// Remove any remaining highlights and error messages
//			getHighlighter().removeAllHighlights();
//			// a. If the encoding contains encoding errors: give error messages and highlight
//			String[] encErrs = Encoding.checkEncodingOLD(rawEnc, cleanEnc, enc.getTabSymbolSet());
//			if (encErrs != null) {
//				int hilitStartInd = Integer.parseInt(encErrs[firstErrCharInd]);
//				int hilitEndInd = Integer.parseInt(encErrs[lastErrCharInd]);
//				HighlightPainter painter = 
//					new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
//				try {
//					getHighlighter().addHighlight(hilitStartInd, hilitEndInd, painter);
//				} catch (BadLocationException e) {  
//					System.err.println("BadLocationException: " + e.getMessage());
//				}
//				createErrorDialog(encErrs[errStrInd] + "\n" + encErrs[ruleStrInd]);
//				return false;
//			}
//			// b. If the encoding contains no encoding errors
//			else {
//				return true;
//			} 
//		}
//	}
//
//
////	private JLabel getUpperErrorLabel() {
////		return upperErrorLabel;
////	}
//
//
////	private JLabel getLowerErrorLabel() {
////		return lowerErrorLabel;
////	}
//
//
////	private JPanel getEditorPanel() {
////		return editorPanel;
////	}

}