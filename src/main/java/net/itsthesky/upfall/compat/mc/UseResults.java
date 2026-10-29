package net.itsthesky.upfall.compat.mc;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
//? if <26.1 {
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.ItemInteractionResult;
//?}

public final class UseResults {
    private UseResults() {}

    public enum Outcome {
        SUCCESS,
        SIDED_SUCCESS,
        CONSUME,
        FAIL,
        PASS,
        PASS_TO_DEFAULT,
        SKIP_DEFAULT
    }

    public record Held(Outcome outcome, ItemStack stack) {
        public static Held success(ItemStack stack) {
            return new Held(Outcome.SUCCESS, stack);
        }

        public static Held sidedSuccess(ItemStack stack) {
            return new Held(Outcome.SIDED_SUCCESS, stack);
        }

        public static Held consume(ItemStack stack) {
            return new Held(Outcome.CONSUME, stack);
        }

        public static Held pass(ItemStack stack) {
            return new Held(Outcome.PASS, stack);
        }

        public static Held fail(ItemStack stack) {
            return new Held(Outcome.FAIL, stack);
        }
    }

    //? if >=26.1 {
    /*public static InteractionResult item(Outcome outcome, Level level) {
        return switch (outcome) {
            case SUCCESS -> level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            case SIDED_SUCCESS -> InteractionResult.SUCCESS;
            case CONSUME -> InteractionResult.CONSUME;
            case FAIL -> InteractionResult.FAIL;
            case PASS, PASS_TO_DEFAULT -> InteractionResult.TRY_WITH_EMPTY_HAND;
            case SKIP_DEFAULT -> InteractionResult.PASS;
        };
    }

    public static InteractionResult hold(Held held, Level level) {
        ItemStack stack = held.stack();
        return switch (held.outcome()) {
            case SUCCESS -> (level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER).heldItemTransformedTo(stack);
            case SIDED_SUCCESS -> InteractionResult.SUCCESS.heldItemTransformedTo(stack);
            case CONSUME -> InteractionResult.CONSUME.heldItemTransformedTo(stack);
            case PASS -> InteractionResult.PASS;
            case FAIL -> InteractionResult.FAIL;
            case PASS_TO_DEFAULT, SKIP_DEFAULT -> throw new IllegalArgumentException(held.outcome().name());
        };
    }

    public static InteractionResult plain(Outcome outcome, Level level) {
        return switch (outcome) {
            case SUCCESS -> level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
            case SIDED_SUCCESS -> InteractionResult.SUCCESS;
            case CONSUME -> InteractionResult.CONSUME;
            case FAIL -> InteractionResult.FAIL;
            case PASS -> InteractionResult.PASS;
            case PASS_TO_DEFAULT, SKIP_DEFAULT -> throw new IllegalArgumentException(outcome.name());
        };
    }
    *///?} else {
    public static ItemInteractionResult item(Outcome outcome, Level level) {
        return switch (outcome) {
            case SUCCESS -> ItemInteractionResult.SUCCESS;
            case SIDED_SUCCESS -> ItemInteractionResult.sidedSuccess(level.isClientSide());
            case CONSUME -> ItemInteractionResult.CONSUME;
            case FAIL -> ItemInteractionResult.FAIL;
            case PASS, PASS_TO_DEFAULT -> ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            case SKIP_DEFAULT -> ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        };
    }

    public static InteractionResultHolder<ItemStack> hold(Held held, Level level) {
        ItemStack stack = held.stack();
        return switch (held.outcome()) {
            case SUCCESS -> InteractionResultHolder.success(stack);
            case SIDED_SUCCESS -> InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
            case CONSUME -> InteractionResultHolder.consume(stack);
            case PASS -> InteractionResultHolder.pass(stack);
            case FAIL -> InteractionResultHolder.fail(stack);
            case PASS_TO_DEFAULT, SKIP_DEFAULT -> throw new IllegalArgumentException(held.outcome().name());
        };
    }

    public static InteractionResult plain(Outcome outcome, Level level) {
        return switch (outcome) {
            case SUCCESS -> InteractionResult.SUCCESS;
            case SIDED_SUCCESS -> InteractionResult.sidedSuccess(level.isClientSide());
            case CONSUME -> InteractionResult.CONSUME;
            case FAIL -> InteractionResult.FAIL;
            case PASS -> InteractionResult.PASS;
            case PASS_TO_DEFAULT, SKIP_DEFAULT -> throw new IllegalArgumentException(outcome.name());
        };
    }
    //?}

    public static InteractionResult sided(Level level) {
        return plain(Outcome.SIDED_SUCCESS, level);
    }
}
