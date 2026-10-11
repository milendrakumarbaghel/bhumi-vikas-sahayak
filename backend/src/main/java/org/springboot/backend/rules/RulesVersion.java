package org.springboot.backend.rules;

/** Which version of the rules every answer comes from. */
public final class RulesVersion {

    public static final String VERSION = "MP Bhumi Vikas Rules 2012, consolidated to 6 March 2025";

    public static final String AMENDMENT_WARNING =
            "Rule 2(5)(c) was amended on 28 Aug 2026 (plots up to 500 sq m). That amendment is NOT in the "
                    + "loaded text. Check the latest notification before deciding.";

    public static final String AMENDMENT_WARNING_HI =
            "नियम 2(5)(c) में 28 अगस्त 2026 को संशोधन हुआ है (500 वर्ग मीटर तक के प्लॉट)। यह संशोधन लोड किए गए "
                    + "नियमों में नहीं है। निर्णय से पहले नवीनतम अधिसूचना देखें।";

    public static final String AMENDMENT_SOURCE =
            "https://teamleaseregtech.com/updates/article/59469/government-of-madhya-pradesh-amendment-to-land-development-rules";

    private RulesVersion() {
    }
}
