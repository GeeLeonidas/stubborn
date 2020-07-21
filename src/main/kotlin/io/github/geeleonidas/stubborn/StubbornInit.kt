package io.github.geeleonidas.stubborn

import io.github.geeleonidas.stubborn.block.TransceiverBlock
import io.github.geeleonidas.stubborn.block.entity.TransceiverBlockEntity
import io.github.geeleonidas.stubborn.container.TransceiverGuiDescription
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import java.util.function.Supplier


object StubbornInit {

    private val registeredBlocks = mutableMapOf<Identifier, Block>()
    private val registeredItems = mutableMapOf<Identifier, Item>()

    val transceiverBlock = TransceiverBlock()
    val transceiverBlockEntityType = Registry.register(
        Registry.BLOCK_ENTITY_TYPE, transceiverBlock.id,
        BlockEntityType.Builder.create(Supplier { TransceiverBlockEntity() }, transceiverBlock).build(null)
    )
    val transceiverHandlerType =
        ScreenHandlerRegistry.registerSimple<TransceiverGuiDescription>(transceiverBlock.id) {
                syncId, playerInventory ->
            TransceiverGuiDescription(syncId, playerInventory, BlockPos.ORIGIN)
    }

    fun addBlock(id: Identifier, block: Block) = registeredBlocks.putIfAbsent(id, block)
    fun addItem(id: Identifier, item: Item) = registeredItems.putIfAbsent(id, item)

    fun registerBlocks() {
        for (pair in registeredBlocks)
            Registry.register(Registry.BLOCK, pair.key, pair.value)
    }

    fun registerItems() {
        for (pair in registeredItems)
            Registry.register(Registry.ITEM, pair.key, pair.value)
    }
}

interface StubbornBlock {
    val id: Identifier

    fun register(ref: Block, hasItem: Boolean = true) {
        if (hasItem)
            StubbornInit.addItem(id, BlockItem(ref, Item.Settings().group(Stubborn.modItemGroup)))

        StubbornInit.addBlock(id, ref)
    }
}

interface StubbornItem {
    val id: Identifier

    fun register(ref: Item) = StubbornInit.addItem(id, ref)
}