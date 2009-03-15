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
package de.burlov.bouncycastle.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * 
 * @author paul
 * 
 */
public class CryptInputStream extends InputStream
{
	private AEADBlockCipher cipher;
	private byte[] processedBuf;
	private byte[] inputBuf = new byte[1024];
	private InputStream in;
	private int pos;
	private int length;
	private boolean finished = false;

	public CryptInputStream(InputStream in, BlockCipher cipher, byte[] key) throws IOException
	{
		byte[] iv = new byte[cipher.getBlockSize()];
		/*
		 * IV aus Stream lesen
		 */
		DataInputStream din = new DataInputStream(in);
		din.readFully(iv);
		this.in = in;
		this.cipher = new EAXBlockCipher(cipher);
		this.cipher.init(false, new ParametersWithIV(new KeyParameter(key), iv));
		processedBuf = new byte[inputBuf.length + cipher.getBlockSize() * 10];
	}

	/**
	 * @author paul
	 * @return
	 * @throws IOException
	 */
	@Override
	public int read() throws IOException
	{
		if (length < 0)
		{
			/*
			 * Keine Daten mehr
			 */
			return -1;
		}
		if (length == 0)
		{
			/*
			 * Interne Buffer ist leer. Neue Daten holen und verarbeiten
			 */
			processInput();
			return read();
		}
		length--;
		return processedBuf[pos++] & 0xFF;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException
	{
		if (length < 0)
		{
			/*
			 * Keine Daten mehr
			 */
			return -1;
		}
		if (length == 0)
		{
			/*
			 * Interne Buffer ist leer. Neue Daten holen und verarbeiten
			 */
			processInput();
			return read(b, off, len);
		}
		/*
		 * Verarbeitete Daten sind noch im Buffer vorhanden
		 */
		if (len > length)
		{
			len = length;
		}
		System.arraycopy(processedBuf, pos, b, off, len);
		length -= len;
		pos += len;
		return len;
	}

	private void processInput() throws IOException
	{
		if (length != 0)
		{
			return;
		}

		pos = 0;
		length = -1;
		while (length < 1)
		{
			if (finished)
			{
				return;
			}
			int readed = in.read(inputBuf);
			if (readed < 0)
			{
				/*
				 * Keine Daten mehr in InputStream. Prozess finalisieren
				 */
				try
				{
					finished = true;
					length = cipher.doFinal(processedBuf, 0);
					if (length < 1)
					{
						/*
						 * Alle Daten aus InputStream verarbeitet also auch -1
						 * zuruckgeben
						 */
						length = -1;
						return;
					}
				} catch (Exception e)
				{
					throw new IOException(e.getLocalizedMessage());
				}
			} else
			{
				length = cipher.processBytes(inputBuf, 0, readed, processedBuf, 0);
			}
		}
	}

	@Override
	public int available() throws IOException
	{
		return length < 0 ? 0 : length;
	}

	@Override
	public void close() throws IOException
	{
		in.close();
	}
}
