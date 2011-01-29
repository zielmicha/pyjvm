// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public class Main {

	public static void main(String[] args) {
		Unserializer unserializer = new Unserializer(System.in);
		Obj read = unserializer.read();
		Instr main = (Instr)read;
		
		Frame frame = new Frame(null);
		Frame.execute(frame, main);
		Module module = (Module)frame.reg[0];
		module.dump();
		frame.globals.dump();
	}

}
