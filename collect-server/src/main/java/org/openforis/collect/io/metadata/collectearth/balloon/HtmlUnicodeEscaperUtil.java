package org.openforis.collect.io.metadata.collectearth.balloon;

import java.io.IOException;
import java.io.Writer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.CodePointTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;

public class HtmlUnicodeEscaperUtil extends CodePointTranslator  {

    private final int below;
    private final int above;
    private final boolean between;

    
    // Escapes both the HTML tags and the UTF-8 characters that can be problematic in a Google Earth Balloon (like for Lao language)
    public static String escapeForBalloon(String stringToEscape){
    	stringToEscape = StringEscapeUtils.escapeHtml4(stringToEscape);
    	return escapeHtmlUnicode( stringToEscape );
    }
    
    private static final CharSequenceTranslator ESCAPE_UNICODE = 
            new LookupTranslator().with(
            		HtmlUnicodeEscaperUtil.outsideOf(32, 0x7f) 
          );
    
    
    /**
     * <p>Constructs a <code>UnicodeEscaper</code> for all characters. </p>
     */
    private HtmlUnicodeEscaperUtil(){
        this(0, Integer.MAX_VALUE, true);
    }

    public static final String escapeHtmlUnicode(String input) {
        return ESCAPE_UNICODE.translate(input);
    }
    /**
     * <p>Constructs a <code>UnicodeEscaper</code> for the specified range. This is
     * the underlying method for the other constructors/builders. The <code>below</code>
     * and <code>above</code> boundaries are inclusive when <code>between</code> is
     * <code>true</code> and exclusive when it is <code>false</code>. </p>
     *
     * @param below int value representing the lowest codepoint boundary
     * @param above int value representing the highest codepoint boundary
     * @param between whether to escape between the boundaries or outside them
     */
    private HtmlUnicodeEscaperUtil(int below, int above, boolean between) {
        this.below = below;
        this.above = above;
        this.between = between;
    }

    /**
     * <p>Constructs a <code>UnicodeEscaper</code> below the specified value (exclusive). </p>
     *
     * @param codepoint below which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    private static HtmlUnicodeEscaperUtil below(int codepoint) {
        return outsideOf(codepoint, Integer.MAX_VALUE);
    }

    /**
     * <p>Constructs a <code>UnicodeEscaper</code> above the specified value (exclusive). </p>
     *
     * @param codepoint above which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    private static HtmlUnicodeEscaperUtil above(int codepoint) {
        return outsideOf(0, codepoint);
    }

    /**
     * <p>Constructs a <code>UnicodeEscaper</code> outside of the specified values (exclusive). </p>
     *
     * @param codepointLow below which to escape
     * @param codepointHigh above which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    private static HtmlUnicodeEscaperUtil outsideOf(int codepointLow, int codepointHigh) {
        return new HtmlUnicodeEscaperUtil(codepointLow, codepointHigh, false);
    }

    /**
     * <p>Constructs a <code>UnicodeEscaper</code> between the specified values (inclusive). </p>
     *
     * @param codepointLow above which to escape
     * @param codepointHigh below which to escape
     * @return the newly created {@code UnicodeEscaper} instance
     */
    private static HtmlUnicodeEscaperUtil between(int codepointLow, int codepointHigh) {
        return new HtmlUnicodeEscaperUtil(codepointLow, codepointHigh, true);
    }

    /**
     * {@inheritDoc}
     */
    public boolean translate(int codepoint, Writer out) throws IOException {
        if(between) {
            if (codepoint < below || codepoint > above) {
                return false;
            }
        } else {
            if (codepoint >= below && codepoint <= above) {
                return false;
            }
        }

        // TODO: Handle potential + sign per various Unicode escape implementations
        if (codepoint > 0xffff) {
            // TODO: Figure out what to do. Output as two Unicodes?
            //       Does this make this a Java-specific output class?
            out.write("&#x" + hex(codepoint));
        } else if (codepoint > 0xfff) {
            out.write("&#x" + hex(codepoint));
        } else if (codepoint > 0xff) {
            out.write("&#x0" + hex(codepoint));
        } else if (codepoint > 0xf) {
            out.write("&#x00" + hex(codepoint));
        } else {
            out.write("&#x000" + hex(codepoint));
        }
        return true;
    }
}
