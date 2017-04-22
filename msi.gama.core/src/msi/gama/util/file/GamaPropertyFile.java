/*********************************************************************************************
 *
 * 'GamaPropertyFile.java, in plugin msi.gama.core, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.util.file;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import msi.gama.common.geometry.Envelope3D;
import msi.gama.precompiler.GamlAnnotations.file;
import msi.gama.precompiler.IConcept;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.GamaListFactory;
import msi.gama.util.GamaMap;
import msi.gama.util.GamaMapFactory;
import msi.gama.util.IList;
import msi.gaml.statements.Facets;
import msi.gaml.types.IContainerType;
import msi.gaml.types.IType;
import msi.gaml.types.Types;

@file (
		name = "property",
		extensions = { "properties" },
		buffer_type = IType.MAP,
		buffer_content = IType.STRING,
		buffer_index = IType.STRING,
		concept = { IConcept.FILE })
public class GamaPropertyFile extends GamaFile<GamaMap<String, String>, String, String, String> {

	public GamaPropertyFile(final IScope scope, final String pathName) throws GamaRuntimeException {
		super(scope, pathName);
	}

	public GamaPropertyFile(final IScope scope, final String pathName, final GamaMap<String, String> buffer)
			throws GamaRuntimeException {
		super(scope, pathName, buffer);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see msi.gama.util.GamaFile#fillBuffer()
	 */
	@SuppressWarnings ({ "unchecked", "rawtypes" })
	@Override
	protected void fillBuffer(final IScope scope) throws GamaRuntimeException {
		final Properties p = new Properties();
		final GamaMap m = GamaMapFactory.create(Types.STRING, Types.STRING);
		try (FileReader f = new FileReader(getFile(scope))) {
			p.load(f);
		} catch (final IOException e) {
			throw GamaRuntimeException.create(e, scope);
		}
		m.putAll(p);
		setBuffer(m);
	}

	@Override
	public IContainerType<?> getType() {
		return Types.FILE.of(Types.STRING, Types.STRING);
	}

	@Override
	public IList<String> getAttributes(final IScope scope) {
		// TODO return the keys of the map as "attributes"
		return GamaListFactory.create();
	}

	@Override
	protected void flushBuffer(final IScope scope, final Facets facets) throws GamaRuntimeException {
		final Properties p = new Properties();
		if (getBuffer() != null && !getBuffer().isEmpty())
			getBuffer().forEachEntry((a, b) -> {
				p.setProperty(a, b);
				return true;
			});
		try (FileWriter fw = new FileWriter(getFile(scope))) {
			p.store(fw, null);
		} catch (final IOException e) {}

	}

	@Override
	public Envelope3D computeEnvelope(final IScope scope) {
		// TODO Probably possible to get some information there
		return null;
	}

}
