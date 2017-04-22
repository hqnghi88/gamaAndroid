/*********************************************************************************************
 *
 * 'AbstractStatementSequence.java, in plugin msi.gama.core, is part of the source code of the
 * GAMA modeling and simulation platform.
 * (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gaml.statements;

import com.google.common.collect.FluentIterable;

import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gaml.compilation.ISymbol;
import msi.gaml.descriptions.IDescription;
import msi.gaml.descriptions.StatementDescription;

public class AbstractStatementSequence extends AbstractStatement {

	protected IStatement[] commands;

	public AbstractStatementSequence(final IDescription desc) {
		super(desc);
	}

	@Override
	public void setChildren(final Iterable<? extends ISymbol> commands) {
		this.commands = FluentIterable.from(commands).filter(IStatement.class).toArray(IStatement.class);
	}

	public boolean isEmpty() {
		return commands.length == 0;
	}

	@Override
	public Object executeOn(final IScope scope) throws GamaRuntimeException {
		enterScope(scope);
		Object result;
		try {
			result = super.executeOn(scope);
		} finally {
			leaveScope(scope);
		}
		return result;
	}

	@Override
	public Object privateExecuteIn(final IScope scope) throws GamaRuntimeException {
		Object lastResult = null;
		for (int i = 0; i < commands.length; i++) {
			if (scope.interrupted()) {
				return lastResult;
			}
			lastResult = commands[i].executeOn(scope);
		}
		return lastResult;
	}

	public void leaveScope(final IScope scope) {
		// Clears any action_halted status in case we are a top-level behavior
		// (reflex, init, state, etc.)
		final StatementDescription description = getDescription();
		if (description != null && description.getMeta().isTopLevel()) {
			scope.popAction();
		}
		scope.pop(this);
	}

	public void enterScope(final IScope scope) {
		scope.push(this);
	}

	public IStatement[] getCommands() {
		return commands;
	}

}