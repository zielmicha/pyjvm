// Copyright (C) 2011 by Michal Zielinski
// 
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
// 
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
// 
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.

package pyjvm;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

public class Importer {

	public static Module importModule(SString name) {
		if(!builtinsImported) {
			builtinsImported = true;
			Builtins.importBuiltins();
		}
		Obj imported = modules.getOrNull(name.intern());
		if(imported != null)
			return (Module)imported;
		
		Obj builtin = builtinModules.getOrNull(name.intern());
		if(builtin != null) {
			modules.put(name, builtin);
			return (Module)builtin;
		}
		
		Instr module = readModule(name.toString());
		
		return loadModule(module, name);
	}
	

	public static Module importModule(String name) {
		return importModule(new SString(name));
	}
	
	public static Module loadModule(Instr code, SString name) {
		Module object = new Module();
		object.dict.put("__name__", name);
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
		executeModule(object, code);
		return object;
	}
	
	private static void executeModule(Module module, Instr mainInstr) {
		Frame frame = new Frame(null);
		frame.builtins = Builtins.dict;
		frame.module = module;
		frame.globals = frame.module.dict;
		
		Frame.execute(frame, mainInstr);
	}
	
	public static Instr readModule(String name) {
		Obj preloaded = loadedFiles.getOrNull(new SString(name).intern());
		if(preloaded != null)
			return (Instr) preloaded;
		InputStream in = findModule(name);
		return (Instr) Unserializer.unserialize(in);
	}
	
	private static InputStream findModule(String name) {
		String nameExt = name + ".bc";
		for(int i=0; i<path.length(); i++) {
			String item = path.getItem(i).stringValue().toString();
			try {
                                File f = new File(new File(item), nameExt);
                                return new FileInputStream(f);
			} catch (Exception e) {
				try {
                                    InputStream res = Importer.class.getResourceAsStream("/" + item + "/" + nameExt);
                                    if(res == null)
                                            throw new IOException();
                                    return res;
				} catch (Exception e2) {
					continue;
				}
			}
		}
		throw new ScriptError(ScriptError.ImportError, "No module named " + name);
	}
	
	public static void loadArchive(InputStream stream) {
		StringDict read = (StringDict) Unserializer.unserialize(stream);
		loadedFiles.update(read);
	}
	
	public static List path = new List(); 
	public static StringDict modules = new StringDict();
	public static StringDict builtinModules = new StringDict();
	public static StringDict loadedFiles = new StringDict();
	public static boolean builtinsImported;
	
	static {
		builtinModules.put("sys", new Module(pyjvm.modules.Sys.dict));
		builtinModules.put("reflect", new Module(pyjvm.modules.reflect.Reflect.dict));
	}
}
