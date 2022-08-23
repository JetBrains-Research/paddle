package io.paddle.plugin.python.dependencies.authentication

import io.paddle.tasks.Task
import io.paddle.utils.config.ConfigurationYAML
import java.io.File


/**
 * A class for representing configuration from $PADDLE_HOME/profiles.yaml
 *
 * Expected structure:
 * ```
 * profiles:
 *   - name: <your-username-1>
 *     token: <your-private-token-1>
 *   - name: <your-username-2>
 *     token: <your-private-token-2>
 * ```
 */
class PaddleProfilesConfig(private val profiles: List<Profile>) {
    data class Profile(val name: String, val token: String)

    companion object {
        fun getInstance(profilesFile: File): PaddleProfilesConfig? {
            val config = try {
                ConfigurationYAML.from(profilesFile)
            } catch (e: Throwable) {
                throw Task.ActException("Parse error: ${profilesFile.path} has wrong structure.")
            }
            val profiles = config.get<List<Map<String, String>>>("profiles")?.map { profileDescriptor ->
                Profile(
                    name = profileDescriptor["name"]
                        ?: throw Task.ActException("Could not parse profiles.yaml: name is not specified for some profile."),
                    token = profileDescriptor["token"]
                        ?: throw Task.ActException("Could not parse profiles.yaml: token is not specified for some profile."),
                )
            } ?: throw Task.ActException("Could not parse profiles.yaml: profiles section is not specified.")
            return PaddleProfilesConfig(profiles)
        }
    }

    fun getTokenByNameOrNull(name: String): String? {
        return profiles.find { it.name == name }?.token
    }
}
