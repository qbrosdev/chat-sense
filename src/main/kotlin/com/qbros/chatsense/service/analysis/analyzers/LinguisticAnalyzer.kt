package com.qbros.chatsense.service.analysis.analyzers

import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import com.qbros.chatsense.service.analysis.model.SentenceAnalysis
import edu.stanford.nlp.pipeline.CoreDocument
import edu.stanford.nlp.pipeline.CoreSentence
import edu.stanford.nlp.pipeline.StanfordCoreNLP
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.apache.lucene.analysis.CharArraySet
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.util.*


@Order(0) //make sure this analyzer is added first in every collection of analyzers
@Component
class LinguisticAnalyzer : Analyzer {

    private val logger = KotlinLogging.logger {}
    private val scope = CoroutineScope(Dispatchers.Default)
    private val stopWords: CharArraySet = EnglishAnalyzer.getDefaultStopSet()

    /*  Part-of-Speech (POS)
        POS TagMeaning	    Example
        NN	Noun,singular	"cat"
        NNS	Noun,plural	    "dogs"
        JJ	Adjective	    "beautiful"
        VB	Verb,base	    "run"
        VBD	Verb,past	    "ran"    */
    private val nounTagPrefix = "NN"
    private val adjectiveTagPrefix = "JJ"

    /*
        tokenize	split text into words
        ssplit		split text into sentences
        pos			token.tag() for POS filtering
        lemma		token.lemma() for normalized words
        ner			token.ner() for named entities
        parse		required for sentiment tree
        sentiment	computes sentence-level sentiment */
    private val pipeline: StanfordCoreNLP by lazy {
        StanfordCoreNLP(Properties().apply {
            setProperty("annotators", "tokenize,ssplit,pos,lemma")
            setProperty("ner.useSUTime", "false")
        })
    }

    @PostConstruct
    fun preloadPipeline() {
        scope.launch {
            logger.info { "Initializing the analyzer" }
            pipeline
            logger.info { "Completed initializing the analyzer" }
        }
    }

    @PreDestroy
    fun close() {
        scope.cancel()
    }

    override fun process(analysis: CommentAnalysis): CommentAnalysis {
        val doc = CoreDocument(analysis.text)
        pipeline.annotate(doc)
        logger.debug { "annotate completed" }
        val sentencesAnalysis = doc.sentences()
            .map { processSentence(it) }
            .toList()

        val words = sentencesAnalysis
            .flatMap { it.keywords }
            .toMutableList()

        return analysis.copy(words = words)
    }

    private fun processSentence(sentence: CoreSentence): SentenceAnalysis {
        logger.debug { "Processing sentence ${sentence.text()}" }
        val words = mutableListOf<String>()
        sentence.tokens().forEach { token ->
            val tag = token.tag()
            val word = token.lemma().lowercase()

            val hasRelevantTag = tag.startsWith(nounTagPrefix) || tag.startsWith(adjectiveTagPrefix)

            if (hasRelevantTag && !stopWords.contains(word)) {
                words.add(word)
            }
        }
        return SentenceAnalysis(words)
    }
}