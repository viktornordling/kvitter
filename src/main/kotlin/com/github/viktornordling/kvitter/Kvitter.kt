package com.github.viktornordling.kvitter

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class KvitterClient: CliktCommand(name = "kvitter") {
    override fun run() = Unit
}

object Kvitter {
    @JvmStatic
    fun main(args: Array<String>) = KvitterClient()
            .subcommands(Install(), Fetch(), Search())
            .main(args)
}
