//package org.drogonframework.mapping.core;
//
//import java.lang.reflect.Field;
//import java.lang.reflect.ParameterizedType;
//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Deque;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import javax.lang.model.element.Element;
//import javax.lang.model.element.ElementKind;
//import javax.lang.model.element.Modifier;
//import javax.lang.model.element.TypeElement;
//import javax.lang.model.type.DeclaredType;
//import javax.lang.model.type.TypeMirror;
//
//public class ElementTypeTreeTraverser 
//{
//	private Deque<Element> bfsQueue = new ArrayDeque<>();
//	private Deque<Element> alreadyVisited = new ArrayDeque<>();
//	
//	private List<Element> collectionClasses = new ArrayList<>();  
//	
//	private ElementTypeTreeToElementTypeGraphConverter cc;
//
//	/**
//	 * Tries to detect if there is a cyclic association or not
//	 * @param clazz
//	 * @return
//	 */
//	public boolean traverseUsingBFS(Element clazz)
//	{
//		initializeEnvironment();
//		bfsQueue.offer(clazz);
//		
//		cc = new ElementTypeTreeToElementTypeGraphConverter(clazz);
//		
//		while(!bfsQueue.isEmpty())
//		{
//			visitFromQueue();
//		}	
//		return false;
//	}
//	
//	/**
//	 * Recursive traversing method starting from the root class
//	 * @param clazz
//	 */
//	public void visitFromQueue()
//	{
//		Element clazz = bfsQueue.poll();
//		if(clazz == null)
//		{
//			return;
//		}
//
//		alreadyVisited.add(clazz);
//		Collection<Element> allDeclaredFieldsOfClazz = new ArrayList<>();
//		// TODO: getter a gore mi, direkt field okuma mi?
//		addAllRelevantFieldsOfClassToFinalCollection(allDeclaredFieldsOfClazz, clazz);
//
//		TypeElement t = (TypeElement)clazz;
//		
//		TypeMirror superClass = t.getSuperclass();
//				
//		DeclaredType superClassAsDeclared = (DeclaredType)superClass; 
//		Element superClassasElement = superClassAsDeclared.asElement();
//		
//		while(superClassasElement != null)
//		{
//			addAllRelevantFieldsOfClassToFinalCollection(allDeclaredFieldsOfClazz, superClassasElement);
//			
//			t = (TypeElement)superClassasElement;
//			superClass = t.getSuperclass();
//			superClassAsDeclared = (DeclaredType)superClass; 
//			superClassasElement = superClassAsDeclared.asElement();
//		}
//		if(allDeclaredFieldsOfClazz.isEmpty())
//		{
//			return;
//		}
//		for(Element childTypeInTurn: allDeclaredFieldsOfClazz)
//		{
//			if(isAssignableFromAnyCollectionAPI(childTypeInTurn))//burdayim!!!!
//			{
//				ParameterizedType typeErasuredTypes = (ParameterizedType) childTypeInTurn.getGenericType();
//		        Class<?> erasuredType = (Class<?>) typeErasuredTypes.getActualTypeArguments()[0];//TODO: Map tipleri icin key ve value pairler ayri ayri eklenebilir.
//		        //System.out.println("A collection found in " + clazz.getSimpleName() + " which is of type " + childTypeInTurn.getType().getSimpleName() + " which consists of " + erasuredType);
//		        cc.addEdge(clazz, erasuredType);
//				bfsQueue.offer(erasuredType);
//				
//			}
//			else if(childTypeInTurn.getType().isArray())
//			{
//				Class<?> componentType = childTypeInTurn.getType().getComponentType();
//								
//				if(componentType != null)
//				{
//					//System.out.println("An array found in " + clazz.getSimpleName() + " which is " + componentType.getSimpleName());
//					cc.addEdge(clazz, componentType);
//					bfsQueue.offer(componentType);
//				}
//			}
//			else
//			{
//				Class<?> classInAssociationAsInstanceVariable = childTypeInTurn.getType();
//				if(classInAssociationAsInstanceVariable != null)
//				{
//					cc.addEdge(clazz, classInAssociationAsInstanceVariable);
//					bfsQueue.offer(classInAssociationAsInstanceVariable);
//				}
//			}
//		}
//	}
//	
//	public void printGraph()
//	{
//		cc.traverse();
//	}
//	
//	/* ************************************************************************************************	
//     *                                    PRIVATE METHODS                                             *
//     **************************************************************************************************/
//
//	private boolean isAssignableFromAnyCollectionAPI(Element clazz)
//	{
//		for(Element collectionClass : collectionClasses)
//		{
//			if(collectionClass.isAssignableFrom(clazz))
//			{
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private void initializeEnvironment()
//	{
//		collectionClasses.add(Iterable.class);
//		collectionClasses.add(Map.class);
//		bfsQueue = new ArrayDeque<>();
//		alreadyVisited = new ArrayDeque<>();
//	}
//	
//	private boolean isFieldStatic(Field field)
//	{
//		boolean toReturn = false;
//		if(java.lang.reflect.Modifier.isStatic(field.getModifiers()))
//		{
//			toReturn = true;
//		}
//		return toReturn;
//	}
//	
//	private void addAllRelevantFieldsOfClassToFinalCollection(Collection<Element> finalCollection, Element clazz)
//	{
//		if(clazz.getKind() != ElementKind.CLASS)
//		{
//			return;
//		}
//		if(finalCollection == null)
//		{
//			finalCollection = new ArrayList<>();
//		}	
//		for (Element enclosedElement : clazz.getEnclosedElements()) 
//		{
//            if (enclosedElement.getKind() == ElementKind.FIELD) 
//            {
//            	Set<Modifier> modifiers = enclosedElement.getModifiers();
//            	if(!modifiers.contains(Modifier.STATIC))
//            	{
//            		finalCollection.add(enclosedElement);
//            	}
//            }
//		}		
//	}
//}
