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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.modes.AEADBlockCipher;
import org.bouncycastle.crypto.modes.EAXBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * @author paul
 * 
 */
public class CryptOutputStream extends OutputStream
{
	private OutputStream out;
	private AEADBlockCipher cipher;
	private byte[] buf = new byte[512];
	private boolean finalized = false;

	/**
	 * @throws IOException
	 */
	public CryptOutputStream(OutputStream out, BlockCipher cipher, byte[] key) throws IOException
	{
		if (out == null)
		{
			throw new NullPointerException("OutputStream is null");
		}
		this.out = out;
		byte[] iv = new byte[cipher.getBlockSize()];
		new Random().nextBytes(iv);
		this.cipher = new EAXBlockCipher(cipher);
		this.cipher.init(true, new ParametersWithIV(new KeyParameter(key), iv));
		out.write(iv);
	}

	/**
	 * @author paul
	 * @param b
	 * @throws IOException
	 */
	@Override
	public void write(int b) throws IOException
	{
		int count = cipher.processByte((byte) b, buf, 0);
		if (count > 0)
		{
			out.write(buf, 0, count);
		}
	}

	@Override
	public void close() throws IOException
	{
		doFinal();
		out.close();
	}

	private void doFinal() throws IOException
	{
		if (finalized)
		{
			return;
		}
		finalized = true;
		try
		{
			int count = cipher.doFinal(buf, 0);
			out.write(buf, 0, count);
		} catch (InvalidCipherTextException e)
		{
			/*
			 * Bei decrypt hat MAC nicht gestimmt
			 */
			throw new IOException(e.getLocalizedMessage());
		}
	}

	@Override
	public void flush() throws IOException
	{
		out.flush();
	}

}
