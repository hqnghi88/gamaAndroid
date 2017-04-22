/*********************************************************************************************
 *
 * 'SpeciesLayer.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.outputs.layers;

import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import msi.gama.common.interfaces.IGraphics;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.agent.IMacroAgent;
import msi.gama.metamodel.population.IPopulation;
import msi.gama.runtime.IScope;
import msi.gama.runtime.IScope.ExecutionResult;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.species.ISpecies;
import msi.gaml.statements.AspectStatement;
import msi.gaml.statements.IExecutable;

/**
 * Written by drogoul Modified on 23 août 2008
 */

public class SpeciesLayer extends AgentLayer {

	public SpeciesLayer(final ILayerStatement layer) {
		super(layer);
	}

	@Override
	public Set<IAgent> getAgentsForMenu(final IScope scope) {
		final Set<IAgent> result = ImmutableSet.copyOf(
				scope.getSimulation().getMicroPopulation(((SpeciesLayerStatement) definition).getSpecies()).iterator());
		return result;
	}

	@Override
	public String getType() {
		return "Species layer";
	}

	@Override
	public void privateDrawDisplay(final IScope scope, final IGraphics g) throws GamaRuntimeException {
		shapes.clear();
		final ISpecies species = ((SpeciesLayerStatement) definition).getSpecies();
		final IMacroAgent world = scope.getSimulation();
		if (world != null && !world.dead()) {
			final IPopulation<? extends IAgent> microPop = world.getMicroPopulation(species);
			if (microPop != null) {
				drawPopulation(scope, g, (SpeciesLayerStatement) definition, microPop);
			}
		}
	}

	private void drawPopulation(final IScope scope, final IGraphics g, final SpeciesLayerStatement layer,
			final IPopulation<? extends IAgent> population) throws GamaRuntimeException {
		IExecutable aspect = layer.getAspect();
		// IAspect aspect = population.getAspect(layer.getAspectName());
		if (aspect == null) {
			aspect = AspectStatement.DEFAULT_ASPECT;
		}
		// IAgent[] _agents = null;
		// _agents = Iterators.toArray(population.iterator(), IAgent.class);

		// draw the population. A copy of the population is made to avoid
		// concurrent modification exceptions
		for (final IAgent a : /* population.iterable(scope) */population.toArray()) {
			if (a == null || a.dead()) {
				continue;
			}
			ExecutionResult result;
			if (a == scope.getGui().getHighlightedAgent()) {
				IExecutable hAspect = population.getSpecies().getAspect("highlighted");
				if (hAspect == null) {
					hAspect = aspect;
				}
				result = scope.execute(hAspect, a, null);
			} else {
				result = scope.execute(aspect, a, null);
			}
			final Rectangle2D r = (Rectangle2D) result.getValue();
			if (r != null) {
				shapes.put(a, r);
			}
			if (!(a instanceof IMacroAgent)) {
				continue;
			}
			IPopulation<? extends IAgent> microPop;
			// draw grids first...
			final List<GridLayerStatement> gridLayers = layer.getGridLayers();
			for (final GridLayerStatement gl : gridLayers) {
				// a.acquireLock();
				if (a.dead() /* || scope.interrupted() */ ) {
					continue;
				}
				microPop = ((IMacroAgent) a).getMicroPopulation(gl.getName());
				if (microPop != null && microPop.size() > 0) {
					// FIXME Needs to be entirely redefined using the new
					// interfaces
					// drawGridPopulation(a, gl, microPop, scope, g);
				}
			}

			// then recursively draw the micro-populations
			final List<SpeciesLayerStatement> microLayers = layer.getMicroSpeciesLayers();
			for (final SpeciesLayerStatement ml : microLayers) {
				// a.acquireLock();
				if (a.dead()) {
					continue;
				}
				microPop = ((IMacroAgent) a).getMicroPopulation(ml.getSpecies());

				if (microPop != null && microPop.size() > 0) {
					drawPopulation(scope, g, ml, microPop);
				}
			}
		}

	}

	// private void drawGridPopulation(final IAgent host, final
	// GridLayerStatement layer, final IPopulation population,
	// final IScope scope, final IGraphics g) throws GamaRuntimeException {
	// GamaSpatialMatrix gridAgentStorage = (GamaSpatialMatrix)
	// population.getTopology().getPlaces();
	// gridAgentStorage.refreshDisplayData(scope);
	//
	// // MUST cache this image as GridDisplayLayer does to increase performance
	// BufferedImage supportImage =
	// ImageUtils.createCompatibleImage(gridAgentStorage.numCols,
	// gridAgentStorage.numRows);
	// supportImage.setRGB(0, 0, gridAgentStorage.numCols,
	// gridAgentStorage.numRows,
	// gridAgentStorage.getDisplayData(), 0, gridAgentStorage.numCols);
	//
	// IShape hostShape = host.getGeometry();
	// Envelope hostEnv = hostShape.getEnvelope();
	// g.setDrawingCoordinates(hostEnv.getMinX() * g.getXScale(),
	// hostEnv.getMinY() * g.getYScale());
	// g.setDrawingDimensions((int) (gridAgentStorage.numCols * g.getXScale()),
	// (int) (gridAgentStorage.numCols * g.getYScale()));
	// g.setOpacity(layer.getTransparency());
	// g.drawImage(scope, supportImage, null, 0.0f, true);
	//
	// }

}
