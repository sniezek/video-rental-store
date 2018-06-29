package pricing.domain;

import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class FilmTypeTest {
    @Before
    public void setUp() {
        assertEquals(BigDecimal.valueOf(40.0), Price.PREMIUM.get());
        assertEquals(BigDecimal.valueOf(30.0), Price.BASIC.get());
    }

    @Test
    public void testUpfrontPricingStrategy() {
        assertEquals(BigDecimal.valueOf(40.0), FilmType.NEW.getUpfrontPricingStrategy().apply(1));
        assertEquals(BigDecimal.valueOf(90.0), FilmType.REGULAR.getUpfrontPricingStrategy().apply(5));
        assertEquals(BigDecimal.valueOf(30.0), FilmType.REGULAR.getUpfrontPricingStrategy().apply(2));
        assertEquals(BigDecimal.valueOf(30.0), FilmType.OLD.getUpfrontPricingStrategy().apply(2));
        assertEquals(BigDecimal.valueOf(90.0), FilmType.OLD.getUpfrontPricingStrategy().apply(7));
    }

    @Test
    public void testLatePricingStrategy() {
        assertEquals(BigDecimal.valueOf(80.0), FilmType.NEW.getLatePricingStrategy().apply(2));
        assertEquals(BigDecimal.valueOf(30.0), FilmType.REGULAR.getLatePricingStrategy().apply(1));
        assertEquals(BigDecimal.valueOf(180.0), FilmType.REGULAR.getLatePricingStrategy().apply(6));
    }
}
