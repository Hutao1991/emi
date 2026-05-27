package dev.emi.emi.search;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.emi.emi.EmiPort;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.runtime.EmiTagKey;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class RegexTagQuery extends Query {
    private final Set<Object> valid;

    public RegexTagQuery(String name) {
        Pattern p = null;
        try {
            p = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
        }
        if (p == null) {
            valid = Set.of();
        } else {
            final Pattern pat = p;
            valid = Stream.<EmiTagKey<?>>concat(
                    Stream.concat(
                        EmiTagKey.fromRegistry(EmiPort.getItemRegistry()),
                        EmiTagKey.fromRegistry(EmiPort.getBlockRegistry())
                    ),
                    EmiTagKey.fromRegistry(EmiPort.getFluidRegistry()) // Add fluid registry
                ).filter(t -> {
                    if (t.hasTranslation()) {
                        if (pat.matcher(t.getTagName().getString().toLowerCase()).find()) {
                            return true;
                        }
                    }
                    if (pat.matcher(t.id().toString()).find()) {
                        return true;
                    }
                    return false;
                })
                .flatMap(tagKey -> tagKey.stream()
                    .flatMap(entry -> {
                        Stream<Object> result = Stream.empty();
                        if (entry instanceof Block block) {
                            // Map block tag entries to their corresponding items
                            result = Stream.concat(result, Stream.of(block.asItem()));
                        } else if (entry instanceof Item item) {
                            // Keep item tag entries as is
                            result = Stream.concat(result, Stream.of(item));
                        } else if (entry instanceof Fluid fluid) {
                            // Keep fluid tag entries as is
                            result = Stream.concat(result, Stream.of(fluid));
                        }
                        return result;
                    })
                    .filter(obj -> obj != Items.AIR) // Filter out air
                )
                .collect(Collectors.toSet());
        }
    }

    @Override
    public boolean matches(EmiStack stack) {
        return valid.contains(stack.getKey());
    }
}