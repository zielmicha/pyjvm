package pyjvm;

public final class SBool extends Obj {
	public final boolean value;

	private SBool(boolean val) {
		this.value = val;
	}
	
	public static final SBool get(boolean val) {
		if(val)
			return SBool.True;
		else
			return SBool.False;
	}
	
	public static final SBool True = new SBool(true);
	public static final SBool False = new SBool(false);
	
	public int hashCode() {
		return value?1:0;
	}
	
	public boolean boolValue() {
		return this.value;
	}
	
	public String toString() {
		if(value)
			return "True";
		else
			return "False";
	}
	
	public SBool isEqual(Object other) {
		if(other instanceof SBool)
			return other == this? SBool.True: SBool.False;
		else
			return null;
	}
}
