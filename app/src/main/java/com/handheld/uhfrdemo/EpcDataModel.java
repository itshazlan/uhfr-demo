package com.handheld.uhfrdemo;


public class EpcDataModel {
	
	private int id;
	private String rssi;
	private String epc;
	private int count;
	
	
	


	






	public void setepcid(int epcid) {
		this.id = epcid;
	}




	public EpcDataModel(String rssi, String epc, int count) {
		super();
		this.rssi = rssi;
		this.epc = epc;
		this.count = count;
	}



	public EpcDataModel() {
		super();
	}
	
	
	

	public int getepcid() {
		return id;
	}


	public String getrssi() {
		return rssi;
	}


	public void setrssi(String rssi) {
		this.rssi = rssi;
	}
	
	public String getepc() {
		return epc;
	}
	public void setepc(String epc) {
		this.epc = epc;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "rssi" + this.rssi + ",epc " + this.epc;
	}

}
