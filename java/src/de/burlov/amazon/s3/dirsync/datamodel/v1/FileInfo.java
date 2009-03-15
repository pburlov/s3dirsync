/*
 * Copyright 2008 Paul Burlov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package de.burlov.amazon.s3.dirsync.datamodel.v1;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author paul
 * 
 */
public class FileInfo implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	
	static final private String STORAGE_ID = "STORAGE_ID";
	static final private String LAST_MODIFIED = "LAST_MODIFIED";
	static final private String LENGT = "LENGT";
	static final private String HASH = "HASH";
	private HashMap<String, Object> data = new HashMap<String, Object>(4);
	
	public FileInfo(long lastModified, long length, String storageId)
	{
		super();
		data.put(STORAGE_ID, storageId);
		data.put(LAST_MODIFIED, lastModified);
		data.put(LENGT, length);
	}
	
	public String getStorageId()
	{
		return (String) data.get(STORAGE_ID);
	}
	
	public long getLastModified()
	{
		return (Long) data.get(LAST_MODIFIED);
	}
	
	public void setLastModified(long value)
	{
		data.put(LAST_MODIFIED, value);
	}
	
	public long getLength()
	{
		return (Long) data.get(LENGT);
	}
	
	public byte[] getHash()
	{
		return (byte[]) data.get(HASH);
	}
	
	public void setHash(byte[] hash)
	{
		data.put(HASH, hash);
	}
	
}
