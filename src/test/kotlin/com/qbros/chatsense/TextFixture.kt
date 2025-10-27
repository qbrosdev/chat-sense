package com.qbros.chatsense

import com.qbros.chatsense.domain.Sentiment
import com.qbros.chatsense.service.analysis.model.AnalysisSummary
import com.qbros.chatsense.service.analysis.model.CommentAnalysis
import net.datafaker.Faker
import java.util.*
import kotlin.random.Random



object TextFixture {

    private val faker = Faker(Locale.ENGLISH)
    private val random = Random(System.currentTimeMillis())

    private val positiveVerbs = listOf("loved", "enjoyed", "adored", "appreciated")
    private val negativeVerbs = listOf("hated", "disliked", "regretted", "avoided")
    private val activities = listOf("reading", "buying", "finding", "visiting")
    private val templates = listOf(
        "I {verb} the book '{book}' in {city} with {person}.",
        "{person} told me that the book '{book}' was {adjective}, and I {verb} it too.",
        "While {activity} in {city}, I {verb} '{book}' and it was {adjective}.",
        "The book '{book}' by {person} is {adjective}. I truly {verb} it!"
    )

    fun generateSentence(): String {
        val positive = random.nextBoolean()
        val verb = if (positive) positiveVerbs.random(random) else negativeVerbs.random(random)
        val adjective = if (positive) listOf("amazing", "wonderful", "fantastic", "excellent").random(random)
        else listOf("terrible", "boring", "disappointing", "awful").random(random)

        return templates.random(random)
            .replace("{verb}", verb)
            .replace("{adjective}", adjective)
            .replace("{book}", faker.book().title())
            .replace("{city}", faker.address().city())
            .replace("{person}", faker.name().fullName())
            .replace("{activity}", activities.random(random))
    }

    fun analysisSummary(vararg inputs: String) = AnalysisSummary(
        sentimentOverview = emptyMap(),
        topComments = inputs.map { input ->
            CommentAnalysis(
                text = input.trimIndent(),
                words = emptyList(),
                sentiment = Sentiment.NATURAL,
                entities = emptyList()
            )
        },
        topKeywords = emptyMap(),
        topEntityNames = emptyMap()
    )
}