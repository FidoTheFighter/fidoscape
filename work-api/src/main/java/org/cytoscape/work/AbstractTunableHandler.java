package org.cytoscape.work;

/*
 * #%L
 * Cytoscape Work API (work-api)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;


/** 
 * Provides the standard implementation for most of the methods declared by the 
 * TunableHandler interface.
 * @CyAPI.Abstract.Class
 * @CyAPI.InModule work-api
 */
public abstract class AbstractTunableHandler implements TunableHandler {
	private enum ParamsParseState {
		KEY_START, LOOKING_FOR_EQUAL_SIGN, VALUE_START, LOOKING_FOR_SEMICOLON;
	}

	private final Field field;
	private final Method getter;
	private final Method setter;
	private final Reference<?> instance;
	private final Tunable tunable;
	public static final String TOOLTIP = "tooltip";
	public static final String GRAVITY = "gravity";
	public static final String CONTEXT = "context";

	/** 
	 * Standard base class constructor for <code>TunableHandler</code>s that deal with
	 * <code>Tunable</code>s that annotate a field.
	 *
	 * @param field    An instance of <code>Field</code> that represents a field 
	 * annotated with <code>@Tunable</code>
	 * @param instance An object instance that contains a field corresponding to 
	 * the <i>field</i> parameter
	 * @param tunable  The <code>Tunable</code> that annotates <i>field</i>
	 */
	public AbstractTunableHandler(final Field field, final Object instance, final Tunable tunable) {
		this.field = field;
		this.getter = null;
		this.setter = null;
		this.instance = new WeakReference<Object>(instance);
		this.tunable = tunable;
	}

	/** 
	 * Standard base class constructor for <code>TunableHandler</code>s that deal with
	 * <code>Tunable</code>s that use getter and setter methods.
	 *
	 * @param getter The getter method of the tunable object represented by the 
	 * <i>instance</i> parameter.
	 * @param setter The setter method complimentary to the getter.
	 * @param instance An instance of an object with a getter method that has been 
	 * determined to be annotated with <code>@Tunable</code>.
	 * @param tunable  The <code>Tunable</code> that annotates the <i>getter</i>.
	 */
	public AbstractTunableHandler(final Method getter, final Method setter, final Object instance, final Tunable tunable) {
		this.field = null;
		this.getter = getter;
		this.setter = setter;
		this.instance = new WeakReference<Object>(instance);
		this.tunable = tunable;
	}

	/** 
	 * {@inheritDoc}
	 */
	public final Class<?> getType() {
		return field != null ? field.getType() : getter.getReturnType();
	}

	/** 
	 * {@inheritDoc}
	 */
	public final Object getValue() throws IllegalAccessException, InvocationTargetException {
		return field != null ? field.get(instance.get()) : getter.invoke(instance.get());
	}

	/**
	 * {@inheritDoc}
	 */
	 public void setValue(final Object newValue) throws IllegalAccessException, InvocationTargetException {
		 
		if (field != null)
			field.set(instance.get(), newValue);
		else
			setter.invoke(instance.get(), newValue);
		
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getDescription() {
		return tunable.description();
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] getGroups() {
		return tunable.groups();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public final boolean controlsMutuallyExclusiveNestedChildren() {
		return tunable.xorChildren();
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getChildKey() {
		return tunable.xorKey();
	}

	/**
	 * {@inheritDoc}
	 */
	public final String dependsOn() {
		return tunable.dependsOn();
	}

	/**
	 * {@inheritDoc}
	 */
	public final String[] listenForChange() {
		return tunable.listenForChange();
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getName() {
		if (field != null)
			return field.getName();
		else
			return setter.getName().substring(3);
	}

	/**
	 * {@inheritDoc}
	 */
	public final String getQualifiedName() {
		final String unqualifiedClassName =
			field == null ? getter.getDeclaringClass().toString() : field.getDeclaringClass().toString();
		
                return unqualifiedClassName.substring(unqualifiedClassName.lastIndexOf(".") + 1) + "." + getName();
	}

	/**
	 * {@inheritDoc}
	 */
	public final Properties getParams() throws IllegalArgumentException {
		final String rawString = tunable.params();
		final Properties keyValuesPairs = new Properties();
		keyValuesPairs.put(GRAVITY, Double.toString(tunable.gravity()));
		keyValuesPairs.put(TOOLTIP, tunable.tooltip());
		keyValuesPairs.put(CONTEXT, tunable.context());

		StringBuilder key = null;
		StringBuilder value = null;
		ParamsParseState state = ParamsParseState.KEY_START;
		boolean escaped = false;
		for (int i = 0; i < rawString.length(); ++i) {
			final char ch = rawString.charAt(i);

			switch (state) {
			case KEY_START:
				key = new StringBuilder();
				if (!Character.isLetter(ch))
					throw new IllegalArgumentException(getName() + "'s getParams() returns an invalid key.");
				key.append(ch);
				state = ParamsParseState.LOOKING_FOR_EQUAL_SIGN;
				break;
			case LOOKING_FOR_EQUAL_SIGN:
				if (ch == '=')
					state = ParamsParseState.VALUE_START;
				else {
					if (!Character.isLetter(ch))
						throw new IllegalArgumentException(getName() + "'s getParams() returns an invalid key.");
					key.append(ch);
				}
				break;
			case VALUE_START:
				value = new StringBuilder();
				if (ch == ';')
					throw new IllegalArgumentException(getName() + "'s getParams() returns an invalid value.");
				if (ch == '\\')
					escaped = true;
				else
					value.append(ch);
				state = ParamsParseState.LOOKING_FOR_SEMICOLON;
				break;
			case LOOKING_FOR_SEMICOLON:
				if (escaped) {
					value.append(ch);
					escaped = false;
				} else if (ch == ';') {
					keyValuesPairs.setProperty(key.toString(), value.toString());
					state = ParamsParseState.KEY_START;
				} else {
					if (ch == '\\')
						escaped = true;
					else
						value.append(ch);
				}
				break;
			}
		}

		if (escaped)
			throw new IllegalArgumentException(getName() + "'s getParams() returns an invalid escaped character.");
		if (state != ParamsParseState.KEY_START && state != ParamsParseState.LOOKING_FOR_SEMICOLON)
			throw new IllegalArgumentException(getName() + "'s getParams() returns an incomplete string: \"" + rawString + "\".");

		if (key != null) {
			if (value == null)
				throw new IllegalArgumentException(getName() + "'s getParams() returns a key without a value.");
			keyValuesPairs.setProperty(key.toString(), value.toString());
		}

		return keyValuesPairs;
	}

	/**
	 * This returns the context of the Tunable.  The context must be one of:
	 * <ul><li><b>gui</b>: used only when a graphical user interface is available</li>
	 * <li><b>nogui</b>: used only when a graphical user interface is not available</li>
	 * <li><b>both</b>: always used</li>
	 * </ul>
	 *
	 * @return the context
	 */
	public final String getContext() {
		return tunable.context();
	}

	/**
	 * Returns the tooltip annotation for this Tunable if there is one.
	 *
	 * @return tooltip string, if any
	 */
	public final String getTooltip() {
		return tunable.tooltip();
	}

	/**
	 * Returns the gravity value for the annotation.
	 *
	 * @return Tunable gravity as a double
	 */
	public final double getGravity() {
		return tunable.gravity();
	}

	/**
	 * Returns true if this Tunable must be set (i.e. is required), otherwise
	 * returns false.
	 *
	 * @return true if it is required.
	 */
	public final boolean getRequired() {
		return tunable.required();
	}
}
