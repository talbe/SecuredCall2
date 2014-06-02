package com.example.securedcall.general;

import java.util.LinkedList;

/**
 * An implementation of a bounded queue.
 * When a new item is added it is added to the end of the queue.
 * If the queue is full then the item in the head of the queue is removed
 * 
 * @param <T> The type of the item to store
 */
public class BoundedQueue<T> {
	private int m_nMaxItems;
	private LinkedList<T> m_lstItems;
	
	/**
	 * Ctor, initialize data members
	 * 
	 * @param nMaxItems Set the maximal number of items to store
	 */
	public BoundedQueue(int nMaxItems)
	{
		m_nMaxItems = nMaxItems;
		m_lstItems = new LinkedList<T>();
	}
	
	/**
	 * Get the head of the queue and remove it from the queue.
	 * 
	 * @return The item at the head of the queue
	 */
	public T dequeue()
	{
		return m_lstItems.removeFirst();
	}
	
	/**
	 * Enqueue an item to the queue.
	 * Note that if the queue is full items will be removed
	 * 
	 * @param cItem The item to enqueue into the collection.
	 */
	public void enqueue(T cItem)
	{
		// Make sure that we have room for the new item
		while (m_lstItems.size() >= m_nMaxItems)
		{
			m_lstItems.removeFirst();
		}
		
		m_lstItems.addLast(cItem);
	}
	
	/**
	 * Convert the collection of items to an array and return the result.
	 * 
	 * @return
	 */
	public T[] toArray(T[] arData)
	{
		return m_lstItems.toArray(arData);
	}
	
	public void clear()
	{
		m_lstItems.clear();
	}
	
	public int size()
	{
		return m_lstItems.size();
	}
	
	public boolean isFull()
	{
		return (size() == m_nMaxItems);
	}
}
