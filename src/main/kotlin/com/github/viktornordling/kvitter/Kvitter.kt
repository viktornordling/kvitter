package com.github.viktornordling.kvitter

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class KvitterBase: CliktCommand() {
    override fun run() = Unit
}

object Kvitter {
    @JvmStatic
    fun main(args: Array<String>) = KvitterBase()
            .subcommands(Install(), Fetch(), Search())
            .main(args)
}
