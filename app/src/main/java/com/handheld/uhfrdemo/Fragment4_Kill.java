package com.handheld.uhfrdemo;

import com.BRMicro.Tools;
import com.handheld.uhfr.R;
import com.uhf.api.cls.Reader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Fragment4_Kill extends Fragment implements View.OnClickListener{
	private View view;

	private Spinner spinnerEPC ;
	private EditText editKillPwd ;
	private EditText editTips  ;
	private Button btnKill ;
	private CheckBox checkBoxFilter;

	private byte[] password ;
	private byte[] epc ;
	private List<String> listEPC ;
	private String selectEPC ;

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		  view= inflater.inflate(R.layout.fragment_kill, null);  
		initView();
		return view/*super.onCreateView(inflater, container, savedInstanceState)*/;
	}
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden) {

		} else {
			Set<String> epcSet = MainActivity.mSetEpcs;
			if (epcSet != null) {
				listEPC = new ArrayList<String>();
				listEPC.addAll(epcSet);
				spinnerEPC.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, listEPC));
				spinnerEPC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						selectEPC = listEPC.get(position);
					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
			}
		}
	}
	private void initView() {
		editKillPwd = (EditText) view.findViewById(R.id.editText_kill_password) ;
		editTips = (EditText) view.findViewById(R.id.editText_tips) ;
		spinnerEPC = (Spinner) view.findViewById(R.id.spinner_select_epc) ;
		btnKill = (Button) view.findViewById(R.id.button_kill) ;
		checkBoxFilter = (CheckBox) view.findViewById(R.id.checkbox_filter_epc);
		btnKill.setOnClickListener(this);
		selectEPC = null ;
		Set<String> epcSet = MainActivity.mSetEpcs;
		if(epcSet != null){
			listEPC = new ArrayList<String>() ;
			listEPC.addAll(epcSet) ;
			spinnerEPC.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, listEPC));
			spinnerEPC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					selectEPC = listEPC.get(position) ;
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		if(checkBoxFilter.isChecked()&&selectEPC == null){
			showToast(getResources().getString(R.string.please_inventory_epc));
			return ;
		}
		kill() ;
	}

	private void kill() {
		String accessStr = editKillPwd.getText().toString().trim() ;
		if(accessStr == null || accessStr.length() != 8 ){
			showToast(getResources().getString(R.string.please_access_password));
			return ;
		}
		password = Tools.HexString2Bytes(accessStr) ;
		epc = Tools.HexString2Bytes(selectEPC) ;
		Reader.READER_ERR er ;
		//kill tag
		if (checkBoxFilter.isChecked())
			er= MainActivity.mUhfrManager.killTagByFilter(password,(short) 1000,epc,1,2,true);
		else
			er = MainActivity.mUhfrManager.killTag(password,(short) 1000);
		if(er == Reader.READER_ERR.MT_OK_ERR) {
			editTips.append(selectEPC + getResources().getString(R.string.kill) + getResources().getString(R.string.success)+"\n");
		}else{
			Log.e("kill fail",er.toString());
			editTips.append(selectEPC + getResources().getString(R.string.kill) + getResources().getString(R.string.fail)+"\n");

		}
	}
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		
	}
	private Toast mToast;
	private void showToast(String info){
		if (mToast==null)
			mToast = Toast.makeText(getActivity(),info,Toast.LENGTH_SHORT);
		else
			mToast.setText(info);
		mToast.show();
	}
}
