package com.github.viktornordling.kvitter

import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.serialization.Serializable
import twitter4j.*
import twitter4j.conf.ConfigurationBuilder
import java.io.File
import java.lang.Long.parseLong
import java.nio.file.Paths

@Serializable
data class TwitterCredentials(
        val oauthConsumerKey: String,
        val oauthConsumerSecret: String,
        val oauthAccessToken: String,
        val oauthAccessTokenSecret: String
)

class Fetch : CliktCommand(name = "fetch", help = "Fetch tweets") {

    override fun run() {
        val file = File("min_id.txt")
        val minId = when {
            file.exists() -> parseLong(file.inputStream().bufferedReader().use { it.readText().trim() })
            else -> 1L
        }
        val config = Yaml.default.parse(TwitterCredentials.serializer(), File("config.yaml").readText())
        println("Fetching tweets newer than $minId.")

        val configBuilder = ConfigurationBuilder()
        configBuilder.setDebugEnabled(true).setTweetModeExtended(true)
                .setOAuthConsumerKey(config.oauthConsumerKey)
                .setOAuthConsumerSecret(config.oauthConsumerSecret)
                .setOAuthAccessToken(config.oauthAccessToken)
                .setOAuthAccessTokenSecret(config.oauthAccessTokenSecret)
        val twitterFactory = TwitterFactory(configBuilder.build())
        val twitter: Twitter = twitterFactory.instance
        var count = 0
        var maxId = -1L
        var newMin = -1L

        while (count < 100) {
            val paging = if (maxId == -1L) Paging().count(1000).sinceId(minId) else Paging().sinceId(minId).maxId(maxId).count(1000)
            println("Requesting tweets with minId = $minId and maxId = $maxId")
            val tweets: ResponseList<Status>? = twitter.getHomeTimeline(paging)
            if (tweets != null && !tweets.isEmpty()) {
                newMin = Math.max(newMin, tweets.first().id)
                maxId = tweets.last().id - 1
                Paths.get("archive", "$maxId.json").toFile().printWriter().use { out ->
                    val array = JSONArray(tweets)
                    for (tweet: Status in tweets) {
                        println("---")
                        print("${tweet.id}, ${tweet.createdAt}, ${tweet.text}")
                        print("\n")
                    }
                    out.println(array.toString())
                }
                // Sleep a bit so we don't hit rate limits.
                Thread.sleep(200)
                count++
            } else {
                println("Done. New min = $newMin")
                break
            }
        }
        if (newMin != -1L) {
            File("min_id.txt").printWriter().use { out ->
                out.println(newMin)
            }
        }
    }
}
