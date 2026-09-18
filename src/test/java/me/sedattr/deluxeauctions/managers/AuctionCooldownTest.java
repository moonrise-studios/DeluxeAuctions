package me.sedattr.deluxeauctions.managers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuctionCooldownTest {
    private static final long START = 1_800_000_000L;

    @ParameterizedTest
    @CsvSource({"0, 60", "1, 59", "59, 1", "60, 0", "61, 0", "3600, 0"})
    void countsDownAndExpiresAtTheDeadline(long elapsed, long expected) {
        assertEquals(expected, AuctionCooldown.remainingSeconds(60, START, START + elapsed));
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "-1, 0", "-60, 0"})
    void disabledCooldownDoesNotBlockEvenBeforeStart(long cooldown, long expected) {
        assertEquals(expected, AuctionCooldown.remainingSeconds(cooldown, START, START - 10));
    }

    @ParameterizedTest
    @CsvSource({"0", "-1"})
    void auctionsWithoutAStartTimeRemainExempt(long startTime) {
        assertEquals(0, AuctionCooldown.remainingSeconds(60, startTime, START));
    }

    @Test
    void aFutureStartTimeKeepsTheOriginalDeadline() {
        assertEquals(70, AuctionCooldown.remainingSeconds(60, START, START - 10));
        assertEquals(60, AuctionCooldown.remainingSeconds(60, START, START));
        assertEquals(0, AuctionCooldown.remainingSeconds(60, START, START + 60));
    }

    @Test
    void bidAndPurchaseCooldownsUseTheirOwnConfiguredDuration() {
        long now = START + 30;
        assertEquals(30, AuctionCooldown.remainingSeconds(60, START, now));
        assertEquals(90, AuctionCooldown.remainingSeconds(120, START, now));
        assertEquals(0, AuctionCooldown.remainingSeconds(0, START, now));
    }
}
