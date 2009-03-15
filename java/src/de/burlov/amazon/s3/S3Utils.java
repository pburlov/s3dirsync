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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.StringUtils;
import org.jets3t.service.S3Service;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.security.AWSCredentials;

/**
 * Klasse fuer grundlegende S3S Operationen
 * 
 * @author paul
 * 
 */
public class S3Utils
{
	private static final String DELETE = "delete";
	private static final String LIST = "list";

	public static void main(String[] args)
	{
		Options opts = new Options();
		OptionGroup gr = new OptionGroup();
		gr.setRequired(true);
		gr.addOption(new Option(LIST, false, ""));
		gr.addOption(new Option(DELETE, false, ""));

		opts.addOptionGroup(gr);

		opts.addOption(new Option("k", true, "Access key for AWS account"));
		opts.addOption(new Option("s", true, "Secret key for AWS account"));
		opts.addOption(new Option("b", true, "Bucket"));
		CommandLine cmd = null;
		try
		{
			cmd = new PosixParser().parse(opts, args);

			String accessKey = cmd.getOptionValue("k");
			if (StringUtils.isBlank(accessKey))
			{
				System.out.println("Missing amazon access key");
				return;
			}
			String secretKey = cmd.getOptionValue("s");
			if (StringUtils.isBlank(secretKey))
			{
				System.out.println("Missing secret key");
				return;
			}
			String bucket = cmd.getOptionValue("b");
			if (cmd.hasOption(LIST))
			{
				if (StringUtils.isBlank(bucket))
				{
					printBuckets(accessKey, secretKey);
				} else
				{
					printBucket(accessKey, secretKey, bucket);
				}
			} else if (cmd.hasOption(DELETE))
			{
				if (StringUtils.isBlank(bucket))
				{
					System.out.println("Bucket name required");
					return;
				}
				int count = deleteBucket(accessKey, secretKey, bucket);
				System.out.println("Deleted objects in bucket: " + count);
			}
		} catch (ParseException e)
		{
			System.out.println(e.getMessage());
			printUsage(opts);
			return;
		} catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}

	static final private void printUsage(Options opts)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(" ", opts, true);
	}

	static public void printBuckets(String accessKey, String secret) throws Exception
	{
		for (String b : S3Utils.listBuckets(accessKey, secret))
		{
			System.out.println(b);
		}

	}

	static public void printBucket(String accessKey, String secret, String bucket) throws Exception
	{
		int count = 0;
		for (String o : S3Utils.listObjects(accessKey, secret, bucket))
		{
			System.out.println(o);
			count++;
		}
		System.out.println(count + " entries");

	}

	static public List<String> listBuckets(String accessKey, String secret) throws Exception
	{
		S3Service s3s = createService(accessKey, secret);
		S3Bucket[] buckets = s3s.listAllBuckets();
		if (buckets == null)
		{
			return Collections.emptyList();
		}
		ArrayList<String> ret = new ArrayList<String>(buckets.length);
		for (S3Bucket bucket : buckets)
		{
			ret.add(bucket.getName());
		}
		return ret;

	}

	static public Iterable<String> listObjects(String accessKey, String secret, String bucket) throws Exception
	{
		S3Service s3s = createService(accessKey, secret);
		return new BucketIterator(bucket, s3s);

	}

	static public boolean bucketExists(String accessKey, String secret, String bucket) throws Exception
	{
		return bucketExistsIntern(createService(accessKey, secret), bucket);
	}

	static private boolean bucketExistsIntern(S3Service service, String bucket) throws Exception
	{
		for (S3Bucket b : service.listAllBuckets())
		{
			if (StringUtils.equals(b.getName(), bucket))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Loescht ein Bucket. Falls Bucket nicht leer ist, werden zuerst alle
	 * Objekte daraus geloescht.
	 * 
	 * @param accessKey
	 * @param secret
	 * @param bucket
	 * @return Anzahl der geloesche Objekte in Bucket
	 * @throws Exception
	 */
	static public int deleteBucket(String accessKey, String secret, String bucket) throws Exception
	{
		S3Service s3s = createService(accessKey, secret);
		int count = 0;
		if (!bucketExistsIntern(s3s, bucket))
		{
			return 0;
		}
		/*
		 * Zuerst alle Objekte aus einem Bucket loeschen falls welche vorhanden
		 * sind
		 */
		for (String o : new BucketIterator(bucket, s3s))
		{
			s3s.deleteObject(bucket, o);
			count++;
		}
		/*
		 * Erst wenn Bucket leer ist darf man ihn loeschen
		 */
		s3s.deleteBucket(bucket);
		return count;
	}

	static private S3Service createService(String accessKey, String secret) throws Exception
	{
		return new RestS3Service(new AWSCredentials(accessKey, secret));
	}

}
