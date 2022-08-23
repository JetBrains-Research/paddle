package io.paddle.plugin.python.dependencies.authentication

import io.paddle.execution.EnvProvider
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.utils.exists
import io.paddle.utils.splitAndTrim
import java.nio.file.Paths
import java.util.regex.Pattern
import kotlin.io.path.readText

typealias NetrcHost = String

class NetrcConfig(private val hosts: Map<NetrcHost, PyPackageRepository.Credentials>) {
    companion object {
        fun findInstance(env: EnvProvider): NetrcConfig? {
            val home: String? = env.get("HOME")
            val netrcLocation = env.get("NETRC")?.let { Paths.get(it) }
                ?: home?.let { Paths.get(it, ".netrc") }
            val netrcContent = netrcLocation?.takeIf { it.exists() }?.readText() ?: return null
            val lines = Parser.dropComments(netrcContent.lines())
            return NetrcConfig(Parser(lines).parse())
        }
    }

    fun authenticators(host: String): PyPackageRepository.Credentials? {
        return hosts[host] ?: hosts["default"]
    }

    private class Parser(private val lines: List<String>) {
        private enum class State {
            START, AWAIT_KEY, AWAIT_VALUE, MACHINE, LOGIN, PASSWORD, ACCOUNT, MACDEF
        }

        companion object {
            private val commentPattern = Pattern.compile("(^|\\s)#\\s");

            fun dropComments(lines: List<String>): List<String> {
                val commentMatcher = commentPattern.matcher("")
                val result = ArrayList<String>()
                for (line in lines) {
                    commentMatcher.reset(line);
                    if (commentMatcher.find()) {
                        result.add(line.substring(0, commentMatcher.start()).trim())
                    } else {
                        result.add(line)
                    }
                }
                return result
            }
        }

        private var state = State.START
        private var machine: String? = null
        private var login: String? = null
        private var password: String? = null
        private var account: String? = null

        fun parse(): Map<NetrcHost, PyPackageRepository.Credentials> {
            val hosts = HashMap<NetrcHost, PyPackageRepository.Credentials>()

            fun saveHostSafely(newHost: String? = null) {
                if (machine != null && login != null && password != null) {
                    hosts[machine!!] = PyPackageRepository.Credentials(login!!, password!!, account)
                }
                machine = newHost
                login = null
                password = null
                account = null
            }

            for (line in lines) {
                if (line.isEmpty()) {
                    if (state == State.MACDEF) {
                        state = State.AWAIT_KEY
                    }
                    continue
                }

                val tokens = line.splitAndTrim(" ", "\t")
                for (token in tokens) {
                    when (state) {
                        State.START -> {
                            when (token) {
                                "machine" -> state = State.MACHINE
                                "default" -> state = State.AWAIT_KEY.also { saveHostSafely("default") }
                            }
                        }
                        State.AWAIT_KEY -> {
                            state = when (token) {
                                "login" -> State.LOGIN
                                "password" -> State.PASSWORD
                                "account" -> State.ACCOUNT
                                "macdef" -> State.MACDEF
                                "machine" -> State.MACHINE
                                "default" -> State.AWAIT_KEY.also { saveHostSafely("default") }
                                else -> State.AWAIT_VALUE
                            }
                        }
                        State.AWAIT_VALUE -> state = State.AWAIT_KEY
                        State.MACHINE -> {
                            saveHostSafely(token)
                            state = State.AWAIT_KEY
                        }
                        State.LOGIN -> {
                            login = token
                            state = State.AWAIT_KEY
                        }
                        State.PASSWORD -> {
                            password = token
                            state = State.AWAIT_KEY
                        }
                        State.ACCOUNT -> {
                            account = token
                            state = State.AWAIT_KEY
                        }
                        State.MACDEF -> {
                            continue
                        }
                    }
                }
            }

            saveHostSafely()

            return hosts
        }
    }
}
