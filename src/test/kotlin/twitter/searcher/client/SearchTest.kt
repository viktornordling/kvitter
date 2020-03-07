package twitter.searcher.client

import com.github.viktornordling.kvitter.Search
import com.github.viktornordling.kvitter.Tweet
import com.github.viktornordling.kvitter.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SearchTest {

    @Test
    fun testFixTimezone() {
        val someUser = User(screenName = "someone")
        val tweet = Tweet(createdAt = "Sat Apr 13 19:13:56 PHT 2019", text = "some text", id = "some id", user = someUser, URLEntities = listOf(), retweet = false)
        val fixedTweet = Search().fixTimezone(tweet)

        assertThat(fixedTweet.createdAt).doesNotContain("PHT")
    }
}
