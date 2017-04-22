/*****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ****************************************************************/
/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package msi.gama.metamodel.topology;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Linked list implementation of the Deque interface (not available in Java 5, so not formally declared) where the link
 * pointers are tightly integrated with the element. Linked deques have no capacity restrictions; they grow as necessary
 * to support usage. They are not thread-safe; in the absence of external synchronization, they do not support
 * concurrent access by multiple threads. Null elements are prohibited.
 * <p>
 * Most <tt>LinkedDeque</tt> operations run in constant time by assuming that the {@link Linked} parameter is associated
 * with the deque instance. Any usage that violates this assumption will result in non-deterministic behavior.
 * <p>
 * The iterators returned by this class are <em>not</em> <i>fail-fast</i>: If the deque is modified at any time after
 * the iterator is created, the iterator will be in an unknown state. Thus, in the face of concurrent modification, the
 * iterator risks arbitrary, non-deterministic behavior at an undetermined time in the future.
 * 
 * @param <E>
 *            the type of elements held in this collection
 */
class LinkedDeque<E extends Linked<E>> extends AbstractCollection<E> {

	// This class provides a doubly-linked list that is optimized for the virtual
	// machine. The first and last elements are manipulated instead of a slightly
	// more convenient sentinel element to avoid the insertion of null checks with
	// NullPointerException throws in the byte code. The links to a removed
	// element are cleared to help a generational garbage collector if the
	// discarded elements inhabit more than one generation.

	/**
	 * Pointer to first node. Invariant: (first == null && last == null) || (first.prev == null)
	 */
	E first;

	/**
	 * Pointer to last node. Invariant: (first == null && last == null) || (last.next == null)
	 */
	E last;

	/**
	 * Links the element to the front of the deque so that it becomes the first element.
	 * 
	 * @param e
	 *            the unlinked element
	 */
	void linkFirst(final E e) {
		final E f = first;
		first = e;

		if (f == null) {
			last = e;
		} else {
			f.setPrevious(e);
			e.setNext(f);
		}
	}

	/**
	 * Links the element to the back of the deque so that it becomes the last element.
	 * 
	 * @param e
	 *            the unlinked element
	 */
	void linkLast(final E e) {
		final E l = last;
		last = e;

		if (l == null) {
			first = e;
		} else {
			l.setNext(e);
			e.setPrevious(l);
		}
	}

	/** Unlinks the non-null first element. */
	E unlinkFirst() {
		final E f = first;
		final E next = f.getNext();
		f.setNext(null);

		first = next;
		if (next == null) {
			last = null;
		} else {
			next.setPrevious(null);
		}
		return f;
	}

	/** Unlinks the non-null last element. */
	E unlinkLast() {
		final E l = last;
		final E prev = l.getPrevious();
		l.setPrevious(null);
		last = prev;
		if (prev == null) {
			first = null;
		} else {
			prev.setNext(null);
		}
		return l;
	}

	/** Unlinks the non-null element. */
	void unlink(final E e) {
		final E prev = e.getPrevious();
		final E next = e.getNext();

		if (prev == null) {
			first = next;
		} else {
			prev.setNext(next);
			e.setPrevious(null);
		}

		if (next == null) {
			last = prev;
		} else {
			next.setPrevious(prev);
			e.setNext(null);
		}
	}

	@Override
	public boolean isEmpty() {
		return first == null;
	}

	void checkNotEmpty() {
		if (isEmpty()) { throw new NoSuchElementException(); }
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Beware that, unlike in most collections, this method is <em>NOT</em> a constant-time operation.
	 */
	@Override
	public int size() {
		int size = 0;
		for (E e = first; e != null; e = e.getNext()) {
			size++;
		}
		return size;
	}

	@Override
	public void clear() {
		for (E e = first; e != null;) {
			final E next = e.getNext();
			e.setPrevious(null);
			e.setNext(null);
			e = next;
		}
		first = last = null;
	}

	@Override
	public boolean contains(final Object o) {
		return o instanceof Linked<?> && contains((Linked<?>) o);
	}

	// A fast-path containment check
	boolean contains(final Linked<?> e) {
		return e.getPrevious() != null || e.getNext() != null || e == first;
	}

	/**
	 * Moves the element to the front of the deque so that it becomes the first element.
	 * 
	 * @param e
	 *            the linked element
	 */
	public void moveToFront(final E e) {
		if (e != first) {
			unlink(e);
			linkFirst(e);
		}
	}

	/**
	 * Moves the element to the back of the deque so that it becomes the last element.
	 * 
	 * @param e
	 *            the linked element
	 */
	public void moveToBack(final E e) {
		if (e != last) {
			unlink(e);
			linkLast(e);
		}
	}

	public E peek() {
		return peekFirst();
	}

	public E peekFirst() {
		return first;
	}

	public E peekLast() {
		return last;
	}

	public E getFirst() {
		checkNotEmpty();
		return peekFirst();
	}

	public E getLast() {
		checkNotEmpty();
		return peekLast();
	}

	public E element() {
		return getFirst();
	}

	public boolean offer(final E e) {
		return offerLast(e);
	}

	public boolean offerFirst(final E e) {
		if (contains(e)) { return false; }
		linkFirst(e);
		return true;
	}

	public boolean offerLast(final E e) {
		if (contains(e)) { return false; }
		linkLast(e);
		return true;
	}

	@Override
	public boolean add(final E e) {
		return offerLast(e);
	}

	public void addFirst(final E e) {
		if (!offerFirst(e)) { throw new IllegalArgumentException(); }
	}

	public void addLast(final E e) {
		if (!offerLast(e)) { throw new IllegalArgumentException(); }
	}

	public E poll() {
		return pollFirst();
	}

	public E pollFirst() {
		if (isEmpty()) { return null; }
		return unlinkFirst();
	}

	public E pollLast() {
		if (isEmpty()) { return null; }
		return unlinkLast();
	}

	public E remove() {
		return removeFirst();
	}

	@Override
	@SuppressWarnings ("unchecked")
	public boolean remove(final Object o) {
		if (contains(o)) {
			unlink((E) o);
			return true;
		}
		return false;
	}

	public E removeFirst() {
		checkNotEmpty();
		return pollFirst();
	}

	public boolean removeFirstOccurrence(final Object o) {
		return remove(o);
	}

	public E removeLast() {
		checkNotEmpty();
		return pollLast();
	}

	public boolean removeLastOccurrence(final Object o) {
		return remove(o);
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		boolean modified = false;
		for (final Object o : c) {
			modified |= remove(o);
		}
		return modified;
	}

	public void push(final E e) {
		addFirst(e);
	}

	public E pop() {
		return removeFirst();
	}

	@Override
	public Iterator<E> iterator() {
		return new AbstractLinkedIterator(first) {

			@Override
			E computeNext() {
				return cursor.getNext();
			}
		};
	}

	public Iterator<E> descendingIterator() {
		return new AbstractLinkedIterator(last) {

			@Override
			E computeNext() {
				return cursor.getPrevious();
			}
		};
	}

	abstract class AbstractLinkedIterator implements Iterator<E> {

		E cursor;

		/**
		 * Creates an iterator that can can traverse the deque.
		 * 
		 * @param start
		 *            the initial element to begin traversal from
		 */
		AbstractLinkedIterator(final E start) {
			cursor = start;
		}

		@Override
		public boolean hasNext() {
			return cursor != null;
		}

		@Override
		public E next() {
			if (!hasNext()) { throw new NoSuchElementException(); }
			final E e = cursor;
			cursor = computeNext();
			return e;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

		/**
		 * Retrieves the next element to traverse to or <tt>null</tt> if there are no more elements.
		 */
		abstract E computeNext();
	}
}

/**
 * An element that is linked on the Deque.
 */
interface Linked<T extends Linked<T>> {

	/**
	 * Retrieves the previous element or <tt>null</tt> if either the element is unlinked or the first element on the
	 * deque.
	 */
	T getPrevious();

	/** Sets the previous element or <tt>null</tt> if there is no link. */
	void setPrevious(T prev);

	/**
	 * Retrieves the next element or <tt>null</tt> if either the element is unlinked or the last element on the deque.
	 */
	T getNext();

	/** Sets the next element or <tt>null</tt> if there is no link. */
	void setNext(T next);
}