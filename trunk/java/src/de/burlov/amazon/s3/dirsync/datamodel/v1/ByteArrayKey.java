/*
 * created on 22.12.2008 by paul
 */
/**
 * 
 */
package de.burlov.amazon.s3.dirsync.datamodel.v1;

import java.util.Arrays;

/**
 * Wrapper Klasse um ein Byte Array. Somit koennen Byte Arrays als Keys in einem Map verwendet
 * werden
 * 
 * @author paul
 * 
 */
class ByteArrayKey
{
	private byte[] key;
	
	/**
	 * @param key
	 */
	public ByteArrayKey(byte[] key)
	{
		super();
		this.key = key;
	}
	
	/**
	 * @author paul
	 * @return
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(key);
		return result;
	}
	
	/**
	 * @author paul
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ByteArrayKey other = (ByteArrayKey) obj;
		if (!Arrays.equals(key, other.key)) return false;
		return true;
	}
	
}