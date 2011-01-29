// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public final class None extends Obj {
	private None() {}
	
	public static final None None = new None(); 
	
	public String toString() {
		return "None";
	}
}
