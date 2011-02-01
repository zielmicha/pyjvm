package pyjvm;

public final class UserObj extends Obj {
	private final UserType userClass;
	private final StringDict dict;

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
}
