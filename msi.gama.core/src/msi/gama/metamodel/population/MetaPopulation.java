/*********************************************************************************************
 *
 * 'MetaPopulation.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.metamodel.population;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import msi.gama.common.interfaces.IValue;
import msi.gama.common.util.RandomUtils;
import msi.gama.common.util.StringUtils;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.ILocation;
import msi.gama.metamodel.shape.IShape;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMap;
import msi.gama.util.IContainer;
import msi.gama.util.IList;
import msi.gama.util.TOrderedHashMap;
import msi.gama.util.matrix.IMatrix;
import msi.gaml.species.ISpecies;
import msi.gaml.types.IContainerType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;
import one.util.streamex.StreamEx;

/**
 * Class MetaPopulation. A list of IPopulation, ISpecies or MetaPopulation that behaves like a list of agents (also to
 * filter them).
 * 
 * @author drogoul
 * @since 8 déc. 2013
 * 
 */
@SuppressWarnings ({ "rawtypes", "unchecked" })
public class MetaPopulation implements IContainer.Addressable<Integer, IAgent>, IPopulationSet {

	protected final List<IPopulationSet<? extends IAgent>> populationSets;
	// We cache the value in case.
	protected Map<String, IPopulation> setOfPopulations;
	protected IContainerType type = Types.LIST.of(Types.AGENT);

	public MetaPopulation(final IPopulationSet... pop) {
		populationSets = Lists.newArrayList(pop);
	}

	@Override
	public StreamEx<IAgent> stream(final IScope scope) {
		return StreamEx.of(populationSets).flatMap(each -> each.stream(scope));
	}

	/**
	 * @param pop
	 */
	public void addPopulation(final IPopulation pop) {
		populationSets.add(pop);
	}

	@Override
	public IContainerType getType() {
		return type;
	}

	/**
	 * Method getAgents()
	 * 
	 * @see msi.gama.metamodel.topology.filter.IAgentFilter#getAgents()
	 */
	@Override
	public IContainer<?, ? extends IAgent> getAgents(final IScope scope) {
		final List<java.lang.Iterable<? extends IAgent>> result = new ArrayList<>();
		for (final IPopulationSet p : populationSets) {
			result.add(p.iterable(scope));
		}
		return GamaListFactory.create(scope, Types.AGENT, Iterables.concat(result));
	}

	/**
	 * Method accept()
	 * 
	 * @see msi.gama.metamodel.topology.filter.IAgentFilter#accept(msi.gama.runtime.IScope,
	 *      msi.gama.metamodel.shape.IShape, msi.gama.metamodel.shape.IShape)
	 */
	@Override
	public boolean accept(final IScope scope, final IShape source, final IShape a) {
		final IAgent agent = a.getAgent();
		if (agent == source.getAgent()) { return false; }
		return contains(scope, agent);
	}

	/**
	 * Method filter()
	 * 
	 * @see msi.gama.metamodel.topology.filter.IAgentFilter#filter(msi.gama.runtime.IScope,
	 *      msi.gama.metamodel.shape.IShape, java.util.Collection)
	 */
	@Override
	public void filter(final IScope scope, final IShape source, final Collection<? extends IShape> results) {
		final IAgent sourceAgent = source == null ? null : source.getAgent();
		results.remove(sourceAgent);
		results.removeIf((each) -> !contains(scope, each));
	}

	/**
	 * Method stringValue()
	 * 
	 * @see msi.gama.common.interfaces.IValue#stringValue(msi.gama.runtime.IScope)
	 */
	@Override
	public String stringValue(final IScope scope) throws GamaRuntimeException {
		return serialize(false);
	}

	/**
	 * Method copy()
	 * 
	 * @see msi.gama.common.interfaces.IValue#copy(msi.gama.runtime.IScope)
	 */
	@Override
	public IValue copy(final IScope scope) throws GamaRuntimeException {
		return new MetaPopulation(populationSets.toArray(new IPopulationSet[populationSets.size()]));
	}

	/**
	 * Method toGaml()
	 * 
	 * @see msi.gama.common.interfaces.IGamlable#toGaml()
	 */
	@Override
	public String serialize(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder(populationSets.size() * 10);
		sb.append('[');
		for (int i = 0; i < populationSets.size(); i++) {
			if (i != 0) {
				sb.append(',');
			}
			sb.append(StringUtils.toGaml(populationSets.get(i), includingBuiltIn));
		}
		sb.append(']');
		return sb.toString();

	}

	/**
	 * Method get()
	 * 
	 * @see msi.gama.util.IContainer#get(msi.gama.runtime.IScope, java.lang.Object)
	 */
	@Override
	public IAgent get(final IScope scope, final Integer index) throws GamaRuntimeException {
		return listValue(scope, Types.NO_TYPE, false).get(scope, index);
	}

	/**
	 * Method getFromIndicesList()
	 * 
	 * @see msi.gama.util.IContainer#getFromIndicesList(msi.gama.runtime.IScope, msi.gama.util.IList)
	 */
	@Override
	public IAgent getFromIndicesList(final IScope scope, final IList indices) throws GamaRuntimeException {
		return listValue(scope, Types.NO_TYPE, false).getFromIndicesList(scope, indices);
	}

	/**
	 * Method contains()
	 * 
	 * @see msi.gama.util.IContainer#contains(msi.gama.runtime.IScope, java.lang.Object)
	 */
	@Override
	public boolean contains(final IScope scope, final Object o) throws GamaRuntimeException {
		if (!(o instanceof IAgent)) { return false; }
		for (final IPopulationSet pop : populationSets) {
			if (pop.contains(scope, o)) { return true; }
		}
		return false;
	}

	/**
	 * Method first()
	 * 
	 * @see msi.gama.util.IContainer#first(msi.gama.runtime.IScope)
	 */
	@Override
	public IAgent firstValue(final IScope scope) throws GamaRuntimeException {
		if (populationSets.size() == 0) { return null; }
		return populationSets.get(0).firstValue(scope);
	}

	/**
	 * Method last()
	 * 
	 * @see msi.gama.util.IContainer#last(msi.gama.runtime.IScope)
	 */
	@Override
	public IAgent lastValue(final IScope scope) throws GamaRuntimeException {
		if (populationSets.size() == 0) { return null; }
		return populationSets.get(populationSets.size() - 1).lastValue(scope);
	}

	/**
	 * Method length()
	 * 
	 * @see msi.gama.util.IContainer#length(msi.gama.runtime.IScope)
	 */
	@Override
	public int length(final IScope scope) {
		int result = 0;
		for (final IPopulationSet p : populationSets) {
			result += p.length(scope);
		}
		return result;
	}

	/**
	 * Method isEmpty()
	 * 
	 * @see msi.gama.util.IContainer#isEmpty(msi.gama.runtime.IScope)
	 */
	@Override
	public boolean isEmpty(final IScope scope) {
		for (final IPopulationSet p : populationSets) {
			if (!p.isEmpty(scope)) { return false; }
		}
		return true;
	}

	/**
	 * Method reverse()
	 * 
	 * @see msi.gama.util.IContainer#reverse(msi.gama.runtime.IScope)
	 */
	@Override
	public IContainer reverse(final IScope scope) throws GamaRuntimeException {
		return listValue(scope, Types.AGENT, false).reverse(scope);
	}

	/**
	 * Method any()
	 * 
	 * @see msi.gama.util.IContainer#any(msi.gama.runtime.IScope)
	 */
	@Override
	public IAgent anyValue(final IScope scope) {
		if (populationSets.size() == 0) { return null; }
		final RandomUtils r = scope.getRandom();
		final int i = r.between(0, populationSets.size() - 1);
		return populationSets.get(i).anyValue(scope);
	}

	/**
	 * Method checkBounds()
	 * 
	 * @see msi.gama.util.IContainer#checkBounds(java.lang.Object, boolean)
	 */
	// @Override
	// public boolean checkBounds(final Integer index, final boolean forAdding)
	// {
	// return false;
	// }

	/**
	 * Method add()
	 * 
	 * @see msi.gama.util.IContainer#add(msi.gama.runtime.IScope, java.lang.Object, java.lang.Object, java.lang.Object,
	 *      boolean, boolean)
	 */
	// @Override
	// public void add(final IScope scope, final Integer index, final Object
	// value, final Object parameter,
	// final boolean all, final boolean add) {
	// // Not allowed
	// }

	/**
	 * Method remove()
	 * 
	 * @see msi.gama.util.IContainer#remove(msi.gama.runtime.IScope, java.lang.Object, java.lang.Object, boolean)
	 */
	// @Override
	// public void remove(final IScope scope, final Object index, final Object
	// value, final boolean all) {
	// // Not allowed
	// }

	/**
	 * Method listValue()
	 * 
	 * @see msi.gama.util.IContainer#listValue(msi.gama.runtime.IScope)
	 */
	@Override
	public IList<? extends IAgent> listValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		// WARNING: Verify it is ok because no casting is made here
		return GamaListFactory.create(scope, contentsType, iterable(scope));
	}

	/**
	 * Method matrixValue()
	 * 
	 * @see msi.gama.util.IContainer#matrixValue(msi.gama.runtime.IScope)
	 */
	@Override
	public IMatrix matrixValue(final IScope scope, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		return listValue(scope, contentsType, false).matrixValue(scope, contentsType, false);
	}

	/**
	 * Method matrixValue()
	 * 
	 * @see msi.gama.util.IContainer#matrixValue(msi.gama.runtime.IScope, msi.gama.metamodel.shape.ILocation)
	 */
	@Override
	public IMatrix matrixValue(final IScope scope, final IType contentsType, final ILocation preferredSize,
			final boolean copy) throws GamaRuntimeException {
		return listValue(scope, contentsType, false).matrixValue(scope, contentsType, preferredSize, false);
	}

	/**
	 * Method mapValue()
	 * 
	 * @see msi.gama.util.IContainer#mapValue(msi.gama.runtime.IScope)
	 */
	@Override
	public GamaMap mapValue(final IScope scope, final IType keyType, final IType contentsType, final boolean copy)
			throws GamaRuntimeException {
		return listValue(scope, contentsType, false).mapValue(scope, keyType, contentsType, false);
	}

	/**
	 * Method iterable()
	 * 
	 * @see msi.gama.util.IContainer#iterable(msi.gama.runtime.IScope)
	 */
	@Override
	public java.lang.Iterable<? extends IAgent> iterable(final IScope scope) {
		final List<java.lang.Iterable<? extends IAgent>> result = new ArrayList<>();
		for (final IPopulationSet p : populationSets) {
			result.add(p.iterable(scope));
		}
		return Iterables.concat(result);
	}

	/**
	 * Method getSpecies()
	 * 
	 * @see msi.gama.metamodel.topology.filter.IAgentFilter#getSpecies()
	 */
	@Override
	public ISpecies getSpecies() {
		return null; // We dont know what to return here.
	}

	private Map<String, IPopulation> getMapOfPopulations(final IScope scope) {
		if (setOfPopulations == null) {
			setOfPopulations = new TOrderedHashMap<>();
			for (final IPopulationSet pop : populationSets) {
				if (pop instanceof MetaPopulation) {
					setOfPopulations.putAll(((MetaPopulation) pop).getMapOfPopulations(scope));
				} else {
					final Collection<? extends IPopulation> pops = pop.getPopulations(scope);
					for (final IPopulation p : pops) {
						setOfPopulations.put(p.getName(), p);
					}
				}
			}
		}
		return setOfPopulations;
	}

	/**
	 * Method getPopulations()
	 * 
	 * @see msi.gama.metamodel.population.IPopulationSet#getPopulations(msi.gama.runtime.IScope)
	 */
	@Override
	public Collection<? extends IPopulation> getPopulations(final IScope scope) {
		return getMapOfPopulations(scope).values();
	}

	/**
	 * Method iterator()
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	// @Override
	// public Iterator<IAgent> iterator() {
	// List<Iterator<? extends IAgent>> iterators = new ArrayList<>();
	// for ( IPopulationSet p : populationSets ) {
	// iterators.add(p.iterator());
	// }
	// return Iterators.concat(iterators.iterator());
	// }

}
