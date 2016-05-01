package org.nightswimming.screener.util.tuple;

@SuppressWarnings("serial")
public class TupleException extends RuntimeException {

	public TupleException(String msg){super(msg);}
	public TupleException(String msg, Throwable t){super(msg, t);}
	
	public static class IndexOutOfTupleRangeException extends TupleException{
		public IndexOutOfTupleRangeException(int req, int size){ 
			super("Requested "+(req+1)+"th element is out of the bounds of this tuple's N ("+size+")");
		}
	}

	public static class MaxTupleSuuportedSizeException extends TupleException{
		public MaxTupleSuuportedSizeException(int max, Throwable t){ 
			super("Element couldn't be added because currently we don't support tuples of N > "+max+".", t);
		}
	}
	public static class TupleCopyingReflectionException extends TupleException{
		public TupleCopyingReflectionException(Throwable t){
			super("Problems with tuple's auto instantiation through reflection: ", t);
		}
	}

	public static class UnknownExpectedParamTypeException extends TupleException{
		public UnknownExpectedParamTypeException(int index, Object current){
			super("Tuple is not a homogenicly Typed Tuple so we cannot infer if a '"+current.getClass().getSimpleName()+"' parameter type is expected at index "+index+".");
		}
	}
	
	public static class TypeNotModifiableException extends TupleException{
		public TypeNotModifiableException(){
			super("Tuple is an homogenicly Typed Tuple so we cannot modify the type of any element.");
		}
	}
}
