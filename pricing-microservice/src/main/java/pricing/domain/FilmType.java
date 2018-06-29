package pricing.domain;

import lombok.Getter;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.IntFunction;

import static pricing.domain.Price.BASIC;
import static pricing.domain.Price.PREMIUM;

public enum FilmType {
    NEW(days -> multiply(PREMIUM, days), days -> multiply(PREMIUM, days)),
    REGULAR(days -> firstXDaysPricePlusPriceMultipliedByExtraDays(BASIC, 3, days), days -> multiply(BASIC, days)),
    OLD(days -> firstXDaysPricePlusPriceMultipliedByExtraDays(BASIC, 5, days), days -> multiply(BASIC, days));

    @Getter
    private final IntFunction<BigDecimal> upfrontPricingStrategy;
    @Getter
    private final IntFunction<BigDecimal> latePricingStrategy;

    FilmType(IntFunction<BigDecimal> upfrontPricingStrategy, IntFunction<BigDecimal> latePricingStrategy) {
        this.upfrontPricingStrategy = upfrontPricingStrategy;
        this.latePricingStrategy = latePricingStrategy;
    }

    private static BigDecimal multiply(Price price, int days) {
        return price.get().multiply(BigDecimal.valueOf(days));
    }

    private static BigDecimal firstXDaysPricePlusPriceMultipliedByExtraDays(Price price, int x, int days) {
        return days <= x ? price.get() : add(price, multiply(price, days - x));
    }

    private static BigDecimal add(Price price, BigDecimal bigDecimal) {
        return price.get().add(bigDecimal);
    }

    public static Optional<FilmType> ofString(@Nullable String filmType) {
        if (filmType == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(FilmType.valueOf(filmType.replace("\"", "")));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
