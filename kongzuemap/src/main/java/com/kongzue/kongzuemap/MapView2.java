package com.kongzue.kongzuemap;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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
 * CreateTime: 2018/7/23 19:11
 */
public class MapView2 extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    
    private List<MapPoint> mapPointList;
    
    private SurfaceHolder mHolder;
    private Canvas canvas;//绘图的画布
    private boolean mIsDrawing;//控制绘画线程的标志位
    
    public MapView2(Context context) {
        super(context);
        initView();
    }
    
    public MapView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    
    public MapView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    
    public MapView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }
    
    private void initView() {
        mHolder = getHolder();//获取SurfaceHolder对象
        mHolder.addCallback(this);//注册SurfaceHolder的回调方法
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
        this.setZOrderOnTop(true);
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        new Thread(this).start();
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }
    
    public static final int TIME_IN_FRAME = 30;
    
    @Override
    public void run() {
        while (mIsDrawing) {
            /**取得更新之前的时间**/
            long startTime = System.currentTimeMillis();
            /**在这里加上线程安全锁**/
            synchronized (mHolder) {
                /**拿到当前画布 然后锁定**/
                canvas = mHolder.lockCanvas();
                draw();
            }
            /**取得更新结束的时间**/
            long endTime = System.currentTimeMillis();
            /**计算出一次更新的毫秒数**/
            int diffTime = (int) (endTime - startTime);
            /**确保每次更新时间为30帧**/
            while (diffTime <= TIME_IN_FRAME) {
                diffTime = (int) (System.currentTimeMillis() - startTime);
                /**线程等待**/
                Thread.yield();
            }
        }
    }
    
    private MapPoint aimPoint;          //目标点
    private MapPoint localPoint;        //定位点
    private float min_x, min_y, max_x, max_y;       //地图边界
    private float defaultZoom = 50;             //此值是为了能看清楚而设置的缩放比
    
    private void draw() {
        try {
            
            //位移距离
            if (moveDistance!=null)canvas.translate(-moveDistance.x, -moveDistance.y);
            
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
            
            //绘制地图边界
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.BLACK);
            canvas.drawRect(-100, -100, max_x - min_x + 100, max_y - min_y + 100, paint);
            
            //绘制点
            for (MapPoint mapPoint : mapPointList) {
                paint.setStyle(Paint.Style.FILL);
                paint.setTextAlign(Paint.Align.CENTER);     //设置居中绘制文字
                paint.setTextSize(18); //设置字号
                canvas.drawText(mapPoint.getLabel(), mapPoint.x * defaultZoom - min_x, mapPoint.y * defaultZoom - min_y, paint);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null)
                mHolder.unlockCanvasAndPost(canvas);//保证每次都将绘图的内容提交
        }
    }
    
    private boolean touchFlag;
    private PointF touchPoint;
    private PointF moveDistance;        //地图位移的距离
    private PointF movedDistance;       //记录上一次已经位移的距离
    private float mapScale = 1.0f;            //地图缩放比
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
        
//            if (localPoint != null) {
//                invalidate();   //重绘
//                return false;
//            }
//
//            switch (event.getAction() & MotionEvent.ACTION_MASK) {           //处理多点触摸
//                case MotionEvent.ACTION_POINTER_DOWN:
//                    mapScale_Temp = mapScale;
//                    doubleTouchDistance = getDistance(event);
//                    doubleTouchMidPoint = getMidPoint(event);
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    double distance = getDistance(event);
//                    if (distance > 10f) {
//                        mapScale = (float) (mapScale_Temp * distance / doubleTouchDistance);
//                        invalidate();   //重绘
//                    }
//                    break;
//                case MotionEvent.ACTION_POINTER_UP:
//                    log("ACTION_POINTER_UP");
//                    touchFlag = false;
//                    break;
//
//            }
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
    
    //清空
    private void cleanAll(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }
    
    public void setMapPointList(List<MapPoint> mapPointList) {
        this.mapPointList = mapPointList;
    }
    
    public List<MapPoint> getMapPointList() {
        return mapPointList;
    }
}
