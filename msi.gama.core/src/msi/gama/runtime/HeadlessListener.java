/*********************************************************************************************
 *
 * 'HeadlessListener.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.runtime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;

import msi.gama.common.interfaces.IConsoleDisplayer;
import msi.gama.common.interfaces.IDisplayCreator;
import msi.gama.common.interfaces.IDisplayCreator.DisplayDescription;
import msi.gama.common.interfaces.IDisplaySurface;
import msi.gama.common.interfaces.IGamaView;
import msi.gama.common.interfaces.IGamlLabelProvider;
import msi.gama.common.interfaces.IGui;
import msi.gama.common.interfaces.IStatusDisplayer;
import msi.gama.kernel.experiment.IExperimentPlan;
import msi.gama.kernel.experiment.ITopLevelAgent;
import msi.gama.kernel.model.IModel;
import msi.gama.kernel.simulation.SimulationAgent;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.outputs.IDisplayOutput;
import msi.gama.outputs.LayeredDisplayOutput;
import msi.gama.outputs.display.NullDisplaySurface;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaColor;
import msi.gama.util.file.IFileMetaDataProvider;
import msi.gama.util.file.IGamaFileMetaData;
import msi.gaml.architecture.user.UserPanelStatement;
import msi.gaml.compilation.ast.ISyntacticElement;
import msi.gaml.operators.Strings;
import msi.gaml.types.IType;

public class HeadlessListener implements IGui {

	static Logger LOGGER = LogManager.getLogManager().getLogger("");
	final ThreadLocal<BufferedWriter> outputWriter = new ThreadLocal<BufferedWriter>();

	static {

		if (GAMA.isInHeadLessMode()) {

			for (final Handler h : LOGGER.getHandlers()) {
				h.setLevel(Level.ALL);
			}
			LOGGER.setLevel(Level.ALL);
		}
		GAMA.setHeadlessGui(new HeadlessListener());
	}

	@Override
	public Map<String, Object> openUserInputDialog(final IScope scope, final String title,
			final Map<String, Object> initialValues, final Map<String, IType<?>> types) {
		return initialValues;
	}

	public void registerJob(final BufferedWriter w) {
		this.outputWriter.set(w);
	}

	public BufferedWriter leaveJob() {
		final BufferedWriter res = this.outputWriter.get();
		this.outputWriter.remove();
		return res;
	}

	@Override
	public void openUserControlPanel(final IScope scope, final UserPanelStatement panel) {}

	@Override
	public void closeDialogs() {}

	@Override
	public IAgent getHighlightedAgent() {
		return null;
	}

	@Override
	public void setHighlightedAgent(final IAgent a) {}

	@Override
	public IGamaView showView(final String viewId, final String name, final int code) {
		return null;
	}

	@Override
	public void tell(final String message) {
		System.out.println("Message: " + message);
	}

	@Override
	public void error(final String error) {
		System.out.println("Error: " + error);
	}

	@Override
	public void showParameterView(final IExperimentPlan exp) {}

	@Override
	public void debug(final String string) {
		System.out.println("Debug: " + string);
	}

	@Override
	public void runtimeError(final GamaRuntimeException g) {
		System.out.println("Runtime error: " + g.getMessage());
	}

	@Override
	public boolean confirmClose(final IExperimentPlan experiment) {
		return true;
	}

	@Override
	public void prepareForExperiment(final IExperimentPlan exp) {}

	@Override
	public boolean openSimulationPerspective(final IModel model, final String id, final boolean immediately) {
		return true;
	}

	@SuppressWarnings ("rawtypes") static Map<String, Class> displayClasses = null;

	@Override
	public IDisplaySurface getDisplaySurfaceFor(final LayeredDisplayOutput output) {

		IDisplaySurface surface = null;
		final IDisplayCreator creator = DISPLAYS.get("image");
		if (creator != null) {
			surface = creator.create(output);
			surface.outputReloaded();
		} else {
			return new NullDisplaySurface();
		}
		return surface;
	}

	@Override
	public void editModel(final Object eObject) {}

	@Override
	public void updateParameterView(final IExperimentPlan exp) {}

	@Override
	public void setSelectedAgent(final IAgent a) {}

	@Override
	public void cleanAfterExperiment() {
		// System.out.println("[Headless] Clean after experiment.");
		try {
			outputWriter.get().flush();
			outputWriter.get().close();			
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}

	@Override
	public void runModel(final Object object, final String exp) {}

	/**
	 * Method updateSpeedDisplay()
	 * 
	 * @see msi.gama.common.interfaces.IGui#updateSpeedDisplay(java.lang.Double)
	 */
	@Override
	public void updateSpeedDisplay(final Double d, final boolean notify) {}

	/**
	 * Method getMetaDataProvider()
	 * 
	 * @see msi.gama.common.interfaces.IGui#getMetaDataProvider()
	 */
	@Override
	public IFileMetaDataProvider getMetaDataProvider() {
		return new IFileMetaDataProvider() {

			@Override
			public void storeMetadata(final IResource file, final IGamaFileMetaData data, final boolean immediately) {}

			@Override
			public void storeMetadata(final File f, final IGamaFileMetaData data) {}

			@Override
			public boolean isGAML(final IFile resource) {
				return false;
			}

			@Override
			public IGamaFileMetaData getMetaData(final Object element, final boolean includeOutdated,
					final boolean immediately) {
				return new IGamaFileMetaData() {

					@Override
					public String toPropertyString() {
						return "";
					}

					@Override
					public void setModificationStamp(final long modificationStamp) {}

					@Override
					public Object getThumbnail() {
						return "";
					}

					@Override
					public String getSuffix() {
						return "";
					}

					@Override
					public long getModificationStamp() {
						return 0;
					}

					@Override
					public String getDocumentation() {
						return "";
					}
				};
			}

			@Override
			public String getDecoratorSuffix(final Object element) {
				return "";
			}
		};
	}

	/**
	 * Method closeSimulationViews()
	 * 
	 * @see msi.gama.common.interfaces.IGui#closeSimulationViews(boolean)
	 */
	@Override
	public void closeSimulationViews(final boolean andOpenModelingPerspective, final boolean immediately) {}

	/**
	 * Method getDisplayDescriptionFor()
	 * 
	 * @see msi.gama.common.interfaces.IGui#getDisplayDescriptionFor(java.lang.String)
	 */
	@Override
	public DisplayDescription getDisplayDescriptionFor(final String name) {
		return new DisplayDescription(null, "display", "msi.gama.core");
	}

	/**
	 * Method getFrontmostSimulationState()
	 * 
	 * @see msi.gama.common.interfaces.IGui#getExperimentState()
	 */
	@Override
	public String getExperimentState() {
		return RUNNING; // ???
	}

	/**
	 * Method updateSimulationState()
	 * 
	 * @see msi.gama.common.interfaces.IGui#updateExperimentState(java.lang.String)
	 */
	@Override
	public void updateExperimentState(final String state) {}

	/**
	 * Method updateSimulationState()
	 * 
	 * @see msi.gama.common.interfaces.IGui#updateExperimentState()
	 */
	@Override
	public void updateExperimentState() {}

	@Override
	public boolean openSimulationPerspective(final boolean immediately) {
		return true;
	}

	@Override
	public void updateViewTitle(final IDisplayOutput output, final SimulationAgent agent) {}

	@Override
	public void openWelcomePage(final boolean b) {}

	@Override
	public void updateDecorator(final String string) {}

	IStatusDisplayer status = new IStatusDisplayer() {

		@Override
		public void resumeStatus() {}

		@Override
		public void waitStatus(final String string) {}

		@Override
		public void informStatus(final String string) {}

		@Override
		public void errorStatus(final String message) {}

		@Override
		public void setSubStatusCompletion(final double status) {}

		@Override
		public void setStatus(final String msg, final GamaColor color) {}

		@Override
		public void informStatus(final String message, final String icon) {}

		@Override
		public void setStatus(final String msg, final String icon) {}

		@Override
		public void beginSubStatus(final String name) {}

		@Override
		public void endSubStatus(final String name) {}

		@Override
		public void neutralStatus(final String string) {}

	};

	IConsoleDisplayer console = new IConsoleDisplayer() {

		@Override
		public void debugConsole(final int cycle, final String s, final ITopLevelAgent root, final GamaColor color) {
			debug(s);
		}

		@Override
		public void debugConsole(final int cycle, final String s, final ITopLevelAgent root) {
			debug(s);
		}

		@Override
		public void informConsole(final String s, final ITopLevelAgent root, final GamaColor color) {
			informConsole(s, root);
		}

		@Override
		public void informConsole(final String s, final ITopLevelAgent root) {
			System.out.println(s);
			if (outputWriter.get() != null) {
				try {
					outputWriter.get().write(s + Strings.LN);
					//outputWriter.get().flush();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void showConsoleView(final ITopLevelAgent agent) {}

		@Override
		public void eraseConsole(final boolean setToNull) {}

	};

	@Override
	public IStatusDisplayer getStatus() {
		return status;
	}

	@Override
	public IConsoleDisplayer getConsole() {
		return console;
	}

	@Override
	public void clearErrors() {}

	@Override
	public void run(final Runnable opener) {
		if (opener != null)
			opener.run();
	}

	@Override
	public void setFocusOn(final IShape o) {}

	@Override
	public void applyLayout(final int layout) {}

	@Override
	public void displayErrors(final List<GamaRuntimeException> list) {}

	@Override
	public ILocation getMouseLocationInModel() {
		return new GamaPoint(0, 0);
	}

	@Override
	public void setMouseLocationInModel(final ILocation modelCoordinates) {}

	@Override
	public IGamlLabelProvider getGamlLabelProvider() {
		return new IGamlLabelProvider() {

			@Override
			public String getText(final ISyntacticElement element) {
				return "";
			}

			@Override
			public Object getImage(final ISyntacticElement element) {
				return null;
			}
		};
	}

	@Override
	public void exit() {
		System.exit(0);
	}

	@Override
	public void openInteractiveConsole() {}

}
