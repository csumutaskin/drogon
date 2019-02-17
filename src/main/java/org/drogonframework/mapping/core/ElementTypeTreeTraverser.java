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

public class ElementTypeTreeTraverser 
{
	private static final String OBJECT_CLASS_WITH_PACKAGE_NAME = "java.lang.Object";
	private static final String COLLECTION_CLASS_WITH_PACKAGE_NAME = "java.util.Collection";
	private static final String MAP_CLASS_WITH_PACKAGE_NAME = "java.util.Map";
	
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
		
		cc = new ElementTypeTreeToElementTypeGraphConverter(clazz, messager);
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
		//messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() +" retrieved from queue");
		alreadyVisited.add(clazz);
		Collection<Element> allDeclaredFieldsOfClazz = new ArrayList<>();
		// TODO: getter a gore mi, direkt field okuma mi?
		addAllRelevantFieldsOfClassToFinalCollection(allDeclaredFieldsOfClazz, clazz);

		TypeElement t = (TypeElement)clazz;
		
		TypeMirror superClass = t.getSuperclass();
				
		DeclaredType superClassAsDeclared = (DeclaredType)superClass; 
		Element superClassasElement = superClassAsDeclared.asElement();
		
		while(superClassasElement != null && !superClassasElement.toString().equals(OBJECT_CLASS_WITH_PACKAGE_NAME))
		{
			//messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + ", SuperClass: " + superClassasElement.toString());
			addAllRelevantFieldsOfClassToFinalCollection(allDeclaredFieldsOfClazz, superClassasElement);
			
			t = (TypeElement)superClassasElement;
			superClass = t.getSuperclass();
			superClassAsDeclared = (DeclaredType)superClass; 
			superClassasElement = superClassAsDeclared.asElement();
		}
		if(allDeclaredFieldsOfClazz.isEmpty())
		{
			//messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " has no instance variable!");
			return;
		}
		for(Element childTypeInTurn: allDeclaredFieldsOfClazz)
		{			
			//messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + ", Field: " + childTypeInTurn.toString() + ", Type: " + childTypeInTurn.asType());
			Element mostInnerElement = extractMostInnerElement(childTypeInTurn.asType());
			if(mostInnerElement != null)
			{
				//messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " Field++: " + mostInnerElement.toString());
				cc.addEdge(clazz, mostInnerElement);//TODO eger onceden mevcutsa bu edge i ekleme bir daha kontrol et				
        		bfsQueue.offer(mostInnerElement);//TODO eger onceden mevcutsa bu edge i ekleme bir daha kontrol et
			}
//			else
// 			{
// 				messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + " Field of uppermost is null--: " + childTypeInTurn.toString());
// 			}
		}
	}	
	
	public void printGraph()
	{
		cc.traverse(messager);
	}
	
	/* ************************************************************************************************	
     *                                    PRIVATE METHODS                                             *
     **************************************************************************************************/

	private Element extractMostInnerElement(TypeMirror typeMirror)//TODO: en ic elemanina kadar extract et..
	{
		TypeMirror toReturn = typeMirror;
		//messager.printMessage(Kind.NOTE, "EXTRACTING MOST INNER FOR ELEMENT: " + toReturn.toString() + " KIND: " + toReturn.getKind());
		extraction:while(toReturn != null && (isArray(toReturn) || isAssignableFromAnyCollectionAPI(toReturn)))
		{			
			while(isArray(toReturn))
			{
				ArrayType asArrayType = (ArrayType) toReturn;
				toReturn = asArrayType.getComponentType();
			}
			while(isAssignableFromAnyCollectionAPI(toReturn))
			{
				List<? extends TypeMirror> typeArguments = null;
				DeclaredType dt = (DeclaredType)toReturn;
				typeArguments = dt.getTypeArguments();
				if(typeArguments != null && typeArguments.size() > 0)
     			{
    				toReturn = typeArguments.get(0);
    				continue extraction;
     			}
				else
				{
					toReturn = null;					
				}
			}				
		}
		if(isDeclaredType(toReturn))
		{
			//messager.printMessage(Kind.NOTE, "EXTRACTED INNER FOR ELEMENT: " + toReturn.toString() + ", KIND: " + toReturn.getKind());
			return ((DeclaredType)toReturn).asElement();
		}
		else if(isPrimitive(toReturn))
		{
			//messager.printMessage(Kind.NOTE, "EXTRACTED INNER FOR ELEMENT(PRIMITIVE): " + toReturn.toString() + ", KIND: " + toReturn.getKind());
			return null;
		}
		else
		{
			//String message =  (toReturn != null)? toReturn.getKind().toString() : null;
			//messager.printMessage(Kind.NOTE, "EXTRACTED INNER FOR ELEMENT: NULL, KIND: " + message);
			return null;
		}
	}
			
	private void initializeEnvironment()
	{
        TypeMirror collectionTypes = elementUtils.getTypeElement(COLLECTION_CLASS_WITH_PACKAGE_NAME).asType();
        TypeMirror mapTypes = elementUtils.getTypeElement(MAP_CLASS_WITH_PACKAGE_NAME).asType();
		
		collectionClasses.add(collectionTypes);
		collectionClasses.add(mapTypes);
				
		bfsQueue = new ArrayDeque<>();
		alreadyVisited = new ArrayDeque<>();
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
            if (enclosedElement.getKind() == ElementKind.FIELD && !isFieldStatic(enclosedElement)) 
            {
           		//messager.printMessage(Kind.NOTE, "Class: " + clazz.toString() + ", ADDING: " + enclosedElement + ", TYPE (UTILS): " + typeUtils.asElement(enclosedElement.asType()) + ", TYPE NO CAST: " + enclosedElement.asType() + ", IS PRIMITIVE: " + isPrimitive(enclosedElement.asType()));
           		finalCollection.add(enclosedElement);
            }
		}		
	}

	/*
	 * Checks whether current element contains static modifier or not
	 */
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
	
	/*
	 * Checks whether current type mirror is assignable from collection classes (Currently supports Collection and Map classes)
	 */
	private boolean isAssignableFromAnyCollectionAPI(TypeMirror typeMirror)
	{
		if(typeMirror != null)
		{
			for(TypeMirror collectionClass : collectionClasses)
			{
				if(typeUtils.isAssignable(typeMirror, collectionClass))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/*
	 * Checks whether current type mirror is declared type
	 */
	private boolean isDeclaredType(TypeMirror typeMirror)
	{		
		if(typeMirror != null && typeMirror.getKind() == TypeKind.DECLARED)
		{
			return true;
		}
		return false;
	}
	
	/*
	 * Checks whether current type mirror is array type
	 */
	private boolean isArray(TypeMirror typeMirror)
	{
		if (typeMirror != null && typeMirror.getKind() == TypeKind.ARRAY) 
		{
			return true;
		}
		return false;
	}
	
	/*
	 * Checks whether current type mirror is primitive
	 */
	private boolean isPrimitive(TypeMirror mirror)
	{
		if(mirror == null)
		{
			return false;
		}
		return mirror.getKind().isPrimitive();
	}
}
