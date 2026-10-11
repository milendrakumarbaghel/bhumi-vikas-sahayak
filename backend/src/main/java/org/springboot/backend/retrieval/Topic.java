package org.springboot.backend.retrieval;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Topics a clerk asks about, with the rules that always belong in the context for that topic.
 * Rule lists were chosen from the rule titles and checked against the rule text.
 * A reference may name a sub-rule, e.g. "2(5)(c)".
 */
public enum Topic {

    SETBACK("Open spaces / setbacks / margins around a building",
            List.of("56", "57", "42"), "front rear side open space setback margin MOS",
            "\\b(setbacks?|open spaces?|margins?|gap|mos)\\b|खाली|जगह"),
    FAR("Floor area ratio, total built-up area, number of floors",
            List.of("61", "42"), "floor area ratio FAR built up area",
            "(?<!how )\\b(far|floor area|floors?|storeys?)\\b|मंजिल|एफएआर"), // not "how far"
    HEIGHT("Maximum building height",
            List.of("65", "56", "42"), "height limit building height",
            "\\b(height|tall|taller)\\b|ऊंचाई|ऊँचाई"),
    FEES("Application and permission fees",
            List.of("21"), "fees permission fee application fee built up area",
            "\\b(fees?|charges?)\\b|फीस|शुल्क"),
    PARKING("Parking", List.of("84", "63"), "parking space",
            "\\bparking\\b|पार्किंग"),
    FARM_HOUSE("Farm house on agricultural land", List.of("17"), "farm house agricultural plot FAR",
            "\\bfarm ?houses?\\b|फार्म ?हाउस"),
    VALIDITY("How long a permission is valid; renewal / revalidation",
            List.of("23"), "duration of sanction valid revalidation renewal",
            "\\b(valid|validity|renew|renewal|revalidat\\w*|expire[sd]?)\\b|वैध|नवीनीकरण"),
    DEEMED_SANCTION("Authority does not reply in time; deemed sanction; instant approval",
            List.of("28", "12-A"), "limitation period refusal deemed sanctioned",
            "\\b(not replied|no reply|deadline|deemed|miss(es|ed)?|fails? to)\\b|जवाब नहीं|समय सीमा"),
    ELECTRIC_LINES("Distance from overhead electric lines",
            List.of("52"), "distance from electric lines overhead voltage vertical horizontal",
            "\\b(electric\\w*|voltage|power lines?)\\b|बिजली"),
    PLAN_SIGNING("Who may prepare, sign and submit plans",
            List.of("18", "26"), "preparation and signing of plans architect engineer licensed",
            "\\b(sign|signs|signed|signing|architects?|engineers?|submit my (building )?plans)\\b|हस्ताक्षर"),
    APPLICATION_DOCUMENTS("Documents and information with an application",
            List.of("16", "14"), "information accompanying application documents",
            "\\b(documents?|papers?|accompany)\\b|दस्तावेज"),
    PLAN_CHANGE("Changing a sanctioned plan; deviation; alteration",
            List.of("24", "19"), "deviation from sanctioned plan alteration modification",
            "\\b(change (my |the )?(sanctioned )?plan|after (it is )?sanctioned|deviat\\w*|alterations?|modif\\w*)\\b|नक्शा बदल"),
    LIFTS("Lifts and escalators", List.of("83"), "provision of lifts escalators",
            "\\b(lifts?|elevators?|escalators?)\\b|लिफ्ट"),
    DEVELOPMENT_PLAN("Development Plan versus these rules",
            List.of("103"), "provisions of development plan take precedence contrary",
            "\\b(development plan|master plan|disagree|conflicts?|contrary)\\b|विकास योजना"),
    DEVELOPMENT_PERMISSION("Whether development / building permission is needed and who grants it",
            List.of("2(5)(c)", "12"), "permission for development construction authority",
            "\\b(development permission|need (a )?(separate )?permission)\\b|अनुमति"),
    PLOT_SIZE("Minimum plot size and frontage", List.of("53"), "size of plots frontage",
            "\\b(plot size|minimum plot|frontage)\\b"),
    BALCONY_PROJECTIONS("Balconies and projections", List.of("58"), "projections balcony",
            "\\b(balcon\\w*|projections?)\\b|बालकनी"),
    BASEMENT("Basements", List.of("76"), "basement", "\\bbasements?\\b|तहखाना"),
    FIRE("Fire safety", List.of("87", "86"), "fire protection requirements",
            "\\bfire\\b|अग्नि");

    public final String description;
    /** Rules (optionally with sub-rule) to always include for this topic, most important first. */
    public final List<String> pinnedRules;
    /** English words added to the search query for this topic. */
    public final String searchWords;
    /** Recognises the topic in English or Hindi text (used when Claude is not available). */
    public final Pattern cue;

    Topic(String description, List<String> pinnedRules, String searchWords, String cueRegex) {
        this.description = description;
        this.pinnedRules = pinnedRules;
        this.searchWords = searchWords;
        this.cue = Pattern.compile(cueRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }
}
