// AUTOGENERATED by exporter.py from NativeObj.java
package pyjvm;

public final class NativeObjClass extends Type {
	private NativeObjClass() {}

	public static final StringDict dict;
	public static final NativeObjClass instance = new NativeObjClass();
	public static final Obj constructor = null;
	
	static {
		if("NativeObj".equals("NativeObj") || "NativeObj".equals("UserObj"))
			dict = new StringDict();
		else
			dict = NativeObjClass.dict.copy();
		
		dict.put("__len__", new Method() {
			public Obj callMethod(Obj self, Obj[] args) {
				if(args.length != 0) {
					throw new ScriptError(ScriptError.TypeError, "Bad number of arguments");
				}
				return SInt.get(((NativeObj)self).length());
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