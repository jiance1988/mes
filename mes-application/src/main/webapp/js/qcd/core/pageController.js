var QCD = QCD || {};

QCD.PageController = function(_viewName) {
	
	var pageComponents;
	var viewName = _viewName;
	
	function constructor(_this) {
		QCDConnector.windowName = viewName;
		
		var contentElement = $("#content");
		pageComponents = QCDPageConstructor.getChildrenComponents(contentElement.children(), _this);
		QCD.debug(pageComponents);
	}
	
	this.init = function(entityId) {
		var parameters = new Object();
		if (entityId && entityId.trim() != "") {
			parameters.entityId = entityId;
		}
		QCDConnector.sendGet("data", parameters, function(response) {
			setValueData(response);
		});
	}
	
	this.getUpdate = function(componentName, value, listeners) {
		QCD.info(componentName+"->"+value);
		if (listeners) {
			for (var i in listeners) {
				getComponent(listeners[i]).setLoading(true);
			}
		}
		var valuesJson = JSON.stringify(getValueData());
		QCD.info(valuesJson);
		QCDConnector.sendPost("dataUpdate", valuesJson, function(response) {
			QCD.info(response);
			setValueData(response);
			if (listeners) {
				for (var i in listeners) {
					getComponent(listeners[i]).setLoading(false);
				}
			}
		});
	}
	
	this.performSave = function(componentName) {
		QCD.info("save " +componentName);
		var parameters = {
			componentName: componentName,
			data: getValueData()
		};
		var parametersJson = JSON.stringify(parameters);
		QCD.info(parametersJson);
		QCDConnector.sendPost("save", parametersJson, function(response) {
			QCD.info(response);
			//setValueData(response);
		});
	}
	
	function getValueData() {
		var values = new Object();
		for (var i in pageComponents) {
			var value = pageComponents[i].getValue();
			if (value) {
				values[i] = value;
			}
		}
		//QCD.info(values);
		return values;
	}
	
	function setValueData(data) {
		QCD.debug(data);
		for (var i in data.components) {
			var component = pageComponents[i];
			component.setValue(data.components[i]);
		}
	}
	
	function getComponent(componentPath) {
		var componentName = componentPath.split(".")[0];
		var path = componentPath.substring(componentName.length+1);
		return pageComponents[componentName].getComponent(path);
	}
	
	this.getTranslation = function(key) {
		return window.translationsMap[key] ? window.translationsMap[key] : "TT: "+key;
	}
	
	this.goToPage = function(url) {
		var serializationObject = new Object();
//		for (var i in pageElements) {
//			serializationObject[i] = pageElements[i].serialize();
//		}
		window.parent.goToPage(url, serializationObject);
	}
	
	this.goBack = function() {
		window.parent.goBack();
	}
	
	this.onSessionExpired = function() {
		var serializationObject = new Object();
		for (var i in pageElements) {
			serializationObject[i] = pageElements[i].serialize();
		}
		window.parent.onSessionExpired(serializationObject);
	}
	
	constructor(this);
}