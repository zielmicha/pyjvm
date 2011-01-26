package pyjvm;

public final class StringDict extends SObject {
	public final class Entry {
		public Entry(int key, SObject val) {
			this.key = key;
			this.val = val;
		}
		
		public int key;
		public SObject val;
		Entry next;
	}
	
	private Entry[] entries;
	private int resizeAt;
	private int length = 0;
	
	private static final float scaleFactor = 1.35f;
	
	public StringDict(int initalCapacity) {
		entries = new Entry[(int)(initalCapacity * scaleFactor)];
		resizeAt = initalCapacity;
	}
	
	public final void put(int key, SObject val) {
		int hash = key;
		int index = Math.abs(hash % entries.length);
		Entry e = entries[index];
		while(e != null) {
			if(e.key == key) {
				e.val = val;
				return;
			}
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
	
	public final void put(SString key, SObject val) {
		put(key.intern(), val);
	}
	
	public final void put(String string, SObject val) {
		put(SString.fromJavaString(string), val);
	}
	
	private void resize() {
		Entry[] newEntries = new Entry[this.entries.length * 2];
		Entry[] oldEntries = this.entries;
		this.entries = newEntries;
		this.resizeAt = (int) (scaleFactor * newEntries.length);
		
		for(int i=0; i<oldEntries.length; i++) {
			Entry e = oldEntries[i];
			while(e != null) {
				put(e.key, e.val);
				e = e.next;
			}
		}
	}
	
	public final SObject getOrNull(int key) {
		int hash = key;
		int index = Math.abs(hash % entries.length);
		Entry e = entries[index];
		while(e != null) {
			if(e.key == key)
				return e.val;
			e = e.next;
		}
		return null;
	}
	
	public final SObject get(int key) {
		int hash = key;
		int index = Math.abs(hash % entries.length);
		Entry e = entries[index];
		while(e != null) {
			if(e.key == key)
				return e.val;
			e = e.next;
		}
		SString keyString = SString.uninternQuiet(key);
		throw new ScriptError(ScriptError.KeyError, "not found: " + keyString);
	}
	
	public final SObject get(String s) {
		return get(SString.intern(s));
	}
	
	public StringDict() {
		this(8);
	}
	
	public SBool isEqual() {
		// TODO: isEqual
		throw new RuntimeException("Not implemented.");
	}
	public int hashCode() {
		// TODO: hashCode
		throw new RuntimeException("Not implemented.");
	}
	
	
	public String toString() {
		DictEntryIterator iterator = new DictEntryIterator();
		Entry entry;
		StringBuilder builder = new StringBuilder();
		builder.append("{");
		while((entry=iterator.next()) != null) {
			builder.append(SString.uninternQuiet(entry.key).repr());
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
		
		public int next() {
			if(entry == null) {
				entry = entries[entryIndex];
				entryIndex ++;
				if(entryIndex == entries.length)
					return -1; // string keys are always >=0
			}
			int key = entry.key;
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
