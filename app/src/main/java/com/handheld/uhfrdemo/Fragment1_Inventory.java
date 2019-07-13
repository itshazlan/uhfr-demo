package com.handheld.uhfrdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.BRMicro.Tools;
import com.handheld.uhfr.R;
import com.uhf.api.cls.Reader.TAGINFO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Fragment1_Inventory extends Fragment implements OnCheckedChangeListener,OnClickListener{
		private View view;// this fragment UI
	 	private TextView tvTagCount;//tag count text view
	    private TextView tvTagSum ;//tag sum text view
	    private ListView lvEpc;// epc list view
	    private Button btnStart ;//inventory button
	    private Button btnClear ;// clear button
	    private CheckBox checkMulti ;//multi model check box
	    
	    private Set<String> epcSet = null ; //store different EPC
	    private List<EpcDataModel> listEpc = null;//EPC list
	    private Map<String, Integer> mapEpc = null; //store EPC position
	    private EPCadapter adapter ;//epc list adapter

	    private boolean isMulti = false ;// multi mode flag
	    private int allCount = 0 ;// inventory count

	    private long lastTime =  0L ;// record play sound time
	//handler
	    private Handler handler = new Handler(){
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what){
	                case 1:
	                    String epc = msg.getData().getString("epc");
	                    String rssi = msg.getData().getString("rssi");
	                    if (epc == null || epc.length() == 0) {
	                        return ;
	                    }
	                    int position ;
	                    allCount++ ;

	                    if (epcSet == null) {//first add
	                        epcSet = new HashSet<String>();
	                        listEpc = new ArrayList<EpcDataModel>();
	                        mapEpc = new HashMap<String, Integer>();
	                        epcSet.add(epc);
	                        mapEpc.put(epc, 0);
	                        EpcDataModel epcTag = new EpcDataModel() ;
	                        epcTag.setepc(epc);
	                        epcTag.setrssi(rssi);
	                        epcTag.setCount(1) ;
	                        listEpc.add(epcTag);
	                        adapter = new EPCadapter(getActivity(), listEpc);
	                        lvEpc.setAdapter(adapter);
	                        Util.play(1, 0);
	                        MainActivity.mSetEpcs=epcSet;
	                    }else{
	                        if (epcSet.contains(epc)) {//set already exit
	                            position = mapEpc.get(epc);
	                            EpcDataModel epcOld = listEpc.get(position);
	                            epcOld.setCount(epcOld.getCount()+1);
	                            listEpc.set(position, epcOld);
	                        }else{
	                            epcSet.add(epc);
	                            mapEpc.put(epc, listEpc.size());
	                            EpcDataModel epcTag = new EpcDataModel() ;
	                            epcTag.setepc(epc);
	                            epcTag.setrssi(rssi);
	                            epcTag.setCount(1);
	                            listEpc.add(epcTag);
	                            MainActivity.mSetEpcs = epcSet;
	                        }

	                        if(System.currentTimeMillis() - lastTime > 100){
	                            lastTime = System.currentTimeMillis() ;
	                            Util.play(1, 0);
	                        }
	                        tvTagCount.setText("" +  allCount);
	                        tvTagSum.setText("" + listEpc.size());
	                        adapter.notifyDataSetChanged();

	                    }
	                    
	                    break ;
	            }
	        }
	    } ;
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
			Log.e("f1","create view");
		  view= inflater.inflate(R.layout.fragment_inventory, null);
		  initView();
		  IntentFilter filter = new IntentFilter() ;
	      filter.addAction("android.rfid.FUN_KEY");
	      getActivity().registerReceiver(keyReceiver, filter) ;

		return view/*super.onCreateView(inflater, container, savedInstanceState)*/;
	}
	
	private void initView() {
		tvTagCount = (TextView) view.findViewById(R.id.textView_tag_count);
        lvEpc = (ListView) view.findViewById(R.id.listView_epc);
        btnStart = (Button) view.findViewById(R.id.button_start);
        tvTagSum = (TextView) view.findViewById(R.id.textView_tag) ;
        checkMulti = (CheckBox) view.findViewById(R.id.checkBox_multi) ;
        checkMulti.setOnCheckedChangeListener( this);
        btnClear = (Button) view.findViewById(R.id.button_clear_epc) ;


        lvEpc.setFocusable(false);
        lvEpc.setClickable(false);
        lvEpc.setItemsCanFocus(false);
        lvEpc.setScrollingCacheEnabled(false);
        lvEpc.setOnItemClickListener(null);
        btnStart.setOnClickListener(this);
        btnClear.setOnClickListener(this);
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
//		Log.e("f1","destroy view");
		if (isStart) {
			isStart = false;
			isRunning = false;
			MainActivity.mUhfrManager.stopTagInventory();
		}
		getActivity().unregisterReceiver(keyReceiver);//
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
//		Log.e("f1","pause");
		if (isStart) {
			runInventory();
		}
	}
	private boolean f1hidden = false;
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		f1hidden = hidden;
//		Log.e("hidden", "hide"+hidden) ;
		if (hidden) {
			if (isStart) runInventory();// stop inventory
		}
		if (MainActivity.mUhfrManager!=null) MainActivity.mUhfrManager.setCancleInventoryFilter();
	}


	private boolean isRunning = false ;
    private boolean isStart = false ;
    String epc ;
    //inventory epc
    private Runnable inventoryTask = new Runnable() {
        @Override
        public void run() {
            while(isRunning){
                if (isStart) {
                	List<TAGINFO> list1 ;
                	if (isMulti) { // multi mode
                		list1 = MainActivity.mUhfrManager.tagInventoryRealTime();
					}else{
						//sleep can save electricity
						try {
							Thread.sleep(250);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						list1 = MainActivity.mUhfrManager.tagInventoryByTimer((short)50);
//						//get epc and tid :
//						long time = System.currentTimeMillis();
//						byte[] password = new byte[4];
//						list1 = MainActivity.mUhfrManager.tagEpcOtherInventoryByTimer((short) 50,2,0,6,password);
//						if (list1 != null&&list1.size()>0) {
////							Log.e("time", list1.size() +"");
//							for (TAGINFO tfs:list1) {
//								byte[] epcdata = tfs.EpcId;
//								byte[] tid = tfs.EmbededData;
//							}
//						}
//						//get epc and user
//						list1 = MainActivity.mUhfrManager.tagEpcOtherInventoryByTimer((short) 50,3,0,6,password);
//						if (list1 != null&&list1.size()>0) {
////							Log.e("time", list1.size() +"");
//							for (TAGINFO tfs:list1) {
//								byte[] epcdata = tfs.EpcId;
//								byte[] user = tfs.EmbededData;
//							}
//						}
//						Log.e("time",System.currentTimeMillis() - time +"");
					}
                    if (list1 != null&&list1.size()>0) {//
                        for (TAGINFO tfs:list1) {
                        	byte[] epcdata = tfs.EpcId;

                        	epc = Tools.Bytes2HexString(epcdata, epcdata.length);
                            int rssi = tfs.RSSI;

                            Message msg = new Message() ;
                            msg.what = 1 ;
                            Bundle b = new Bundle();
                            b.putString("epc", epc);
                            b.putString("rssi", rssi+"");
                            msg.setData(b);
                            handler.sendMessage(msg);
						}
                    }
                }
            }
        }
    } ;
	private boolean keyControl = true;
    private void runInventory() {
		if (keyControl) {
			keyControl = false;
			if (!isStart) {
				MainActivity.mUhfrManager.setCancleInventoryFilter();
				isRunning = true;
				if (isMulti) {
					MainActivity.mUhfrManager.setFastMode();
					MainActivity.mUhfrManager.asyncStartReading();
				}else {
					MainActivity.mUhfrManager.setCancleFastMode();
				}
				new Thread(inventoryTask).start();
				checkMulti.setClickable(false);
				checkMulti.setTextColor(Color.GRAY);
				btnStart.setText(getResources().getString(R.string.stop_inventory_epc));
//            Log.e("inventoryTask", "start inventory") ;
				isStart = true;
			} else {
				checkMulti.setClickable(true);
				checkMulti.setTextColor(Color.BLACK);
				if (isMulti)
					MainActivity.mUhfrManager.asyncStopReading();
				else
					MainActivity.mUhfrManager.stopTagInventory();
				try {
					Thread.sleep(100);
				} catch (Exception e) {
					e.printStackTrace();
				}
				isRunning = false;
				btnStart.setText(getResources().getString(R.string.start_inventory_epc));
				isStart = false;
			}
			keyControl = true;
		}
    }
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked) isMulti = false;
		else isMulti = false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
        case R.id.button_start:
            runInventory() ;
            break ;
        case R.id.button_clear_epc:
            clearEpc();
            break ;
    }
	}

	private void clearEpc(){
        if (epcSet != null) {
            epcSet.removeAll(epcSet); //store different EPC
        }
         if(listEpc != null)
         listEpc.removeAll(listEpc);//EPC list
        if(mapEpc != null)
         mapEpc.clear(); //store EPC position
        if(adapter != null)
         adapter.notifyDataSetChanged();
        allCount = 0 ;
        tvTagSum.setText("0");
        tvTagCount.setText("0");
        MainActivity.mSetEpcs.clear();
//        lvEpc.removeAllViews();
    }

    //show tips
	private Toast toast;
    private void showToast(String info) {
		if (toast==null) toast =  Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT);
		else toast.setText(info);
		toast.show();
    }
	//key receiver
	private  long startTime = 0 ;
	private boolean keyUpFalg= true;
	private BroadcastReceiver keyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
			if (f1hidden) return;
            int keyCode = intent.getIntExtra("keyCode", 0) ;
            if(keyCode == 0){//H941
                keyCode = intent.getIntExtra("keycode", 0) ;
            }
//            Log.e("key ","keyCode = " + keyCode) ;
            boolean keyDown = intent.getBooleanExtra("keydown", false) ;
//			Log.e("key ", "down = " + keyDown);
            if(keyUpFalg&&keyDown && System.currentTimeMillis() - startTime > 500){
				keyUpFalg = false;
                startTime = System.currentTimeMillis() ;
				if ( (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F2
						|| keyCode == KeyEvent.KEYCODE_F3 || keyCode == KeyEvent.KEYCODE_F4 ||
						keyCode == KeyEvent.KEYCODE_F5)) {
//                Log.e("key ","inventory.... " ) ;
					runInventory();
				}
                return ;
            }else if (keyDown){
				startTime = System.currentTimeMillis() ;
			}else {
				keyUpFalg = true;
			}

        }
    } ;

}
