/*********************************************************************************************
 *
 * 'IAttributed.java, in plugin ummisco.gama.annotations, is part of the source code of the GAMA modeling and simulation
 * platform. (c) 2007-2016 UMI 209 UMMISCO IRD/UPMC & Partners
 *
 * Visit https://github.com/gama-platform/gama for license information and developers contact.
 * 
 *
 **********************************************************************************************/
package msi.gama.common.interfaces;

import java.util.Map;

public interface IAttributed {

	/**
	 * Allows to retrieve the attributes of the object as a GamaMap
	 * 
	 * @return a map containing the attributes or null if no attributes are defined
	 */
	public Map<String, Object> getAttributes();

	/**
	 * Allows to retrieve the attributes of the object as a GamaMap. If the object has no attributes, should return an
	 * empty map
	 * 
	 * @return a map containing the attributes or an empty map if no attributes are defined
	 */
	public Map<String, Object> getOrCreateAttributes();

	/**
	 * Allows to retrieve the value stored at key "key"
	 * 
	 * @return the value stored at key "key". Returns null if no such key exists. However, please note that null is a
	 *         valid value, which means that receiving null when calling this method does not necessarily mean that the
	 *         key is absent. Use hasAttribute(Object key) to verify the presence of a key
	 */
	public Object getAttribute(String key);

	/**
	 * Allows to set the value stored at key "key". A new entry is created when "key" is not already present, otherwise
	 * the previous occurrence is replaced.
	 * 
	 */

	public void setAttribute(String key, Object value);

	/**
	 * Answers whether or not this object has any value set at key "key".
	 * 
	 * @return true if the object has such an attribute, false otherwise
	 */
	public boolean hasAttribute(String key);

}
