package org.smartregister.chw.fragment;

import android.view.View;

import org.smartregister.chw.core.model.FamilyRegisterFramentModel;
import org.smartregister.chw.presenter.DiabeticRegisterFragmentPresenter;
import org.smartregister.chw.provider.PncRegisterProvider;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.family.contract.FamilyRegisterFragmentContract;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.HashMap;
import java.util.Set;

public class DiabeticRegisterFragment extends BaseRegisterFragment
    implements FamilyRegisterFragmentContract.View{


    @Override
    public void countExecute() {
        //todo implement
    }

    @Override
    protected void initializePresenter() {
        presenter = new DiabeticRegisterFragmentPresenter(this, new FamilyRegisterFramentModel(), "");
    }

    @Override
    public void initializeQueryParams(String tableName, String countSelect, String mainSelect) {
        //todo implement
        this.tablename = tableName;
        this.mainCondition = this.getMainCondition();
        this.countSelect = countSelect;
        this.mainSelect = mainSelect;
    }

    @Override
    public void setUniqueID(String qrCode) {
        //set unique ID
    }

    @Override
    public void setAdvancedSearchFormData(HashMap<String, String> advancedSearchFormData) {

    }

    @Override
    protected String getMainCondition() {
        //todo return main condition
        return "";
    }

    @Override
    protected String getDefaultSortQuery() {
        //todo return sorting query condition
        return "";
    }

    @Override
    protected void startRegistration() {
        //todo implement starting registration
    }

    @Override
    protected void onViewClicked(View view) {
        //todo implement
    }

    @Override
    public void setTotalPatients() {
        //todo implement
    }

    @Override
    public void showNotFoundPopup(String opensrpId) {
        //todo implement
    }

    @Override
    public void initializeAdapter(Set<org.smartregister.configurableviews.model.View> set) {
        //todo implement
        PncRegisterProvider provider = new PncRegisterProvider(getActivity(), commonRepository(), set, registerActionHandler, paginationViewHandler);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, provider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }

    @Override
    public FamilyRegisterFragmentContract.Presenter presenter() {
        return (FamilyRegisterFragmentContract.Presenter)presenter;
    }
}
