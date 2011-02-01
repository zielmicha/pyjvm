// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public final class SInt extends Obj {
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
	
	public SBool isEqual(Obj other) {
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
	
	public Obj add(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(value + otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj radd(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(otherValue + value);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj sub(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(value - otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj rsub(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(otherValue - value);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj mul(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(otherValue * value);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj rmul(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(value * otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj rfloordiv(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(otherValue / value);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj floordiv(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(value / otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj rdiv(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(otherValue / value);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj div(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SInt.get(value / otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj truediv(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SFloat.get((double)otherValue / value);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj rtruediv(Obj other) {
		if(other instanceof SInt) {
			int otherValue = ((SInt)other).value;
			return SFloat.get((double)value / otherValue);
		} else {
			return NotImplemented;
		}
	}
}
