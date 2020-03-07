package com.github.viktornordling.kvitter

import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class Install: CliktCommand(name = "install", help = "Install cron job for fetching tweets (Mac only)") {

    override fun run() {
        val workingDirectory: String = getWorkingDirectory()
        println("Working dir is: $workingDirectory")
        val hour: Int = getHour()
        val minute: Int = getMinute()
        installFetcher(workingDirectory, hour, minute)
        createTwitterCredentialsConf(workingDirectory)
    }

    private fun getWorkingDirectory(): String {
        val path = System.getProperty("user.dir")
        print("Enter working directory ($path): ")
        val workingDir: String = readLine().orEmpty()
        return workingDir.ifEmpty { path }
    }

    private fun getMinute(): Int {
        print("Enter minute when fetcher should run (0): ")
        val minute: String = readLine().orEmpty().ifEmpty { "0" }
        return minute.toInt()
    }

    private fun getHour(): Int {
        print("Enter hour when fetcher should run (10): ")
        val hour: String = readLine().orEmpty().ifEmpty { "10" }
        return hour.toInt()
    }

    private fun installFetcher(workingDirectory: String, hour: Int, minute: Int) {
        val launchAgentsDiretory = Paths.get(System.getProperty("user.home"), "Library", "LaunchAgents")
        prepareLaunchdFile(workingDirectory, launchAgentsDiretory, hour, minute)
        val unloadCommand = "launchctl unload -w com.github.viktornordling.kvitter.plist"
        println("Unloading existing job. Running launchctl command: $unloadCommand")
        val command = "launchctl load -w com.github.viktornordling.kvitter.plist"
        println("Running launchctl command: $command")
        val exec = Runtime.getRuntime().exec(command, null, launchAgentsDiretory.toFile())
        exec.outputStream
        exec.waitFor()
        println("Launch agent installed.")
    }

    private fun prepareLaunchdFile(workingDirectory: String, launchAgentsDiretory: Path, hour: Int, minute: Int) {
        var launchdFile: String = Install::class.java.getResource("/launchd.plist.template").readText()
        val userName = System.getProperty("user.name")
        launchdFile = launchdFile.replace("{{workingDirectory}}", workingDirectory)
        launchdFile = launchdFile.replace("{{hour}}", "$hour")
        launchdFile = launchdFile.replace("{{minute}}", "$minute")
        launchdFile = launchdFile.replace("{{username}}", userName)
        val plistFile = launchAgentsDiretory.resolve("com.github.viktornordling.kvitter.plist")
        Files.createDirectories(plistFile.parent)
        Files.write(plistFile, launchdFile.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }

    private fun createTwitterCredentialsConf(workingDirectory: String) {
        println("Setting up twitter credentials config file.")
        print("Enter oauthConsumerKey: ")
        val oauthConsumerKey: String = readLine().orEmpty()
        print("Enter oauthConsumerSecret: ")
        val oauthConsumerSecret: String = readLine().orEmpty()
        print("Enter oauthAccessToken: ")
        val oauthAccessToken: String = readLine().orEmpty()
        print("Enter oauthAccessTokenSecret: ")
        val oauthAccessTokenSecret: String = readLine().orEmpty()

        val twitterCredentials = TwitterCredentials(
                oauthConsumerKey = oauthConsumerKey,
                oauthConsumerSecret = oauthConsumerSecret,
                oauthAccessToken = oauthAccessToken,
                oauthAccessTokenSecret = oauthAccessTokenSecret
        )

        val yamlString = Yaml.default.stringify(TwitterCredentials.serializer(), twitterCredentials)
        val yamlFile = Paths.get(workingDirectory).resolve("config.yaml")
        Files.write(yamlFile, yamlString.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    }
}