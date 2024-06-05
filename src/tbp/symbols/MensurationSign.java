package tbp.symbols;

import java.util.Arrays;

/**
 * @author Reinier de Valk
 * @version 11.04.2023 (last well-formedness check)
 */
public class MensurationSign extends Symbol {
	public static final int DEFAULT_STAFFLINE = 3; 

	private Integer[] meter;
	private String meterString;
	private int staffLine;


	///////////////////////////////
	//
	//  C O N S T R U C T O R S
	//
	public MensurationSign() {
	}


	public MensurationSign(String e, String s, Integer[] m) {
		// The basic type (M2, M3, ..., MC\) has beat unit 4 and staffline 3, and is encoded as  
		// M<n> or M<n>\ (cut MS), where <n> is a number or a symbol
		// A variant type may have a different beat unit or staffline, and is encoded as 
		// M<n>:<b><l> or M<n>\\:<b><l> (cut MS), where <b> is the beat unit and <l> the staffline
		init(e, s, m);
	}


	private void init(String e, String s, Integer[] m) {
		setEncoding(e);
		setSymbol(s);
		setMeter(m);
		setMeterString();
		setStaffLine();		
	}


	//////////////////////////////
	//
	//  S E T T E R S
	//  for instance variables
	//
	void setMeter(Integer[] m) {
		meter = m;
	}


	void setMeterString() {
		meterString = getMeter()[0] + "/" + getMeter()[1];
	}


	void setStaffLine() {
		staffLine = makeStaffLine();
	}


	// TESTED
	int makeStaffLine() {
		String e = getEncoding();
		// Basic M<n> or M<n>\ type
		if ((!e.contains("\\") && e.length() == 2) || e.endsWith("\\")) {
			return DEFAULT_STAFFLINE;
		}
		// Variant type
		else {
			// Default beat
			if (!e.contains(":")) {
				return Integer.parseInt(e.substring(e.length()-1));
			}
			// Non-default beat
			else {
				String end =  ":" + String.valueOf(getMeter()[1]);
				return 
					e.endsWith(end) ? DEFAULT_STAFFLINE : 
					Integer.parseInt(e.substring(e.indexOf(end) + end.length()));
			}
		}
	}


	//////////////////////////////
	//
	//  G E T T E R S
	//  for instance variables
	//
	public Integer[] getMeter() {
		return meter;  
	}


	public String getMeterString() {
		return meterString;
	}


	public int getStaffLine() {
		return staffLine;  
	}


	//////////////////////////////////////
	//
	//  I N S T A N C E  M E T H O D S 
	//
	/**
	 * Makes a variant (beat unit, staff line, or combined) of the MS.
	 * 
	 * @param beatUnit
	 * @param staffLine
	 * @return
	 */
	// TESTED
	public MensurationSign makeVariant(int beatUnit, int staffLine) {
		String e = getEncoding();
		Integer[] m = getMeter();
		Integer[] newM = Arrays.copyOf(m, m.length);
		if (beatUnit != -1) {
			newM[1] = beatUnit;
		}
		if (beatUnit != -1) {
			e += ":" + String.valueOf(beatUnit);
		}
		if (staffLine != -1) {
			e += String.valueOf(staffLine);
		}
		return new MensurationSign(e, getSymbol(), newM);
	}


	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof MensurationSign)) {
			return false;
		}
		MensurationSign m = (MensurationSign) o;
		return 
			getEncoding().equals(m.getEncoding()) &&
			getSymbol().equals(m.getSymbol()) &&
			Arrays.equals(getMeter(), m.getMeter()) &&
			getMeterString().equals(m.getMeterString()) &&
			getStaffLine() == m.getStaffLine();
	}
}