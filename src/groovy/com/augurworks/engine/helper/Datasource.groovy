package com.augurworks.engine.helper

import com.augurworks.engine.rest.ApiClient
import com.augurworks.engine.rest.BarchartClient
import com.augurworks.engine.rest.TDClient

enum Datasource {
	TD('TD', new TDClient()),
	BARCHART('Barchart', new BarchartClient())

	private final String name
	private final ApiClient apiClient

	Datasource(String name, ApiClient apiClient) {
		this.name = name
		this.apiClient = apiClient
	}
}
