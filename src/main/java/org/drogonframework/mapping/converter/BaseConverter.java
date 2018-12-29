package org.drogonframework.mapping.converter;

import java.io.Serializable;
import java.util.Collection;

import org.drogonframework.mapping.entity.ConvertibleDTO;
import org.drogonframework.mapping.entity.ConvertibleEntity;

@SuppressWarnings("serial")
public abstract class BaseConverter<E extends ConvertibleEntity,D extends ConvertibleDTO> implements Serializable
{	
	public abstract E convertToEntity(D bd);	
	public abstract D convertToDTO(E be);	
	public abstract Collection<D> mapEntityCollectionToDTOCollection(Collection<E> entityCollection);	
	public abstract Collection<E> mapEntityDTOtoEntityCollection(Collection<D> dtoCollection);	
}