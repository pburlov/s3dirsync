/*
 * created on 22.12.2008 by paul
 */
/**
 * 
 */
package de.burlov.amazon.s3.dirsync;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Formatter Klasse um nur eigentliche Log-Information auszugeben ohne zusatzinfos wie Zeit, Klassen
 * und Methodennamen, etc.
 * 
 * @author paul
 * 
 */
public class VerySimpleFormatter extends Formatter
{
	/**
	 * @author paul
	 * @param logRec
	 * @return
	 */
	@Override
	public String format(LogRecord logRec)
	{
		if (logRec.getThrown() != null)
		{
			StringWriter writer = new StringWriter();
			writer.write(logRec.getMessage());
			writer.append('\n');
			logRec.getThrown().printStackTrace(new PrintWriter(writer));
			writer.append('\n');
			return writer.toString();
		}
		else
		{
			return logRec.getMessage() + "\n";
		}
	}
	
}
