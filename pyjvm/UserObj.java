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

public final class UserObj extends Obj { //!export UserObj
	private final UserType userClass;
	final StringDict dict;

	public UserObj(UserType userClass) {
		this.userClass = userClass;
		this.dict = new StringDict();
	}
	
	public Obj getAttr(int name) {
		// TODO: use __getattr__ etc.
		Obj classAttr = userClass.dict.getOrNull(name);
		if(classAttr == null)
			return dict.get(name);
		return classAttr.getObjectAttr(this);
	}

	public void setAttr(int name, Obj value) {
		// TODO: use __setattr__ 
		Obj classAttr = userClass.dict.getOrNull(name);
		if(classAttr == null)
			dict.put(name, value);
		else
			classAttr.setObjectAttr(this, value);
	}
	
	public void delAttr(int name) {
		// TODO: delAttr 
		dict.delete(name);
	}
	
	public String toString() {
		return "<" + userClass.getName() + " object at " + System.identityHashCode(this) + ">"; 
	}
	
	 Obj __init__(Obj[] args) { //!export direct __init__
		 return None;
	 }
}
