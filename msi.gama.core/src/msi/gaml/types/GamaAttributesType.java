package msi.gaml.types;

import java.util.Map;

import msi.gama.common.interfaces.IKeyword;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.agent.SavedAgent;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.type;
import msi.gama.precompiler.IConcept;
import msi.gama.precompiler.ISymbolKind;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gaml.expressions.IExpression;

@type (
		name = IKeyword.ATTRIBUTES,
		id = IType.ATTRIBUTES,
		wraps = { SavedAgent.class },
		kind = ISymbolKind.Variable.CONTAINER,
		concept = { IConcept.TYPE, IConcept.CONTAINER, IConcept.MAP },
		doc = @doc ("Represents the attributes of an agent, a special kind of map<string, unknown> with additional information such as its index or its sub-populations"))

public class GamaAttributesType extends GamaMapType {

	@SuppressWarnings ("unchecked")
	@Override
	public SavedAgent cast(final IScope scope, final Object obj, final Object param, final IType keyType,
			final IType contentType, final boolean copy) throws GamaRuntimeException {
		if (obj instanceof IAgent)
			return new SavedAgent(scope, (IAgent) obj);
		if (obj instanceof SavedAgent) {
			if (copy)
				return ((SavedAgent) obj).clone();
			else
				return (SavedAgent) obj;
		}
		if (obj instanceof Map) {
			final GamaMap<String, Object> map = GamaMapFactory.create(scope, Types.STRING, Types.NO_TYPE, (Map) obj);
			return new SavedAgent(map);
		}
		return null;
	}

	@Override
	public IType keyTypeIfCasting(final IExpression exp) {
		return Types.get(STRING);
	}

}
