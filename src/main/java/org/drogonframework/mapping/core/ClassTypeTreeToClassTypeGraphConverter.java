package org.drogonframework.mapping.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClassTypeTreeToClassTypeGraphConverter 
{
	private Map<Class<?>, LinkedHashSet<Class<?>>> adjacencyList = new HashMap<>();
	
	private List<Class<?>> classesRegistered;
	
	public ClassTypeTreeToClassTypeGraphConverter(Class<?> rootNode)
	{
		classesRegistered = new ArrayList<Class<?>>();
		classesRegistered.add(rootNode);
	
		LinkedHashSet<Class<?>> edgeEndPointsForRootNode = new LinkedHashSet<>();
		adjacencyList.put(rootNode, edgeEndPointsForRootNode);
	}
	
	public void addEdge(Class<?> parentNode, Class<?> clazzToAddToGraph)
	{
		if(!classesRegistered.contains(parentNode))
		{
			throw new RuntimeException("no parent node in graph");//TODO: Define own exception later on...
		}
			
		//register class if not exists
		if(!classesRegistered.contains(clazzToAddToGraph))
		{
			classesRegistered.add(clazzToAddToGraph);
			LinkedHashSet<Class<?>> edgeEndPointsForCurrentNode = new LinkedHashSet<>();
			adjacencyList.put(clazzToAddToGraph, edgeEndPointsForCurrentNode);
		}
		
		boolean edgeAdded = false;
		LinkedHashSet<Class<?>> currentEdgeEndsLinkedList = adjacencyList.get(parentNode);
		if(currentEdgeEndsLinkedList != null)
		{
			currentEdgeEndsLinkedList.add(clazzToAddToGraph);
			edgeAdded = true;
		}
		
		if(!edgeAdded)
		{
			throw new RuntimeException("Eklenecek edge in source vertexi bulunamadi");
		}
		
		if(checkIsCyclicAfterAnEdgeAdded())
		{
			throw new RuntimeException("Cyclic loop detected in current class association tree");
		}		
	}
	
	public boolean checkIsCyclicAfterAnEdgeAdded()
	{
		int numOfVertices = classesRegistered.size();
		boolean[] visitedVertices = new boolean[numOfVertices];
		boolean[] recursionHolder = new boolean[numOfVertices];
		List<Class<?>> recursiveClassesList = new CopyOnWriteArrayList<>();
		
		for(int i=0 ; i<numOfVertices; i++)
		{
			recursiveClassesList = new CopyOnWriteArrayList<>();
			recursiveClassesList.add(classesRegistered.get(i));
			if(isCyclicAssociation(i, visitedVertices, recursionHolder, recursiveClassesList))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isCyclicAssociation(int index, boolean[] visitedVertices, boolean[] recursionHolder, List<Class<?>> recursiveClassesList)
	{
		if(index == -1)//item not found in class registered list
		{
			return false;
		}

		if(recursionHolder[index])
		{
			StringBuilder errorMessageBuilder = new StringBuilder("A cyclic loop found between classes: ");
			for(int i = 0; i< recursiveClassesList.size(); i++)
			{				
				errorMessageBuilder.append(recursiveClassesList.get(i)).append(" ");
			}
			errorMessageBuilder.append(", rearrange class associations.");
			throw new RuntimeException(errorMessageBuilder.toString());
			//return true;
		}

		if(visitedVertices[index])
		{
			return false;
		}
		
		visitedVertices[index] = true;
		recursionHolder[index] = true;
		
		Class<?> classAsVertex = classesRegistered.get(index);
		LinkedHashSet<Class<?>> edgesOfCurrentVertex = adjacencyList.get(classAsVertex);
		
		if(edgesOfCurrentVertex == null)
		{
			return false;
		}
		
		for(Class<?> clzz : edgesOfCurrentVertex)
		{
			int indexOf = classesRegistered.indexOf(clzz);
			
			if(indexOf != -1)//class found in main list
			{
				recursiveClassesList.add(clzz);
			}
			
			isCyclicAssociation(indexOf, visitedVertices, recursionHolder, recursiveClassesList);
			/*
			if(isCyclicAssociation(indexOf, visitedVertices, recursionHolder, recursiveClassesList))
			{
				return true;
			}
			*/
			
			if(indexOf != -1)//class found in main list
			{
				recursiveClassesList.remove(clzz);
			}
		}
		
		recursionHolder[index] = false;
		return false;
	}
	
	public void traverse()
	{
		Iterator<Class<?>> iterator = adjacencyList.keySet().iterator();
		while(iterator.hasNext())
		{
			Class<?> beginVertex = iterator.next();
			LinkedHashSet<Class<?>> edgesOfVertex = adjacencyList.get(beginVertex);
			
			System.out.print( beginVertex.getSimpleName() + " -> ");
			Iterator<Class<?>> iteratorOfEdgeEnds = edgesOfVertex.iterator();
			while(iteratorOfEdgeEnds.hasNext())
			{				
				System.out.print(iteratorOfEdgeEnds.next() + ", ");
			}
			System.out.println();
		}
	}
}
