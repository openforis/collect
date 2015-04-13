/**
 * 
 */
package org.openforis.collect.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openforis.collect.Collect;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.metamodel.SurveyTarget;
import org.openforis.collect.metamodel.ui.UIConfiguration;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UIOptionsConstants;
import org.openforis.collect.persistence.jooq.tables.OfcSamplingDesign;
import org.openforis.commons.versioning.Version;
import org.openforis.idm.metamodel.ApplicationOptions;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.SurveyContext;

/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class CollectSurvey extends Survey {

	private static final long serialVersionUID = 1L;
	
	public static final String SAMPLING_DESIGN_CODE_LIST_NAME = "sampling_design";
	
	private boolean work;
	private Version collectVersion;
	private SurveyTarget target;
	
	private CollectAnnotations annotations;
	private UIConfiguration uiConfiguration;
	
	protected CollectSurvey(SurveyContext surveyContext) {
		super(surveyContext);
		this.work = false;
		this.target = SurveyTarget.COLLECT_DESKTOP;
		this.collectVersion = Collect.getVersion();
		this.annotations = new CollectAnnotations(this);
	}

	@Override
	public CollectRecord createRecord(String version) {
		return new CollectRecord(this, version);
	}
	
	@Override
	public void addApplicationOptions(ApplicationOptions options) {
		super.addApplicationOptions(options);
		if ( options instanceof UIOptions ) {
			((UIOptions) options).setSurvey(this);
		}
	}

	@Override
	public List<CodeList> getCodeLists() {
		return getCodeLists(true);
	}
	
	public UIOptions createUIOptions() {
		return new UIOptions(this);
	}
	
	public UIOptions getUIOptions() {
		ApplicationOptions applicationOptions = getApplicationOptions(UIOptionsConstants.UI_TYPE);
		return (UIOptions) applicationOptions;
	}
	
	/**
	 * Returns true if the specified list is not user defined (e.g. sampling data code list)
	 */
	public boolean isPredefinedCodeList(CodeList list) {
		CodeList samplingDesignCodeList = ((CollectSurvey) list.getSurvey()).getSamplingDesignCodeList();
		return samplingDesignCodeList != null && samplingDesignCodeList.getId() == list.getId();
	}
	
	public CodeList getSamplingDesignCodeList() {
		for (CodeList list : getCodeLists()) {
			if ( OfcSamplingDesign.OFC_SAMPLING_DESIGN.getName().equals(list.getLookupTable()) ) {
				return list;
			}
		}
		return null;
	}
	
	public CodeList addSamplingDesignCodeList() {
		CodeList list = createCodeList();
		list.setName(SAMPLING_DESIGN_CODE_LIST_NAME);
		list.setLookupTable(OfcSamplingDesign.OFC_SAMPLING_DESIGN.getName());
		//add hierarchy levels
		String[] levels = new String[] { 
				OfcSamplingDesign.OFC_SAMPLING_DESIGN.LEVEL1.getName(), 
				OfcSamplingDesign.OFC_SAMPLING_DESIGN.LEVEL2.getName(), 
				OfcSamplingDesign.OFC_SAMPLING_DESIGN.LEVEL3.getName() 
		};
		for (String name : levels) {
			CodeListLevel level = new CodeListLevel();
			level.setName(name);
			list.addLevel(level);
		}
		addCodeList(list);
		return list;
	}
	
	public List<CodeList> getCodeLists(boolean includeSamplingDesignList) {
		List<CodeList> codeLists = new ArrayList<CodeList>(super.getCodeLists());
		if ( ! includeSamplingDesignList ) {
			CodeList samplingDesignCodeList = getSamplingDesignCodeList();
			if ( samplingDesignCodeList != null ) { 
				Iterator<CodeList> iterator = codeLists.iterator();
				while (iterator.hasNext()) {
					CodeList list = (CodeList) iterator.next();
					if ( list.getId() == samplingDesignCodeList.getId() ) {
						iterator.remove();
						break;
					}
				}
			}
		}
		return codeLists;
	}
	
	public CollectAnnotations getAnnotations() {
		return annotations;
	}
	
	public boolean isWork() {
		return work;
	}

	public void setWork(boolean work) {
		this.work = work;
	}
	
	public SurveyTarget getTarget() {
		return target;
	}
	
	public void setTarget(SurveyTarget target) {
		this.target = target;
		if (annotations.getSurveyTarget() != target) {
			annotations.setSurveyTarget(target);
		}
	}
	
	public Version getCollectVersion() {
		return collectVersion;
	}
	
	public void setCollectVersion(Version collectVersion) {
		this.collectVersion = collectVersion;
		if (! annotations.getCollectVersion().equals(collectVersion)) {
			annotations.setCollectVersion(collectVersion);
		}
	}
	
	public UIConfiguration getUIConfiguration() {
		return uiConfiguration;
	}
	
	public void setUIConfiguration(UIConfiguration uiConfiguration) {
		this.uiConfiguration = uiConfiguration;
	}
	
}
