package org.springboot.backend.retrieval;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.springboot.backend.rules.Chunk;
import org.springboot.backend.rules.RuleRepository;
import org.springframework.stereotype.Component;

/**
 * In-memory BM25 index (Lucene's default similarity) over the English rule chunks.
 * Each chunk is indexed with its rule title (boosted) and its text.
 */
@Component
public class RuleSearch {

    private static final Map<String, Float> FIELD_BOOSTS = Map.of("title", 2.0f, "text", 1.0f);

    private final Analyzer analyzer = new EnglishAnalyzer();
    private final IndexSearcher searcher;
    private final Map<String, Chunk> chunksById = new LinkedHashMap<>();

    public RuleSearch(RuleRepository rules) {
        Directory directory = new ByteBuffersDirectory();
        try (IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(analyzer))) {
            for (Chunk chunk : rules.chunks()) {
                Document doc = new Document();
                doc.add(new StringField("id", chunk.id(), Field.Store.YES));
                doc.add(new StringField("rule", chunk.rule(), Field.Store.NO));
                doc.add(new TextField("title", chunk.title(), Field.Store.NO));
                doc.add(new TextField("text", chunk.text(), Field.Store.NO));
                writer.addDocument(doc);
                chunksById.put(chunk.id(), chunk);
            }
            writer.commit();
            searcher = new IndexSearcher(DirectoryReader.open(directory));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not build the search index", e);
        }
    }

    /** Top chunks for free-text English search terms. */
    public List<Hit> search(String terms, int limit) {
        Query query = parse(terms);
        return query == null ? List.of() : run(query, limit);
    }

    /** Best chunks of one rule for the search terms. */
    public List<Hit> searchWithinRule(String rule, String terms, int limit) {
        BooleanQuery.Builder query = new BooleanQuery.Builder()
                .add(new TermQuery(new Term("rule", rule)), BooleanClause.Occur.FILTER);
        Query text = parse(terms);
        if (text != null) {
            query.add(text, BooleanClause.Occur.SHOULD);
        }
        return run(query.build(), limit);
    }

    private Query parse(String terms) {
        if (terms == null || terms.isBlank()) {
            return null;
        }
        MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"title", "text"}, analyzer, FIELD_BOOSTS);
        try {
            return parser.parse(QueryParser.escape(terms));
        } catch (ParseException e) {
            return null;
        }
    }

    private List<Hit> run(Query query, int limit) {
        try {
            List<Hit> hits = new ArrayList<>();
            for (ScoreDoc sd : searcher.search(query, limit).scoreDocs) {
                String id = searcher.storedFields().document(sd.doc).get("id");
                hits.add(new Hit(chunksById.get(id), sd.score));
            }
            return hits;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (IndexSearcher.TooManyClauses e) {
            return List.of();
        }
    }

    public record Hit(Chunk chunk, float score) {
    }
}
