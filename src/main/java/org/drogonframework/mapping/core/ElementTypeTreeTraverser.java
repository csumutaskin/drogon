package org.drogonframework.mapping.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Set;

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

//TODO: Arraylerin her bir elemaninin da cekilip collectionlara eklenmesi lazim ki arraylerde de cyclic kontrol edilebilsin
public class ElementTypeTreeTraverser 
{
	private Deque<Element> bfsQueue = new ArrayDeque<>();
	private Deque<Element> alreadyVisited = new ArrayDeque<>();
	
	private List<TypeMirror> collectionClasses = new ArrayList<>();  
	
	private ElementTypeTreeToElementTypeGraphConverter cc;
	
	private Elements elementUtils;
	private Types typeUtils;

	public ElementTypeTreeTraverser(Elements elementUtils, Types typeUtils)
	{
		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
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

		alreadyVisited.add(clazz);
		Collection<Element> allDeclaredFieldsOfClazz = new ArrayList<>();
		// TODO: getter a gore mi, direkt field okuma mi?
		addAllRelevantFieldsOfClassToFinalCollection(allDeclaredFieldsOfClazz, clazz);

		TypeElement t = (TypeElement)clazz;
		
		TypeMirror superClass = t.getSuperclass();
				
		DeclaredType superClassAsDeclared = (DeclaredType)superClass; 
		Element superClassasElement = superClassAsDeclared.asElement();
		
		while(superClassasElement != null)
		{
			addAllRelevantFieldsOfClassToFinalCollection(allDeclaredFieldsOfClazz, superClassasElement);
			
			t = (TypeElement)superClassasElement;
			superClass = t.getSuperclass();
			superClassAsDeclared = (DeclaredType)superClass; 
			superClassasElement = superClassAsDeclared.asElement();
		}
		if(allDeclaredFieldsOfClazz.isEmpty())
		{
			return;
		}
		for(Element childTypeInTurn: allDeclaredFieldsOfClazz)
		{
			if(isAssignableFromAnyCollectionAPI(childTypeInTurn))
			{
				//TODO map ler i diger collectionlardan ayirip key value olarak dusunmek gerekir mi ??
				TypeMirror genericType = ((DeclaredType) childTypeInTurn.asType()).getTypeArguments().get(0);
				DeclaredType currentGenericType = (DeclaredType)genericType;
				cc.addEdge(clazz, currentGenericType.asElement());
				bfsQueue.offer(currentGenericType.asElement());
				
			}
			if(isArray(childTypeInTurn))
			{
            	ArrayType asArrayType = (ArrayType) childTypeInTurn.asType();
            	DeclaredType typeOfArray = (DeclaredType)asArrayType.getComponentType();
            	if(typeOfArray != null)
            	{
            		cc.addEdge(clazz, typeOfArray.asElement());
            		bfsQueue.offer(typeOfArray.asElement());
            	}
				
			}
			else
			{
				if(childTypeInTurn != null)
				{
					cc.addEdge(clazz, childTypeInTurn);
					bfsQueue.offer(childTypeInTurn);
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
		if(clazz.getKind() != ElementKind.CLASS)
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
            		finalCollection.add(enclosedElement);
            	}
            }
		}		
	}
}
