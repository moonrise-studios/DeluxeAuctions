package me.sedattr.deluxeauctions.util;

import net.objecthunter.exp4j.ExpressionBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class BidAmounts {
    private BidAmounts() {
    }

    public static double minimum(double startingPrice, @Nullable Double highestBid, @NotNull String formula, int fractionDigits) {
        if (!Double.isFinite(startingPrice) || startingPrice < 0)
            return Double.NaN;
        if (highestBid == null)
            return startingPrice;
        if (!Double.isFinite(highestBid) || highestBid < 0)
            return Double.NaN;

        double calculated;
        try {
            calculated = new ExpressionBuilder(formula.replace("%highest_bid%", highestBid.toString()))
                    .build().evaluate();
        } catch (RuntimeException exception) {
            return Double.NaN;
        }
        if (!Double.isFinite(calculated))
            return Double.NaN;

        int scale = Math.max(0, Math.min(15, fractionDigits));
        // The next displayed currency step must still beat the highest bid if the formula does not.
        BigDecimal nextStep = BigDecimal.valueOf(highestBid).setScale(scale, RoundingMode.FLOOR)
                .add(BigDecimal.ONE.scaleByPowerOfTen(-scale));
        BigDecimal value = BigDecimal.valueOf(calculated);
        BigDecimal floor = value.setScale(scale, RoundingMode.FLOOR);
        // Ignore a single floating-point step above an exact decimal boundary (for example 0.1 + 0.2).
        BigDecimal rounded = calculated <= Math.nextUp(floor.doubleValue())
                ? floor : value.setScale(scale, RoundingMode.CEILING);
        return Math.max(Math.nextUp(highestBid), rounded.max(nextStep).doubleValue());
    }

    public static boolean accepts(double price, double minimum) {
        return Double.isFinite(price) && Double.isFinite(minimum) && price >= 0 && price >= minimum;
    }
}
