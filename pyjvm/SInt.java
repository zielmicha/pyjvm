// Copyright (C) 2011 by Michal Zielinski
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.


package pyjvm;

public final class SInt extends Obj {
	public static final SInt ONE = SInt.get(1);
	public static final SInt MINUS_ONE = SInt.get(-1);
	public static final SInt ZERO = SInt.get(0);
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
	
	public Obj compare(Obj other) {
		if(!(other instanceof SInt)) return NotImplemented;
		int otherVal = other.intValue();
		if(otherVal == value)
			return ZERO;
		else if(value < otherVal)
			return MINUS_ONE;
		else
			return ONE;
	}
	
	public Obj unarySub() {
		return SInt.get(-value);
	}
}
