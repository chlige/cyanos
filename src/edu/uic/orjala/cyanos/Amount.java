package edu.uic.orjala.cyanos;

import java.math.BigDecimal;
import java.util.regex.Matcher;

public class Amount {
	
	public enum AmountUnit {
		NONE(null),
		MASS("g"),
		VOLUME("L"),
		CONCENTRATION("g", AmountScale.MILLI, "L");
		
		private String unit;
		private String denom = null;
		private AmountScale denomScale = AmountScale.BASE;
		
		AmountUnit(String unit) {
			this.unit = unit;
		}
		
		AmountUnit(String unit, AmountScale denomScale, String denom) {
			this.unit = unit;
			this.denomScale = denomScale;
			this.denom = denom;
		}
		
		public String getUnit() {
			return this.unit;
		}
		
		public String getDenom() {
			return this.denom;
		}
		
		public String print(AmountScale scale) {
			if ( this.denom != null ) {
				int power = scale.power + this.denomScale.power;
				String denomPrefix = "";
				String numPrefix = "";
				if ( power > this.denomScale.power ) {
					for ( AmountScale prefix : AmountScale.values() ) {
						if ( prefix.power == power ) { numPrefix = prefix.prefix; }
						else if ( prefix.power == 0 ) { denomPrefix = prefix.prefix; }
					}
				} else {
					denomPrefix = this.denomScale.prefix;
					numPrefix = scale.prefix;
				}
				return String.format("%s%s/%s%s", numPrefix, this.unit, denomPrefix, this.denom);		
			} else {
				return scale.prefix.concat(this.unit);
			}
		}
	}
	
	public enum AmountScale {
		KILO("k", 3, null),
		MILLI("m", -3, null),
		MICRO("\u00B5", -6, "u"),
		NANO("n", -9, null),
		BASE("", 0, null);

		private String prefix;
		private int power;
		private String alternate = null;
		
		AmountScale(String prefix, int power, String alternate) {
			this.prefix = prefix;
			this.power = power;
			this.alternate = alternate;
		}
		
		public int getPower() {
			return this.power;
		}
		
		public String getPrefix() {
			return this.prefix;
		}
		
		public boolean isPrefix(String unit) {
			if ( this.prefix.length() == 0 ) {
				return false;
			}
			if ( this.alternate != null ) {
				if ( unit.startsWith(this.alternate)  )
					return true;
			}
			return unit.startsWith(prefix);
		}
	}

	private BigDecimal value = BigDecimal.ZERO;
	private AmountUnit unit = AmountUnit.NONE;
	
	public Amount(String amount) {
		this.parseAmount(amount, null);
	}

	public Amount(String amount, String defaultUnit) {
		this.parseAmount(amount, defaultUnit);
	}
	
	public Amount(String amount, AmountUnit defaultUnit) {
		this.parseAmount(amount, defaultUnit.unit);
	}
	
	public Amount(BigDecimal value, String unit) {
		this.parseAmount(value, unit);
	}
	
	public Amount(BigDecimal value, AmountUnit unit) {
		this.value = value;
		this.unit = unit;
	}
	
	public static Amount grams(BigDecimal value) {
		return new Amount(value, AmountUnit.MASS);
	}
	
	public static Amount liters(BigDecimal value) {
		return new Amount(value, AmountUnit.VOLUME);
	}
	
	public static Amount gramsPermL(BigDecimal value) {
		return new Amount(value, AmountUnit.CONCENTRATION);
	}
	
	private void parseAmount(String amount, String unit) {	
		Matcher match = CyanosObject.UNIT_PATTERN.matcher(amount);
		if ( match.matches() ) {
			amount = match.group(1);
			unit = match.group(2);
		} 
		if ( unit != null & amount.length() > 0 ) {
			this.parseAmount(new BigDecimal(amount), unit);
		} 
	}
	
	private void parseAmount(BigDecimal newValue, String unit) {
		if ( unit.length() > 1 ) {
			int power = 0;
			String[] units = unit.split("\\s*/\\s*");
			for ( AmountScale prefix : AmountScale.values() ) {
				if ( prefix.isPrefix(units[0]) ) {
					power = prefix.power;
					break;
				}
			}
			if ( units.length == 2 ) {
				int denomPower = 0;
				for ( AmountScale prefix : AmountScale.values() ) {
					if ( prefix.isPrefix(units[0]) ) {
						denomPower = prefix.power;
						break;
					}
				}
				power = power - (denomPower - AmountScale.MILLI.power);
				this.unit = AmountUnit.CONCENTRATION;
			} else {
				String baseUnit = units[0].substring(units[0].length() - 1);
				if ( baseUnit.equalsIgnoreCase(AmountUnit.VOLUME.unit) ) {
					this.unit = AmountUnit.VOLUME;
				} else if ( baseUnit.equalsIgnoreCase(AmountUnit.MASS.unit) ) {
					this.unit = AmountUnit.MASS;
				}						
			}
			newValue = newValue.scaleByPowerOfTen(power);
		}
		this.value = newValue;
	}
	
	private static final AmountScale[] BY_SCALE = {AmountScale.KILO, AmountScale.BASE, AmountScale.MILLI, AmountScale.MICRO, AmountScale.NANO}; 
	
	public String toString() {
		if ( value == null ) return "";
		if ( unit == AmountUnit.NONE ) {
			return value.toPlainString();
		} else {
			int power = value.precision() - value.scale();
			AmountScale autoScale = AmountScale.BASE;

			for ( AmountScale prefix : BY_SCALE ) {
				if ( power >= prefix.power ) {
					autoScale = prefix;
					break;
				}
			}

			return this.toString(autoScale);
		}
	}

	public Amount convert(BigDecimal concentration) {
		Amount newAmt = this;
		if ( concentration != null && concentration.compareTo(BigDecimal.ZERO) == 0 ) {
			if ( unit == AmountUnit.MASS ) {
				newAmt = new Amount(value.divide(concentration).movePointRight(3), AmountUnit.MASS);
			} else if ( unit == AmountUnit.VOLUME ) {
				newAmt = new Amount(value.movePointLeft(3).multiply(concentration), AmountUnit.VOLUME);
			}
		}
		return newAmt;
	}
	
	public String toString(BigDecimal concentration) {
		return this.convert(concentration).toString();
	}
	
	public String toString(AmountScale scale) {
		BigDecimal formatValue = value.movePointLeft(scale.power);
		return formatValue.toPlainString().concat(" ").concat(unit.print(scale));			
	}
	
	public BigDecimal getValue() {
		return this.value;
	}
	
	public AmountUnit getBaseUnit() {
		return this.unit;
	}
	
	public Amount negate() {
		return new Amount(this.value.negate(), this.unit);
	}
}