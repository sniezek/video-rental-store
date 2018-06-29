package pricing.domain.response;

import java.math.BigDecimal;
import java.util.List;

public class ReturnPricingResponse extends PricingResponse {
    ReturnPricingResponse(BigDecimal price, List<String> messages) {
        super(price, messages);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends PricingResponse.Builder<Builder> {
        private Builder() {
        }

        @Override
        Builder getThis() {
            return this;
        }

        public ReturnPricingResponse build() {
            return new ReturnPricingResponse(price, messages);
        }
    }
}
