package compiler;

import java.util.function.Supplier;

public class TypeResolver<T> {
	DataType resolving;
	T recovered = null;
	boolean caseMatched = false;
	public T get() {
		return recovered;
	}
	public TypeResolver(DataType resolveOn) {
		resolving = resolveOn;
	}
	public TypeResolver<T> CASE(DataType ifMatches, Callback cb) {
		if(ifMatches.equals(resolving)) {
			cb.call();
			caseMatched = true;
		}
		return this;
	}
	public TypeResolver<T> MULTI_CASE(DataType[] ifMatches, Supplier<T> evaluable) {
		for(DataType dt:ifMatches) {
			CASE(dt, evaluable);
		}
		return this;
	}
	public TypeResolver<T> MULTI_CASE(DataType[] ifMatches, Callback cb) {
		for(DataType dt:ifMatches) {
			CASE(dt, cb);
		}
		return this;
	}
	public TypeResolver<T> MULTI_CASE(DataType[] ifMatches, T evaluable) {
		for(DataType dt:ifMatches) {
			CASE(dt, evaluable);
		}
		return this;
	}
	public TypeResolver<T> CASE(DataType ifMatches, Supplier<T> evaluable) {
		if(ifMatches.equals(resolving)) {
			recovered = evaluable.get();
			caseMatched = true;
		}
		return this;
	}
	public TypeResolver<T> CASE(DataType ifMatches, T evaluable) {
		if(ifMatches.equals(resolving)) {
			recovered = evaluable;
			caseMatched = true;
		}
		return this;
	}
	public TypeResolver<T> CASE_THROW(DataType ifMatches, RuntimeException e) {
		if(ifMatches.equals(resolving)) {
			throw e;
		}
		return this;
	}
	public void DEFAULT(Callback cb) {
		if(!caseMatched)
			cb.call();
	}
	public TypeResolver<T> DEFAULT_THROW(RuntimeException e) {
		if(!caseMatched)
			throw e;
		return this;
	}
}
