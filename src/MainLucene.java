/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * @author rnoronha
 */
public class MainLucene {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		Set stopWords = new HashSet();
		stopWords.add("the");
		stopWords.add("it");
		stopWords.add("is");

		File INDEX_DIR = new File("index");
		// "C:\\Documents and Settings\\rafinha\\Desktop\\nyt"

		IndexWriter writer = new IndexWriter(FSDirectory.open(INDEX_DIR), new StandardAnalyzer(Version.LUCENE_46),
				false, IndexWriter.MaxFieldLength.LIMITED);

		Document doc = new Document();

		doc.add(new Field("nome", "O senhor dos aneis.", Field.Store.YES, Field.Index.NOT_ANALYZED));

		doc.add(new Field("nome", "Harry potter e o cálice de fogo.", Field.Store.YES, Field.Index.NOT_ANALYZED));

		writer.addDocument(doc);

		String valueToBeSearched = "red";
		String index = "indexDir"; // dirotorio base do indice
		IndexReader reader = IndexReader.open(FSDirectory.open(new File(index)), true); // indexador
		Searcher searcher = new IndexSearcher(reader); // pesquisador
		QueryParser parser = new QueryParser(Version.LUCENE_CURRENT, field, analyzer); // transoformador
																						// do
																						// texto
																						// em
																						// uma
																						// query
		Query query = parser.parse(valueToBeSearched); // a consulta (query) em
														// si
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, false); // os
																							// melhores
																							// resultados
		searcher.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs; // o conjunto de
															// melhores
															// documentos para a
															// consulta

		int maximo = hits.length;
		Document doc = searcher.doc(hits[index].doc);
		String valor = doc.get("nomeDoCampo");
	}
}