package com.plusls.carpet;

import com.mojang.brigadier.CommandDispatcher;
import com.plusls.carpet.impl.network.PcaSyncProtocol;
import com.plusls.carpet.util.rule.flippingTotemOfUndying.FlipCooldown;
import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import top.hendrixshen.magiclib.carpet.api.CarpetExtensionCompatApi;
import top.hendrixshen.magiclib.carpet.impl.WrappedSettingManager;

public class PluslsCarpetAdditionExtension implements CarpetExtensionCompatApi {
    @Getter
    private static final PluslsCarpetAdditionSettingManager settingsManager = new PluslsCarpetAdditionSettingManager(
            PluslsCarpetAdditionReference.getModVersion(),
            PluslsCarpetAdditionReference.getModIdentifier(),
            PluslsCarpetAdditionReference.getModName());
    @Getter
    private static MinecraftServer server;

    @Override
    public WrappedSettingManager getSettingsManagerCompat() {
        return PluslsCarpetAdditionExtension.settingsManager;
    }

    @Override
    public void registerCommandCompat(CommandDispatcher<CommandSourceStack> dispatcher) {
    }

    @Override
    public void onGameStarted() {
        if (PluslsCarpetAdditionSettings.pcaDebug) {
            PluslsCarpetAdditionReference.getLogger().getName();
            Configurator.setLevel(PluslsCarpetAdditionReference.getModIdentifier(), Level.DEBUG);
        }
    }

    @Override
    public void onServerLoaded(MinecraftServer server) {
        PluslsCarpetAdditionExtension.server = server;
    }

    @Override
    public void onPlayerLoggedOut(ServerPlayer player) {
        PcaSyncProtocol.clearPlayerWatchData(player);
        FlipCooldown.removePlayer(player);
    }
}