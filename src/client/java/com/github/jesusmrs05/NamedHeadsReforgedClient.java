package com.github.jesusmrs05;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NamedHeadsReforgedClient implements ClientModInitializer {

    private DisplayEntity.TextDisplayEntity nameDisplay;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient mc) {
        if (mc.world == null || mc.player == null) {
            removeDisplay(mc);
            return;
        }

        if (mc.crosshairTarget == null ||
                mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            removeDisplay(mc);
            return;
        }

        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        BlockPos pos = hit.getBlockPos();

        BlockState state = mc.world.getBlockState(pos);
        if (state.getBlock() != Blocks.PLAYER_HEAD &&
                state.getBlock() != Blocks.PLAYER_WALL_HEAD) {
            removeDisplay(mc);
            return;
        }

        if (!(mc.world.getBlockEntity(pos) instanceof SkullBlockEntity skull)) {
            removeDisplay(mc);
            return;
        }

        var profile = skull.getOwner();
        if (profile == null || profile.getName().isEmpty()) {
            removeDisplay(mc);
            return;
        }

        String name = profile.getName().get();

        ensureDisplay(mc);
        updateDisplay(mc, pos, name);
    }

    private void ensureDisplay(MinecraftClient mc) {
        if (nameDisplay != null && nameDisplay.isAlive()) return;

        nameDisplay = new DisplayEntity.TextDisplayEntity(
                EntityType.TEXT_DISPLAY,
                mc.world
        );

        nameDisplay.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        nameDisplay.setBackground(0x40000000);
        nameDisplay.setViewRange(64.0f);
        byte flags = 0;
        flags |= DisplayEntity.TextDisplayEntity.SEE_THROUGH_FLAG;
        nameDisplay.setDisplayFlags(flags);

        mc.world.addEntity(nameDisplay);
    }

    private void updateDisplay(MinecraftClient mc, BlockPos pos, String name) {
        Vec3d textPos = Vec3d.ofCenter(pos).add(0.0, 0.25, 0.0);

        nameDisplay.setPosition(textPos);
        nameDisplay.setText(Text.literal(name));
    }

    private void removeDisplay(MinecraftClient mc) {
        if (nameDisplay != null) {
            nameDisplay.discard();
            nameDisplay = null;
        }
    }
}