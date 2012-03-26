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

package pyjvm.modules;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import pyjvm.*;

public class Sys { //!export modules.Sys
	public static final StringDict dict;
	
	public static void setArgs(String[] args, int offset) {
		List argv = new List(args.length);
		argv.append(SString.fromJavaString("pyjvm"));
		for(int i=offset; i<args.length; i++) {
			argv.append(SString.fromJavaString(args[i]));
		}
		dict.put("argv", argv);
	}
	
	public static Obj unserialize(Obj data) { //!export
		InputStream in = new ByteArrayInputStream(data.stringValue().bytes);
		return Unserializer.unserialize(in);
	}
	
	static {
		dict = SysClass.dict;
		
		dict.put("modules", Importer.modules);
		dict.put("builtins", Importer.builtinModules);
		dict.put("__name__", SString.fromJavaString("sys"));
	}
}
