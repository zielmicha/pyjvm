// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public class Main {

	public static void main(String[] args) {;
		Importer.path.append(SString.fromJavaString(args[0]));
		
		Unserializer unserializer = new Unserializer(System.in);
		Obj read = unserializer.read();
		Instr main = (Instr)read;
		
		Module.create(main);
	}

}
