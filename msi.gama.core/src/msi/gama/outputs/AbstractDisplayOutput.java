/*********************************************************************************************
 *
 * 'AbstractDisplayOutput.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.outputs;

import msi.gama.common.interfaces.IGamaView;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.descriptions.IDescription;

/**
 * The Class AbstractDisplayOutput.
 *
 * @author drogoul
 */
public abstract class AbstractDisplayOutput extends AbstractOutput implements IDisplayOutput {

	public AbstractDisplayOutput(final IDescription desc) {
		super(desc);
	}

	protected boolean disposed = false;
	protected boolean synchro = false;
	protected boolean inInitPhase = true;
	protected IGamaView view;

	final Runnable opener = () -> {
		view = getScope().getGui().showView(getViewId(), isUnique() ? null : getName(), 1); // IWorkbenchPage.VIEW_ACTIVATE
		if (view == null) { return; }
		view.addOutput(AbstractDisplayOutput.this);
	};

	@Override
	public void open() {
		super.open();
		GAMA.getGui().run(opener);
	}

	@Override
	public boolean init(final IScope scope) throws GamaRuntimeException {
		super.init(scope);
		return true;
	}

	@Override
	public void dispose() {
		if (disposed) { return; }
		disposed = true;
		if (view != null) {
			view.removeOutput(this);
			view = null;
		}
		if (getScope() != null) {
			GAMA.releaseScope(getScope());
		}
	}

	@Override
	public void update() throws GamaRuntimeException {
		if (view != null) {
			view.update(this);
		}
	}

	@Override
	public boolean isUnique() {
		return false;
	}

	@Override
	public boolean isSynchronized() {
		return synchro;
	}

	@Override
	public void setSynchronized(final boolean sync) {
		synchro = sync;
	}

	@Override
	public abstract String getViewId();

	@Override
	public String getId() {
		final String cName = ((AbstractOutput) this).getDescription().getModelDescription().getAlias();
		if (cName != null && !cName.equals("") && !getName().contains("#")) { return isUnique() ? getViewId()
				: getViewId() + getName() + "#" + cName; }
		return isUnique() ? getViewId() : getViewId() + getName();
	}

	@Override
	public boolean isInInitPhase() {
		return inInitPhase;
	}

	@Override
	public void setInInitPhase(final boolean state) {
		inInitPhase = state;
	}
}
