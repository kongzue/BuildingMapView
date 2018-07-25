package com.kongzue.kongzuemap.util;

import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/7/23 17:03
 */
public class MapPoint extends PointF {
    
    public float x;
    public float y;
    public String label;
    
    public MapPoint(float y, float x, String label) {
        this.x = x;
        this.y = y;
        this.label = label;
    }

    
    public MapPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public float getX() {
        return (float) x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return (float) y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public PointF toPrintF() {
        return new PointF((float) x, (float) y);
    }
    
    public MapPoint toLog() {
        Log.i("MapPoint", "{" +
                "x=" + x +
                ", y=" + y +
                ", label=" + label +
                '}');
        return this;
    }
}

