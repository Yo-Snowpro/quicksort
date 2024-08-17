package net.pcal.quicksort.mixins;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.pcal.quicksort.QuicksortService;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.IntStream;

@Mixin(BarrelBlockEntity.class)
public class QuicksortBarrelMixin {

    @Inject(method = "transfer", at = @At("TAIL"))
    private static ItemStack transfer(@Nullable Inventory from,
                                      Inventory to,
                                      ItemStack stack,
                                      @Nullable Direction side,
                                      CallbackInfoReturnable<ItemStack> callbackInfoReturnable) {
        final var result = callbackInfoReturnable.getReturnValue();

        if (from instanceof final BarrelBlockEntity barrelBlock && to instanceof ChestBlockEntity chest) {
            if (chest.getWorld() == null) {
                return result;
            }

            final boolean isChestOpen = ChestBlockEntity.getPlayersLookingInChestCount(
                chest.getWorld(), chest.getPos()
            ) > 0;

            final var isBarrelEmpty = IntStream.range(0, BarrelBlockEntity.INVENTORY_SIZE).boxed()
                                               .allMatch(barrelSlot -> barrelBlock.getStack(barrelSlot).isEmpty());
            if (!isChestOpen && isBarrelEmpty) {
                // remove the old QuicksorterJob to avoid zombie jobs
                QuicksortService.getInstance().onChestOpened(chest);

                // start a new QuicksorterJob
                QuicksortService.getInstance().onChestClosed(chest);
            }
        }

        return result;
    }
}
