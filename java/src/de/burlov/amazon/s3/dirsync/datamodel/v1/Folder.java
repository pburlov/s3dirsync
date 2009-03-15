/*
   Copyright 2008 Paul Burlov

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package de.burlov.amazon.s3.dirsync.datamodel.v1;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author paul
 * 
 */
public class Folder implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	static final private String LAST_MODIFIED = "LAST_MODIFIED";
	static final private String NAME = "NAME";
	static final private String STORAGE_ID = "STORAGE_ID";

	private HashMap<String, Object> properties = new HashMap<String, Object>(3);
	
	/*
	 * Nicht serialisierbare Index zum schnellen finden der FileInfo Objekten anhand der Hash Werte
	 */
	transient private HashMap<ByteArrayKey, FileInfo> fileHashIndex;
	
	/*
	 * Filename zu FileInfo Objekten
	 */
	private Map<String, FileInfo> indexData = new TreeMap<String, FileInfo>();

	public Folder(String name, String storageId)
	{
		super();
		properties.put(STORAGE_ID, storageId);
		properties.put(NAME, name);
	}

	public long getLastModified()
	{
		return (Long) properties.get(LAST_MODIFIED);
	}

	public void setLastModified(long lastModified)
	{
		properties.put(LAST_MODIFIED, lastModified);
	}

	public Map<String, FileInfo> getIndexData()
	{
		return indexData;
	}

	public String getName()
	{
		return (String) properties.get(NAME);
	}

	public String getStorageId()
	{
		return (String) properties.get(STORAGE_ID);
	}

	public void setIndexData(Map<String, FileInfo> indexData)
	{
		this.indexData = indexData;
	}

	/**
	 * Methode konstruiert Hash-Index aus vorhandenen FileInfo Objekte. Wird unmittelbar nach
	 * der Deserialisierung aufgerufen
	 */
	public void initFileHashIndex()
	{
		fileHashIndex = new HashMap<ByteArrayKey, FileInfo>();
		for(FileInfo info : indexData.values())
		{
			if(info.getHash() != null)
			{
			fileHashIndex.put(new ByteArrayKey(info.getHash()), info);
			}
		}
	}
	
	/**
	 * Synchronisiert Hashwerte Index-Konstrukt mit aktuellen Daten
	 * @return Set mit S3-Keys die nicht mehr referenziert werden und sollen aus S3 Storage geloescht werden
	 */
	public Set<String> syncFileHashIndex()
	{
		Map<ByteArrayKey, FileInfo> oldData = fileHashIndex;
		initFileHashIndex();
		HashSet<String> ret = new HashSet<String>();
		for(FileInfo info : oldData.values())
		{
			if(info.getHash() == null)
			{
				continue;
			}
			/*
			 * Alte Objekte durchgehen und nachsehen ob deren Hashwert nicht mehr referenziert wird
			 */
			if(!fileHashIndex.containsKey(new ByteArrayKey(info.getHash())))
			{
				ret.add(info.getStorageId());
			}
		}
		return ret;
	}
	
	public FileInfo getFileInfo(byte[] hash)
	{
		return fileHashIndex.get(new ByteArrayKey(hash));
	}
	
	public FileInfo getFileInfo(String name)
	{
		return indexData.get(name);
	}
}
