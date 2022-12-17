package br.com.camelodev.conversoespdf.config;

import java.util.Map;

import javax.faces.context.FacesContext;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

public class ViewScope implements Scope {
	public Object get(String name, ObjectFactory<?> objectFactory) {

		if (FacesContext.getCurrentInstance().getViewRoot() != null) {
			Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap(); // Map do JSF
																										// cujo os
																										// valores são
																										// referentes ao
																										// escopo de
																										// view

			if (viewMap.containsKey(name)) {
				return viewMap.get(name);
			} else {
				Object object = objectFactory.getObject();
				viewMap.put(name, object);
				return object;
			}

		} else {
			return null;
		}

	}





	public Object remove(String name) {

		if (FacesContext.getCurrentInstance().getViewRoot() != null) {
			return FacesContext.getCurrentInstance().getViewRoot().getViewMap().remove(name);
		} else {
			return null;
		}

	}





	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
	}





	@Override
	public Object resolveContextualObject(String key) {
		return null;
	}





	@Override
	public String getConversationId() {
		return null;
	}
}
