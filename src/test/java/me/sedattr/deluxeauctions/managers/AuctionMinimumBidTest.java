package me.sedattr.deluxeauctions.managers;

import me.sedattr.deluxeauctions.DeluxeAuctions;
import me.sedattr.deluxeauctions.others.NumberFormat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuctionMinimumBidTest {
    @Test
    void minimumAndAcceptanceFollowCurrentAuctionStateAndConfiguration() {
        DeluxeAuctions plugin = mock(DeluxeAuctions.class);
        plugin.configFile = new YamlConfiguration();
        plugin.configFile.set("settings.bid_formula", "%highest_bid% + 0.25");
        plugin.configFile.set("number_format.decimal_settings.maximum_fraction", 2);
        try (MockedStatic<DeluxeAuctions> access = mockStatic(DeluxeAuctions.class)) {
            access.when(DeluxeAuctions::getInstance).thenReturn(plugin);
            Auction auction = new Auction(mock(Economy.class), 100.125, AuctionType.NORMAL, 300);
            assertEquals(100.125, auction.getMinimumBid());
            assertTrue(auction.acceptsBid(100.125));
            auction.getAuctionBids().addPlayerBid(new PlayerBid(UUID.randomUUID(), "bidder", 100.125, 1));
            assertEquals(100.38, auction.getMinimumBid());
            assertFalse(auction.acceptsBid(100.125));
            assertFalse(auction.acceptsBid(100.379));
            assertTrue(auction.acceptsBid(100.38));
            plugin.configFile.set("settings.bid_formula", "invalid");
            assertFalse(auction.acceptsBid(1000));
        }
    }

    @Test
    void exactDisplayPreservesFractionalAmountsRegardlessOfShortFormat() {
        DeluxeAuctions plugin = mock(DeluxeAuctions.class);
        plugin.configFile = new YamlConfiguration();
        plugin.configFile.set("number_format.type", "short");
        try (MockedStatic<DeluxeAuctions> access = mockStatic(DeluxeAuctions.class)) {
            access.when(DeluxeAuctions::getInstance).thenReturn(plugin);
            NumberFormat format = new NumberFormat();
            assertEquals("1234.125", format.formatExact(1234.125));
            assertEquals("0.0000001", format.formatExact(0.0000001));
            assertEquals("100", format.formatExact(100));
            assertEquals("", format.formatExact(Double.NaN));
        }
    }
}
