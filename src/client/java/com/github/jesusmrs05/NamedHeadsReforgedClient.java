package com.github.jesusmrs05;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class NamedHeadsReforgedClient implements ClientModInitializer {

    private Display.TextDisplay nameDisplay;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            removeDisplay();
            return;
        }

        if (mc.hitResult == null ||
                mc.hitResult.getType() != HitResult.Type.BLOCK) {
            removeDisplay();
            return;
        }

        BlockHitResult hit = (BlockHitResult) mc.hitResult;
        BlockPos pos = hit.getBlockPos();

        BlockState state = mc.level.getBlockState(pos);
        if (state.getBlock() != Blocks.PLAYER_HEAD &&
                state.getBlock() != Blocks.PLAYER_WALL_HEAD) {
            removeDisplay();
            return;
        }

        if (!(mc.level.getBlockEntity(pos) instanceof SkullBlockEntity skull)) {
            removeDisplay();
            return;
        }

        var profile = skull.getOwnerProfile();
        if (profile == null || profile.name().isEmpty()) {
            removeDisplay();
            return;
        }

        String name = profile.name().get();

        ensureDisplay(mc);
        updateDisplay(pos, name);
    }

    private void ensureDisplay(Minecraft mc) {
        if (nameDisplay != null && nameDisplay.isAlive()) {
            return;
        }

        nameDisplay = new Display.TextDisplay(
                EntityTypes.TEXT_DISPLAY,
                mc.level
        );

        nameDisplay.setId(-1);

        nameDisplay.setBillboardConstraints(
                Display.BillboardConstraints.CENTER
        );
        nameDisplay.setBackgroundColor(0x40000000);
        nameDisplay.setViewRange(64.0f);
        nameDisplay.setFlags(Display.TextDisplay.FLAG_SEE_THROUGH);

        mc.level.addEntity(nameDisplay);
    }

    private void updateDisplay(BlockPos pos, String name) {
        Vec3 textPos = Vec3.atCenterOf(pos).add(0.0, 0.25, 0.0);

        nameDisplay.setPos(textPos);
        nameDisplay.setText(Component.literal(name));
    }

    private void removeDisplay() {
        if (nameDisplay != null) {
            nameDisplay.discard();
            nameDisplay = null;
        }
    }
}