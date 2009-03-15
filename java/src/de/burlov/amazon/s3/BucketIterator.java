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
package de.burlov.amazon.s3;

import java.util.Iterator;

import org.jets3t.service.S3ObjectsChunk;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Object;

/**
 * Klasse dient zur iterativer Auflistung OBjekte in einem Bucket. Liste der
 * Objekte wird nicht gleicht vollstaendig geladen sondern in mehreren Teilen,
 * daher die Klasse zur Auflistung groessen Buckets geeignet
 * 
 * @author paul
 * 
 */
class BucketIterator implements Iterable<String>, Iterator<String>
{
	private static final int CHUNK_SIZE = 500;
	private S3Service s3Service;
	private String bucketName;
	private S3ObjectsChunk chunk;
	private int nextIndex;

	public BucketIterator(String bucketName, S3Service service)
	{
		super();
		this.bucketName = bucketName;
		s3Service = service;
	}

	private boolean hasNextImpl() throws S3ServiceException
	{
		if (chunk != null && chunk.getObjects().length < 1)
		{
			return false;
		}
		if (chunk == null || nextIndex >= chunk.getObjects().length)
		{
			nextChunk();
		}
		return chunk.getObjects().length > 0;
	}

	private void nextChunk() throws S3ServiceException
	{
		String lastKey = null;
		if (chunk != null && chunk.getObjects().length > 0)
		{
			lastKey = chunk.getObjects()[chunk.getObjects().length - 1].getKey();
		}
		chunk = s3Service.listObjectsChunked(bucketName, null, null, CHUNK_SIZE, lastKey);
		nextIndex = 0;
	}

	/**
	 * @author paul
	 * @return
	 */
	public boolean hasNext()
	{
		try
		{
			return hasNextImpl();
		} catch (S3ServiceException e)
		{
			throw new RuntimeException(e);
		}
	}

	/**
	 * @author paul
	 * @return
	 */
	public String next()
	{
		if (!hasNext())
		{
			throw new IllegalStateException("No more elements");
		}
		S3Object obj = chunk.getObjects()[nextIndex++];
		if (obj == null)
		{
			return null;
		}
		return obj.getKey();
	}

	/**
	 * @author paul
	 */
	public void remove()
	{
		// ignore
	}

	/**
	 * @author paul
	 * @return
	 */
	public Iterator<String> iterator()
	{
		return this;
	}

}
