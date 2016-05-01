package org.nightswimming.screener.util.tuple;

import java.util.List;

import org.nightswimming.screener.util.tuple.TupleException.*;

public interface NTuple<T,N extends NTuple.Size>{
	int size();
	List<T> asList();
	void clear();
	NTuple<T,N> clone();
	T getAt(int index) throws IndexOutOfTupleRangeException;
	void setAt(int index, T element) throws IndexOutOfTupleRangeException, UnknownExpectedParamTypeException;
	NTuple<T,N> replaceAt(int index, T element);
	String toString();

	//<U>replaceAt(U replacement) canviant tipus
	//	NTuple removeAt(int index) throws IndexOutOfTupleRangeException;

	//Add B o constructor a GPair que accepta Gunit + B
	//	NTuple add(Object t)  throws NotAllowedTypeParamException //�MaxTupleSuuportedSizeException? o retornem anonymous?; ;
	
	//TODO?: public interface IPair, so TPair & Pair can be converged
	//public interface IPair<A,B> extends NTuple<>{}
	
	interface Size{}
	public static class ZERO implements Size{}
	public static class ONE implements Size{}
	public static class TWO implements Size{}
	public static class THREE implements Size{}
	public static class FOUR implements Size{}
	public static class FIVE implements Size{}
	
}
