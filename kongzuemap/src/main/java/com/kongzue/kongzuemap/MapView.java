//package com.kongzue.kongzuemap;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.DashPathEffect;
//import android.graphics.Paint;
//import android.graphics.PathEffect;
//import android.graphics.PixelFormat;
//import android.graphics.PointF;
//import android.graphics.PorterDuff;
//import android.graphics.PorterDuffXfermode;
//import android.os.Build;
//import android.support.annotation.Nullable;
//import android.support.annotation.RequiresApi;
//import android.support.v4.view.MotionEventCompat;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//
//import com.kongzue.kongzuemap.util.MapPoint;
//
//import java.util.List;
//
///**
// * Author: @Kongzue
// * Github: https://github.com/kongzue/
// * Homepage: http://kongzue.com/
// * Mail: myzcxhh@live.cn
// * CreateTime: 2018/7/23 17:02
// */
//public class MapView extends View {
//
//    public MapView(Context context) {
//        super(context);
//    }
//
//    public MapView(Context context, @Nullable AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    public MapView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }
//
//    private List<MapPoint> mapPointList;
//
//    public List<MapPoint> getMapPointList() {
//        return mapPointList;
//    }
//
//    public void setMapPointList(List<MapPoint> mapPointList) {
//        this.mapPointList = mapPointList;
//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);       //硬件加速关闭
//        invalidate();   //重绘
//    }
//
//    private float min_x, min_y, max_x, max_y;
//    private float zoomProportion = 100;
//    private MapPoint aimPoint;
//    private MapPoint localPoint;
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);       //开启抗锯齿
//
//        //是否有导航
//        for (MapPoint mapPoint : mapPointList) {
//            if (mapPoint.isAim()) {
//                aimPoint = mapPoint;
//            }
//            if (mapPoint.isLocation()) {
//                localPoint = mapPoint;
//            }
//        }
//        if (aimPoint != null && localPoint != null) {
//            float delta_x_ofPoint = (localPoint.getX() - aimPoint.getX());
//            float delta_y_ofPoint = (localPoint.getY() - aimPoint.getY());
//
//            float bl_x = delta_x_ofPoint / getWidth();
//            float bl_y = delta_y_ofPoint / getHeight();
//            log("bl_x=" + bl_x + "      bl_y=" + bl_y);
//            if (delta_y_ofPoint > delta_x_ofPoint) {
//                //y轴边长
//                zoomProportion = 0.4f/bl_y;
//                log("y.zoomProportion=" + zoomProportion);
//            } else {
//                //x轴边长
//                zoomProportion = 0.4f/bl_x;
//                log("x.zoomProportion=" + zoomProportion);
//            }
//
//            if (zoomProportion < 0) zoomProportion = -zoomProportion;
//        }
//
//        //获取边界信息
//        //初始化
//        min_x = mapPointList.get(0).getX() * zoomProportion;
//        min_y = mapPointList.get(0).getY() * zoomProportion;
//        max_x = mapPointList.get(0).getX() * zoomProportion;
//        max_y = mapPointList.get(0).getY() * zoomProportion;
//        if (mapPointList != null && !mapPointList.isEmpty()) {
//            for (MapPoint mapPoint : mapPointList) {
//                if (mapPoint.getX() * zoomProportion < min_x)
//                    min_x = mapPoint.getX() * zoomProportion;
//                if (mapPoint.getY() * zoomProportion < min_y)
//                    min_y = mapPoint.getY() * zoomProportion;
//                if (mapPoint.getX() * zoomProportion > max_x)
//                    max_x = mapPoint.getX() * zoomProportion;
//                if (mapPoint.getY() * zoomProportion > max_y)
//                    max_y = mapPoint.getY() * zoomProportion;
//            }
//        }
//
//        if (aimPoint != null && localPoint != null) {
//            //有导航点时
//            //1.先获取两点（目标点和定位点）最大、最小范围
//            float nav_min_x, nav_min_y, nav_max_x, nav_max_y;
//            if (aimPoint.getX() < localPoint.getX()) {
//                nav_min_x = aimPoint.getX() * zoomProportion - min_x;
//                nav_max_x = localPoint.getX() * zoomProportion - min_x;
//            } else {
//                nav_min_x = localPoint.getX() * zoomProportion - min_x;
//                nav_max_x = aimPoint.getX() * zoomProportion - min_x;
//            }
//            if (aimPoint.getY() < localPoint.getY()) {
//                nav_min_y = aimPoint.getY() * zoomProportion - min_y;
//                nav_max_y = localPoint.getY() * zoomProportion - min_y;
//            } else {
//                nav_min_y = localPoint.getY() * zoomProportion - min_y;
//                nav_max_y = aimPoint.getY() * zoomProportion - min_y;
//            }
//            //绘制导航边线
//            paint.setPathEffect(null);
//            paint.setStyle(Paint.Style.STROKE);
//            paint.setColor(Color.GREEN);
////            canvas.translate(getWidth() / 2 - (nav_max_x - nav_min_x) / 2 - nav_min_x, getHeight() / 2 - (nav_max_y - nav_min_y) / 2 - nav_max_y);       //位移距离
////            canvas.translate(-nav_min_x*0.5f,-nav_min_y*0.5f);       //位移距离
//            float x_scale = (max_x - min_x) / (nav_max_x - nav_min_x);
//            float y_scale = (max_y - min_y) / (nav_max_y - nav_min_y);
//            mapScale = x_scale > y_scale ? x_scale : y_scale;
//            doubleTouchMidPoint = getMidPoint(new PointF(nav_min_x,nav_min_y),new PointF(nav_max_x,nav_max_y));
//            mapScale = mapScale*0.8f;
////            canvas.scale(mapScale, mapScale,nav_min_x);
//
//            float delta_x_ofMap = localPoint.getX() * zoomProportion - min_x - getWidth() / 2;
//            float delta_y_ofMap = localPoint.getY() * zoomProportion - min_y - getHeight() / 2;
//
////            log((localPoint.getX() * zoomProportion - min_x) + "," + (localPoint.getY() * zoomProportion - min_y));
////            log(delta_x_ofMap + "," + delta_y_ofMap);
//
//            moveDistance = new PointF(delta_x_ofMap, delta_y_ofMap);
//
//        }
//
//        if (moveDistance != null) {
//            canvas.translate(-moveDistance.x, -moveDistance.y);       //位移距离
//        } else {
//            moveDistance = new PointF(0, 0);
//        }
//        if (doubleTouchMidPoint != null) {
//            canvas.scale(mapScale, mapScale,
//                         moveDistance.x + doubleTouchMidPoint.x, moveDistance.y + doubleTouchMidPoint.y); //缩放
//        } else {
//            canvas.scale(mapScale, mapScale); //缩放
//        }
//
//        //绘制边线
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setColor(Color.BLACK);
//        canvas.drawRect(-100, -100, max_x - min_x + 100, max_y - min_y + 100, paint);
//
//        //开始画
//        if (mapPointList != null && !mapPointList.isEmpty()) {
//            for (MapPoint mapPoint : mapPointList) {
//                float r = mapPoint.getR()*zoomProportion;
//                if (mapPoint.isAim() || mapPoint.isLocation()) {
//                    paint.setStyle(Paint.Style.FILL);
//                    if (mapPoint.isAim()) {
//                        paint.setColor(Color.RED);
//                    }
//                    if (mapPoint.isLocation()) {
//                        paint.setColor(Color.BLUE);
//                    }
//                } else {
//                    paint.setStyle(Paint.Style.STROKE);
//                    paint.setColor(Color.BLACK);
//                }
//                float x = (mapPoint.getX() * zoomProportion - min_x);
//                float y = (mapPoint.getY() * zoomProportion - min_y);
//                //画圈
//                paint.setStrokeWidth(1.5f);
//                canvas.drawCircle(x, y, r, paint);
//                //画字
//                if (mapPoint.getLabel() != null && !mapPoint.getLabel().isEmpty()) {
//                    if (mapPoint.isAim()) {
//                        paint.setColor(Color.WHITE);
//                    }
//                    paint.setStyle(Paint.Style.FILL);
//                    paint.setTextAlign(Paint.Align.CENTER);     //设置居中绘制文字
//                    paint.setTextSize(mapPoint.getR()* zoomProportion * 0.8f); //设置字号
//                    canvas.drawText(mapPoint.getLabel(), x, y +  mapPoint.getR()* zoomProportion * 0.8f / 2, paint);
//                }
//            }
//
//            //若有导航路线则显示导航
//            if (aimPoint != null && localPoint != null) {
//                paint.setColor(Color.BLUE);
//                paint.setStyle(Paint.Style.STROKE);
//                PathEffect effects = new DashPathEffect(new float[]{10, 10, 10, 10}, 1);
//                paint.setPathEffect(effects);
//                canvas.drawLine((float) (localPoint.getX() * zoomProportion - min_x), (float) (localPoint.getY() * zoomProportion - min_y),
//                                (float) (aimPoint.getX() * zoomProportion - min_x), (float) (aimPoint.getY() * zoomProportion - min_y), paint);
//
//                log("已经绘制导航");
//            } else {
//                log("没有导航点无法进行导航");
//            }
//        }
//
//        super.onDraw(canvas);
//    }
//
//    private boolean touchFlag;
//    private PointF touchPoint;
//    private PointF moveDistance;        //地图位移的距离
//    private PointF movedDistance;       //记录上一次已经位移的距离
//
//    private double doubleTouchDistance;     //记录双指触摸按下时距离
//    private PointF doubleTouchMidPoint;     //记录双指触摸时中心点
//    private float mapScale = 1.0f;            //地图缩放比
//    private double mapScale_Temp = 1.0f;            //记录上一次地图缩放比
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        //导航模式
////        if (localPoint != null) {
////            invalidate();   //重绘
////            return false;
////        }
//
//        if (MotionEventCompat.getPointerCount(event) == 1) {        //单指触摸
//            switch (event.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    log("ACTION_DOWN");
//                    touchFlag = true;
//                    if (touchPoint == null) {
//                        touchPoint = new PointF(event.getX(), event.getY());
//                    } else {
//                        touchPoint.set(event.getX(), event.getY());
//                    }
//                    if (moveDistance != null) {
//                        if (movedDistance == null) {
//                            movedDistance = new PointF(moveDistance.x, moveDistance.y);
//                        } else {
//                            movedDistance.set(moveDistance.x, moveDistance.y);
//                        }
//                    } else {
//                        if (movedDistance == null) {
//                            movedDistance = new PointF(0, 0);
//                        } else {
//                            movedDistance.set(0, 0);
//                        }
//                    }
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    if (touchFlag) {
//                        float deltaX = touchPoint.x - event.getX();
//                        float deltaY = touchPoint.y - event.getY();
//
//                        if (mapPointList != null && !mapPointList.isEmpty()) {
//                            if (moveDistance == null) {
//                                moveDistance = new PointF(deltaX / mapScale, deltaY / mapScale);
//                            } else {
//                                moveDistance.set(movedDistance.x + deltaX / mapScale, movedDistance.y + deltaY / mapScale);
//                            }
//                            invalidate();   //重绘
//                        }
//                    }
//                    break;
//                case MotionEvent.ACTION_UP:
//                    log("ACTION_UP");
//                    touchFlag = false;
//                    break;
//                default:
//                    break;
//            }
//        } else if (MotionEventCompat.getPointerCount(event) == 2) {             //双指触摸
//
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
//        }
//        return true;
//    }
//
//    private PointF getMidPoint(MotionEvent event) {
//        float midX = (event.getX(1) + event.getX(0)) / 2;
//        float midY = (event.getY(1) + event.getY(0)) / 2;
//        return new PointF(midX, midY);
//    }
//
//    private PointF getMidPoint(PointF e1, PointF e2) {
//        float midX = (e1.x + e2.x) / 2;
//        float midY = (e1.y + e2.y) / 2;
//        return new PointF(midX, midY);
//    }
//
//    //两点间的距离
//    private float getDistance(MotionEvent event) {
//        float x = event.getX(1) - event.getX(0);
//        float y = event.getY(1) - event.getY(0);
//        float distance = (float) Math.sqrt(x * x + y * y);
//        return distance;
//    }
//
//    private void log(Object o) {
//        Log.i(">>>basemap:", o.toString());
//    }
//}
