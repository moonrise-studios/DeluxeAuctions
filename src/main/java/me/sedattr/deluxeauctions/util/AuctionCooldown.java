package me.sedattr.deluxeauctions.util;

public final class AuctionCooldown {
    private AuctionCooldown() {
    }

    public static long remainingSeconds(long cooldown, long startTime, long currentTime) {
        // Loaded auctions without a saved start time retain their existing exemption.
        if (cooldown <= 0 || startTime <= 0)
            return 0;

        long elapsed = currentTime - startTime;
        return Math.max(0, cooldown - elapsed);
    }
}
