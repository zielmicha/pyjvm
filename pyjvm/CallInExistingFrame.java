package pyjvm;

public interface CallInExistingFrame {
	/**
	 * Should store next instuction in frame.setInstr
	 * */
	void callInExistingFrame(Frame frame, Obj[] args);
}
