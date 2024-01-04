package one.devsky.spielehalle.annotations

/**
 * Represents an annotation to indicate that a class is a message command.
 *
 * @property name The name of the message command.
 */
annotation class MessageCommand(
    val name: String,
    val permissionScope: PermissionScope = PermissionScope.USER
)