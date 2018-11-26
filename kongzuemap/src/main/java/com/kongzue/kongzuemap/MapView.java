package com.kongzue.kongzuemap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.kongzue.kongzuemap.util.MapPoint;

import java.util.List;

/**
 * Author: @Kongzue
 * Github: https://github.com/kongzue/
 * Homepage: http://kongzue.com/
 * Mail: myzcxhh@live.cn
 * CreateTime: 2018/7/23 17:02
 */
public class MapView extends View {
    
    private List<MapPoint> mapPointList;
    
    private MapPoint locPoint;      //当前点
    private MapPoint aimPoint;      //目标点
    private boolean mapTouchLock;   //是否锁定不允许自由缩放位移
    
    public List<MapPoint> getMapPointList() {
        return mapPointList;
    }
    
    public void setMapPointList(List<MapPoint> mapPointList) {
        this.mapPointList = mapPointList;
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);       //硬件加速关闭
        
        //获取地图边界
        min_x = mapPointList.get(0).x * defaultZoom;
        min_y = mapPointList.get(0).y * defaultZoom;
        max_x = mapPointList.get(0).x * defaultZoom;
        max_y = mapPointList.get(0).y * defaultZoom;
        for (MapPoint mapPoint : mapPointList) {
            if (mapPoint.x * defaultZoom < min_x) {
                min_x = mapPoint.x * defaultZoom;
            }
            if (mapPoint.y * defaultZoom < min_y) {
                min_y = mapPoint.y * defaultZoom;
            }
            if (mapPoint.x * defaultZoom > max_x) {
                max_x = mapPoint.x * defaultZoom;
            }
            if (mapPoint.y * defaultZoom > max_y) {
                max_y = mapPoint.y * defaultZoom;
            }
        }
        
        invalidate();   //重绘
    }
    
    public boolean isMapTouchLock() {
        return mapTouchLock;
    }
    
    public MapView setMapTouchLock(boolean mapTouchLock) {
        this.mapTouchLock = mapTouchLock;
        return this;
    }
    
    public MapPoint getLocPoint() {
        return locPoint;
    }
    
    public MapView setLocPoint(MapPoint locPoint) {
        this.locPoint = locPoint;
        refresh();
        return this;
    }
    
    public MapPoint getAimPoint() {
        return aimPoint;
    }
    
    public MapView setAimPoint(MapPoint aimPoint) {
        this.aimPoint = aimPoint;
        refresh();
        return this;
    }
    
    public MapView(Context context) {
        super(context);
    }
    
    public MapView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    
    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
    
    private float min_x, min_y, max_x, max_y;       //地图边界
    private float defaultZoom = 50;             //此值是为了能看清楚而设置的缩放比
    
    @Override
    protected void onDraw(Canvas canvas) {
        try {
            float scale = 1;
            
            //若有导航，计算出位移和缩放比
            if (locPoint != null && aimPoint != null) {
                float deltaX = Math.abs(locPoint.getX() - aimPoint.getX());
                float deltaY = Math.abs(locPoint.getY() - aimPoint.getY());
                
                float scaleX = deltaX * defaultZoom / getWidth();
                float scaleY = deltaY * defaultZoom / getHeight();
                log("scaleX:" + scaleX);
                log("scaleY:" + scaleY);
                
                if (scaleX > scaleY) {
                    scale = 0.7f / scaleX;
                } else {
                    scale = 0.7f / scaleY;
                }
                
                log("================scale:" + scale);
                if (scale > 5) scale = 5;                       //最高放大5倍
                
                log("defaultZoom:" + defaultZoom);
                
                //缩放
                canvas.scale(scale, scale);
                
                PointF midPoint = getMidPoint(locPoint, aimPoint);
                midPoint.x = midPoint.x * defaultZoom - min_x - (getWidth() / 2 / scale);
                midPoint.y = midPoint.y * defaultZoom - min_y - (getHeight() / 2 / scale);
                
                moveDistance = midPoint;
                
            }
            
            //只有当前点，没有目标点的情况
            if (locPoint != null && aimPoint == null) {
                
                scale = 1;
                
                //缩放
                canvas.scale(scale, scale);
                
                PointF midPoint = new PointF(locPoint.x, locPoint.y);
                midPoint.x = midPoint.x * defaultZoom - min_x - (getWidth() / 2 / scale);
                midPoint.y = midPoint.y * defaultZoom - min_y - (getHeight() / 2 / scale);
                
                moveDistance = midPoint;
            }
            
            //位移距离
            if (moveDistance != null) {
                canvas.translate(-moveDistance.x, -moveDistance.y);
                log("move:  x=" + moveDistance.x + "  y=" + moveDistance.y);
            }
            
            //双指缩放
            if (locPoint == null && aimPoint == null && !mapTouchLock) {
                if (doubleTouchMidPoint != null) {
                    canvas.scale(mapScale, mapScale,
                                 moveDistance.x + doubleTouchMidPoint.x, moveDistance.y + doubleTouchMidPoint.y
                    ); //缩放
                } else {
                    canvas.scale(mapScale, mapScale); //缩放
                }
            }
            
            Paint paint = new Paint();
            
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPaint(paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
            paint.setAntiAlias(true);       //开启抗锯齿
            
            //没有任何点时清空画布
            if (mapPointList == null || mapPointList.isEmpty()) {
                cleanAll(canvas);
                return;
            }
            
            //绘制地图边界
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            canvas.drawRect(-100, -100, max_x - min_x + 100, max_y - min_y + 100, paint);
            
            //绘制点
            for (MapPoint mapPoint : mapPointList) {
                int color = Color.BLACK;
                switch (mapPoint.getStage()){
                    case 0:
                        color = Color.rgb(50, 123, 254);            //蓝色
                        break;
                    case 1:
                        color = Color.rgb(100, 217, 100);            //绿色
                        break;
                    case 2:
                        color = Color.rgb(200, 199, 204);            //灰色
                        break;
                }
    
                paint.setStyle(Paint.Style.FILL);
                paint.setTextAlign(Paint.Align.CENTER);     //设置居中绘制文字
                paint.setColor(color);
                paint.setTextSize(18); //设置字号
                canvas.drawText(mapPoint.getLabel(), mapPoint.x * defaultZoom - min_x, mapPoint.y * defaultZoom - min_y + 8, paint);
    
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(color);
                paint.setStrokeWidth(1.5f);
                canvas.drawCircle(mapPoint.x * defaultZoom - min_x, mapPoint.y * defaultZoom - min_y, 26, paint);
            }
            
            //绘制导航虚线
            if (locPoint != null && aimPoint != null) {
                paint.setColor(Color.BLUE);
                paint.setStyle(Paint.Style.STROKE);
                PathEffect effects = new DashPathEffect(new float[]{10, 10, 10, 10}, 1);
                paint.setPathEffect(effects);
                canvas.drawLine(locPoint.x * defaultZoom - min_x, locPoint.y * defaultZoom - min_y,                     //from: loc.x, loc.y
                                aimPoint.x * defaultZoom - min_x, aimPoint.y * defaultZoom - min_y,                     //to: aim.x, aim.y
                                paint
                );
            }
            
            //绘制locPoint
            if (locPoint != null) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.BLUE);
                canvas.drawCircle(locPoint.x * defaultZoom - min_x, locPoint.y * defaultZoom - min_y, 26, paint);
            }
            
            //绘制aimPoint
            if (aimPoint != null) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.RED);
                paint.setStrokeWidth(1.5f);
                canvas.drawCircle(aimPoint.x * defaultZoom - min_x, aimPoint.y * defaultZoom - min_y, 26, paint);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDraw(canvas);
    }
    
    //清空
    private void cleanAll(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
    
    private boolean touchFlag;
    private PointF touchPoint;
    private PointF moveDistance;        //地图位移的距离
    private PointF movedDistance;       //记录上一次已经位移的距离
    private float mapScale = 1.0f;            //地图缩放比
    
    private float mapScale_Temp = 1.0f;
    private float doubleTouchDistance;
    private PointF doubleTouchMidPoint;
    private boolean doubleTouched = false;
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mapTouchLock) {
            return true;
        }
        if (MotionEventCompat.getPointerCount(event) == 1) {        //单指触摸
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touchFlag = true;
                    if (touchPoint == null) {
                        touchPoint = new PointF(event.getX(), event.getY());
                    } else {
                        touchPoint.set(event.getX(), event.getY());
                    }
                    if (moveDistance != null) {
                        if (movedDistance == null) {
                            movedDistance = new PointF(moveDistance.x, moveDistance.y);
                        } else {
                            movedDistance.set(moveDistance.x, moveDistance.y);
                        }
                    } else {
                        if (movedDistance == null) {
                            movedDistance = new PointF(0, 0);
                        } else {
                            movedDistance.set(0, 0);
                        }
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (touchFlag) {
                        float deltaX = touchPoint.x - event.getX();
                        float deltaY = touchPoint.y - event.getY();
                        
                        if (mapPointList != null && !mapPointList.isEmpty()) {
                            if (moveDistance == null) {
                                moveDistance = new PointF(deltaX / mapScale, deltaY / mapScale);
                            } else {
                                moveDistance.set(movedDistance.x + deltaX / mapScale, movedDistance.y + deltaY / mapScale);
                            }
                            invalidate();   //重绘
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    touchFlag = false;
                    break;
                default:
                    break;
            }
        } else if (MotionEventCompat.getPointerCount(event) == 2) {             //双指触摸
            
            if (mapPointList == null) {
                return false;
            }
            
            switch (event.getAction() & MotionEvent.ACTION_MASK) {           //处理多点触摸
                case MotionEvent.ACTION_POINTER_DOWN:
                    doubleTouched = true;
                    //log("ACTION_POINTER_DOWN");
                    mapScale_Temp = mapScale;
                    doubleTouchDistance = getDistance(event);
                    doubleTouchMidPoint = getMidPoint(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    //log("ACTION_MOVE");
                    double distance = getDistance(event);
                    if (distance > 10f) {
                        mapScale = (float) (mapScale_Temp * distance / doubleTouchDistance);
                        invalidate();   //重绘
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    doubleTouched = false;
                    //log("ACTION_POINTER_UP");
                    touchFlag = false;
                    break;
                
            }
        }
        return true;
    }
    
    private PointF getMidPoint(MotionEvent event) {
        float midX = (event.getX(1) + event.getX(0)) / 2;
        float midY = (event.getY(1) + event.getY(0)) / 2;
        return new PointF(midX, midY);
    }
    
    private PointF getMidPoint(PointF e1, PointF e2) {
        float midX = (e1.x + e2.x) / 2;
        float midY = (e1.y + e2.y) / 2;
        return new PointF(midX, midY);
    }
    
    private PointF getMidPoint(MapPoint point1, MapPoint point2) {
        float midX = (point1.x + point2.x) / 2;
        float midY = (point1.y + point2.y) / 2;
        return new PointF(midX, midY);
    }
    
    //两点间的距离
    private float getDistance(MotionEvent event) {
        float x = event.getX(1) - event.getX(0);
        float y = event.getY(1) - event.getY(0);
        float distance = (float) Math.sqrt(x * x + y * y);
        return distance;
    }
    
    private void log(Object o) {
        Log.i(">>>basemap:", o.toString());
    }
    
    public void refresh() {
        invalidate();
    }
}
