package com.github.viktornordling.kvitter

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import java.io.File
import java.nio.file.Paths
import java.text.DateFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import java.util.spi.TimeZoneNameProvider

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(val screenName: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class URLEntity(val URL: String, val displayURL: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Tweet(val createdAt: String, val retweet: Boolean, val text: String, val user: User, val id: String, val URLEntities: List<URLEntity>)

class TweetList : TypeReference<List<Tweet>>()

class Search: CliktCommand(help = "Search tweets") {
    private val searchTerm by argument(name = "SEARCH-TERM", help = "Term you want to search for")
    private val knownTimezones = TimeZone.getAvailableIDs().toSet()

    override fun run() {
        println("Searching for '$searchTerm'")
        val mapper = ObjectMapper().registerModule(KotlinModule())
        val files = Paths.get("archive").toFile().walkTopDown().filter { f -> f.name.endsWith(".json") }
        // Format is "Wed Mar 20 10:43:53 JST 2019"
        val formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy")
        val matchingTweets = files.map { file ->
            val tweets: List<Tweet> = mapper.readValue(file, TweetList())
            tweets.filter { tweet -> tweet.text.toLowerCase().contains(searchTerm) }
        }.flatten()
        matchingTweets
                .map { fixTimezone(it) }
                .sortedBy { LocalDate.parse(it.createdAt, formatter) }.forEach { tweet -> println("Found match: ${tweet.createdAt}: ${tweet.text}, \nlink: https://twitter.com/${tweet.user.screenName}/status/${tweet.id}") }
    }

    fun fixTimezone(tweet: Tweet): Tweet {
        // Because twitter is horrible, the timezone is sometimes bogus (like PHT), so convert any bogus
        // timezones to the user's local timezone.
        val parts = tweet.createdAt.split(" ")
        if (parts.size == 6) {
            val timezone = tweet.createdAt.split(" ").takeLast(2).first()
            if (!knownTimezones.contains(timezone)) {
                return tweet.copy(createdAt = tweet.createdAt.replace(timezone, TimeZone.getDefault().id))
            }
        }
        return tweet
    }
}


