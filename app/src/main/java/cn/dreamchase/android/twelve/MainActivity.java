package cn.dreamchase.android.twelve;


import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;


/**
 * -百度地图
 */
public class MainActivity extends AppCompatActivity {

    private MapView mapView;

    private BaiduMap baiduMap;

    private boolean isFirstLoc = true; // 是否首次定位

    private LocationClient locationClient = null;//定位控制类

    @Override
    protected void onCreate(@Nullable Bundle savedInstancState) {
        super.onCreate(savedInstancState);

        // 在使用SDK各个组件之前初始化context信息，传入ApplicationContext
        // 注意该方法要在setContentView() 之前实现，在真实开发中，这句代码放到自定义的Application类中比较合适
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView);

        baiduMap = mapView.getMap();

        baiduMap.setMyLocationEnabled(true); // 开启定位图层

        // 用户自定义定位图标
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.mipmap.icon_geo);

        // 参数1：三个值
        //      .. LocationMode.COMPASS 为罗盘形状，显示定位方向圈，保持定位图标在地图中心
        //      .. LocationMode.FOLLOWING 为跟随形态，保持定额我i图标在地图中心
        //      .. LocationMode.NORMAL 为普通形态，更新定位数据时不对地图做任何操作
        // 参数2：是否允许显示方向信息
        // 参数3：用户自定义定位图标
        MyLocationConfiguration configuration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,
                true, bitmapDescriptor);

        baiduMap.setMyLocationConfiguration(configuration);

        // 开启定位
        startRequestLocation();

    }

    private void startRequestLocation() {
        LocationClientOption option = new LocationClientOption();

        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd0911");
        option.setScanSpan(3000);
        try {
            locationClient = new LocationClient(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        locationClient.setLocOption(option);
        locationClient.registerLocationListener(locationListener);
        locationClient.start();

    }


    private BDLocationListener locationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (null != bdLocation && bdLocation.getLocType() != BDLocation.TypeServerError) {
                Log.i("ansen", "getLongitude :" + bdLocation.getLongitude());

                MyLocationData locationData = new MyLocationData.Builder()
                        .accuracy(bdLocation.getRadius())
                        .latitude(bdLocation.getLatitude())
                        .longitude(bdLocation.getLongitude())
                        .build();

                baiduMap.setMyLocationData(locationData);

                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(bdLocation.getLatitude(),
                            bdLocation.getLongitude());

                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(18.0f); // 设置地图缩放比例
                    MapStatus mapStatus = builder.build();
                    // 以动画方式更新地图状态，动画耗时300ms
                    baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));

                }
            }
        }
    };


    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        locationClient.stop();//停止定位
        baiduMap.setMyLocationEnabled(false);//关闭定位图层
        //在activity执行onDestroy时执行mapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
        mapView = null;
    }
}