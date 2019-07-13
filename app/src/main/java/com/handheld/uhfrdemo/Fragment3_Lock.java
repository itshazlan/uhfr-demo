package com.handheld.uhfrdemo;

import com.BRMicro.Tools;
import com.handheld.uhfr.R;
import com.uhf.api.cls.Reader;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

public class Fragment3_Lock extends Fragment implements View.OnClickListener{
	private View view;
	private Spinner spinnerEPC ;
	private Spinner spinnerMembank ;
	private Spinner spinnerAction ;
	private EditText editAccess ;
	private Button btnLock ;
	private CheckBox checkBoxFilter;

	private String[] maskArray ;
	private String[] actionArray ;

	private byte[] password ;
	private byte[] epc ;
	private List<String> listEPC ;
	private String selectEPC ;

	Reader.Lock_Obj lock_obj = null;//lock bank
	Reader.Lock_Type lock_type =null;//lock type
	private int ltype;
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.fragment_lock, null);
		initView() ;
		return view;
	}
	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden){

		}else {
			epcSet = MainActivity.mSetEpcs ;
			epcSet = MainActivity.mSetEpcs;
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
	}
	private void initView(){
		spinnerEPC = (Spinner) view.findViewById(R.id.spinner_select_epc) ;
		spinnerMembank = (Spinner) view.findViewById(R.id.spinner_select_membank) ;
		spinnerAction = (Spinner) view.findViewById(R.id.spinner_select_lock_action) ;
//		editTips = (EditText) view.findViewById(R.id.editText_tips) ;
		editAccess = (EditText) view.findViewById(R.id.edittext_access) ;
		btnLock = (Button) view.findViewById(R.id.button_lock) ;
		checkBoxFilter = (CheckBox) view.findViewById(R.id.checkbox_filter_epc);

		maskArray = getResources().getStringArray(R.array.spilockbank) ;
		actionArray = getResources().getStringArray(R.array.spilocktype) ;

		initSpinner() ;
		btnLock.setOnClickListener(this);
	}
	private Set<String> epcSet;
	private void initSpinner(){
		selectEPC = null ;
		epcSet = MainActivity.mSetEpcs;
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
		spinnerMembank.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, maskArray));
		spinnerMembank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				switch (position){
					case 0:
						lock_obj = Reader.Lock_Obj.LOCK_OBJECT_ACCESS_PASSWD;
						break;
					case 1:
						lock_obj = Reader.Lock_Obj.LOCK_OBJECT_KILL_PASSWORD;
						break;
					case 2:
						lock_obj = Reader.Lock_Obj.LOCK_OBJECT_EPC;//epc
						break;
					case 3:
						lock_obj = Reader.Lock_Obj.LOCK_OBJECT_TID;
						break;
					case 4:
						lock_obj = Reader.Lock_Obj.LOCK_OBJECT_USER;
						break;
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				lock_obj = Reader.Lock_Obj.LOCK_OBJECT_ACCESS_PASSWD;
			}
		});

		spinnerAction.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, actionArray));
		spinnerAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				ltype = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	@Override
	public void onClick(View v) {
		if(checkBoxFilter.isChecked()&&selectEPC == null){
			showToast(getResources().getString(R.string.please_inventory_epc));
			return ;
		}
		if(v.getId() == R.id.button_lock){
			lock() ;
		}
	}

	private void lock() {
// lock type jude:
		if(lock_obj== Reader.Lock_Obj.LOCK_OBJECT_ACCESS_PASSWD)
		{

			if(ltype==0)
				lock_type= Reader.Lock_Type.ACCESS_PASSWD_UNLOCK;
			else if(ltype==1)
				lock_type= Reader.Lock_Type.ACCESS_PASSWD_LOCK;
			else if(ltype==2)
				lock_type= Reader.Lock_Type.ACCESS_PASSWD_PERM_LOCK;

		}
		else if(lock_obj== Reader.Lock_Obj.LOCK_OBJECT_KILL_PASSWORD)
		{

			if(ltype==0)
				lock_type= Reader.Lock_Type.KILL_PASSWORD_UNLOCK;
			else if(ltype==1)
				lock_type= Reader.Lock_Type.KILL_PASSWORD_LOCK;
			else if(ltype==2)
				lock_type= Reader.Lock_Type.KILL_PASSWORD_PERM_LOCK;
		}
		else if(lock_obj == Reader.Lock_Obj.LOCK_OBJECT_EPC)
		{

			if(ltype==0)
				lock_type= Reader.Lock_Type.EPC_UNLOCK;
			else if(ltype==1)
				lock_type= Reader.Lock_Type.EPC_LOCK;
			else if(ltype==2)
				lock_type= Reader.Lock_Type.EPC_PERM_LOCK;
		}
		else if(lock_obj == Reader.Lock_Obj.LOCK_OBJECT_TID)
		{

			if(ltype==0)
				lock_type= Reader.Lock_Type.TID_UNLOCK;
			else if(ltype==1)
				lock_type= Reader.Lock_Type.TID_LOCK;
			else if(ltype==2)
				lock_type= Reader.Lock_Type.TID_PERM_LOCK;
		}
		else if(lock_obj== Reader.Lock_Obj.LOCK_OBJECT_USER)
		{

			if(ltype==0)
				lock_type= Reader.Lock_Type.USER_UNLOCK;
			else if(ltype==1)
				lock_type= Reader.Lock_Type.USER_LOCK;
			else if(ltype==2)
				lock_type= Reader.Lock_Type.USER_PERM_LOCK;
		}

		String accessStr = editAccess.getText().toString().trim();//password
		if (accessStr == null || accessStr.length() != 8) {
			showToast(getResources().getString(R.string.please_access_password));
			return;
		}
		password = Tools.HexString2Bytes(accessStr);
		epc = Tools.HexString2Bytes(selectEPC);
		Reader.READER_ERR er;
		//lock：
		if (checkBoxFilter.isChecked())
			er  = MainActivity.mUhfrManager.lockTagByFilter(lock_obj,lock_type,password,(short)1000,epc,1,2,true);
		else
			er  = MainActivity.mUhfrManager.lockTag(lock_obj,lock_type,password,(short)1000);
		if (er== Reader.READER_ERR.MT_OK_ERR) {
			showToast("Lock Success!");
//			editTips.append("Lock Success!" + "\n");
		} else {
			showToast("Lock Fail!");
//			editTips.append("Lock Fail!" + "\n");
		}
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
