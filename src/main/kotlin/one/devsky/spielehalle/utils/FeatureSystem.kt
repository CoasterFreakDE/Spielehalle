package one.devsky.spielehalle.utils

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import one.devsky.spielehalle.annotations.MessageCommand
import one.devsky.spielehalle.annotations.PermissionScope
import one.devsky.spielehalle.annotations.SlashCommand
import one.devsky.spielehalle.annotations.UserCommand
import one.devsky.spielehalle.extensions.getLogger
import one.devsky.spielehalle.interfaces.HasOptions
import one.devsky.spielehalle.interfaces.HasSubcommandGroups
import one.devsky.spielehalle.interfaces.HasSubcommands
import org.reflections8.Reflections
import kotlin.time.measureTime

object FeatureSystem {

    private var loadedClasses: Map<String, Any> = mutableMapOf()

    fun JDABuilder.registerAll() : JDABuilder {
        val reflections = Reflections("one.devsky.spielehalle")

        // Registering both ListenerAdapters and EventListeners
        val listenerTime = measureTime {
            for (clazz in (reflections.getSubTypesOf(ListenerAdapter::class.java)).distinct()) {
                if (clazz.simpleName == "ListenerAdapter") continue

                val constructor = clazz.getDeclaredConstructor()
                constructor.trySetAccessible()

                val listener = constructor.newInstance()
                loadedClasses += clazz.simpleName to listener

                addEventListeners(listener)
                getLogger().info("Registered listener: ${listener.javaClass.simpleName}")
            }
        }
        getLogger().info("Registered listeners in $listenerTime")

        return this
    }

    /**
     * Registers the commands defined in the package "de.rainbowalliance.neo".
     *
     * This method uses reflection to scan for classes annotated with annotations such as SlashCommand, UserCommand, and MessageCommand.
     * It then calls the private method "doRegister" to register the commands based on the annotation type.
     *
     * @return The JDA instance after registering the commands.
     */
    fun JDA.registerCommands(): JDA {
        val reflections = Reflections("one.devsky.spielehalle")

        // Registering commands
        val commandsTime = measureTime {
            for (clazz in reflections.getTypesAnnotatedWith(SlashCommand::class.java)) {
                val annotation = clazz.getAnnotation(SlashCommand::class.java)

                val data = Commands.slash(annotation.name, annotation.description)
                    .apply {
                        when(annotation.permissionScope) {
                            PermissionScope.MODERATOR -> {
                                defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
                            }
                            PermissionScope.ADMIN -> {
                                defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                            }
                            else -> {}
                        }
                    }

                if (clazz.simpleName !in loadedClasses) {
                    val constructor = clazz.getDeclaredConstructor()
                    constructor.trySetAccessible()

                    val command = constructor.newInstance()
                    loadedClasses += clazz.simpleName to command
                    getLogger().info("Registered command class: ${command.javaClass.simpleName}")
                }

                val command = loadedClasses[clazz.simpleName]

                if (command is HasOptions) {
                    data.addOptions(command.getOptions())
                }

                if (command is HasSubcommandGroups) {
                    data.addSubcommandGroups(command.getSubcommandGroups())
                }

                if (command is HasSubcommands) {
                    data.addSubcommands(command.getSubCommands())
                }


                upsertCommand(data).queue()
                getLogger().info("Registered global command: ${annotation.name}")
            }

            // UserCommands
            for (clazz in reflections.getTypesAnnotatedWith(UserCommand::class.java)) {
                val annotation = clazz.getAnnotation(UserCommand::class.java)
                val data = Commands.user(annotation.name)
                    .apply {
                        when(annotation.permissionScope) {
                            PermissionScope.MODERATOR -> {
                                defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
                            }
                            PermissionScope.ADMIN -> {
                                defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                            }
                            else -> {}
                        }
                    }

                if (clazz.simpleName !in loadedClasses) {
                    val constructor = clazz.getDeclaredConstructor()
                    constructor.trySetAccessible()

                    val command = constructor.newInstance()
                    loadedClasses += clazz.simpleName to command
                    getLogger().info("Registered user command class: ${command.javaClass.simpleName}")
                }

                upsertCommand(data).queue()
                getLogger().info("Registered global user command: ${annotation.name}")
            }


            // MessageCommands
            for (clazz in reflections.getTypesAnnotatedWith(MessageCommand::class.java)) {
                val annotation = clazz.getAnnotation(MessageCommand::class.java)
                val data = Commands.message(annotation.name)
                    .apply {
                        when(annotation.permissionScope) {
                            PermissionScope.MODERATOR -> {
                                defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.MODERATE_MEMBERS)
                            }
                            PermissionScope.ADMIN -> {
                                defaultPermissions = DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)
                            }
                            else -> {}
                        }
                    }

                if (clazz.simpleName !in loadedClasses) {
                    val constructor = clazz.getDeclaredConstructor()
                    constructor.trySetAccessible()

                    val command = constructor.newInstance()
                    loadedClasses += clazz.simpleName to command
                    getLogger().info("Registered message command class: ${command.javaClass.simpleName}")
                }


                upsertCommand(data).queue()
                getLogger().info("Registered global message command: ${annotation.name}")
            }
        }
        getLogger().info("Registered commands in $commandsTime")

        return this
    }


}