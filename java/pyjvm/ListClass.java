// AUTOGENERATED by exporter.py from List.java
package pyjvm;

import pyjvm.*;

public final class ListClass extends Type {
	private ListClass() {}

	public static final StringDict dict;
	public static final ListClass instance = new ListClass();
	public static final Obj constructor;
	
	static {
		if("List".equals("NativeObj") || "List".equals("UserObj"))
			dict = new StringDict();
		else
			dict = NativeObjClass.dict.copy();
		
		dict.put("append", new Method() {
			public Obj callMethod(Obj self, Obj[] args) {
				if(args.length != 1) {
					throw new ScriptError(ScriptError.TypeError, "Bad number of arguments");
				}
				((List)self).append(args[0]); return None;
			}
		});
		constructor = new Obj() {
			public Obj call(Obj[] args)  {
				// direct
				return List.construct(args);
			}
		};
		
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