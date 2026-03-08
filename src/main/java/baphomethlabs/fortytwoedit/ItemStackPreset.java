package baphomethlabs.fortytwoedit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class ItemStackPreset {

    private ItemStack stack = null;
    private boolean isCached = false;

    private ItemStackPreset() {}

    public ItemStack get() {
        if (!isCached) {
            resolve();
        }
        return stack;
    }

    protected abstract void resolve();

    protected void setStack(ItemStack stack) {
        this.isCached = true;
        this.stack = stack;
    }

    public static ItemStackPreset set(Item item) {
        return new ItemStackPresetFromItem(item);
    }

    public static ItemStackPreset set(ItemStack stack) {
        return new ItemStackPresetFromItemStack(stack);
    }

    public static ItemStackPreset set(CompoundTag nbt) {
        return set(BlackMagick.nbtToSnbt(nbt));
    }

    public static ItemStackPreset set(String snbt) {
        return new ItemStackPresetFromString(snbt);
    }

    protected static class ItemStackPresetFromItemStack extends ItemStackPreset {

        protected ItemStackPresetFromItemStack(ItemStack stack) {
            super();
            this.setStack(stack);
        }

        @Override
        protected void resolve() {}

    }

    protected static class ItemStackPresetFromItem extends ItemStackPreset {

        private Item item = null;

        protected ItemStackPresetFromItem(Item item) {
            super();
            this.item = item;
        }

        @Override
        protected void resolve() {
            this.setStack(new ItemStack(item));
        }

    }

    protected static class ItemStackPresetFromString extends ItemStackPreset {

        private String snbt = null;

        protected ItemStackPresetFromString(String snbt) {
            super();
            this.snbt = snbt;
        }

        @Override
        protected void resolve() {
            this.setStack(BlackMagick.itemFromString(snbt));
        }

    }
    
}
