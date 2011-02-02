package pyjvm;

public final class UserType extends Type {
	private final int name;
	private final Tuple bases;
	final StringDict dict;

	private UserType(int name, Tuple bases, StringDict dict) {
		this.name = name;
		this.bases = bases;
		this.dict = UserObjClass.dict.copy();
		this.dict.update(dict);
	}

	public static Obj create(int name, Tuple bases, StringDict dict) {
		return new UserType(name, bases, dict);
	}

	public Obj getEntry(int name) {
		return dict.get(name);
	}

	public Obj call(Obj[] args) {
		// TODO: call __new__
		UserObj instance = new UserObj(this);
		
		Obj init = instance.getAttr(StringConst.__init__);
		Obj returned = init.call(args);
		if(returned != None)
			throw new ScriptError(ScriptError.TypeError, "__init__() should return None, not " + SString.repr(returned));
		
		return instance;
	}
	
	public Obj getAttr(int name) {
		Obj classAttr = dict.get(name);
		return classAttr; // TODO: think how does it work in cpython (or check docs)
	}
	
	public void setAttr(int name, Obj value) {
		dict.put(name, value);
	}

	public SString getName() {
		return SString.uninternQuiet(name);
	}
	
}
