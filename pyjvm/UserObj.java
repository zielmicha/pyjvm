package pyjvm;

public final class UserObj extends Obj { //!export UserObj
	private final UserType userClass;
	final StringDict dict;

	public UserObj(UserType userClass) {
		this.userClass = userClass;
		this.dict = new StringDict();
	}
	
	public Obj getAttr(int name) {
		// TODO: use __getattr__ etc.
		Obj classAttr = userClass.dict.getOrNull(name);
		if(classAttr == null)
			return dict.get(name);
		return classAttr.getObjectAttr(this);
	}

	public void setAttr(int name, Obj value) {
		// TODO: use __setattr__ 
		Obj classAttr = userClass.dict.getOrNull(name);
		if(classAttr == null)
			dict.put(name, value);
		else
			classAttr.setObjectAttr(this, value);
	}
	
	public String toString() {
		return "<" + userClass.getName() + " object at " + System.identityHashCode(this) + ">"; 
	}
	
	 Obj __init__(Obj[] args) { //!export direct __init__
		 return None;
	 }
}
