/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.openforis.collect.designer.form.FormObject;
import org.openforis.collect.designer.form.SurveyMainInfoFormObject;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.designer.util.MessageUtil.ConfirmHandler;
import org.openforis.collect.designer.util.Resources;
import org.openforis.collect.io.metadata.collectearth.CollectEarthExternalServices;
import org.openforis.collect.io.metadata.collectearth.CollectEarthPlotLayout;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.SurveyFile.SurveyFileType;
import org.zkoss.bind.Binder;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.select.annotation.WireVariable;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyMainInfoVM extends SurveyObjectBaseVM<CollectSurvey> {
	
	@WireVariable
	private SurveyManager surveyManager;
	
	private boolean editingNewSurveyFile;
	private SurveyFile editedSurveyFile;
	private Set<SurveyFile> selectedSurveyFiles = new HashSet<SurveyFile>();

	private Window surveyFilePopUp;

	private static final String COLLECT_EARTH_REFERENCE_AREA_DISTANCE_FIELD = "collectEarthReferenceAreaDistance";
	private static final String HECTARES_FORMAT = "###.##";

	
	@Init(superclass=false)
	public void init(@ContextParam(ContextType.BINDER) Binder binder) {
		super.init();
		setEditedItem(getSurvey());
		validateForm(binder);
	}
	
	@Override
	protected void performItemSelection(CollectSurvey item) {
		super.performItemSelection(item);
		dispatchValidateAllCommand();
	}
	
	@Override
	protected CollectSurvey createItemInstance() {
		//do nothing, no child instances created
		return null;
	}
	
	@Override
	protected FormObject<CollectSurvey> createFormObject() {
		return new SurveyMainInfoFormObject();
	}

	@Override
	protected List<CollectSurvey> getItemsInternal() {
		return null;
	}
	
	@Override
	protected void addNewItemToSurvey() {}

	@Override
	protected void deleteItemFromSurvey(CollectSurvey item) {}
	
	@Override
	protected void moveSelectedItemInSurvey(int indexTo) {}

	@Override
	public void commitChanges(@ContextParam(ContextType.BINDER) Binder binder) {
		super.commitChanges(binder);
		notifyChange("collectEarthPlotLayout", "collectEarthDistanceBetweenSamplePointsLabel",
				"collectEarthDistanceBetweenSamplePointsTooltip", "collectEarthPlotAreaInfo",
				"collectEarthGeeAppDatesVisible", "collectEarthPlanetOptionsVisible",
				"collectEarthPlanetTfoMosaicsVisible", "collectEarthSecureWatchUrlVisible");
	}

	/**
	 * The rows of the external services that only make sense once the service they belong to is on
	 */
	public boolean isCollectEarthGeeAppDatesVisible() {
		return getCollectEarthExternalServices().isGEEAppEnabled();
	}
	
	public boolean isCollectEarthPlanetOptionsVisible() {
		return getCollectEarthExternalServices().isPlanetMapsEnabled();
	}
	
	public boolean isCollectEarthPlanetTfoMosaicsVisible() {
		CollectEarthExternalServices services = getCollectEarthExternalServices();
		return services.isPlanetMapsEnabled() && services.isPlanetMapsUseTfo();
	}
	
	public boolean isCollectEarthSecureWatchUrlVisible() {
		return getCollectEarthExternalServices().isSecureWatchEnabled();
	}
	
	private CollectEarthExternalServices getCollectEarthExternalServices() {
		return ((SurveyMainInfoFormObject) formObject).toCollectEarthExternalServices();
	}
	
	/**
	 * Shown for the mosaic that Collect Earth picks when none was chosen
	 */
	public String getPlanetTfoDefaultMosaicLabel() {
		return Labels.getLabel("survey.collect_earth.other_settings.planet_tfo_mosaic.default");
	}
	
	/**
	 * Mosaics that the Planet Tropical Forest Observatory publishes, the most recent one first
	 */
	public List<String> getPlanetTfoMosaics() {
		List<String> mosaics = new ArrayList<String>(CollectEarthExternalServices.getPlanetTfoMosaics());
		Collections.reverse(mosaics);
		// the empty one is what Collect Earth stores for "the first (or the latest) mosaic available"
		mosaics.add(0, "");
		return mosaics;
	}
	
	/**
	 * Plot layout being edited: it tells which options the selected plot shape uses
	 */
	public CollectEarthPlotLayout getCollectEarthPlotLayout() {
		return ((SurveyMainInfoFormObject) formObject).toCollectEarthPlotLayout();
	}

	/**
	 * The same field is the distance between the points of a square plot and the radius of round and NFI cluster plots
	 */
	public String getCollectEarthDistanceBetweenSamplePointsLabel() {
		return Labels.getLabel(distanceBetweenSamplePointsLabelKey()) + ":";
	}
	
	public String getCollectEarthDistanceBetweenSamplePointsTooltip() {
		return Labels.getLabel(distanceBetweenSamplePointsLabelKey() + ".tooltip");
	}
	
	private String distanceBetweenSamplePointsLabelKey() {
		CollectEarthPlotLayout layout = getCollectEarthPlotLayout();
		String suffix = layout.isRoundPlot() ? "radius" : layout.isNfiClusterPlot() ? "radius_of_plots" : "distance_between_sample_points";
		return "survey.collect_earth.plot_layout." + suffix;
	}

	public String getCollectEarthPlotAreaInfo() {
		CollectEarthPlotLayout layout = getCollectEarthPlotLayout();
		DecimalFormat format = new DecimalFormat(HECTARES_FORMAT);
		String info = Labels.getLabel("survey.collect_earth.plot_layout.plot_area_info",
				new Object[] {format.format(layout.getPlotAreaHectares())});
		Double referenceAreaArea = layout.getReferenceAreaHectares();
		if (referenceAreaArea != null) {
			info += "      " + Labels.getLabel("survey.collect_earth.plot_layout.reference_area_info",
					new Object[] {format.format(referenceAreaArea)});
		}
		return info;
	}

	/**
	 * Turning the reference area on (or changing its shape) proposes the distance that gives enough context
	 * when none has been set yet or when the current one would not enclose the plot.
	 */
	@Command
	public void collectEarthReferenceAreaShapeChanged(@ContextParam(ContextType.BINDER) Binder binder) {
		CollectEarthPlotLayout layout = SurveyMainInfoFormObject.createCollectEarthPlotLayout(
				this.<String>getFormFieldValue(binder, "collectEarthPlotShape"),
				this.<String>getFormFieldValue(binder, "collectEarthSamplePoints"),
				this.<Integer>getFormFieldValue(binder, "collectEarthDistanceBetweenSamplePoints"),
				this.<Integer>getFormFieldValue(binder, "collectEarthDistanceToPlotBoundaries"),
				this.<Integer>getFormFieldValue(binder, "collectEarthInnerPointSide"),
				this.<Integer>getFormFieldValue(binder, "collectEarthLargeCentralPlotSide"),
				this.<Integer>getFormFieldValue(binder, "collectEarthDistanceBetweenPlots"),
				this.<String>getFormFieldValue(binder, "collectEarthReferenceAreaShape"),
				this.<Integer>getFormFieldValue(binder, COLLECT_EARTH_REFERENCE_AREA_DISTANCE_FIELD));
		if (layout.isReferenceAreaEnabled()) {
			Integer distance = layout.getReferenceAreaDistance();
			if (distance == null || distance < layout.getMinimumReferenceAreaDistance()) {
				setFormFieldValue(binder, COLLECT_EARTH_REFERENCE_AREA_DISTANCE_FIELD, layout.getRecommendedReferenceAreaDistance());
			}
		}
		dispatchApplyChangesCommand(binder);
	}


	
	public SurveyManager getSurveyManager() {
		return surveyManager;
	}
	
	public Integer getEditedSurveyPublishedId() {
		return getSessionStatus().getPublishedSurveyId();
	}
	
	public String getSurveyFileTypeLabel(SurveyFile surveyFile) {
		SurveyFileType type = surveyFile.getType();
		return Labels.getLabel("survey.file.type." + type.name().toLowerCase(Locale.ENGLISH));
	}
	
	public String getSurveyFileName(SurveyFile surveyFile) {
		return surveyFile.getFilename();
	}
	
	public List<SurveyFile> getSurveyFiles() {
		return survey == null ? null : surveyManager.loadSurveyFileSummaries(survey);
	}
	
	@Command
	public void addSurveyFile() {
		editedSurveyFile = new SurveyFile(survey);
		editingNewSurveyFile = true;
		openSurveyFileEditPopUp();
	}
	
	@Command
	public void editSelectedSurveyFile() {
		if (!isSingleSurveyFileSelected()) {
			return;
		}
		editedSurveyFile = selectedSurveyFiles.iterator().next();
		editingNewSurveyFile = false;
		openSurveyFileEditPopUp();
	}
	
	@Command
	public void downloadSelectedSurveyFile() {
		if (!isSingleSurveyFileSelected()) {
			return;
		}
		SurveyFile selectedSurveyFile = selectedSurveyFiles.iterator().next();
		byte[] content = surveyManager.loadSurveyFileContent(selectedSurveyFile);
		String fileName = selectedSurveyFile.getFilename();
		String contentType = URLConnection.guessContentTypeFromName(fileName);
		Filedownload.save(content, contentType, fileName);
	}
	
	@GlobalCommand
	public void applyChangesToEditedSurveyFile(@ContextParam(ContextType.BINDER) Binder binder) {
		closeSurveyFileEditPopUp(binder);
		notifyChange("surveyFiles");
	}
	
	private void openSurveyFileEditPopUp() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("newItem", editingNewSurveyFile);
		args.put("surveyFile", editedSurveyFile);
		surveyFilePopUp = openPopUp(Resources.Component.SURVEY_FILE_POPUP.getLocation(), true, args);
	}
	
	@GlobalCommand
	public void closeSurveyFileEditPopUp(@ContextParam(ContextType.BINDER) Binder binder) {
		closePopUp(surveyFilePopUp);
		surveyFilePopUp = null;
		validateForm(binder);
	}
	
	@Command
	public void deleteSelectedSurveyFiles() {
		if (selectedSurveyFiles.isEmpty()) {
			return;
		}
		String messageKey = isSingleSurveyFileSelected() ? 
				"survey.file.delete.confirm" :
				"survey.file.delete_multiple.confirm";
		String[] messageArgs = isSingleSurveyFileSelected() ?
				new String[]{selectedSurveyFiles.iterator().next().getFilename()}:
				new String[]{Integer.toString(selectedSurveyFiles.size())};
		MessageUtil.showConfirm(new ConfirmHandler() {
			@Override
			public void onOk() {
				surveyManager.deleteSurveyFiles(selectedSurveyFiles);
				setSelectedSurveyFiles(new HashSet<>()); 
				notifyChange("surveyFiles", "selectedSurveyFiles");
			}
		}, messageKey, messageArgs);
	}
	
	@GlobalCommand 
	public void surveyFileRandomGridGenerationComplete(@ContextParam(ContextType.BINDER) Binder binder) {
		closeSurveyFileEditPopUp(binder);
		notifyChange("surveyFiles");
	}

	public boolean isSingleSurveyFileSelected() {
		return selectedSurveyFiles.size() == 1;
	}
	
	public Set<SurveyFile> getSelectedSurveyFiles() {
		return selectedSurveyFiles;
	}
	
	public void setSelectedSurveyFiles(Set<SurveyFile> selectedSurveyFiles) {
		this.selectedSurveyFiles = selectedSurveyFiles;
		notifyChange("singleSurveyFileSelected");
	}
}
