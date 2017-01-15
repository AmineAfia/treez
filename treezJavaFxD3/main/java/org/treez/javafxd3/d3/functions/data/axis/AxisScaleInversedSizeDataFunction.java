package org.treez.javafxd3.d3.functions.data.axis;

import org.treez.javafxd3.d3.functions.DataFunction;
import org.treez.javafxd3.d3.scales.Scale;

import netscape.javascript.JSObject;

public class AxisScaleInversedSizeDataFunction implements DataFunction<Double> {
	
	//#region ATTRIBUTES
	
	private Scale<?> scale;		
	
	//#end region
	
	//#region CONSTRUCTORS
	
	public AxisScaleInversedSizeDataFunction(Scale<?> scale){
		this.scale = scale;			
	}
	
	//#end region
	
	//#region METHODS

	@Override
	public Double apply(Object context, Object datum, int index) {
		
		JSObject jsObject = (JSObject) datum;	
		
		Double value = Double.parseDouble(jsObject.eval("this.datum.value").toString());
		Double size = Double.parseDouble(jsObject.eval("this.datum.size").toString());				
		
		Double scaledRightValueInPx = scale.applyForDouble(""+ (value+size));	
		Double scaledLeftValueInPx = scale.applyForDouble("" + value);	
		Double sizeInPx = scaledRightValueInPx-scaledLeftValueInPx;			
		return - sizeInPx;		
	}
	
	//#end region

}

   
