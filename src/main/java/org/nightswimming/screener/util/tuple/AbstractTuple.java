package org.nightswimming.screener.util.tuple;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.nightswimming.screener.util.tuple.NTuple.Size;
import org.nightswimming.screener.util.tuple.TupleException.*;

@SuppressWarnings("unchecked") //We made all castings safe, no other way to do it
public abstract class AbstractTuple<T,N extends Size,Self extends NTuple<T,N>>{
	
	private final List<T> innerStore;
	private final boolean typed;
	
	protected AbstractTuple(boolean typed){
		this.typed = typed;
		innerStore = new ArrayList<>(Collections.nCopies(this.size(),null));
	}
	
	public abstract int size();

	public List<T> asList(){ return new ArrayList<T>(this.innerStore);}
	
	public final T getAt(int index) throws IndexOutOfTupleRangeException{
		if (index < 0 || index > this.size()-1) 
			throw new IndexOutOfTupleRangeException(index,this.size());
		return this.innerStore.get(index);
	}

	public void setAt(int index, T element) throws IndexOutOfTupleRangeException, UnknownExpectedParamTypeException{
		if(!typed) throw new UnknownExpectedParamTypeException(index, element);
		if (index < 0 || index > this.size()-1) 
			throw new IndexOutOfTupleRangeException(index,this.size());
		setUnsafeAt(index, element);
	}
	
	public final void setUnsafeAt(int index, T element) throws IndexOutOfTupleRangeException{
		this.innerStore.set(index, element);
	}

	public final Self clone(){
		return (Self) reinstantiate((T[])this.innerStore.toArray());
	}
	
	public NTuple<T,N> replaceAt(int index, T element) throws TypeNotModifiableException{
		if(typed) throw new TypeNotModifiableException();
		List<T> copy = new ArrayList<>(this.innerStore);
		copy.set(index, element);
		return reinstantiate((T[])copy.toArray());
	}
	
	private final Self reinstantiate(T... params) throws TupleCopyingReflectionException{
		try {
			Class<?>[] constructorTypes = new Class[params.length];
			Arrays.fill(constructorTypes,Object.class);
			Constructor<Self> constructor = (Constructor<Self>) this.getClass().getDeclaredConstructor(constructorTypes);
			Self instance = constructor.newInstance(params);
			return instance;
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new TupleCopyingReflectionException(e);
		}
	}	
	
	public final void clear(){
		Collections.fill(this.innerStore, null);
	}
	
	public String toString(){
		return this.innerStore.toString();
	}
	//TODO: Generic add and remove
	
	
	/********************************************************
	 *                   TUPLE ABSTRACT CLASSES             *
	 * ******************************************************/
	protected static abstract class GVoid<T,N extends Size,Self extends NTuple<T,N>> extends AbstractTuple<T,N,Self>{
		protected GVoid(boolean t) {super(t);}
		@Override public int size() { return 0; }
	}	
	protected static abstract class GUnit<A,T,N extends Size,Self extends NTuple<T,N>> extends GVoid<T,N,Self>{
		protected GUnit(A a,boolean t){ super(t); setA(a); }
		public void setA(A a){ super.setUnsafeAt(0,(T)a); }
		public A  getA() { return (A) this.getAt(0); }
		@Override public int size() { return 1; }
	}
	protected static abstract class GPair<A,B,T,N extends Size,Self extends NTuple<T,N>> extends GUnit<A,T,N,Self>{
		protected GPair(A a,B b,boolean t){ super(a,t); setB(b); }
		public void setB(B b){ super.setUnsafeAt(1,(T)b); }
		public B getB() { return (B) this.getAt(1); }
		@Override public int size() { return 2; }
	}
	protected static abstract class GTriplet<A,B,C,T,N extends Size,Self extends NTuple<T,N>> extends GPair<A,B,T,N,Self>{
		protected GTriplet(A a,B b,C c,boolean t){ super(a,b,t); setC(c); }
		public void setC(C c){ super.setUnsafeAt(2,(T)c); }
		public C getC() { return (C) this.getAt(2); }
		@Override public int size() { return 3; }
	}
	protected static abstract class GQuartet<A,B,C,D,T,N extends Size,Self extends NTuple<T,N>> extends GTriplet<A,B,C,T,N,Self>{
		protected GQuartet(A a,B b,C c,D d,boolean t){ super(a,b,c,t); setD(d); }
		public void setD(D d){ super.setUnsafeAt(3,(T)d); }
		public D getD() { return (D) this.getAt(3); }
		@Override public int size() { return 4; }
	}
	protected static abstract class GQuintet<A,B,C,D,E,T,N extends Size,Self extends NTuple<T,N>> extends GQuartet<A,B,C,D,T,N,Self>{
		protected GQuintet(A a,B b,C c,D d,E e,boolean t){ super(a,b,c,d,t); setE(e); }
		public void setE(E e){ super.setUnsafeAt(4,(T)e); }
		public E getE() { return (E) this.getAt(4); }
		@Override public int size() { return 5; }
	}
}
