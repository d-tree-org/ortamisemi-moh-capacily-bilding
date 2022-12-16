package org.smartregister.chw.presenter;

import org.smartregister.chw.core.presenter.FamilyRegisterFragmentPresenter;
import org.smartregister.family.contract.FamilyRegisterFragmentContract;

public class DiabeticRegisterFragmentPresenter extends FamilyRegisterFragmentPresenter {

    public DiabeticRegisterFragmentPresenter(FamilyRegisterFragmentContract.View view, FamilyRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }
}
