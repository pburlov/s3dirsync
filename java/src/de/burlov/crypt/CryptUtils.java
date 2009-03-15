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
package de.burlov.crypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.engines.SerpentEngine;

import de.burlov.bouncycastle.io.CryptInputStream;
import de.burlov.bouncycastle.io.CryptOutputStream;

/**
 * @author paul
 * 
 */
public class CryptUtils
{
	/**
	 * Verschluesselt ein Datenblock mit Serpent Cipher in AEAD/EAX Modus.
	 * 
	 * @param cleartext
	 * @param key
	 * @return
	 */
	static public byte[] encrypt(byte[] cleartext, byte[] key)
	{
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		try
		{
			CryptOutputStream out = new CryptOutputStream(bout, new SerpentEngine(), key);
			out.write(cleartext);
			out.close();
			return bout.toByteArray();
		} catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	static public byte[] decrypt(byte[] ciphertext, byte[] key) throws IOException
	{
		CryptInputStream in = new CryptInputStream(new ByteArrayInputStream(ciphertext), new SerpentEngine(), key);
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		IOUtils.copy(in, bout);
		return bout.toByteArray();
	}

}
