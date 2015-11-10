package com.pful.pico.core;


import java.util.Map;

/**
 * Entity is a class to represent the information that an user needs.
 * It is retrieved and stored in the database.
 */
public class Entity
{
	/**
	 * appId is an identifier to recognize which an application have the entity.
	 */
	private String appId;

	/**
	 * id is an unique identifier.
	 */
	private String id;

	/**
	 * type is a classification for what an entity is.
	 * Any string value could be given for it.
	 * No rule exists for the type.
	 * For example, 'person' represents the entity contains the information for a person.
	 * Also any hierarchical representation could be used such as 'animal/mammal'.
	 */
	private String type;

	/**
	 * properties is a collection to hold the information that the entity needs.
	 */
	private Map<String, Object> properties;

	/**
	 * createdAt is a unix timestamp representing when the entity was created.
	 */
	private long createdAt;

	/**
	 * updatedAt is a unix timestamp representing when the entity was updated.
	 */
	private long updatedAt;

	public static void create(final ApplicationContext context,
	                          final String type,
	                          final Map<String, Object> properties,
	                          final EntityManipulationCallback callback)
			throws PICOException
	{
	}

	public void read(final ApplicationContext context, final String id, final EntityManipulationCallback callback)
			throws PICOException
	{
		// TODO Implement
	}

	void update(final ApplicationContext context,
	            final String id,
	            final String type,
	            final Map<String, Object> properties,
	            final EntityManipulationCallback callback)
			throws PICOException
	{
	}

	void delete(final ApplicationContext context, final String id, final EntityManipulationCallback callback)
			throws PICOException
	{
	}

	void list(final ApplicationContext context, final String type, final int offset, final int limit, final EntityListCallback callback)
			throws PICOException
	{

	}
}
