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
		messager.printMessage(Kind.NOTE, "Constructing ElementTypeTreeTraverser");
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
		messager.printMessage(Kind.NOTE, "Before initializing environment");
		initializeEnvironment();
		messager.printMessage(Kind.NOTE, "Before putting first class to queue");
		bfsQueue.offer(clazz);
		
		cc = new ElementTypeTreeToElementTypeGraphConverter(clazz);
		messager.printMessage(Kind.NOTE, "Start visiting queue");
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
		messager.printMessage(Kind.NOTE, "In visit method...");
		Element clazz = bfsQueue.poll();
		messager.printMessage(Kind.NOTE, "Getting element from queue: " + clazz.toString());
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
		
		while(superClassasElement != null && !superClassasElement.toString().equals("java.lang.Object"))
		{
			messager.printMessage(Kind.NOTE, clazz.toString() + " classinin super classi var: " + superClassasElement.toString());
			addAllRelevantFieldsOfClassToFinalCollection(allDeclaredFieldsOfClazz, superClassasElement);
			
			t = (TypeElement)superClassasElement;
			superClass = t.getSuperclass();
			superClassAsDeclared = (DeclaredType)superClass; 
			superClassasElement = superClassAsDeclared.asElement();
		}
		if(allDeclaredFieldsOfClazz.isEmpty())
		{
			messager.printMessage(Kind.NOTE, clazz.toString() + " classinin hic bir tanimli fieldi yok...");
			return;
		}
		for(Element childTypeInTurn: allDeclaredFieldsOfClazz)
		{
			messager.printMessage(Kind.NOTE, clazz.toString() + " classinin fieldlari process edilecek. Siradaski field: " + childTypeInTurn.toString());
			if(isAssignableFromAnyCollectionAPI(childTypeInTurn))
			{
				messager.printMessage(Kind.NOTE, clazz.toString() + " classinin collection olan fieldi " + childTypeInTurn.toString() +" generic tipi var");
				//TODO map ler i diger collectionlardan ayirip key value olarak dusunmek gerekir mi ??
     			DeclaredType dt = (DeclaredType)childTypeInTurn.asType();
				messager.printMessage(Kind.NOTE, "bunun birazdan generic type i alinacak" + dt);//java.util.list<E>
				messager.printMessage(Kind.NOTE, "generic type lar: " + dt.getTypeArguments());
				//TypeMirror genericType = ((DeclaredType) childTypeInTurn.asType()).getTypeArguments().get(0);
				TypeMirror genericType = dt.getTypeArguments().get(0);
				messager.printMessage(Kind.NOTE, "generic type lardan ilki: " + genericType.toString());//<E>
				DeclaredType currentGenericType = (DeclaredType)genericType;
				messager.printMessage(Kind.NOTE, clazz.toString() + " classinin genericli fieldi " + childTypeInTurn.toString() +" generici: " + currentGenericType + " dir...");
				cc.addEdge(clazz, currentGenericType.asElement());
				bfsQueue.offer(currentGenericType.asElement());
				
			}
			if(isArray(childTypeInTurn))
			{
				messager.printMessage(Kind.NOTE, clazz.toString() + " classinin array tipi olan fieldi: " + childTypeInTurn.toString());
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
					messager.printMessage(Kind.NOTE, clazz.toString() + " classinin fieldi queue ya eklenecek: " + childTypeInTurn.toString());
					cc.addEdge(clazz, childTypeInTurn);
					bfsQueue.offer(childTypeInTurn);
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

	private boolean isAssignableFromAnyCollectionAPI(Element clazz)
	{
		messager.printMessage(Kind.NOTE, clazz.toString() + " classi collection tipinde mi bakilacak");
		for(TypeMirror collectionClass : collectionClasses)
		{
			if(typeUtils.isAssignable(clazz.asType(), collectionClass))
			{
				messager.printMessage(Kind.NOTE, clazz.toString() + " classi collection tipindeymis evet...");
				return true;
			}
		}
		messager.printMessage(Kind.NOTE, clazz.toString() + " classi collection tipinde degil, false donecek");
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
            		messager.printMessage(Kind.NOTE, " " + clazz.toString() + " classinin listeye eklenen alt elemani: " + enclosedElement.toString() + " tipin class versiyonu: " + typeUtils.asElement(enclosedElement.asType()));//TODO burda sample2 diyor classi koymasi lazim...
            		//finalCollection.add(enclosedElement);
            		//finalCollection.add(typeUtils.asElement(enclosedElement.asType())); BU INTERFACE VE CLASS OLARAK EKLIYOR FIELD OLARAK EKLE
            		finalCollection.add(enclosedElement);
            	}
            }
		}		
	}
}
