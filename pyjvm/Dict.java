package pyjvm;

public final class Dict extends SObject {
	public final class Entry {
		public Entry(SObject key, SObject val) {
			this.key = key;
			this.val = val;
		}
		
		public SObject key;
		public SObject val;
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
	
	public final void put(SObject key, SObject val) {
		int hash = Math.abs(key == null?0: key.hashCode());
		int index = hash % entries.length;
		Entry e = entries[index];
		while(e != null) {
			if(e.key.equals(key)) {
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
	
	private void resize() {
		Entry[] newEntries = new Entry[entries.length];
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
	
	public final SObject getOrNull(SObject key) {
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
	
	public final SObject get(SObject key) {
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
		
		public SObject next() {
			if(entry == null) {
				entry = entries[entryIndex];
				entryIndex ++;
				if(entryIndex == entries.length)
					return null;
			}
			SObject key = entry.key;
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
