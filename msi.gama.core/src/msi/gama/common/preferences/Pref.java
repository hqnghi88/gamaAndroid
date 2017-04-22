package msi.gama.common.preferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import gnu.trove.map.hash.THashMap;
import msi.gama.common.util.StringUtils;
import msi.gama.kernel.experiment.IParameter;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

public class Pref<T> implements IParameter {

	String key, title, tab, group;
	T value, initial;
	final int type;
	List<T> values;
	Number min, max;
	boolean slider = true; // by default
	String[] activates, deactivates;
	Set<IPreferenceChangeListener<T>> listeners = new HashSet<IPreferenceChangeListener<T>>();
	private T[] v;

	Pref(final String key, final int type) {
		this.type = type;
		this.key = key;
	}

	public Pref<T> group(final String g) {
		this.group = g;
		return this;
	}

	public Pref<T> onChange(final Consumer<T> consumer) {
		addChangeListener(new IPreferenceChangeListener<T>() {

			@Override
			public boolean beforeValueChange(final T newValue) {
				return true;
			}

			@Override
			public void afterValueChange(final T newValue) {
				consumer.accept(newValue);

			}
		});
		return this;
	}

	public Pref<T> noSlider() {
		slider = false;
		return this;
	}

	public Pref<T> among(@SuppressWarnings ("unchecked") final T... v) {
		return among(Arrays.asList(v));
	}

	public Pref<T> among(final List<T> v) {
		this.values = v;
		return this;
	}

	public Pref<T> between(final Number mini, final Number maxi) {
		this.min = mini;
		this.max = maxi;
		return this;
	}

	public Pref<T> in(final String category, final String group) {
		this.tab = category;
		this.group = group;
		return this;
	}

	public Pref<T> named(final String t) {
		this.title = t;
		return this;
	}

	public Pref<T> init(final T v) {
		this.initial = v;
		this.value = v;
		return this;
	}

	public Pref<T> set(final T value) {
		if (isValueChanged(value) && acceptChange(value)) {
			this.value = value;
			afterChange(value);
		}
		return this;
	}

	private boolean isValueChanged(final T newValue) {
		return value == null ? newValue != null : !value.equals(newValue);
	}

	public Pref<T> activates(final String... link) {
		activates = link;
		return this;
	}

	public Pref<T> deactivates(final String... link) {
		deactivates = link;
		return this;
	}

	public T getValue() {
		return value;
	}

	@Override
	public IType<?> getType() {
		return Types.get(type);
	}

	@Override
	public String getTitle() {
		return title;
	}

	public String getKey() {
		return key;
	}

	public List<T> getValues() {
		return values;
	}

	@Override
	public String getName() {
		return key;
	}

	@Override
	public String getCategory() {
		return group;
	}

	@Override
	public String getUnitLabel(final IScope scope) {
		return null;
	}

	@Override
	public void setUnitLabel(final String label) {}

	@SuppressWarnings ("unchecked")
	@Override
	public void setValue(final IScope scope, final Object value) {
		set((T) value);
	}

	public Pref<T> addChangeListener(final IPreferenceChangeListener<T> r) {
		listeners.add(r);
		return this;
	}

	public void removeChangeListener(final IPreferenceChangeListener<T> r) {
		listeners.remove(r);
	}

	@Override
	public T value(final IScope scope) throws GamaRuntimeException {
		return getValue();
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		return StringUtils.toGaml(value, includingBuiltIn);
	}

	@Override
	public Object getInitialValue(final IScope scope) {
		return initial;
	}

	@Override
	public Number getMinValue(final IScope scope) {
		return min;
	}

	@Override
	public Number getMaxValue(final IScope scope) {
		return max;
	}

	@Override
	public List getAmongValue(final IScope scope) {
		return values;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public boolean isDefined() {
		return true;
	}

	@Override
	public void setDefined(final boolean b) {}

	@Override
	public Number getStepValue(final IScope scope) {
		return null;
	}

	/**
	 * If the value is modified, this method is called. Should return true to accept the change, false otherwise
	 */
	public boolean acceptChange(final T newValue) {
		for (final IPreferenceChangeListener<T> listener : listeners) {
			if (!listener.beforeValueChange(newValue)) { return false; }
		}
		return true;
	}

	protected void afterChange(final T newValue) {
		for (final IPreferenceChangeListener<T> listener : listeners) {
			listener.afterValueChange(newValue);
		}
	}

	public String[] getActivable() {
		return this.activates;
	}

	public String[] getDeactivable() {
		return this.deactivates;
	}

	public void save() {
		final Map<String, Object> map = new THashMap<>();
		map.put(getName(), getValue());
		GamaPreferences.setNewPreferences(map);
	}

	@Override
	public boolean acceptsSlider(final IScope scope) {
		return slider;
	}
}