package com.example.busline;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.busline.BusLineResult;
import com.baidu.mapapi.search.busline.BusLineSearch;
import com.baidu.mapapi.search.busline.BusLineSearchOption;
import com.baidu.mapapi.search.busline.OnGetBusLineSearchResultListener;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    EditText startEdit, finishEdit;
    Button mButton;
    ListView listView;
    BusLineSearch busLineSearch;
    BusLineSearchOption mBusLineSearchOption;
    PoiSearch mPoiSearch;
    String busLineId;
    ArrayAdapter dataAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        startEdit = (EditText) findViewById(R.id.start);
        finishEdit = (EditText) findViewById(R.id.finish);
        mButton = (Button) findViewById(R.id.search);
        listView = (ListView)findViewById(R.id.list);
        init();
        mButton.setOnClickListener(this);
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        busLineSearch.setOnGetBusLineSearchResultListener(busLineListener);
    }

    public void init(){
        busLineSearch = BusLineSearch.newInstance();
        mBusLineSearchOption = new BusLineSearchOption();
        mPoiSearch = PoiSearch.newInstance();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.search:
                if(startEdit.getText().toString() == null || "".equals(startEdit.getText().toString().replace(" ",""))){
                    Toast.makeText(this,"请输入城市",Toast.LENGTH_SHORT).show();
                    break;
                }
                if(finishEdit.getText().toString() == null || "".equals(finishEdit.getText().toString().replace(" ",""))){
                    Toast.makeText(this,"请输入路线",Toast.LENGTH_SHORT).show();
                    break;
                }
                Log.d("YANG","startEdit.getText().toString() = "+ startEdit.getText().toString());
                mPoiSearch.searchInCity((new PoiCitySearchOption())
                        .city(startEdit.getText().toString()).keyword(finishEdit.getText().toString()));
                break;
        }
    }

    OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {

        public void onGetPoiResult(PoiResult result) {
            Log.d("YANG","onGetPoiResult");
            //获取POI检索结果
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(MainActivity.this,"查询失败",Toast.LENGTH_SHORT).show();
                return;
            }
            //遍历所有POI，找到类型为公交线路的POI
            for (PoiInfo poi : result.getAllPoi()) {
                Log.d("YANG","poi.uid = "+poi.uid + ", poi.type = "+poi.type);
                if (/*poi.type == PoiInfo.POITYPE.BUS_STATION*/poi.type == PoiInfo.POITYPE.BUS_LINE ||poi.type == PoiInfo.POITYPE.SUBWAY_LINE) {
                    //说明该条POI为公交信息，获取该条POI的UID
                    busLineId = poi.uid;
                    if(null!=busLineId) {
                        boolean serchResult = busLineSearch.searchBusLine(mBusLineSearchOption
                                .city(startEdit.getText().toString()).uid(busLineId));
                        Log.d("YANG","result = "+serchResult + ", busLineId = "+busLineId);
                    }else{
                        Toast.makeText(MainActivity.this,"未查找到，请输入其他线路试试……",Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        }

        public void onGetPoiDetailResult(PoiDetailResult result) {
            //获取Place详情页检索结果
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

        }
    };

    OnGetBusLineSearchResultListener busLineListener = new OnGetBusLineSearchResultListener() {
        @Override
        public void onGetBusLineResult(BusLineResult busLineResult) {
            Log.d("YANG","onGetBusLineResult");
            String name;
            if(null == busLineResult){
                Log.d("YANG","busLineResult = null" );
            }
            List<BusLineResult.BusStation> stationList = busLineResult.getStations();
            if(null == busLineResult.getStations()){
                Log.d("YANG","busLineResult.getStations() = null" );
            }
            List<String> datas = new ArrayList<String>();
            if(null == stationList){
                Log.d("YANG","stationList = null" );
                return;
            }
            for (BusLineResult.BusStation station:stationList) {
                name = station.getTitle();
                datas.add(name);
            }
            dataAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, datas);
            listView.setAdapter(dataAdapter);
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPoiSearch.destroy();
        busLineSearch.destroy();
    }
}
