package org.eclipse.wildwebdeveloper.json.schema;

public class Tuple<T, U> {
	
	public T t1;
	public U t2;

	public Tuple(T t1, U t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	public T getT1() {
		return t1;
	}

	public void setT1(T t1) {
		this.t1 = t1;
	}

	public U getT2() {
		return t2;
	}

	public void setT2(U t2) {
		this.t2 = t2;
	}

}
