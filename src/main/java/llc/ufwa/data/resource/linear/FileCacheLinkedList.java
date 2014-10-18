package llc.ufwa.data.resource.linear;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;

import llc.ufwa.data.exception.FileCacheLinkedListException;
import llc.ufwa.data.exception.ResourceException;
import llc.ufwa.data.resource.InputStreamConverter;
import llc.ufwa.data.resource.ReverseConverter;
import llc.ufwa.data.resource.SerializingConverter;
import llc.ufwa.data.resource.cache.Cache;
import llc.ufwa.data.resource.cache.FileHashCache;
import llc.ufwa.data.resource.cache.ValueConvertingCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LinkedList which is persisted in a cache instead of memory
 * 
 * @author Chase Adams
 */
public class FileCacheLinkedList<E> implements List<E> { //TODO fully implement this implements Collection<T>, List<T>, Serializable {

	/*
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(FileCacheLinkedList.class);

	/*
	 * Cache used to store values
	 * 
	 * TODO change DNode to hold the E value instead of doing 2 different cache objects. Use serialization for DNode instead of a string representation/parsing
	 * 
	 */
	final Cache<String, E> cache;
	final Cache<String, String> cacheLinkedData;

	/*
	 * String constants used in cache
	 */
	final String TOP_KEY = "TOP_KEY";
	final String BOTTOM_KEY = "BOTTOM_KEY";
	final String LINKED_KEY = "LINKED_KEY_";

	/*
	 * Variables used for key assignment
	 */
	final Random random;

	public FileCacheLinkedList(final File rootFolder) throws ResourceException {

		random = new Random();

		final File persistRoot = new File(rootFolder, "listPersisted");

		persistRoot.mkdirs();

		if (!persistRoot.isDirectory()) {
			throw new IllegalArgumentException("persist root must be a folder");
		}

		final File dataFolder = new File(persistRoot, "data");
		final File tempFolder = new File(persistRoot, "temp");

		final FileHashCache diskCache = new FileHashCache(dataFolder, tempFolder);

		cache = new ValueConvertingCache<String, E, byte[]>(
				new ValueConvertingCache<String, byte[], InputStream>(diskCache,
						new ReverseConverter<byte[], InputStream>(new InputStreamConverter())),
				new SerializingConverter<E>());

		final File persistRootData = new File(rootFolder, "listDataPersisted");

		persistRootData.mkdirs();

		if (!persistRootData.isDirectory()) {
			throw new IllegalArgumentException("persist data root must be a folder");
		}

		final File dataFolder2 = new File(persistRootData, "data");
		final File tempFolder2 = new File(persistRootData, "temp");

		final FileHashCache diskDataCache = new FileHashCache(dataFolder2, tempFolder2);

		cacheLinkedData = new ValueConvertingCache<String, String, byte[]>(
				new ValueConvertingCache<String, byte[], InputStream>(diskDataCache,
						new ReverseConverter<byte[], InputStream>(new InputStreamConverter())),
				new SerializingConverter<String>());

		if (isEmpty()) {
			cacheLinkedData.put(TOP_KEY, (new DNode(null, BOTTOM_KEY)).toString());
			cacheLinkedData.put(BOTTOM_KEY, (new DNode(TOP_KEY, null)).toString());
		}

	}

	/**
	 * Generates a random key for a linked value.
	 * 
	 * TODO One key collision causes the entire thing to break. Lets change this to be an incrementing number persisted in the cache.
	 * 
	 * @return random linked key
	 */
	private String getRandomLinkedKey() {
		return (LINKED_KEY + String.valueOf(random.nextInt()));
	}

	/**
	 * Checks if the list is empty.
	 * 
	 * @return size is 0
	 */
	@Override
	public boolean isEmpty() {

		try {
			if (!cacheLinkedData.exists(TOP_KEY) || !cacheLinkedData.exists(BOTTOM_KEY)) {
				return true;
			}
		}
		catch (ResourceException e) {
			e.printStackTrace();
		}

		return (size() == 0);
	}

	/**
	 * Removes the value from the list under the specified key.
	 * 
	 * @param removeNode
	 * @return removed value
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 */
	private E remove(final String removeKey) throws ResourceException, FileCacheLinkedListException {

		logger.debug("removing key:" + removeKey);

		// get the value from the cache
		final E elem = cache.get(removeKey);

		// get the node to remove
		final DNode removeNode = getNode(removeKey);

		// get the previous node and its data
		final String prevNodeString = removeNode.getPrev();
		final DNode prevNode = getNode(prevNodeString);

		// get the next node and its data
		final String nextNodeString = removeNode.getNext();
		final DNode nextNode = getNode(nextNodeString);

		// remove the old key
		cache.remove(removeKey);
		cacheLinkedData.remove(removeKey);

		// Set the other references
		prevNode.setNext(nextNodeString);
		nextNode.setPrev(prevNodeString);

		// put them in the cache
		cacheLinkedData.put(nextNodeString, nextNode.toString());
		cacheLinkedData.put(prevNodeString, prevNode.toString());

		// return the stored element of the removeNode
		return elem;

	}

	/**
	 * Returns the first element in this list.
	 * 
	 * @return the first element in this list
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 * @throws NoSuchElementException
	 * if this list is empty
	 */
	public E getFirst() throws ResourceException, FileCacheLinkedListException {
		final DNode first = getNode(TOP_KEY);
		if (first.getNext() == null) {
			throw new NoSuchElementException();
		}
		else {
			return cache.get(first.getNext());
		}
	}

	/**
	 * Returns the last element in this list.
	 * 
	 * @return the last element in this list
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 * @throws NoSuchElementException
	 * if this list is empty
	 */
	public E getLast() throws ResourceException, FileCacheLinkedListException {
		final DNode last = getNode(BOTTOM_KEY);
		if (last.getPrev() == null) {
			throw new NoSuchElementException();
		}
		else {
			return cache.get(last.getPrev());
		}
	}

	/**
	 * @param key
	 * @return
	 * @throws FileCacheLinkedListException
	 * @throws ResourceException
	 */
	private DNode getNode(final String key) throws FileCacheLinkedListException, ResourceException {
		final String keyData = cacheLinkedData.get(key);
		logger.debug("key data for " + key + " = " + keyData);
		return (new DNode(keyData));
	}

	/**
	 * Removes and returns the first element from this list.
	 * 
	 * @return the first element from this list
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 * @throws NoSuchElementException
	 * if this list is empty
	 */
	public E removeFirst() throws ResourceException, FileCacheLinkedListException {
		final DNode tempNode = getNode(TOP_KEY);
		return remove(tempNode.getNext());
	}

	/**
	 * Removes and returns the last element from this list.
	 * 
	 * @return the last element from this list
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 * @throws NoSuchElementException
	 * if this list is empty
	 */
	public E removeLast() throws ResourceException, FileCacheLinkedListException {
		final DNode tempNode = getNode(BOTTOM_KEY);
		return remove(tempNode.getPrev());
	}

	/**
	 * Add an element before the refnode
	 * 
	 * @param referenceNode
	 * @param element
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 */
	private void addBefore(final String referenceNodeKey, final E element) throws FileCacheLinkedListException, ResourceException {

		final DNode referenceNode = getNode(referenceNodeKey);

		// get the previous node and its data
		final String prevNodeKey = referenceNode.getPrev();
		final DNode prevNode = getNode(prevNodeKey);

		// create new values for added value
		final String addedNodeKey = getRandomLinkedKey();
		final DNode addedNode = new DNode(prevNodeKey, referenceNodeKey);

		// update references
		referenceNode.setPrev(addedNodeKey);
		prevNode.setNext(addedNodeKey);

		// put the changes values back into the data cache
		cacheLinkedData.put(referenceNodeKey, referenceNode.toString());
		cacheLinkedData.put(addedNodeKey, addedNode.toString());
		cacheLinkedData.put(prevNodeKey, prevNode.toString());

		// put the actual value into the cache
		cache.put(addedNodeKey, element);

	}

	/**
	 * Add an element after the refnode
	 * 
	 * @param referenceNode
	 * @param element
	 */
	private void addAfter(final String referenceNodeKey, final E element) throws FileCacheLinkedListException, ResourceException {

		final DNode referenceNode = getNode(referenceNodeKey);

		// get the next node and its data
		final String nextNodeKey = referenceNode.getNext();
		final DNode nextNode = getNode(nextNodeKey);

		// create new values for added value
		final String addedNodeKey = getRandomLinkedKey();
		final DNode addedNode = new DNode(referenceNodeKey, nextNodeKey);

		// update references
		referenceNode.setNext(addedNodeKey);
		nextNode.setPrev(addedNodeKey);

		// put the changes values back into the data cache
		cacheLinkedData.put(referenceNodeKey, referenceNode.toString());
		cacheLinkedData.put(addedNodeKey, addedNode.toString());
		cacheLinkedData.put(nextNodeKey, nextNode.toString());

		// put the actual value into the cache
		cache.put(addedNodeKey, element);

	}

	/**
	 * Inserts the specified element at the beginning of this list.
	 * 
	 * @param e
	 * the element to add
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 */
	public void addFirst(final E e) throws ResourceException, FileCacheLinkedListException {
		addAfter(TOP_KEY, e);
	}

	/**
	 * Appends the specified element to the end of this list.
	 * <p>
	 * This method is equivalent to {@link #add}.
	 * 
	 * @param e
	 * the element to add
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 */
	public void addLast(final E e) throws ResourceException, FileCacheLinkedListException {
		addBefore(BOTTOM_KEY, e);
	}

	/**
	 * Inserts the specified element at the specified position in this list.
	 * Shifts the element currently at that position (if any) and any subsequent
	 * elements to the right (adds one to their indices).
	 * 
	 * @param index
	 * index at which the specified element is to be inserted
	 * @param e
	 * element to be inserted
	 * @throws IndexOutOfBoundsException
	 * {@inheritDoc}
	 */
	@Override
	public void add(final int index, final E el) {

		final int size = size();

		if (index > size) {
			throw new IndexOutOfBoundsException("Index is larger than size");
		}

		int location;

		if (index < (size / 2)) {

			location = 0;

			try {

				DNode node = getNode(TOP_KEY);

				while ((location != index) && (!node.getNext().equals(BOTTOM_KEY))) {

					location++;
					node = getNode(node.getNext());

				}

				addBefore(node.getNext(), el);

			}
			catch (final ResourceException e) {
				e.printStackTrace();
			}
			catch (final FileCacheLinkedListException e) {
				e.printStackTrace(); //TODO you need to use logger instead of e.printstack trace. You also need to bubble the exception out, throw a runtimeException of some sort.
			}

		}
		else {

			location = size;

			try {

				DNode node = getNode(BOTTOM_KEY);

				while ((location != index) && (!node.getPrev().equals(TOP_KEY))) {

					location--;
					node = getNode(node.getPrev());

				}

				addAfter(node.getPrev(), el);

			}
			catch (final ResourceException e) {
				e.printStackTrace();
			}
			catch (final FileCacheLinkedListException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Appends the specified element to the end of this list.
	 * <p>
	 * This method is equivalent to {@link #addLast}.
	 * 
	 * @param e
	 * element to be appended to this list
	 * @return <tt>true</tt> (as specified by {@link Collection#add})
	 */
	@Override
	public boolean add(final E e) {

		try {
			addLast(e);
		}
		catch (final ResourceException e1) {
		    //TODO Never swallow an exception without at least logging it.

			return false;
		}
		catch (final FileCacheLinkedListException e1) {

			return false;
		}

		return true;

	}

	/**
	 * Removes the first occurrence of the specified element from this list, if
	 * it is present. If this list does not contain the element, it is
	 * unchanged. More formally, removes the element with the lowest index
	 * <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>
	 * (if such an element exists). Returns <tt>true</tt> if this list contained
	 * the specified element (or equivalently, if this list changed as a result
	 * of the call).
	 * 
	 * @param o
	 * element to be removed from this list, if present
	 * @return <tt>true</tt> if this list contained the specified element
	 */
	@Override
	public boolean remove(final Object o) {

		try {

			DNode node = getNode(TOP_KEY);
			if (node.getNext() == null) {
				return false;
			}

			E element = cache.get(node.getNext());

			while (!o.equals(element) && (!node.getNext().equals(BOTTOM_KEY))) {

				node = getNode(node.getNext());
				element = cache.get(node.getNext());

			}

			return o.equals(remove(node.getNext()));

		}
		catch (final ResourceException e) {
			e.printStackTrace();
		}
		catch (final FileCacheLinkedListException e) {
			e.printStackTrace();
		}

		return false;

	}

	/**
	 * Removes all of the elements from this list.
	 */
	@Override
	public void clear() {

		try {
			cache.clear();
			cacheLinkedData.clear();
			cacheLinkedData.put(TOP_KEY, (new DNode(null, BOTTOM_KEY)).toString());
			cacheLinkedData.put(BOTTOM_KEY, (new DNode(TOP_KEY, null)).toString());
		}
		catch (final ResourceException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Retrieves, but does not remove, the head (first element) of this list.
	 * 
	 * @return the head of this list, or <tt>null</tt> if this list is empty
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 * @since 1.5
	 */
	public E peek() throws ResourceException, FileCacheLinkedListException {
		if (isEmpty()) {
			return null;
		}
		return getFirst();
	}

	/**
	 * Retrieves, but does not remove, the head (first element) of this list.
	 * 
	 * @return the head of this list
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 * @throws NoSuchElementException
	 * if this list is empty
	 * @since 1.5
	 */
	public E element() throws ResourceException, FileCacheLinkedListException {
		return getFirst();
	}

	/**
	 * Retrieves and removes the head (first element) of this list
	 * 
	 * @return the head of this list, or <tt>null</tt> if this list is empty
	 * @throws FileCacheLinkedListException
	 * @throws ResourceException
	 * @since 1.5
	 */
	public E poll() throws ResourceException, FileCacheLinkedListException {
		if (isEmpty()) {
			return null;
		}
		return removeFirst();
	}

	/**
	 * Retrieves and removes the head (first element) of this list.
	 * 
	 * @return the head of this list
	 * @throws FileCacheLinkedListException
	 * @throws ResourceException
	 * @throws NoSuchElementException
	 * if this list is empty
	 * @since 1.5
	 */
	public E remove() throws ResourceException, FileCacheLinkedListException {
		return removeFirst();
	}

	/**
	 * Adds the specified element as the tail (last element) of this list.
	 * 
	 * @param e
	 * the element to add
	 * @return <tt>true</tt> (as specified by {@link Queue#offer})
	 * @since 1.5
	 */
	public boolean offer(final E e) {
		return add(e);
	}

	/**
	 * Inserts the specified element at the front of this list.
	 * 
	 * @param e
	 * the element to insert
	 * @return <tt>true</tt> (as specified by {@link Deque#offerFirst})
	 * @throws FileCacheLinkedListException
	 * @throws ResourceException
	 * @since 1.6
	 */
	public boolean offerFirst(final E e) throws ResourceException, FileCacheLinkedListException {
		addFirst(e);
		return true;
	}

	/**
	 * Inserts the specified element at the end of this list.
	 * 
	 * @param e
	 * the element to insert
	 * @return <tt>true</tt> (as specified by {@link Deque#offerLast})
	 * @throws FileCacheLinkedListException
	 * @throws ResourceException
	 * @since 1.6
	 */
	public boolean offerLast(final E e) throws ResourceException, FileCacheLinkedListException {
		addLast(e);
		return true;
	}

	/**
	 * Retrieves, but does not remove, the first element of this list, or
	 * returns <tt>null</tt> if this list is empty.
	 * 
	 * @return the first element of this list, or <tt>null</tt> if this list is
	 * empty
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 * @since 1.6
	 */
	public E peekFirst() throws ResourceException, FileCacheLinkedListException {
		if (isEmpty()) {
			return null;
		}
		return getFirst();
	}

	/**
	 * Retrieves, but does not remove, the last element of this list, or returns
	 * <tt>null</tt> if this list is empty.
	 * 
	 * @return the last element of this list, or <tt>null</tt> if this list is
	 * empty
	 * @throws ResourceException
	 * @throws FileCacheLinkedListException
	 * @since 1.6
	 */
	public E peekLast() throws ResourceException, FileCacheLinkedListException {
		if (isEmpty()) {
			return null;
		}
		return getLast();
	}

	/**
	 * Retrieves and removes the first element of this list, or returns
	 * <tt>null</tt> if this list is empty.
	 * 
	 * @return the first element of this list, or <tt>null</tt> if this list is
	 * empty
	 * @throws FileCacheLinkedListException
	 * @throws ResourceException
	 * @since 1.6
	 */
	public E pollFirst() throws ResourceException, FileCacheLinkedListException {
		if (isEmpty()) {
			return null;
		}
		return removeFirst();
	}

	/**
	 * Retrieves and removes the last element of this list, or returns
	 * <tt>null</tt> if this list is empty.
	 * 
	 * @return the last element of this list, or <tt>null</tt> if this list is
	 * empty
	 * @throws FileCacheLinkedListException
	 * @throws ResourceException
	 * @since 1.6
	 */
	public E pollLast() throws ResourceException, FileCacheLinkedListException {
		if (isEmpty()) {
			return null;
		}
		return removeLast();
	}

	/**
	 * Pushes an element onto the stack represented by this list. In other
	 * words, inserts the element at the front of this list.
	 * <p>
	 * This method is equivalent to {@link #addFirst}.
	 * 
	 * @param e
	 * the element to push
	 * @throws FileCacheLinkedListException
	 * @throws ResourceException
	 * @since 1.6
	 */
	public void push(final E e) throws ResourceException, FileCacheLinkedListException {
		addFirst(e);
	}

	/**
	 * Pops an element from the stack represented by this list. In other words,
	 * removes and returns the first element of this list.
	 * <p>
	 * This method is equivalent to {@link #removeFirst()}.
	 * 
	 * @return the element at the front of this list (which is the top of the
	 * stack represented by this list)
	 * @throws FileCacheLinkedListException
	 * @throws ResourceException
	 * @throws NoSuchElementException
	 * if this list is empty
	 * @since 1.6
	 */
	public E pop() throws ResourceException, FileCacheLinkedListException {
		return removeFirst();
	}

	/**
	 * Removes the first occurrence of the specified element in this list (when
	 * traversing the list from head to tail). If the list does not contain the
	 * element, it is unchanged.
	 * 
	 * @param o
	 * element to be removed from this list, if present
	 * @return <tt>true</tt> if the list contained the specified element
	 * @since 1.6
	 */
	public boolean removeFirstOccurrence(final Object o) {
		return remove(o);
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence (from first to last element).
	 * <p>
	 * The returned array will be "safe" in that no references to it are
	 * maintained by this list. (In other words, this method must allocate a new
	 * array). The caller is thus free to modify the returned array.
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 * 
	 * @return an array containing all of the elements in this list in proper
	 * sequence
	 */
	@Override
	public Object[] toArray() {

		final Object[] result = new Object[size()];

		int x = 0;

		try {

			DNode node = getNode(TOP_KEY);

			if (node.getNext() == null) {
				return result;
			}

			E element = cache.get(node.getNext());

			while (!node.getNext().equals(BOTTOM_KEY)) {

				result[x] = element;

				node = getNode(node.getNext());
				element = cache.get(node.getNext());

				x++;

			}

		}
		catch (final ResourceException e) {
			e.printStackTrace();
		}
		catch (final FileCacheLinkedListException e) {
			e.printStackTrace();
		}

		return result;

	}

	/**
	 * Appends all of the elements in the specified collection to the end of
	 * this list, in the order that they are returned by the specified
	 * collection's iterator. The behavior of this operation is undefined if the
	 * specified collection is modified while the operation is in progress.
	 * (Note that this will occur if the specified collection is this list, and
	 * it's nonempty.)
	 * 
	 * @param c
	 * collection containing elements to be added to this list
	 * @return <tt>true</tt> if this list changed as a result of the call
	 * @throws NullPointerException
	 * if the specified collection is null
	 */
	@Override
	public boolean addAll(final Collection<? extends E> c) {
		return addAll(size(), c);
	}

	/**
	 * Inserts all of the elements in the specified collection into this list,
	 * starting at the specified position. Shifts the element currently at that
	 * position (if any) and any subsequent elements to the right (increases
	 * their indices). The new elements will appear in the list in the order
	 * that they are returned by the specified collection's iterator.
	 * 
	 * @param index
	 * index at which to insert the first element from the specified collection
	 * @param c
	 * collection containing elements to be added to this list
	 * @return <tt>true</tt> if this list changed as a result of the call
	 * @throws IndexOutOfBoundsException
	 * {@inheritDoc}
	 * @throws NullPointerException
	 * if the specified collection is null
	 */
	@Override
	public boolean addAll(int index, final Collection<? extends E> c) {
		if ((index < 0) || (index > size())) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
		}

		int x = index;

		for (final E elem : c) {
			add(x, elem);
			x++;
		}

		return false;
	}

	/**
	 * Returns <tt>true</tt> if this list contains the specified element. More
	 * formally, returns <tt>true</tt> if and only if this list contains at
	 * least one element <tt>e</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
	 * 
	 * @param o
	 * element whose presence in this list is to be tested
	 * @return <tt>true</tt> if this list contains the specified element
	 */
	@Override
	public boolean contains(final Object o) {
		return indexOf(o) != -1;
	}

	/**
	 * Returns the element at the specified position in this list.
	 * 
	 * @param index
	 * index of the element to return
	 * @return the element at the specified position in this list
	 * @throws IndexOutOfBoundsException
	 * {@inheritDoc}
	 */
	@Override
	public E get(final int index) {

		int x = 0;

		try {

			DNode node = getNode(TOP_KEY);

			E element = cache.get(node.getNext());

			while ((x != index) && !node.getNext().equals(BOTTOM_KEY)) {

				node = getNode(node.getNext());
				element = cache.get(node.getNext());

				x++;

			}

			return element;

		}
		catch (final ResourceException e) {
			e.printStackTrace();
		}
		catch (final FileCacheLinkedListException e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * Returns the index of the first occurrence of the specified element in
	 * this list, or -1 if this list does not contain the element. More
	 * formally, returns the lowest index <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
	 * or -1 if there is no such index.
	 * 
	 * @param o
	 * element to search for
	 * @return the index of the first occurrence of the specified element in
	 * this list, or -1 if this list does not contain the element
	 */
	@Override
	public int indexOf(final Object o) {

		int location = 0;

		try {

			DNode node = getNode(TOP_KEY);
			if (node.getNext() == null) {
				return -1;
			}

			E element = cache.get(node.getNext());

			while (!node.getNext().equals(BOTTOM_KEY)) {

				if (o.equals(element)) {
					return location;
				}

				location++;

				logger.debug("next node = " + node.getNext());

				node = getNode(node.getNext());
				element = cache.get(node.getNext());

				logger.debug("next node = " + node.getNext() + ", element = " + element);

			}

		}
		catch (final ResourceException e) {
			e.printStackTrace();
			return -1;
		}
		catch (final FileCacheLinkedListException e) {
			e.printStackTrace();
			return -1;
		}

		return -1;

	}

	@Override
	public int lastIndexOf(final Object o) {

		int location = size() - 1;

		try {

			DNode node = getNode(BOTTOM_KEY);
			if (node.getPrev() == null) {
				return -1;
			}

			E element = cache.get(node.getPrev());

			while (!node.getNext().equals(TOP_KEY)) {

				if (o.equals(element)) {
					return location;
				}

				location--;

				logger.debug("prev node = " + node.getPrev());

				node = getNode(node.getPrev());
				element = cache.get(node.getPrev());

				logger.debug("prev node = " + node.getPrev() + ", element = " + element);

			}

		}
		catch (final ResourceException e) {
			e.printStackTrace();
			return -1;
		}
		catch (final FileCacheLinkedListException e) {
			e.printStackTrace();
			return -1;
		}

		return 0;

	}

	/**
	 * Removes all of the objects in the collection.
	 * 
	 * @param c
	 * collection of the elements to remove
	 * @return if all the elements were removed
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {
		for (final Object o : c) {
			if (!remove(o)) {
				return false;
			}
		}
		return false;
	}

	/**
	 * Replaces the element at the specified position in this list with the
	 * specified element.
	 * 
	 * @param index
	 * index of the element to replace
	 * @param element
	 * element to be stored at the specified position
	 * @return the element previously at the specified position
	 * @throws IndexOutOfBoundsException
	 * {@inheritDoc}
	 */
	@Override
	public E set(final int index, final E element) {
		final E prev = remove(index);
		add(index, element);
		return prev;
	}

	/**
	 * Returns the number of elements in this list.
	 * 
	 * @return the number of elements in this list
	 */
	@Override
	public int size() {

		int size = 0;

		try {

			DNode node = getNode(TOP_KEY);
			if (node.getNext() == null) {
				return -1;
			}

			E element = cache.get(node.getNext());

			while (!node.getNext().equals(BOTTOM_KEY)) {

				size++;

				logger.debug("next node = " + node.getNext());

				node = getNode(node.getNext());
				element = cache.get(node.getNext());

				logger.debug("next node = " + node.getNext() + ", element = " + element);

			}

		}
		catch (final ResourceException e) {
			e.printStackTrace();
		}
		catch (final FileCacheLinkedListException e) {
			e.printStackTrace();
		}

		return size;

	}

	/**
	 * Gets the sublist from the start and end indices.
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @return sublist from the specified indices
	 */
	@Override
	public List<E> subList(final int fromIndex, final int toIndex) {
		final List<E> list = new LinkedList<E>();

		for (int x = fromIndex; x < toIndex; x++) {
			list.add(get(x));
		}

		return list;
	}

	/**
	 * Checks if all the values in the collection are contained in the list.
	 * 
	 * @param c
	 * the collection to check
	 * @return if all values are contained
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {
		for (final Object o : c) {
			if (!contains(o)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns an array containing all of the elements in this list in proper
	 * sequence (from first to last element); the runtime type of the returned
	 * array is that of the specified array. If the list fits in the specified
	 * array, it is returned therein. Otherwise, a new array is allocated with
	 * the runtime type of the specified array and the size of this list.
	 * <p>
	 * If the list fits in the specified array with room to spare (i.e., the
	 * array has more elements than the list), the element in the array
	 * immediately following the end of the list is set to <tt>null</tt>. (This
	 * is useful in determining the length of the list <i>only</i> if the caller
	 * knows that the list does not contain any null elements.)
	 * <p>
	 * Like the {@link #toArray()} method, this method acts as bridge between
	 * array-based and collection-based APIs. Further, this method allows
	 * precise control over the runtime type of the output array, and may, under
	 * certain circumstances, be used to save allocation costs.
	 * <p>
	 * 
	 * @param a
	 * the array into which the elements of the list are to be stored, if it is
	 * big enough; otherwise, a new array of the same runtime type is allocated
	 * for this purpose.
	 * @return an array containing the elements of the list
	 * @throws ArrayStoreException
	 * if the runtime type of the specified array is not a supertype of the
	 * runtime type of every element in this list
	 * @throws NullPointerException
	 * if the specified array is null
	 */
	@Override
	public <T> T[] toArray(final T[] a) {

		final int size = size();

		if (a.length < size) {
			return (T[]) Arrays.copyOf(toArray(), size, a.getClass());
		}
		System.arraycopy(toArray(), 0, a, 0, size);
		if (a.length > size) {
			a[size] = null;
		}
		return a;

	}

	/**
	 * Returns a list-iterator of the elements in this list (in proper
	 * sequence), starting at the specified position in the list. Obeys the
	 * general contract of <tt>List.listIterator(int)</tt>.
	 * <p>
	 * The list-iterator is <i>fail-fast</i>: if the list is structurally
	 * modified at any time after the Iterator is created, in any way except
	 * through the list-iterator's own <tt>remove</tt> or <tt>add</tt> methods,
	 * the list-iterator will throw a <tt>ConcurrentModificationException</tt>.
	 * Thus, in the face of concurrent modification, the iterator fails quickly
	 * and cleanly, rather than risking arbitrary, non-deterministic behavior at
	 * an undetermined time in the future.
	 * 
	 * @param index
	 * index of the first element to be returned from the list-iterator (by a
	 * call to <tt>next</tt>)
	 * @return a ListIterator of the elements in this list (in proper sequence),
	 * starting at the specified position in the list
	 * @throws IndexOutOfBoundsException
	 * {@inheritDoc}
	 * @see List#listIterator(int)
	 */
	@Override
	public ListIterator<E> listIterator(final int index) {
		return new ListItr(index);
	}

	private class ListItr implements ListIterator<E> {

		final int size;
		private DNode lastReturned;
		private DNode next;
		private int nextIndex;

		private ListItr(final int index) {

			size = size();

			try {

				lastReturned = getNode(TOP_KEY);

				if ((index < 0) || (index > size)) {
					throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
				}

				if (index < (size >> 1)) {

					next = getNode(getNode(TOP_KEY).getNext());
					for (nextIndex = 0; nextIndex < index; nextIndex++) {
						next = getNode(next.getNext());
					}

				}
				else {

					next = getNode(TOP_KEY);

					for (nextIndex = size; nextIndex > index; nextIndex--) {
						next = getNode(next.getNext());
					}

				}

			}
			catch (FileCacheLinkedListException e) {
				e.printStackTrace();
			}
			catch (ResourceException e) {
				e.printStackTrace();
			}

		}

		@Override
		public boolean hasNext() {
			return nextIndex != size;
		}

		@Override
		public E next() {

			if (nextIndex == size) {
				throw new NoSuchElementException();
			}

			try {
				lastReturned = next;
				next = getNode(next.getNext());
				nextIndex++;

				return cache.get(next.getPrev());
			}
			catch (final ResourceException e) {
				e.printStackTrace();
			}
			catch (final FileCacheLinkedListException e) {
				e.printStackTrace();
			}

			return null;

		}

		@Override
		public boolean hasPrevious() {
			return nextIndex != 0;
		}

		@Override
		public E previous() {

			if (nextIndex == 0) {
				throw new NoSuchElementException();
			}

			try {
				lastReturned = next;
				next = getNode(next.getPrev());
				nextIndex--;

				return cache.get(next.getNext());
			}
			catch (final ResourceException e) {
				e.printStackTrace();
			}
			catch (final FileCacheLinkedListException e) {
				e.printStackTrace();
			}

			return null;

		}

		@Override
		public int nextIndex() {
			return nextIndex;
		}

		@Override
		public int previousIndex() {
			return nextIndex - 1;
		}

		@Override
		public void remove() {

			try {

				final DNode lastNext = getNode(lastReturned.getNext());
				try {
					FileCacheLinkedList.this.remove(lastReturned);
				}
				catch (final NoSuchElementException e) {
					throw new IllegalStateException();
				}
				if (next == lastReturned) {
					next = lastNext;
				}
				else {
					nextIndex--;
				}
				lastReturned = getNode(TOP_KEY);

			}
			catch (final FileCacheLinkedListException e1) {
				e1.printStackTrace();
			}
			catch (final ResourceException e1) {
				e1.printStackTrace();
			}

		}

		@Override
		public void add(final E e) {

			try {
				lastReturned = getNode(TOP_KEY);
				addAfter(next.getPrev(), e);
				nextIndex++;
			}
			catch (final FileCacheLinkedListException e1) {
				e1.printStackTrace();
			}
			catch (final ResourceException e1) {
				e1.printStackTrace();
			}

		}

		@Override
		public void set(final E e) {

			try {

				if (lastReturned == getNode(TOP_KEY)) {
					throw new IllegalStateException();
				}

				addBefore(lastReturned.getNext(), e);

			}
			catch (final FileCacheLinkedListException e1) {
				e1.printStackTrace();
			}
			catch (final ResourceException e1) {
				e1.printStackTrace();
			}

		}

	}

	/**
	 * @since 1.6
	 */
	public Iterator<E> descendingIterator() {
		return new DescendingIterator();
	}

	/**
	 * Adapter to provide descending iterators via ListItr.previous
	 */
	private class DescendingIterator implements Iterator<E> {

		final ListItr itr = new ListItr(size());

		public boolean hasNext() {
			return itr.hasPrevious();
		}

		public E next() {
			return itr.previous();
		}

		public void remove() {
			itr.remove();
		}
	}

	@Override
	public Iterator<E> iterator() {
		return new ListItr(0);
	}

	@Override
	public ListIterator<E> listIterator() {
		return new ListItr(0);
	}

	/**
	 * Retains only the elements in this list that are contained in the
	 * specified collection. In other words, removes from this list all of its
	 * elements that are not contained in the specified collection.
	 * 
	 * @param c
	 * collection containing elements to be retained in this list
	 * @return {@code true} if this list changed as a result of the call
	 * @throws ClassCastException
	 * if the class of an element of this list is incompatible with the
	 * specified collection (optional)
	 * @throws NullPointerException
	 * if this list contains a null element and the specified collection does
	 * not permit null elements (optional), or if the specified collection is
	 * null
	 */
	@SuppressWarnings ("unchecked")
	@Override
	public boolean retainAll(final Collection<?> c) {

		if (!containsAll(c)) {
			return false;
		}

		clear();
		addAll((Collection<? extends E>) c);

		return false;

	}

	/**
	 * Removes a value based on its index.
	 * 
	 * @param index
	 * index of value to be removed
	 * @return element removed
	 */
	@Override
	public E remove(int index) {

		int x = 0;

		try {

			DNode node = getNode(TOP_KEY);

			while (!(index == x) && (!node.getNext().equals(BOTTOM_KEY))) {

				node = getNode(node.getNext());
				x++;

			}

			return remove(node.getNext());

		}
		catch (final ResourceException e) {
			e.printStackTrace();
		}
		catch (final FileCacheLinkedListException e) {
			e.printStackTrace();
		}

		return null;

	}

	/**
	 * Returns the String representation of the list.
	 * 
	 * @return String representation of list
	 */
	@Override
	public String toString() {

		final StringBuilder str = new StringBuilder();

		try {

			DNode node = getNode(TOP_KEY);

			while (!node.getNext().equals(BOTTOM_KEY)) {

				str.append(cache.get(node.getNext()) + ";");
				node = getNode(node.getNext());

			}

		}
		catch (final ResourceException e) {
			e.printStackTrace();
		}
		catch (final FileCacheLinkedListException e) {
			e.printStackTrace();
		}

		return str.toString();

	}

}