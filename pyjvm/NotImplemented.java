package pyjvm;

public final class NotImplemented extends Obj {
	private NotImplemented() {}
	
	public static final NotImplemented NotImplemented = new NotImplemented(); 
	
	public String toString() {
		return "NotImplemented";
	}
}
