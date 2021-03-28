package org.epilogtool.io;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.colomoto.biolqm.LogicalModel;
import org.colomoto.biolqm.NodeInfo;
import org.colomoto.biolqm.modifier.perturbation.LogicalModelPerturbation;
import org.colomoto.biolqm.modifier.perturbation.FixedValuePerturbation;
import org.colomoto.biolqm.modifier.perturbation.MultiplePerturbation;
import org.colomoto.biolqm.modifier.perturbation.RangePerturbation;
import org.colomoto.biolqm.tool.simulation.grouping.ModelGrouping;
import org.colomoto.biolqm.tool.simulation.grouping.SplittingType;
import org.colomoto.biolqm.tool.simulation.grouping.ModelGrouping.VarInfo;
import org.colomoto.biolqm.widgets.UpdaterFactoryModelGrouping;
import org.colomoto.biolqm.tool.simulation.LogicalModelUpdater;
import org.epilogtool.OptionStore;
import org.epilogtool.common.EnumRandomSeed;
import org.epilogtool.common.RandCentral;
import org.epilogtool.common.Tuple2D;
import org.epilogtool.common.Txt;
import org.epilogtool.core.ComponentIntegrationFunctions;
import org.epilogtool.core.EmptyModel;
import org.epilogtool.core.Epithelium;
import org.epilogtool.core.EpitheliumGrid;
import org.epilogtool.core.UpdateCells;
import org.epilogtool.core.topology.RollOver;
import org.epilogtool.gui.color.ColorUtils;
import org.epilogtool.notification.NotificationManager;
import org.epilogtool.project.Project;
import org.epilogtool.project.ProjectFeatures;
import org.epilogtool.services.TopologyService;

public class Parser { 
	
	public static final String SEPVAR = ",";
	public static final String SEPGROUP = "/";
	public static final String SEPCLASS = ":";
	public static final String SEPUPDATER = "\\$";
	public static Map<String, String> updatersEpiBioLQM;
	static {
		updatersEpiBioLQM = new HashMap<>();
		updatersEpiBioLQM.put("RN", "Random non uniform");
		updatersEpiBioLQM.put("RU", "Random uniform");
		updatersEpiBioLQM.put("S", "Synchronous");
	}

	public static void loadConfigurations(File fConfig) throws IOException, InstantiationException,
			IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException, ClassNotFoundException {
		FileInputStream fstream = new FileInputStream(fConfig);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		Map<String, String> modelKey2Name = new HashMap<String, String>();
		Epithelium currEpi = null;
		RollOver rollover = null;
		EnumRandomSeed randomSeedType = null;
		int randomSeed = 0;

		String x = null;
		String y = null;
		String topologyLayout = null;

		String line, epiName = null;
		String[] saTmp;

		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("#"))
				continue;

			// Load SBML model numerical identifiers and create new project
			if (line.startsWith("SB")) {
				saTmp = line.split("\\s+");

				File fSBML = new File(fConfig.getParent() + File.separator + saTmp[2]);
				try {
					LogicalModel m = FileIO.loadSBMLModel(fSBML);
					Project.getInstance().loadModel(fSBML.getName(), m);
				} catch (Exception e) {
					throw new IOException(Txt.get("s_SBML_failed_load"));
				}
				
				modelKey2Name.put(saTmp[1], saTmp[2]);
				Color modelColor = ColorUtils.getColor(saTmp[3], saTmp[4], saTmp[5]);
				Project.getInstance().getProjectFeatures().setModelColor(saTmp[2], modelColor);
			}

			if (line.startsWith("CC")) {
				saTmp = line.split("\\s+");
				Color componentColor = ColorUtils.getColor(saTmp[2], saTmp[3], saTmp[4]);
				Project.getInstance().getProjectFeatures().setNodeColor(saTmp[1], componentColor);
			}

			// Epithelium name
			if (line.startsWith("SN")) {
				epiName = line.split("\\s+")[1];
				currEpi = null;
				rollover = RollOver.NONE;
				randomSeed = RandCentral.getInstance().nextInt();
				randomSeedType = EnumRandomSeed.RANDOM;
			}

			if (line.startsWith("GD")) {
				saTmp = line.split("\\s+");
				x = saTmp[1];
				y = saTmp[2];
				topologyLayout = saTmp[3];

				if (topologyLayout == "Hexagon-Even-PointyTopped") {
					topologyLayout = "Pointy-Even";
				} else if (topologyLayout == "Hexagon-Odd-PointyTopped") {
					topologyLayout = "Pointy-Odd";
				} else if (topologyLayout == "Hexagon-Even-FlatTopped") {
					topologyLayout = "Flat-Even";
				} else if (topologyLayout == "Hexagon-Odd-FlatTopped") {
					topologyLayout = "Flat-Odd";
				}
			}

			// RollOver
			if (line.startsWith("RL")) {
				rollover = RollOver.string2RollOver(epiName, line.split("\\s+")[1]);
				if (rollover == null) {
					NotificationManager.warning("Parser",
							epiName + ": Loaded border option incorrect. Border set to rectangular.");
					rollover = RollOver.NONE;
				}
				if (currEpi != null) {
					currEpi.getEpitheliumGrid().setRollOver(rollover);
				}
			}

			// Random Seed
			if (line.startsWith("SD")) {
				saTmp = line.split("\\s+");
				EnumRandomSeed rsType = EnumRandomSeed.string2RandomSeed(saTmp[1]);
				if (rsType != null && rsType.equals(EnumRandomSeed.FIXED)) {
					if (saTmp.length == 3) {
						randomSeedType = rsType;
						randomSeed = Integer.parseInt(saTmp[2]);
					} else {
						NotificationManager.warning("Parser", "File with an undefined Fixed Random Seed");
					}
				}
			}

			// Model grid
			if (line.startsWith("GM")) {
				saTmp = line.split("\\s+");
				LogicalModel m = Project.getInstance().getModel(modelKey2Name.get(saTmp[1]));
				
				// está a entrar aqui....
				if (currEpi == null) {
					currEpi = Project.getInstance().newEpithelium(Integer.parseInt(x), Integer.parseInt(y),
							topologyLayout, epiName, EmptyModel.getInstance().getName(), rollover, randomSeedType,
							randomSeed);
				}
				if (saTmp.length > 2) {
					currEpi.setGridWithModel(m,
							currEpi.getEpitheliumGrid().getTopology().instances2Tuples2D(saTmp[2].split(",")));
					currEpi.initPriorityClasses(m);
				}
			}
			// alpha-asynchronous value
			if (line.startsWith("AS")) {
				saTmp = line.split("\\s+");
				currEpi.getUpdateSchemeInter().setAlpha(Float.parseFloat(saTmp[1]));
			}

			// Cell Update
			if (line.startsWith("CU")) {
				String updateCells = line.substring(line.indexOf(" ") + 1);
				currEpi.getUpdateSchemeInter().setUpdateCells(UpdateCells.fromString(updateCells));
			}

			// Initial Conditions grid
			if (line.startsWith("IC")) {
				saTmp = line.split("\\s+");
				currEpi.setGridWithComponentValue(saTmp[1], Byte.parseByte(saTmp[2]),
						currEpi.getEpitheliumGrid().getTopology().instances2Tuples2D(saTmp[3].split(",")));
			}

			// Component Integration Functions
			// IT #model Node Level {Function}
			// Old Integration function identifier, where an integration function was
			// associated with a model and a component.
			if (line.startsWith("IT")) {
				saTmp = line.split("\\s+");
				byte value = Byte.parseByte(saTmp[3]);
				String nodeID = saTmp[2];
				String function = "";
				if (saTmp.length > 4) {
					int pos = line.indexOf(" ");
					int n = 4;
					while (--n > 0) {
						pos = line.indexOf(" ", pos + 1);
					}
					function = line.substring(pos).trim();
				}
				try {
					currEpi.setIntegrationFunction(nodeID, value, function);
				} catch (RuntimeException re) {
					NotificationManager.warning("Parser",
							"Integration function: " + saTmp[2] + ":" + value + " has invalid expression: " + function);
				}
			}

			// IF #model Node Level {Function}
			if (line.startsWith("IF")) {
				saTmp = line.split("\\s+");
				byte value = Byte.parseByte(saTmp[2]);
				String nodeID = saTmp[1];
				String function = "";
				if (saTmp.length > 3) {
					int pos = line.indexOf(" ");
					int n = 4;
					while (--n > 0) {
						pos = line.indexOf(" ", pos + 1);
					}
					function = line.substring(pos).trim();
				}
				try {
					currEpi.setIntegrationFunction(nodeID, value, function);
				} catch (RuntimeException re) {
					NotificationManager.warning("Parser",
							"Integration function: " + nodeID + ":" + value + " has invalid expression: " + function);
				}
			}
			
			
			// Model Priority classes
			// PR #model node1,node2:...:nodei
			if (line.startsWith("PR")) {
				saTmp = line.split("\\s+");
				LogicalModel m = Project.getInstance().getModel(modelKey2Name.get(saTmp[1]));
				
				Map<Integer, Map<List<VarInfo>, LogicalModelUpdater>> pcList = 
						new HashMap<Integer, Map<List<VarInfo>, LogicalModelUpdater>>();

//				Map<Integer, Map<List<VarInfo>, LogicalModelUpdater>> ranks

				
				String[] ranks = saTmp[2].split(SEPCLASS);
				int rankCount = 0;
				for (String rank : ranks) {
					
					Map<List<VarInfo>, LogicalModelUpdater> newGroups 
					= new HashMap<List<VarInfo>, LogicalModelUpdater>();
					
					for(String group : rank.split(SEPGROUP)) {
 						String[] groupTemp = group.split(SEPUPDATER);
						
						LogicalModelUpdater up = null;
						
						String[] vars =  null;
						if (groupTemp.length == 1) {
							up = UpdaterFactoryModelGrouping.getUpdater(m,"Synchronous");
							vars =  groupTemp[0].split(SEPVAR);

						} else if (groupTemp.length == 2) {
						
							String updater = groupTemp[1];
							if (groupTemp[1].length() > 2) {
								String[] rates = updater.substring(3, updater.length() - 1).split(",");
								updater = updater.substring(0,2);
								Double[] doubleRates = new Double[rates.length];
								for (int e = 0; e < doubleRates.length; e++) {
									Double rate = (rates[e].equals("null")) ? null : Double.parseDouble(rates[e]);
									doubleRates[e] = rate;
								}
							
								up = UpdaterFactoryModelGrouping.getUpdater(m, updatersEpiBioLQM.get(updater),
										doubleRates);
								vars =  groupTemp[0].split(SEPVAR);

							} else {
								up = UpdaterFactoryModelGrouping.getUpdater(m, updatersEpiBioLQM.get(updater));
								vars =  groupTemp[0].split(SEPVAR);
							}
						}
				
						List<VarInfo> newVars = new ArrayList<VarInfo>();
						for (String var : vars) {
							int split = 0;
							if (var.endsWith(SplittingType.NEGATIVE.toString())) {
								split = -1;
								var = var.substring(0, var.length() - SplittingType.NEGATIVE.toString().length());
							} else if (var.endsWith(SplittingType.POSITIVE.toString())) {
								split = 1;
								var = var.substring(0, var.length() - SplittingType.POSITIVE.toString().length());
							}
							for (int idx = 0; idx < m.getComponents().size(); idx++) {
								NodeInfo node = m.getComponents().get(idx);
								// find Node with var nodeID
								if (node.getNodeID().equals(var)) {
									VarInfo newVar = new VarInfo (idx, split, m);
									newVars.add(newVar);
								}
							}
						}
						newGroups.put(newVars, up);
					}
					pcList.put(rankCount, newGroups);
					rankCount ++;
				}

				ModelGrouping mpc = null;
				try {
					mpc = new ModelGrouping(m, pcList);
				} catch (Exception e) {
					e.printStackTrace();
				}
				currEpi.setPriorityClasses(mpc);
			}

			// Model All Perturbations
			// Old version -> PT #model (Perturbation) R G B cell1-celli,celln,...
			// Old NewVersion -> PT (Perturbation) R G B cell1-celli,celln,...

			if (line.startsWith("PT")) {
				saTmp = line.split("\\s+");
				String sPerturb = line.substring(line.indexOf("(") + 1, line.indexOf(")"));
				LogicalModelPerturbation ap = string2LogicalModelPerturbation(Project.getInstance().getProjectFeatures(),
						sPerturb);
				currEpi.addPerturbation(ap);

				String rest = line.substring(line.indexOf(")") + 1).trim();
				if (!rest.isEmpty()) {
					saTmp = rest.split("\\s+");
					Color c = ColorUtils.getColor(saTmp[0], saTmp[1], saTmp[2]);
					List<Tuple2D<Integer>> lTuple = null;
					if (saTmp.length > 3) {
						lTuple = currEpi.getEpitheliumGrid().getTopology().instances2Tuples2D(saTmp[3].split(","));
					}
					currEpi.applyPerturbation(ap, c, lTuple);
				}
			}
		}
		br.close();
		in.close();
		fstream.close();
		
		// // Ensure coherence of all epithelia
		for (Epithelium epi : Project.getInstance().getEpitheliumList()) {
			epi.getEpitheliumGrid().updateGrid();
		}
		Project.getInstance().setChanged(false);
		NotificationManager.dispatchDialogWarning(true, false);
		
	}

	private static LogicalModelPerturbation string2LogicalModelPerturbation(ProjectFeatures features,
			String sExpr) {
		String[] saExpr = sExpr.split(", ");
		List<LogicalModelPerturbation> lPerturb = new ArrayList<LogicalModelPerturbation>();

		for (String sTmp : saExpr) {
			LogicalModelPerturbation ap;
			String name = sTmp.split(" ")[0];
			NodeInfo node = features.getNodeInfo(name);
			String perturb = sTmp.split(" ")[1];

			if (perturb.equals("KO")) {
				ap = new FixedValuePerturbation(node, 0);
			} else if (perturb.startsWith("E")) {
				ap = new FixedValuePerturbation(node, Integer.parseInt(perturb.substring(1)));
			} else {
				String[] saTmp = perturb.split(",");
				ap = new RangePerturbation(node, Integer.parseInt(saTmp[0].replace("[", "")),
						Integer.parseInt(saTmp[1].replace("]", "")));
			}
			lPerturb.add(ap);
		}
		if (lPerturb.size() == 1) {
			return lPerturb.get(0);
		} else {
			return new MultiplePerturbation<LogicalModelPerturbation>(lPerturb);
		}
	}

	public static void saveConfigurations(PrintWriter w) throws IOException {
		// SBML numerical identifiers
		OptionStore.setOption("EM", ColorUtils.getColorCode(EmptyModel.getInstance().getColor()));

		int i = 0;
		Map<LogicalModel, Integer> model2Key = new HashMap<LogicalModel, Integer>();
		for (String sbml : Project.getInstance().getModelNames()) {
			LogicalModel m = Project.getInstance().getModel(sbml);
			model2Key.put(m, i);
			Color c = Project.getInstance().getProjectFeatures().getModelColor(m);
			w.println("SB " + i + " " + sbml + " " + c.getRed() + " " + c.getGreen() + " " + c.getBlue());
			i++;
		}
		w.println();

		// Component colors
		for (String nodeID : Project.getInstance().getProjectFeatures().getNodeIDs()) {
			Color c = Project.getInstance().getProjectFeatures().getNodeColor(nodeID);
			w.println("CC " + nodeID + " " + c.getRed() + " " + c.getGreen() + " " + c.getBlue());
			OptionStore.setOption("CC " + nodeID, ColorUtils.getColorCode(c));
		}

		for (Epithelium epi : Project.getInstance().getEpitheliumList()) {
			writeEpithelium(epi, model2Key, w);
		}
	}

	private static void writeEpithelium(Epithelium epi, Map<LogicalModel, Integer> model2Key, PrintWriter w)
			throws IOException {
		w.println();

		// Epithelium name
		w.println("SN " + epi.getName());
		w.println("GD " + epi.getX() + " " + epi.getY() + " "
				+ TopologyService.getManager().getTopologyID(epi.getEpitheliumGrid().getTopology().getDescription()));

		// Rollover
		w.println("RL " + epi.getEpitheliumGrid().getTopology().getRollOver());

		// Random Seed
		EnumRandomSeed rsType = epi.getUpdateSchemeInter().getRandomSeedType();
		w.print("SD " + rsType.toString());
		if (rsType.equals(EnumRandomSeed.FIXED)) {
			w.print(" " + epi.getUpdateSchemeInter().getRandomSeed());
		}
		w.println();

		// Models in the grid
		EpitheliumGrid grid = epi.getEpitheliumGrid();
		Map<LogicalModel, List<String>> modelInst = new HashMap<LogicalModel, List<String>>();
		LogicalModel lastM = grid.getModel(0, 0);
		for (int y = 0, currI = 0, lastI = 0; y < grid.getY(); y++) {
			for (int x = 0; x < grid.getX(); x++, currI++) {
				LogicalModel currM = grid.getModel(x, y);
				if (!currM.equals(lastM)) {
					if (!modelInst.containsKey(lastM))
						modelInst.put(lastM, new ArrayList<String>());
					List<String> lTmp = modelInst.get(lastM);
					if ((currI - 1) == lastI) {
						lTmp.add("" + lastI);
					} else {
						lTmp.add(lastI + "-" + (currI - 1));
					}
					lastI = currI;
				}
				lastM = currM;
				if (x == (grid.getX() - 1) && y == (grid.getY() - 1)) {
					if (!modelInst.containsKey(lastM))
						modelInst.put(lastM, new ArrayList<String>());
					List<String> lTmp = modelInst.get(lastM);
					if (currI == lastI) {
						lTmp.add("" + lastI);
					} else {
						lTmp.add(lastI + "-" + currI);
					}
				}
			}
		}
		for (LogicalModel m : modelInst.keySet()) {
			if (model2Key.containsKey(m)) {
				w.println("GM " + model2Key.get(m) + " " + join(modelInst.get(m), ","));
			}
		}

		// Alpha asynchronism
		w.println("AS " + epi.getUpdateSchemeInter().getAlpha());
		w.println();

		// Cell Update
		w.println("CU " + epi.getUpdateSchemeInter().getUpdateCells());
		w.println();

		// Initial Conditions
		// varA value 1-2,3,4-6
		Map<LogicalModel, Map<String, Map<Byte, List<Integer>>>> valueInst = new HashMap<LogicalModel, Map<String, Map<Byte, List<Integer>>>>();
		for (int y = 0, inst = 0; y < grid.getY(); y++) {
			for (int x = 0; x < grid.getX(); x++, inst++) {
				LogicalModel currM = grid.getModel(x, y);
				if (!valueInst.containsKey(currM))
					valueInst.put(currM, new HashMap<String, Map<Byte, List<Integer>>>());

				List<NodeInfo> nodeOrder = currM.getComponents();
				byte[] currState = grid.getCellState(x, y);
				for (int n = 0; n < nodeOrder.size(); n++) {
					String nodeID = nodeOrder.get(n).getNodeID();
					if (!valueInst.get(currM).containsKey(nodeID))
						valueInst.get(currM).put(nodeID, new HashMap<Byte, List<Integer>>());
					byte value = currState[n];
					if (!valueInst.get(currM).get(nodeID).containsKey(value))
						valueInst.get(currM).get(nodeID).put(value, new ArrayList<Integer>());

					List<Integer> iTmp = valueInst.get(currM).get(nodeID).get(value);
					iTmp.add(inst);
					valueInst.get(currM).get(nodeID).put(value, iTmp);
				}
			}
		}
		for (LogicalModel m : valueInst.keySet()) {
			for (String nodeID : valueInst.get(m).keySet()) {
				for (byte value : valueInst.get(m).get(nodeID).keySet()) {
					List<String> sInsts = compactIntegerSequences(valueInst.get(m).get(nodeID).get(value));
					if (!sInsts.isEmpty()) {
						w.println("IC " + nodeID + " " + value + " " + join(sInsts, ","));
					}
				}
			}
		}
		w.println();

		// Component Integration Functions
		// IT #model Node Level {Function}
		// for (NodeInfo node : epi.getIntegrationNodes()) {
		// ComponentIntegrationFunctions cif =
		// epi.getIntegrationFunctionsForComponent(node);
		// List<String> lFunctions = cif.getFunctions();
		// for (int i = 0; i < lFunctions.size(); i++) {
		// int modelIndex = model2Key.get(node.getModel());
		// w.println("IT " + modelIndex + " " + cp.getNodeInfo().getNodeID() + " " + (i
		// + 1) + " "
		// + lFunctions.get(i));
		// }
		// }
		// w.println();

		// IF Node Level {Function}
		for (NodeInfo node : epi.getIntegrationNodes()) {
			ComponentIntegrationFunctions cif = epi.getIntegrationFunctionsForComponent(node);
			List<String> lFunctions = cif.getFunctions();
			for (int i = 0; i < lFunctions.size(); i++) {
				w.println("IF " + " " + node.getNodeID() + " " + (i + 1) + " " + lFunctions.get(i));
			}
		}
		w.println();
		
		// Model Priority classes
		// PR #model node1,node2:...:nodei
		for (LogicalModel m : model2Key.keySet()) {
			if (epi.hasModel(m)) {
				ModelGrouping mpc = epi.getPriorityClasses(m);
				w.println("PR " + model2Key.get(m) + " " + mpc.toString());
			}
			w.println();
		}
		

		/*
		 * // Model Priority classes // PR #model node1,node2:...:nodei for
		 * (LogicalModel m : model2Key.keySet()) { if (epi.hasModel(m)) {
		 * 
		 * ModelGrouping mpc = null; Rates rates = epi.getRates(m); Boolean active =
		 * epi.getActive(m);
		 * 
		 * String sPCs = "";
		 * 
		 * // if PC are selected if (active) { mpc = epi.getPriorityClasses(m); for (int
		 * idxPC = 0; idxPC < mpc.size(); idxPC++) { if (!sPCs.isEmpty()) sPCs += ":";
		 * List<String> pcVars = mpc.getClassVars(idxPC).get(0); sPCs += join(pcVars,
		 * ","); }
		 * 
		 * System.out.println(mpc.toString()); System.out.println(sPCs);
		 * 
		 * // if Rates ise selected } else { // Init PC, so there is only one class.
		 * epi.initPriorityClasses(m); mpc = epi.getPriorityClasses(m);
		 * 
		 * Boolean uni = rates.isUniform(); if (uni) { sPCs += "$RU"; } else { sPCs +=
		 * "$RN"; sPCs += Arrays.toString(rates.getAllRates()).replaceAll("\\s+",""); }
		 * }
		 * 
		 * w.println("PR " + model2Key.get(m) + " " + sPCs);
		 * 
		 * 
		 * 
		 * }
		 * 
		 * w.println(); }
		 */ 

		// Model All Perturbations
		// old -> PT #model (Perturbation) R G B cell1-celli,celln,...
		// new -> PT (Perturbation) R G B cell1-celli,celln,...
		Map<LogicalModelPerturbation, List<Integer>> apInst = new HashMap<LogicalModelPerturbation, List<Integer>>();
		for (int y = 0, currI = 0; y < grid.getY(); y++) {
			for (int x = 0; x < grid.getX(); x++, currI++) {
				LogicalModelPerturbation currAP = grid.getPerturbation(x, y);
				if (currAP == null) {
					continue;
				} else {
					if (!apInst.containsKey(currAP)) {
						apInst.put(currAP, new ArrayList<Integer>());
					}
					apInst.get(currAP).add(currI);
				}
			}
		}
		w.println();
		for (LogicalModelPerturbation ap : epi.getEpitheliumPerturbations().getAllCreatedPerturbations()) {
			w.print("PT " + "(" + ap + ")");
			Color c = epi.getEpitheliumPerturbations().getPerturbationColor(ap);
			if (c != null) {
				w.print(" " + c.getRed() + " " + c.getGreen() + " " + c.getBlue());
				if (apInst.containsKey(ap)) {
					w.print(" " + join(compactIntegerSequences(apInst.get(ap)), ","));
				}
			}
			w.println();

		}

		w.println("\n\n");
		// EpitheliumCell Connections
	}
	
	public static String getTextFormatCellularUpdateMode(Epithelium epi) {
		String text = "";
		
		Set<String> models = Project.getInstance().getModelNames();

		for (String model : models) {
			LogicalModel m = Project.getInstance().getModel(model);
			if (epi.hasModel(m)) {
				ModelGrouping mpc = epi.getPriorityClasses(m);
				// Uses model name and not integer key, since the key is random for each parsing.
				 text += "PR " + model + " " + mpc.toString();
			}
			text += "\n";
		}
		return text;
	}
	public static String getTextFormatEpitheliumUpdateMode(Epithelium epi) {
		String text = "AS " + epi.getUpdateSchemeInter().getAlpha();
		return text;
	}
	public static String getTextFormatInputDef(Epithelium epi) {
		String text = "";
		// IF Node Level {Function}
		for (NodeInfo node : epi.getIntegrationNodes()) {
			ComponentIntegrationFunctions cif = epi.getIntegrationFunctionsForComponent(node);
			List<String> lFunctions = cif.getFunctions();
			for (int i = 0; i < lFunctions.size(); i++) {
				text += "IF " + " " + node.getNodeID() + " " + (i + 1) + " " + lFunctions.get(i);
			}
		}
		return text;
	}
	public static boolean parseCelullarUpdateMode(Epithelium epi, String[] definitions, boolean valid) throws NumberFormatException, IOException {
		
		String[] saTmp;
		try {

		for (String line : definitions) {
			line = line.trim();
			
		if (line.startsWith("PR")) {
			saTmp = line.split("\\s+");
			LogicalModel m = Project.getInstance().getModel(saTmp[1]);
			
			Map<Integer, Map<List<VarInfo>, LogicalModelUpdater>> pcList = 
					new HashMap<Integer, Map<List<VarInfo>, LogicalModelUpdater>>();

			String[] ranks = saTmp[2].split(SEPCLASS);
			int rankCount = 0;
			for (String rank : ranks) {
				
				Map<List<VarInfo>, LogicalModelUpdater> newGroups 
				= new HashMap<List<VarInfo>, LogicalModelUpdater>();
				
				for(String group : rank.split(SEPGROUP)) {
						String[] groupTemp = group.split(SEPUPDATER);
					
					LogicalModelUpdater up = null;
					
					String[] vars =  null;
					if (groupTemp.length == 1) {
						try {
							up = UpdaterFactoryModelGrouping.getUpdater(m,"Synchronous");
						} catch (Exception ex) {
							return false;
						}
						vars =  groupTemp[0].split(SEPVAR);

					} else if (groupTemp.length == 2) {
					
						String updater = groupTemp[1];
						if (groupTemp[1].length() > 2) {
							String[] rates = updater.substring(3, updater.length() - 1).split(",");
							if (rates.length != m.getComponents().size()*2) 
								return false;
							updater = updater.substring(0,2);
							Double[] doubleRates = new Double[rates.length];
							for (int e = 0; e < doubleRates.length; e++) {
								Double rate = (rates[e].equals("null")) ? null : Double.parseDouble(rates[e]);
								doubleRates[e] = rate;
							}
						
							up = UpdaterFactoryModelGrouping.getUpdater(m, updatersEpiBioLQM.get(updater),
									doubleRates);
							vars =  groupTemp[0].split(SEPVAR);

						} else {
							up = UpdaterFactoryModelGrouping.getUpdater(m, updatersEpiBioLQM.get(updater));
							vars =  groupTemp[0].split(SEPVAR);
						}
					}
			
					List<VarInfo> newVars = new ArrayList<VarInfo>();
					boolean varFound = false;
					for (String var : vars) {
						varFound = false;
						int split = 0;
						if (var.endsWith(SplittingType.NEGATIVE.toString())) {
							split = -1;
							var = var.substring(0, var.length() - SplittingType.NEGATIVE.toString().length());
						} else if (var.endsWith(SplittingType.POSITIVE.toString())) {
							split = 1;
							var = var.substring(0, var.length() - SplittingType.POSITIVE.toString().length());
						}
						for (int idx = 0; idx < m.getComponents().size(); idx++) {
							NodeInfo node = m.getComponents().get(idx);
							// find Node with var nodeID
							if (node.getNodeID().equals(var)) {
								VarInfo newVar = new VarInfo (idx, split, m);
								newVars.add(newVar);
								varFound = true;
							}
						} if (varFound == false) 
							return false;
					}
					newGroups.put(newVars, up);
				}
				pcList.put(rankCount, newGroups);
				rankCount ++;
			}

			ModelGrouping mpc = null;
			try {
				mpc = new ModelGrouping(m, pcList);
				if (mpc.getClass(0).isEmpty()) 
					return false;
				if (valid) 
					epi.setPriorityClasses(mpc);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
	}
		return false;
		} catch (Exception ae) {
			return false;
		}
	}
	
	public static boolean parseEpitheliumUpdateMode(Epithelium epi, String[] definitions,
			boolean valid) throws IOException {		
		
		String[] saTmp;

		for (String line : definitions) {
			line = line.trim();
			
			if (line.startsWith("AS")) {
				saTmp = line.split("\\s+");
				try {
					Float alfa = Float.parseFloat(saTmp[1]);
					if (valid) 
						epi.getUpdateSchemeInter().setAlpha(alfa);
					return true;
				} catch (Exception e) {				
					return false;
				}
			}
		}
		return false;
	}
	public static boolean parseInputDef(Epithelium epi, String[] definitions, boolean valid) throws NumberFormatException, IOException {

		String[] saTmp;

		for (String line : definitions) {
			line = line.trim();
			
			// Component Integration Functions
			// IT #model Node Level {Function}
			// Old Integration function identifier, where an integration function was
			// associated with a model and a component.
			if (line.startsWith("IT")) {
				saTmp = line.split("\\s+");
				byte value = Byte.parseByte(saTmp[3]);
				String nodeID = saTmp[2];
				String function = "";
				if (saTmp.length > 4) {
					int pos = line.indexOf(" ");
					int n = 4;
					while (--n > 0) {
						pos = line.indexOf(" ", pos + 1);
					
					}
					function = line.substring(pos).trim();
				}
				try {
					epi.setIntegrationFunction(nodeID, value, function);
					return true;
				} catch (RuntimeException re) {
					NotificationManager.warning("Parser",
							"Integration function: " + saTmp[2] + ":" + value
							+ " has invalid expression: " + function);
				}
			}
		// IF #model Node Level {Function}
			if (line.startsWith("IF")) {
				saTmp = line.split("\\s+");
				byte value = Byte.parseByte(saTmp[2]);
				String nodeID = saTmp[1];
				String function = "";
				if (saTmp.length > 3) {
					int pos = line.indexOf(" ");
					int n = 4;
					while (--n > 0) {
						pos = line.indexOf(" ", pos + 1);
					}
					function = line.substring(pos).trim();
				}
				try {
					epi.setIntegrationFunction(nodeID, value, function);
					return true;
				} catch (RuntimeException re) {
					NotificationManager.warning("Parser",
							"Integration function: " + nodeID + ":" + value + 
								" has invalid expression: " + function);
					return false;
				}
			}
		}
		return false;
	}
	
	private static List<String> compactIntegerSequences(List<Integer> iInsts) {
		List<String> sInsts = new ArrayList<String>();
		if (iInsts.size() == 1) {
			sInsts.add(iInsts.get(0).toString());
			return sInsts;
		}
		for (int currI = 1, lastI = 0; currI < iInsts.size(); currI++) {
			if ((iInsts.get(currI - 1) + 1) == iInsts.get(currI)) {
				if ((currI + 1) == iInsts.size()) { // It's at the last position
					if ((iInsts.get(currI - 1) + 1) == iInsts.get(currI))
						sInsts.add(iInsts.get(lastI) + "-" + iInsts.get(currI));
					else {
						sInsts.add("" + iInsts.get(currI));
					}
				}
				continue;
			}
			if ((currI - 1) == lastI)
				sInsts.add("" + iInsts.get(lastI));
			else
				sInsts.add(iInsts.get(lastI) + "-" + iInsts.get(currI - 1));
			if ((currI + 1) == iInsts.size()) { // It's at the last position
				if ((iInsts.get(currI - 1) + 1) == iInsts.get(currI))
					sInsts.add(iInsts.get(lastI) + "-" + iInsts.get(currI));
				else {
					sInsts.add("" + iInsts.get(currI));
				}
			}
			lastI = currI;
		}
		return sInsts;
	}

	private static String join(List<String> list, String sep) {
		String s = "";
		for (int i = 0; i < list.size(); i++) {
			if (i > 0)
				s += sep;
			s += list.get(i);
		}
		return s;
	}
}
