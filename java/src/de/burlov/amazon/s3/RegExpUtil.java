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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author paul
 * 
 */
public class RegExpUtil
{
	/*
	 * Pattern passt auf alle nicht alphanumeriche Zeichen mit Ausnahme von:
	 * '\?' und '\' und '*' und '?' original Form:
	 * (\\(?!\?|\)|(?<!\\)\?|(?<!\\)\
	 * |[\!\"\#\$\%\&\'\(\)\+\,\-\.\/\:\;\<\=\>\@\[\]\^\_\`\{\|\}\~])
	 */
	static final private Pattern SIMPLE_TO_JAVA_PATTERN = Pattern.compile("(\\\\(?!\\?|\\*)|[\\!\\\"\\#\\$\\%\\&\\\'\\(\\)\\+\\,\\-\\.\\/\\:\\;\\<\\=\\>\\@\\[\\]\\^\\_\\`\\{\\|\\}\\~])", Pattern.MULTILINE);

	static final private Pattern WILDCARD_CHAR_PATTERN = Pattern.compile("(?<!\\\\)\\*", Pattern.MULTILINE);

	static final private Pattern QUESTION_CHAR_PATTERN = Pattern.compile("(?<!\\\\)\\?", Pattern.MULTILINE);

	/**
	 * Methode konvertiert eine 'simple' RegExp mit '*' und '?' in Java RegExp
	 * konforme Form. '?' steht fuer genau ein Zeichen, '*' steht fuer keine
	 * oder mehrere Zeichen. Wenn '?' oder '*' Teil des suchbegriffes ist muss
	 * man mit '\' escapen.
	 * 
	 * 
	 * @param simpleRegexp
	 * @return Java RegExp conforme String Pattern
	 */
	static public String convertSimpleRegexpToJava(String simpleRegexp)
	{
		Matcher matcher = SIMPLE_TO_JAVA_PATTERN.matcher(simpleRegexp);
		/*
		 * Alle nicht alphanumerische Zeichen mit Ausnahme von '\?' und '\' und
		 * '*' und '?' mit '\' escapen
		 */
		simpleRegexp = matcher.replaceAll("\\\\$1");

		/*
		 * Jetzt nicht escaped '*' durch Java Regexp '.' ersetzen
		 */
		matcher = WILDCARD_CHAR_PATTERN.matcher(simpleRegexp);
		simpleRegexp = matcher.replaceAll(".*");

		/*
		 * Jetzt nicht escaped '?' durch Java Regexp '.' ersetzen
		 */
		matcher = QUESTION_CHAR_PATTERN.matcher(simpleRegexp);
		simpleRegexp = matcher.replaceAll(".");
		return simpleRegexp;
	}
}
