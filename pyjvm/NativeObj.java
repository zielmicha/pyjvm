// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public abstract class NativeObj extends Obj { //!export NativeObj
	public abstract SClass getSClass();
	
	public Obj getAttr(int name) {
		SClass clazz = getSClass();
		Obj entry = clazz.getEntry(name);
		return entry.getObjectAttr(this);
	}
	
	
	// int length() //!export - __len__
}
