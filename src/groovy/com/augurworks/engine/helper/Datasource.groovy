package com.augurworks.engine.helper

import com.augurworks.engine.rest.ApiClient
import com.augurworks.engine.rest.BarchartClient
import com.augurworks.engine.rest.TDClient

enum Datasource {
	TD(new TDClient()),
	BARCHART(new BarchartClient())

	private final ApiClient apiClient

	Datasource(ApiClient apiClient) {
		this.apiClient = apiClient
	}
}
