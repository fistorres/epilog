package org.epilogtool.project;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.colomoto.logicalmodel.LogicalModel;
import org.epilogtool.core.Epithelium;
import org.epilogtool.core.topology.RollOver;

public class Project {
	private List<Epithelium> epitheliumList;
	private ProjectModelFeatures modelFeatures;
	private String filenamePEPS;
	private boolean isChanged;
	private ProjectComponentFeatures componentFeatures;

	public Project(){
		this.epitheliumList = new ArrayList<Epithelium>();
		this.modelFeatures = new ProjectModelFeatures();
		this.componentFeatures = new ProjectComponentFeatures();
		this.filenamePEPS = null;
		this.isChanged = true;
	}

	public boolean hasChanged() {
		return isChanged;
	}

	public void setChanged(boolean state) {
		this.isChanged = state;
	}

	public List<Epithelium> getEpitheliumList() {
		return this.epitheliumList;
	}

	public List<String> getEpitheliumNameList() {
		List<String> l = new ArrayList<String>();
		for (Epithelium epi : this.epitheliumList) {
			l.add(epi.getName());
		}
		return l;
	}

	public Epithelium cloneEpithelium(Epithelium epi) {
		Epithelium epiClone = epi.clone();
		epiClone.setName(this.getNextAvailableName(epi.getName()));
		this.epitheliumList.add(epiClone);
		return epiClone;
	}

	private String getNextAvailableName(String basename) {
		List<String> l = new ArrayList<String>();
		for (Epithelium epi : this.epitheliumList) {
			l.add(epi.getName());
		}
		for (int i = 1; true; i++) {
			if (!l.contains(basename + "_Cloned" + i))
				return basename + "_Cloned" + i;
		}
	}

	public void removeEpithelium(Epithelium epi) {
		this.epitheliumList.remove(epi);
		// this.updateModelMap();
	}

	//COMPONENT COLOURS
	public ProjectComponentFeatures getComponentFeatures() {
		return this.componentFeatures;
	}

	public String getFilenamePEPS() {
		return this.filenamePEPS;
	}

	public void setFilenamePEPS(String filename) {
		this.filenamePEPS = filename;
	}

	public Epithelium newEpithelium(int x, int y, String topologyLayout, 
			String userName, String modelName, RollOver rollover) 
					throws InstantiationException,
			IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException,
			SecurityException, ClassNotFoundException {
		Epithelium epi = new Epithelium(x, y, topologyLayout, userName, 
				this.modelFeatures.getModel(modelName), rollover, this.componentFeatures);
		this.epitheliumList.add(epi);
		return epi;
	}

	// TODO: test with same name models in diff directories
	// public String getNextModelName(File file) {
	// String name = file.getName().substring(0, file.getName().indexOf("."));
	// while (this.modelMap.containsKey(name)) {
	// name += "_";
	// }
	// return name;
	// }

	public void addModel(String name, LogicalModel m) {
		this.modelFeatures.addModel(name, m);
		this.componentFeatures.addAllComponentsFromModel(m);
		// TODO: should the model be inserted somewhere else?
		this.isChanged = true;
	}

	public boolean removeModel(String name) {
		if (!this.isUsedModel(name)) {
			this.modelFeatures.removeModel(name);
			this.isChanged = true;
			return true;
		}
		return false;
	}

	public boolean isUsedModel(String name) {
		LogicalModel m = this.modelFeatures.getModel(name);
		for (Epithelium epi : this.epitheliumList) {
			if (epi.hasModel(m))
				return true;
		}
		return false;
	}

	public Set<String> getModelNames() {
		return this.modelFeatures.getNames();
	}

	public LogicalModel getModel(String name) {
		return this.modelFeatures.getModel(name);
	}
	
	public ProjectModelFeatures getModelFeatures() {
		return this.modelFeatures;
	}
}