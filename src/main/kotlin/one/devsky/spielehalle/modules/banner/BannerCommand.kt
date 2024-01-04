package one.devsky.spielehalle.modules.banner

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.FileUpload
import one.devsky.spielehalle.annotations.PermissionScope
import one.devsky.spielehalle.annotations.SlashCommand
import one.devsky.spielehalle.extensions.getLogger
import one.devsky.spielehalle.utils.ImageUtils.generateBanner

@SlashCommand("testbanner", "Testet den Banner", PermissionScope.ADMIN)
class BannerCommand: ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) = with(event) {
        if (name != "testbanner") return
        if (!isFromGuild) return

        val deferred = deferReply(true).complete()

        guild!!.findMembers { it.isBoosting }.onSuccess { boosters ->
            val message = deferred.sendMessage("")
            val avatars = boosters.also { getLogger().info("Found ${it.size} boosters") }.mapNotNull { it.user.avatarUrl }
            getLogger().info("Found ${avatars.size} avatars")

            if (avatars.isEmpty()) {
                message.setContent("Keine Booster gefunden")
                message.queue()
                return@onSuccess
            }

            val image =
                generateBanner(guild!!, avatars)
            message.addFiles(FileUpload.fromData(image, "welcome.png"))
            message.queue()
        }.onError {
            getLogger().error("Error while fetching boosters", it)

            deferred.sendMessage("Error while fetching boosters").queue()
        }


        return@with
    }
}