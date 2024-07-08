package divinerpg.loot_modifiers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import divinerpg.DivineRPG;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public class JungleTempleChestModifier extends LootModifier {
    public static final MapCodec<JungleTempleChestModifier> CODEC = RecordCodecBuilder.mapCodec(builder -> codecStart(builder).apply(builder, JungleTempleChestModifier::new));

    protected JungleTempleChestModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    @NotNull
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() <= 0.3){
            ItemStack toAdd = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(DivineRPG.MODID, "jungle_shards")), 1 + context.getRandom().nextInt(3));
            generatedLoot.add(toAdd);
        }
        if (context.getRandom().nextFloat() <= 0.5){
            ItemStack toAdd = new ItemStack(BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(DivineRPG.MODID, "jungle_spider_pumpkin")), 1 + context.getRandom().nextInt(1));
            generatedLoot.add(toAdd);
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
