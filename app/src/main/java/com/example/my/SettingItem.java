package com.example.my;

public class SettingItem {

    private int iconResId;
    private String title;

    public SettingItem(int iconResId, String title) {
        this.iconResId = iconResId;
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getTitle() {
        return title;
    }
}
