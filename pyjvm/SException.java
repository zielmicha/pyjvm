package pyjvm;

public class SException extends Obj { //!export SException
	public SException(Obj reason) { 
		
	}
	
	public static Obj create(Obj reason) { //!export - <new>
		return new SException(reason);
	}
}
