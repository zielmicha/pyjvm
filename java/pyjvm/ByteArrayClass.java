// AUTOGENERATED by exporter.py from ByteArray.java
package pyjvm;

import pyjvm.*;

public final class ByteArrayClass extends Type {
	private ByteArrayClass() {}

	public static final StringDict dict;
	public static final ByteArrayClass instance = new ByteArrayClass();
	public static final Obj constructor = null;

	static {
		if("ByteArray".equals("NativeObj") || "ByteArray".equals("UserObj"))
			dict = new StringDict();
		else
			dict = NativeObjClass.dict.copy();

		dict.put("to_string", new Method() {
			public Obj callMethod(Obj self, Obj[] args) {
				if(args.length != 2) {
					throw new ScriptError(ScriptError.TypeError, "Bad number of arguments");
				}
				return ((ByteArray)self).to_string(args[0].intValue(), args[1].intValue());
			}
		});
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
