/*********************************************************************************************
 *
 * 'AbstractPlaceHolderStatement.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gaml.statements;

import msi.gama.runtime.IScope;
import msi.gaml.descriptions.IDescription;

public abstract class AbstractPlaceHolderStatement extends AbstractStatement {

	public AbstractPlaceHolderStatement(final IDescription desc) {
		super(desc);
	}

	@Override
	protected Object privateExecuteIn(final IScope stack) {
		return null;
	}

}
