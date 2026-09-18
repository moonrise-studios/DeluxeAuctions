package me.sedattr.deluxeauctions.managers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BidAmountsTest {
    private static final String TEN_PERCENT = "%highest_bid% + %highest_bid% / 10";

    @Test
    void firstBidAcceptsTheAdvertisedStartingPrice() {
        double minimum = BidAmounts.minimum(100, null, TEN_PERCENT, 1);
        assertEquals(100, minimum);
        assertFalse(BidAmounts.accepts(99.9, minimum));
        assertTrue(BidAmounts.accepts(100, minimum));
        assertTrue(BidAmounts.accepts(100.1, minimum));
    }

    @Test
    void fractionalStartingPriceIsNotRoundedUpOrDown() {
        double minimum = BidAmounts.minimum(100.125, null, TEN_PERCENT, 1);
        assertEquals(100.125, minimum);
        assertFalse(BidAmounts.accepts(100.1, minimum));
        assertTrue(BidAmounts.accepts(100.125, minimum));
    }

    @Test
    void laterBidMustMeetTheConfiguredIncrement() {
        double minimum = BidAmounts.minimum(100, 100.0, TEN_PERCENT, 1);
        assertEquals(110, minimum);
        assertFalse(BidAmounts.accepts(100, minimum));
        assertFalse(BidAmounts.accepts(100.1, minimum));
        assertFalse(BidAmounts.accepts(109.9, minimum));
        assertTrue(BidAmounts.accepts(110, minimum));
    }

    @Test
    void formulaIsRoundedUpToTheDisplayedPrecision() {
        assertEquals(110.2, BidAmounts.minimum(100, 100.1, TEN_PERCENT, 1));
        assertEquals(110.11, BidAmounts.minimum(100, 100.1, TEN_PERCENT, 2));
        assertEquals(111, BidAmounts.minimum(100, 100.1, TEN_PERCENT, 0));
    }

    @Test
    void decimalArithmeticDoesNotAddAnExtraCurrencyStep() {
        assertEquals(0.3, BidAmounts.minimum(0.1, 0.1, "%highest_bid% + 0.2", 1));
    }

    @Test
    void nonIncreasingFormulaStillRequiresAHigherBid() {
        assertEquals(100.1, BidAmounts.minimum(100, 100.0, "%highest_bid%", 1));
        assertEquals(100.2, BidAmounts.minimum(100, 100.125, "%highest_bid% - 10", 1));
        assertEquals(101, BidAmounts.minimum(100, 100.0, "%highest_bid%", 0));
    }

    @Test
    void staleConfirmationMustBeValidatedAgainstTheLatestHighestBid() {
        double offeredBeforeFirstBid = BidAmounts.minimum(100, null, TEN_PERCENT, 1);
        double offeredBeforeSecondBid = BidAmounts.minimum(100, 100.0, TEN_PERCENT, 1);
        assertFalse(BidAmounts.accepts(offeredBeforeFirstBid,
                BidAmounts.minimum(100, 100.0, TEN_PERCENT, 1)));
        assertFalse(BidAmounts.accepts(offeredBeforeSecondBid,
                BidAmounts.minimum(100, 110.0, TEN_PERCENT, 1)));
    }

    @ParameterizedTest
    @ValueSource(doubles = {Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -1})
    void invalidAmountsAreRejected(double amount) {
        assertFalse(BidAmounts.accepts(amount, 0));
        assertFalse(BidAmounts.accepts(100, BidAmounts.minimum(amount, null, TEN_PERCENT, 1)));
        assertFalse(BidAmounts.accepts(100, BidAmounts.minimum(100, amount, TEN_PERCENT, 1)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "%highest_bid% / 0", "sqrt(-1)"})
    void invalidFormulaFailsClosed(String formula) {
        assertFalse(BidAmounts.accepts(1000, BidAmounts.minimum(100, 100.0, formula, 1)));
    }

    @Test
    void overflowCannotMakeAHighestBidReplaceableByAnEqualBid() {
        assertFalse(BidAmounts.accepts(Double.MAX_VALUE,
                BidAmounts.minimum(100, Double.MAX_VALUE, "%highest_bid%", 1)));
    }
}
