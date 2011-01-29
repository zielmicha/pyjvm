// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public final class SFloat extends Obj {
	public double value;
	
	public SFloat(double val) {
		this.value = val;
	}
	
	public int hashCode() {
		return (int)Double.doubleToLongBits(value);
	}
	
	public SBool isEqual(Object other) {
		if(other instanceof SFloat)
			throw new ScriptError(ScriptError.TypeError, "float.__eq__ is not possible" +
					" (http://bit.ly/decimalEq). Use decimal module instead.");
		return null;
	}
}
