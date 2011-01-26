package pyjvm;

public class NotImplementedError extends ScriptError {
	private static final long serialVersionUID = 7514012672408447720L;
	
	public NotImplementedError(String msg) {
		super(ScriptError.NotImplementedError, msg);
	}
	
	public NotImplementedError() {
		this(null);
	}
}
