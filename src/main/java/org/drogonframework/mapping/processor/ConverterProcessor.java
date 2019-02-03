//package org.drogonframework.mapping.processor;
//
//import java.util.List;
//import java.util.Set;
//
//import javax.annotation.processing.AbstractProcessor;
//import javax.annotation.processing.Filer;
//import javax.annotation.processing.Messager;
//import javax.annotation.processing.ProcessingEnvironment;
//import javax.annotation.processing.Processor;
//import javax.annotation.processing.RoundEnvironment;
//import javax.annotation.processing.SupportedAnnotationTypes;
//import javax.annotation.processing.SupportedSourceVersion;
//import javax.lang.model.SourceVersion;
//import javax.lang.model.element.Element;
//import javax.lang.model.element.ElementKind;
//import javax.lang.model.element.TypeElement;
//import javax.lang.model.type.TypeMirror;
//import javax.tools.Diagnostic.Kind;
//
//import org.drogonframework.mapping.core.ClassTypeTreeTraverser;
//
//import com.google.auto.service.AutoService;
//
//@SupportedAnnotationTypes("org.drogonframework.mapping.annotation.Converter")
//@SupportedSourceVersion(SourceVersion.RELEASE_10)
//@AutoService(Processor.class)
//public class ConverterProcessor extends AbstractProcessor 
//{
//	@SuppressWarnings("unused")
//	private Filer filer;
//	private Messager messager;
//	
//	@Override
//	public synchronized void init(ProcessingEnvironment processingEnv) 
//	{
//	    super.init(processingEnv);
//	    filer = processingEnv.getFiler();
//	    messager = processingEnv.getMessager();
//	}
//	
//	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment)
//	{
//		try
//		{
//		messager.printMessage(Kind.NOTE, "Started processing...");
//		if(annotations == null || annotations.isEmpty())
//		{
//			return false;
//		}
//		for(TypeElement annotation : annotations)
//		{
//			messager.printMessage(Kind.NOTE, "Annotation found: " + annotation.getSimpleName());
//			Set<? extends Element> elementsAnnotatedWith = roundEnvironment.getElementsAnnotatedWith(annotation);
//			if(elementsAnnotatedWith == null || elementsAnnotatedWith.isEmpty())
//			{
//				return false;
//			}
//			for(Element element : elementsAnnotatedWith)
//			{
//				messager.printMessage(Kind.NOTE, "Annotation is: " + annotation);
//				messager.printMessage(Kind.NOTE, "Annotation's class/interface type is: " + element.getKind());
//				messager.printMessage(Kind.NOTE, "Annotation's class short name is: " + element.getSimpleName());
//				messager.printMessage(Kind.NOTE, "Annotation's class modifiers is: " + element.getModifiers());				
//								
//				TypeElement t = (TypeElement)element;
//				
//				messager.printMessage(Kind.NOTE, "Annotation's class full name is: " + t.getQualifiedName());
//				
//				
//				ClassTypeTreeTraverser cgd = new ClassTypeTreeTraverser();
//				
//				try
//				{
//					cgd.traverseUsingBFS(Class.forName(t.getQualifiedName().toString()));
//				}
//				catch(RuntimeException re)
//				{
//					messager.printMessage(Kind.ERROR, "Runtime Exception in class hierarchy" + re.getMessage());
//				}
////				cgd.printGraph();
//				
//				
//				
//				
//				messager.printMessage(Kind.NOTE, "......Super class...... " + t.getSuperclass());
//				
//				List<? extends TypeMirror> interfaces = t.getInterfaces();
//				for(TypeMirror interfac : interfaces)
//				{
//					messager.printMessage(Kind.NOTE, ".....................interfaces.................." + interfac.getKind() + "---" + interfac);
//					
//					TypeMirror comparable = processingEnv.getElementUtils().getTypeElement("org.drogonframework.mapping.entity.ConvertibleDTO").asType();
//					boolean isComparable = processingEnv.getTypeUtils().isAssignable(interfac, comparable);
//									
//					if(isComparable)
//						messager.printMessage(Kind.NOTE, "!!!..................interfaces is of type ConvertibleDTO TRUE !!!!..............");
//					else
//						messager.printMessage(Kind.NOTE, "!!!..................interfaces is of type ConvertibleDTO FALSE !!!!..............");
//				}
//				if(element.getKind() != ElementKind.CLASS)
//				{
//					messager.printMessage(Kind.ERROR, "@Converter is a class level annotation only",element);
//				}
//				else
//				{
//					messager.printMessage(Kind.NOTE, "It is right to put this annotation as class level :)");
//				}
//			}
//			}
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//			messager.printMessage(Kind.NOTE, e.getMessage());
//		}
//		return false;
//	}
//}
