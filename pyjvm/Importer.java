package pyjvm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Importer {

	public static Obj importModule(SString name) {
		InputStream module = findModule(name.toString());
		Instr mainInstr = (Instr) Unserializer.unserialize(module);
		return Module.create(mainInstr);
	}
	
	public static InputStream findModule(String name) {
		String nameExt = name + ".bc";
		for(int i=0; i<path.length(); i++) {
			String item = path.getItem(i).stringValue().toString();
			try {
				InputStream res = Importer.class.getResourceAsStream("/" + item + "/" + nameExt);
				if(res == null)
					throw new IOException();
				return res;
			} catch (Exception e) {
				try {
					File f = new File(new File(item), nameExt);
					return new FileInputStream(f);
				} catch (IOException e2) {
					continue;
				}
			}
		}
		System.err.println("Path is: " + path);
		throw new ScriptError(ScriptError.ImportError, "No module named " + name);
	}
	
	public static List path = new List(); 
}
