package com.kongzue.mapdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.kongzue.kongzuemap.MapView;
import com.kongzue.kongzuemap.util.MapPoint;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    private static final int UP = 1;
    private static final int DOWN = 2;
    private static final int LEFT = 3;
    private static final int RIGHT = 4;
    
    private MapView mapView;
    private Button btnTestLeft;
    private Button btnTestRight;
    private Button btnTestUp;
    private Button btnTestDown;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mapView = findViewById(R.id.mapView);
        btnTestLeft = findViewById(R.id.btn_test_left);
        btnTestRight = findViewById(R.id.btn_test_right);
        btnTestUp = findViewById(R.id.btn_test_up);
        btnTestDown = findViewById(R.id.btn_test_down);
        
        //锁定不允许自己缩放位移
        mapView.setMapTouchLock(false);
        
        //加载所有测试点
        initTestPoint();
        mapView.setMapPointList(mapPointList);
        
        //设置目标点和当前点
        mapView.setAimPoint(DemoPoint.getAimPoint());
        mapView.setLocPoint(DemoPoint.getLocalPoint());
        
        btnTestDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(DOWN);
            }
        });
        btnTestUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(UP);
            }
        });
        btnTestLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(LEFT);
            }
        });
        btnTestRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                go(RIGHT);
            }
        });
        
    }
    
    private void go(int where) {
        MapPoint locPoint = mapView.getLocPoint();
        if (locPoint==null)return;
        switch (where) {                 //注意此处坐标系xy颠倒
            case UP:
                locPoint.subX();
                break;
            case DOWN:
                locPoint.addX();
                break;
            case LEFT:
                locPoint.subY();
                break;
            case RIGHT:
                locPoint.addY();
                break;
        }
        mapView.setLocPoint(locPoint);
    }
    
    private List<MapPoint> mapPointList;    //所有坐标点
    
    private void initTestPoint() {
        mapPointList = DemoPoint.getAllTestPoint();             //DemoPoint为点样例库，暂不对外提供，若需要尝试，MapPoint的使用样例为：new MapPoint(12000.122f, 546600.111f, "A123")
    }
    
}
