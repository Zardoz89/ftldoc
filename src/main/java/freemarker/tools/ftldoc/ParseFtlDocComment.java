/*
 * ParseFtlDocComment.java
 */
package freemarker.tools.ftldoc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper class to parse a Comment with FTLDoc anotations
 */
class ParseFtlDocComment
{
    private static final Pattern LINESPLIT_PATTERN = Pattern.compile("(\\r\\n)|(\\r)|(\\n)");

    private static final String SIMPLE_ARGNAME_REGEX = "(?:[a-zA-Z0-9_\\-]*)";
    private static final String OPTINAL_REGEX = "(?:\\[" + SIMPLE_ARGNAME_REGEX + "(?:=(.*))?\\])";
    private static final String JSDOC_PARAM_REGEX =
        "^\\s*(?:--)?\\s*@param\\s*(\\{[a-zA-Z0-9_,|<>]*\\})?\\s*(" + OPTINAL_REGEX + "|" + SIMPLE_ARGNAME_REGEX
            + ")(?:\\s|-)*(.*)$";

    // Regex that detects a JsDoc like @para, where group 1 it's Type , group 2 it's argument, group 3 is default value
    // and group 4 it's description. Examples :
    // @param {TypeExpresion} arg Description
    // @param {TypeExpresion} [arg=defVal] Description
    // @param [arg] Description
    private static final Pattern JSDOC_PARAM_PATTERN = Pattern.compile(JSDOC_PARAM_REGEX);
    // Regex that detects a "@param XXXX {YYYY} ZZZZ", where group 1 is XXXX , group 2 it's YYYY and group 3 its ZZZZ
    private static final Pattern PARAM_PATTERN =
        Pattern
            .compile("^\\s*(?:--)?\\s*@param\\s*(" + SIMPLE_ARGNAME_REGEX + ")\\s*(\\{[a-zA-Z0-9_,|<>]*\\})?\\s*(.*)$");
    // Regex that detects a "@return YYYY", where group 1 is @return and group 2 it's YYYY
    private static final Pattern RETURN = Pattern.compile("^\\s*(?:--)?\\s*(@return)\\s*(.*)$");
    // Regex that detects a "@deprecated YYYY", where group 1 is @deprecated and group 2 it's YYYY
    private static final Pattern DEPRECATED = Pattern.compile("^\\s*(?:--)?\\s*(@deprecated)\\s*(.*)$");
    // Regex that detects a "@XXXX YYYY", where group 1 is XXXX and group 2 it's YYYY
    private static final Pattern AT_PATTERN = Pattern.compile("^\\s*(?:--)?\\s*(@\\w+)\\s*(.*)$");
    // Regex that detects a text line
    private static final Pattern TEXT_PATTERN = Pattern.compile("^\\s*(?:--)?(.*)$");

    private static final String PARAM_KEYWORD = "@param";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String OPTIONAL = "optional";
    private static final String DEFAULT_VALUE = "def_val";
    private static final String DESCRIPTION = "description";
    private static final String SHORT_COMMENT = "short_comment";
    private static final String COMMENT = "comment";

    private ParseFtlDocComment()
    {
    }

    /**
     * Parses a string containg the comment text
     */
    static Map<String, Object> parse(String commentText)
    {
        // always return a hash, even if doesn't have any content
        if (StringUtils.isEmpty(commentText)) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, ParamInformation> paramsCache = new LinkedHashMap<>();

        Matcher m;
        // remove leading hyphen (last hyphen of '<#---')
        String fixedComment = commentText.substring(1);
        StringBuffer bufText = new StringBuffer();

        String[] lines = LINESPLIT_PATTERN.split(fixedComment);
        String line;

        String lastParamName = "";
        for (String line2 : lines) {
            line = line2;
            if ((m = JSDOC_PARAM_PATTERN.matcher(line)).matches()) {
                ParamInformation param = new ParamInformation();
                param.name = m.group(2);
                param.name = StringUtils.removeStart(param.name, "[");
                param.name = StringUtils.removeEnd(param.name, "]");
                if (StringUtils.isNotEmpty(param.name)) {
                    param.name = (StringUtils.split(param.name, '='))[0];
                }
                lastParamName = param.name;
                param.typeExpressions = getTypeExpressions(m.group(1));
                param.description = m.group(4);
                if (param.typeExpressions.isEmpty() && param.description.startsWith("{")) {
                    int closingIndex = param.description.indexOf("}");
                    if (closingIndex != -1) {
                        param.typeExpressions = getTypeExpressions(param.description.substring(0, closingIndex));
                        param.description = StringUtils.stripStart(param.description.substring(closingIndex), " }");
                    }
                }

                param.optional = StringUtils.startsWith(m.group(2), "[") && StringUtils.endsWith(m.group(2), "]");
                if (StringUtils.isNotEmpty(m.group(3))) {
                    param.defaultValue = m.group(3);
                }

                paramsCache.put(lastParamName, param);
            } else if ((m = PARAM_PATTERN.matcher(line)).matches()) {
                ParamInformation param = new ParamInformation();
                param.name = m.group(1);
                lastParamName = param.name;

                param.typeExpressions = getTypeExpressions(m.group(2));
                param.description = m.group(3);

                paramsCache.put(lastParamName, param);

            } else if ((m = RETURN.matcher(line)).matches()) {
                result.put(m.group(1), m.group(2));

            } else if ((m = DEPRECATED.matcher(line)).matches()) {
                result.put(m.group(1), m.group(2));

            } else if ((m = AT_PATTERN.matcher(line)).matches()) {
                String annotation = m.group(1);
                String value = m.group(2);
                List<String> previousValues = (List<String>)result.get(annotation);
                if (previousValues == null) {
                    previousValues = new ArrayList<>();
                }
                previousValues.add(value);
                result.put(annotation, previousValues);

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
                    ParamInformation param = paramsCache.get(lastParamName);
                    param.description += text;
                    paramsCache.put(lastParamName, param);

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

        List<Map<String, Object>> params = new ArrayList<>();
        for (ParamInformation param : paramsCache.values()) {
            params.add(param.toHash());
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

    private static List<String> getTypeExpressions(String exp)
    {
        return Arrays.asList(
            StringUtils.split(
                StringUtils.removeEnd(StringUtils.removeStart(StringUtils.defaultString(exp), "{"), "}"),
                '|'));
    }

    private static class ParamInformation
    {
        String name;
        List<String> typeExpressions = new ArrayList<>();
        boolean optional = false;
        String defaultValue;
        String description;

        Map<String, Object> toHash()
        {
            Map<String, Object> hash = new HashMap<>();
            hash.put(NAME, this.name);
            hash.put(DESCRIPTION, this.description);
            hash.put(OPTIONAL, this.optional);
            hash.put(DEFAULT_VALUE, this.defaultValue);

            List<String> typeExpressionsSequence = new ArrayList<>();
            for (String typeExpression : this.typeExpressions) {
                typeExpressionsSequence.add(typeExpression);
            }
            hash.put(TYPE, typeExpressionsSequence);

            return hash;
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder();
            builder.append("ParamInformation [");
            builder.append("name=").append(this.name).append(", ");
            if (!this.typeExpressions.isEmpty()) {
                builder.append("typeExpressions=");
                builder.append(this.typeExpressions);
                builder.append(", ");
            }
            if (this.optional) {
                builder.append("optional=");
                builder.append(this.optional);
                builder.append(", ");
            }
            if (this.defaultValue != null && !this.defaultValue.isEmpty()) {
                builder.append("defaultValue=");
                builder.append(this.defaultValue);
                builder.append(", ");
            }
            if (this.description != null && !this.description.isEmpty()) {
                builder.append("description=");
                builder.append(this.description);
            }
            builder.append("]");
            return builder.toString();
        }
    }

}
