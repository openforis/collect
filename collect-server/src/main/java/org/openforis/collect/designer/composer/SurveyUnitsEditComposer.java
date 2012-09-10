package org.openforis.collect.designer.composer;

import org.openforis.collect.designer.viewmodel.SurveyUnitsEditVM;
import org.openforis.idm.metamodel.Unit;
import org.zkoss.bind.BindComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Textbox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SurveyUnitsEditComposer extends BindComposer<Component> {

	private static final long serialVersionUID = 1L;
	
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	
	@Wire("#labelTextBox")
	Textbox labelTextBox;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
	}
	
	@Listen("onChange=#labelTextBox")
    public void onChangeLabel() {
		SurveyUnitsEditVM viewModel = (SurveyUnitsEditVM) getViewModel();
		String text = labelTextBox.getValue();
		Unit unit = viewModel.getEditedItem();
		unit.setLabel(viewModel.getSelectedLanguageCode(), text);
		//each.setLabel(((SurveyEditVM) getViewModel()).getSelectedLanguageCode(), versionLabelTextBox.getValue());
//        output.setValue(input.getValue());
    }
	
}
