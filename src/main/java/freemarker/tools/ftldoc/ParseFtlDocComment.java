/*
 * ParseFtlDocComment.java
 */
package freemarker.tools.ftldoc;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;

/**
 * Helper class to parse a Comment with FTLDoc anotations
 */
class ParseFtlDocComment
{
    private static final Pattern LINESPLIT_PATTERN = Pattern.compile("(\\r\\n)|(\\r)|(\\n)");

    // Regex that detectes a "@param XXXX YYYY", where group 1 is XXXX and group 2 it's YYYY
    private static final Pattern PARAM_PATTERN = Pattern.compile("^\\s*(?:--)?\\s*@param\\s*(\\w*)\\s*(.*)$");
    // Regex that detectes a "@XXXX YYYY", where group 1 is XXXX and group 2 it's YYYY
    private static final Pattern AT_PATTERN = Pattern.compile("^\\s*(?:--)?\\s*(@\\w+)\\s*(.*)$");
    // Regex that detectes a text line
    private static final Pattern TEXT_PATTERN = Pattern.compile("^\\s*(?:--)?(.*)$");

    private static final String PARAM_KEYWORD = "@param";
    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private static final String SHORT_COMMENT = "short_comment";
    private static final String COMMENT = "comment";

    private ParseFtlDocComment()
    {
    }

    /**
     * Parses a string containg the comment text
     */
    static Map<String, Serializable> parse(String commentText)
    {
        // always return a hash, even if doesn't have any content
        if (StringUtils.isEmpty(commentText)) {
            return Collections.emptyMap();
        }

        Map<String, Serializable> result = new HashMap<>();
        Map<String, String> paramsCache = new HashMap<>();

        Matcher m;
        // remove leading hyphen (last hyphen of '<#---')
        String fixedComment = commentText.substring(1);
        StringBuffer bufText = new StringBuffer();

        String[] lines = LINESPLIT_PATTERN.split(fixedComment);
        String line;

        String lastParamName = "";
        for (String line2 : lines) {
            line = line2;
            if ((m = PARAM_PATTERN.matcher(line)).matches()) {
                lastParamName = m.group(1);
                paramsCache.put(lastParamName, m.group(2));

            } else if ((m = AT_PATTERN.matcher(line)).matches()) {
                result.put(m.group(1), m.group(2));

            } else if ((m = TEXT_PATTERN.matcher(line)).matches()) {
                String text;
                if (line.matches("^\\s+.*$")) {
                    // Line started with spaces, collapse them
                    // in a single one
                    text = " " + m.group(1);
                } else {
                    text = m.group(1);
                }
                text += "\n";
                if (lastParamName.length() > 0) {
                    // We are on a @param block. Append text to it.
                    String paramDescription = paramsCache.get(lastParamName);
                    paramDescription += text;
                    paramsCache.put(lastParamName, paramDescription);

                } else {
                    bufText.append(text);
                }

            } else {
                // one can prove (with some automat theory) that the
                // TEXT_PATTERN regex matches *every* string. Under normal
                // circumstances this else block can never be reached.
                System.err.println("WARNING: reached unreachable point: " + line);
            }
        }
        String text = bufText.toString().replaceAll("\n", "");

        SimpleSequence params = new SimpleSequence();
        for (Entry<String, String> paramEntry : paramsCache.entrySet()) {
            SimpleHash param = new SimpleHash();
            param.put(NAME, paramEntry.getKey());
            param.put(DESCRIPTION, paramEntry.getValue());
            params.add(param);
        }

        result.put(PARAM_KEYWORD, params);
        result.put(COMMENT, text);
        // extract first sentence for "Macro and Function Summary" table
        int endOfSentence = text.indexOf(".");
        if (endOfSentence > 0) {
            result.put(SHORT_COMMENT, text.substring(0, endOfSentence + 1));
        } else {
            result.put(SHORT_COMMENT, text);
        }

        return result;
    }

}
