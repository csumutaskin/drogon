package org.drogonframework.mapping.processor;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
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

import org.drogonframework.mapping.core.ElementTypeTreeTraverser;

import com.google.auto.service.AutoService;

@SupportedAnnotationTypes("org.drogonframework.mapping.annotation.Converter")
@SupportedSourceVersion(SourceVersion.RELEASE_10)
@AutoService(Processor.class)
public class ConverterNewProcessor extends AbstractProcessor 
{
	public static final String LIST_CLASS = "java.util.List";
    public static final String MAP_CLASS = "java.util.Map";
    
	@SuppressWarnings("unused")
	private Filer filer;
	private Messager messager;
	
	private Types typeUtils;
	private Elements elementUtils;
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) 
	{
	    super.init(processingEnv);
	    filer = processingEnv.getFiler();
	    messager = processingEnv.getMessager();
	    
	    typeUtils = processingEnv.getTypeUtils();
	    elementUtils = processingEnv.getElementUtils();
	}
	
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment)
	{
		try
		{
			messager.printMessage(Kind.NOTE, "Started processing...........................................");
			if(annotations == null || annotations.isEmpty())
		{
			return false;
		}
		for(TypeElement annotation : annotations)
		{
			//messager.printMessage(Kind.NOTE, "Annotation found: " + annotation.getSimpleName());
			Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(annotation);
			if(elementsAnnotatedWith == null || elementsAnnotatedWith.isEmpty())
			{
				return false;
			}
			for(Element element : elementsAnnotatedWith)
			{
				ElementTypeTreeTraverser et = new ElementTypeTreeTraverser(elementUtils, typeUtils, messager);
				et.traverseUsingBFS(element);
				
//				messager.printMessage(Kind.NOTE, "Annotation is: " + annotation);
//				messager.printMessage(Kind.NOTE, "Annotation's class/interface type is: " + element.getKind());
//				messager.printMessage(Kind.NOTE, "Annotation's class short name is: " + element.getSimpleName());
//				messager.printMessage(Kind.NOTE, "Annotation's class modifiers is: " + element.getModifiers());				
//								
//				messager.printMessage(Kind.NOTE, "*****************************************************************");
//				printElements(element);
//				messager.printMessage(Kind.NOTE, "*****************************************************************");
//							
//				TypeElement t = (TypeElement)element;
//				
//				//messager.printMessage(Kind.NOTE, "Annotation's class full name is: " + t.getQualifiedName());
//				//messager.printMessage(Kind.NOTE, "......Super class...... " + t.getSuperclass());
//				
//				List<? extends TypeMirror> interfaces = t.getInterfaces();
//				for(TypeMirror interfac : interfaces)
//				{
//					//messager.printMessage(Kind.NOTE, ".....................interfaces.................." + interfac.getKind() + "---" + interfac);
//					
//					TypeMirror comparable = processingEnv.getElementUtils().getTypeElement("org.drogonframework.mapping.entity.ConvertibleDTO").asType();
//					boolean isComparable = processingEnv.getTypeUtils().isAssignable(interfac, comparable);
////									
////					if(isComparable)
////						messager.printMessage(Kind.NOTE, "!!!..................interfaces is of type ConvertibleDTO TRUE !!!!..............");
////					else
////						messager.printMessage(Kind.NOTE, "!!!..................interfaces is of type ConvertibleDTO FALSE !!!!..............");
//				}
////				if(element.getKind() != ElementKind.CLASS)
////				{
////					messager.printMessage(Kind.ERROR, "@Converter is a class level annotation only",element);
////				}
////				else
////				{
////					messager.printMessage(Kind.NOTE, "It is right to put this annotation as class level :)");
////				}
			}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			messager.printMessage(Kind.NOTE, e.getMessage());
		}
		return false;
	}
	

	
	private void printElements(Element classElement)
	{
		
		if(classElement.getKind() != ElementKind.CLASS)
		{
			return;
		}
		int counter = 1;
		for (Element enclosedElement : classElement.getEnclosedElements()) 
		{
			messager.printMessage(Kind.NOTE, "----------------------------- Field: "+ counter +"--------------------------------  element: " + enclosedElement.getSimpleName() + " type: " + enclosedElement.getKind());
            if (enclosedElement.getKind() == ElementKind.FIELD) 
            {
                Set<Modifier> modifiers = enclosedElement.getModifiers();
                StringBuilder sb = new StringBuilder();
                if (modifiers.contains(Modifier.PRIVATE)) 
                {
                    sb.append("private ");
                } 
                else if (modifiers.contains(Modifier.PROTECTED)) 
                {
                    sb.append("protected ");
                } 
                else if (modifiers.contains(Modifier.PUBLIC)) 
                {
                    sb.append("public ");
                }
                if (modifiers.contains(Modifier.STATIC))
                {
                    sb.append("static ");
                }
                if (modifiers.contains(Modifier.FINAL))
                {
                    sb.append("final ");
                }
                
                
                //TypeMirror listType = elementUtils.getTypeElement(LIST_CLASS).asType();
                //TypeMirror mapType = elementUtils.getTypeElement(MAP_CLASS).asType();
                
               // TypeMirror erasure = typeUtils.erasure(retType);
                
                //messager.printMessage(Kind.NOTE, "Mevcut element: " + enclosedElement.getSimpleName());
               // TypeMirror asType = elementUtils.getTypeElement(enclosedElement.getSimpleName()).asType();
                TypeMirror asType = enclosedElement.asType();
                //messager.printMessage(Kind.NOTE, "Type Erasure una bakilan element: " + enclosedElement.getSimpleName());
                //TypeMirror erasured = typeUtils.erasure(asType);
                TypeElement collectionTypes = elementUtils.getTypeElement("java.util.Collection");
                TypeMirror collectionMirror = collectionTypes.asType();

                if (typeUtils.isAssignable(asType, collectionMirror))
                {
                	messager.printMessage(Kind.NOTE, "Bu propertyn collection tipindedir: " + asType.toString());
                	TypeMirror genericType = ((DeclaredType) asType).getTypeArguments().get(0);
                	messager.printMessage(Kind.NOTE, "Bu propertynin Generic Type'i var: " + genericType);
                }
                if (asType.getKind() == TypeKind.ARRAY) 
                {
                	ArrayType asArrayType = (ArrayType) asType;
                	messager.printMessage(Kind.NOTE, "Bu property array tipinde ve su type tan olusuyor: " + asArrayType.getComponentType().toString());
                }
                
                //messager.printMessage(Kind.NOTE, "Erasure type i getirildi. Generic Type: " + genericType);
//                if(erasured != null)
//                {
//                	
//                	  messager.printMessage(Kind.NOTE, "Erasure type yazdiriliyor " + erasured.toString());
//                	sb.append("Generic Type: " + erasured.toString());
//                }
//                messager.printMessage(Kind.NOTE, "Erasure type i basarili process edildi");
                      
                sb.append(enclosedElement.asType()).append(" ").append(enclosedElement.getSimpleName());
                messager.printMessage(Kind.NOTE, sb.toString());
            }
            counter++;
        }
	}
}
