package me.sedattr.deluxeauctions.commands;

import me.sedattr.auctionsapi.AuctionHook;
import me.sedattr.auctionsapi.cache.PlayerCache;
import me.sedattr.deluxeauctions.DeluxeAuctions;
import me.sedattr.deluxeauctions.menus.AuctionsMenu;
import me.sedattr.deluxeauctions.others.PlaceholderUtil;
import me.sedattr.deluxeauctions.others.Utils;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.mockito.MockedConstruction;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuctionCommandTest {
    private DeluxeAuctions plugin;
    private Player player;
    private MockedStatic<DeluxeAuctions> pluginAccess;
    private MockedStatic<Utils> utils;
    private MockedStatic<AuctionHook> menus;
    private AuctionCommand command;

    @BeforeEach
    void setUp() {
        plugin = mock(DeluxeAuctions.class);
        plugin.configFile = new YamlConfiguration();
        plugin.configFile.set("settings.menu_to_open_directly", "main");
        plugin.loaded = true;
        player = mock(Player.class);
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");
        when(player.getWorld()).thenReturn(world);
        pluginAccess = mockStatic(DeluxeAuctions.class);
        pluginAccess.when(DeluxeAuctions::getInstance).thenReturn(plugin);
        utils = mockStatic(Utils.class);
        utils.when(() -> Utils.hasPermission(player, "player_commands", "command")).thenReturn(true);
        utils.when(() -> Utils.hasPermission(player, "player_commands", "menu")).thenReturn(true);
        menus = mockStatic(AuctionHook.class);
        command = new AuctionCommand();
    }

    @AfterEach
    void tearDown() {
        if (menus != null) menus.close();
        if (utils != null) utils.close();
        if (pluginAccess != null) pluginAccess.close();
    }

    private boolean execute(String route) {
        return command.execute(player, "ah", route.isEmpty() ? new String[0] : new String[]{route});
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "menu", "open"})
    void opensTheSameMainMenu(String route) {
        assertTrue(execute(route));
        menus.verify(() -> AuctionHook.openMainMenu(player));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "menu", "auctions", "sell"})
    void lockedAuctionsRejectCommands(String route) {
        plugin.locked = true;
        assertFalse(execute(route));
        utils.verify(() -> Utils.sendMessage(player, "closed"));
        menus.verifyNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "menu"})
    void operatorRetainsLockBypass(String route) {
        plugin.locked = true;
        when(player.isOp()).thenReturn(true);
        assertTrue(execute(route));
        menus.verify(() -> AuctionHook.openMainMenu(player));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "menu", "auctions", "sell"})
    void waitsUntilLoaded(String route) {
        plugin.loaded = false;
        assertFalse(execute(route));
        utils.verify(() -> Utils.sendMessage(player, "loading"));
        menus.verifyNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "menu", "auctions", "sell"})
    void obeysDisabledWorlds(String route) {
        utils.when(() -> Utils.isDisabledWorld("world")).thenReturn(true);
        assertFalse(execute(route));
        utils.verify(() -> Utils.sendMessage(player, "disabled_world"));
        menus.verifyNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "menu"})
    void obeysLagGuard(String route) {
        utils.when(() -> Utils.isLaggy(player)).thenReturn(true);
        assertFalse(execute(route));
        utils.verify(() -> Utils.sendMessage(player, "laggy"));
        menus.verifyNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "menu"})
    void obeysMenuPermission(String route) {
        utils.when(() -> Utils.hasPermission(player, "player_commands", "menu")).thenReturn(false);
        assertFalse(execute(route));
        utils.verify(() -> Utils.sendMessage(player, "no_permission"));
        menus.verifyNoInteractions();
    }

    @Test
    void directAuctionsRequiresAuctionsPermission() {
        plugin.configFile.set("settings.menu_to_open_directly", "auctions");
        assertFalse(execute(""));
        utils.verify(() -> Utils.hasPermission(player, "player_commands", "auctions"));
        utils.verify(() -> Utils.sendMessage(player, "no_permission"));
        menus.verifyNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "auctions"})
    void auctionsSelectorPreservesBrowserDestination(String route) {
        plugin.configFile.set("settings.menu_to_open_directly", "auctions");
        plugin.category = "global";
        utils.when(() -> Utils.hasPermission(player, "player_commands", "auctions")).thenReturn(true);
        try (MockedStatic<PlayerCache> players = mockStatic(PlayerCache.class);
             MockedConstruction<AuctionsMenu> browsers = mockConstruction(AuctionsMenu.class)) {
            players.when(PlayerCache::getPlayers).thenReturn(Collections.emptyMap());
            assertTrue(execute(route));
            verify(browsers.constructed().getFirst()).open("global", 1);
            menus.verifyNoInteractions();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ah", "auction", "deluxeauctions", "auc"})
    void aliasesUseSameRootMenu(String alias) {
        assertTrue(command.execute(player, alias, new String[0]));
        menus.verify(() -> AuctionHook.openMainMenu(player));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "menu"})
    void obeysRootPermission(String route) {
        utils.when(() -> Utils.hasPermission(player, "player_commands", "command")).thenReturn(false);
        assertFalse(execute(route));
        utils.verify(() -> Utils.sendMessage(player, "no_permission"));
        menus.verifyNoInteractions();
    }

    @Test
    void emptySelectorPreservesRootHelpEvenWhileLockedAndLoading() {
        plugin.configFile.set("settings.menu_to_open_directly", "");
        plugin.locked = true;
        plugin.loaded = false;
        assertFalse(execute(""));
        utils.verify(() -> Utils.sendMessage(eq(player), eq("player_usage"), any(PlaceholderUtil.class)));
        menus.verifyNoInteractions();
    }

    @Test
    void obsoleteBooleanStillDoesNotSelectAMenu() {
        plugin.configFile.set("settings.menu_to_open_directly", null);
        plugin.configFile.set("settings.open_menu_directly", true);
        assertFalse(execute(""));
        utils.verify(() -> Utils.sendMessage(eq(player), eq("player_usage"), any(PlaceholderUtil.class)));
        menus.verifyNoInteractions();
    }

    @ParameterizedTest
    @ValueSource(strings = {"help", "unknown"})
    void unrecognizedArgumentsRetainConfiguredFallback(String route) {
        assertTrue(execute(route));
        menus.verify(() -> AuctionHook.openMainMenu(player));
    }

    @Test
    void explicitMenuStillWorksWithEmptySelector() {
        plugin.configFile.set("settings.menu_to_open_directly", "");
        assertTrue(execute("menu"));
        menus.verify(() -> AuctionHook.openMainMenu(player));
    }
}
