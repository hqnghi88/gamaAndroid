/*********************************************************************************************
 *
 * 'NAryOperator.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gaml.expressions;

import static msi.gama.precompiler.ITypeProvider.CONTENT_TYPE_AT_INDEX;
import static msi.gama.precompiler.ITypeProvider.INDEXED_TYPES;
import static msi.gama.precompiler.ITypeProvider.KEY_TYPE_AT_INDEX;
import static msi.gama.precompiler.ITypeProvider.TYPE_AT_INDEX;

import java.util.Arrays;

import msi.gama.common.preferences.GamaPreferences;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GAML;
import msi.gaml.descriptions.OperatorProto;
import msi.gaml.types.GamaType;
import msi.gaml.types.IType;

public class NAryOperator extends AbstractNAryOperator {

	public static IExpression create(final OperatorProto proto, final IExpression... child) {
		final NAryOperator u = new NAryOperator(proto, child);
		if (u.isConst() && GamaPreferences.Runtime.CONSTANT_OPTIMIZATION.getValue()) {
			final IExpression e =
					GAML.getExpressionFactory().createConst(u.value(null), u.getType(), u.serialize(false));
			// System.out.println(" ==== Simplification of " + u.toGaml() + "
			// into " + e.toGaml());
			return e;
		}
		return u;
	}

	public NAryOperator(final OperatorProto proto, final IExpression... exprs) {
		super(proto, exprs);
	}

	@Override
	protected IType computeType(final int t, final IType def, final int kind) {
		int index = -1;
		int kind_of_index = -1;
		if (t < INDEXED_TYPES) {
			if (t >= TYPE_AT_INDEX) {
				index = t - TYPE_AT_INDEX;
				kind_of_index = GamaType.TYPE;
			} else if (t >= CONTENT_TYPE_AT_INDEX) {
				index = t - CONTENT_TYPE_AT_INDEX;
				kind_of_index = GamaType.CONTENT;
			} else if (t >= KEY_TYPE_AT_INDEX) {
				index = t - KEY_TYPE_AT_INDEX;
				kind_of_index = GamaType.KEY;
			}
			if (index != -1 && exprs != null && index < exprs.length) {
				final IExpression expr = exprs[index];
				switch (kind_of_index) {
					case GamaType.TYPE:
						return expr.getType();
					case GamaType.CONTENT:
						return expr.getType().getContentType();
					case GamaType.KEY:
						return expr.getType().getKeyType();
				}
			}
		}
		return super.computeType(t, def, kind);
	}

	@Override
	public Object value(final IScope scope) throws GamaRuntimeException {
		final Object[] values = new Object[exprs == null ? 0 : exprs.length];
		try {
			for (int i = 0; i < values.length; i++) {
				values[i] = prototype.lazy[i] ? exprs[i] : exprs[i].value(scope);
			}
			final Object result = prototype.helper.run(scope, values);
			return result;
		} catch (final GamaRuntimeException e1) {
			e1.addContext("when applying the " + literalValue() + " operator on " + Arrays.toString(values));
			throw e1;
		} catch (final Throwable e) {
			final GamaRuntimeException ee = GamaRuntimeException.create(e, scope);
			ee.addContext("when applying the " + literalValue() + " operator on " + Arrays.toString(values));
			throw ee;
		}
	}

	@Override
	public String serialize(final boolean includingBuiltIn) {
		final StringBuilder sb = new StringBuilder();
		sb.append(literalValue());
		parenthesize(sb, exprs);
		return sb.toString();
	}

	@Override
	public NAryOperator copy() {
		final NAryOperator copy = new NAryOperator(prototype, exprs);
		return copy;
	}

}
