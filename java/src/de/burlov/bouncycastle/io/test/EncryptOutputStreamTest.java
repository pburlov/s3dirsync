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
package de.burlov.bouncycastle.io.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.engines.SerpentEngine;
import org.bouncycastle.util.Arrays;
import org.junit.Test;

import de.burlov.bouncycastle.io.CryptInputStream;
import de.burlov.bouncycastle.io.CryptOutputStream;

/**
 * @author paul
 * 
 */
public class EncryptOutputStreamTest
{
	@Test
	public void testEncryptDecrypt() throws IOException
	{
		BlockCipher cipher = new SerpentEngine();
		Random rnd = new Random();
		byte[] key = new byte[256 / 8];
		rnd.nextBytes(key);
		byte[] iv = new byte[cipher.getBlockSize()];
		rnd.nextBytes(iv);
		byte[] data = new byte[1230000];
		new Random().nextBytes(data);

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		CryptOutputStream eout = new CryptOutputStream(bout, cipher, key);
		eout.write(data);
		eout.close();
		byte[] eData = bout.toByteArray();
		// eData[1000] = (byte) (eData[1000] & 0x88);
		ByteArrayInputStream bin = new ByteArrayInputStream(eData);
		CryptInputStream din = new CryptInputStream(bin, cipher, key);
		bout = new ByteArrayOutputStream();
		IOUtils.copy(din, bout);
		eData = bout.toByteArray();
		Assert.assertTrue(Arrays.areEqual(data, eData));

	}
}
