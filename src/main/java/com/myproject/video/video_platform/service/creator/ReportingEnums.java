package com.myproject.video.video_platform.service.creator;

import com.myproject.video.video_platform.common.enums.commerce.CommerceOrderStatus;

import java.util.Arrays;

public final class ReportingEnums {
    private ReportingEnums() {
    }

    public interface WireValue {
        String wireValue();
    }

    public enum Period implements WireValue {
        TODAY("today", 1), DAYS_7("7d", 7), DAYS_30("30d", 30), DAYS_90("90d", 90);
        private final String wireValue;
        private final int days;
        Period(String wireValue, int days) { this.wireValue = wireValue; this.days = days; }
        public String wireValue() { return wireValue; }
        public int days() { return days; }
        public static Period sales(String value) { return parse(Period.class, value == null ? "30d" : value); }
        public static Period analytics(String value) {
            Period period = parse(Period.class, value == null ? "30d" : value);
            if (period == TODAY) throw new IllegalArgumentException("Analytics period must be 7d, 30d, or 90d");
            return period;
        }
    }

    public enum SalesStatus implements WireValue {
        ALL("all", null), PAID("paid", CommerceOrderStatus.PAID), FAILED("failed", CommerceOrderStatus.FAILED),
        REFUNDED("refunded", CommerceOrderStatus.REFUNDED), PENDING("pending", CommerceOrderStatus.PENDING);
        private final String wireValue;
        private final CommerceOrderStatus status;
        SalesStatus(String wireValue, CommerceOrderStatus status) { this.wireValue = wireValue; this.status = status; }
        public String wireValue() { return wireValue; }
        public CommerceOrderStatus status() { return status; }
        public static SalesStatus parse(String value) { return ReportingEnums.parse(SalesStatus.class, value == null ? "all" : value); }
    }

    public enum SalesSort implements WireValue {
        NEWEST("newest"), OLDEST("oldest"), AMOUNT_DESC("amount-desc"), AMOUNT_ASC("amount-asc");
        private final String wireValue;
        SalesSort(String wireValue) { this.wireValue = wireValue; }
        public String wireValue() { return wireValue; }
        public static SalesSort parse(String value) { return ReportingEnums.parse(SalesSort.class, value == null ? "newest" : value); }
    }

    public enum CustomerStatus implements WireValue {
        ALL("all"), ACTIVE_MEMBER("active-member"), PAST_DUE("past-due"), BUYER("buyer"), WAITLIST("waitlist");
        private final String wireValue;
        CustomerStatus(String wireValue) { this.wireValue = wireValue; }
        public String wireValue() { return wireValue; }
        public static CustomerStatus parse(String value) { return ReportingEnums.parse(CustomerStatus.class, value == null ? "all" : value); }
    }

    public enum MembershipStatus implements WireValue {
        ALL("all"), ACTIVE("active"), PAST_DUE("past_due"), CANCELLED("cancelled"), NONE("none");
        private final String wireValue;
        MembershipStatus(String wireValue) { this.wireValue = wireValue; }
        public String wireValue() { return wireValue; }
        public static MembershipStatus parse(String value) { return ReportingEnums.parse(MembershipStatus.class, value == null ? "all" : value); }
    }

    public enum CustomerSort implements WireValue {
        LAST_ACTIVITY_DESC("last-activity-desc"), SPEND_DESC("spend-desc"), SPEND_ASC("spend-asc"),
        NAME_ASC("name-asc"), NAME_DESC("name-desc");
        private final String wireValue;
        CustomerSort(String wireValue) { this.wireValue = wireValue; }
        public String wireValue() { return wireValue; }
        public static CustomerSort parse(String value) { return ReportingEnums.parse(CustomerSort.class, value == null ? "last-activity-desc" : value); }
    }

    private static <E extends Enum<E> & WireValue> E parse(Class<E> type, String value) {
        return Arrays.stream(type.getEnumConstants())
                .filter(candidate -> candidate.wireValue().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported value '" + value + "'"));
    }
}
