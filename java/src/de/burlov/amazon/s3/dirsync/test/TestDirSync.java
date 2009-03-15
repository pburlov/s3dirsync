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
package de.burlov.amazon.s3.dirsync.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import de.burlov.amazon.s3.S3Utils;
import de.burlov.amazon.s3.dirsync.CLI;
import de.burlov.amazon.s3.dirsync.DirSyncException;

public class TestDirSync
{
	static private String accessKey = "";
	static private String secretKey = "";
	static String uploadDir = "upload";
	static String downloadDir = "download";
	static
	{
		Logger.getLogger("").setLevel(Level.WARNING);
	}

	@Test
	public void testUpload() throws Exception, DirSyncException
	{
		S3Utils.deleteBucket(accessKey, secretKey, accessKey + ".dirsync");
		FileUtils.cleanDirectory(new File(uploadDir));
		createTestData(new File(uploadDir), 10, 10000);
		CLI.main(new String[] { "-snapshot", "up", "-localDir", uploadDir, "-remoteDir", "test", "-s3Key", accessKey, "-s3Secret", secretKey, "-password", "test" });

	}

	@Test
	public void testDownload() throws DirSyncException, IOException
	{
		CLI.main(new String[] { "-snapshot", "down", "-localDir", downloadDir, "-remoteDir", "test", "-s3Key", accessKey, "-s3Secret", secretKey, "-password", "test" });

		Assert.assertTrue(compareFolders(new File(uploadDir), new File(downloadDir)));
	}

	private boolean compareFolders(File dir1, File dir2) throws IOException
	{
		long crc1 = 0;
		for (File file : (Collection<File>) FileUtils.listFiles(dir1, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter()))
		{
			if (file.isFile())
			{
				crc1 += FileUtils.checksumCRC32(file);
			}
		}
		long crc2 = 0;
		for (File file : (Collection<File>) FileUtils.listFiles(dir2, FileFilterUtils.trueFileFilter(), FileFilterUtils.trueFileFilter()))
		{
			if (file.isFile())
			{
				crc2 += FileUtils.checksumCRC32(file);
			}
		}
		return crc1 == crc2;
	}

	private void createTestData(File baseDir, int fileAmount, int medianSize) throws IOException
	{
		for (int i = 0; i < fileAmount; i++)
		{
			int count = RandomUtils.nextInt(512);
			String filename = RandomStringUtils.random(count, "1234567890poiuytrewqasdfghjklmnbvcxz/");
			count = RandomUtils.nextInt(medianSize);
			byte[] data = new byte[count];
			new Random().nextBytes(data);
			File file = new File(baseDir, filename);
			file.getParentFile().mkdirs();

			OutputStream out = new FileOutputStream(file);
			out.write(data);
			out.close();
		}
	}
}
