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

public class Fragment2_ReadAndWrite extends Fragment implements View.OnClickListener{
	private View view;// this fragment UI
	private Spinner spinnerEpc ;//select epc spinner
	private EditText editAccess ;// access password edit text
	private Spinner spinnerMembank ;//select read or write memory bank spinner
	private EditText editStart ;//start address edit text
	private EditText editLength ;//memory length
	private EditText editWriteData ;//data to write edit text
	private EditText editTips ;//tips edit text
	private Button btnWrite ;//write button
	private Button btnRead ;//read button
	private CheckBox checkBoxFilter;//Specify the tag read or write check box
	
	private Set<String> epcSet ;// epc set
	private List<String> listEpc ;//listEPC is inventory epc

	private String[] membankArr = new String[]{"RESERVED", "EPC" , "TID", "USER"} ;//read or write memory bank
	private String selectEpc = "";//select epc
	private int membank ;//read or write memory bank
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.fragment_read_write, null);
		initView();
		return view;
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (hidden){

		}else {
			epcSet = MainActivity.mSetEpcs ;
			initSpinner() ;
		}
	}

	private void initView() {
		spinnerEpc = (Spinner)  view.findViewById(R.id.spinner_select_epc) ;
		editAccess = (EditText)  view.findViewById(R.id.edittext_access) ;
		spinnerMembank = (Spinner)  view.findViewById(R.id.spinner_membank) ;
		editStart = (EditText)  view.findViewById(R.id.edittext_addr) ;
		editLength = (EditText)  view.findViewById(R.id.edittext_length) ;
		editWriteData = (EditText)  view.findViewById(R.id.edittext_write_data) ;
		editTips = (EditText)  view.findViewById(R.id.editText_tips) ;
		btnWrite = (Button)  view.findViewById(R.id.button_write) ;
		btnRead = (Button)  view.findViewById(R.id.button_read) ;
		checkBoxFilter = (CheckBox) view.findViewById(R.id.checkbox_filter_epc);

		btnRead.setOnClickListener(this);
		btnWrite.setOnClickListener(this);

		epcSet = MainActivity.mSetEpcs ;
		initSpinner() ;
	}
	private void initSpinner(){
		selectEpc = "" ;
		if(epcSet != null){
			listEpc = new ArrayList<String>();
			listEpc.addAll(epcSet) ;
			spinnerEpc.setAdapter(new ArrayAdapter<String>(getActivity(),
					android.R.layout.simple_list_item_1, listEpc));
			spinnerEpc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					selectEpc = listEpc.get(position) ;
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {

				}
			});
		}
		spinnerMembank.setAdapter(new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, membankArr));

		spinnerMembank.setSelection(3);
		spinnerMembank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				membank = position  ;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

	}
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		
	}

	@Override
	public void onClick(View v) {
		if(checkBoxFilter.isChecked()&&selectEpc == ""){
			showToast(getResources().getString(R.string.please_inventory_epc));
			return ;
		}
		switch (v.getId()){
			case R.id.button_read:
				readData();
				break ;
			case R.id.button_write:
				writeData() ;
				break ;
		}
	}
	/**
	 * read tag memory bank data
	 */
	private void readData(){
		String startAddrStr = editStart.getText().toString().trim() ;
		String lengthStr = editLength.getText().toString().trim() ;
		String accessStr = editAccess.getText().toString().trim() ;
		if(startAddrStr == null || startAddrStr.length() == 0){
			showToast(getResources().getString(R.string.please_start_addr));
			return ;
		}
		if(lengthStr == null || lengthStr.length() == 0){
			showToast(getResources().getString(R.string.please_length));
			return ;
		}
		if(accessStr == null || accessStr.length() != 8 ){
			showToast(getResources().getString(R.string.please_access_password));
			return ;
		}
		int addr = Integer.valueOf(startAddrStr) ;
		int len =Integer.valueOf(lengthStr) ;
		byte[] epcBytes = Tools.HexString2Bytes(selectEpc) ;
		byte[] accessBytes = Tools.HexString2Bytes(accessStr) ;
		byte[] readBytes = new byte[len*2];
		Reader.READER_ERR er = Reader.READER_ERR.MT_OK_ERR;
		if (checkBoxFilter.isChecked())
			//fbank: 1 epc,2 tid ,3 user
			readBytes =  MainActivity.mUhfrManager.getTagDataByFilter(membank,addr,len,accessBytes,(short)1000,epcBytes,1,2,true) ;
			else
			er = MainActivity.mUhfrManager.getTagData(membank,addr,len,readBytes,accessBytes,(short)1000) ;
		if(er== Reader.READER_ERR.MT_OK_ERR&&readBytes!=null){
			addTips(getResources().getString(R.string.read_success_) +
					Tools.Bytes2HexString(readBytes, readBytes.length)) ;
		}else{
			addTips(getResources().getString(R.string.read_fail_)) ;
		}
	}
	/**
	 * write tag memory bank
	 */
	private void writeData(){
		String startAddrStr = editStart.getText().toString().trim() ;
		String accessStr = editAccess.getText().toString().trim() ;
		String writeStr = editWriteData.getText().toString().trim() ;
		if(startAddrStr == null || startAddrStr.length() == 0){
			showToast(getResources().getString(R.string.please_start_addr));
			return ;
		}
		if(accessStr == null || accessStr.length() != 8 ){
			showToast(getResources().getString(R.string.please_access_password));
			return ;
		}
		if(writeStr == null || writeStr.length() == 0 ){
			showToast(getResources().getString(R.string.please_write_data));
			return ;
		}
		byte[] writeDataBytes = null ;
		try {
			writeDataBytes = Tools.HexString2Bytes(writeStr) ;
			if(writeDataBytes.length%2 != 0){
				showToast(getResources().getString(R.string.please_write_data_type_error));
				return ;
			}
		}catch (Exception e){
			showToast(getResources().getString(R.string.please_write_data_type_error));
			return ;
		}
		int addr = Integer.valueOf(startAddrStr) ;
		byte[] epcBytes = Tools.HexString2Bytes(selectEpc) ;
		byte[] accessBytes = Tools.HexString2Bytes(accessStr) ;

		Reader.READER_ERR er ;
		if (checkBoxFilter.isChecked())
			//change epc:
//			er = MainActivity.mUhfrManager.writeTagEPCByFilter(writeDataBytes, accessBytes,(short)1000, epcBytes,1, 2,true);
			//write data
			er = MainActivity.mUhfrManager.writeTagDataByFilter((char)membank,addr,writeDataBytes,writeDataBytes.length,accessBytes,(short)1000,epcBytes,1,2,true);
		else
		//change epc:
//			er = MainActivity.mUhfrManager.writeTagEPC(writeDataBytes,accessBytes,(short) 1000);
		//write data:
			er = MainActivity.mUhfrManager.writeTagData((char)membank,addr,writeDataBytes,writeDataBytes.length,accessBytes,(short)1000);

		if(er == Reader.READER_ERR.MT_OK_ERR) {
			addTips(getResources().getString(R.string.write_data_success_));
		}else{
			addTips(getResources().getString(R.string.write_data_fail_));
		}
	}

	private void addTips( String info){
		editTips.append(info + "\n");
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
