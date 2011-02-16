package pyjvm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Importer {

	public static Module importModule(SString name) {
		Obj imported = modules.getOrNull(name.intern());
		if(imported != null)
			return (Module)imported;
		
		InputStream module = findModule(name.toString());
		
		return loadModule(module, name);
	}
	

	public static Module importModule(String name) {
		return importModule(new SString(name));
	}
	
	public static Module loadModule(InputStream code, SString name) {
		Instr mainInstr = (Instr) Unserializer.unserialize(code);
		Module object = new Module();
		modules.put(name, object);
		String jName = name.toString();
		if(jName.indexOf('.') != -1) {
			// import parent module
			int lastDot = jName.lastIndexOf('.');
			String parentModuleName = jName.substring(0, lastDot);
			String thisModuleName = jName.substring(lastDot + 1);
			Module parent = importModule(new SString(parentModuleName));
			parent.setAttr(SString.intern(thisModuleName), object);
		}
		executeModule(object, mainInstr);
		return object;
	}
	
	private static void executeModule(Module module, Instr mainInstr) {
		Frame frame = new Frame(null);
		frame.builtins = Builtins.dict;
		frame.module = module;
		frame.globals = frame.module.dict;
		
		Frame.execute(frame, mainInstr);
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
	public static StringDict modules = new StringDict();
}
