package pricing.domain.response;

import lombok.Getter;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class PricingResponse {
    @Nullable
    @Getter
    final BigDecimal price;
    @Getter
    final List<String> messages;

    PricingResponse(BigDecimal price, List<String> messages) {
        this.price = price;
        this.messages = messages;
    }

    abstract static class Builder<T extends Builder<T>> {
        final List<String> messages;
        BigDecimal price;

        Builder() {
            this.messages = new ArrayList<>();
        }

        public T withPrice(BigDecimal price) {
            this.price = price;
            return getThis();
        }

        public T withMessage(String message) {
            this.messages.add(message);
            return getThis();
        }

        public T withMessages(Collection<String> messages) {
            this.messages.addAll(messages);
            return getThis();
        }

        public boolean hasMessages() {
            return !messages.isEmpty();
        }

        abstract T getThis();
    }
}
