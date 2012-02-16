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

public final class SBool extends Obj {
	public final boolean value;

	private SBool(boolean val) {
		this.value = val;
	}
	
	public static final SBool get(boolean val) {
		if(val)
			return SBool.True;
		else
			return SBool.False;
	}
	
	public static final SBool True = new SBool(true);
	public static final SBool False = new SBool(false);
	
	public int hashCode() {
		return value?1:0;
	}
	
	public boolean boolValue() {
		return this.value;
	}
	
	public String toString() {
		if(value)
			return "True";
		else
			return "False";
	}
	
	public SBool isEqual(Object other) {
		if(other instanceof SBool)
			return other == this? SBool.True: SBool.False;
		else
			return null;
	}
}
