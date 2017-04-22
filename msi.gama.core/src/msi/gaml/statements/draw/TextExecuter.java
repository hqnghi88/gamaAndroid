/*********************************************************************************************
 *
 * 'TextExecuter.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gaml.statements.draw;

import java.awt.geom.Rectangle2D;

import msi.gama.common.geometry.Scaling3D;
import msi.gama.common.interfaces.IGraphics;
import msi.gama.metamodel.shape.GamaPoint;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.expressions.IExpression;
import msi.gaml.operators.Cast;

class TextExecuter extends DrawExecuter {

	private final String constText;

	TextExecuter(final IExpression item) throws GamaRuntimeException {
		super(item);
		constText = item.isConst() ? Cast.asString(null, item.value(null)) : null;
	}

	@Override
	Rectangle2D executeOn(final IScope scope, final IGraphics g, final DrawingData data) throws GamaRuntimeException {
		final String info = constText == null ? Cast.asString(scope, item.value(scope)) : constText;
		if (info == null || info.length() == 0) { return null; }
		final TextDrawingAttributes attributes = computeAttributes(scope, data);
		return g.drawString(info, attributes);
	}

	TextDrawingAttributes computeAttributes(final IScope scope, final DrawingData data) {
		final TextDrawingAttributes attributes =
				new TextDrawingAttributes(Scaling3D.of(data.size.value), data.rotation.value, data.location.value,
						data.getCurrentColor(), data.font.value, data.perspective.value);
		// We push the location of the agent if none has been provided
		attributes.setLocationIfAbsent(new GamaPoint(scope.getAgent().getLocation()));
		return attributes;
	}
}