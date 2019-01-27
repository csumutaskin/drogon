package org.drogonframework.mapping.core;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * Performs a BFS to identify the class associations on a given root class to find whether a cyclic association exists between the types of objects/primitives used as instance variable or not 
 * @author UMUT
 * 
 */
public class ClassTypeTreeTraverser 
{
	private Deque<Class<?>> bfsQueue = new ArrayDeque<>();
	private Deque<Class<?>> alreadyVisited = new ArrayDeque<>();
	
	private List<Class<?>> collectionClasses = new ArrayList<>();  
	
	private ClassTypeTreeToClassTypeGraphConverter cc;

	/**
	 * Tries to detect if there is a cyclic association or not
	 * @param clazz
	 * @return
	 */
	public boolean traverseUsingBFS(Class<?> clazz)
	{
		initializeEnvironment();
		bfsQueue.offer(clazz);
		
		cc = new ClassTypeTreeToClassTypeGraphConverter(clazz);
		
		while(!bfsQueue.isEmpty())
		{
			visitFromQueue();
		}	
		return false;
	}
	
	/**
	 * Recursive traversing method starting from the root class
	 * @param clazz
	 */
	public void visitFromQueue()
	{
		Class<?> clazz = bfsQueue.poll();
		if(clazz == null)
		{
			return;
		}

		alreadyVisited.add(clazz);
		Collection<Field> allDeclaredFieldsOfClazz = new ArrayList<>();
		// TODO: getter a gore mi, direkt field okuma mi?
		addAllRelevantFieldsOfClassToFinalCollection(allDeclaredFieldsOfClazz, clazz);

		Class<?> superClass = clazz.getSuperclass();
		while(superClass != null)
		{
			addAllRelevantFieldsOfClassToFinalCollection(allDeclaredFieldsOfClazz, superClass);
			superClass = superClass.getSuperclass();
		}
		if(allDeclaredFieldsOfClazz.isEmpty())
		{
			return;
		}
		for(Field childTypeInTurn: allDeclaredFieldsOfClazz)
		{
			if(isAssignableFromAnyCollectionAPI(childTypeInTurn.getType()))
			{
				ParameterizedType typeErasuredTypes = (ParameterizedType) childTypeInTurn.getGenericType();
		        Class<?> erasuredType = (Class<?>) typeErasuredTypes.getActualTypeArguments()[0];//TODO: Map tipleri icin key ve value pairler ayri ayri eklenebilir.
		        //System.out.println("A collection found in " + clazz.getSimpleName() + " which is of type " + childTypeInTurn.getType().getSimpleName() + " which consists of " + erasuredType);
		        cc.addEdge(clazz, erasuredType);
				bfsQueue.offer(erasuredType);
				
			}
			else if(childTypeInTurn.getType().isArray())
			{
				Class<?> componentType = childTypeInTurn.getType().getComponentType();
								
				if(componentType != null)
				{
					//System.out.println("An array found in " + clazz.getSimpleName() + " which is " + componentType.getSimpleName());
					cc.addEdge(clazz, componentType);
					bfsQueue.offer(componentType);
				}
			}
			else
			{
				Class<?> classInAssociationAsInstanceVariable = childTypeInTurn.getType();
				if(classInAssociationAsInstanceVariable != null)
				{
					cc.addEdge(clazz, classInAssociationAsInstanceVariable);
					bfsQueue.offer(classInAssociationAsInstanceVariable);
				}
			}
		}
	}
	
	public void printGraph()
	{
		cc.traverse();
	}
	
	/* ************************************************************************************************	
     *                                    PRIVATE METHODS                                             *
     **************************************************************************************************/

	private boolean isAssignableFromAnyCollectionAPI(Class<?> clazz)
	{
		for(Class<?> collectionClass : collectionClasses)
		{
			if(collectionClass.isAssignableFrom(clazz))
			{
				return true;
			}
		}
		return false;
	}

	private void initializeEnvironment()
	{
		collectionClasses.add(Iterable.class);
		collectionClasses.add(Map.class);
		bfsQueue = new ArrayDeque<>();
		alreadyVisited = new ArrayDeque<>();
	}
	
	private boolean isFieldStatic(Field field)
	{
		boolean toReturn = false;
		if(java.lang.reflect.Modifier.isStatic(field.getModifiers()))
		{
			toReturn = true;
		}
		return toReturn;
	}
	
	private void addAllRelevantFieldsOfClassToFinalCollection(Collection<Field> finalCollection, Class<?> clazz)
	{
		Field[] declaredFields = clazz.getDeclaredFields();
		
		if(declaredFields != null && declaredFields.length != 0)
		{
			for(Field inTurn : declaredFields)
			{
				if(!isFieldStatic(inTurn))
				{
					finalCollection.add(inTurn);
				}
			}
		}
	}
}
