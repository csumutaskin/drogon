package org.drogonframework.mapping.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

//TODO: Arraylerin her bir elemaninin da cekilip collectionlara eklenmesi lazim ki arraylerde de cyclic kontrol edilebilsin
public class ElementTypeTreeTraverser 
{
	private Deque<Element> bfsQueue = new ArrayDeque<>();
	private Deque<Element> alreadyVisited = new ArrayDeque<>();
	
	private List<TypeMirror> collectionClasses = new ArrayList<>();  
	
	private ElementTypeTreeToElementTypeGraphConverter cc;
	
	private Elements elementUtils;
	private Types typeUtils;
	private Messager messager;

	public ElementTypeTreeTraverser(Elements elementUtils, Types typeUtils, Messager messager)
	{		
		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
		this.messager = messager;
	}
		
	/**
	 * Tries to detect if there is a cyclic association or not
	 * @param clazz
	 * @return
	 */
	public boolean traverseUsingBFS(Element clazz)
	{
		initializeEnvironment();
		bfsQueue.offer(clazz);
		
		cc = new ElementTypeTreeToElementTypeGraphConverter(clazz);
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
		Element clazz = bfsQueue.poll();
		if(clazz == null)
		{
			return;
		}
		messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() +" retrieved from queue");

		alreadyVisited.add(clazz);
		Collection<Element> allDeclaredFieldsOfClazz = new ArrayList<>();
		// TODO: getter a gore mi, direkt field okuma mi?
		addAllRelevantFieldsOfClassToFinalCollection(allDeclaredFieldsOfClazz, clazz);

		TypeElement t = (TypeElement)clazz;
		
		TypeMirror superClass = t.getSuperclass();
				
		DeclaredType superClassAsDeclared = (DeclaredType)superClass; 
		Element superClassasElement = superClassAsDeclared.asElement();
		
		while(superClassasElement != null && !superClassasElement.toString().equals("java.lang.Object"))
		{
			messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + ", SuperClass: " + superClassasElement.toString());
			addAllRelevantFieldsOfClassToFinalCollection(allDeclaredFieldsOfClazz, superClassasElement);
			
			t = (TypeElement)superClassasElement;
			superClass = t.getSuperclass();
			superClassAsDeclared = (DeclaredType)superClass; 
			superClassasElement = superClassAsDeclared.asElement();
		}
		messager.printMessage(Kind.NOTE, "---------------------------------------------------------------------------------------");
		if(allDeclaredFieldsOfClazz.isEmpty())
		{
			messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " has no instance variable!");
			return;
		}
		for(Element childTypeInTurn: allDeclaredFieldsOfClazz)
		{			
			messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + ", Field: " + childTypeInTurn.toString() + ", Type: " + childTypeInTurn.asType());
			if(isAssignableFromAnyCollectionAPI(childTypeInTurn))
			{
				//TODO modify below code for this local to be added
				Element innerMostElement = extractMostInnerElement(childTypeInTurn);
				
//				//TODO map ler i diger collectionlardan ayirip key value olarak dusunmek gerekir mi ??
//     			DeclaredType dt = (DeclaredType)childTypeInTurn.asType();
//				//TypeMirror genericType = ((DeclaredType) childTypeInTurn.asType()).getTypeArguments().get(0);
//     			List<? extends TypeMirror> typeArguments = dt.getTypeArguments();
//     			if(typeArguments != null && typeArguments.size() > 0)
//     			{
//     				TypeMirror genericType = typeArguments.get(0);
//     				DeclaredType currentGenericType = (DeclaredType)genericType;
//     				messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " Field++: " + currentGenericType.asElement().toString() + " (GENERIC)");
//     				cc.addEdge(clazz, currentGenericType.asElement());
//     				bfsQueue.offer(currentGenericType.asElement());
//     			}
//     			else
//     			{
//     				messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " Field--: " + childTypeInTurn.asType() + " (COLL WITH NO GENERIC)");
//     			}
     			
     			
     			
     			if(innerMostElement != null)
     			{
     				messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " Field++: " + innerMostElement.toString() + " (GENERIC)");
     				cc.addEdge(clazz, innerMostElement);
     				bfsQueue.offer(innerMostElement);
     			}
     			else
     			{
     				messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " Field of uppermost is null--: " + childTypeInTurn.toString() + " (GENERIC NO TYPE)");
     			}
				
			}
			else if(isArray(childTypeInTurn))
			{
				//TODO modify below code for this local to be added
				Element innerMostElement = extractMostInnerElement(childTypeInTurn);
				
//	           	ArrayType asArrayType = (ArrayType) childTypeInTurn.asType();
//            	DeclaredType typeOfArray = (DeclaredType)asArrayType.getComponentType();
//            	if(typeOfArray != null)
//            	{
//            		messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " Field++: " + typeOfArray.asElement().toString() + " (ARRAY)");
//            		cc.addEdge(clazz, typeOfArray.asElement());
//            		bfsQueue.offer(typeOfArray.asElement());
//            	}
				
				if(innerMostElement != null)
				{
					messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " Field++: " + innerMostElement.toString() + " (ARRAY)");
            		cc.addEdge(clazz, innerMostElement);
            		bfsQueue.offer(innerMostElement);
				}
			}
			else
			{
				if(childTypeInTurn != null)
				{
					//Element typeOfElement = typeUtils.asElement(childTypeInTurn.asType());
					//messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " Field++: " + typeOfElement.toString() + " (NORMAL)");
					
					if(!isPrimitive(childTypeInTurn))
					{
						messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " Field++: " + typeUtils.asElement(childTypeInTurn.asType()).toString() + " (NORMAL)");
						cc.addEdge(clazz, childTypeInTurn);
						bfsQueue.offer(childTypeInTurn);
						//cc.addEdge(clazz, typeOfElement);
						//bfsQueue.offer(typeOfElement);	
					}
					else//primitive type
					{
						messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " Field--: " + childTypeInTurn.asType() + " (PRIMITIVE: " + childTypeInTurn + ")");
					}					
				}
			}
		}
	}
	
	public void printGraph()
	{
		cc.traverse(messager);
	}
	
	/* ************************************************************************************************	
     *                                    PRIVATE METHODS                                             *
     **************************************************************************************************/

	/*
	 * Extracts the inner most element from nested collection or arrays
	 */
	private Element extractMostInnerElement(Element element)//TODO: en ic elemanina kadar extract et..
	{
		Element toReturn = element;
		messager.printMessage(Kind.NOTE, "EXTRACTING MOST INNER FOR ELEMENT: " + toReturn.toString());
		while(toReturn != null && (isArray(toReturn) || isAssignableFromAnyCollectionAPI(toReturn)))
		{
			if(isArray(toReturn))
			{
				ArrayType asArrayType = (ArrayType) toReturn.asType();
				DeclaredType typeOfArray = (DeclaredType)asArrayType.getComponentType();
            	if(typeOfArray != null)
            	{
            		messager.printMessage(Kind.NOTE, "EXTRACTING MOST INNER: " + typeOfArray.asElement().toString() + " (ARRAY)");
            		toReturn = typeOfArray.asElement();
            	}
			}
			else if(isAssignableFromAnyCollectionAPI(toReturn))
			{
				
				DeclaredType dt = (DeclaredType)toReturn.asType();
     			List<? extends TypeMirror> typeArguments = dt.getTypeArguments();
     			if(typeArguments != null && typeArguments.size() > 0)
     			{
     				TypeMirror genericType = typeArguments.get(0);
     				DeclaredType currentGenericType = (DeclaredType)genericType;
     				messager.printMessage(Kind.NOTE, "EXTRACTING MOST INNER: " + currentGenericType.asElement().toString() + " (GENERIC)");
     				toReturn = currentGenericType.asElement();
     			}
     			else
     			{
     				messager.printMessage(Kind.NOTE, "EXTRACTING MOST INNER: " + toReturn.toString() + " (NO GENERIC TYPE - SETTING RETURN TO NULL)");
     				toReturn = null;
     				//TODO: get precaution, maybe return null...
     			}
			}
		}
		messager.printMessage(Kind.NOTE, "EXTRACTED MOST INNER IS: " + toReturn);
		return toReturn;
	}
	
	private boolean isAssignableFromAnyCollectionAPI(Element clazz)
	{
		for(TypeMirror collectionClass : collectionClasses)
		{
			if(typeUtils.isAssignable(clazz.asType(), collectionClass))
			{
				return true;
			}
		}
		return false;
	}
	
	private boolean isArray(Element element)
	{
		if (element.asType().getKind() == TypeKind.ARRAY) 
		{
			return true;
		}
		return false;
	}

	private void initializeEnvironment()
	{
        TypeMirror collectionTypes = elementUtils.getTypeElement("java.util.Collection").asType();
        TypeMirror mapTypes = elementUtils.getTypeElement("java.util.Map").asType();
		
		collectionClasses.add(collectionTypes);
		collectionClasses.add(mapTypes);
				
		bfsQueue = new ArrayDeque<>();
		alreadyVisited = new ArrayDeque<>();
	}
	
	private boolean isFieldStatic(Element enclosedElement)
	{
		boolean toReturn = false;
		Set<Modifier> modifiers = enclosedElement.getModifiers();
		if(modifiers != null && modifiers.contains(Modifier.STATIC))
		{
			toReturn = true;
		}
		return toReturn;
	}
	
	private void addAllRelevantFieldsOfClassToFinalCollection(Collection<Element> finalCollection, Element clazz)
	{		
		if(clazz.getKind() != ElementKind.CLASS)//TODO: interface should be checked here too...
		{
			return;
		}
		if(finalCollection == null)
		{
			finalCollection = new ArrayList<>();
		}	
		for (Element enclosedElement : clazz.getEnclosedElements()) 
		{
            if (enclosedElement.getKind() == ElementKind.FIELD) 
            {
            	Set<Modifier> modifiers = enclosedElement.getModifiers();
            	if(!modifiers.contains(Modifier.STATIC))
            	{
            		//finalCollection.add(typeUtils.asElement(enclosedElement.asType())); //Change this until it is INTERFACE or CLASS kind.. Kind should be FIELD to be added to queue
            		
            		messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + ", ADDING: " + enclosedElement + ", TYPE (UTILS): " + typeUtils.asElement(enclosedElement.asType()) + ", TYPE NO CAST: " + enclosedElement.asType() + ", IS PRIMITIVE: " + isPrimitive(enclosedElement));
            		finalCollection.add(enclosedElement);
            	}
            }
		}		
	}
	
	private boolean isPrimitive(Element field)
	{
		if(field == null)
		{
			return false;
		}
		return field.asType().getKind().isPrimitive();
	}
}
