package com.example.securedcall.general;

import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class that allows simple communication between tasks using 
 * thread safe message queues.
 */
public class MessageQueueManager<K, M> {
	
	/**
	 * An hash table of all the active queues in the manager
	 */
	private Hashtable<K, LinkedBlockingQueue<M>> m_tblQueues;
	
	/**
	 * Ctor, initialize data members
	 */
	public MessageQueueManager()
	{
		m_tblQueues = new Hashtable<K, LinkedBlockingQueue<M>>();
	}
	
	public void registerQueue(K cIdentifier) throws MessageQueueManagerException
	{
		// Check if the given identifier exists in the queues table
		if (m_tblQueues.containsKey(cIdentifier))
		{
			throw new MessageQueueManagerException("Tried to register a queue with an existing key");
		}
		
		// Create a new queue for the provided identifier
		m_tblQueues.put(cIdentifier, new LinkedBlockingQueue<M>());
	}
	
	public void unregisterQueue(K cIdentifier) throws MessageQueueManagerException
	{
		// Check if the given identifier exists in the queues table
		if (!m_tblQueues.containsKey(cIdentifier))
		{
			throw new MessageQueueManagerException("Tried to unregister an inexisting queue");
		}
		
		// Remove the relevant queue
		m_tblQueues.remove(cIdentifier);
	}
	
	public void sendMessage(K cIdentifier, M cMsg) throws MessageQueueManagerException
	{
		// Check if the given identifier exists in the queues table
		if (!m_tblQueues.containsKey(cIdentifier))
		{
			throw new MessageQueueManagerException("Tried to send a message to an inexisting queue");
		}
		
		m_tblQueues.get(cIdentifier).offer(cMsg);
	}
	
	public M readMessage(K cIdentifier) throws MessageQueueManagerException, InterruptedException
	{
		// Check if the given identifier exists in the queues table
		if (!m_tblQueues.containsKey(cIdentifier))
		{
			throw new MessageQueueManagerException("Tried to read a message from an inexisting queue");
		}
		
		return m_tblQueues.get(cIdentifier).take();
	}
}

