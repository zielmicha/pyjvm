// Copyright 2011 Michal Zielinski
// for license see LICENSE file
package pyjvm;

public class Main {

	public static void main(String[] args) {;
		Importer.path.append(SString.fromJavaString(args[0]));
		
		Importer.importModule("__main__");
	}

}
