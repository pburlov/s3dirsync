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
package de.burlov.crypt.test;

import java.io.IOException;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Test;

import de.burlov.crypt.CryptUtils;

/**
 * @author paul
 * 
 */
public class TestCryptUtils
{
	@Test
	public void testEncrypt() throws Exception
	{
		byte[] key = new byte[256 / 8];
		new Random().nextBytes(key);
		String cleartext = "This is a encryption test";
		byte[] ciphertext = CryptUtils.encrypt(cleartext.getBytes(), key);
		String decryptedText = new String(CryptUtils.decrypt(ciphertext, key));
		Assert.assertEquals(cleartext, decryptedText);
		new Random().nextBytes(key);
		try
		{
			decryptedText = new String(CryptUtils.decrypt(ciphertext, key));
			Assert.fail("Expected Exception was not thrown");
		} catch (IOException e)
		{

		}
		Assert.assertEquals(cleartext, decryptedText);
	}

}
