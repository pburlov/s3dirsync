/*
 * Copyright 2008 Paul Burlov
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package de.burlov.amazon.s3.dirsync;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import de.burlov.amazon.s3.S3Utils;
import de.burlov.amazon.s3.dirsync.datamodel.v1.FileInfo;
import de.burlov.amazon.s3.dirsync.datamodel.v1.Folder;

/**
 * Command Line Interface Klasse
 * 
 * @author paul
 * 
 */
public class CLI
{
	static final private String CMD_LIST_BUCKETS = "listBuckets";
	static final private String CMD_LIST_BUCKET = "listBucket";
	static final private String CMD_DELETE_BUCKET = "deleteBucket";
	static final private String CMD_LIST_DIR = "listDir";
	static final private String CMD_HELP = "help";
	static final private String CMD_UPDATE = "update";
	static final private String CMD_SNAPSHOT = "snapshot";
	static final private String CMD_DELETE_DIR = "deleteDir";
	static final private String CMD_VERSION = "version";
	static final private String CMD_SUMMARY = "summary";
	static final private String CMD_CLEANUP = "cleanup";
	static final private String CMD_CHANGE_PASSWORD = "changePassword";
	static final private String OPT_S3S_KEY = "s3Key";
	static final private String OPT_S3S_SECRET = "s3Secret";
	static final private String OPT_BUCKET = "bucket";
	static final private String OPT_LOCAL_DIR = "localDir";
	static final private String OPT_REMOTE_DIR = "remoteDir";
	static final private String OPT_ENC_PASSWORD = "password";
	static final private String OPT_UP = "up";
	static final private String OPT_LOCATION = "bucketLocation";
	static final private String OPT_EXCLUDE_PATTERNS = "exclude";
	static final private String OPT_INCLUDE_PATTERNS = "include";
	
	/**
	 * @param args
	 */
	@SuppressWarnings("static-access")
	public static void main(String[] args)
	{
		Logger.getLogger("").setLevel(Level.OFF);
		Logger deLogger = Logger.getLogger("de");
		deLogger.setLevel(Level.INFO);
		Handler handler = new ConsoleHandler();
		handler.setFormatter(new VerySimpleFormatter());
		deLogger.addHandler(handler);
		deLogger.setUseParentHandlers(false);
		//		if (true)
		//		{
		//			LogFactory.getLog(CLI.class).error("test msg", new Exception("test extception"));
		//			return;
		//		}
		Options opts = new Options();
		OptionGroup gr = new OptionGroup();
		
		/*
		 * Befehlsgruppe initialisieren
		 */
		gr = new OptionGroup();
		gr.setRequired(true);
		gr.addOption(OptionBuilder.withArgName("up|down").hasArg().withDescription(
			"Upload/Download changed or new files").create(CMD_UPDATE));
		gr.addOption(OptionBuilder.withArgName("up|down").hasArg().withDescription(
			"Upload/Download directory snapshot").create(CMD_SNAPSHOT));
		gr.addOption(OptionBuilder.withDescription("Delete remote folder").create(CMD_DELETE_DIR));
		gr.addOption(OptionBuilder.withDescription("Delete a bucket").create(CMD_DELETE_BUCKET));
		gr.addOption(OptionBuilder.create(CMD_HELP));
		gr.addOption(OptionBuilder.create(CMD_VERSION));
		gr.addOption(OptionBuilder.withDescription("Prints summary for stored data").create(CMD_SUMMARY));
		gr.addOption(OptionBuilder.withDescription("Clean up orphaned objekts").create(CMD_CLEANUP));
		gr.addOption(OptionBuilder.withDescription("Changes encryption password").withArgName("new password").hasArg().create(
			CMD_CHANGE_PASSWORD));
		gr.addOption(OptionBuilder.withDescription("Lists all buckets").create(CMD_LIST_BUCKETS));
		gr.addOption(OptionBuilder.withDescription("Lists raw objects in a bucket").create(CMD_LIST_BUCKET));
		gr.addOption(OptionBuilder.withDescription("Lists files in remote folder").create(CMD_LIST_DIR));
		opts.addOptionGroup(gr);
		/*
		 * Parametergruppe initialisieren
		 */
		opts.addOption(OptionBuilder.withArgName("key").isRequired(false).hasArg().withDescription("S3 access key").create(
			OPT_S3S_KEY));
		opts.addOption(OptionBuilder.withArgName("secret").isRequired(false).hasArg().withDescription(
			"Secret key for S3 account").create(OPT_S3S_SECRET));
		opts.addOption(OptionBuilder.withArgName("bucket").isRequired(false).hasArg().withDescription(
			"Optional bucket name for storage. If not specified then an unique bucket name will be generated").create(
			OPT_BUCKET));
		// opts.addOption(OptionBuilder.withArgName("US|EU").hasArg().
		// withDescription(
		// "Where the new bucket should be created. Default US").create(
		// OPT_LOCATION));
		opts.addOption(OptionBuilder.withArgName("path").isRequired(false).hasArg().withDescription(
			"Local directory path").create(OPT_LOCAL_DIR));
		opts.addOption(OptionBuilder.withArgName("name").isRequired(false).hasArg().withDescription(
			"Remote directory name").create(OPT_REMOTE_DIR));
		opts.addOption(OptionBuilder.withArgName("password").isRequired(false).hasArg().withDescription(
			"Encryption password").create(OPT_ENC_PASSWORD));
		opts.addOption(OptionBuilder.withArgName("patterns").hasArgs().withDescription(
			"Comma separated exclude file patterns like '*.tmp,*/dir/*.tmp'").create(OPT_EXCLUDE_PATTERNS));
		opts.addOption(OptionBuilder.withArgName("patterns").hasArgs().withDescription(
			"Comma separated include patterns like '*.java'. If not specified, then all files in specified local directory will be included").create(
			OPT_INCLUDE_PATTERNS));
		
		if (args.length == 0)
		{
			printUsage(opts);
			return;
		}
		
		CommandLine cmd = null;
		try
		{
			cmd = new GnuParser().parse(opts, args);
			if (cmd.hasOption(CMD_HELP))
			{
				printUsage(opts);
				return;
			}
			if (cmd.hasOption(CMD_VERSION))
			{
				System.out.println("s3dirsync version " + Version.CURRENT_VERSION);
				return;
			}
			String awsKey = cmd.getOptionValue(OPT_S3S_KEY);
			String awsSecret = cmd.getOptionValue(OPT_S3S_SECRET);
			String bucket = cmd.getOptionValue(OPT_BUCKET);
			String bucketLocation = cmd.getOptionValue(OPT_LOCATION);
			String localDir = cmd.getOptionValue(OPT_LOCAL_DIR);
			String remoteDir = cmd.getOptionValue(OPT_REMOTE_DIR);
			String password = cmd.getOptionValue(OPT_ENC_PASSWORD);
			String exclude = cmd.getOptionValue(OPT_EXCLUDE_PATTERNS);
			String include = cmd.getOptionValue(OPT_INCLUDE_PATTERNS);
			
			if (StringUtils.isBlank(awsKey) || StringUtils.isBlank(awsSecret))
			{
				System.out.println("S3 account data required");
				return;
			}
			
			if (StringUtils.isBlank(bucket))
			{
				bucket = awsKey + ".dirsync";
			}
			
			if (cmd.hasOption(CMD_DELETE_BUCKET))
			{
				if (StringUtils.isBlank(bucket))
				{
					System.out.println("Bucket name required");
					return;
				}
				int deleted = S3Utils.deleteBucket(awsKey, awsSecret, bucket);
				System.out.println("Deleted objects: " + deleted);
				return;
			}
			if (cmd.hasOption(CMD_LIST_BUCKETS))
			{
				for (String str : S3Utils.listBuckets(awsKey, awsSecret))
				{
					System.out.println(str);
				}
				return;
			}
			if (cmd.hasOption(CMD_LIST_BUCKET))
			{
				if (StringUtils.isBlank(bucket))
				{
					System.out.println("Bucket name required");
					return;
				}
				for (String str : S3Utils.listObjects(awsKey, awsSecret, bucket))
				{
					System.out.println(str);
				}
				return;
			}
			if (StringUtils.isBlank(password))
			{
				System.out.println("Encryption password required");
				return;
			}
			char[] psw = password.toCharArray();
			DirSync ds = new DirSync(awsKey, awsSecret, bucket, bucketLocation, psw);
			ds.setExcludePatterns(parseSubargumenths(exclude));
			ds.setIncludePatterns(parseSubargumenths(include));
			if (cmd.hasOption(CMD_SUMMARY))
			{
				ds.printStorageSummary();
				return;
			}
			if (StringUtils.isBlank(remoteDir))
			{
				System.out.println("Remote directory name required");
				return;
			}
			if (cmd.hasOption(CMD_DELETE_DIR))
			{
				ds.deleteFolder(remoteDir);
				return;
			}
			if (cmd.hasOption(CMD_LIST_DIR))
			{
				Folder folder = ds.getFolder(remoteDir);
				if (folder == null)
				{
					System.out.println("No such folder found: " + remoteDir);
					return;
				}
				for (Map.Entry<String, FileInfo> entry : folder.getIndexData().entrySet())
				{
					System.out.println(entry.getKey() + " ("
						+ FileUtils.byteCountToDisplaySize(entry.getValue().getLength()) + ")");
				}
				return;
			}
			if (cmd.hasOption(CMD_CLEANUP))
			{
				ds.cleanUp();
				return;
			}
			if (cmd.hasOption(CMD_CHANGE_PASSWORD))
			{
				String newPassword = cmd.getOptionValue(CMD_CHANGE_PASSWORD);
				if (StringUtils.isBlank(newPassword))
				{
					System.out.println("new password required");
					return;
				}
				char[] chars = newPassword.toCharArray();
				ds.changePassword(chars);
				newPassword = null;
				Arrays.fill(chars, ' ');
				return;
			}
			if (StringUtils.isBlank(localDir))
			{
				System.out.println(OPT_LOCAL_DIR + " argument required");
				return;
			}
			String direction = "";
			boolean up = false;
			boolean snapshot = false;
			if (StringUtils.isNotBlank(cmd.getOptionValue(CMD_UPDATE)))
			{
				direction = cmd.getOptionValue(CMD_UPDATE);
			}
			else if (StringUtils.isNotBlank(cmd.getOptionValue(CMD_SNAPSHOT)))
			{
				direction = cmd.getOptionValue(CMD_SNAPSHOT);
				snapshot = true;
			}
			if (StringUtils.isBlank(direction))
			{
				System.out.println("Operation direction required");
				return;
			}
			up = StringUtils.equalsIgnoreCase(OPT_UP, direction);
			File baseDir = new File(localDir);
			if (!baseDir.exists() && !baseDir.mkdirs())
			{
				System.out.println("Invalid local directory: " + baseDir.getAbsolutePath());
				return;
			}
			ds.syncFolder(baseDir, remoteDir, up, snapshot);
			
		}
		catch (DirSyncException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			System.out.println(e.getMessage());
			printUsage(opts);
			
		}
		catch (Exception e)
		{
			e.printStackTrace(System.err);
		}
	}
	
	static private void printUsage(Options opts)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("java -jar dirsync.jar ", opts, true);
	}
	
	static List<String> parseSubargumenths(String arg)
	{
		if (StringUtils.isBlank(arg))
		{
			return Collections.emptyList();
		}
		String[] subs = arg.split(",");
		return Arrays.asList(subs);
	}
}
