package io.github.geeleonidas.stubborn.network

import io.github.geeleonidas.stubborn.Bimoe
import io.github.geeleonidas.stubborn.Stubborn
import io.github.geeleonidas.stubborn.init.types.StubbornC2SPacket
import io.github.geeleonidas.stubborn.resource.DialogManager
import io.github.geeleonidas.stubborn.resource.dialog.FeedbackDialog
import io.github.geeleonidas.stubborn.screen.TransceiverGuiDescription
import io.github.geeleonidas.stubborn.util.StubbornPlayer
import io.netty.buffer.Unpooled
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.client.MinecraftClient
import net.minecraft.network.PacketByteBuf
import org.apache.logging.log4j.Level

object ChangeDialogC2SPacket: StubbornC2SPacket {
    override val id = Stubborn.makeId("change_dialog")

    override fun accept(packetContext: PacketContext, packetByteBuf: PacketByteBuf) {
        val transceiverGuiDescription = packetContext.player.currentScreenHandler
        val playerEntity = packetContext.player
        val bimoe = packetByteBuf.readEnumConstant(Bimoe::class.java)
        val toDialogId = packetByteBuf.readString()
        packetContext.taskQueue.execute {
            if (!transceiverGuiDescription.canUse(playerEntity) ||
                    transceiverGuiDescription !is TransceiverGuiDescription)
                return@execute

            if (transceiverGuiDescription.bimoe != bimoe)
                return@execute

            val moddedPlayer = playerEntity as StubbornPlayer
            val currentEntryIndex = moddedPlayer.getCurrentEntry(bimoe)
            val currentDialog = DialogManager.getDialog(bimoe, playerEntity)

            // Only changes the dialog on the last entry
            if (currentEntryIndex < currentDialog.entries.size - 1)
                return@execute

            // Picking a new NodeDialog resolves in this + Handling of possible anti-ghost packets
            if (toDialogId == currentDialog.id) {
                moddedPlayer.setCurrentDialog(bimoe, toDialogId)
                return@execute
            }

            // Ending a NodeDialog tree resolves in this
            if (toDialogId == "" && currentDialog.nextDialogsIds.isEmpty()) {
                moddedPlayer.setCurrentDialog(bimoe, "")
                return@execute
            }

            // Fail-proof for changing the dialogId to some FeedbackDialog
            if (DialogManager.findDialog(bimoe, toDialogId) is FeedbackDialog)
                return@execute

            // Following a response pointer resolves in this
            if (currentDialog.nextDialogsIds.contains(toDialogId))
                moddedPlayer.setCurrentDialog(bimoe, toDialogId)
        }
    }

    @Environment(EnvType.CLIENT)
    override fun sendToServer(bimoe: Bimoe) {
        val packetByteBuf = PacketByteBuf(Unpooled.buffer())
        val player = MinecraftClient.getInstance().player
        if (player !is StubbornPlayer) {
            Stubborn.log("Player isn't valid in this ChangeDialogC2SPacket!", Level.ERROR)
            return
        }
        val toDialogId = player.getCurrentDialog(bimoe)
        packetByteBuf.writeEnumConstant(bimoe)
        packetByteBuf.writeString(toDialogId)
        ClientSidePacketRegistry.INSTANCE.sendToServer(id, packetByteBuf)
    }
}