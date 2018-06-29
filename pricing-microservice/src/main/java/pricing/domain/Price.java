package pricing.domain;

import java.math.BigDecimal;

public enum Price {
    PREMIUM(40.0),
    BASIC(30.0);

    private final BigDecimal amount;

    Price(double amount) {
        this.amount = BigDecimal.valueOf(amount);
    }

    public BigDecimal get() {
        return amount;
    }
}
