package pricing.service;

import pricing.domain.response.PricingResponse;

public interface PricingService<RQ> {
    PricingResponse getPricingResponse(RQ request);
}
