/*********************************************************************************************
 *
 * 'PlatformAgent.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.kernel.root;

import java.net.URL;

import org.eclipse.core.runtime.Platform;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.util.RandomUtils;
import msi.gama.kernel.experiment.IExperimentAgent;
import msi.gama.kernel.experiment.ITopLevelAgent;
import msi.gama.kernel.simulation.SimulationAgent;
import msi.gama.kernel.simulation.SimulationClock;
import msi.gama.metamodel.agent.GamlAgent;
import msi.gama.metamodel.population.GamaPopulation;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.metamodel.topology.ITopology;
import msi.gama.metamodel.topology.continuous.AmorphousTopology;
import msi.gama.outputs.IOutputManager;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.species;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.precompiler.GamlProperties;
import msi.gama.runtime.ExecutionScope;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaColor;
import msi.gama.util.ICollector;
import msi.gaml.compilation.kernel.GamaMetaModel;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.VariableDescription;
import msi.gaml.expressions.IExpression;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.IExecutable;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@species (
		name = IKeyword.PLATFORM,
		internal = true,
		doc = { @doc ("The species of the unique platform agent, called 'gama'") })
@vars ({ @var (
		name = PlatformAgent.MACHINE_TIME,
		type = IType.FLOAT,
		doc = @doc (
				value = "Returns the current system time in milliseconds",
				comment = "The return value is a float number")),
		@var (
				name = PlatformAgent.WORKSPACE_PATH,
				type = IType.STRING,
				constant = true,
				doc = @doc (
						value = "Contains the absolute path to the workspace of GAMA. Can be used to list all the projects and files present in the platform",
						comment = "Always terminated with a trailing separator",
						see = { "workspace" })),
		@var (
				name = "workspace",
				type = IType.FILE,
				constant = true,
				doc = @doc (
						value = "A folder representing the workspace of GAMA. Can be used to list all the projects and files present in the platform",
						see = { "workspace_path" })), })
public class PlatformAgent extends GamlAgent implements ITopLevelAgent, IExpression {

	public static final String WORKSPACE_PATH = "workspace_path";
	public static final String MACHINE_TIME = "machine_time";
	private final IScope scope;

	public PlatformAgent() {
		this(new GamaPopulation<PlatformAgent>(null,
				GamaMetaModel.INSTANCE.getAbstractModelSpecies().getMicroSpecies(IKeyword.PLATFORM)));
	}

	public PlatformAgent(final IPopulation<PlatformAgent> pop) {
		super(pop);
		scope = new ExecutionScope(this, "Gama platform scope");
	}

	@Override
	public Object primDie(final IScope scope) {
		GAMA.closeAllExperiments(false, true);
		scope.getGui().exit();
		return null;
	}

	@Override
	public ITopology getTopology() {
		return new AmorphousTopology();
	}

	@Override
	public ISpecies getSpecies() {
		return getPopulation().getSpecies();
	}

	@Override
	public SimulationClock getClock() {
		return new SimulationClock(getScope());
	}

	@Override
	public IScope getScope() {
		return scope;
	}

	@Override
	public GamaColor getColor() {
		return GamaColor.NamedGamaColor.colors.get("gamaorange");
	}

	@Override
	public RandomUtils getRandomGenerator() {
		return new RandomUtils();
	}

	@Override
	public IOutputManager getOutputManager() {
		if (getExperiment() != null)
			return getExperiment().getOutputManager();
		return null;
	}

	@Override
	public void postEndAction(final IExecutable executable) {}

	@Override
	public void postDisposeAction(final IExecutable executable) {}

	@Override
	public void postOneShotAction(final IExecutable executable) {}

	@Override
	public void executeAction(final IExecutable executable) {}

	@Override
	public boolean isOnUserHold() {
		return false;
	}

	@Override
	public void setOnUserHold(final boolean state) {}

	@Override
	public SimulationAgent getSimulation() {
		return GAMA.getSimulation();
	}

	@Override
	public IExperimentAgent getExperiment() {
		if (GAMA.getExperiment() != null)
			return GAMA.getExperiment().getAgent();
		return null;
	}

	@getter (
			value = WORKSPACE_PATH,
			initializer = true)
	public String getWorkspacePath() {
		final URL url = Platform.getInstanceLocation().getURL();
		return url.getPath();
	}

	@getter (PlatformAgent.MACHINE_TIME)
	public Double getMachineTime() {
		return (double) System.currentTimeMillis();
	}

	@Override
	public String getTitle() {
		return "gama platform agent";
	}

	@Override
	public String getDocumentation() {
		return "The unique instance of the platform species. Used to access platform properties";
	}

	@Override
	public String getDefiningPlugin() {
		return "msi.gama.core";
	}

	@Override
	public void collectMetaInformation(final GamlProperties meta) {}

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		return this;
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public String literalValue() {
		return IKeyword.GAMA;
	}

	@Override
	public IExpression resolveAgainst(final IScope scope) {
		return this;
	}

	@Override
	public boolean shouldBeParenthesized() {
		return false;
	}

	@Override
	public void collectUsedVarsOf(final IDescription species, final ICollector<VariableDescription> result) {}

	@Override
	public IType<?> getType() {
		return Types.get(IKeyword.PLATFORM);
	}
}
