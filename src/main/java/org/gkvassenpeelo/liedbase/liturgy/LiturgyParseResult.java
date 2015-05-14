package org.gkvassenpeelo.liedbase.liturgy;

import java.util.ArrayList;
import java.util.List;

public class LiturgyParseResult {

	boolean success = false;

	private List<String> infos = new ArrayList<String>();
	private List<String> warnings = new ArrayList<String>();
	private List<String> errors = new ArrayList<String>();

	public boolean hasWarnings() {
		return warnings.size() > 0;
	}

	public boolean hasErrors() {
		return errors.size() > 0;
	}
	
	public void addInfo(String message) {
		infos.add(message);
	}
	
	public void addWarning(String message) {
		warnings.add(message);
	}
	
	public void addError(String message) {
		errors.add(message);
	}
	
	public List<String> getInfos() {
		return infos;
	}
	
	public List<String> getWarnings() {
		return warnings;
	}
	
	public List<String> getErrors() {
		return errors;
	}
}
