package org.jbpm.console.ng.pr.client.editors.definition.details.basic;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.jbpm.console.ng.ga.model.process.DummyProcessPath;
import org.jbpm.console.ng.pr.client.editors.definition.details.BaseProcessDefDetailsPresenter;
import org.jbpm.console.ng.pr.model.ProcessDefinitionKey;
import org.jbpm.console.ng.pr.model.ProcessSummary;
import org.jbpm.console.ng.pr.service.ProcessDefinitionService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.backend.vfs.VFSService;
import org.uberfire.ext.widgets.common.client.common.popups.errors.ErrorPopup;

import com.google.gwt.user.client.ui.IsWidget;

@Dependent
public class BasicProcessDefDetailsPresenter extends BaseProcessDefDetailsPresenter {

    public interface BasicProcessDefDetailsView extends
            BaseProcessDefDetailsPresenter.BaseProcessDefDetailsView {

    }

    @Inject
    private BasicProcessDefDetailsView view;

    @Inject
    private Caller<ProcessDefinitionService> processDefService;

    @Inject
    private Caller<VFSService> fileServices;

    @Override
    public IsWidget getWidget() {
        return view;
    }

    @Override
    protected void refreshView( String currentProcessDefId, String currentDeploymentId ) {
        view.getProcessIdText().setText( currentProcessDefId );
        view.getDeploymentIdText().setText( currentDeploymentId );
    }

    private void refreshProcessItems( final String deploymentId, final String processId ) {

        processDefService.call( new RemoteCallback<ProcessSummary>() {

            @Override
            public void callback( ProcessSummary process ) {
                if (process != null) {
                    view.setEncodedProcessSource( process.getEncodedProcessSource() );
                    view.getProcessNameText().setText( process.getName() );
                    if (process.getOriginalPath() != null) {

                        fileServices.call( new RemoteCallback<Path>() {

                            @Override
                            public void callback( Path processPath ) {
                                view.setProcessAssetPath( processPath );
                            }
                        } ).get( process.getOriginalPath() );
                    } else {
                        view.setProcessAssetPath( new DummyProcessPath( process
                                .getProcessDefId() ) );
                    }
                    changeStyleRow( process.getName(), process.getVersion() );
                } else {
                    // set to null to ensure it's clear state
                    view.setEncodedProcessSource( null );
                    view.setProcessAssetPath( null );
                }
            }
        }, new ErrorCallback<Message>() {

            @Override
            public boolean error( Message message, Throwable throwable ) {
                ErrorPopup.showMessage( "Unexpected error encountered : "
                        + throwable.getMessage() );
                return true;
            }
        } ).getItem( new ProcessDefinitionKey( deploymentId, processId ) );
    }

    @Override
    protected void refreshProcessDef( String deploymentId, String processId ) {
        refreshProcessItems( deploymentId, processId );
    }

}
