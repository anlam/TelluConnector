package no.prediktor.apis.service;

import java.util.List;

import no.prediktor.apis.model.ApisItem;
import no.prediktor.apis.model.ApisItemValue;


public interface ApisService {
	
	
	public List<ApisItem> getAllItems();
	public List<String> getAllItemsName();
	public ApisItemValue getItemByName(String name);
	public boolean setItemValue(String name, ApisItemValue value);
	public boolean setItemsValue(List<ApisItem> items);

}

