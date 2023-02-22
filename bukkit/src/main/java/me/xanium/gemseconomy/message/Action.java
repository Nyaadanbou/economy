package me.xanium.gemseconomy.message;

public class Action {
    public static final String UPDATE_ACCOUNT = "update_account";
    public static final String DELETE_ACCOUNT = "delete_account";
    public static final String CREATE_ACCOUNT = "create_account";
    public static final String UPDATE_CURRENCY = "update_currency";
    public static final String DELETE_CURRENCY = "delete_currency";
    public static final String CREATE_CURRENCY = "create_currency";

    private Action() {
        throw new UnsupportedOperationException();
    }
}
