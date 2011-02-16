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

public final class Dict extends Obj {
	public final class Entry {
		public Entry(Obj key, Obj val) {
			this.key = key;
			this.val = val;
		}
		
		public Obj key;
		public Obj val;
		Entry next;
	}
	
	private Entry[] entries;
	private int resizeAt;
	private int length = 0;
	
	private static final float scaleFactor = 1.35f;
	
	public Dict(int initalCapacity) {
		entries = new Entry[(int)(initalCapacity * scaleFactor)];
		resizeAt = initalCapacity;
	}
	
	public final void put(Obj key, Obj val) {
		int hash = Math.abs(key == null?0: key.hashCode());
		int index = hash % entries.length;
		Entry e = entries[index];
		while(e != null) {
			if(e.key.equals(key)) {
				e.val = val;
				return;
			}
			if(e.next == null)
				break;
			else
				e = e.next;
		}
		Entry entry = new Entry(key, val);
		if(e != null)
			e.next = entry;
		else
			entries[index] = entry;
		length++;
		if(length >= resizeAt) this.resize();
	}
	
	private void resize() {
		Entry[] newEntries = new Entry[entries.length * 2];
		resizeAt *= 2;
		Entry[] oldEntries = this.entries;
		this.entries = newEntries;
		
		for(int i=0; i<oldEntries.length; i++) {
			Entry e = oldEntries[i];
			while(e != null) {
				put(e.key, e.val);
				e = e.next;
			}
		}
	}
	
	public final Obj getOrNull(Obj key) {
		int hash = Math.abs(key == null?0: key.hashCode());
		int index = hash % entries.length;
		Entry e = entries[index];
		while(e != null) {
			if(e.key.equals(key))
				return e.val;
			e = e.next;
		}
		return null;
	}
	
	public final Obj get(Obj key) {
		int hash = Math.abs(key == null?0: key.hashCode());
		int index = hash % entries.length;
		Entry e = entries[index];
		while(e != null) {
			if(e.key.equals(key))
				return e.val;
			e = e.next;
		}
		throw new ScriptError(ScriptError.KeyError, "not found");
	}
	
	public Dict() {
		this(8);
	}
	
	public SBool isEqual(Object other) {
		if(other == this)
			return SBool.True;
		if(other instanceof Dict) {
			return null;
			// TODO: isEqual
		} else {
			return null;
		}
	}
	
	public int hashCode() {
		// TODO: implement
		throw new RuntimeException("Not implemented.");
	}
	
	public String toString() {
		DictEntryIterator iterator = new DictEntryIterator();
		Entry entry;
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		while((entry=iterator.next()) != null) {
			builder.append(entry.key.repr().toString());
			builder.append(": ");
			builder.append(entry.val.repr().toString());
			builder.append(", ");
		}
		builder.append("}");
		return builder.toString();
	}
	
	public final class DictKeyIterator {
		private int entryIndex = 0;
		private Entry entry = null;
		
		public Obj next() {
			if(entry == null) {
				entry = entries[entryIndex];
				entryIndex ++;
				if(entryIndex == entries.length)
					return null;
			}
			Obj key = entry.key;
			entry = entry.next;
			return key;
		}
	}
	private final class DictEntryIterator {
		private int entryIndex = 0;
		private Entry entry = null;
		
		public Entry next() {
			while(entry == null) {
				entry = entries[entryIndex];
				entryIndex ++;
				if(entryIndex == entries.length)
					return null;
			}
			Entry thisEntry = entry;
			entry = entry.next;
			return thisEntry;
		}
	}
}
