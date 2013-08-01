/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author George Chlipala
 *
 */
public abstract class CyanosObject {

	protected String myID = null;
	protected Map<String,String> protocol = null;

	protected static final String[] ALPHABET = {"","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};

	protected final static Pattern MIN_PATTERN = Pattern.compile("([\\d\\.]+)\\s*[mM].*");
	protected final static Pattern SEC_PATTERN = Pattern.compile("([\\d\\.]+)\\s*[sS].*");
	protected final static Pattern CLOCK_PATTERN = Pattern.compile("([\\d\\.]+):([\\d\\.]+)\\s*");
	
	protected final static Pattern UNIT_PATTERN = Pattern.compile("([\\d\\.]+)\\s*([a-zA-Z/]+)$");
	
	protected final static BigDecimal KILO_VALUE = new BigDecimal("1000");
	protected final static BigDecimal MILLI_VALUE = new BigDecimal("0.001");
	protected final static BigDecimal MICRO_VALUE = new BigDecimal("0.000001");
	protected final static BigDecimal NANO_VALUE = new BigDecimal("0.000000001");
	
	protected final static char KILO_CHAR = 'k';
	protected final static char MILLI_CHAR = 'm';
	protected final static char MICRO_CHAR = '\u00B5';
	protected final static char DEGREE_SIGN = '\u00B0';
	
	public final static String KILO_PREFIX = String.valueOf(KILO_CHAR);
	public final static String MILLI_PREFIX = String.valueOf(MILLI_CHAR);
	public final static String MICRO_PREFIX = String.valueOf(MICRO_CHAR);

	protected static final String VOLUME_BASE = "L";
	protected static final String MASS_BASE = "g";
	protected static final String CONCENTRATION_BASE = MASS_BASE.concat("/m").concat(VOLUME_BASE);
	
	
	protected void setID(String newID) {
		this.myID = newID;
	}
	
	public static int[] parseLocation(String aLoc) {
		int[] retVal = {0, 0};
		Pattern pA = Pattern.compile("^([a-zA-Z]{1})([0-9]{1,2})");
		Matcher mA = pA.matcher(aLoc);
		Pattern pN = Pattern.compile("^([0-9]{1,2}),([0-9]{1,2})");
		Matcher mN = pN.matcher(aLoc);
		if ( mA.matches() ) {
			String myLoc = mA.group(1).toUpperCase();
			FINDLETTER: for ( int i = 1; i < ALPHABET.length ; i++ ) {
				if ( myLoc.equals(ALPHABET[i])) {
					retVal[0] = i;
					break FINDLETTER;
				}
			}
			retVal[1] = Integer.parseInt(mA.group(2));
		} else if ( mN.matches() ) {
			retVal[0] = Integer.parseInt(mN.group(1));
			retVal[1] = Integer.parseInt(mN.group(2));
		}	
		return retVal;
	}
	
	public void setProtocol(Map<String,String> newProtocol) {
		this.protocol = new HashMap<String,String>(newProtocol);
	}
	
	public void clearProtocol() {
		this.protocol = null;
	}
	
	public Map<String,String> getProtocol() {
		Map<String,String> export = new HashMap<String,String>(this.protocol);
		return export;
	}

	public static BigDecimal parseAmount(String amount) {
		Matcher match = UNIT_PATTERN.matcher(amount);
		if ( match.matches() )
			return parseAmount(match.group(1), match.group(2));
		return null;
	}
	
	public static BigDecimal parseAmount(String amount, String scale) {
		Matcher match = UNIT_PATTERN.matcher(amount);
		if ( match.matches() ) {
			amount = match.group(1);
			scale = match.group(2);
		} 
		if ( amount.length() > 0 ) {
			BigDecimal newAmount = new BigDecimal(amount);
			if ( scale.length() > 1 ) {
				int power = 0;
				String[] units = scale.split(" */ *");
				switch (units[0].charAt(0)) {
				case KILO_CHAR:	power = 3; break;
				case MILLI_CHAR:	power = -3; break;
				case MICRO_CHAR: 
				case 'u':	power = -6; break;
				}
				if ( units.length == 2 ) {
					switch (units[1].charAt(0)) {
					case KILO_CHAR:	power -= 6; break;
					case MILLI_CHAR: 	break;
					case MICRO_CHAR:
					case 'u':	power += 3; break;
					case 'n':	power += 6; break;
					default: 	power -= 3; 
					}
				}
				newAmount = newAmount.scaleByPowerOfTen(power);
			}
			return newAmount;
		} 
		return null;
	}

	public static String formatAmount(String format, float amount, String scale) {
		if ( scale != null ) {
			if ( scale.length() > 1 ) {
				switch (scale.charAt(0)) {
				case KILO_CHAR:	amount /= 1000; break;
				case MILLI_CHAR:	amount *= 1000; break;
				case MICRO_CHAR:
				case 'u':	amount *= (1000*1000); break;
				}
			}
			return String.format(format, amount, scale);
		} else {
			return String.format(format, amount, "");
		}
	}
	
	public static String formatAmount(BigDecimal amount, String unit) { 
		if ( amount == null ) return "";
		if ( unit != null ) {
			if ( unit.length() > 1 ) {
				int power = amount.scale();
				String[] units = unit.split(" */ *");
				switch (units[0].charAt(0)) {
				case KILO_CHAR:	power = 3; break;
				case MILLI_CHAR:	power = -3; break;
				case MICRO_CHAR:
				case 'u':	power = -6; break;
				case 'n':	power = -9; break;
				}
				if ( units.length == 2 ) {
					switch (units[1].charAt(0)) {
					case KILO_CHAR:	power -= 6; break;
					case MILLI_CHAR: 	break;
					case MICRO_CHAR:
					case 'u':	power += 3; break;
					case 'n': 	power += 6; break;
					default: 	power -= 3;
					}
				}
				amount = amount.movePointLeft(power);
			}
			return amount.toPlainString().concat(" ").concat(unit);		
		} else {
			return amount.toPlainString();
		}
		
	}
	
	public static String autoFormatAmount(BigDecimal amount, int type) {
		return autoFormatAmount(amount, type, KILO_PREFIX, MILLI_PREFIX, MICRO_PREFIX);
	}

	public static String autoFormatAmount(BigDecimal amount, int type, String kilo, String milli, String micro) {
		if ( amount == null ) return "";
		int power = amount.precision() - amount.scale();
		int scale = 0;
		String unit = "";
		if ( power >= 3 ) {
			scale = 3;
			unit = kilo;
		} else if ( power <= -3 )  {
			scale = -6;
			unit = micro;
		} else if ( power <= 0 ) {
			scale = -3;
			unit = milli;
		}
		amount = amount.movePointLeft(scale);

		switch ( type ) {
			case BasicObject.CONCENTRATION_TYPE:
				switch ( scale ) {
				case 3: unit = kilo.concat(CONCENTRATION_BASE); break;
				case -3: unit = milli.concat(CONCENTRATION_BASE); break;
				case -6: unit = micro.concat(CONCENTRATION_BASE); break;
				default: unit = CONCENTRATION_BASE; 
				} 
				break;
			case BasicObject.VOLUME_TYPE:  unit = unit.concat(VOLUME_BASE); break;
			case BasicObject.MASS_TYPE:    
			default: 
				unit = unit.concat(MASS_BASE); break;
		}
		return amount.toPlainString().concat(" ").concat(unit);
	}
	

	protected static Double parseTime(String time) {
		Matcher match = MIN_PATTERN.matcher(time);
		if ( match.matches() ) {
			return Double.parseDouble(match.group(1));
		}
		match = SEC_PATTERN.matcher(time);
		if ( match.matches() ) { 
			return Double.parseDouble(match.group(1)) / 60.0f;
		}
		match = CLOCK_PATTERN.matcher(time);
		if ( match.matches() ) {
			return ( Double.parseDouble(match.group(1)) + ( Double.parseDouble(match.group(2)) / 60.0f) );
		}
		return null;
	}
}
