package com.alexjlockwood.activity.transitions;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * ============================
 * Author：  hammercui
 * Version： 1.0
 * Time:     2017/4/10
 * Description: 动画开始时View在屏幕的大小，位置
 * Fix History:
 * =============================
 */

public class ZoomInfo implements Parcelable {
    private int screenX;
    private int screenY;
    private int width;
    private int height;

    public int getScreenX() {
        return screenX;
    }

    public void setScreenX(int screenX) {
        this.screenX = screenX;
    }

    public int getScreenY() {
        return screenY;
    }

    public void setScreenY(int screenY) {
        this.screenY = screenY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.screenX);
        dest.writeInt(this.screenY);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
    }

    public ZoomInfo() {
    }

    protected ZoomInfo(Parcel in) {
        this.screenX = in.readInt();
        this.screenY = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
    }

    public static final Creator<ZoomInfo> CREATOR = new Creator<ZoomInfo>() {
        @Override
        public ZoomInfo createFromParcel(Parcel source) {
            return new ZoomInfo(source);
        }

        @Override
        public ZoomInfo[] newArray(int size) {
            return new ZoomInfo[size];
        }
    };
}
