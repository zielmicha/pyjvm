// AUTOGENERATED by exporter.py from List.java
package pyjvm;

public final class ListClass extends Type {
	private ListClass() {}

	public static final StringDict dict;
	public static final ListClass instance = new ListClass();
	
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
	}
	public final StringDict getDict() {
		return dict;
	}
	public final Obj getEntry(int name) {
		return dict.get(name);
	}
}
