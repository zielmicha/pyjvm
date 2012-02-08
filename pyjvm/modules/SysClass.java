// AUTOGENERATED by exporter.py from Sys.java
package pyjvm.modules;

import pyjvm.*;

public final class SysClass extends Type {
	private SysClass() {}

	public static final StringDict dict;
	public static final SysClass instance = new SysClass();
	public static final Obj constructor = null;
	
	static {
		if("Sys".equals("NativeObj") || "Sys".equals("UserObj"))
			dict = new StringDict();
		else
			dict = NativeObjClass.dict.copy();
		
	}
	public final StringDict getDict() {
		return dict;
	}
	public final Obj getEntry(int name) {
		return dict.get(name);
	}
	public final Obj call(Obj[] args) {
		if(constructor == null)
			throw new ScriptError(ScriptError.TypeError, "Object uninitializable");
		return constructor.call(args);
	}
}
