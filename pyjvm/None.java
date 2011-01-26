package pyjvm;

public final class None extends SObject {
	private None() {}
	
	public static final None None = new None(); 
	
	public String toString() {
		return "None";
	}
}
