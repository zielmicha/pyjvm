// AUTOGENERATED by exporter.py from Builtins.java
package pyjvm;

import pyjvm.*;

public final class BuiltinsClass extends Type {
	private BuiltinsClass() {}

	public static final StringDict dict;
	public static final BuiltinsClass instance = new BuiltinsClass();
	public static final Obj constructor = null;
	
	static {
		if("Builtins".equals("NativeObj") || "Builtins".equals("UserObj"))
			dict = new StringDict();
		else
			dict = NativeObjClass.dict.copy();
		
		dict.put("int", new Obj() {
			public Obj call(Obj[] args) {
				if(args.length != 1) {
					throw new ScriptError(ScriptError.TypeError, "Bad number of arguments");
				}
				return Builtins.int_(args[0]);
			}
		});
		dict.put("len", new Obj() {
			public Obj call(Obj[] args) {
				if(args.length != 1) {
					throw new ScriptError(ScriptError.TypeError, "Bad number of arguments");
				}
				return SInt.get(Builtins.len(args[0]));
			}
		});
		dict.put("repr", new Obj() {
			public Obj call(Obj[] args) {
				if(args.length != 1) {
					throw new ScriptError(ScriptError.TypeError, "Bad number of arguments");
				}
				return Builtins.repr(args[0]);
			}
		});
		dict.put("string_dict", new Obj() {
			public Obj call(Obj[] args) {
				if(args.length != 0) {
					throw new ScriptError(ScriptError.TypeError, "Bad number of arguments");
				}
				return Builtins.string_dict();
			}
		});
		dict.put("type", new Obj() {
			public Obj call(Obj[] args) {
				// direct
				return Builtins.type(args);
			}
		});
		dict.put("isinstance", new Obj() {
			public Obj call(Obj[] args) {
				if(args.length != 2) {
					throw new ScriptError(ScriptError.TypeError, "Bad number of arguments");
				}
				return Builtins.isinstance(args[0], args[1])? SBool.True: SBool.False;
			}
		});
		dict.put("xrange", new Obj() {
			public Obj call(Obj[] args) {
				// direct
				return Builtins.xrange(args);
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
