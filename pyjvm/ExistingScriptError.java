package pyjvm;

public final class ExistingScriptError extends RuntimeException {
	public final Obj exception;
	public final Traceback traceback;

	public ExistingScriptError(Obj exception, Traceback traceback) {
		this.exception = exception;
		this.traceback = traceback;
	}
	
	public String toString() {
		return exception.toString();
	}
	
	private static final long serialVersionUID = -7415773783405904392L;

	
	
}
