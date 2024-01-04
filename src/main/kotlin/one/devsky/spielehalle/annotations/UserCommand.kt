package one.devsky.spielehalle.annotations

/**
 * UserCommand annotation is used to annotate functions or methods that represent user commands.
 *
 * @param name The name of the command.
 */
annotation class UserCommand(
    val name: String,
    val permissionScope: PermissionScope = PermissionScope.USER
)