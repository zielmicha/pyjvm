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

public final class SFloat extends Obj { //!export SFloat
	public double value;
	
	public SFloat(double val) {
		this.value = val;
	}
	
	public int hashCode() {
		return (int)Double.doubleToLongBits(value);
	}
	
	public SBool isEqual(Obj other) {
		if(other instanceof SFloat)
			return this.value == ((SFloat)other).value ? SBool.True: SBool.False;
			/*throw new ScriptError(ScriptError.TypeError, "float.__eq__ is not possible" +
					" (http://bit.ly/decimalEq). Use decimal module instead.");*/
		return null;
	}

	public static SFloat get(double d) {
		return new SFloat(d);
	}
	
	
	public Obj add(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(value + otherValue);
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(value + otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj radd(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(value + otherValue);
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(value + otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj sub(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(value - otherValue);
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(value - otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj rsub(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(otherValue - value);
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(otherValue - value);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj mul(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(value * otherValue);
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(value * otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj rmul(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(otherValue * value);
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(otherValue * value);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj div(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(value / otherValue);
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(value / otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj rdiv(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(otherValue / value);
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(otherValue / value);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj truediv(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(value / otherValue);
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(value / otherValue);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj rtruediv(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(otherValue / value);
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(otherValue / value);
		} else {
			return NotImplemented;
		}
	}
	
	public Obj floordiv(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(Math.floor(value / otherValue));
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(Math.floor(value / otherValue));
		} else {
			return NotImplemented;
		}
	}
	
	public Obj rfloordiv(Obj other) {
		if(other instanceof SInt) {
			long otherValue = ((SInt)other).value;
			return SFloat.get(Math.floor(otherValue / value));
		} else if(other instanceof SFloat) {
			double otherValue = ((SFloat)other).value;
			return SFloat.get(Math.floor(otherValue / value));
		} else {
			return NotImplemented;
		}
	}
	
	public Type getType() {
		return SFloatClass.instance;
	}
	
	public String toString() {
		return "" + value;
	}
}
