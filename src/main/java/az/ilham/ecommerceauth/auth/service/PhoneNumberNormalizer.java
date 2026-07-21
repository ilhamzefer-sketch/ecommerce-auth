package az.ilham.ecommerceauth.auth.service;

import org.springframework.stereotype.Component;

@Component
public class PhoneNumberNormalizer {

    public String normalize(String value) {
        String compact = value == null ? "" : value.replaceAll("[-\\s()]", "");
        String normalized;

        if (compact.startsWith("+")) {
            normalized = compact;
        } else if (compact.startsWith("994")) {
            normalized = "+" + compact;
        } else if (compact.startsWith("0")) {
            normalized = "+994" + compact.substring(1);
        } else {
            normalized = "+" + compact;
        }

        if (!normalized.matches("^\\+[1-9]\\d{7,14}$")) {
            throw new IllegalArgumentException("Phone number must be a valid international number");
        }
        return normalized;
    }
}
