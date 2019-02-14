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
import javax.lang.model.element.TypeParameterElement;
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
     				cc.addEdge(clazz, innerMostElement);//TODO eger onceden mevcutsa bu edge i ekleme bir daha kontrol et
     				bfsQueue.offer(innerMostElement);//TODO eger onceden mevcutsa bu edge i ekleme bir daha kontrol et
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
            		cc.addEdge(clazz, innerMostElement);//TODO eger onceden mevcutsa bu edge i ekleme bir daha kontrol et
            		bfsQueue.offer(innerMostElement);//TODO eger onceden mevcutsa bu edge i ekleme bir daha kontrol et
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
						cc.addEdge(clazz, typeUtils.asElement(childTypeInTurn.asType()));//TODO eger onceden mevcutsa bu edge i ekleme bir daha kontrol et
						bfsQueue.offer(typeUtils.asElement(childTypeInTurn.asType()));//TODO eger onceden mevcutsa bu edge i ekleme bir daha kontrol et
						//cc.addEdge(clazz, childTypeInTurn);
						//bfsQueue.offer(childTypeInTurn);
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
		TypeMirror mirrorInLoop = null;
		messager.printMessage(Kind.NOTE, "EXTRACTING MOST INNER FOR ELEMENT: " + toReturn.toString() + " KIND: " + toReturn.getKind());
		while(toReturn != null && (isArray(toReturn) || isAssignableFromAnyCollectionAPI(toReturn)))
		{			
			if(isArray(toReturn))
			{
				if(toReturn.getKind() == ElementKind.FIELD)
				{
				messager.printMessage(Kind.NOTE, "ARRAY ARRAY ARRAY  1111");
					ArrayType asArrayType = (ArrayType) toReturn.asType();
					TypeMirror componentType = asArrayType.getComponentType();
					messager.printMessage(Kind.NOTE, "ARRAY ARRAY ARRAY : " + componentType + " (!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!)" + componentType.getKind() + " KIND: " + typeUtils.asElement(componentType).getKind());
					mirrorInLoop = componentType;
					toReturn = typeUtils.asElement(componentType);
					
					//Previous code below
//					DeclaredType typeOfArray = (DeclaredType)asArrayType.getComponentType();
//					if(typeOfArray != null)
//					{
//						messager.printMessage(Kind.NOTE, "EXTRACTING MOST INNER: " + typeOfArray.asElement().toString() + " (ARRAY)");
//						toReturn = typeOfArray.asElement();
//					}
					//Previous code above
				}
				else if(toReturn.getKind() == ElementKind.CLASS || toReturn.getKind() == ElementKind.INTERFACE)
				{
					ArrayType asArrayType = (ArrayType) toReturn.asType();
					TypeMirror componentType = asArrayType.getComponentType();
					messager.printMessage(Kind.NOTE, "SU AN TORETURN CLASS/INTERFACE TYPE OLDUGUNDAN BURADA: " + toReturn + " KIND: " + toReturn.getKind());
				}
			}
			else if(isAssignableFromAnyCollectionAPI(toReturn))
			{
				messager.printMessage(Kind.NOTE, "Su anki nested generic tipi : " + toReturn.getKind() + " ToString: " + toReturn.toString());
				List<? extends TypeMirror> typeArguments = null;
				if(toReturn.getKind() == ElementKind.FIELD)
				{
					DeclaredType dt = (DeclaredType)toReturn.asType();
					typeArguments = dt.getTypeArguments();
				}
				else if(toReturn.getKind() == ElementKind.CLASS || toReturn.getKind() == ElementKind.INTERFACE)
				{
//					List<TypeParameterElement> enclosedElements = (List<TypeParameterElement>)((TypeElement)toReturn).getTypeParameters();	
					
					//!!!!!!!!!!!!!!!!! BURDAYIM private List<List<String>> nestedListWithGenerics icin hala generic olarak  <E> aliyoruz.. duzelt !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//					toReturn= enclosedElements.get(0).getGenericElement();//TODO: burasi type parameter element list olmayabilir degisik nesnelerde (ust satirdaki atama) incele
					
					DeclaredType dtype = (DeclaredType)mirrorInLoop;
					List<? extends TypeMirror> typeArguments2 = dtype.getTypeArguments();
					if(typeArguments2 != null && typeArguments2.size() >0)
					{
						TypeMirror typeMirror = typeArguments2.get(0);
						DeclaredType currentTypeInLoop = (DeclaredType)typeMirror;
						
						toReturn = currentTypeInLoop.asElement();
						mirrorInLoop = typeMirror;
						messager.printMessage(Kind.NOTE, "COLLECTION LOOP INTERFACE TIPINDE: " + mirrorInLoop);
						continue;
					}
				}
					
     			if(typeArguments != null && typeArguments.size() > 0)
     			{
     				TypeMirror genericType = typeArguments.get(0);
     				DeclaredType currentGenericType = (DeclaredType)genericType;
     				messager.printMessage(Kind.NOTE, "EXTRACTING MOST INNER: " + currentGenericType.asElement().toString() + " (GENERIC)");
     				
     				
     				
     				if("nestedListWithGenerics".equals(toReturn.toString()))
     				{
     					messager.printMessage(Kind.NOTE, "***????***: Nested enclosing element: " + element.getEnclosingElement());
     					messager.printMessage(Kind.NOTE, "***????***: " + currentGenericType.asElement().toString() + ", BU DA ONEMLI: " + currentGenericType.getEnclosingType());
     					messager.printMessage(Kind.NOTE, "***????***: " + typeUtils.asElement(currentGenericType));
     					messager.printMessage(Kind.NOTE, "***????***: " + currentGenericType.getTypeArguments().get(0));
     					messager.printMessage(Kind.NOTE, "***????***: " + genericType + " !!!!! " + typeUtils.asElement(genericType));
     					
     					
     					
//     					messager.printMessage(Kind.NOTE, "***????***: " + ((TypeElement)element).getTypeParameters());
//     					messager.printMessage(Kind.NOTE, "***????***: " + ((TypeElement)genericType).getTypeParameters());
//     					messager.printMessage(Kind.NOTE, "***????***: " + ((TypeElement)currentGenericType).getTypeParameters());
     				}
     				
     				mirrorInLoop = genericType;
     				toReturn = currentGenericType.asElement(); //Buraya generictype (TypeMirror) to Element gelmesi lazim, generic bir tek orada korunuyor. typeUtils classi aldigi icin bozuyor.
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
