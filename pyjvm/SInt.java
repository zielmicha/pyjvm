package pyjvm;

public final class SInt extends SObject {
	public int value;

	private SInt(int i) {
		this.value = i;
	}
	
	public int hashCode() {
		return this.value;
	}
	
	public final int intValue() {
		return this.value;
	}
	
	public SBool isEqual(SObject other) {
		if(other == this)
			return SBool.True;
		if(other instanceof SInt)
			return ((SInt)other).value == this.value? SBool.True: SBool.False;
		return null;
	}

	public static SInt get(int num) {
		return new SInt(num);
	}
	
	public String toString() {
		return this.value + "";
	}
	
	public boolean boolValue() {
		return this.value != 0;
	}
	
	public SObject add(SObject other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(value + otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public SObject radd(SObject other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(otherValue + value);
		} else {
			return NotImplemented;
		}
	}
	
	public SObject sub(SObject other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(otherValue - value);
		} else {
			return NotImplemented;
		}
	}
	
	public SObject rsub(SObject other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(value - otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public SObject mul(SObject other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(otherValue * value);
		} else {
			return NotImplemented;
		}
	}
	
	public SObject rmul(SObject other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(value * otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public SObject floordiv(SObject other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(otherValue / value);
		} else {
			return NotImplemented;
		}
	}
	
	public SObject rfloordiv(SObject other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(value / otherValue);
		} else {
			return NotImplemented;
		}
	}
}
