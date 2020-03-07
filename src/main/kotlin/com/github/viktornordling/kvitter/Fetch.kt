package com.github.viktornordling.kvitter

import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.serialization.Serializable
import twitter4j.*
import twitter4j.conf.ConfigurationBuilder
import java.io.File
import java.lang.Long.parseLong
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.math.max

@Serializable
data class TwitterCredentials(
        val oauthConsumerKey: String,
        val oauthConsumerSecret: String,
        val oauthAccessToken: String,
        val oauthAccessTokenSecret: String
)

class Fetch : CliktCommand(name = "fetch", help = "Fetch tweets") {

    override fun run() {
        val twitter: Twitter = createTwitterClient()
        val archiveDirectory = getOrCreateArchiveDirectory()

        val minId = getMinId()
        var count = 0
        var maxId = -1L
        var newMin = -1L

        while (count < 100) {
            val paging = if (maxId == -1L) Paging().count(1000).sinceId(minId) else Paging().sinceId(minId).maxId(maxId).count(1000)
            println("Requesting tweets with minId = $minId and maxId = $maxId")
            val tweets: ResponseList<Status>? = twitter.getHomeTimeline(paging)
            if (tweets != null && !tweets.isEmpty()) {
                newMin = max(newMin, tweets.first().id)
                maxId = tweets.last().id - 1
                archiveDirectory.resolve("$maxId.json").toFile().printWriter().use { out ->
                    val array = JSONArray(tweets)
                    out.println(array.toString())
                }
                count++
            } else {
                break
            }
        }
        if (newMin != -1L) {
            File("min_id.txt").printWriter().use { out -> out.println(newMin) }
        }
    }

    private fun getOrCreateArchiveDirectory(): Path {
        val archiveDirectory = Paths.get("archive")
        Files.createDirectories(archiveDirectory)
        return archiveDirectory
    }

    private fun getMinId(): Long {
        val file = File("min_id.txt")
        return when {
            file.exists() -> parseLong(file.inputStream().bufferedReader().use { it.readText().trim() })
            else -> 1L
        }
    }

    private fun createTwitterClient(): Twitter {
        val config = Yaml.default.parse(TwitterCredentials.serializer(), File("config.yaml").readText())
        val configBuilder = ConfigurationBuilder()
        configBuilder.setDebugEnabled(true).setTweetModeExtended(true)
                .setOAuthConsumerKey(config.oauthConsumerKey)
                .setOAuthConsumerSecret(config.oauthConsumerSecret)
                .setOAuthAccessToken(config.oauthAccessToken)
                .setOAuthAccessTokenSecret(config.oauthAccessTokenSecret)
        val twitterFactory = TwitterFactory(configBuilder.build())
        return twitterFactory.instance
    }
}
