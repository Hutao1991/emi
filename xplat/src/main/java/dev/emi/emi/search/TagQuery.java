package dev.emi.emi.search;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiTagKey;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class TagQuery extends Query {
    private final Set<Object> valid;

    public TagQuery(String name) {
        String lowerName = name.toLowerCase();
        valid = Stream.<EmiTagKey<?>>concat(
                Stream.concat(
                    EmiTagKey.fromRegistry(EmiPort.getItemRegistry()),
                    EmiTagKey.fromRegistry(EmiPort.getBlockRegistry())
                ),
                EmiTagKey.fromRegistry(EmiPort.getFluidRegistry()) // 添加流体注册表
            ).filter(t -> {
                if (t.hasTranslation()) {
                    if (t.getTagName().getString().toLowerCase().contains(lowerName)) {
                        return true;
                    }
                }
                if (t.id().toString().contains(lowerName)) {
                    return true;
                }
                return false;
            })
            .flatMap(tagKey -> tagKey.stream()
                .flatMap(entry -> {
                    Stream<Object> result = Stream.empty();
                    if (entry instanceof Block block) {
                        // 搜索方块标签对应物品
                        result = Stream.concat(result, Stream.of(block.asItem()));
                    } else if (entry instanceof Item item) {
                        // 搜索物品标签对应物品
                        result = Stream.concat(result, Stream.of(item));
                    } else if (entry instanceof Fluid fluid) {
                        // 流体搜索流体标签对应流体
                        result = Stream.concat(result, Stream.of(fluid));
                    }
                    return result;
                })
                .filter(obj -> obj != Items.AIR) // 过滤空气
            )
            .collect(Collectors.toSet());
    }

    @Override
    public boolean matches(EmiStack stack) {
        return valid.contains(stack.getKey());
    }
}