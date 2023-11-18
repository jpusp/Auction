package model;

public enum ActionType {
    AUCTION_BID("AUCTION_BID"),
    END_OF_AUCTION("END_OF_AUCTION"),
    AUCTION_WINNER("AUCTION_WINNER"),
    BID_SUCCESSFUL("BID_SUCCESSFUL"),
    BID_ERROR("BID_ERROR"),
    BID_NEW_VALUE("BID_NEW_VALUE"),
    BID_CURRENT_VALUE("BID_CURRENT_VALUE"),
    LOGIN("LOGIN"),
    NEW_USER("NEW_USER"),
    ALL_USERS("All_USERS"),
    TIMER_TICK("TIMER_TICK");

    private final String value;

    ActionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ActionType fromValue(String value) {
        for (ActionType type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
