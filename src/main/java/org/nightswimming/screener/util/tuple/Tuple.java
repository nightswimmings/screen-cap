package org.nightswimming.screener.util.tuple;

import org.nightswimming.screener.util.tuple.AbstractTuple.*;

import static org.nightswimming.screener.util.tuple.NTuple.*;

//TODO: Tuples of N > 5
@SuppressWarnings("unchecked")
public class Tuple {
	
	/*************************** ***************
	 *             TUPLE BUILDERS              *
	 *******************************************/
	
	public static             Void               of()                      	  { return new Void(); }
	public static <A>         Unit<A>            of(A a)                      { return new Unit<A>(a); }
	public static <A,B>       Pair<A,B>          of(A a,B b)                  { return new Pair<A,B>(a,b);}
	public static <A,B,C>     Triplet<A,B,C>     of(A a,B b,C c)              { return new Triplet<A,B,C>(a,b,c);}
	public static <A,B,C,D>   Quartet<A,B,C,D>   of(A a,B b,C c,D d)          { return new Quartet<A,B,C,D>(a,b,c,d);}
	public static <A,B,C,D,E> Quintet<A,B,C,D,E> of(A a,B b,C c,D d,E e)      { return new Quintet<A,B,C,D,E>(a,b,c,d,e);}

	public static <T>         TVoid<T>           ofTyped()                    { return new TVoid<T>(); }
	public static <T>         TUnit<T>           ofTyped(T a)                 { return new TUnit<T>(a); }
	public static <T>         TPair<T>           ofTyped(T a,T b)             { return new TPair<T>(a,b);}	
	public static <T>         TTriplet<T>        ofTyped(T a,T b,T c)         { return new TTriplet<T>(a,b,c);}	
	public static <T>         TQuartet<T>        ofTyped(T a,T b,T c,T d)     { return new TQuartet<T>(a,b,c,d);}	
	public static <T>         TQuintet<T>        ofTyped(T a,T b,T c,T d,T e) { return new TQuintet<T>(a,b,c,d,e);}	
	
	@SafeVarargs
	public static <T>         NTuple<Object,?>   of(Object... elements)        { switch(elements.length){
																					case 0: return of();
																					case 1: return of(elements[0]);
																					case 2: return of(elements[0], elements[1]);
																					case 3: return of(elements[0], elements[1], elements[2]);
																					case 4: return of(elements[0], elements[1], elements[2], elements[3]);
																					case 5: return of(elements[0], elements[1], elements[2], elements[3], elements[4]);
																					default: throw new TupleException.MaxTupleSuuportedSizeException(5, null);
																			}
	}
	
	@SafeVarargs
	public static <T>         NTuple<T,?>        ofTyped(T... elements)        { switch(elements.length){
																					case 0: return ofTyped();
																					case 1: return ofTyped(elements[0]);
																					case 2: return ofTyped(elements[0], elements[1]);
																					case 3: return ofTyped(elements[0], elements[1], elements[2]);
																					case 4: return ofTyped(elements[0], elements[1], elements[2], elements[3]);
																					case 5: return ofTyped(elements[0], elements[1], elements[2], elements[3], elements[4]);
																					default: throw new TupleException.MaxTupleSuuportedSizeException(5, null);
																				}
	}
	
	/*************************** ***************
	 *             TUPLE CLASSES               *
	 *******************************************/
		
	/** HETEROGENIC TUPLES **/
	
	public static class Void                 extends GVoid<Object,ZERO,Void> implements NTuple<Object,ZERO>{
		protected Void()	     		     { super(false);}
	}
	public static class Unit<A>              extends GUnit<A,Object,ONE,Unit<A>> implements NTuple<Object,ONE>{
		protected Unit(A a)					 { super(a,false);}
	}
	public static class Pair<A,B>            extends GPair<A,B,Object,TWO,Pair<A,B>> implements NTuple<Object,TWO>{
		protected Pair(A a,B b)			     { super(a,b,false); }
		public <A2> Pair<A2,B> replaceA(A2 replacement){ return (Pair<A2, B>) super.replaceAt(0, replacement); }
		public <B2> Pair<A,B2> replaceB(B2 replacement){ return (Pair<A, B2>) super.replaceAt(1, replacement); }	
	}
	public static class Triplet<A,B,C>       extends GTriplet<A,B,C,Object,THREE,Triplet<A,B,C>> implements NTuple<Object,THREE>{
		protected Triplet(A a,B b,C c) 	     { super(a,b,c,false); }
		public <A2> Triplet<A2,B,C> replaceA(A2 replacement){ return (Triplet<A2, B, C>) super.replaceAt(0, replacement); }
		public <B2> Triplet<A,B2,C> replaceB(B2 replacement){ return (Triplet<A, B2, C>) super.replaceAt(1, replacement); }
		public <C2> Triplet<A,B,C2> replaceC(C2 replacement){ return (Triplet<A, B, C2>) super.replaceAt(2, replacement); }
	}
	public static class Quartet<A,B,C,D>   	 extends GQuartet<A,B,C,D,Object,FOUR,Quartet<A,B,C,D>> implements NTuple<Object,FOUR>{
		protected Quartet(A a,B b,C c,D d) 	 { super(a,b,c,d,false); }
		public <A2> Quartet<A2,B,C,D> replaceA(A2 replacement){ return (Quartet<A2, B, C, D>) super.replaceAt(0, replacement); }
		public <B2> Quartet<A,B2,C,D> replaceB(B2 replacement){ return (Quartet<A, B2, C, D>) super.replaceAt(1, replacement); }
		public <C2> Quartet<A,B,C2,D> replaceC(C2 replacement){ return (Quartet<A, B, C2, D>) super.replaceAt(2, replacement); }
		public <D2> Quartet<A,B,C,D2> replaceD(D2 replacement){ return (Quartet<A, B, C, D2>) super.replaceAt(3, replacement); }
	}
	public static class Quintet<A,B,C,D,E> 	 extends GQuintet<A,B,C,D,E,Object,FIVE,Quintet<A,B,C,D,E>> implements NTuple<Object,FIVE>{
		protected Quintet(A a,B b,C c,D d,E e) { super(a,b,c,d,e,false); }
		public <A2> Quintet<A2,B,C,D,E> replaceA(A2 replacement){ return (Quintet<A2, B, C, D, E>) super.replaceAt(0, replacement); }
		public <B2> Quintet<A,B2,C,D,E> replaceB(B2 replacement){ return (Quintet<A, B2, C, D, E>) super.replaceAt(1, replacement); }
		public <C2> Quintet<A,B,C2,D,E> replaceC(C2 replacement){ return (Quintet<A, B, C2, D, E>) super.replaceAt(2, replacement); }
		public <D2> Quintet<A,B,C,D2,E> replaceD(D2 replacement){ return (Quintet<A, B, C, D2, E>) super.replaceAt(3, replacement); }
		public <E2> Quintet<A,B,C,D,E2> replaceE(E2 replacement){ return (Quintet<A, B, C, D, E2>) super.replaceAt(4, replacement); }
	}
	
	/** Homogenicly Typed Tuples **/
	
	public static class TVoid<T>              extends GVoid<T,ZERO,TVoid<T>>		         implements NTuple<T,ZERO>{
		protected TVoid()					    {super(true);}
	}
	public static class TUnit<T>              extends GUnit<T,T,ONE,TUnit<T>>				 implements NTuple<T,ONE>{
		protected TUnit(T t)					{super(t,true);}
	}
	public static class TPair<T>              extends GPair<T,T,T,TWO,TPair<T>> 		     implements NTuple<T,TWO>{
		protected TPair(T a,T b)                {super(a,b,true);}			
	}
	public static class TTriplet<T>           extends GTriplet<T,T,T,T,THREE,TTriplet<T>> 	 implements NTuple<T,THREE>{
		protected TTriplet(T a,T b,T c)         {super(a,b,c,true);}
	}
	public static class TQuartet<T>           extends GQuartet<T,T,T,T,T,FOUR,TQuartet<T>>   implements NTuple<T,FOUR>{
		protected TQuartet(T a,T b,T c,T d)     {super(a,b,c,d,true);}
	}
	public static class TQuintet<T>           extends GQuintet<T,T,T,T,T,T,FIVE,TQuintet<T>> implements NTuple<T,FIVE>{
		protected TQuintet(T a,T b,T c,T d,T e) {super(a,b,c,d,e,true);}
	}
}
/* Pair<Object,Object> != TPair<Object>: First doesn't allow set, second does not allow replace 
 * Pair<String,String> != TPair<String>: First is NTuple<Object> (cannot call set but replace), the later is NTuple<String> (right the opposite indications)
 */