package org.kleypas.muc.model

import java.util.regex.Pattern
import java.util.regex.Matcher

/**
 * What if we want to grab a tagged thing from the thing? Eh?
 *
 * <p>Honestly, i had no idea where to put this functionality, but you might
 * "want" something like this if you are dealing with a model and want to parse
 * its output in a quick, structured way.</p>
 */
class TagParser {
    private TagParser() {}

    public static Boolean extractBoolean(String line, String tagName) {
        final String regex = "<${Pattern.quote(tagName)}\\s*>\\s*(true|false)\\s*</${Pattern.quote(tagName)}>"
        final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE)

        final Matcher matcher = pattern.matcher(line)
        if (!matcher.find()) { return null }

        final String val = matcher.group(1)
        if ("true".equalsIgnoreCase(val))  return Boolean.TRUE
        if ("false".equalsIgnoreCase(val)) return Boolean.FALSE

        throw new IllegalArgumentException(
            "Malformed <$tagName> tag: unexpected value '${val}'")
    }

    public static String extractString(String line, String tagName) {
        if (! line) return null

        final String regex = "<\\s*${Pattern.quote(tagName)}\\s*>(.*?)</\\s*${Pattern.quote(tagName)}\\s*>"
        final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL)

        final Matcher matcher = pattern.matcher(line)
        return matcher.find() ? matcher.group(1).trim() : null
    }
}