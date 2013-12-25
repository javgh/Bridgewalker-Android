package com.bridgewalkerapp.androidclient.data;

public class Maybe<T> {
	private boolean containsValue;
	private T value;
	
	public Maybe() {
		this.value = null;
		this.containsValue = false;
	}
	
	public Maybe(T value) {
		this.value = value;
		this.containsValue = value != null;
	}
	
	public boolean containsValue() {
		return containsValue;
	}
	
	public T getValue() {
		return value;
	}
}
