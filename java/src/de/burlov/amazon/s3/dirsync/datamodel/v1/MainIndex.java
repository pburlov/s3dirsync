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
import java.util.HashMap;
import java.util.Map;

public class MainIndex implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/*
	 * Name wird zu Key gemappt
	 */
	private Map<String, String> folders = new HashMap<String, String>();
	private long sequence = 0;
	private byte[] encryptionKey;

	public byte[] getEncryptionKey()
	{
		return encryptionKey;
	}

	public void setEncryptionKey(byte[] encryptionKey)
	{
		this.encryptionKey = encryptionKey;
	}

	public Map<String, String> getFolders()
	{
		return folders;
	}

	public void setFolders(Map<String, String> folders)
	{
		this.folders = folders;
	}

	synchronized public long getNextId()
	{
		return ++sequence;
	}
}
