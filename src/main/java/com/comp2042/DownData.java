package com.comp2042;

public final class DownData {
    private final ClearRow clearRow;
    private final ViewData viewData;
    private final int dropCount;
    private final boolean userEvent;

    public DownData(ClearRow clearRow, ViewData viewData, int dropCount, boolean userEvent) {
        this.clearRow = clearRow;
        this.viewData = viewData;
        this.dropCount = dropCount;
        this.userEvent = userEvent;
    }

    public ClearRow getClearRow() {
        return clearRow;
    }

    public ViewData getViewData() {
        return viewData;
    }
    public int getDropCount() { return dropCount; }
    public boolean isUserEvent() { return userEvent; }
}
