package org.springboot.backend.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Locale;

/** Department terminology shared by offline intent extraction and PostgreSQL retrieval. */
public final class QueryLexicon {
    private QueryLexicon() {}
    private static final Map<String,String> HINDI = Map.ofEntries(
            Map.entry("सड़क","road street"), Map.entry("रोड","road street"), Map.entry("प्लॉट","plot"),
            Map.entry("भूखंड","plot"),Map.entry("ऊंचाई","height"),Map.entry("ऊँचाई","height"),
            Map.entry("मंजिल","storey height"),Map.entry("खाली","open space setback"),
            Map.entry("आगे","front open space"),Map.entry("सामने","front"),Map.entry("पीछे","rear open space"),
            Map.entry("बगल","side open space"),Map.entry("फीस","building permission fee"),
            Map.entry("शुल्क","building permission fee"),Map.entry("परमिशन","permission"),
            Map.entry("आवासीय","residential"),Map.entry("मकान","residential building"),
            Map.entry("घर","residential building"),Map.entry("फार्म","farm house agricultural"),
            Map.entry("वैधता","validity permission"),Map.entry("नवीनीकरण","revalidation permission"),
            Map.entry("बिजली","electric voltage line"),Map.entry("लिफ्ट","lifts"),
            Map.entry("दस्तावेज","application documents"),Map.entry("नक्शा","plans"),
            Map.entry("एफएआर","floor area ratio"),Map.entry("विकास योजना","development plan"));
    private static final Set<String> STOP = Set.of("the","and","for","with","what","which","how","does","from","that","this","have","will","want","when","much","more","than","can","are","not","my","you","your","was","its","into","given","should","shall","under","about","there");
    public static String expand(String query) {
        String text=query==null?"":query.toLowerCase(Locale.ROOT);
        StringBuilder result=new StringBuilder(text);
        HINDI.forEach((word,translation)-> { if(text.contains(word)) result.append(' ').append(translation); });
        return result.toString();
    }
    public static List<String> terms(String text) {
        var result=new LinkedHashSet<String>();
        for(String token:expand(text).split("[^\\p{L}\\p{M}\\p{N}()-]+"))
            if(token.length()>=3 && !STOP.contains(token)) result.add(token);
        return result.stream().limit(30).toList();
    }
    public static List<String> ruleHints(String query) {
        String text=expand(query);
        var refs=new LinkedHashSet<String>();
        if(text.matches("(?s).*(open space|setback|rear|front gap).*")) refs.add("56");
        if(text.matches("(?s).*(farm house|farmhouse).*")) refs.add("17");
        if(text.matches("(?s).*(valid|renew|revalidation).*")) refs.add("23");
        if(text.matches("(?s).*(not replied|deadline|deemed|miss.*deadline).*")) refs.add("28");
        if(text.matches("(?s).*(electric|voltage|overhead).*")) refs.add("52");
        if(text.matches("(?s).*(sign.*plan|submit.*plan|allowed.*sign).*")) {refs.add("18");refs.add("26");}
        if(text.matches("(?s).*(document.*application|application.*document).*")) {refs.add("16(1)");refs.add("16(11)");}
        if(text.matches("(?s).*(fee|fees).*")) refs.add("21");
        if(text.matches("(?s).*(change.*plan|deviation|plan.*sanctioned).*")) refs.add("24");
        if(text.matches("(?s).*(separate development|development permission.*plot|plot.*development permission).*")) {refs.add("2(5)(c)");refs.add("12");}
        if(text.matches("(?s).*(disagree|precedence|development plan.*rules).*")) refs.add("103");
        if(text.matches("(?s).*(lift|escalator).*")) refs.add("83");
        if(!text.matches("(?s).*(lift|escalator).*") && text.matches("(?s).*(permitted height|high.?rise|height|taller).*")) {refs.add("42");refs.add("2(38)");}
        return List.copyOf(refs);
    }
    public static boolean outsideCorpus(String query) {
        String text=expand(query);
        return text.matches("(?s).*(property tax.*online|pay.*property tax|market (rate|price).*land|land.*market (rate|price)).*");
    }
}
